/*
 * MIT License
 *
 * Copyright (c) 2020 Wesley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dirk.commands.tournament.match;

import com.dirk.helper.EmbedHelper;
import com.dirk.helper.TournamentHelper;
import com.dirk.models.GoogleSpreadsheetAuthenticator;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandArgument;
import com.dirk.models.command.CommandArgumentType;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.tournament.Tournament;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TakeAsRefereeCommand extends Command {
    private final TournamentRepository tournamentRepository;

    @Autowired
    public TakeAsRefereeCommand(TournamentRepository tournamentRepository) {
        this.commandName = "takeasreferee";
        this.description = "Take a match as a referee";
        this.group = "Tournament management";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("match id", "The id of the match to take", CommandArgumentType.String));

        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        Tournament existingTournament = TournamentHelper.getRunningTournament(messageCreateEvent, tournamentRepository);

        if (existingTournament == null) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("There is no tournament running in this server.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        CommandParameter commandParamMatchId = commandParams.stream().findFirst().orElse(null);

        // Prevent empty input
        if (commandParamMatchId == null || commandParamMatchId.getValue() == "") {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed(this.getCommandHelpFormat(), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        try {
            String spreadsheetId = TournamentHelper.getSpreadsheetIdFromUrl(existingTournament.getSpreadsheet());
            GoogleSpreadsheetAuthenticator authenticator = new GoogleSpreadsheetAuthenticator(spreadsheetId);

            List<List<Object>> allMatchIds = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getMatchIdRow());

            // Loop through all matches
            for (int i = 0; i < allMatchIds.size(); i++) {
                List<Object> currentMatch = allMatchIds.get(i);
                String matchId = (String) currentMatch.stream().findFirst().orElse(null);

                // Check if the match exists, should always be true
                if (matchId != null) {
                    // The entered match exists
                    if (matchId.equals(commandParamMatchId.getValue())) {
                        String rangeLetter = String.valueOf(existingTournament.getRefereeRow().charAt(0));
                        int rangeNumber = Character.getNumericValue(existingTournament.getRefereeRow().charAt(1));

                        // Get the listed referees from the spreadsheet
                        List<List<Object>> listedRefereesFromSheet = authenticator.getDataFromRange(existingTournament.getScheduleTab(), rangeLetter + (rangeNumber + i));
                        String listedReferees = null;

                        // Check if there are no referees listed on the sheet
                        if (listedRefereesFromSheet != null) {
                            listedReferees = (String) listedRefereesFromSheet.get(0).stream().findFirst().orElse(null);
                        }

                        StringBuilder newReferees = new StringBuilder();

                        // Check if there is already a referees
                        if (listedReferees != null) {
                            List<String> splitReferees = new ArrayList<>(Arrays.asList(listedReferees.split("/")));

                            // Check if there is at least one referees in the list
                            if (splitReferees.size() >= 1) {
                                for (int j = 0; j < splitReferees.size(); j++) {
                                    splitReferees.set(j, splitReferees.get(j).trim());
                                }

                                for (String splitReferee : splitReferees) {
                                    if (splitReferee.equals(messageCreateEvent.getMessageAuthor().getName())) {
                                        messageCreateEvent
                                                .getChannel()
                                                .sendMessage(EmbedHelper.genericErrorEmbed("You are already listed as a referee for this match.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                                        return;
                                    }
                                }

                                splitReferees.add(messageCreateEvent.getMessageAuthor().getName());
                                newReferees.append(String.join(" / ", splitReferees));
                            } else {
                                newReferees.append(messageCreateEvent.getMessageAuthor().getName());
                            }
                        }
                        // There is no referee yet, add it right away
                        else {
                            newReferees = new StringBuilder(messageCreateEvent.getMessageAuthor().getName());
                        }

                        authenticator.updateDataOnSheet(existingTournament.getScheduleTab(), rangeLetter + (rangeNumber + i), newReferees.toString());

                        messageCreateEvent
                                .getChannel()
                                .sendMessage(EmbedHelper
                                        .genericSuccessEmbed("**<@" + messageCreateEvent.getMessageAuthor().getId() + ">** successfully took the match " + matchId + " as a **referee**", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                        return;
                    }
                }
            }

            // There was no match with the given match id
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("There seems to be no match with the id `" + commandParamMatchId.getValue() + "`", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        } catch (Exception ex) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed(GoogleSpreadsheetAuthenticator.parseException(ex), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        }
    }
}

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

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DropAsStreamerCommand extends Command {
    private final TournamentRepository tournamentRepository;

    @Autowired
    public DropAsStreamerCommand(TournamentRepository tournamentRepository) {
        this.commandName = "dropasstreamer";
        this.description = "Drop a match as a streamer";
        this.group = "Tournament management";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("match id", "The id of the match to drop", CommandArgumentType.String));

        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    @Transactional
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        Tournament existingTournament = TournamentHelper.getRunningTournament(messageCreateEvent, tournamentRepository);

        if (existingTournament == null) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("There is no tournament running in this server.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        // The user doesn't have the appropriate role to run this command
        if (!TournamentHelper.hasRoleOrIsServerOwner(messageCreateEvent, existingTournament.getCommentatorRoleSnowflake())) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("You don't have the **Commentator** role. \n" +
                            "**Ping one of the Tournament hosts in order to get this role.**", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        // Check if the tournament is properly setup
        if (!TournamentHelper.isTournamentProperlySetup(existingTournament)) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("The tournament hasn't been setup yet.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
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
                        // Get the listed streamers from the spreadsheet
                        String listedStreamers = TournamentHelper.getSheetRowAsString(authenticator, existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getStreamerRow(), i));

                        StringBuilder newStreamers = new StringBuilder();
                        List<String> splitStreamers = new ArrayList<>();

                        boolean streamerFound = false;

                        // Check if there is already a streamers
                        if (listedStreamers != null) {
                            splitStreamers = new ArrayList<>(Arrays.asList(listedStreamers.split("/")));

                            // Check if there is at least one streamers in the list
                            if (splitStreamers.size() >= 1) {
                                for (int j = 0; j < splitStreamers.size(); j++) {
                                    splitStreamers.set(j, splitStreamers.get(j).trim());
                                }

                                for (String splitStreamer : splitStreamers) {
                                    if (splitStreamer.equals(messageCreateEvent.getMessageAuthor().getDisplayName())) {
                                        streamerFound = true;
                                    }
                                }
                            }
                        }

                        // Streamer was not found, can't drop
                        if (!streamerFound) {
                            messageCreateEvent
                                    .getChannel()
                                    .sendMessage(EmbedHelper.genericErrorEmbed("You are not listed as a **Streamer** on this match.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                            return;
                        }

                        splitStreamers.remove(messageCreateEvent.getMessageAuthor().getDisplayName());
                        newStreamers.append(String.join(" / ", splitStreamers));

                        authenticator.updateDataOnSheet(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getStreamerRow(), i), newStreamers.toString());

                        TournamentHelper.synchronizeSpreadsheet(tournamentRepository, existingTournament);

                        messageCreateEvent
                                .getChannel()
                                .sendMessage(EmbedHelper
                                        .genericSuccessEmbed("**<@" + messageCreateEvent.getMessageAuthor().getId() + ">** successfully dropped the match " + matchId + " as a **Streamer**", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
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

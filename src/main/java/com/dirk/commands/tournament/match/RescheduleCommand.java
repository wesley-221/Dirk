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
import com.dirk.helper.Emoji;
import com.dirk.helper.TournamentHelper;
import com.dirk.models.GoogleSpreadsheetAuthenticator;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandArgument;
import com.dirk.models.command.CommandArgumentType;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.tournament.Team;
import com.dirk.models.tournament.Tournament;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class RescheduleCommand extends Command {
    private final TournamentRepository tournamentRepository;

    @Autowired
    public RescheduleCommand(TournamentRepository tournamentRepository) {
        this.commandName = "reschedule";
        this.description = "Reschedule a match";
        this.group = "Tournament management";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("match id", "The id of the match to reschedule", CommandArgumentType.SingleString));
        this.commandArguments.add(new CommandArgument("day", "The day of when the new match is supposed to happen", CommandArgumentType.SingleString));
        this.commandArguments.add(new CommandArgument("month", "The full name of the month of when the new match is supposed to happen (ie. December)", CommandArgumentType.SingleString));
        this.commandArguments.add(new CommandArgument("time", "The time of when the new match is supposed to happen (in UTC+0)", CommandArgumentType.SingleString));

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

        String userMatchId = (String) commandParams.get(0).getValue();
        String userDay = (String) commandParams.get(1).getValue();
        String userMonth = (String) commandParams.get(2).getValue();
        String userTime = (String) commandParams.get(3).getValue();
        String userDateString = userDay + "-" + userMonth + "-" + Calendar.getInstance().get(Calendar.YEAR) + " " + userTime;

        try {
            Date date;

            try {
                SimpleDateFormat format = new SimpleDateFormat("d-MMMM-y H:m");
                date = format.parse(userDateString);
            } catch (Exception ex) {
                messageCreateEvent
                        .getChannel()
                        .sendMessage(EmbedHelper.genericErrorEmbed("Invalid date and/or time given.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));

                ex.printStackTrace();
                return;
            }

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
                    if (matchId.equals(userMatchId)) {
                        List<List<Object>> listedPlayerOne = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getPlayerOneRow(), i));
                        List<List<Object>> listedPlayerTwo = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getPlayerTwoRow(), i));

                        String playerOne = (String) listedPlayerOne.get(0).stream().findFirst().orElse(null);
                        String playerTwo = (String) listedPlayerTwo.get(0).stream().findFirst().orElse(null);

                        assert playerOne != null;
                        assert playerTwo != null;

                        // The current tournament is a team tournament
                        if (existingTournament.getIsTeamTournament()) {
                            List<Team> allTeams = existingTournament.getAllTeams();

                            boolean teamCaptainFound = false;

                            for (Team team : allTeams) {
                                if (team.getCaptain().equals(messageCreateEvent.getMessageAuthor().getDisplayName())) {
                                    teamCaptainFound = true;
                                }
                            }

                            if (!teamCaptainFound) {
                                messageCreateEvent
                                        .getChannel()
                                        .sendMessage(EmbedHelper.genericErrorEmbed("You are not the team captain of either team.\n\n" +
                                                "**Is this incorrect? Ping any of the tournament hosts!**", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                                return;
                            }
                        }
                        // The current tournament is a player vs player tournament
                        else {
                            // Check if the user is either player one or two
                            if (!List.of(playerOne, playerTwo).contains(messageCreateEvent.getMessageAuthor().getDisplayName())) {
                                messageCreateEvent
                                        .getChannel()
                                        .sendMessage(EmbedHelper.genericErrorEmbed("You are not part of this match. \n\n" +
                                                "**Is this incorrect? Ping any of the tournament hosts!**", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                                return;
                            }
                        }

                        SimpleDateFormat format = new SimpleDateFormat("d MMMM H:mm");

                        Server server = messageCreateEvent.getServer().get();

                        Object userOne;
                        Object userTwo;

                        // Change userOne/Two to either a Role or User depending on solo or team tournament
                        if (existingTournament.getIsTeamTournament()) {
                            userOne = server.getRolesByName(playerOne).stream().findFirst().orElse(null);
                            userTwo = server.getRolesByName(playerTwo).stream().findFirst().orElse(null);
                        } else {
                            userOne = server.getMembersByName(playerOne).stream().findFirst().orElse(null);
                            userTwo = server.getMembersByName(playerTwo).stream().findFirst().orElse(null);
                        }

                        // Check if the users exist in the Discord
                        if (userOne == null || userTwo == null) {
                            List<String> usersNotFound = new ArrayList<>();

                            if (userOne == null) {
                                usersNotFound.add("**" + playerOne + "**");
                            }

                            if (userTwo == null) {
                                usersNotFound.add("**" + playerTwo + "**");
                            }

                            messageCreateEvent
                                    .getChannel()
                                    .sendMessage(EmbedHelper.genericErrorEmbed("Unable to find the user(s): " + String.join("/", usersNotFound) + " in the Discord.\n\n" +
                                            "**Is this incorrect? Ping any of the tournament hosts!**", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                            return;
                        }

                        List<List<Object>> listedDate = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getDateRow(), i));
                        List<List<Object>> listedTime = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getTimeRow(), i));

                        String spreadsheetDate = (String) listedDate.get(0).stream().findFirst().orElse(null);
                        String spreadsheetTime = (String) listedTime.get(0).stream().findFirst().orElse(null);

                        Date originalDate;
                        String originalDateString = spreadsheetDate + "/" + Calendar.getInstance().get(Calendar.YEAR) + " " + spreadsheetTime;

                        try {
                            String dateFormat = existingTournament
                                    .getDateFormat()
                                    .replace("%d", "dd")
                                    .replace("%m", "MM")
                                    .replace("-", "/")
                                    + "/yyyy H:m";

                            SimpleDateFormat tmpFormat = new SimpleDateFormat(dateFormat);
                            originalDate = tmpFormat.parse(originalDateString);
                        } catch (Exception ex) {
                            messageCreateEvent
                                    .getChannel()
                                    .sendMessage(EmbedHelper.genericErrorEmbed("Invalid date and/or time given.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));

                            ex.printStackTrace();
                            return;
                        }

                        String userOnePing = (userOne instanceof User) ? "<@" + ((User) userOne).getId() + ">" : "<@&" + ((Role) userOne).getId() + ">";
                        String userTwoPing = (userTwo instanceof User) ? "<@" + ((User) userTwo).getId() + ">" : "<@&" + ((Role) userTwo).getId() + ">";

                        String message;

                        if (existingTournament.getIsTeamTournament()) {
                            String teamOneCaptain = "";

                            for (Team team : existingTournament.getAllTeams()) {
                                if (team.getTeamId().getName().equals(((Role) userOne).getName())) {
                                    teamOneCaptain = team.getCaptain();
                                }
                            }

                            message = String.format("Hello %s! %s would like to reschedule **match %s** from **%s UTC+0** to **%s UTC+0**. If you would like to accept this reschedule, react to this message with a " + Emoji.THUMBS_UP + ". ",
                                    (messageCreateEvent.getMessageAuthor().getDisplayName().equals(teamOneCaptain) ? userTwoPing : userOnePing),
                                    (messageCreateEvent.getMessageAuthor().getDisplayName().equals(teamOneCaptain) ? userOnePing : userTwoPing),
                                    userMatchId,
                                    format.format(originalDate),
                                    format.format(date));
                        } else {
                            message = String.format("Hello %s! %s would like to reschedule **match %s** from **%s UTC+0** to **%s UTC+0**. If you would like to accept this reschedule, react to this message with a " + Emoji.THUMBS_UP + ". ",
                                    (messageCreateEvent.getMessageAuthor().getDisplayName().equals(playerOne) ? userTwoPing : userOnePing),
                                    (messageCreateEvent.getMessageAuthor().getDisplayName().equals(playerOne) ? userOnePing : userTwoPing),
                                    userMatchId,
                                    format.format(originalDate),
                                    format.format(date));
                        }

                        CompletableFuture<Message> sentMessage = messageCreateEvent
                                .getChannel()
                                .sendMessage(message);

                        sentMessage.whenComplete((msg, throwable) -> msg.addReaction(Emoji.THUMBS_UP));

                        return;
                    }
                }
            }

            // There was no match with the given match id
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("There seems to be no match with the id `" + userMatchId + "`", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        } catch (Exception ex) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed(GoogleSpreadsheetAuthenticator.parseException(ex), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        }
    }
}

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

package com.dirk.listeners;

import com.dirk.helper.EmbedHelper;
import com.dirk.helper.Emoji;
import com.dirk.helper.RegisterListener;
import com.dirk.helper.TournamentHelper;
import com.dirk.models.GoogleSpreadsheetAuthenticator;
import com.dirk.models.tournament.Team;
import com.dirk.models.tournament.Tournament;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TournamentListener implements ReactionAddListener, RegisterListener {
    private final TournamentRepository tournamentRepository;
    // Toggle to true if you want to accept reschedules from everyone
    // Note: only use this for development reasons
    private final Boolean DEVELOPMENT_TOGGLE = false;
    @Value("${bot.name}")
    private String botName;

    @Autowired
    public TournamentListener(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    @Override
    @Transactional
    public void onReactionAdd(ReactionAddEvent reaction) {
        Server server = reaction.getServer().orElse(null);

        // Check if the reaction is in a server
        if (server != null) {
            User reactionUser = reaction.getUser().orElse(null);

            assert reactionUser != null;

            // Ignore bot users
            if (reactionUser.isBot()) {
                return;
            }

            TextChannel textChannel = reaction.getServerTextChannel().orElse(null);
            Message msg = reaction.getApi().getMessageById(reaction.getMessageId(), textChannel).getNow(null);

            // Gets executed when message is found
            if (msg != null) {
                MessageAuthor messageAuthor = msg.getAuthor();

                // Ignore all reactions added to non-Dirk messages
                if (messageAuthor.isBotUser() && messageAuthor.getName().contains(botName)) {
                    // Check if the reaction is a thumbs up
                    if (reaction.getEmoji().equalsEmoji(Emoji.THUMBS_UP)) {
                        Pattern reschedulePattern = Pattern.compile("Hello <@&?([0-9]+)>! <@&?([0-9]+)> would like to reschedule \\*\\*match ([0-9A-Za-z]+)\\*\\* from \\*\\*([0-9A-Za-z:\\s]+) UTC\\+0\\*\\* to \\*\\*([0-9A-Za-z:\\s]+) UTC\\+0\\*\\*\\. " +
                                "If you would like to accept this reschedule, react to this message with a ");
                        Matcher matcher = reschedulePattern.matcher(msg.getContent());

                        // Proper pattern was found for reschedule
                        if (matcher.find()) {
                            String opponent = matcher.group(1);
                            String matchId = matcher.group(3);
                            String originalDate = matcher.group(4);
                            String proposedDate = matcher.group(5);

                            try {
                                Tournament existingTournament = TournamentHelper.getRunningTournament(msg, tournamentRepository);
                                boolean canReschedule = false;

                                // Tournament is a team tournament
                                if (existingTournament.getIsTeamTournament()) {
                                    List<Team> allTeams = existingTournament.getAllTeams();
                                    Role opponentRole = server.getRoleById(opponent).orElse(null);

                                    if (opponentRole != null) {
                                        for (Team team : allTeams) {
                                            // Check for both team captain & team name
                                            if (team.getCaptain().equals(reactionUser.getDisplayName(server)) && opponentRole.getName().equals(team.getTeamId().getName())) {
                                                canReschedule = true;
                                            }
                                        }
                                    }
                                }
                                // Tournament is a player vs player
                                else {
                                    canReschedule = reactionUser.getIdAsString().equals(opponent);
                                }

                                // The reaction was send by the opponent
                                if (canReschedule || DEVELOPMENT_TOGGLE) {
                                    String spreadsheetId = TournamentHelper.getSpreadsheetIdFromUrl(existingTournament.getSpreadsheet());
                                    GoogleSpreadsheetAuthenticator authenticator = new GoogleSpreadsheetAuthenticator(spreadsheetId);

                                    // Check if the tournament is properly setup
                                    if (TournamentHelper.isTournamentProperlySetup(existingTournament)) {
                                        List<List<Object>> spreadsheetMatchId = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getMatchIdRow());

                                        for (int i = 0; i < spreadsheetMatchId.size(); i++) {
                                            String currentMatchId = (String) spreadsheetMatchId.get(i).stream().findFirst().orElse(null);

                                            if (currentMatchId != null) {
                                                if (currentMatchId.equals(matchId)) {
                                                    List<List<Object>> spreadsheetDate = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getDateRow(), i));
                                                    List<List<Object>> spreadsheetTime = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getTimeRow(), i));

                                                    List<List<Object>> spreadsheetPlayerOne = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getPlayerOneRow(), i));
                                                    List<List<Object>> spreadsheetPlayerTwo = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getPlayerTwoRow(), i));
                                                    List<List<Object>> spreadsheetReferee = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getRefereeRow(), i));
                                                    List<List<Object>> spreadsheetStreamer = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getStreamerRow(), i));
                                                    List<List<Object>> spreadsheetCommentator = authenticator.getDataFromRange(existingTournament.getScheduleTab(), TournamentHelper.getRangeFromRow(existingTournament.getCommentatorRow(), i));

                                                    List<Object> allRefereesList = spreadsheetReferee != null ? spreadsheetReferee.get(0) : null;
                                                    List<Object> allStreamersList = spreadsheetStreamer != null ? spreadsheetStreamer.get(0) : null;
                                                    List<Object> allCommentatorsList = spreadsheetCommentator != null ? spreadsheetCommentator.get(0) : null;

                                                    String playerOneDiscordTag = TournamentHelper.getUserAsDiscordHighlight(server, (String) spreadsheetPlayerOne.get(0).stream().findFirst().get());
                                                    String playerTwoDiscordTag = TournamentHelper.getUserAsDiscordHighlight(server, (String) spreadsheetPlayerTwo.get(0).stream().findFirst().get());

                                                    String allRefereesString = TournamentHelper.getUsersAsDiscordHighlights(server, allRefereesList);
                                                    String allStreamersString = TournamentHelper.getUsersAsDiscordHighlights(server, allStreamersList);
                                                    String allCommentatorsString = TournamentHelper.getUsersAsDiscordHighlights(server, allCommentatorsList);

                                                    // Update new time to sheet
                                                    String dateRow = TournamentHelper.getRangeFromRow(existingTournament.getDateRow(), i);
                                                    String timeRow = TournamentHelper.getRangeFromRow(existingTournament.getTimeRow(), i);

                                                    String sheetDateFormat = existingTournament
                                                            .getDateFormat()
                                                            .replace("%d", "dd")
                                                            .replace("%m", "MM");

                                                    SimpleDateFormat sheetDateSDF = new SimpleDateFormat(sheetDateFormat);
                                                    SimpleDateFormat timeSDF = new SimpleDateFormat("H:mm");
                                                    SimpleDateFormat originalDateSDF = new SimpleDateFormat("d MMMM H:mm");

                                                    SimpleDateFormat sheetDateTimeSDF = new SimpleDateFormat(sheetDateFormat + " H:mm");
                                                    Date formattedDate = originalDateSDF.parse(proposedDate);

                                                    String dateFromSpreadsheet = (String) spreadsheetDate.get(0).stream().findFirst().orElse(null);
                                                    String timeFromSpreadsheet = (String) spreadsheetTime.get(0).stream().findFirst().orElse(null);

                                                    Date dateTimeFromSpreadsheet = sheetDateTimeSDF.parse(dateFromSpreadsheet + " " + timeFromSpreadsheet);

                                                    // Ignore match reschedule if the new time is the same as the old time
                                                    if (formattedDate.equals(dateTimeFromSpreadsheet)) {
                                                        return;
                                                    }

                                                    authenticator.updateDataOnSheet(existingTournament.getScheduleTab(), dateRow, sheetDateSDF.format(formattedDate));
                                                    authenticator.updateDataOnSheet(existingTournament.getScheduleTab(), timeRow, timeSDF.format(formattedDate));

                                                    String rescheduledMatch = "**Match " + matchId + " (" + playerOneDiscordTag + " vs " + playerTwoDiscordTag + ")** has been rescheduled from **" + originalDate + " UTC+0** to **" + proposedDate + " UTC+0**.";
                                                    String staffOnMatch = "**__Referee:__** " + allRefereesString + "\n" +
                                                            "**__Streamer:__** " + allStreamersString + "\n" +
                                                            "**__Commentator:__** " + allCommentatorsString;

                                                    msg
                                                            .getChannel()
                                                            .sendMessage(rescheduledMatch);

                                                    CompletableFuture<Message> sentMessage = msg
                                                            .getServer().flatMap(msgServer -> msgServer.getTextChannelById(existingTournament.getRescheduleNotifierChannelSnowflake())).get()
                                                            .sendMessage(rescheduledMatch + "\n\n" + staffOnMatch +
                                                                    "\n\nIf you are unable to participate for this match, click on the emojis to remove yourself from the match.\n\n" +
                                                                    Emoji.CHECKERED_FLAG + ": referee \n" +
                                                                    Emoji.CAMERA + ": streamer \n" +
                                                                    Emoji.MICROPHONE + ": commentator");

                                                    sentMessage.whenComplete((newMsg, ignore) -> {
                                                        newMsg.addReaction(Emoji.CHECKERED_FLAG);
                                                        newMsg.addReaction(Emoji.CAMERA);
                                                        newMsg.addReaction(Emoji.MICROPHONE);
                                                    });

                                                    return;
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                msg
                                        .getChannel()
                                        .sendMessage(EmbedHelper.genericErrorEmbed(GoogleSpreadsheetAuthenticator.parseException(ex), msg.getAuthor().getDiscriminatedName()));
                            }
                        }
                    }

                    /* TODO: remove yourself from a match when clicking on these emojis
                    else if (reaction.getEmoji().equalsEmoji(Emoji.CHECKERED_FLAG) ||
                            reaction.getEmoji().equalsEmoji(Emoji.CAMERA) ||
                            reaction.getEmoji().equalsEmoji(Emoji.MICROPHONE)) {

                    }*/
                }
            }
        }
    }
}

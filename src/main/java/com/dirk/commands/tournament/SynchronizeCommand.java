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

package com.dirk.commands.tournament;

import com.dirk.helper.EmbedHelper;
import com.dirk.helper.TournamentHelper;
import com.dirk.models.GoogleSpreadsheetAuthenticator;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.tournament.Match;
import com.dirk.models.tournament.Team;
import com.dirk.models.tournament.Tournament;
import com.dirk.models.tournament.embeddable.MatchId;
import com.dirk.models.tournament.embeddable.TeamId;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class SynchronizeCommand extends Command {
    private final TournamentRepository tournamentRepository;

    @Autowired
    public SynchronizeCommand(final TournamentRepository tournamentRepository) {
        this.commandName = "synchronize";
        this.description = "Synchronize the spreadsheet with the database so that everything is up to date";
        this.group = "Tournament management";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.tournamentRepository = tournamentRepository;
    }

    @Transactional
    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
        Tournament existingTournament = TournamentHelper.getRunningTournament(messageCreateEvent, tournamentRepository);

        if (existingTournament == null) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("There is no tournament running in this server.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        // The user doesn't have the appropriate role to run this command
        if (!TournamentHelper.hasRoleOrIsServerOwner(messageCreateEvent, existingTournament.getAdminRoleSnowflake())) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("Unable to synchronize. You have to be the Server Owner or an Admin in order to run this.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        if (!TournamentHelper.isTournamentProperlySetup(existingTournament)) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(new EmbedBuilder()
                            .setTimestampToNow()
                            .setColor(Color.RED)
                            .setAuthor("Tournament has not been finalized yet.")
                            .setDescription("The tournament hasn't been setup properly yet. Check the table below to see what you still have to set: \n\n" + TournamentHelper.getTournamentStatus(existingTournament)));
            return;
        }

        try {
            String spreadsheetId = TournamentHelper.getSpreadsheetIdFromUrl(existingTournament.getSpreadsheet());
            GoogleSpreadsheetAuthenticator authenticator = new GoogleSpreadsheetAuthenticator(spreadsheetId);

            List<List<Object>> matchId = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getMatchIdRow());
            List<List<Object>> date = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getDateRow());
            List<List<Object>> time = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getTimeRow());
            List<List<Object>> playerOne = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getPlayerOneRow());
            List<List<Object>> playerTwo = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getPlayerTwoRow());
            List<List<Object>> referee = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getRefereeRow());
            List<List<Object>> streamer = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getStreamerRow());
            List<List<Object>> commentator = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getCommentatorRow());
            List<List<Object>> teams = authenticator.getDataFromRange(existingTournament.getScheduleTab(), existingTournament.getTeamsRow());

            String dateFormat = existingTournament
                    .getDateFormat()
                    .replace("%d", "dd")
                    .replace("%m", "MM")
                    .replace("-", "/")
                    + "/yyyy H:m";

            for (int i = 0; i < matchId.size(); i++) {
                String currentMatchId = (String) matchId.get(i).stream().findFirst().orElse(null);

                // Reformat the string to database format
                SimpleDateFormat sheetFormat = new SimpleDateFormat(dateFormat);

                String currentDateString = (String) date.get(i).stream().findFirst().orElse(null);
                currentDateString += "/" + Calendar.getInstance().get(Calendar.YEAR);
                currentDateString += " " + time.get(i).stream().findFirst().orElse(null);

                Date currentDate = sheetFormat.parse(currentDateString);
                String currentPlayerOne = (String) playerOne.get(i).stream().findFirst().orElse(null);
                String currentPlayerTwo = (String) playerTwo.get(i).stream().findFirst().orElse(null);
                String currentReferee = referee.size() > i ? (String) referee.get(i).stream().findFirst().orElse(null) : null;
                String currentStreamer = streamer.size() > i ? (String) streamer.get(i).stream().findFirst().orElse(null) : null;
                String currentCommentator = commentator.size() > i ? (String) commentator.get(i).stream().findFirst().orElse(null) : null;

                Match match = new Match();

                // Create the primary key
                MatchId matchIdEmbeddable = new MatchId();
                matchIdEmbeddable.setMatchId(currentMatchId);
                matchIdEmbeddable.setServerSnowflake(existingTournament.getServerSnowflake());

                // Set the primary key & tournament
                match.setMatchId(matchIdEmbeddable);
                match.setTournament(existingTournament);

                match.setDate(currentDate);
                match.setPlayerOne(currentPlayerOne);
                match.setPlayerTwo(currentPlayerTwo);
                match.setReferee(currentReferee);
                match.setStreamer(currentStreamer);
                match.setCommentator(currentCommentator);

                existingTournament.getAllMatches().add(match);
            }

            for (List<Object> teamObject : teams) {
                String teamName = teamObject.get(0).toString();
                String teamCaptain = teamObject.get(1).toString();

                Team team = new Team();

                TeamId teamIdEmbeddable = new TeamId();
                teamIdEmbeddable.setName(teamName);
                teamIdEmbeddable.setServerSnowflake(existingTournament.getServerSnowflake());

                team.setTeamId(teamIdEmbeddable);
                team.setTournament(existingTournament);
                team.setCaptain(teamCaptain);

                existingTournament.getAllTeams().add(team);
            }

            tournamentRepository.save(existingTournament);

            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericSuccessEmbed("Successfully synchronized all matches and teams.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        } catch (Exception ex) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed(GoogleSpreadsheetAuthenticator.parseException(ex), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        }
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
    }
}

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

package com.dirk.meta;

import com.dirk.DiscordConfiguration;
import com.dirk.helper.TournamentHelper;
import com.dirk.models.tournament.Match;
import com.dirk.models.tournament.Tournament;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.server.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@Transactional
public class TournamentMatchTimer {
    TournamentRepository tournamentRepository;
    DiscordConfiguration discordConfiguration;

    @Autowired
    public TournamentMatchTimer(TournamentRepository tournamentRepository, DiscordConfiguration discordConfiguration) {
        this.tournamentRepository = tournamentRepository;
        this.discordConfiguration = discordConfiguration;
    }

    // Run every 5 minutes
    // <second> <minute> <hour> <day-of-month> <month> <day-of-week> <year> <command>
//    @Scheduled(cron = "*/5 * * * * *") // run every 5 seconds, testing purposes
    @Scheduled(cron = "* */5 * * * *")
    public void tournamentMatchTask() {
        List<Tournament> allTournaments = (List<Tournament>) tournamentRepository.findAll();

        for (Tournament tournament : allTournaments) {
            List<Match> allMatches = tournament.getAllMatches();

            for (Match match : allMatches) {
                if (match.getDate().after(new Date()) && !match.isIgnored()) {
                    Duration timeBetween = Duration.between(
                            Instant.now(),
                            match.getDate().toInstant()
                    );

                    // Match is within the next 30 minutes
                    if (timeBetween.toMinutes() <= 30) {
                        Server server = this.discordConfiguration.getDiscordApi().getServerById(tournament.getServerSnowflake()).orElse(null);

                        // Check if the server exists
                        if (server != null) {
                            TextChannel textChannel = server.getTextChannelById(tournament.getMatchNotifierChannelSnowflake()).orElse(null);

                            // Check if the text channel exists
                            if (textChannel != null) {
                                String finalMessage;

                                if (tournament.getIsTeamTournament()) {
                                    finalMessage = "Hello " +
                                            TournamentHelper.getTeamAsDiscordHighlight(server, match.getPlayerOne()) +
                                            " and " +
                                            TournamentHelper.getTeamAsDiscordHighlight(server, match.getPlayerTwo()) + "!";
                                } else {
                                    finalMessage = "Hello " +
                                            TournamentHelper.getUserAsDiscordHighlight(server, match.getPlayerOne()) +
                                            " and " +
                                            TournamentHelper.getUserAsDiscordHighlight(server, match.getPlayerTwo()) + "!";
                                }

                                finalMessage += " Your match will be starting in " + timeBetween.toMinutes() + " minutes.\n\n";

                                List<Object> allReferees;
                                List<Object> allStreamers;
                                List<Object> allCommentators;

                                String allRefereesString = null;
                                String allStreamersString = null;
                                String allCommentatorsString = null;

                                if (match.getReferee() != null) {
                                    allReferees = Collections.singletonList(match.getReferee());
                                    allRefereesString = TournamentHelper.getUsersAsDiscordHighlights(server, allReferees);
                                }

                                if (match.getStreamer() != null) {
                                    allStreamers = Collections.singletonList(match.getStreamer());
                                    allStreamersString = TournamentHelper.getUsersAsDiscordHighlights(server, allStreamers);
                                }

                                if (match.getCommentator() != null) {
                                    allCommentators = Collections.singletonList(match.getCommentator());
                                    allCommentatorsString = TournamentHelper.getUsersAsDiscordHighlights(server, allCommentators);
                                }

                                finalMessage += "**Referee:** " + (allRefereesString != null ? allRefereesString : "") + "\n";
                                finalMessage += "**Streamer:** " + (allStreamersString != null ? allStreamersString : "") + "\n";
                                finalMessage += "**Commentator:** " + (allCommentatorsString != null ? allCommentatorsString : "");

                                textChannel
                                        .sendMessage(finalMessage);

                                match.setIgnoreMatch(true);
                            }
                        }
                    }
                }
            }

            tournamentRepository.save(tournament);
        }
    }
}

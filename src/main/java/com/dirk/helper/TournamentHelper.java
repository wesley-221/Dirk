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

package com.dirk.helper;

import com.dirk.models.tournament.Tournament;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TournamentHelper {
    /**
     * The email of Dirk to add to spreadsheets
     */
    public static final String DIRK_BOT_EMAIL = "dirk.osu.bot@gmail.com";

    /**
     * Get the tournament if there is one running
     *
     * @param messageCreateEvent   the MessageCreateEvent object
     * @param tournamentRepository the TournamentRepository
     * @return Whether or not a tournament is running in the given server
     */
    public static Tournament getRunningTournament(MessageCreateEvent messageCreateEvent, TournamentRepository tournamentRepository) {
        Server server = messageCreateEvent.getServer().get();
        return tournamentRepository.getTournamentByServerSnowflake(server.getIdAsString());
    }

    /**
     * Get a role by the given string and server
     *
     * @param role   the Role to look for
     * @param server the Server to look in
     * @return A role or null
     */
    public static Role getRoleByString(String role, Server server) {
        Pattern rolePattern = Pattern.compile("<@&([0-9]+)>");
        Matcher matcher = rolePattern.matcher(role);

        Optional<Role> foundRole;

        // Check if role is typed out or mention
        if (matcher.find()) {
            String roleId = matcher.group(1);
            foundRole = server.getRoleById(roleId);
        } else {
            foundRole = server.getRolesByName(role).stream().findFirst();
        }

        return foundRole.orElse(null);
    }

    /**
     * Validate the input to see if it is a correct row
     *
     * @param row the row to check
     * @return result of whether or not the row is correct
     */
    public static Boolean validateSpreadsheetRowInput(String row) {
        Pattern rowPattern = Pattern.compile("[A-Za-z0-9]{1,3}:[A-Za-z0-9]{1,3}");
        Matcher matcher = rowPattern.matcher(row);

        return matcher.find();
    }

    /**
     * Validate if the input to see if it is a correct date format
     *
     * @param dateFormat the format of the date
     * @return result of whether or not the date format is correct
     */
    public static Boolean validateDateFormat(String dateFormat) {
        Pattern dateFormatPattern = Pattern.compile("[%dm][/-][%dm]");
        Matcher matcher = dateFormatPattern.matcher(dateFormat);

        return matcher.find();
    }

    /**
     * Validate if the input is a correct spreadsheet format
     *
     * @param spreadsheetUrl the url to the spreadsheet
     * @return result of whether or not the spreadsheet format is correct
     */
    public static Boolean validateSpreadsheetUrl(String spreadsheetUrl) {
        Pattern spreadsheetPattern = Pattern.compile("https://docs.google.com/spreadsheets/d/[A-Za-z0-9-_]+");
        Matcher matcher = spreadsheetPattern.matcher(spreadsheetUrl);

        return matcher.find();
    }

    /**
     * Get the spreadsheetId from the given url
     *
     * @param spreadsheetUrl the url to get the spreadsheetId from
     * @return the spreadsheetId
     */
    public static String getSpreadsheetIdFromUrl(String spreadsheetUrl) {
        Pattern spreadsheetPattern = Pattern.compile("https://docs.google.com/spreadsheets/d/([A-Za-z0-9-_]+)");
        Matcher matcher = spreadsheetPattern.matcher(spreadsheetUrl);

        String spreadsheetId = null;

        if (matcher.find()) {
            spreadsheetId = matcher.group(1);
        }

        return spreadsheetId;
    }

    /**
     * Check if the tournament is properly setup
     *
     * @param tournament the tournament to check
     * @return if the tournament is properly setup
     */
    public static Boolean isTournamentProperlySetup(Tournament tournament) {
        return tournament != null &&
                tournament.getSpreadsheet() != null &&
                tournament.getScheduleTab() != null &&
                tournament.getIsTeamTournament() != null &&
                tournament.getMatchIdRow() != null &&
                tournament.getDateFormat() != null &&
                tournament.getPlayerOneRow() != null &&
                tournament.getPlayerTwoRow() != null &&
                tournament.getRefereeRow() != null &&
                tournament.getStreamerRow() != null &&
                tournament.getCommentatorRow() != null &&
                tournament.getDateRow() != null &&
                tournament.getTimeRow() != null;
    }

    /**
     * Get the status of the given tournament
     *
     * @param tournament the tournament to check
     * @return the status of the tournament
     */
    public static String getTournamentStatus(Tournament tournament) {
        return "**Spreadsheet:** " +
                (tournament.getSpreadsheet() != null ? "[link to spreadsheet](" + tournament.getSpreadsheet() + ")" : "Not set") + "\n" +
                "**Schedule tab:** " +
                (tournament.getScheduleTab() != null ? "`" + tournament.getScheduleTab() + "`" : "Not set") + "\n" +
                "**Tournament type:** " +
                (tournament.getIsTeamTournament() != null ? "`" + (tournament.getIsTeamTournament() ? "teams" : "solo") + "`" : "Not set") + "\n\n" +
                "**Match id row:** " +
                (tournament.getMatchIdRow() != null ? "`" + tournament.getMatchIdRow() + "`" : "Not set") + "\n" +
                "**Date format:** " +
                (tournament.getDateFormat() != null ? "`" + tournament.getDateFormat() + "`" : "Not set") + "\n\n" +
                "**Player/team 1 row:** " +
                (tournament.getPlayerOneRow() != null ? "`" + tournament.getPlayerOneRow() + "`" : "Not set") + "\n" +
                "**Player/team 2 row:** " +
                (tournament.getPlayerTwoRow() != null ? "`" + tournament.getPlayerTwoRow() + "`" : "Not set") + "\n\n" +
                "**Referee row**: " +
                (tournament.getRefereeRow() != null ? "`" + tournament.getRefereeRow() + "`" : "Not set") + "\n" +
                "**Streamer row**: " +
                (tournament.getStreamerRow() != null ? "`" + tournament.getStreamerRow() + "`" : "Not set") + "\n" +
                "**Commentator row**: " +
                (tournament.getCommentatorRow() != null ? "`" + tournament.getCommentatorRow() + "`" : "Not set") + "\n\n" +
                "**Date row**: " +
                (tournament.getDateRow() != null ? "`" + tournament.getDateRow() + "`" : "Not set") + "\n" +
                "**Time row**: " +
                (tournament.getTimeRow() != null ? "`" + tournament.getTimeRow() + "`" : "Not set") + "\n";
    }
}

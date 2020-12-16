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

import com.dirk.models.GoogleSpreadsheetAuthenticator;
import com.dirk.models.tournament.Match;
import com.dirk.models.tournament.Team;
import com.dirk.models.tournament.Tournament;
import com.dirk.models.tournament.embeddable.MatchId;
import com.dirk.models.tournament.embeddable.TeamId;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.CertainMessageEvent;
import org.javacord.api.event.message.MessageCreateEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    public static Tournament getRunningTournament(Object messageCreateEvent, TournamentRepository tournamentRepository) {
        Server server;

        if (messageCreateEvent instanceof CertainMessageEvent) {
            server = ((MessageCreateEvent) messageCreateEvent).getServer().get();
        } else {
            server = ((Message) messageCreateEvent).getServer().get();
        }

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
     * Validate the team row and check if there's actually two rows
     *
     * @param row the rows to check
     * @return result of whether or not the rows are correct
     */
    public static Boolean validateSpreadsheetTeamRow(String row) {
        Pattern rowPattern = Pattern.compile("([A-Za-z0-9]{1,3}):([A-Za-z0-9]{1,3})");
        Matcher matcher = rowPattern.matcher(row);

        if (matcher.find()) {
            String firstRow = String.valueOf(matcher.group(1).charAt(0));
            String lastRow = matcher.group(2);

            return !firstRow.equals(lastRow);
        }

        return false;
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

    /**
     * Get the range for a row with an offset
     *
     * @param row    the row to get the data from
     * @param offset the offset for the new range
     * @return the new range to get data from
     */
    public static String getRangeFromRow(String row, Integer offset) {
        String rangeLetter = String.valueOf(row.charAt(0));
        int rangeNumber = Character.getNumericValue(row.charAt(1));

        return rangeLetter + (rangeNumber + offset);
    }

    /**
     * Get the discord tag from all the given users
     *
     * @param server the server to check for the users
     * @param list   the users to get the discord tag for
     * @return all users with discord tags
     */
    public static String getUsersAsDiscordHighlights(Server server, List<Object> list) {
        String finalString = "";
        List<String> allUsersSplit;

        if (list != null) {
            allUsersSplit = Arrays.asList(((String) list.stream().findFirst().get()).split("/"));

            if (allUsersSplit.size() > 1) {
                List<String> tmpAllUsers = new ArrayList<>();

                for (String user : allUsersSplit) {
                    User serverUser = server.getMembersByDisplayName(user.trim()).stream().findFirst().orElse(null);

                    if (serverUser != null) {
                        tmpAllUsers.add(serverUser.getMentionTag());
                    } else {
                        tmpAllUsers.add("**" + user + "**");
                    }
                }

                finalString = String.join(" / ", tmpAllUsers);
            } else {
                User serverUser = server.getMembersByDisplayName(allUsersSplit.get(0)).stream().findFirst().orElse(null);

                if (serverUser != null) {
                    finalString = serverUser.getMentionTag();
                } else {
                    finalString = "**" + allUsersSplit.get(0) + "**";
                }
            }
        }

        return finalString;
    }

    /**
     * Get the discord tag from the given user
     *
     * @param server the server to check for the user
     * @param user   the user to get the discord tag for
     * @return the user with discord tag
     */
    public static String getUserAsDiscordHighlight(Server server, String user) {
        User serverUser = server.getMembersByDisplayName(user).stream().findFirst().orElse(null);
        String finalString;

        if (serverUser != null) {
            finalString = serverUser.getMentionTag();
        } else {
            finalString = "**" + user + "**";
        }

        return finalString;
    }

    /**
     * Get the discord tag from the given role
     *
     * @param server   the server toc heck for the user
     * @param roleName the role to get the discord tag for
     * @return the role with the discord tag
     */
    public static String getTeamAsDiscordHighlight(Server server, String roleName) {
        Role serverRole = server.getRolesByName(roleName).stream().findFirst().orElse(null);
        String finalString;

        if (serverRole != null) {
            finalString = serverRole.getMentionTag();
        } else {
            finalString = "**" + roleName + "**";
        }

        return finalString;
    }

    /**
     * Check if the user has the given role
     *
     * @param messageCreateEvent the event given from the executed command
     * @param roleSnowflake      the snowflake of the role to check for
     * @return whether or not the user has the given role
     */
    public static Boolean hasRoleOrIsServerOwner(MessageCreateEvent messageCreateEvent, String roleSnowflake) {
        Server server = messageCreateEvent.getServer().orElse(null);

        if (server != null) {
            User messageUser = messageCreateEvent.getMessageAuthor().asUser().orElse(null);

            if (messageUser != null) {
                User serverUser = server.getMemberById(messageUser.getId()).orElse(null);

                if (serverUser != null) {
                    Role role = server.getRoleById(roleSnowflake).orElse(null);

                    if (role != null || server.isOwner(serverUser)) {
                        return serverUser.getRoles(server).contains(role) || server.isOwner(serverUser);
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check if the user is a server owner
     *
     * @param messageCreateEvent the event given from the executed command
     * @return whether or not the user is the server owner
     */
    public static Boolean isServerOwner(MessageCreateEvent messageCreateEvent) {
        Server server = messageCreateEvent.getServer().orElse(null);
        User user = messageCreateEvent.getMessageAuthor().asUser().orElse(null);

        if (server != null && user != null) {
            return server.isOwner(user);
        }

        return false;
    }

    /**
     * Get data from the given spreadsheet as a string
     *
     * @param authenticator the spreadsheet authenticator
     * @param scheduleTab   the tab where the schedules are
     * @param rowRange      the range of what to get
     * @return the data as a string
     * @throws IOException the error when something fails
     */
    public static String getSheetRowAsString(GoogleSpreadsheetAuthenticator authenticator, String scheduleTab, String rowRange) throws IOException {
        List<List<Object>> listedRowsFromSheetObject = authenticator.getDataFromRange(scheduleTab, rowRange);
        String listedRowFromSheet = null;

        // Check if there are no commentators listed on the sheet
        if (listedRowsFromSheetObject != null) {
            listedRowFromSheet = (String) listedRowsFromSheetObject.get(0).stream().findFirst().orElse(null);
        }

        return listedRowFromSheet;
    }

    /**
     * Save all spreadsheet data to the database
     *
     * @param tournamentRepository the repository used to save the tournament
     * @param tournament           the tournament to save as
     * @throws IOException              the error when something fails
     * @throws GeneralSecurityException the error when something fails
     * @throws ParseException           the error when something fails
     */
    public static void synchronizeSpreadsheet(TournamentRepository tournamentRepository, Tournament tournament) throws IOException, GeneralSecurityException, ParseException {
        String spreadsheetId = TournamentHelper.getSpreadsheetIdFromUrl(tournament.getSpreadsheet());
        GoogleSpreadsheetAuthenticator authenticator = new GoogleSpreadsheetAuthenticator(spreadsheetId);

        List<List<Object>> matchId = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getMatchIdRow());
        List<List<Object>> date = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getDateRow());
        List<List<Object>> time = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getTimeRow());
        List<List<Object>> playerOne = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getPlayerOneRow());
        List<List<Object>> playerTwo = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getPlayerTwoRow());
        List<List<Object>> referee = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getRefereeRow());
        List<List<Object>> streamer = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getStreamerRow());
        List<List<Object>> commentator = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getCommentatorRow());
        List<List<Object>> teams = authenticator.getDataFromRange(tournament.getScheduleTab(), tournament.getTeamsRow());

        String dateFormat = tournament
                .getDateFormat()
                .replace("%d", "dd")
                .replace("%m", "MM")
                .replace("-", "/")
                + "/yyyy H:m";

        for (int i = 0; i < matchId.size(); i++) {
            String currentMatchId = (String) matchId.get(i).stream().findFirst().orElse(null);
            Match oldMatch = tournament.getAllMatches().stream().filter(match -> match.getMatchId().getMatchId().equals(currentMatchId)).findFirst().orElse(null);

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
            matchIdEmbeddable.setServerSnowflake(tournament.getServerSnowflake());

            // Set the primary key & tournament
            match.setMatchId(matchIdEmbeddable);
            match.setTournament(tournament);

            match.setDate(currentDate);
            match.setPlayerOne(currentPlayerOne);
            match.setPlayerTwo(currentPlayerTwo);
            match.setReferee(currentReferee);
            match.setStreamer(currentStreamer);
            match.setCommentator(currentCommentator);

            // Make sure to update the ignoreMatch from the old saved match
            if (oldMatch != null) {
                match.setIgnoreMatch(oldMatch.getIgnoreMatch());
            }

            tournament.getAllMatches().set(tournament.getAllMatches().indexOf(oldMatch), match);
        }

        for (List<Object> teamObject : teams) {
            String teamName = teamObject.get(0).toString();
            String teamCaptain = teamObject.get(1).toString();

            Team team = new Team();

            TeamId teamIdEmbeddable = new TeamId();
            teamIdEmbeddable.setName(teamName);
            teamIdEmbeddable.setServerSnowflake(tournament.getServerSnowflake());

            team.setTeamId(teamIdEmbeddable);
            team.setTournament(tournament);
            team.setCaptain(teamCaptain);

            tournament.getAllTeams().add(team);
        }

        tournamentRepository.save(tournament);
    }
}

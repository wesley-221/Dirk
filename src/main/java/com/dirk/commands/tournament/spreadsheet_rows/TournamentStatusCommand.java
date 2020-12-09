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

package com.dirk.commands.tournament.spreadsheet_rows;

import com.dirk.helper.EmbedHelper;
import com.dirk.helper.TournamentHelper;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.tournament.Tournament;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
public class TournamentStatusCommand extends Command {
    private final TournamentRepository tournamentRepository;

    @Autowired
    public TournamentStatusCommand(TournamentRepository tournamentRepository) {
        this.commandName = "tournamentstatus";
        this.description = "Get the status of how the tournament is setup.";
        this.group = "Tournament management";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
        Tournament existingTournament = TournamentHelper.getRunningTournament(messageCreateEvent, tournamentRepository);

        if (existingTournament == null) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("There is no tournament running in this server.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        String tournamentStatus = "**Spreadsheet:** " +
                (existingTournament.getSpreadsheet() != null ? "[link to spreadsheet](" + existingTournament.getSpreadsheet() + ")" : "Not set") + "\n" +
                "**Schedule tab:** " +
                (existingTournament.getScheduleTab() != null ? "`" + existingTournament.getScheduleTab() + "`" : "Not set") + "\n" +
                "**Tournament type:** " +
                (existingTournament.getIsTeamTournament() != null ? "`" + (existingTournament.getIsTeamTournament() ? "teams" : "solo") + "`" : "Not set") + "\n\n" +
                "**Match id row:** " +
                (existingTournament.getMatchIdRow() != null ? "`" + existingTournament.getMatchIdRow() + "`" : "Not set") + "\n" +
                "**Date format:** " +
                (existingTournament.getDateFormat() != null ? "`" + existingTournament.getDateFormat() + "`" : "Not set") + "\n\n" +
                "**Player/team 1 row:** " +
                (existingTournament.getPlayerOneRow() != null ? "`" + existingTournament.getPlayerOneRow() + "`" : "Not set") + "\n" +
                "**Player/team 2 row:** " +
                (existingTournament.getPlayerTwoRow() != null ? "`" + existingTournament.getPlayerTwoRow() + "`" : "Not set") + "\n\n" +
                "**Referee row**: " +
                (existingTournament.getRefereeRow() != null ? "`" + existingTournament.getRefereeRow() + "`" : "Not set") + "\n" +
                "**Streamer row**: " +
                (existingTournament.getStreamerRow() != null ? "`" + existingTournament.getStreamerRow() + "`" : "Not set") + "\n" +
                "**Commentator row**: " +
                (existingTournament.getCommentatorRow() != null ? "`" + existingTournament.getCommentatorRow() + "`" : "Not set") + "\n\n" +
                "**Date row**: " +
                (existingTournament.getDateRow() != null ? "`" + existingTournament.getDateRow() + "`" : "Not set") + "\n" +
                "**Time row**: " +
                (existingTournament.getTimeRow() != null ? "`" + existingTournament.getTimeRow() + "`" : "Not set") + "\n";

        messageCreateEvent
                .getChannel()
                .sendMessage(new EmbedBuilder()
                        .setTimestampToNow()
                        .setColor(Color.GREEN)
                        .setAuthor("Tournament status")
                        .setDescription(tournamentStatus));
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
    }
}

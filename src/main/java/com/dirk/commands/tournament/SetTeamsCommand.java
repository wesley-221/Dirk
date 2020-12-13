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
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandArgument;
import com.dirk.models.command.CommandArgumentType;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.tournament.Tournament;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SetTeamsCommand extends Command {
    private final TournamentRepository tournamentRepository;

    @Autowired
    public SetTeamsCommand(TournamentRepository tournamentRepository) {
        this.commandName = "setteams";
        this.description = "Set whether or not this tournament will be a 1v1 or a team vs team tournament.";
        this.group = "Tournament management";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("teams", "Enter `true` for team vs team, enter `false` for 1v1", CommandArgumentType.String));

        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        String isTeamTournament = (String) commandParams.stream().findFirst().get().getValue();

        if (!isTeamTournament.equals("true") && !isTeamTournament.equals("false")) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed(this.getCommandHelpFormat("Invalid `teams` argument given.\n\n"), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

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
                    .sendMessage(EmbedHelper.genericErrorEmbed("Unable to set the teams. You have to be the Server Owner or an Admin in order to run this.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        existingTournament.setIsTeamTournament(Boolean.valueOf(isTeamTournament));
        tournamentRepository.save(existingTournament);

        String message = Boolean.parseBoolean(isTeamTournament) ? "Set the tournament as a team tournament." : "Set the tournament as a solo tournament.";

        messageCreateEvent
                .getChannel()
                .sendMessage(EmbedHelper.genericSuccessEmbed(message, messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
    }
}

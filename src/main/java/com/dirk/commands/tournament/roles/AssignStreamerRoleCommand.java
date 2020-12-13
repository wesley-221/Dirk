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

package com.dirk.commands.tournament.roles;

import com.dirk.helper.EmbedHelper;
import com.dirk.helper.TournamentHelper;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandArgument;
import com.dirk.models.command.CommandArgumentType;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.tournament.Tournament;
import com.dirk.repositories.TournamentRepository;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssignStreamerRoleCommand extends Command {
    private final TournamentRepository tournamentRepository;

    @Autowired
    public AssignStreamerRoleCommand(TournamentRepository tournamentRepository) {
        this.commandName = "assignstreamerrole";
        this.description = "Assign what the streamer role will be. This role will be able to configure the tournament.";
        this.group = "Tournament management";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("tournament role", "Type out the name of the role or highlight the role.", CommandArgumentType.String));

        this.tournamentRepository = tournamentRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        Server server = messageCreateEvent.getServer().get();
        String enteredRole = (String) commandParams.stream().findFirst().get().getValue();

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
                    .sendMessage(EmbedHelper.genericErrorEmbed("Unable to assign the Streamer role. You have to be the Server Owner or an Admin in order to run this.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        Role role = TournamentHelper.getRoleByString(enteredRole, server);

        // Check if the role exists
        if (role == null) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("The given role was not found.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        } else {
            existingTournament.setStreamerRoleSnowflake(role.getIdAsString());
            tournamentRepository.save(existingTournament);

            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericSuccessEmbed("Set the streamer role to " + role.getMentionTag() + ".", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        }
    }
}

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

package com.dirk.commands.server_moderation;

import com.dirk.helper.EmbedHelper;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandArgument;
import com.dirk.models.command.CommandArgumentType;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.entities.ServerJoinRole;
import com.dirk.repositories.ServerJoinRoleRepository;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SetJoinRoleCommand extends Command {
    private final ServerJoinRoleRepository serverJoinRoleRepository;

    @Autowired
    public SetJoinRoleCommand(ServerJoinRoleRepository serverJoinRoleRepository) {
        this.commandName = "setjoinrole";
        this.description = "Give the user that just joined this guild the given role.";
        this.group = "Server moderation";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("role highlight", "The role that will be given to the user (use the highlight, ie @User)", CommandArgumentType.SingleString));

        this.serverJoinRoleRepository = serverJoinRoleRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter roleParam = commandParams.stream().findFirst().orElse(null);

        if (roleParam == null) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("Something went wrong", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        String role = (String) roleParam.getValue();
        Server server = messageCreateEvent.getServer().orElse(null);

        role = role.replace("<@&", "").replace(">", "");

        if (server == null) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("Something went wrong", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        ServerJoinRole serverJoinRole = new ServerJoinRole();

        serverJoinRole.setRoleSnowflake(role);
        serverJoinRole.setServerSnowflake(server.getIdAsString());

        serverJoinRoleRepository.save(serverJoinRole);

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("Users will now get the role <@&" + role + "> when they join this guild.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
    }
}

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
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CreateTeamCommand extends Command {
    private final String TEAM_CATEGORY = "Teams";
    private final String TEAM_VOICE_CATEGORY = "Team voice";

    public CreateTeamCommand() {
        this.commandName = "createteam";
        this.description = "Creates a text and voice channel and adds the highlighted users to the team.";
        this.group = "Server moderation";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("team name", "The name of the team \n`user highlights`: The users you want to add to the team. Tag one or multiple users with `@Username`", CommandArgumentType.String));
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        String commandString = (String) commandParams.get(0).getValue();
        List<String> splittedString = Arrays.stream(commandString.split("(?=<@[0-9!]+>)")).map(String::trim).collect(Collectors.toList());
        String teamName = splittedString.subList(0, 1).get(0);
        List<String> highlightedUsers = splittedString.subList(1, splittedString.size());

        Server server = messageCreateEvent.getServer().get();

        server.createRoleBuilder()
                .setColor(new Color(96, 125, 136))
                .setName(teamName)
                .setMentionable(true)
                .create().thenAccept(role -> {
                    if (server.getChannelCategoriesByName(TEAM_CATEGORY).isEmpty()) {
                        server.createChannelCategoryBuilder()
                                .setName(TEAM_CATEGORY)
                                .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build())
                                .addPermissionOverwrite(role, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES, PermissionType.MANAGE_MESSAGES).build())
                                .create()
                                .whenComplete((createdChannelCategory, throwable) -> {
                                    server.createTextChannelBuilder()
                                            .setName(teamName)
                                            .setCategory(createdChannelCategory)
                                            .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build())
                                            .addPermissionOverwrite(role, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES, PermissionType.MANAGE_MESSAGES).build())
                                            .create();
                                });
                    } else {
                        ChannelCategory channelCategory = server.getChannelCategoriesByName(TEAM_CATEGORY).get(0);
                        channelCategory
                                .createUpdater()
                                .addPermissionOverwrite(role, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES, PermissionType.MANAGE_MESSAGES).build())
                                .update();

                        server.createTextChannelBuilder()
                                .setName(teamName)
                                .setCategory(channelCategory)
                                .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build())
                                .addPermissionOverwrite(role, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES, PermissionType.MANAGE_MESSAGES).build())
                                .create();
                    }

                    if (server.getChannelCategoriesByName(TEAM_VOICE_CATEGORY).isEmpty()) {
                        server.createChannelCategoryBuilder()
                                .setName(TEAM_VOICE_CATEGORY)
                                .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build())
                                .addPermissionOverwrite(role, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build())
                                .create()
                                .whenComplete((createdChannelCategory, throwable) -> {
                                    server.createVoiceChannelBuilder()
                                            .setName(teamName)
                                            .setCategory(createdChannelCategory)
                                            .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build())
                                            .addPermissionOverwrite(role, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES, PermissionType.MANAGE_MESSAGES).build())
                                            .create();
                                });
                    } else {
                        ChannelCategory channelCategory = server.getChannelCategoriesByName(TEAM_VOICE_CATEGORY).get(0);
                        channelCategory
                                .createUpdater()
                                .addPermissionOverwrite(role, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES).build())
                                .update();

                        server.createVoiceChannelBuilder()
                                .setName(teamName)
                                .setCategory(channelCategory)
                                .addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.READ_MESSAGES).build())
                                .addPermissionOverwrite(role, new PermissionsBuilder().setAllowed(PermissionType.READ_MESSAGES, PermissionType.MANAGE_MESSAGES).build())
                                .create();
                    }

                    highlightedUsers.forEach(userString -> {
                        userString = userString.replace("<", "")
                                .replace("@", "")
                                .replace(">", "")
                                .replace("!", "");

                        server.getMemberById(userString).ifPresent(user1 ->
                                user1.addRole(role)
                                        .thenAccept(unused -> System.out.println("Successfully assigned role to " + user1.getDiscriminatedName()))
                                        .exceptionally(ExceptionLogger.get()));
                    });
                }).whenComplete((role, throwable) ->
                        messageCreateEvent
                                .getChannel()
                                .sendMessage(EmbedHelper.genericSuccessEmbed("Successfully created the role, voice and text channel for `" + teamName + "`! \n\nAccess has been given to " + String.join(", ", highlightedUsers) + ".", messageCreateEvent.getMessageAuthor().getDiscriminatedName())));
    }
}

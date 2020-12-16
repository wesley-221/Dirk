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
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SetupVerificationCommand extends Command {
    public static final String VERIFIED_ROLE = "Verified";
    public static final String VERIFICATION_CHANNEL_NAME = "verification";

    public SetupVerificationCommand() {
        this.commandName = "setupverification";
        this.description = "Setup verification so that everyone has to verify themselves before being able to talk in text channels.";
        this.group = "Server moderation";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("information channel", "Highlight the channel which should always be visible even though the user isn't verified yet.", CommandArgumentType.String));
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        Server server = messageCreateEvent.getServer().orElse(null);
        CommandParameter informationChannelParameter = commandParams.stream().findFirst().orElse(null);

        if (server != null) {
            if (informationChannelParameter == null) {
                messageCreateEvent
                        .getChannel()
                        .sendMessage(EmbedHelper.genericErrorEmbed(this.getCommandHelpFormat(), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));

                return;
            }

            ServerChannel informationChannel = server.getChannelById(informationChannelParameter.getValue().toString().replace("<", "").replace(">", "").replace("#", "")).orElse(null);

            if (informationChannel == null) {
                messageCreateEvent
                        .getChannel()
                        .sendMessage(EmbedHelper.genericErrorEmbed(this.getCommandHelpFormat("Unable to find the given channel. \n\n"), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                return;
            }

            Role verifiedRole = server.getRolesByName(VERIFIED_ROLE).stream().findFirst().orElse(null);

            if (verifiedRole == null) {
                PermissionsBuilder newVerifiedRolePermissions = new PermissionsBuilder()
                        .setDenied(PermissionType.CHANGE_NICKNAME);

                PermissionsBuilder newEveryoneRolePermissions = new PermissionsBuilder();

                server.getEveryoneRole().getPermissions().getAllowedPermission().forEach(newEveryoneRolePermissions::setAllowed);
                server.getEveryoneRole().getPermissions().getDeniedPermissions().forEach(newEveryoneRolePermissions::setDenied);
                newEveryoneRolePermissions.setDenied(PermissionType.CHANGE_NICKNAME);

                // Deny change nickname
                server.getEveryoneRole()
                        .createUpdater()
                        .setPermissions(newEveryoneRolePermissions.build())
                        .update();

                server.createRoleBuilder()
                        .setName(VERIFIED_ROLE)
                        .setMentionable(false)
                        .setPermissions(newVerifiedRolePermissions.build())
                        .create()
                        .whenComplete((newVerifiedRole, throwable) -> {
                            PermissionsBuilder permissionVerifiedRole = new PermissionsBuilder()
                                    .setDenied(PermissionType.READ_MESSAGES);

                            server.createTextChannelBuilder()
                                    .setName(VERIFICATION_CHANNEL_NAME)
                                    .addPermissionOverwrite(newVerifiedRole, permissionVerifiedRole.build())
                                    .create()
                                    .whenComplete((verificationTextChannel, throwable1) -> {
                                        updatePermission(server, newVerifiedRole, informationChannel.getName());
                                    });
                        });
            } else {
                updatePermission(server, verifiedRole, informationChannel.getName());
            }

            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericSuccessEmbed("Successfully updated all channels for verification. \n\n" +
                            "**NOTE:** Make sure to check all information related channels `@everyone` SEND_MESSAGE permission.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        }
    }

    public void updatePermission(Server server, Role verifiedRole, String exludeFromVerification) {
        server.getChannels().forEach(serverChannel -> {
            if (serverChannel.getType() == ChannelType.SERVER_TEXT_CHANNEL) {
                ServerTextChannel channel = serverChannel.asServerTextChannel().orElse(null);

                if (channel != null) {
                    // Channel is the verification channel
                    if (channel.getName().equals(VERIFICATION_CHANNEL_NAME)) {
                        PermissionsBuilder verifiedPermissions = new PermissionsBuilder()
                                .setDenied(PermissionType.READ_MESSAGES);

                        PermissionsBuilder everyonePermissions = new PermissionsBuilder();

                        channel.getOverwrittenPermissions().forEach((aLong, permissions) -> {
                            permissions.getAllowedPermission().forEach(everyonePermissions::setAllowed);
                            permissions.getDeniedPermissions().forEach(everyonePermissions::setDenied);
                        });

                        everyonePermissions
                                .setAllowed(PermissionType.READ_MESSAGES);

                        channel
                                .createUpdater()
                                .addPermissionOverwrite(verifiedRole, verifiedPermissions.build())
                                .addPermissionOverwrite(server.getEveryoneRole(), everyonePermissions.build())
                                .update();
                    } else {
                        if (exludeFromVerification != null && channel.getName().equals(exludeFromVerification))
                            return;

                        PermissionsBuilder verifiedPermissions = new PermissionsBuilder()
                                .setAllowed(PermissionType.SEND_MESSAGES);

                        PermissionsBuilder everyonePermissions = new PermissionsBuilder();

                        channel.getOverwrittenPermissions().forEach((aLong, permissions) -> {
                            if (aLong == server.getEveryoneRole().getId()) {
                                permissions.getAllowedPermission().forEach(everyonePermissions::setAllowed);
                                permissions.getDeniedPermissions().forEach(everyonePermissions::setDenied);
                            }
                        });

                        everyonePermissions
                                .setDenied(PermissionType.SEND_MESSAGES);

                        channel
                                .createUpdater()
                                .addPermissionOverwrite(verifiedRole, verifiedPermissions.build())
                                .addPermissionOverwrite(server.getEveryoneRole(), everyonePermissions.build())
                                .update();
                    }
                }
            }
        });
    }
}

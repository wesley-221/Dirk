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
import com.dirk.models.OsuVerification;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandParameter;
import com.dirk.repositories.OsuVerifyRepository;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class VerifyCommand extends Command {
    OsuVerifyRepository osuVerifyRepository;
    @Value("${webserver.url}")
    String webServer;
    @Value("${bot.name}")
    String botName;

    @Autowired
    public VerifyCommand(OsuVerifyRepository osuVerifyRepository) {
        this.commandName = "verify";
        this.description = "Verify yourself through the osu! api";
        this.group = "Server moderation";

        this.guildOnly = true;

        this.osuVerifyRepository = osuVerifyRepository;
    }

    @Override
    @Transactional
    public void execute(MessageCreateEvent messageCreateEvent) {
        Server server = messageCreateEvent.getServer().orElse(null);

        if (server == null) {
            return;
        }

        ServerTextChannel textChannel = server.getTextChannelsByName(SetupVerificationCommand.VERIFICATION_CHANNEL_NAME).stream().findFirst().orElse(null);
        Role verifiedRole = server.getRolesByName(SetupVerificationCommand.VERIFIED_ROLE).stream().findFirst().orElse(null);

        if (verifiedRole == null) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("This server hasn't been setup for verification yet.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        if (textChannel != null && messageCreateEvent.getChannel().getId() != textChannel.getId()) {
            messageCreateEvent
                    .getChannel()
                    .sendMessage(EmbedHelper.genericErrorEmbed("You can only use this command in " + textChannel.getMentionTag(), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        OsuVerification osuVerification = new OsuVerification();

        osuVerification.setUserSnowflake(messageCreateEvent.getMessageAuthor().getIdAsString());
        osuVerification.setServerSnowflake(messageCreateEvent.getServer().get().getIdAsString());
        osuVerification.setUserSecret(UUID.randomUUID());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);

        osuVerification.setExpireDate(calendar.getTime());

        osuVerifyRepository.save(osuVerification);

        messageCreateEvent
                .getChannel()
                .sendMessage("<@" + messageCreateEvent.getMessageAuthor().getId() + ">, the verification link has been sent to you privately. \n\n" +
                        "**If you didn't receive a DM, enable the following setting: Settings > Privacy & Safety > Allow direct messages from server members.** \n" +
                        "**Once you have changed this setting, re-run the command again.** This will send a new link for you to verify yourself.\n" +
                        "Once you've received the DM and followed the link, you can disable this setting again.");

        messageCreateEvent
                .getMessageAuthor()
                .asUser()
                .ifPresent(user -> user.sendMessage("The server **" + messageCreateEvent.getServer().get().getName() + "** wants to know who you are. \n" +
                        "This link will redirect you to the **official osu! website**, asking you to give permission to **" + botName + "** so he can read your profile. \n" +
                        "Once the authentication process is done, he will then change your name to whatever he gets off your profile. \n\n" +
                        "Click on the following link to start the authentication process: " + webServer + "/verify/" + osuVerification.getUserSecretAsURLSafeString() + "\n" +
                        "__**Note:** This link will expire in **1 hour**.__"));
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
    }
}

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

package com.dirk.listeners;

import com.dirk.commands.server_moderation.SetupVerificationCommand;
import com.dirk.helper.EmbedHelper;
import com.dirk.helper.RegisterListener;
import com.dirk.models.entities.ServerTraffic;
import com.dirk.repositories.ServerTrafficRepository;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerTrafficListener implements ServerMemberJoinListener, ServerMemberLeaveListener, RegisterListener {
    private final ServerTrafficRepository serverTrafficRepository;

    @Autowired
    public ServerTrafficListener(ServerTrafficRepository serverTrafficRepository) {
        this.serverTrafficRepository = serverTrafficRepository;
    }

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        ServerTraffic serverTraffic = serverTrafficRepository.findByServerSnowflake(event.getServer().getId());

        if (serverTraffic != null) {
            if (serverTraffic.getShowJoining()) {
                event.getServer().getTextChannelById(serverTraffic.getChannelSnowflake()).ifPresent(serverTextChannel -> serverTextChannel.sendMessage(EmbedHelper.personJoinedServer(serverTraffic.getJoinMessage(), event.getUser())));
            }
        }

        // Check for the verifying process
        Role verifiedRole = event.getServer().getRolesByName(SetupVerificationCommand.VERIFIED_ROLE).stream().findFirst().orElse(null);

        if (verifiedRole != null) {
            ServerTextChannel textChannel = event.getServer().getTextChannelsByName(SetupVerificationCommand.VERIFICATION_CHANNEL_NAME).stream().findFirst().orElse(null);

            if (textChannel != null) {
                textChannel.sendMessage("Hello " + event.getUser().getMentionTag() + "! In order for you to view all channels, run the command `d!authenticate` and follow the instructions.");
            }
        }
    }

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        ServerTraffic serverTraffic = serverTrafficRepository.findByServerSnowflake(event.getServer().getId());

        if (serverTraffic != null) {
            if (serverTraffic.getShowLeaving()) {
                event.getServer().getTextChannelById(serverTraffic.getChannelSnowflake()).ifPresent(serverTextChannel -> serverTextChannel.sendMessage(EmbedHelper.personLeaveServer(serverTraffic.getJoinMessage(), event.getUser())));
            }
        }
    }
}

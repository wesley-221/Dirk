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

package com.dirk.commands.server_moderation.greeting;

import com.dirk.helper.EmbedHelper;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.entities.ServerTraffic;
import com.dirk.repositories.ServerTrafficRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ToggleJoinCommand extends Command {
    private final ServerTrafficRepository serverTrafficRepository;

    @Autowired
    public ToggleJoinCommand(ServerTrafficRepository serverTrafficRepository) {
        this.commandName = "togglejoin";
        this.description = "Enable or disable the traffic message for when someone joins this guild. The message will be send in the channel where it was executed from.";
        this.group = "Server moderation";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.serverTrafficRepository = serverTrafficRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
        long serverSnowflake = messageCreateEvent.getServer().get().getId();
        long channelSnowflake = messageCreateEvent.getChannel().getId();

        ServerTraffic serverTraffic = serverTrafficRepository.findByServerSnowflakeAndChannelSnowflake(serverSnowflake, channelSnowflake);

        if (serverTraffic == null) {
            serverTraffic = new ServerTraffic(serverSnowflake, channelSnowflake, true, false);
        } else {
            serverTraffic.setShowJoining(!serverTraffic.getShowJoining());
        }

        serverTrafficRepository.save(serverTraffic);

        String parsedMessage = "You will " +
                (serverTraffic.getShowJoining() ? "now receive" : "no longer") +
                " a message when someone joins this guild." +
                (serverTraffic.getShowJoining() ? "\n\nAn example message is shown below." : "");

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed(parsedMessage, messageCreateEvent.getMessageAuthor().getDiscriminatedName()));

        if (serverTraffic.getShowJoining()) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.personJoinedServer(serverTraffic.getJoinMessage(), Objects.requireNonNull(messageCreateEvent.getMessageAuthor().asUser().orElse(null))));
        }
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
    }
}

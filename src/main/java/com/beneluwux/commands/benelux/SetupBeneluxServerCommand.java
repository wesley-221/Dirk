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

package com.beneluwux.commands.benelux;

import com.beneluwux.helper.Emoji;
import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandParameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
public class SetupBeneluxServerCommand extends Command {
    private final String discordCommandPrefix;

    @Autowired
    public SetupBeneluxServerCommand(@Value("${discord.prefix}") String discordCommandPrefix) {
        this.commandName = "setupbeneluxserver";
        this.description = "Setup the embed message for the Beneluwux server.";
        this.group = "Benelux";

        this.guildOnly = true;
        this.requiresAdmin = true;

        this.discordCommandPrefix = discordCommandPrefix;
    }

    @Override
    public void execute(MessageCreateEvent message) {
        String stringBuilder = "Click on any of the emoji's on this message in order to get the appropriate role. \n" +
                ":flag_nl: : My nationality is **Dutch**! \n" +
                ":flag_be: : My nationality is **Belgian**! \n" +
                ":flag_lu: : My nationality is **Luxembourgian**! \n" +
                ":beer: : I want to have access to the drinking channels, here we post announcements when we do drinking games over discord on this server. \n" +
                ":birthday: : I want to get pings for when it's someones birthday. Want to enlist your birthday? Run the command `" + discordCommandPrefix + "birthday`";

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.RED)
                .setDescription(stringBuilder);

        message.getChannel().sendMessage(embedBuilder).whenComplete((sendMessage, throwable) -> {
            sendMessage.addReaction(Emoji.NETHERLANDS_FLAG);
            sendMessage.addReaction(Emoji.BELGIUM_FLAG);
            sendMessage.addReaction(Emoji.LUXEMBOURG_FLAG);
            sendMessage.addReaction(Emoji.BEER);
            sendMessage.addReaction(Emoji.CAKE);
        });
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {

    }
}

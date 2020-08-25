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

package com.dirk.commands.basic;

import com.dirk.models.command.Command;
import com.dirk.models.command.CommandParameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

@Component
public class InviteCommand extends Command {
    @Value("${bot.name}")
    private String BOT_NAME;

    @Value("${bot.github}")
    private String GITHUB_LINK;

    @Value("${bot.github-issues}")
    private String GITHUB_ISSUES_LINK;

    public InviteCommand() {
        this.commandName = "invite";
        this.description = "Sends a message with the invite link and Github information.";
        this.group = "Basic";
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
        String embedDescription = "**Add " + BOT_NAME + " to your Discord guild:** \n" +
                messageCreateEvent.getApi().createBotInvite() + "\n\n" +
                "**Bug/feature requests:** \n" +
                "File an issue: " + GITHUB_ISSUES_LINK + "\n\n" +
                "**Source code:** \n" +
                GITHUB_LINK;

        EmbedBuilder embedBuilder = new EmbedBuilder()
                .setThumbnail(messageCreateEvent.getApi().getYourself().getAvatar().getUrl().toString())
                .setTimestampToNow()
                .setFooter(BOT_NAME)
                .setColor(new Color(53, 84, 171))
                .setDescription(embedDescription);

        messageCreateEvent.getChannel().sendMessage(embedBuilder);
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
    }
}

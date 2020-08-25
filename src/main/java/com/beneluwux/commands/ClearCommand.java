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

package com.beneluwux.commands;

import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandArgumentType;
import com.beneluwux.models.command.CommandParameter;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClearCommand extends Command {
    public ClearCommand() {
        this.commandName = "clear";
        this.description = "Clear an X amount of lines in the channel it is executed from.";

        this.requiresAdmin = true;
        this.guildOnly = true;

        this.commandArguments.add(new CommandArgument("lines", "The amount of lines you want to clear", CommandArgumentType.Integer));
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter commandParameter = commandParams.get(0);

        if (!commandParameter.isParsedCorrectly()) {
            messageCreateEvent.getChannel().sendMessage(getIncorrectCommandHelpFormat());
            return;
        }

        int messagesToDelete = (Integer) commandParameter.getValue() > 0 ? (Integer) commandParameter.getValue() : 5;

        messageCreateEvent.getServerTextChannel().ifPresent(serverTextChannel -> serverTextChannel.getMessages(messagesToDelete).whenCompleteAsync((messages, throwable) -> messages.deleteAll()));
    }
}

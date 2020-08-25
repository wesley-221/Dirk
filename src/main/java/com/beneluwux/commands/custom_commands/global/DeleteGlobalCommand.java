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

package com.beneluwux.commands.custom_commands.global;

import com.beneluwux.helper.EmbedHelper;
import com.beneluwux.meta.CustomCommandComponent;
import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandArgumentType;
import com.beneluwux.models.command.CommandParameter;
import com.beneluwux.models.entities.CustomCommand;
import com.beneluwux.repositories.CustomCommandRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteGlobalCommand extends Command {
    private final CustomCommandRepository customCommandRepository;
    private final CustomCommandComponent customCommandComponent;

    @Autowired
    public DeleteGlobalCommand(CustomCommandRepository customCommandRepository, CustomCommandComponent customCommandComponent) {
        this.commandName = "deleteglobalcommand";
        this.requiresBotOwner = true;

        this.commandArguments.add(new CommandArgument("command name", "The name of the command to delete", CommandArgumentType.SingleString));
        this.customCommandRepository = customCommandRepository;
        this.customCommandComponent = customCommandComponent;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {

    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter commandKey = commandParams.get(0);
        CustomCommand customCommand = customCommandRepository.findByNameAndServerSnowflake((String) commandKey.getValue(), 0L);

        if (customCommand == null) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("The global command `" + commandKey.getValue() + "` doesn't exist.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        } else {
            customCommandRepository.delete(customCommand);
            customCommandComponent.refreshCustomCommandsFromJPA();

            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("The global command `" + commandKey.getValue() + "` has been deleted.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        }
    }
}

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

package com.dirk.commands.custom_commands.global;

import com.dirk.helper.EmbedHelper;
import com.dirk.meta.CustomCommandComponent;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandArgument;
import com.dirk.models.command.CommandArgumentType;
import com.dirk.models.command.CommandParameter;
import com.dirk.models.entities.CustomCommand;
import com.dirk.repositories.CustomCommandRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CreateGlobalCommand extends Command {
    private final ApplicationContext applicationContext;
    private final CustomCommandRepository customCommandRepository;
    private final CustomCommandComponent customCommandComponent;

    @Autowired
    public CreateGlobalCommand(ApplicationContext applicationContext, CustomCommandRepository customCommandRepository, CustomCommandComponent customCommandComponent) {
        this.commandName = "createglobalcommand";
        this.description = "Create a global command that can be used in all guilds where I'm in.";
        this.group = "Custom commands";

        this.requiresBotOwner = true;

        this.commandArguments.add(new CommandArgument("command name", "The name of the command to create", CommandArgumentType.SingleString));
        this.commandArguments.add(new CommandArgument("command output", "The output of the command", CommandArgumentType.String));

        this.applicationContext = applicationContext;
        this.customCommandRepository = customCommandRepository;
        this.customCommandComponent = customCommandComponent;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter commandKey = commandParams.get(0);
        CommandParameter commandMessage = commandParams.get(1);

        Map<String, Command> staticCommands = applicationContext.getBeansOfType(Command.class);
        boolean commandWasFound = false;

        // Loop through static commands
        for (Command command : staticCommands.values()) {
            if (command.getCommandName().equals(commandKey.getValue())) {
                commandWasFound = true;
                break;
            }
        }

        // Check if the custom command exists
        if (!commandWasFound) {
            commandWasFound = customCommandRepository.existsByNameAndServerSnowflake((String) commandKey.getValue(), 0L);
        }

        // There was a command found
        if (commandWasFound) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("There is already a global command with the name `" + commandKey.getValue() + "`.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        CustomCommand customCommand = new CustomCommand(0L, messageCreateEvent.getMessageAuthor().getId(), (String) commandKey.getValue(), (String) commandMessage.getValue());
        customCommandRepository.save(customCommand);
        customCommandComponent.refreshCustomCommandsFromJPA();

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("Created the global command `" + commandKey.getValue() + "`: `" + commandMessage.getValue() + "`", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
    }
}

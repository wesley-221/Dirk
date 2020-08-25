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

package com.beneluwux.commands.custom_commands.guild;

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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CreateGuildCommand extends Command {
    private final ApplicationContext applicationContext;
    private final CustomCommandRepository customCommandRepository;
    private final CustomCommandComponent customCommandComponent;

    @Autowired
    public CreateGuildCommand(ApplicationContext applicationContext, CustomCommandRepository customCommandRepository, CustomCommandComponent customCommandComponent) {
        this.commandName = "createguildcommand";
        this.requiresAdmin = true;
        this.guildOnly = true;

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

        // There was a command found
        if (commandWasFound) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("The guild command `" + commandKey.getValue() + "` already exists.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        // Check if a global custom command exists
        if (customCommandRepository.existsByNameAndServerSnowflake((String) commandKey.getValue(), 0L)) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("There is already a global command with the name `" + commandKey.getValue() + "`.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        // Check if a guild custom command exists
        if (customCommandRepository.existsByNameAndServerSnowflake((String) commandKey.getValue(), messageCreateEvent.getServer().get().getId())) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("There is already a guild command with the name `" + commandKey.getValue() + "`.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        CustomCommand customCommand = new CustomCommand(messageCreateEvent.getServer().get().getId(), messageCreateEvent.getMessageAuthor().getId(), (String) commandKey.getValue(), (String) commandMessage.getValue());
        customCommandRepository.save(customCommand);
        customCommandComponent.refreshCustomCommandsFromJPA();

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("Created the guild command `" + commandKey.getValue() + "`: `" + commandMessage.getValue() + "`", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
    }
}

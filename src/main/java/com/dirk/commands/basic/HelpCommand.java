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

import com.dirk.helper.EmbedHelper;
import com.dirk.models.command.Command;
import com.dirk.models.command.CommandArgument;
import com.dirk.models.command.CommandArgumentType;
import com.dirk.models.command.CommandParameter;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class HelpCommand extends Command {
    ApplicationContext applicationContext;
    @Value("${discord.prefix}")
    private String discordCommandPrefix;

    @Autowired
    public HelpCommand(ApplicationContext applicationContext) {
        this.commandName = "help";
        this.description = "Send a message with all the commands available.";
        this.group = "Basic";

        this.addCommandArgument(new CommandArgument("command name", "The command to get help for", CommandArgumentType.SingleString, true));

        this.applicationContext = applicationContext;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter commandName = commandParams.get(0);
        Map<String, Command> allCommands = applicationContext.getBeansOfType(Command.class);
        Map<String, List<Command>> allCommandsPerGroup = new TreeMap<>();

        for (Command command : allCommands.values()) {
            if (command.isValidCommand()) {
                if (!allCommandsPerGroup.containsKey(command.getGroup())) {
                    allCommandsPerGroup.put(command.getGroup(), new ArrayList<>());
                }

                allCommandsPerGroup.get(command.getGroup()).add(command);
            }
        }

        // Show command list
        if (commandName.isOptional()) {
            StringBuilder commandString = new StringBuilder("To execute a command, use `")
                    .append(discordCommandPrefix)
                    .append("commandname`.\n")
                    .append("Want to know more about a specific command? Run `")
                    .append(discordCommandPrefix)
                    .append("help <commandname>` to view detailed information about the command. \n");

            allCommandsPerGroup.forEach((s, commands) -> {
                commandString.append("\n__").append(s).append("__\n");
                commands.forEach(command -> commandString.append("**").append(command.getCommandName()).append(":** ").append(command.getDescription()).append("\n"));
            });

            messageCreateEvent.getChannel().sendMessage(commandString.toString());
        }
        // Show specific command
        else {
            Command foundCommand = null;

            for (Command command : allCommands.values()) {
                if (command.getCommandName().equals(commandName.getValue())) {
                    foundCommand = command;
                    break;
                }
            }

            if (foundCommand == null) {
                messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("There is no command named `" + commandName.getValue() + "`.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            } else {
                messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed(foundCommand.getCommandHelpFormat(), messageCreateEvent.getMessageAuthor().getDiscriminatedName()).setAuthor(""));
            }
        }
    }
}

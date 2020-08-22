package com.beneluwux.listeners;

import com.beneluwux.helper.Log;
import com.beneluwux.helper.RegisterListener;
import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandParameter;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class CommandListener implements MessageCreateListener, RegisterListener {
    private final String discordCommandPrefix;
    private final Map<String, Command> allCommands;

    @Autowired
    public CommandListener(ApplicationContext applicationContext, @Value("${discord.prefix}") String discordCommandPrefix) {
        this.discordCommandPrefix = discordCommandPrefix;

        allCommands = applicationContext.getBeansOfType(Command.class);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        // Check if the author is not a bot
        if (messageCreateEvent.getMessageAuthor().isBotUser())
            return;

        // Check if the message starts with the command prefix
        if (!messageCreateEvent.getMessage().getContent().startsWith(discordCommandPrefix))
            return;

        // Remove the discord prefix
        List<String> commandSplit = new ArrayList<>(Arrays.asList(messageCreateEvent.getMessage().getContent().substring(discordCommandPrefix.length()).split(" ")));
        String commandName = commandSplit.get(0);
        commandSplit.remove(0);

        // Get the command by the given name
        Command command = this.getCommandByName(commandName);

        // Check if the command exists
        if (command != null) {
            // Check if the command has arguments
            if (command.hasCommandArguments()) {
                // Check if the arguments match
                if (command.getCommandArgumentsCount() != commandSplit.size()) {
                    messageCreateEvent.getChannel().sendMessage(command.getCommandHelpFormat(":no_entry: **There was an error while performing the command.**\n"));
                    return;
                }

                List<CommandParameter> commandParameters = new ArrayList<>();
                int index = 0;

                // Loop through all parameters
                for (CommandArgument commandArgument : command.getCommandArguments()) {
                    String commandSplitIndex = commandSplit.get(index);

                    // Check for the command types and parse them
                    switch (commandArgument.getCommandType()) {
                        case String:
                            commandParameters.add(new CommandParameter(commandArgument.getCommandKey(), commandSplitIndex, true));
                            break;
                        case Boolean:
                            commandParameters.add(new CommandParameter(commandArgument.getCommandKey(), Boolean.valueOf(commandSplitIndex), true));
                            break;
                        case Date:
                            CommandParameter commandParameter;

                            try {
                                Date formattedDate;
                                DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                formattedDate = format.parse(commandSplitIndex);

                                commandParameter = new CommandParameter(commandArgument.getCommandKey(), formattedDate, true);
                            } catch (Exception ex) {
                                commandParameter = new CommandParameter(commandArgument.getCommandKey(), new Date(), false);
                            }

                            commandParameters.add(commandParameter);
                            break;
                        case Integer:
                            commandParameters.add(new CommandParameter(commandArgument.getCommandKey(), Integer.parseInt(commandSplitIndex), true));
                            break;
                        default:
                            break;
                    }

                    index++;
                }

                command.execute(messageCreateEvent, commandParameters);
            }
            // The command has no arguments
            else {
                command.execute(messageCreateEvent);
            }

            Log.info(String.format("%s ran the command: %s", messageCreateEvent.getMessageAuthor().getDiscriminatedName(), commandName));
        }
    }

    /**
     * Get a command by the given name
     *
     * @param commandName the name of the command
     * @return the command
     */
    private Command getCommandByName(String commandName) {
        Command foundCommand = null;

        for (Command command : this.allCommands.values()) {
            if (command.getCommandName().equals(commandName)) {
                foundCommand = command;
            }
        }

        return foundCommand;
    }
}
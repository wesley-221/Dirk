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
    private final List<Command> allCommands = new ArrayList<>();

    @Autowired
    public CommandListener(ApplicationContext applicationContext, @Value("${discord.prefix}") String discordCommandPrefix) {
        this.discordCommandPrefix = discordCommandPrefix;

        // Validate commands
        for (Command command : applicationContext.getBeansOfType(Command.class).values()) {
            if(command.getCommandName() == null) {
                Log.error("Unable to register the command " + command.getClass() + ". You have to set the command name in order for it to be recognized.");
            }
            else {
                allCommands.add(command);
            }
        }
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
            // Check if the command requires administrator privileges
            if(command.getRequiresAdmin() != null && command.getRequiresAdmin()) {
                // The user is not an administrator
                if(!messageCreateEvent.getMessageAuthor().isServerAdmin()) {
                    messageCreateEvent.getChannel().sendMessage(":no_entry: **There was an error while performing the command.** \nYou have to be an administrator to run this command.");
                    return;
                }
            }

            // Check if the command has arguments
            if (command.hasCommandArguments()) {
                // Check if the arguments match
                if (command.getCommandArgumentsCount() != commandSplit.size()) {
                    messageCreateEvent.getChannel().sendMessage(command.getIncorrectCommandHelpFormat());
                    return;
                }

                List<CommandParameter> commandParameters = new ArrayList<>();
                int index = 0;

                // Loop through all parameters
                for (CommandArgument commandArgument : command.getCommandArguments()) {
                    String commandSplitIndex = commandSplit.get(index);

                    CommandParameter commandParameter;

                    // Check for the command types and parse them
                    switch (commandArgument.getCommandType()) {
                        case String:
                            commandParameters.add(new CommandParameter(commandArgument.getCommandKey(), commandSplitIndex, true));
                            break;
                        case Boolean:
                            commandParameters.add(new CommandParameter(commandArgument.getCommandKey(), Boolean.valueOf(commandSplitIndex), true));
                            break;
                        case Date:
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
                            try {
                                Integer parsed = Integer.parseInt(commandSplitIndex);
                                commandParameters.add(new CommandParameter(commandArgument.getCommandKey(), parsed, true));
                            }
                            catch(Exception ex) {
                                commandParameters.add(new CommandParameter(commandArgument.getCommandKey(), -1, false));
                            }

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

        for (Command command : this.allCommands) {
            if (command.getCommandName().equals(commandName)) {
                foundCommand = command;
            }
        }

        return foundCommand;
    }
}

package com.beneluwux.listeners;

import com.beneluwux.helper.EmbedHelper;
import com.beneluwux.helper.Log;
import com.beneluwux.helper.RegisterListener;
import com.beneluwux.meta.CustomCommandComponent;
import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandArgumentType;
import com.beneluwux.models.command.CommandParameter;
import com.beneluwux.models.entities.CustomCommand;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class CommandListener implements MessageCreateListener, RegisterListener {
    private final String discordCommandPrefix;
    private final CustomCommandComponent customCommandComponent;
    private final List<Command> allCommands = new ArrayList<>();
    private List<CustomCommand> allCustomCommands;

    @Autowired
    public CommandListener(ApplicationContext applicationContext, @Value("${discord.prefix}") String discordCommandPrefix, CustomCommandComponent customCommandComponent) {
        this.discordCommandPrefix = discordCommandPrefix;
        this.customCommandComponent = customCommandComponent;
        this.allCustomCommands = customCommandComponent.getAllCustomCommands();

        // Validate commands
        for (Command command : applicationContext.getBeansOfType(Command.class).values()) {
            if (command.getCommandName() == null) {
                Log.error("Unable to register the command " + command.getClass() + ". You have to set the command name in order for it to be recognized.");
            } else if (command.getCommandArgumentsCount() > 0) {
                int commandArgumentIndex = 1;
                boolean commandArgumentsValid = true;

                // Loop through all command arguments
                for (CommandArgument commandArgument : command.getCommandArguments()) {
                    // Check for String arguments
                    if (commandArgument.getType() == CommandArgumentType.String) {
                        // String argument was found, check if it is the last argument
                        if (commandArgumentIndex != command.getCommandArgumentsCount()) {
                            commandArgumentsValid = false;
                            break;
                        }
                    }

                    commandArgumentIndex++;
                }

                if (commandArgumentsValid) {
                    allCommands.add(command);
                    Log.info("Registered the command " + command.getClass().getName());
                } else {
                    Log.error("Unable to register the command " + command.getClass() + ". The String argument can only be last parameter. Use SingleString for a single word.");
                }
            } else {
                allCommands.add(command);
                Log.info("Registered the command " + command.getClass().getName());
            }
        }

        // Log the custom commands
        for (CustomCommand customCommand : this.allCustomCommands) {
            Log.info("Registered the custom " + (customCommand.getServerSnowflake() == 0L ? "global" : "guild") + " command " + customCommand.getName());
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

        this.allCustomCommands = customCommandComponent.getAllCustomCommands();

        // Remove the discord prefix
        List<String> commandSplit = new ArrayList<>(Arrays.asList(messageCreateEvent.getMessage().getContent().substring(discordCommandPrefix.length()).split(" ")));
        String commandName = commandSplit.get(0);
        commandSplit.remove(0);

        // Get the command by the given name
        Command command = this.getCommandByName(commandName);

        // Check if the command exists
        if (command != null) {
            // Check if the command requires owner privileges
            if (command.getRequiresBotOwner() != null && command.getRequiresBotOwner()) {
                if (!messageCreateEvent.getMessageAuthor().isBotOwner()) {
                    messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("You have to be the owner of the bot to use this command.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                    return;
                }
            }

            // Check if the command requires administrator privileges
            if (command.getRequiresAdmin() != null && command.getRequiresAdmin()) {
                // The user is not an administrator
                if (!messageCreateEvent.getMessageAuthor().isServerAdmin()) {
                    messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("You have to be an administrator to use this command.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                    return;
                }
            }

            // Check if the command has arguments
            if (command.hasCommandArguments()) {
                // Check if last argument is a String
                if (command.getCommandArguments().get(command.getCommandArgumentsCount() - 1).getType() == CommandArgumentType.String) {
                    String commandArgumentsString = String.join(" ", commandSplit);
                    commandSplit = new ArrayList<>(Arrays.asList(commandArgumentsString.split(" ", command.getCommandArgumentsCount())));
                }

                // Check if the arguments match
                if (command.getCommandArgumentsCount() != commandSplit.size()) {
                    messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed(command.getIncorrectCommandHelpFormat(), messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
                    return;
                }

                List<CommandParameter> commandParameters = new ArrayList<>();
                int index = 0;

                // Loop through all parameters
                for (CommandArgument commandArgument : command.getCommandArguments()) {
                    String commandSplitIndex = commandSplit.get(index);

                    CommandParameter commandParameter;

                    // Check for the command types and parse them
                    switch (commandArgument.getType()) {
                        case SingleString:
                        case String:
                            commandParameters.add(new CommandParameter(commandArgument.getKey(), commandSplitIndex, true));
                            break;
                        case Boolean:
                            commandParameters.add(new CommandParameter(commandArgument.getKey(), Boolean.valueOf(commandSplitIndex), true));
                            break;
                        case Date:
                            try {
                                Date formattedDate;
                                DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                                formattedDate = format.parse(commandSplitIndex);

                                commandParameter = new CommandParameter(commandArgument.getKey(), formattedDate, true);
                            } catch (Exception ex) {
                                commandParameter = new CommandParameter(commandArgument.getKey(), new Date(), false);
                            }

                            commandParameters.add(commandParameter);
                            break;
                        case Integer:
                            try {
                                Integer parsed = Integer.parseInt(commandSplitIndex);
                                commandParameters.add(new CommandParameter(commandArgument.getKey(), parsed, true));
                            } catch (Exception ex) {
                                commandParameters.add(new CommandParameter(commandArgument.getKey(), -1, false));
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
        } else {
            CustomCommand customCommand = this.getCustomCommandByName(commandName);

            if (customCommand != null) {
                // Global command, run regardless of server
                if (customCommand.getServerSnowflake() == 0L) {
                    messageCreateEvent.getChannel().sendMessage(customCommand.getMessage());
                } else {
                    if (messageCreateEvent.isServerMessage() && messageCreateEvent.getServer().get().getId() == customCommand.getServerSnowflake()) {
                        messageCreateEvent.getChannel().sendMessage(customCommand.getMessage());
                    }
                }
            }
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

    /**
     * Get a custom command by the given name and server snowflake
     *
     * @param commandName the name of the custom command
     * @return the custom command
     */
    private CustomCommand getCustomCommandByName(String commandName) {
        CustomCommand foundCommand = null;

        for (CustomCommand customCommand : this.allCustomCommands) {
            if (customCommand.getName().equals(commandName)) {
                foundCommand = customCommand;
            }
        }

        return foundCommand;
    }
}

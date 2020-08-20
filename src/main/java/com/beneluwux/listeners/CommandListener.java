package com.beneluwux.listeners;

import com.beneluwux.helper.Log;
import com.beneluwux.helper.Settings;
import com.beneluwux.models.command.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandListener implements MessageCreateListener {
    private final DiscordApi discordApi;
    private final Settings settings;

    private final List<Command> commandList = new ArrayList<>();
    private final List<Command> initializedCommands = new ArrayList<>();

    public CommandListener(DiscordApi discordApi, Settings settings) {
        this.discordApi = discordApi;
        this.settings = settings;

        initializeCommands();
    }

    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        // Check if the author is not a bot
        if (messageCreateEvent.getMessageAuthor().isBotUser())
            return;

        // Check if the message starts with the command prefix
        if (!messageCreateEvent.getMessage().getContent().startsWith(settings.discordCommandPrefix))
            return;

        // Remove the discord prefix
        List<String> commandSplit = new ArrayList<>(Arrays.asList(messageCreateEvent.getMessage().getContent().substring(settings.discordCommandPrefix.length()).split(" ")));
        String commandName = commandSplit.get(0);
        commandSplit.remove(0);

        // Get the command by the given name
        Command command = this.getCommandByName(commandName);

        // Check if the command exists
        if (command != null) {
            // Check if the command has arguments
            if(command.hasCommandArguments()) {
                // Check if the arguments match
                if(command.getCommandArgumentsCount() != commandSplit.size()) {
                    messageCreateEvent.getChannel().sendMessage(command.getCommandHelpFormat(":no_entry: **There was an error while performing the command.**\n"));
                    return;
                }

                command.execute(messageCreateEvent, commandSplit);
            }
            // The command has no arguments
            else {
                command.execute(messageCreateEvent);
            }

            Log.info(String.format("%s ran the command: %s", messageCreateEvent.getMessageAuthor().getDiscriminatedName(), commandName));
        }
    }

    /**
     * Attempt to initialize all commands
     */
    private void initializeCommands() {
        // Iterate through all commands and attempt to initialize
        commandList.forEach(command -> {
            // Check if the command is properly initialized
            if (command.getCommandName() == null || command.getCommandName().isEmpty()) {
                Log.error("Unable to initialize the command " + command.getClass().getName() + ", you have to set the command name in order for it to work.");
            } else {
                initializedCommands.add(command);
                Log.info("Successfully initialized the command " + command.getClass().getName() + ".");
            }
        });
    }

    /**
     * Get a command by the given name
     *
     * @param commandName the name of the command
     * @return the command
     */
    private Command getCommandByName(String commandName) {
        return initializedCommands.stream().filter(command -> command.getCommandName().equals(commandName)).findFirst().orElse(null);
    }
}

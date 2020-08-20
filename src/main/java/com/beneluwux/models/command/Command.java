package com.beneluwux.models.command;

import com.beneluwux.helper.Settings;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {
    private final Settings settings = new Settings();

    protected String commandName;
    protected String description;
    protected List<CommandArgument> commandArguments = new ArrayList<>();

    public Command() {
    }

    public Command(String commandName) {
        this.commandName = commandName;
    }

    public Command(String commandName, String description) {
        this.commandName = commandName;
        this.description = description;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public List<CommandArgument> getCommandArguments() {
        return commandArguments;
    }

    public Boolean hasCommandArguments() {
        return this.commandArguments.size() > 0;
    }

    public Integer getCommandArgumentsCount() {
        return this.commandArguments.size();
    }

    public void addCommandArgument(CommandArgument commandArguments) {
        this.commandArguments.add(commandArguments);
    }

    public String getCommandHelpFormat() {
        StringBuilder mainMessage = new StringBuilder("**Command:** `");
        StringBuilder arguments;

        mainMessage.append(settings.discordCommandPrefix).append(commandName).append("`");

        if (this.description != null && !this.description.isEmpty()) {
            mainMessage.append(": ").append(description);
        }

        mainMessage.append("\n\n").append("**Format:** `").append(settings.discordCommandPrefix).append(commandName);

        if (this.hasCommandArguments()) {
            arguments = new StringBuilder("**Arguments:**\n");

            this.commandArguments.forEach(commandArgument -> {
                mainMessage.append(" <").append(commandArgument.getCommandKey()).append(">");
                arguments.append("`").append(commandArgument.getCommandKey()).append("`: ").append(commandArgument.getCommandPrompt()).append("\n");
            });

            mainMessage.append("`\n").append(arguments);
        }

        return mainMessage.toString();
    }

    public String getCommandHelpFormat(String extraMessage) {
        return extraMessage + getCommandHelpFormat();
    }

    public abstract void execute(MessageCreateEvent messageCreateEvent);
    public abstract void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams);
}

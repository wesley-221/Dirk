package com.beneluwux.models.command;

public class CommandArgument {
    private String commandKey;
    private String commandPrompt;
    private CommandArgumentType commandType;

    public CommandArgument(String commandKey, String commandPrompt, CommandArgumentType commandType) {
        this.commandKey = commandKey;
        this.commandPrompt = commandPrompt;
        this.commandType = commandType;
    }

    public String getCommandKey() {
        return commandKey;
    }

    public void setCommandKey(String commandKey) {
        this.commandKey = commandKey;
    }

    public String getCommandPrompt() {
        return commandPrompt;
    }

    public void setCommandPrompt(String commandPrompt) {
        this.commandPrompt = commandPrompt;
    }

    public CommandArgumentType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandArgumentType commandType) {
        this.commandType = commandType;
    }
}

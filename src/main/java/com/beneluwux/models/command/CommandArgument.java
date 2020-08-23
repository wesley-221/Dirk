package com.beneluwux.models.command;

public class CommandArgument {
    private String key;
    private String prompt;
    private CommandArgumentType type;
    private Boolean optional;

    public CommandArgument(String key, String prompt, CommandArgumentType type) {
        this.key = key;
        this.prompt = prompt;
        this.type = type;
    }

    public CommandArgument(String key, String prompt, CommandArgumentType type, Boolean optional) {
        this(key, prompt, type);
        this.optional = optional;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public CommandArgumentType getType() {
        return type;
    }

    public void setType(CommandArgumentType type) {
        this.type = type;
    }

    public Boolean isOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }
}

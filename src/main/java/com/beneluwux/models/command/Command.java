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

package com.beneluwux.models.command;

import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public abstract class Command {
    protected String commandName;
    protected String description;
    protected List<CommandArgument> commandArguments = new ArrayList<>();
    protected Boolean requiresAdmin;
    protected Boolean requiresBotOwner;
    protected Boolean guildOnly;
    @Value("${discord.prefix}")
    private String discordCommandPrefix;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Boolean getRequiresAdmin() {
        return requiresAdmin;
    }

    public void setRequiresAdmin(Boolean requiresAdmin) {
        this.requiresAdmin = requiresAdmin;
    }

    public Boolean getRequiresBotOwner() {
        return requiresBotOwner;
    }

    public void setRequiresBotOwner(Boolean requiresBotOwner) {
        this.requiresBotOwner = requiresBotOwner;
    }

    public Boolean isGuildOnly() {
        return guildOnly;
    }

    public void setGuildOnly(Boolean guildOnly) {
        this.guildOnly = guildOnly;
    }

    public String getCommandHelpFormat() {
        StringBuilder mainMessage = new StringBuilder("**Command:** `");
        StringBuilder arguments;

        mainMessage.append(discordCommandPrefix).append(commandName).append("`");

        if (this.description != null && !this.description.isEmpty()) {
            mainMessage.append(": ").append(description);
        }

        mainMessage.append("\n\n").append("**Format:** `").append(discordCommandPrefix).append(commandName);

        if (this.hasCommandArguments()) {
            arguments = new StringBuilder("**Arguments:**\n");

            this.commandArguments.forEach(commandArgument -> {
                mainMessage.append(" <").append(commandArgument.getKey()).append(">");
                arguments.append("`").append(commandArgument.getKey()).append("`: ").append(commandArgument.getPrompt()).append("\n");
            });

            mainMessage.append("`\n").append(arguments);
        }

        return mainMessage.toString();
    }

    public String getCommandHelpFormat(String extraMessage) {
        return extraMessage + getCommandHelpFormat();
    }

    public String getIncorrectCommandHelpFormat() {
        return "**There was an error while performing the command.**\n\n" + getCommandHelpFormat();
    }

    public abstract void execute(MessageCreateEvent messageCreateEvent);

    public abstract void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams);
}

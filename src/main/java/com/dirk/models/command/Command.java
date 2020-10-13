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

package com.dirk.models.command;

import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public abstract class Command {
    protected String commandName;
    protected String description;
    protected String group;
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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
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

    /**
     * Get a pre-formatted string that shows the information of the command
     *
     * @return the information of the command
     */
    public String getCommandHelpFormat() {
        StringBuilder mainMessage = new StringBuilder("**Command:** `");
        StringBuilder arguments;

        mainMessage.append(discordCommandPrefix).append(commandName).append("`\n")
                .append("**Group:** `").append(group).append("`").append("\n")
                .append(description).append("\n\n")
                .append("**Format:** `").append(discordCommandPrefix).append(commandName);

        if (this.hasCommandArguments()) {
            arguments = new StringBuilder("**Arguments:**\n");

            this.commandArguments.forEach(commandArgument -> {
                mainMessage.append(" <").append(commandArgument.getKey()).append(">");
                arguments.append("`").append(commandArgument.getKey());

                if (commandArgument.isOptional()) {
                    arguments.append(" (optional)");
                }

                arguments.append("`")
                        .append(": ")
                        .append(commandArgument.getPrompt())
                        .append("\n");
            });

            mainMessage.append("`\n").append(arguments);
        }

        return mainMessage.toString();
    }

    /**
     * Get a pre-formatted string that shows the information of the command with an extra message on top
     *
     * @param extraMessage the message to send with the help message
     * @return the information and message of the command
     */
    public String getCommandHelpFormat(String extraMessage) {
        return extraMessage + getCommandHelpFormat();
    }

    /**
     * Check if the command is a valid command. commandName, description and group has to be filled in
     *
     * @return the validation of the command
     */
    public Boolean isValidCommand() {
        return this.commandName != null && !this.commandName.isEmpty() && this.description != null && !this.description.isEmpty() && this.group != null && !this.group.isEmpty();
    }

    /**
     * Sends a generic message that something went wrong with executing this command
     *
     * @return the generic message
     */
    public String getIncorrectCommandHelpFormat() {
        return "**There was an error while performing the command.**\n\n" + getCommandHelpFormat();
    }

    /**
     * The method that will be executed when no CommandParameters were found
     *
     * @param messageCreateEvent the MessageCreateEvent object of the current channel it was send in
     */
    public abstract void execute(MessageCreateEvent messageCreateEvent);

    /**
     * The method that will be executed when CommandParameters were found
     *
     * @param messageCreateEvent the MessageCreateEvent object of the current channel it was send in
     * @param commandParams      the parameters that were found
     */
    public abstract void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams);
}

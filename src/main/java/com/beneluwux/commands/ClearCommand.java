package com.beneluwux.commands;

import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandArgumentType;
import com.beneluwux.models.command.CommandParameter;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClearCommand extends Command {
    public ClearCommand() {
        this.commandName = "clear";
        this.commandArguments.add(new CommandArgument("lines", "The amount of lines you want to clear", CommandArgumentType.Integer));
        this.requiresAdmin = true;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {

    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter commandParameter = commandParams.get(0);

        if (!commandParameter.getParameterParsedCorrectly()) {
            messageCreateEvent.getChannel().sendMessage(getIncorrectCommandHelpFormat());
            return;
        }

        int messagesToDelete = (Integer) commandParameter.getParamaterValue() > 0 ? (Integer) commandParameter.getParamaterValue() : 5;

        messageCreateEvent.getServerTextChannel().ifPresent(serverTextChannel -> serverTextChannel.getMessages(messagesToDelete).whenCompleteAsync((messages, throwable) -> messages.deleteAll()));
    }
}

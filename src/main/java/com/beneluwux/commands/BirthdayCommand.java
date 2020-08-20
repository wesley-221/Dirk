package com.beneluwux.commands;

import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandArgumentType;
import com.beneluwux.models.command.CommandParameter;
import com.beneluwux.models.entities.Birthday;
import com.beneluwux.models.entities.embeddables.BirthdayId;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class BirthdayCommand extends Command {
    public BirthdayCommand() {
        this.commandName = "birthday";

        this.addCommandArgument(new CommandArgument("date", "Enter the date in dd/mm/yyyy format (05/01/1995).", CommandArgumentType.Date));
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
        messageCreateEvent.getChannel().sendMessage("no params");
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        BirthdayId birthdayId = new BirthdayId(messageCreateEvent.getServer().get().getId(), messageCreateEvent.getMessageAuthor().getId());

        Birthday birthday = new Birthday();
        birthday.setBirthdayId(birthdayId);
        birthday.setBirthday((Date) commandParams.get(0).getParamaterValue());

        // Handle saving

        messageCreateEvent.getChannel().sendMessage("Updated your birthday to " + commandParams.stream().findFirst());
    }
}

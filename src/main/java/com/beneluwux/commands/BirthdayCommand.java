package com.beneluwux.commands;

import com.beneluwux.helper.EmbedHelper;
import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandArgumentType;
import com.beneluwux.models.command.CommandParameter;
import com.beneluwux.models.entities.Birthday;
import com.beneluwux.models.entities.embeddables.BirthdayId;
import com.beneluwux.repositories.BirthdayRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class BirthdayCommand extends Command {
    BirthdayRepository birthdayRepository;

    @Autowired
    public BirthdayCommand(BirthdayRepository birthdayRepository) {
        this.commandName = "birthday";
        this.addCommandArgument(new CommandArgument("date", "Enter the date in dd/mm/yyyy format (01/05/1995).", CommandArgumentType.Date));

        this.birthdayRepository = birthdayRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        BirthdayId birthdayId = new BirthdayId(messageCreateEvent.getServer().get().getId(), messageCreateEvent.getMessageAuthor().getId());

        Birthday birthday = new Birthday();
        birthday.setBirthdayId(birthdayId);
        birthday.setBirthday((Date) commandParams.get(0).getValue());

        birthdayRepository.save(birthday);

        // Parse the date to a readable format
        String parsedDate = new SimpleDateFormat("dd MMMMM, yyyy").format(commandParams.get(0).getValue());

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("Updated your birthday to " + parsedDate + ".", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
    }
}

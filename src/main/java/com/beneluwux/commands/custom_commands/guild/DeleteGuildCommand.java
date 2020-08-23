package com.beneluwux.commands.custom_commands.guild;

import com.beneluwux.helper.EmbedHelper;
import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandArgument;
import com.beneluwux.models.command.CommandArgumentType;
import com.beneluwux.models.command.CommandParameter;
import com.beneluwux.models.entities.CustomCommand;
import com.beneluwux.repositories.CustomCommandRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteGuildCommand extends Command {
    private final CustomCommandRepository customCommandRepository;

    @Autowired
    public DeleteGuildCommand(CustomCommandRepository customCommandRepository) {
        this.commandName = "deleteguildcommand";
        this.requiresAdmin = true;

        this.commandArguments.add(new CommandArgument("command name", "The name of the command to delete", CommandArgumentType.SingleString));
        this.customCommandRepository = customCommandRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {

    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter commandKey = commandParams.get(0);
        CustomCommand customCommand = customCommandRepository.findByNameAndServerSnowflake((String) commandKey.getParamaterValue(), messageCreateEvent.getServer().get().getId());

        if (customCommand == null) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("The guild command `" + commandKey.getParamaterValue() + "` doesn't exist.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        } else {
            customCommandRepository.delete(customCommand);
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("The guild command `" + commandKey.getParamaterValue() + "` has been deleted.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        }
    }
}

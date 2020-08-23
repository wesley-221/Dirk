package com.beneluwux.commands.custom_commands.guild;

import com.beneluwux.helper.EmbedHelper;
import com.beneluwux.meta.CustomCommandComponent;
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
public class EditGuildCommand extends Command {
    private final CustomCommandRepository customCommandRepository;
    private final CustomCommandComponent customCommandComponent;

    @Autowired
    public EditGuildCommand(CustomCommandRepository customCommandRepository, CustomCommandComponent customCommandComponent) {
        this.commandName = "editguildcommand";
        this.requiresAdmin = true;

        this.commandArguments.add(new CommandArgument("command name", "The name of the command to edit", CommandArgumentType.SingleString));
        this.commandArguments.add(new CommandArgument("command output", "The output of the command", CommandArgumentType.String));

        this.customCommandRepository = customCommandRepository;
        this.customCommandComponent = customCommandComponent;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
        CommandParameter commandKey = commandParams.get(0);
        CommandParameter commandMessage = commandParams.get(1);

        CustomCommand customCommand = customCommandRepository.findByNameAndServerSnowflake((String) commandKey.getValue(), messageCreateEvent.getServer().get().getId());

        if (customCommand == null) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("The guild command `" + commandKey.getValue() + "` doesn't exist.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        } else {
            customCommand.setMessage((String) commandMessage.getValue());
            customCommandRepository.save(customCommand);
            customCommandComponent.refreshCustomCommandsFromJPA();

            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("Edited the guild command `" + commandKey.getValue() + "`: `" + commandMessage.getValue() + "`", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
        }
    }
}

package com.beneluwux.commands.custom_commands.global;

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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CreateGlobalCommand extends Command {
    private final ApplicationContext applicationContext;
    private final CustomCommandRepository customCommandRepository;
    private final CustomCommandComponent customCommandComponent;

    @Autowired
    public CreateGlobalCommand(ApplicationContext applicationContext, CustomCommandRepository customCommandRepository, CustomCommandComponent customCommandComponent) {
        this.commandName = "createglobalcommand";
        this.requiresBotOwner = true;

        this.commandArguments.add(new CommandArgument("command name", "The name of the command to create", CommandArgumentType.SingleString));
        this.commandArguments.add(new CommandArgument("command output", "The output of the command", CommandArgumentType.String));

        this.applicationContext = applicationContext;
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

        Map<String, Command> staticCommands = applicationContext.getBeansOfType(Command.class);
        boolean commandWasFound = false;

        // Loop through static commands
        for (Command command : staticCommands.values()) {
            if (command.getCommandName().equals(commandKey.getParamaterValue())) {
                commandWasFound = true;
                break;
            }
        }

        // Check if the custom command exists
        if (!commandWasFound) {
            commandWasFound = customCommandRepository.existsByNameAndServerSnowflake((String) commandKey.getParamaterValue(), 0L);
        }

        // There was a command found
        if (commandWasFound) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericErrorEmbed("There is already a global command with the name `" + commandKey.getParamaterValue() + "`.", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
            return;
        }

        CustomCommand customCommand = new CustomCommand(0L, messageCreateEvent.getMessageAuthor().getId(), (String) commandKey.getParamaterValue(), (String) commandMessage.getParamaterValue());
        customCommandRepository.save(customCommand);
        customCommandComponent.refreshCustomCommandsFromJPA();

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed("Created the global command `" + commandKey.getParamaterValue() + "`: `" + commandMessage.getParamaterValue() + "`", messageCreateEvent.getMessageAuthor().getDiscriminatedName()));
    }
}

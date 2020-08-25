package com.beneluwux.commands.greeting;

import com.beneluwux.helper.EmbedHelper;
import com.beneluwux.models.command.Command;
import com.beneluwux.models.command.CommandParameter;
import com.beneluwux.models.entities.ServerTraffic;
import com.beneluwux.repositories.ServerTrafficRepository;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ToggleLeaveCommand extends Command {
    private final ServerTrafficRepository serverTrafficRepository;

    @Autowired
    public ToggleLeaveCommand(ServerTrafficRepository serverTrafficRepository) {
        this.commandName = "toggleleave";
        this.requiresAdmin = true;
        this.guildOnly = true;

        this.serverTrafficRepository = serverTrafficRepository;
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent) {
        long serverSnowflake = messageCreateEvent.getServer().get().getId();
        long channelSnowflake = messageCreateEvent.getChannel().getId();

        ServerTraffic serverTraffic = serverTrafficRepository.findByServerSnowflakeAndChannelSnowflake(serverSnowflake, channelSnowflake);

        if (serverTraffic == null) {
            serverTraffic = new ServerTraffic(serverSnowflake, channelSnowflake, false, true);
        } else {
            serverTraffic.setShowLeaving(!serverTraffic.getShowLeaving());
        }

        serverTrafficRepository.save(serverTraffic);

        String parsedMessage = "You will " +
                (serverTraffic.getShowLeaving() ? "now receive" : "no longer") +
                " a message when someone leaves this guild." +
                (serverTraffic.getShowLeaving() ? "\n\nAn example message is shown below." : "");

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed(parsedMessage, messageCreateEvent.getMessageAuthor().getDiscriminatedName()));

        if (serverTraffic.getShowLeaving()) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.personLeaveServer(serverTraffic.getLeaveMessage(), Objects.requireNonNull(messageCreateEvent.getMessageAuthor().asUser().orElse(null))));
        }
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
    }
}

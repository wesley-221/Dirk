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
public class ToggleJoinCommand extends Command {
    private final ServerTrafficRepository serverTrafficRepository;

    @Autowired
    public ToggleJoinCommand(ServerTrafficRepository serverTrafficRepository) {
        this.commandName = "togglejoin";
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
            serverTraffic = new ServerTraffic(serverSnowflake, channelSnowflake, true, false);
        } else {
            serverTraffic.setShowJoining(!serverTraffic.getShowJoining());
        }

        serverTrafficRepository.save(serverTraffic);

        String parsedMessage = "You will " +
                (serverTraffic.getShowJoining() ? "now receive" : "no longer") +
                " a message when someone joins this guild." +
                (serverTraffic.getShowJoining() ? "\n\nAn example message is shown below." : "");

        messageCreateEvent.getChannel().sendMessage(EmbedHelper.genericSuccessEmbed(parsedMessage, messageCreateEvent.getMessageAuthor().getDiscriminatedName()));

        if (serverTraffic.getShowJoining()) {
            messageCreateEvent.getChannel().sendMessage(EmbedHelper.personJoinedServer(serverTraffic.getJoinMessage(), Objects.requireNonNull(messageCreateEvent.getMessageAuthor().asUser().orElse(null))));
        }
    }

    @Override
    public void execute(MessageCreateEvent messageCreateEvent, List<CommandParameter> commandParams) {
    }
}

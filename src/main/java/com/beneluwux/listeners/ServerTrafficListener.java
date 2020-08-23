package com.beneluwux.listeners;

import com.beneluwux.helper.EmbedHelper;
import com.beneluwux.helper.RegisterListener;
import com.beneluwux.models.entities.ServerTraffic;
import com.beneluwux.repositories.ServerTrafficRepository;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServerTrafficListener implements ServerMemberJoinListener, ServerMemberLeaveListener, RegisterListener {
    private final ServerTrafficRepository serverTrafficRepository;

    @Autowired
    public ServerTrafficListener(ServerTrafficRepository serverTrafficRepository) {
        this.serverTrafficRepository = serverTrafficRepository;
    }

    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        ServerTraffic serverTraffic = serverTrafficRepository.findByServerSnowflake(event.getServer().getId());

        if (serverTraffic != null) {
            if (serverTraffic.getShowJoining()) {
                event.getServer().getTextChannelById(serverTraffic.getChannelSnowflake()).ifPresent(serverTextChannel -> serverTextChannel.sendMessage(EmbedHelper.personJoinedServer(serverTraffic.getJoinMessage(), event.getUser())));
            }
        }
    }

    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        ServerTraffic serverTraffic = serverTrafficRepository.findByServerSnowflake(event.getServer().getId());

        if (serverTraffic != null) {
            if (serverTraffic.getShowLeaving()) {
                event.getServer().getTextChannelById(serverTraffic.getChannelSnowflake()).ifPresent(serverTextChannel -> serverTextChannel.sendMessage(EmbedHelper.personLeaveServer(serverTraffic.getJoinMessage(), event.getUser())));
            }
        }
    }
}

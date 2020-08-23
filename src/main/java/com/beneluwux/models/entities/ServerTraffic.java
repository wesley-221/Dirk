package com.beneluwux.models.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class ServerTraffic {
    private static final String JOIN_MESSAGE = "{{tag}} ({{userid}})";
    private static final String LEAVE_MESSAGE = "{{tag}} ({{userid}})";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long serverSnowflake;
    private Long channelSnowflake;
    private Boolean showJoining;
    private Boolean showLeaving;
    private String joinMessage;
    private String leaveMessage;

    public ServerTraffic() {
    }

    public ServerTraffic(Long serverSnowflake, Long channelSnowflake, Boolean showJoining, Boolean showLeaving) {
        this.serverSnowflake = serverSnowflake;
        this.channelSnowflake = channelSnowflake;
        this.showJoining = showJoining;
        this.showLeaving = showLeaving;
        this.joinMessage = JOIN_MESSAGE;
        this.leaveMessage = LEAVE_MESSAGE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getServerSnowflake() {
        return serverSnowflake;
    }

    public void setServerSnowflake(Long serverSnowflake) {
        this.serverSnowflake = serverSnowflake;
    }

    public Long getChannelSnowflake() {
        return channelSnowflake;
    }

    public void setChannelSnowflake(Long channelSnowflake) {
        this.channelSnowflake = channelSnowflake;
    }

    public Boolean getShowJoining() {
        return showJoining;
    }

    public void setShowJoining(Boolean showJoining) {
        this.showJoining = showJoining;
    }

    public Boolean getShowLeaving() {
        return showLeaving;
    }

    public void setShowLeaving(Boolean showLeaving) {
        this.showLeaving = showLeaving;
    }

    public String getJoinMessage() {
        return joinMessage;
    }

    public void setJoinMessage(String joinMessage) {
        this.joinMessage = joinMessage;
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setLeaveMessage(String leaveMessage) {
        this.leaveMessage = leaveMessage;
    }
}

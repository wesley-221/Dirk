package com.beneluwux.models.entities.embeddables;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BirthdayId implements Serializable {
    private String serverSnowflake;
    private String userSnowflake;

    public BirthdayId() {
    }

    public BirthdayId(String serverSnowflake, String userSnowflake) {
        this.serverSnowflake = serverSnowflake;
        this.userSnowflake = userSnowflake;
    }

    public BirthdayId(Long serverSnowflake, Long userSnowflake) {
        this.serverSnowflake = serverSnowflake.toString();
        this.userSnowflake = userSnowflake.toString();
    }

    public String getServerSnowflake() {
        return serverSnowflake;
    }

    public void setServerSnowflake(String serverSnowflake) {
        this.serverSnowflake = serverSnowflake;
    }

    public String getUserSnowflake() {
        return userSnowflake;
    }

    public void setUserSnowflake(String userSnowflake) {
        this.userSnowflake = userSnowflake;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BirthdayId that = (BirthdayId) o;

        return this.serverSnowflake.equals(that.serverSnowflake) && this.userSnowflake.equals(that.userSnowflake);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.serverSnowflake, this.userSnowflake);
    }

    @Override
    public String toString() {
        return "BirthdayId{" +
                "serverSnowflake='" + serverSnowflake + '\'' +
                ", userSnowflake='" + userSnowflake + '\'' +
                '}';
    }
}

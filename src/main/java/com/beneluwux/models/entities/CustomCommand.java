package com.beneluwux.models.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class CustomCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long serverSnowflake;
    private Long authorSnowflake;
    private String name;
    private String message;

    public CustomCommand() {
    }

    public CustomCommand(Long serverSnowflake, Long authorSnowflake, String name, String message) {
        this.serverSnowflake = serverSnowflake;
        this.authorSnowflake = authorSnowflake;
        this.name = name;
        this.message = message;
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

    public Long getAuthorSnowflake() {
        return authorSnowflake;
    }

    public void setAuthorSnowflake(Long authorSnowflake) {
        this.authorSnowflake = authorSnowflake;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

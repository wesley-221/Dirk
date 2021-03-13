package com.dirk.models.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ServerJoinRole {
    @Id
    private String serverSnowflake;
    private String roleSnowflake;
}

/*
 * MIT License
 *
 * Copyright (c) 2020 Wesley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dirk.models.tournament;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Tournament {
    @Id
    private String serverSnowflake;
    private String name;
    private String spreadsheet;
    private String scheduleTab;

    private Boolean isTeamTournament;

    private String adminRoleSnowflake;
    private String refereeRoleSnowflake;
    private String commentatorRoleSnowflake;
    private String streamerRoleSnowflake;
    private String teamCaptainRoleSnowflake;
    private String playerRoleSnowflake;
    private String rescheduleNotifierChannelSnowflake;

    private String teamsRow;

    private String refereeRow;
    private String streamerRow;
    private String commentatorRow;

    private String playerOneRow;
    private String playerTwoRow;

    private String dateRow;
    private String timeRow;
    private String matchIdRow;

    private String dateFormat;

    @OneToMany(mappedBy = "tournament", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Match> allMatches = new ArrayList<>();

    @OneToMany(mappedBy = "tournament", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Team> allTeams = new ArrayList<>();

    public Tournament() {
        this.isTeamTournament = false;
        this.dateFormat = "%d/%m";
    }

    public Tournament(String serverSnowflake, String name) {
        this();

        this.serverSnowflake = serverSnowflake;
        this.name = name;
    }

    public void setAllMatches(List<Match> allMatches) {
        for (Match match : allMatches) {
            match.setTournament(this);
        }

        this.allMatches = allMatches;
    }

    public void setAllTeams(List<Team> allTeams) {
        for (Team team : allTeams) {
            team.setTournament(this);
        }

        this.allTeams = allTeams;
    }
}

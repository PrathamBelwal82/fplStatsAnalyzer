package com.fpl.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PlayerEntity {

    @Id
    private Long id;

    private String name;
    private int team;
    private int totalPoints;

    public PlayerEntity() {}

    public PlayerEntity(Long id, String name, int team, int totalPoints) {
        this.id = id;
        this.name = name;
        this.team = team;
        this.totalPoints = totalPoints;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
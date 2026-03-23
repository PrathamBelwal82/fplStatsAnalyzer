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

    // getters + setters
}
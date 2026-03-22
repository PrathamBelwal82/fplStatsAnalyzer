package com.fpl.model;

import java.util.Map;

public class Player {
    private String name;
    private Map<Integer, Integer> gwPoints;

    public Player(String name, Map<Integer, Integer> gwPoints) {
        this.name = name;
        this.gwPoints = gwPoints;
    }

    public String getName() { return name; }
    public Map<Integer, Integer> getGwPoints() { return gwPoints; }
}
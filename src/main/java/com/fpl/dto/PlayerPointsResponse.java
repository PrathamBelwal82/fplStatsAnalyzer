package com.fpl.dto;

public record PlayerPointsResponse(
        long playerId,
        String playerName,
        Integer fromGameweek,
        Integer toGameweek,
        int points
) {}

package com.fpl.service;

import com.fpl.dto.PlayerPointsResponse;
import com.fpl.model.PlayerEntity;
import com.fpl.repository.PlayerGameweekScoreRepository;
import com.fpl.repository.PlayerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlayerGameweekPointsService {

    private final PlayerRepository playerRepository;
    private final PlayerGameweekScoreRepository gameweekScoreRepository;

    public PlayerGameweekPointsService(
            PlayerRepository playerRepository,
            PlayerGameweekScoreRepository gameweekScoreRepository) {
        this.playerRepository = playerRepository;
        this.gameweekScoreRepository = gameweekScoreRepository;
    }

    public PlayerPointsResponse getPoints(Long playerId, Integer fromGw, Integer toGw) {
        PlayerEntity player = playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));

        boolean hasFrom = fromGw != null;
        boolean hasTo = toGw != null;
        if (hasFrom != hasTo) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Both fromGw and toGw are required for a gameweek range");
        }

        int points;
        Integer rangeFrom = null;
        Integer rangeTo = null;
        if (hasFrom) {
            if (fromGw < 1 || toGw < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gameweek must be at least 1");
            }
            if (fromGw > toGw) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromGw must be less than or equal to toGw");
            }
            rangeFrom = fromGw;
            rangeTo = toGw;
            points = toInt(gameweekScoreRepository.sumPointsForPlayerBetweenGameweeks(playerId, fromGw, toGw));
        } else {
            points = toInt(gameweekScoreRepository.sumPointsForPlayer(playerId));
        }

        return new PlayerPointsResponse(player.getId(), player.getName(), rangeFrom, rangeTo, points);
    }

    public PlayerPointsResponse getPointsByName(String name, Integer fromGw, Integer toGw) {
        PlayerEntity player = playerRepository
                .findFirstByNameIgnoreCaseOrderByIdAsc(name.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found"));
        return getPoints(player.getId(), fromGw, toGw);
    }

    private static int toInt(Long value) {
        return value == null ? 0 : value.intValue();
    }
}

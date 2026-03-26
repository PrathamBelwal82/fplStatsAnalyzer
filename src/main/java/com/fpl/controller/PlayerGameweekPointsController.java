package com.fpl.controller;

import com.fpl.dto.PlayerPointsResponse;
import com.fpl.service.PlayerGameweekPointsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerGameweekPointsController {

    private final PlayerGameweekPointsService gameweekPointsService;

    public PlayerGameweekPointsController(PlayerGameweekPointsService gameweekPointsService) {
        this.gameweekPointsService = gameweekPointsService;
    }

    /**
     * Total points across all stored gameweeks when {@code fromGw} and {@code toGw} are omitted.
     * Inclusive gameweek range when both query params are present.
     */
    @GetMapping("/{playerId}/points")
    public PlayerPointsResponse getPointsById(
            @PathVariable Long playerId,
            @RequestParam(required = false) Integer fromGw,
            @RequestParam(required = false) Integer toGw) {
        return gameweekPointsService.getPoints(playerId, fromGw, toGw);
    }

    @GetMapping("/by-name/{name}/points")
    public PlayerPointsResponse getPointsByName(
            @PathVariable String name,
            @RequestParam(required = false) Integer fromGw,
            @RequestParam(required = false) Integer toGw) {
        return gameweekPointsService.getPointsByName(name, fromGw, toGw);
    }
}

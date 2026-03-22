package com.fpl.controller;

import com.fpl.model.Player;
import com.fpl.service.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlayerController {

    private final PlayerService service;

    public PlayerController(PlayerService service) {
        this.service = service;
    }

    @GetMapping("/players")
    public List<Player> getPlayers() {
        return service.getAllPlayers();
    }

    @GetMapping("/player/{name}")
    public Player getPlayer(@PathVariable String name) {
        return service.getPlayerByName(name);
    }

    @GetMapping("/player/{name}/points")
    public String getPoints(
            @PathVariable String name,
            @RequestParam int start,
            @RequestParam int end) {

        Player p = service.getPlayerByName(name);

        if (p == null) {
            return "Player not found";
        }

        int total = service.getTotalPoints(p, start, end);

        return "Total points from GW " + start + " to " + end + " = " + total;
    }
}
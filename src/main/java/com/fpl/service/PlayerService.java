package com.fpl.service;

import com.fpl.data.DataStore;
import com.fpl.model.Player;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    private final DataStore dataStore;

    public PlayerService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Player> getAllPlayers() {
        return dataStore.getPlayers();
    }

    public Player getPlayerByName(String name) {
        return dataStore.getPlayers()
                .stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public int getTotalPoints(Player player, int start, int end) {
        int total = 0;

        for (int gw = start; gw <= end; gw++) {
            total += player.getGwPoints().getOrDefault(gw, 0);
        }

        return total;
    }
}
package com.fpl.data;

import com.fpl.model.Player;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DataStore {

    private final List<Player> players;

    public DataStore() {
        Map<Integer, Integer> haaland = Map.of(1,8, 2,6, 3,12, 4,5);
        Map<Integer, Integer> saka = Map.of(1,7, 2,10, 3,3, 4,9);

        players = List.of(
            new Player("Haaland", haaland),
            new Player("Saka", saka)
        );
    }

    public List<Player> getPlayers() {
        return players;
    }
}
package com.fpl.service;

import com.fpl.model.PlayerEntity;
import com.fpl.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class FplService {

    private final PlayerRepository repo;

    public FplService(PlayerRepository repo) {
        this.repo = repo;
    }

    public void fetchAndStorePlayers() {

        String url = "https://fantasy.premierleague.com/api/bootstrap-static/";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> players =
                (List<Map<String, Object>>) response.get("elements");

        List<PlayerEntity> entities = new ArrayList<>();

        for (Map<String, Object> p : players) {

            Long id = ((Number) p.get("id")).longValue();
            String name = p.get("web_name").toString();
            int team = ((Number) p.get("team")).intValue();
            int totalPoints = ((Number) p.get("total_points")).intValue();

            PlayerEntity player = new PlayerEntity(id, name, team, totalPoints);

            entities.add(player);
        }

        repo.saveAll(entities);
    }
}
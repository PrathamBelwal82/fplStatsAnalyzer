package com.fpl.service;

import com.fpl.model.PlayerEntity;
import com.fpl.model.PlayerGameweekScoreEntity;
import com.fpl.repository.PlayerGameweekScoreRepository;
import com.fpl.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FplSyncService {

    private static final Logger log = LoggerFactory.getLogger(FplSyncService.class);

    private final RestTemplate restTemplate;
    private final PlayerRepository playerRepository;
    private final PlayerGameweekScoreRepository gameweekScoreRepository;
    private final FplSyncService self;

    private final String apiBaseUrl;

    public FplSyncService(
            RestTemplate restTemplate,
            PlayerRepository playerRepository,
            PlayerGameweekScoreRepository gameweekScoreRepository,
            @Lazy FplSyncService self,
            @Value("${fpl.api.base-url:https://fantasy.premierleague.com/api}") String apiBaseUrl) {
        this.restTemplate = restTemplate;
        this.playerRepository = playerRepository;
        this.gameweekScoreRepository = gameweekScoreRepository;
        this.self = self;
        this.apiBaseUrl = apiBaseUrl.endsWith("/") ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1) : apiBaseUrl;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchBootstrapStatic() {
        return restTemplate.getForObject(apiBaseUrl + "/bootstrap-static/", Map.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchEventLive(int eventId) {
        return restTemplate.getForObject(apiBaseUrl + "/event/" + eventId + "/live/", Map.class);
    }

    public int syncPlayersFromBootstrap() {
        Map<String, Object> response = fetchBootstrapStatic();
        if (response == null) {
            log.warn("bootstrap-static returned null");
            return 0;
        }
        return self.syncPlayersFromBootstrapUsingBootstrap(response);
    }

    /**
     * Highest event id that is finished (and data checked when present), or empty if none.
     */
    @SuppressWarnings("unchecked")
    public Optional<Integer> latestFinishedGameweekId(Map<String, Object> bootstrap) {
        List<Map<String, Object>> events = (List<Map<String, Object>>) bootstrap.get("events");
        if (events == null) {
            return Optional.empty();
        }
        int best = -1;
        for (Map<String, Object> ev : events) {
            Boolean finished = (Boolean) ev.get("finished");
            if (finished == null || !finished) {
                continue;
            }
            Boolean dataChecked = (Boolean) ev.get("data_checked");
            if (dataChecked != null && !dataChecked) {
                continue;
            }
            int id = ((Number) ev.get("id")).intValue();
            if (id > best) {
                best = id;
            }
        }
        return best < 0 ? Optional.empty() : Optional.of(best);
    }

    /**
     * All finished (and data-checked) gameweek ids in ascending order.
     */
    @SuppressWarnings("unchecked")
    public List<Integer> allFinishedGameweekIds(Map<String, Object> bootstrap) {
        List<Map<String, Object>> events = (List<Map<String, Object>>) bootstrap.get("events");
        if (events == null) {
            return List.of();
        }
        List<Integer> ids = new ArrayList<>();
        for (Map<String, Object> ev : events) {
            Boolean finished = (Boolean) ev.get("finished");
            if (finished == null || !finished) {
                continue;
            }
            Boolean dataChecked = (Boolean) ev.get("data_checked");
            if (dataChecked != null && !dataChecked) {
                continue;
            }
            ids.add(((Number) ev.get("id")).intValue());
        }
        ids.sort(Integer::compareTo);
        return ids;
    }

    @Transactional
    public int upsertGameweekScoresFromLive(int gameweek, Map<String, Object> liveResponse) {
        if (liveResponse == null) {
            return 0;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> elements = (List<Map<String, Object>>) liveResponse.get("elements");
        if (elements == null) {
            return 0;
        }
        int updated = 0;
        for (Map<String, Object> el : elements) {
            Long playerId = ((Number) el.get("id")).longValue();
            if (!playerRepository.existsById(playerId)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) el.get("stats");
            int points = 0;
            if (stats != null && stats.get("total_points") instanceof Number n) {
                points = n.intValue();
            }
            PlayerEntity playerRef = playerRepository.getReferenceById(playerId);
            PlayerGameweekScoreEntity row =
                    gameweekScoreRepository.findByPlayer_IdAndGameweek(playerId, gameweek).orElse(null);
            if (row == null) {
                row = new PlayerGameweekScoreEntity(playerRef, gameweek, points);
            } else {
                row.setPoints(points);
            }
            gameweekScoreRepository.save(row);
            updated++;
        }
        return updated;
    }

    public int syncGameweekScores(int gameweek) {
        Map<String, Object> live = fetchEventLive(gameweek);
        return self.upsertGameweekScoresFromLive(gameweek, live);
    }

    /**
     * Refresh all players, then load scores for the latest finished gameweek only (typical weekly job).
     * HTTP calls run outside transactions; each DB write runs in its own short transaction.
     */
    public WeeklySyncResult runWeeklyPipeline() {
        Map<String, Object> bootstrap = fetchBootstrapStatic();
        if (bootstrap == null) {
            return new WeeklySyncResult(0, null, 0);
        }
        int players = self.syncPlayersFromBootstrapUsingBootstrap(bootstrap);
        Optional<Integer> gw = latestFinishedGameweekId(bootstrap);
        if (gw.isEmpty()) {
            return new WeeklySyncResult(players, null, 0);
        }
        int g = gw.get();
        Map<String, Object> live = fetchEventLive(g);
        int scores = self.upsertGameweekScoresFromLive(g, live);
        return new WeeklySyncResult(players, g, scores);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public int syncPlayersFromBootstrapUsingBootstrap(Map<String, Object> response) {
        List<Map<String, Object>> players = (List<Map<String, Object>>) response.get("elements");
        if (players == null) {
            return 0;
        }
        List<PlayerEntity> entities = new ArrayList<>(players.size());
        for (Map<String, Object> p : players) {
            Long id = ((Number) p.get("id")).longValue();
            String name = p.get("web_name").toString();
            int team = ((Number) p.get("team")).intValue();
            int totalPoints = ((Number) p.get("total_points")).intValue();
            entities.add(new PlayerEntity(id, name, team, totalPoints));
        }
        playerRepository.saveAll(entities);
        return entities.size();
    }

    /**
     * One bootstrap fetch, then event/live for every finished gameweek (initial backfill; many HTTP calls).
     */
    public BackfillResult backfillAllFinishedGameweekScores() {
        Map<String, Object> bootstrap = fetchBootstrapStatic();
        if (bootstrap == null) {
            return new BackfillResult(0, List.of());
        }
        int players = self.syncPlayersFromBootstrapUsingBootstrap(bootstrap);
        List<Integer> gws = allFinishedGameweekIds(bootstrap);
        List<Integer> synced = new ArrayList<>();
        for (Integer gw : gws) {
            try {
                int n = self.syncGameweekScores(gw);
                synced.add(gw);
                log.info("Backfilled gameweek {} ({} player rows)", gw, n);
            } catch (Exception e) {
                log.error("Failed to sync gameweek {}: {}", gw, e.getMessage());
            }
        }
        return new BackfillResult(players, synced);
    }

    public record WeeklySyncResult(int playersUpserted, Integer latestFinishedGameweek, int scoreRowsWritten) {}

    public record BackfillResult(int playersUpserted, List<Integer> gameweeksSynced) {}
}

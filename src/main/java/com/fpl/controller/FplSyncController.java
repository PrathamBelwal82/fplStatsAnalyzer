package com.fpl.controller;

import com.fpl.service.FplSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class FplSyncController {

    private final FplSyncService fplSyncService;

    public FplSyncController(FplSyncService fplSyncService) {
        this.fplSyncService = fplSyncService;
    }

    /** Refresh players from bootstrap-static only (same as legacy {@code GET /fetch}). */
    @PostMapping("/players")
    public ResponseEntity<Map<String, Object>> syncPlayers() {
        int n = fplSyncService.syncPlayersFromBootstrap();
        return ResponseEntity.ok(Map.of("playersUpserted", n));
    }

    /** Players + scores for the latest finished gameweek (what the weekly job runs). */
    @PostMapping("/weekly")
    public ResponseEntity<FplSyncService.WeeklySyncResult> syncWeekly() {
        return ResponseEntity.ok(fplSyncService.runWeeklyPipeline());
    }

    /** Load event/live for one gameweek and upsert {@code player_gameweek_score} rows. */
    @PostMapping("/gameweeks/{gameweek}")
    public ResponseEntity<Map<String, Object>> syncOneGameweek(@PathVariable int gameweek) {
        int rows = fplSyncService.syncGameweekScores(gameweek);
        return ResponseEntity.ok(Map.of("gameweek", gameweek, "scoreRowsWritten", rows));
    }

    /**
     * Bootstrap + event/live for every finished gameweek (many requests; use for first-time backfill).
     */
    @PostMapping("/gameweeks/backfill")
    public ResponseEntity<FplSyncService.BackfillResult> backfillGameweeks() {
        return ResponseEntity.ok(fplSyncService.backfillAllFinishedGameweekScores());
    }
}

package com.fpl.controller;

import com.fpl.kafka.FplSyncKafkaPublisher;
import com.fpl.service.FplSyncService;
import org.springframework.beans.factory.ObjectProvider;
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
    private final ObjectProvider<FplSyncKafkaPublisher> kafkaPublisher;

    public FplSyncController(
            FplSyncService fplSyncService, ObjectProvider<FplSyncKafkaPublisher> kafkaPublisher) {
        this.fplSyncService = fplSyncService;
        this.kafkaPublisher = kafkaPublisher;
    }

    /** Refresh players from bootstrap-static only (same as legacy {@code GET /fetch}). */
    @PostMapping("/players")
    public ResponseEntity<?> syncPlayers() {
        FplSyncKafkaPublisher pub = kafkaPublisher.getIfAvailable();
        if (pub != null) {
            String correlationId = pub.publishPlayersOnly();
            return ResponseEntity.accepted().body(enqueued(correlationId));
        }
        int n = fplSyncService.syncPlayersFromBootstrap();
        return ResponseEntity.ok(Map.of("playersUpserted", n));
    }

    /** Players + scores for the latest finished gameweek (what the weekly job runs). */
    @PostMapping("/weekly")
    public ResponseEntity<?> syncWeekly() {
        FplSyncKafkaPublisher pub = kafkaPublisher.getIfAvailable();
        if (pub != null) {
            String correlationId = pub.publishWeekly();
            return ResponseEntity.accepted().body(enqueued(correlationId));
        }
        return ResponseEntity.ok(fplSyncService.runWeeklyPipeline());
    }

    /** Load event/live for one gameweek and upsert {@code player_gameweek_score} rows. */
    @PostMapping("/gameweeks/{gameweek}")
    public ResponseEntity<?> syncOneGameweek(@PathVariable int gameweek) {
        FplSyncKafkaPublisher pub = kafkaPublisher.getIfAvailable();
        if (pub != null) {
            String correlationId = pub.publishSingleGameweek(gameweek);
            return ResponseEntity.accepted().body(enqueued(correlationId, Map.of("gameweek", gameweek)));
        }
        int rows = fplSyncService.syncGameweekScores(gameweek);
        return ResponseEntity.ok(Map.of("gameweek", gameweek, "scoreRowsWritten", rows));
    }

    /**
     * Bootstrap + event/live for every finished gameweek (many requests; use for first-time backfill).
     */
    @PostMapping("/gameweeks/backfill")
    public ResponseEntity<?> backfillGameweeks() {
        FplSyncKafkaPublisher pub = kafkaPublisher.getIfAvailable();
        if (pub != null) {
            String correlationId = pub.publishBackfill();
            return ResponseEntity.accepted().body(enqueued(correlationId));
        }
        return ResponseEntity.ok(fplSyncService.backfillAllFinishedGameweekScores());
    }

    private static Map<String, Object> enqueued(String correlationId) {
        return Map.of("status", "enqueued", "correlationId", correlationId);
    }

    private static Map<String, Object> enqueued(String correlationId, Map<String, Object> extra) {
        return Map.of(
                "status",
                "enqueued",
                "correlationId",
                correlationId,
                "details",
                extra);
    }
}

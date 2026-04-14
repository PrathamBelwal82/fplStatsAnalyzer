package com.fpl.kafka;

import com.fpl.service.FplSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "fpl.kafka.enabled", havingValue = "true")
public class FplSyncCommandListener {

    private static final Logger log = LoggerFactory.getLogger(FplSyncCommandListener.class);

    private final FplSyncService fplSyncService;

    public FplSyncCommandListener(FplSyncService fplSyncService) {
        this.fplSyncService = fplSyncService;
    }

    @KafkaListener(
            topics = "${fpl.kafka.topics.sync-commands}",
            containerFactory = "fplSyncKafkaListenerContainerFactory")
    public void onSyncCommand(SyncCommand command) {
        String cid = command.correlationId() != null ? command.correlationId() : "n/a";
        log.info("Received sync command type={} correlationId={}", command.type(), cid);
        try {
            switch (command.type()) {
                case WEEKLY -> {
                    FplSyncService.WeeklySyncResult r = fplSyncService.runWeeklyPipeline();
                    log.info(
                            "Weekly sync done correlationId={} players={} gw={} scoreRows={}",
                            cid,
                            r.playersUpserted(),
                            r.latestFinishedGameweek(),
                            r.scoreRowsWritten());
                }
                case PLAYERS_ONLY -> {
                    int n = fplSyncService.syncPlayersFromBootstrap();
                    log.info("Players sync done correlationId={} count={}", cid, n);
                }
                case BACKFILL -> {
                    FplSyncService.BackfillResult b = fplSyncService.backfillAllFinishedGameweekScores();
                    log.info(
                            "Backfill done correlationId={} players={} gameweeks={}",
                            cid,
                            b.playersUpserted(),
                            b.gameweeksSynced().size());
                }
                case SINGLE_GAMEWEEK -> {
                    if (command.gameweek() == null) {
                        log.warn("Ignoring SINGLE_GAMEWEEK without gameweek correlationId={}", cid);
                        return;
                    }
                    int rows = fplSyncService.syncGameweekScores(command.gameweek());
                    log.info(
                            "Gameweek sync done correlationId={} gameweek={} scoreRows={}",
                            cid,
                            command.gameweek(),
                            rows);
                }
            }
        } catch (Exception e) {
            log.error("Sync command failed type={} correlationId={}", command.type(), cid, e);
            throw e;
        }
    }
}

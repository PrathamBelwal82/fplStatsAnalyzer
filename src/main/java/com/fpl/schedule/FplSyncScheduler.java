package com.fpl.schedule;

import com.fpl.kafka.FplSyncKafkaPublisher;
import com.fpl.service.FplSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "fpl.sync.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class FplSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(FplSyncScheduler.class);

    private final FplSyncService fplSyncService;
    private final ObjectProvider<FplSyncKafkaPublisher> kafkaPublisher;

    public FplSyncScheduler(
            FplSyncService fplSyncService, ObjectProvider<FplSyncKafkaPublisher> kafkaPublisher) {
        this.fplSyncService = fplSyncService;
        this.kafkaPublisher = kafkaPublisher;
    }

    @Scheduled(cron = "${fpl.sync.cron:0 0 4 * * TUE}", zone = "${fpl.sync.zone:UTC}")
    public void runWeeklySync() {
        try {
            FplSyncKafkaPublisher publisher = kafkaPublisher.getIfAvailable();
            if (publisher != null) {
                String correlationId = publisher.publishWeekly();
                log.info("Scheduled weekly sync enqueued to Kafka correlationId={}", correlationId);
                return;
            }
            FplSyncService.WeeklySyncResult r = fplSyncService.runWeeklyPipeline();
            log.info(
                    "Scheduled FPL sync: players={}, latestFinishedGw={}, scoreRows={}",
                    r.playersUpserted(),
                    r.latestFinishedGameweek() != null ? r.latestFinishedGameweek() : "none",
                    r.scoreRowsWritten());
        } catch (Exception e) {
            log.error("Scheduled FPL sync failed", e);
        }
    }
}

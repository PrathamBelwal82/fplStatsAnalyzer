package com.fpl.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnProperty(name = "fpl.kafka.enabled", havingValue = "true")
public class FplSyncKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(FplSyncKafkaPublisher.class);

    private final KafkaTemplate<String, SyncCommand> kafkaTemplate;
    private final String topic;

    public FplSyncKafkaPublisher(
            KafkaTemplate<String, SyncCommand> kafkaTemplate,
            @Value("${fpl.kafka.topics.sync-commands}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public String publishWeekly() {
        return send(SyncCommand.weekly(newCorrelationId()));
    }

    public String publishPlayersOnly() {
        return send(SyncCommand.playersOnly(newCorrelationId()));
    }

    public String publishBackfill() {
        return send(SyncCommand.backfill(newCorrelationId()));
    }

    public String publishSingleGameweek(int gameweek) {
        return send(SyncCommand.singleGameweek(gameweek, newCorrelationId()));
    }

    private static String newCorrelationId() {
        return UUID.randomUUID().toString();
    }

    private String send(SyncCommand command) {
        String key = command.correlationId();
        CompletableFuture<SendResult<String, SyncCommand>> future = kafkaTemplate.send(topic, key, command);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish sync command {} to topic {}", command.type(), topic, ex);
            } else {
                log.debug(
                        "Published sync command {} correlationId={} offset={}",
                        command.type(),
                        command.correlationId(),
                        result.getRecordMetadata().offset());
            }
        });
        return command.correlationId();
    }
}

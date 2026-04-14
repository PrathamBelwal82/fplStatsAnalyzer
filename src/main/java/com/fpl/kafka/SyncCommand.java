package com.fpl.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Message consumed from {@code fpl.kafka.topics.sync-commands} to trigger FPL DB sync work.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SyncCommand(SyncCommandType type, Integer gameweek, String correlationId) {

    public static SyncCommand weekly(String correlationId) {
        return new SyncCommand(SyncCommandType.WEEKLY, null, correlationId);
    }

    public static SyncCommand playersOnly(String correlationId) {
        return new SyncCommand(SyncCommandType.PLAYERS_ONLY, null, correlationId);
    }

    public static SyncCommand backfill(String correlationId) {
        return new SyncCommand(SyncCommandType.BACKFILL, null, correlationId);
    }

    public static SyncCommand singleGameweek(int gameweek, String correlationId) {
        return new SyncCommand(SyncCommandType.SINGLE_GAMEWEEK, gameweek, correlationId);
    }
}

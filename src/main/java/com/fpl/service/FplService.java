package com.fpl.service;

import org.springframework.stereotype.Service;

@Service
public class FplService {

    private final FplSyncService fplSyncService;

    public FplService(FplSyncService fplSyncService) {
        this.fplSyncService = fplSyncService;
    }

    /** @see FplSyncService#syncPlayersFromBootstrap() */
    public void fetchAndStorePlayers() {
        fplSyncService.syncPlayersFromBootstrap();
    }
}
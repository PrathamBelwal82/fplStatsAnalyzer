package com.fpl.controller;

import com.fpl.service.FplService;
import org.springframework.web.bind.annotation.*;

@RestController
public class FplController {

    private final FplService service;

    public FplController(FplService service) {
        this.service = service;
    }

    @GetMapping("/fetch")
    public String fetchData() {
        service.fetchAndStorePlayers();
        return "Data fetched and stored!";
    }
}
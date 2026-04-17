package com.notam.controller;

import com.notam.NotamService;
import com.notam.model.NotamSearchResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notams")
@CrossOrigin(origins = "http://localhost:5173")
public class NotamController {

    private final NotamService notamService;

    public NotamController(NotamService notamService) {
        this.notamService = notamService;
    }

    @GetMapping
    public NotamSearchResponse getNotams(
            @RequestParam String departure,
            @RequestParam String destination) {
        return notamService.getNotamsForRoute(departure, destination);
    }
}
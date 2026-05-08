package com.notam.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.notam.NotamService;
import com.notam.model.NOTAM;
import com.notam.model.NotamSearchResponse;

/**
 * REST controller that exposes an HTTP endpoint for fetching NOTAMs.
 * Delegates to NotamService which calls the FAA API and deduplicates results.
 */
@RestController
@RequestMapping("/notams")
public class NotamController {

    private final NotamService notamService;

    // NotamService is injected by Spring via constructor injection
    public NotamController(NotamService notamService) {
        this.notamService = notamService;
    }

    @GetMapping
public List<NOTAM> getNotams(@RequestParam("icaoLocation") String icaoLocation) throws Exception {
    return notamService.getNotams(icaoLocation);
}

@GetMapping("/stored")
public List<NOTAM> getStoredNotams(@RequestParam("icaoLocation") String icaoLocation) {
    return notamService.getStoredNotams(icaoLocation);
}

@GetMapping("/stored/route")
public NotamSearchResponse getStoredNotamsForRoute(
        @RequestParam("departure") String departure,
        @RequestParam("destination") String destination,
        @RequestParam(value = "corridorNM", defaultValue = "25") double corridorNM) {
    return notamService.getStoredNotamsForRoute(departure, destination, corridorNM);
}
}
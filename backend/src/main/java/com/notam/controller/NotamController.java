package com.notam.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.notam.NotamService;
import com.notam.model.NOTAM;

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

    /**
     * GET /api/notams?icaoLocation=KOKC
     * Returns a deduplicated list of NOTAMs for the given ICAO location code.
     *
     * @param icaoLocation the ICAO airport code to query (e.g. "KOKC")
     * @return list of NOTAM objects as JSON
     */
    @GetMapping
    public List<NOTAM> getNotams(@RequestParam String icaoLocation) throws Exception {
        return notamService.getNotams(icaoLocation);
    }
}
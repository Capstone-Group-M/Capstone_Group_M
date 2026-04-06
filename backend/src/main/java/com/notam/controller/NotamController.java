package com.notam.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import com.notam.client.FAAOldClient;
import com.notam.model.NOTAM;

/**
 * REST controller that exposes an HTTP endpoint for fetching NOTAMs.
 * Calls the FAA API via FAAOldClient and returns results as JSON.
 */
@RestController
@RequestMapping("/api/notams")
public class NotamController {

    private static final String CLIENT_ID = "12345";
    private static final String CLIENT_SECRET;

    static {
        CLIENT_SECRET = System.getenv("FAA_CLIENT_SECRET");
        if (CLIENT_SECRET == null || CLIENT_SECRET.isBlank()) {
            throw new IllegalStateException("FAA_CLIENT_SECRET environment variable is not set.");
        }
    }

    /**
     * GET /api/notams?icaoLocation=KOKC
     * Returns a list of NOTAMs for the given ICAO location code.
     *
     * @param icaoLocation the ICAO airport code to query (e.g. "KOKC")
     * @return list of NOTAM objects as JSON
     */
    @GetMapping
    public List<NOTAM> getNotams(@RequestParam String icaoLocation) throws Exception {
        FAAOldClient faaClient = new FAAOldClient(CLIENT_ID, CLIENT_SECRET);
        return faaClient.fetchAllNotams(icaoLocation);
    }
}
package com.notam.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;
import com.notam.parser.NotamParser;

/**
 * Client for the FAA NOTAM API (https://external-api.faa.gov/notamapi/v1/notams).
 * Fetches Notices to Air Missions (NOTAMs) by ICAO location code,
 * handling pagination, rate limiting, and retry logic.
 */
public class FAAOldClient {
    private final HttpClient client;
    private final String clientId;
    private final String clientSecret;
    private final ObjectMapper mapper = new ObjectMapper();
    private final NotamParser parser = new NotamParser(mapper);

    public FAAOldClient(String clientId, String clientSecret) {
        this.client = HttpClient.newHttpClient();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
    //used for testing
    private FAAOldClient(String clientId, String clientSecret, HttpClient client) {
        this.client = client;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
}   
    public static boolean isValidIcao(String code){
        return code != null && code.matches("[A-Z]{4}");
    }

    public List<NOTAM> fetchAllNotams(String icaoLocation) throws Exception {
        List<NOTAM> allNotams = new ArrayList<>();
        int pageNum = 1;
        final int MAX_PAGES = 50;
        if (icaoLocation == null || icaoLocation.isBlank() || !isValidIcao(icaoLocation)) {
            throw new IllegalArgumentException("ICAO location cannot be null or empty or more or less than 4 characters");
        }

        while (pageNum <= MAX_PAGES) {
            String url = "https://external-api.faa.gov/notamapi/v1/notams"
                       + "?icaoLocation=" + URLEncoder.encode(icaoLocation, StandardCharsets.UTF_8)
                       + "&pageNum=" + pageNum;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("client_id", clientId)
                    .header("client_secret", clientSecret)
                    .GET()
                    .build();

            int retries = 3;
            HttpResponse<String> response = null;

            for (int attempt = 1; attempt <= retries; attempt++) {
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    break;
                } catch (IOException e) {
                    if (attempt == retries) {
                        throw new RuntimeException("FAA API unreachable after " + retries + " attempts", e);
                    }
                    System.out.println("Exception caught while attempting HTTP Responce on attempt: " + attempt + ", system sleeping for " + attempt + " seconds...");
                    Thread.sleep(1000 * attempt); // backoff
                }
            }
            if (response.statusCode() == 429) {
                System.out.println("Rate limit due to large number of NOTAMs. Sleeping for 2 seconds...");
                Thread.sleep(2000);
                continue; // retry the same page
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("FAA API error code: " + response.statusCode() + ", Error : " + response.body());
            }

            List<NOTAM> page = parser.parsePage(response);
            if (page.isEmpty()) break;

            allNotams.addAll(page);
            pageNum++;
        }
        return allNotams;
    }
}
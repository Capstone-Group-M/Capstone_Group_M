package com.notam.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;
import com.notam.parser.NotamParser;

public class FAAClient {
    private final HttpClient client;
    private final String clientId;
    private final String clientSecret;
    private final ObjectMapper mapper;
    private final NotamParser parser;

    public FAAClient(String clientId, String clientSecret) {
        this.client = HttpClient.newHttpClient();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.mapper = new ObjectMapper();
        this.parser = new NotamParser(mapper);
    }
    //used for testing
    public FAAClient(String clientId, String clientSecret, HttpClient client) {
        this.client = client;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.mapper = new ObjectMapper();
        this.parser = new NotamParser(mapper);
}

    public List<NOTAM> fetchAllNotams(String icaoLocation) throws Exception {
        List<NOTAM> allNotams = new ArrayList<>();
        int pageNum = 1;
        final int MAX_PAGES = 50;
        if (icaoLocation == null || icaoLocation.isBlank()) {
            throw new IllegalArgumentException("ICAO location cannot be null or empty");
        }

        while (pageNum <= MAX_PAGES) {
            String url = "https://external-api.faa.gov/notamapi/v1/notams"
                       + "?icaoLocation=" + icaoLocation
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
                    Thread.sleep(1000 * attempt); // backoff
                }
            }
            if (response.statusCode() == 429) {
                System.out.println("Rate limit due to large number of NOTAMs. Sleeping for 2 seconds...");
                Thread.sleep(2000);
                continue; // retry the same page
            }
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("FAA API error: " + response.statusCode());
            }

            List<NOTAM> page = parser.parsePage(response);
            if (page.isEmpty()) break;

            allNotams.addAll(page);
            pageNum++;
        }
        return allNotams;
    }
}
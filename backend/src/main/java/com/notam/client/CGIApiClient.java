package com.notam.client;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;
import com.notam.parser.NotamParser;

public class CGIApiClient {
    private static final String AUTH_URL = "https://api-staging.cgifederal-aim.com/v1/auth/token";
    private static final String NOTAM_URL = "https://api-staging.cgifederal-aim.com/nmsapi/v1";

    private final HttpClient client;
    private final String clientId;
    private final String clientSecret;
    private final ObjectMapper mapper = new ObjectMapper();
    private final NotamParser parser = new NotamParser(mapper);
    private String token = null;

    public CGIApiClient(String clientId, String clientSecret) {
        this.client = HttpClient.newHttpClient();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    // Used for testing
    CGIApiClient(String clientId, String clientSecret, HttpClient client) {
        this.client = client;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public static boolean isValidIcao(String code) {
        code = code.trim().toUpperCase();
        return code != null && code.matches("[A-Z]{4}");
    }

    private void authenticate() throws Exception {
        String auth = Base64.getEncoder().encodeToString(
            (clientId + ":" + clientSecret).getBytes()
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(AUTH_URL))
                .header("Authorization", "Basic " + auth)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Authentication failed: " + response.statusCode() + " " + response.body());
        }

        token = mapper.readTree(response.body()).get("access_token").asText();
    }

    public List<NOTAM> fetchAllNotams(String icaoLocation) throws Exception {
        if (icaoLocation == null || icaoLocation.isBlank() || !isValidIcao(icaoLocation)) {
            throw new IllegalArgumentException("ICAO location cannot be null or empty or more or less than 4 characters");
        }

        if (token == null) {
            authenticate();
        }

        List<NOTAM> allNotams = new ArrayList<>();
        int pageNum = 1;
        final int MAX_PAGES = 50;
        final int MAX_RETRIES = 3;

        while (pageNum <= MAX_PAGES) {
            String url = NOTAM_URL + "/notams?location=" + URLEncoder.encode(icaoLocation, StandardCharsets.UTF_8)
                       + "&pageNum=" + pageNum;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("nmsResponseFormat", "GEOJSON")
                    .GET()
                    .build();

            HttpResponse<String> response = null;

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    break;
                } catch (IOException e) {
                    if (attempt == MAX_RETRIES) {
                        throw new RuntimeException("CGI API unreachable after " + MAX_RETRIES + " attempts", e);
                    }
                    System.out.println("Exception caught while attempting HTTP Response on attempt: " + attempt + ", system sleeping for " + attempt + " seconds...");
                    Thread.sleep(1000 * attempt);
                }
            }

            if (response.statusCode() == 429) {
                System.out.println("Rate limit due to large number of NOTAMs. Sleeping for 2 seconds...");
                Thread.sleep(2000);
                continue;
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("CGI API error code: " + response.statusCode() + ", Error: " + response.body());
            }

            List<NOTAM> page = parser.parseCGIPage(response);
            if (page.isEmpty()) break;

            allNotams.addAll(page);
            pageNum++;
        }

        return allNotams;
    }
}
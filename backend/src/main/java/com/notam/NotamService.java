package com.notam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;
import com.notam.model.NOTAMUtils;
import com.notam.model.NotamDto;
import com.notam.model.NotamSearchResponse;

@Service
public class NotamService {

    private static final String CLIENT_ID = "5982191bfef7458aa9cb8e8c9674b645";
    private static final String CLIENT_SECRET = System.getenv("FAA_CLIENT_SECRET");

    public List<NOTAM> getNotams(String icaoLocation) throws Exception {
        String url = "https://external-api.faa.gov/notamapi/v1/notams?icaoLocation=" + icaoLocation;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("client_id", CLIENT_ID)
                .header("client_secret", CLIENT_SECRET)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.body());
        JsonNode items = root.get("items");

        List<NOTAM> notamList = new ArrayList<>();

        for (JsonNode notamNode : items) {
            JsonNode data = notamNode.path("properties")
                                     .path("coreNOTAMData")
                                     .path("notam");

            NOTAM notam = new NOTAM();
            notam.setId(data.path("id").asText());
            notam.setNumber(data.path("number").asText());
            notam.setText(data.path("text").asText());
            notam.setIcaoLocation(icaoLocation);

            notamList.add(notam);
        }

        return NOTAMUtils.removeDuplicates(notamList);
    }

    public NotamSearchResponse getNotamsForRoute(String departure, String destination) {
        try {
            List<NOTAM> combined = new ArrayList<>();
            combined.addAll(getNotams(departure));
            combined.addAll(getNotams(destination));

            List<NOTAM> deduped = NOTAMUtils.removeDuplicates(combined);
            List<NotamDto> notamDtos = new ArrayList<>();

            for (NOTAM notam : deduped) {
                notamDtos.add(new NotamDto(
                        notam.getId(),
                        notam.getIcaoLocation(),
                        notam.getEffectiveStart() != null ? notam.getEffectiveStart().toString() : null,
                        notam.getEffectiveEnd() != null ? notam.getEffectiveEnd().toString() : null,
                        notam.getClassification(),
                        notam.getText()
                ));
            }

            return new NotamSearchResponse(departure, destination, notamDtos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch NOTAMs for route", e);
        }
    }
}
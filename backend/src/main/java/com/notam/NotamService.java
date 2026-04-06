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
}
package com.notam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;
import com.notam.model.NOTAMUtils;
import com.notam.model.NotamDto;
import com.notam.model.NotamSearchResponse;
import com.notam.repository.NotamRepository;

@Service
public class NotamService {

    private static final String CLIENT_ID = "5982191bfef7458aa9cb8e8c9674b645";
    private static final String CLIENT_SECRET = System.getenv("FAA_CLIENT_SECRET");

    private final NotamRepository notamRepository;

    public NotamService(ObjectProvider<NotamRepository> notamRepositoryProvider) {
        this.notamRepository = notamRepositoryProvider.getIfAvailable();
    }

    public List<NOTAM> getNotams(String icaoLocation) throws Exception {
        String normalizedIcao = normalizeIcao(icaoLocation);
        String url = "https://external-api.faa.gov/notamapi/v1/notams?icaoLocation=" + normalizedIcao;

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

        if (items != null && items.isArray()) {
            for (JsonNode notamNode : items) {
                JsonNode data = notamNode.path("properties")
                                         .path("coreNOTAMData")
                                         .path("notam");

                NOTAM notam = new NOTAM();
                notam.setId(data.path("id").asText(null));
                notam.setNumber(data.path("number").asText(null));
                notam.setText(data.path("text").asText(null));
                notam.setIcaoLocation(normalizedIcao);

                notamList.add(notam);
            }
        }

        List<NOTAM> deduped = NOTAMUtils.removeDuplicates(notamList);
        saveToFirestore(deduped);
        return deduped;
    }

    public List<NOTAM> getStoredNotams(String icaoLocation) {
        if (notamRepository == null) {
            throw new IllegalStateException("Firestore storage is not enabled. Set firestore.enabled=true and configure GOOGLE_APPLICATION_CREDENTIALS.");
        }
        return NOTAMUtils.removeDuplicates(notamRepository.findByIcaoLocation(normalizeIcao(icaoLocation)));
    }

    public NotamSearchResponse getNotamsForRoute(String departure, String destination) {
        try {
            List<NOTAM> combined = new ArrayList<>();
            combined.addAll(getNotams(departure));
            combined.addAll(getNotams(destination));

            List<NOTAM> deduped = NOTAMUtils.removeDuplicates(combined);
            List<NotamDto> notamDtos = toDtos(deduped);

            return new NotamSearchResponse(departure, destination, notamDtos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch NOTAMs for route", e);
        }
    }

    private void saveToFirestore(List<NOTAM> notams) {
        if (notamRepository != null) {
            notamRepository.saveAll(notams);
        }
    }

    private static List<NotamDto> toDtos(List<NOTAM> notams) {
        List<NotamDto> notamDtos = new ArrayList<>();
        for (NOTAM notam : notams) {
            notamDtos.add(new NotamDto(
                    notam.getId(),
                    notam.getIcaoLocation(),
                    notam.getEffectiveStart() != null ? notam.getEffectiveStart().toString() : null,
                    notam.getEffectiveEnd() != null ? notam.getEffectiveEnd().toString() : null,
                    notam.getClassification(),
                    notam.getText()
            ));
        }
        return notamDtos;
    }

    public NotamSearchResponse getStoredNotamsForRoute(
        String departure,
        String destination,
        double corridorNM) {

    if (notamRepository == null) {
        throw new IllegalStateException(
                "Firestore storage is not enabled. Set firestore.enabled=true and configure GOOGLE_APPLICATION_CREDENTIALS.");
    }

    String normalizedDeparture = normalizeIcao(departure);
    String normalizedDestination = normalizeIcao(destination);

    List<NOTAM> all = notamRepository.findAll();

    List<NOTAM> filtered = all.stream()
            .filter(n -> {
                String loc = n.getIcaoLocation();
                if (loc == null) {
                    return false;
                }

                return loc.equalsIgnoreCase(normalizedDeparture)
                        || loc.equalsIgnoreCase(normalizedDestination);
            })
            .toList();

    List<NOTAM> deduped = NOTAMUtils.removeDuplicates(filtered);
    List<NotamDto> notamDtos = toDtos(deduped);

    return new NotamSearchResponse(
            normalizedDeparture,
            normalizedDestination,
            notamDtos
    );
}

    private static String normalizeIcao(String icaoLocation) {
        if (icaoLocation == null || icaoLocation.isBlank()) {
            throw new IllegalArgumentException("ICAO location is required");
        }
        return icaoLocation.trim().toUpperCase();
    }
}

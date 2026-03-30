package com.notam.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;
import com.notam.parser.NotamParser;

public class NotamCLI {

    private static final String CLIENT_ID = "5982191bfef7458aa9cb8e8c9674b645";
    private static final String CLIENT_SECRET;

    static {
        CLIENT_SECRET = System.getenv("FAA_CLIENT_SECRET");
        if (CLIENT_SECRET == null || CLIENT_SECRET.isBlank()) {
            System.err.println("ERROR: FAA_CLIENT_SECRET environment variable is not set.");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 'EXIT' at any time to quit.");

        while (true) {
            System.out.print("Enter departure ICAO code: ");
            String departure = scanner.nextLine().trim().toUpperCase();
            if (departure.equals("EXIT")) break;

            System.out.print("Enter destination ICAO code: ");
            String destination = scanner.nextLine().trim().toUpperCase();
            if (destination.equals("EXIT")) break;

            System.out.println("\nFetching NOTAMs for departure: " + departure);
            fetchAndDisplay(departure);

            System.out.println("\nFetching NOTAMs for destination: " + destination);
            fetchAndDisplay(destination);

            System.out.println("\n--------------------------------------\n");
        }

        scanner.close();
        System.out.println("Goodbye!");
    }

    private static void fetchAndDisplay(String icaoCode) {
        try {
            String url = "https://external-api.faa.gov/notamapi/v1/notams?icaoLocation=" + icaoCode;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("client_id", CLIENT_ID)
                    .header("client_secret", CLIENT_SECRET)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();

            if (code >= 200 && code < 300) {
                ObjectMapper mapper = new ObjectMapper();
                NotamParser parser = new NotamParser(mapper);
                List<NOTAM> notams = parser.parsePage(response);

                int index = 0;
                for (NOTAM notam : notams) {
                    index++;
                    System.out.println("------NOTAM------ number: " + index);
                    System.out.println("---text---\n" + notam.getText());
                    System.out.println("---issued---\n" + notam.getIssued());
                }
            } else {
                System.err.println("Error fetching NOTAMs for " + icaoCode + ". HTTP status: " + code);
            }

        } catch (Exception e) {
            System.err.println("Exception while fetching NOTAMs for " + icaoCode + ": " + e.getMessage());
        }
    }
}
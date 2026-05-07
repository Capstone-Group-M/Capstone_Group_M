package com.notam.controller;

import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Arrays;
import java.util.stream.Collectors;
import com.notam.client.CGIApiClient;
import com.notam.client.new_client.SWIMConsumer;
import com.notam.model.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.notam.utils.AirportDataUtil;
import com.notam.service.*;

public class Main {

    private static final String CLIENT_ID = "t9hKmqUptGmfBAa7lzpVfDISM4KYEoNR7wCHxTAK8RkigeE9";
    private static final String CLIENT_SECRET;

    static {
        CLIENT_SECRET = System.getenv("CGI_CLIENT_SECRET");
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==== Welcome to your NOTAM Flight Management System ====\n");
        System.out.println("Select which NOTAM features to display by entering each number separated by a comma:");
        System.out.println(" 1:  All Features");
        System.out.println(" 2:  Id");
        System.out.println(" 3:  NOTAM Number");
        System.out.println(" 4:  Series");
        System.out.println(" 5:  Type");
        System.out.println(" 6:  Account Id");
        System.out.println(" 7:  ICAO Location");
        System.out.println(" 8:  NOTAM Dates");
        System.out.println(" 9:  NOTAM Location Info");
        System.out.println(" 10: Classification & Purpose");
        System.out.println(" 11: Text");

        // Validate menu choice
        Set<String> validOptions = Set.of("1","2","3","4","5","6","7","8","9","10","11");
        Set<String> selections = null;
        while (selections == null) {
            System.out.print("\nChoice: ");
            String choice = scanner.nextLine().trim();
            Set<String> parsed = Arrays.stream(choice.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            if (parsed.stream().allMatch(validOptions::contains)) {
                selections = parsed;
            } else {
                System.out.println("Invalid choice. Please enter numbers between 1 and 11 separated by commas.");
            }
        }

        boolean showAll = selections.contains("1");

        // Validate max display
        int max_display = promptForInt(scanner,
                "\nEnter the max number of NOTAMs to display (for all NOTAMs enter -1): ",
                n -> n == -1 || n > 0,
                "Please enter -1 for all NOTAMs or a positive number.");

        // Validate radius
        int radius = promptForInt(scanner,
                "\nEnter the flight corridor size (nautical miles, must be > 0 and < 100): ",
                n -> n > 0 && n < 100,
                "Please enter a positive number less than 100.");

        if (CLIENT_SECRET == null || CLIENT_SECRET.isBlank()) {
            System.err.println("ERROR: CGI_CLIENT_SECRET is not set.");
            return;
        }

        System.out.print("\nEnter the departure airport id from csv: ");
        Coordinate coordinate_1 = promptForAirportId(scanner);

        System.out.print("\nEnter the destination airport id from csv: ");
        Coordinate coordinate_2 = promptForAirportId(scanner);

        CGIApiClient cgiClient = new CGIApiClient(CLIENT_ID, CLIENT_SECRET);
        List<NOTAM> notams = cgiClient.fetchNotamsByCoordinates(coordinate_1, radius);
        notams.addAll(0, cgiClient.fetchNotamsByCoordinates(coordinate_2, radius));

        notams = NOTAMUtils.removeDuplicates(notams);
        NOTAMRanking ranking = new NOTAMRanking();
        notams = ranking.rankNOTAMs(coordinate_1, coordinate_2, radius, notams);

        if (max_display == -1) {
            max_display = notams.size();
        }

        for (int i = 0; i < Math.min(max_display, notams.size()); i++) {
            NOTAM notam = notams.get(i);
            System.out.println("\n==== NOTAM " + (i + 1) + " ====");

            if (showAll || selections.contains("2"))
                System.out.println("Id:             " + notam.getId());
            if (showAll || selections.contains("3"))
                System.out.println("Number:         " + notam.getNumber());
            if (showAll || selections.contains("4"))
                System.out.println("Series:         " + notam.getSeries());
            if (showAll || selections.contains("5"))
                System.out.println("Type:           " + notam.getType());
            if (showAll || selections.contains("6"))
                System.out.println("Account Id:     " + notam.getAccountId());
            if (showAll || selections.contains("7"))
                System.out.println("ICAO:           " + notam.getIcaoLocation());
            if (showAll || selections.contains("8")) {
                System.out.println("Issued:         " + notam.getIssued());
                System.out.println("Effective:      " + notam.getEffectiveStart() + " → " + notam.getEffectiveEnd());
                System.out.println("Last updated:   " + notam.getLastUpdated());
            }
            if (showAll || selections.contains("9")) {
                System.out.println("Location:       " + notam.getLocation());
                System.out.println("Coordinates:    " + notam.getCoordinates());
                System.out.println("Min FL:         " + notam.getMinimumFL());
                System.out.println("Max FL:         " + notam.getMaximumFL());
            }
            if (showAll || selections.contains("10")) {
                System.out.println("Classification: " + notam.getClassification());
                System.out.println("Traffic:        " + notam.getTraffic());
                System.out.println("Purpose:        " + notam.getPurpose());
                System.out.println("Scope:          " + notam.getScope());
                System.out.println("Selection code: " + notam.getSelectionCode());
            }
            if (showAll || selections.contains("11"))
                System.out.println("Text:\n" + notam.getText());
        }
        scanner.close();
    }

    private static int promptForInt(Scanner scanner, String prompt,
                                    java.util.function.IntPredicate validator,
                                    String errorMsg) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (validator.test(value)) return value;
                System.out.println("Invalid input. " + errorMsg);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input — please enter a whole number. " + errorMsg);
            }
        }
    }

    private static void runSWIMFeed() throws Exception {
        System.out.println("----- Starting SWIM NOTAM Feed -----");
        System.out.println("Receiving US NOTAMs in real-time...");
        System.out.println("Press Ctrl+C to stop.\n");

        Config config = ConfigFactory.load();

        SWIMConsumer consumer = new SWIMConsumer(
            config.getString("providerUrl"),
            config.getString("queue"),
            config.getString("connectionFactory"),
            config.getString("username"),
            config.getString("password"),
            config.getString("vpn"),
            config.getString("output"),
            config.getBoolean("json")
        );
        consumer.connect();

        Thread.currentThread().join();
    }

    private static Coordinate promptForAirportId(Scanner scanner) {
        Coordinate coordinates = null;
        String airport = scanner.nextLine();
        while (coordinates == null) {
            coordinates = AirportDataUtil.getCoordinatesFromIdent(airport);
            if (coordinates == null) {
                System.out.print("Coordinates for that airport ID not found, try again: ");
                airport = scanner.nextLine();
            }
        }
        return coordinates;
    }
}
package com.notam.controller;

import java.util.List;
import java.util.Scanner;
import com.notam.client.FAAOldClient;
import com.notam.client.new_client.SWIMConsumer;
import com.notam.model.NOTAM;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Main {

    private static final String CLIENT_ID = "5982191bfef7458aa9cb8e8c9674b645";
    private static final String CLIENT_SECRET;

    static {
        CLIENT_SECRET = System.getenv("FAA_CLIENT_SECRET");
    }

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select data source:");
        System.out.println("1) Old FAA REST API");
        System.out.println("2) New SWIM Feed");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            runOldApi(scanner);
        } else if (choice.equals("2")) {
            runSWIMFeed();
        } else {
            System.out.println("Invalid choice.");
        }

        scanner.close();
    }

    private static void runOldApi(Scanner scanner) throws Exception {
        if (CLIENT_SECRET == null || CLIENT_SECRET.isBlank()) {
            System.err.println("ERROR: FAA_CLIENT_SECRET is not set.");
            return;
        }

        System.out.println("----- Search Notams by ICAO Location -----");
        String input = "";

        while (!input.equals("EXIT")) {
            System.out.println("\nEnter 'EXIT' to quit");
            System.out.print("Enter ICAO Location (example: KOKC): ");
            input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("EXIT")) break;

            FAAOldClient faaClient = new FAAOldClient(CLIENT_ID, CLIENT_SECRET);
            List<NOTAM> notams = faaClient.fetchAllNotams(input);

            int number = 0;
            for (NOTAM notam : notams) {
                System.out.println("\n==== NOTAM " + number + ", ID = " + notam.getAccountId() + " ====");
                System.out.println("---text---\n" + notam.getText());
                System.out.println("---issued---\n" + notam.getIssued());
                System.out.println("---id---\n" + notam.getId());
                System.out.println("---number---\n" + notam.getNumber());
                number++;
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

        // Keep running until Ctrl+C
        Thread.currentThread().join();
    }
}
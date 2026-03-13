package cli;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

public class NotamCLI {

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
            runBackendQuery(departure);

            System.out.println("\nFetching NOTAMs for destination: " + destination);
            runBackendQuery(destination);

            System.out.println("\n--------------------------------------\n");
        }

        scanner.close();
        System.out.println("Goodbye!");
    }

    private static void runBackendQuery(String airportCode) {
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "mvn",
                    "exec:java",
                    "-Dexec.mainClass=com.notam.controller.Main"
            );

            builder.directory(new java.io.File("backend"));
            builder.redirectErrorStream(true);

            Process process = builder.start();

            InputStream backendOut = process.getInputStream();
            OutputStream backendIn = process.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(backendOut));

            String line;
            boolean sentAirport = false;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                if (!sentAirport && line.contains("Enter ICAO Location")) {
                    backendIn.write((airportCode + "\n").getBytes());
                    backendIn.flush();
                    sentAirport = true;
                }
            }

            process.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
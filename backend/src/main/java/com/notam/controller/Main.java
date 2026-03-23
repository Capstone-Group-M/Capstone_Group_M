package com.notam.controller;

import java.util.List;
import java.util.Scanner;
import com.notam.client.FAAClient;
import com.notam.model.NOTAM;


public class Main {

    //do not share CLIENT_SECRET in plain text
    private static final String CLIENT_ID = "5982191bfef7458aa9cb8e8c9674b645";
    //this is a secret value, FAA_CLIENT_SECRET is put in terminal manually with ~export FAA_CLIENT_SECRET=abc123
    private static final String CLIENT_SECRET;

    static {
        CLIENT_SECRET = System.getenv("FAA_CLIENT_SECRET");

        if (CLIENT_SECRET == null || CLIENT_SECRET.isBlank()) {
            System.err.println("ERROR: Environment variable FAA_CLIENT_SECRET is not set.");
            System.err.println("Please set it before running the program.");
            System.exit(1);
        }
    }
 
    public static void main(String[] args) throws Exception {
        System.out.println("----- Search Notams by ICAO Location-----");
        Scanner scanner = new Scanner(System.in);
        String input = "";

        //get NOTAMs for different locations until "EXIT"
        while (!input.equals("EXIT")) {
            //get input
            System.out.println("\n Enter 'EXIT' to quit program");
            System.out.println("Enter ICAO Location (example: KOKC): ");
            input = scanner.nextLine().trim().toUpperCase();

            FAAClient faaClient = new FAAClient(CLIENT_ID, CLIENT_SECRET);
            List<NOTAM> notams = faaClient.fetchAllNotams(input);
            
            int number = 0;
            for (NOTAM notam : notams){
                System.out.println("\n==== NOTAM number " + number + ", Notam ID = " + notam.getAccountId() + "====");
                System.out.println("---text---\n" + notam.getText());
                System.out.println("---issued---\n" + notam.getIssued());
                number++;
            }
        }
        scanner.close();
    }
}
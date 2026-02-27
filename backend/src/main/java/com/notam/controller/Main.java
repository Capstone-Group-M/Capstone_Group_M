package com.notam.controller;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        System.out.println("----- Search Notams by ICAO Location-----\n");

        //get user location input
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter 'EXIT' to quit program");
        System.out.println("Enter ICAO Location (example: KOKC): ");
        String input = scanner.nextLine().trim().toUpperCase();

        //get NOTAMs for different locations until "EXIT"
        while (!input.equals("EXIT")) {

            //domain to FAA API server
            String domain = "external-api.faa.gov";

            //full url to send request. does not have to be icaoLocation=.
            //it can be other parameters such as notamType,classification,notamNumber, etc... 
            String url = "https://" + domain + "/notamapi/v1/notams?icaoLocation=" + input;
            
            //HTTP Client used to connect to server, send request, and recieve responses
            HttpClient client = HttpClient.newHttpClient();

            //create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    //send request to this url
                    .uri(URI.create(url))
                    //checks credentials
                    .header("client_id", CLIENT_ID)
                    .header("client_secret", CLIENT_SECRET)
                    //tells server "I want to retrieve data"
                    .GET()
                    //finalize request
                    .build();

            //send the request and store the response        
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            int code = response.statusCode();
            
            // check for any error codes from request
            if (code > 200 || code < 300){
                //from Jackson Library used to parse JSON object        
                ObjectMapper mapper = new ObjectMapper();

                //takes repsonse and converts it to a navigable JSON tree object
                JsonNode root = mapper.readTree(response.body());

                //retrieves the "items" key from JSON object
                JsonNode items = root.get("items");

                //check if Notam was recieved
                if (items.isEmpty()) {
                    System.out.println("No NOTAMs found.");
                } else {
                    for (JsonNode notam : items) {
                        System.out.println("----- NOTAM -----");
                        JsonNode textNode = notam.path("properties")
                                                .path("coreNOTAMData")
                                                .path("notam")
                                                .path("text");

                        if (!textNode.isMissingNode() && !textNode.isNull() && !textNode.asText().isBlank()) {
                            System.out.println(textNode.asText());
                        } else {
                            System.out.println("No text field found. Full object:");
                            System.out.println(notam.toPrettyString());
                        }
                    }
                }
            }
            else{
                System.err.println("There was an error connecting to client. Error code = " + String.valueOf(code));
                System.out.println("If you would like to try again enter 'y', if not enter 'n' : ");
                String try_again = scanner.nextLine().trim().toUpperCase();
                if (try_again.equals("n")){
                    break;
                }
            }

            //get next input
            System.out.println("\n Enter 'EXIT' to quit program");
            System.out.println("Enter ICAO Location (example: KOKC): ");
            input = scanner.nextLine().trim().toUpperCase();
        }
        scanner.close();
    }
}

package cli;

import java.util.*;

public class NotamCLI {

    static class Notam {
        String id;
        String type;
        String location;
        String urgency;

        Notam(String id, String type, String location, String urgency) {
            this.id = id;
            this.type = type;
            this.location = location;
            this.urgency = urgency;
        }
    }

    static List<Notam> fetchNotams() {
        List<Notam> list = new ArrayList<>();
        list.add(new Notam("A1", "Runway Closure", "KOKC", "normal"));
        list.add(new Notam("B2", "Airspace Restriction", "KLAX", "high"));
        return list;
    }

    public static void main(String[] args) {

        System.out.println("Fetching NOTAMs...\n");

        List<Notam> notams = fetchNotams();

        for (Notam n : notams) {
            System.out.println("ID: " + n.id);
            System.out.println("Type: " + n.type);
            System.out.println("Location: " + n.location);
            System.out.println("Urgency: " + n.urgency);
            System.out.println("--------------------");
        }
    }
}
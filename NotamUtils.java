import java.util.*;

public class NotamUtils {

    public static class Notam {
        public String id;
        public String type;
        public String location;
        public String urgency;

        public Notam(String id, String type, String location, String urgency) {
            this.id = id;
            this.type = type;
            this.location = location;
            this.urgency = urgency;
        }
    }

    public static List<Notam> fetchNotams() {
        List<Notam> notams = new ArrayList<>();
        notams.add(new Notam("A1", "Runway Closure", "KOKC", "normal"));
        notams.add(new Notam("B2", "Airspace Restriction", "KLAX", "high"));
        return notams;
    }

    public static Notam processNotam(Map<String, String> rawNotam) {
        String id = rawNotam.get("id");
        String type = rawNotam.get("type");
        String location = rawNotam.get("location");
        String urgency = rawNotam.getOrDefault("urgency", "normal");

        return new Notam(id, type, location, urgency);
    }
}

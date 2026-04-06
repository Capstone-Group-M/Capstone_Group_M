package notam.client;

import java.util.ArrayList;
import java.util.List;

import notam.model.Notam;

public class FaaNotamClient {

    // Simulated API response
    public List<Notam> fetchNotams(String airportCode) {

        List<Notam> notams = new ArrayList<>();

        notams.add(new Notam(
                "A1234/24",
                airportCode,
                "2026-03-01T10:00:00Z",
                "2026-03-02T18:00:00Z",
                "RWY",
                "RWY 13L/31R CLOSED"
        ));

        // Duplicate
        notams.add(new Notam(
                "A1234/24",
                airportCode,
                "2026-03-01T10:00:00Z",
                "2026-03-02T18:00:00Z",
                "RWY",
                "RWY 13L/31R CLOSED"
        ));

        notams.add(new Notam(
                "B5678/24",
                airportCode,
                "2026-03-01T12:00:00Z",
                "2026-03-05T18:00:00Z",
                "NAV",
                "ILS OUT OF SERVICE"
        ));

        return notams;
    }
}
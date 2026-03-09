// Unit tests for NOTAM ranking
/*
The math for this could be wrong
*/
package com.notam.service;

import com.notam.model.Coordinate;
import com.notam.model.Route;
import com.notam.model.NOTAM;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

public class NOTAMRankingTest {

    // Testing cross track
    @Test
    void testCrossTrackDistance() {
        // Route from (0,0) to (0,10)
        Route route = new Route(new Coordinate(0, 0), new Coordinate(0, 10), 5.0);
        NOTAMRanking ranking = new NOTAMRanking();

        // NOTAM directly on route
        Coordinate notamOnRoute = new Coordinate(0, 5);
        double distanceOnRoute = ranking.crossTrackDistance(route, notamOnRoute);
        assertEquals(0, distanceOnRoute, 0.01, "Distance should be 0 for point on route");

        // NOTAM off route by 1 degree latitude (~60 NM)
        Coordinate notamOffRoute = new Coordinate(1, 5);
        double distanceOffRoute = ranking.crossTrackDistance(route, notamOffRoute);
        assertTrue(distanceOffRoute > 0, "Distance should be positive for off-route NOTAM");
    }

    // Test ranking
    @Test
    void testRankNOTAMs() {
        // Create corridor route of 5 NM
        Route route = new Route(new Coordinate(0, 0), new Coordinate(0, 10), 5.0); // 5 NM corridor
        NOTAMRanking ranking = new NOTAMRanking();

        // Create NOTAMs
        NOTAM closeNOTAM = new NOTAM("1", "001", "Somewhere");
        closeNOTAM.setCoordinates("0000N0050E"); // approx 0N 5E so on route
        NOTAM farNOTAM = new NOTAM("2", "002", "FarAway");
        farNOTAM.setCoordinates("0100N0050E"); // 1 deg north which is about ~60 NM away
        NOTAM nullNOTAM = new NOTAM("3", "003", "Unknown");
        nullNOTAM.setCoordinates(null); // should be skipped

        List<NOTAM> notams = List.of(closeNOTAM, farNOTAM, nullNOTAM);

        List<NOTAM> ranked = ranking.rankNOTAMs(route, notams);

        // Only the close NOTAM should be included
        assertEquals(1, ranked.size(), "Only NOTAMs within corridor should be included");
        assertEquals("1", ranked.get(0).getId(), "The closest NOTAM should come first");
    }

    // Testing rank again with more NOTAMs
    // This one is more sorting based
    @Test
    void testRankNOTAMsSort() {

        // Route with massive corridor width to sort all NOTAMs
        Route route = new Route(new Coordinate(0, 0), new Coordinate(0, 10), 200.0);

        NOTAMRanking ranking = new NOTAMRanking();

        // NOTAM directly on route
        NOTAM n1 = new NOTAM("1", "001", "A");
        n1.setCoordinates("0000N0050E");

        // NOTAM slightly north
        NOTAM n2 = new NOTAM("2", "002", "B");
        n2.setCoordinates("0030N0050E");

        // NOTAM farther north
        NOTAM n3 = new NOTAM("3", "003", "C");
        n3.setCoordinates("0100N0050E");

        // List of notams not in sorted order to see if they are actually sorted
        List<NOTAM> notams = List.of(n3, n1, n2); 

        // Rank NOTAMs
        List<NOTAM> ranked = ranking.rankNOTAMs(route, notams);

        // Check order: closest first
        assertEquals("1", ranked.get(0).getId());
        assertEquals("2", ranked.get(1).getId());
        assertEquals("3", ranked.get(2).getId());
    }

    // Testing to see if it removes all NOTAMS since they are outside
    @Test
    void testRankNOTAMsOutside() {

        // Very small corridor width
        Route route = new Route(new Coordinate(0,0), new Coordinate(0,10), 1.0);

        NOTAMRanking ranking = new NOTAMRanking();

        // Both NOTAMs far from route
        NOTAM n1 = new NOTAM("1", "001", "Far1");
        n1.setCoordinates("0100N0050E");

        NOTAM n2 = new NOTAM("2", "002", "Far2");
        n2.setCoordinates("0200N0050E");

        List<NOTAM> ranked = ranking.rankNOTAMs(route, List.of(n1, n2));

        // No NOTAMs should pass the filter
        assertTrue(ranked.isEmpty());
    }

    // Test the edge cases of NOTAMs around the corridor boundary (outside and inside)
    @Test
    void testRankNOTAMsEdgeCases() {

        // Create a route from (0,0) to (0,10)
        // Corridor width is 61 NM
        Route route = new Route(new Coordinate(0, 0), new Coordinate(0, 10), 61.0);

        NOTAMRanking ranking = new NOTAMRanking();

        // NOTAM exactly on the route 
        NOTAM onRoute = new NOTAM("1", "001", "OnRoute");
        onRoute.setCoordinates("0000N0050E");

        // NOTAM just inside corridor (~0.9 degree north is about 54 NM)
        NOTAM justInside = new NOTAM("2", "002", "Inside");
        justInside.setCoordinates("0054N0050E");

        // NOTAM approximately on the corridor boundary (~1 degree north is about 60 NM)
        NOTAM onEdge = new NOTAM("3", "003", "Edge");
        onEdge.setCoordinates("0100N0050E");

        // NOTAM just outside corridor (~1.1 degree north is about 66 NM)
        NOTAM justOutside = new NOTAM("4", "004", "Outside");
        justOutside.setCoordinates("0110N0050E");

        // Add NOTAMs to list
        List<NOTAM> notams = List.of(onRoute, justInside, onEdge, justOutside);

        // Run ranking 
        List<NOTAM> ranked = ranking.rankNOTAMs(route, notams);

        // Expect 3 NOTAMs to remain (outside one should be removed)
        assertEquals(3, ranked.size());

        // Ensure the outside NOTAM was filtered out
        boolean containsOutside = ranked.stream().anyMatch(n -> n.getId().equals("4"));

        assertFalse(containsOutside, "NOTAM outside corridor should be removed");

        // Ensure edge NOTAM is included (because condition uses <= corridor)
        boolean containsEdge = ranked.stream().anyMatch(n -> n.getId().equals("3"));

        assertTrue(containsEdge, "NOTAM exactly on corridor edge should be included");
    }
}
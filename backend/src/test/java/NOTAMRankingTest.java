// Unit tests for NOTAM ranking
/*
The math for this could be wrong
*/
package com.notam.service;

import com.notam.model.Coordinate;
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
        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);
        double corridorNM = 5.0;
        NOTAMRanking ranking = new NOTAMRanking();

        // NOTAM directly on route
        Coordinate notamOnRoute = new Coordinate(0, 5);
        double distanceOnRoute = ranking.crossTrackDistance(departure, destination, notamOnRoute);
        assertEquals(0, distanceOnRoute, 0.01, "Distance should be 0 for point on route");

        // NOTAM off route by 1 degree latitude (~60 NM)
        Coordinate notamOffRoute = new Coordinate(1, 5);
        double distanceOffRoute = ranking.crossTrackDistance(departure, destination, notamOffRoute);
        assertTrue(distanceOffRoute > 0, "Distance should be positive for off-route NOTAM");
    }

    // Test ranking
    @Test
    void testRankNOTAMs() {
        // Create corridor route of 5 NM
        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);
        double corridorNM = 5.0;
        NOTAMRanking ranking = new NOTAMRanking();

        // Create NOTAMs
        NOTAM closeNOTAM = new NOTAM("1", "001", "Somewhere");
        closeNOTAM.setCoordinates("0000N0050E"); // approx 0N 5E so on route
        NOTAM farNOTAM = new NOTAM("2", "002", "FarAway");
        farNOTAM.setCoordinates("0100N0050E"); // 1 deg north which is about ~60 NM away
        NOTAM nullNOTAM = new NOTAM("3", "003", "Unknown");
        nullNOTAM.setCoordinates(null); // should be skipped

        List<NOTAM> notams = List.of(closeNOTAM, farNOTAM, nullNOTAM);

        List<NOTAM> ranked = ranking.rankNOTAMs(departure, destination, corridorNM, notams);

        // Only the close NOTAM should be included
        assertEquals(1, ranked.size(), "Only NOTAMs within corridor should be included");
        assertEquals("1", ranked.get(0).getId(), "The closest NOTAM should come first");
    }

    // Testing rank again with more NOTAMs
    // This one is more sorting based
    @Test
    void testRankNOTAMsSort() {

        // Route with massive corridor width to sort all NOTAMs
        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);
        double corridorNM = 200.0;

        NOTAMRanking ranking = new NOTAMRanking();

        // NOTAM directly on route
        NOTAM notam1 = new NOTAM("1", "001", "A");
        notam1.setCoordinates("0000N0050E");

        // NOTAM slightly north
        NOTAM notam2 = new NOTAM("2", "002", "B");
        notam2.setCoordinates("0030N0050E");

        // NOTAM farther north
        NOTAM n3 = new NOTAM("3", "003", "C");
        n3.setCoordinates("0100N0050E");

        // List of notams not in sorted order to see if they are actually sorted
        List<NOTAM> notams = List.of(n3, notam1, notam2); 

        // Rank NOTAMs
        List<NOTAM> ranked = ranking.rankNOTAMs(departure, destination, corridorNM, notams);

        // Check order: closest first
        assertEquals("1", ranked.get(0).getId());
        assertEquals("2", ranked.get(1).getId());
        assertEquals("3", ranked.get(2).getId());
    }

    // Testing to see if it removes all NOTAMS since they are outside
    @Test
    void testRankNOTAMsOutside() {

        // Very small corridor width
        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);
        double corridorNM = 1.0;

        NOTAMRanking ranking = new NOTAMRanking();

        // Both NOTAMs far from route
        NOTAM notam1 = new NOTAM("1", "001", "Far1");
        notam1.setCoordinates("0100N0050E");

        NOTAM notam2 = new NOTAM("2", "002", "Far2");
        notam2.setCoordinates("0200N0050E");

        List<NOTAM> ranked = ranking.rankNOTAMs(departure, destination, corridorNM, List.of(notam1, notam2));

        // No NOTAMs should pass the filter
        assertTrue(ranked.isEmpty());
    }

    // Test the edge cases of NOTAMs around the corridor boundary (outside and inside)
    @Test
    void testRankNOTAMsEdgeCases() {

        // Create a route from (0,0) to (0,10)
        // Corridor width is 61 NM
        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);
        double corridorNM = 61.0;

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
        List<NOTAM> ranked = ranking.rankNOTAMs(departure, destination, corridorNM, notams);

        // Expect 3 NOTAMs to remain (outside one should be removed)
        assertEquals(3, ranked.size());

        // Ensure the outside NOTAM was filtered out
        boolean containsOutside = ranked.stream().anyMatch(n -> n.getId().equals("4"));

        assertFalse(containsOutside, "NOTAM outside corridor should be removed");

        // Ensure edge NOTAM is included (because condition uses <= corridor)
        boolean containsEdge = ranked.stream().anyMatch(n -> n.getId().equals("3"));

        assertTrue(containsEdge, "NOTAM exactly on corridor edge should be included");
    }


    // BOUNDING BOX METHOD TEST(S)
    // Test bounding box filters out far NOTAMs
    // This is just the basic filter to see if it works (or the correctnes of the method)
    @Test
    void testBoundingBoxBasicFilter() {

        NOTAMRanking ranking = new NOTAMRanking();

        // Route from (0,0) to (0,10)
        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);

        double corridorNM = 60.0; // ~1 degree buffer

        // Notams that should be inside box
        NOTAM inside1 = new NOTAM("1", "001", "Inside1");
        inside1.setCoordinates("0000N0050E"); // on route

        NOTAM inside2 = new NOTAM("2", "002", "Inside2");
        inside2.setCoordinates("0050N0050E"); // 0.5 deg north (inside)

        // Notams that should be outside the box
        NOTAM outside = new NOTAM("3", "003", "Outside");
        outside.setCoordinates("0300N0050E"); // 3 deg north (outside)

        // List of the notams
        List<NOTAM> notams = List.of(inside1, inside2, outside);

        // Result after running function
        List<NOTAM> result = ranking.boundingBox(departure, destination, corridorNM, notams);

        // Should only include the two inside NOTAMs
        assertEquals(2, result.size());
        assertTrue(result.contains(inside1));
        assertTrue(result.contains(inside2));
        assertFalse(result.contains(outside));
    }

    // Test bounding box includes NOTAMs on the boundary line
    @Test
    void testBoundingBoxEdgeInclusion() {

        NOTAMRanking ranking = new NOTAMRanking();

        // Departure and desintaiton 10 apart
        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);

        // Corridor width
        double corridorNM = 60.0; // ~1 degree

        // Exactly on upper boundary (~1 degree north)
        NOTAM edge = new NOTAM("1", "001", "Edge");
        edge.setCoordinates("0100N0050E");

        // Result
        List<NOTAM> result = ranking.boundingBox(departure, destination, corridorNM, List.of(edge));

        // Should be included because we use <= in the method
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
    }

    // Test bounding box removes all NOTAMs if all are outside
    // Should be empty
    @Test
    void testBoundingBoxAllOutside() {

        NOTAMRanking ranking = new NOTAMRanking();

        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);

        double corridorNM = 30.0; // small (~0.5 degree)

        NOTAM notam1 = new NOTAM("1", "001", "Far1");
        notam1.setCoordinates("0200N0050E");

        NOTAM notam2 = new NOTAM("2", "002", "Far2");
        notam2.setCoordinates("0300N0050E");

        List<NOTAM> result = ranking.boundingBox(departure, destination, corridorNM, List.of(notam1, notam2));

        assertTrue(result.isEmpty());
    }

    // Test bounding box preserves input order
    // Mainly just in case we needed order preserved for future implementations
    // This makes sure that we keep it like that
    @Test
    void testBoundingBoxOrderPreserved() {

        NOTAMRanking ranking = new NOTAMRanking();

        // New coordinates for route
        Coordinate departure = new Coordinate(0,0);
        Coordinate destination = new Coordinate(0,10);

        // Corridor width
        double corridorNM = 60.0;

        // Notams
        NOTAM notam1 = new NOTAM("1", "001", "A");
        notam1.setCoordinates("0000N0050E");

        NOTAM notam2 = new NOTAM("2", "002", "B");
        notam2.setCoordinates("0050N0050E");

        List<NOTAM> result = ranking.boundingBox(departure, destination, corridorNM, List.of(notam1, notam2));

        // Order should stay the same
        assertEquals("1", result.get(0).getId());
        assertEquals("2", result.get(1).getId());
    }

}
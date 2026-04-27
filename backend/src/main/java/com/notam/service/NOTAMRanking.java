package com.notam.service;

// Defined classes to be used for NOTAM ranking
import com.notam.model.Coordinate; // Coordinates
import com.notam.model.NOTAM;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/*
STEP 0: Perform base filter of bounding box
STEP 1: Compute route distance (great circle path)
STEP 2: Compute cross-track distance (and filter by corridor width)
*/
public class NOTAMRanking {

    // Ranks NOTAMs based on how close they are to the flight route (as of now)
    // The suggested values for corridor width is 25 NM
    public List<NOTAM> rankNOTAMs(Coordinate departure, Coordinate destination, double corridorNM, List<NOTAM> notams) {

        // Filtered NOTAMs via the bounding box to lower the amount of NOTAMs to go through
        List<NOTAM> boxedNotams = boundingBox(departure, destination, corridorNM, notams);

        // HashMap to store computed distances of NOTAMs to flight route
        Map<NOTAM, Double> notamDistance = new HashMap<>();

        // List to store filtered NOTAMs
        List<NOTAM> filteredNotams = new ArrayList<>();

        // Loop through all NOTAMs returned from FAA API filtered by boundingBox
        for (int index = 0; index < boxedNotams.size(); index++)
        {
            // Get NOTAM at index
            NOTAM notam = boxedNotams.get(index);

            // Get coordinate values (this is under the assumption that coordinate
            // values are under coordinate format ex: (x, y) of Doubles)
            Coordinate notamCoordinate = Coordinate.parse(notam.getCoordinates());

            // A check to skip NOTAM if coordinate is missing
            if (notamCoordinate == null) {
                continue;
            }

            // Compute distance of NOTAM to route (cross-track)
            double distance = crossTrackDistance(departure, destination, notamCoordinate);

            // Check and make sure it is within corridor width
            if (distance <= corridorNM) {
                // If within add into the list/map
                filteredNotams.add(notam);
                notamDistance.put(notam, distance);
            }
        }

        // Sort filtered NOTAMs from closest to farthest
        filteredNotams.sort((n1, n2) -> Double.compare(notamDistance.get(n1), notamDistance.get(n2)));

        // Return the sorted list
        return filteredNotams;
    }

    // STEP 0: BOUNDING BOX
    // This takes departure and destination and creates a box around the route
    // plus the buffer (corrdidor width). This is a base filter to quickly parse
    // through a multitude of NOTAMs
    public List<NOTAM> boundingBox(Coordinate departure, Coordinate destination, double corridorNM, List<NOTAM> notams) {

        // List to hold the filtered NOTAMs
        List<NOTAM> filtered = new ArrayList<>();

        // Convert nautical miles to approximate degrees
        double bufferDegrees = corridorNM / 60.0;

        // Create bounding box around route
        double minimumLatitude = Math.min(departure.getLatitude(), destination.getLatitude()) - bufferDegrees;
        double maximumLatitude = Math.max(departure.getLatitude(), destination.getLatitude()) + bufferDegrees;
        double minimumLongitude = Math.min(departure.getLongitude(), destination.getLongitude()) - bufferDegrees;
        double maximumLongitude = Math.max(departure.getLongitude(), destination.getLongitude()) + bufferDegrees;

        // Filter NOTAMs directly
        for (NOTAM notam : notams) {

            // Parse the coordinate string
            Coordinate coordinate = Coordinate.parse(notam.getCoordinates());

            // Skip if coordinate parsing fails or is null
            if (coordinate == null) continue;

            // Get the longitude and latitude
            double latitude = coordinate.getLatitude();
            double longitude = coordinate.getLongitude();

            // Bounding box check to see if NOTAM is within
            if (latitude >= minimumLatitude && latitude <= maximumLatitude &&
                longitude >= minimumLongitude && longitude <= maximumLongitude) {

                filtered.add(notam);
            }
        }

        // Return the filtered notams
        return filtered;
    }

    // STEP 2: COMPUTE CROSS TRACK
    // Computes the distance from a NOTAM to the route with cross-track
    // Returns the shortest perpendicular distance
    public double crossTrackDistance(Coordinate departure, Coordinate destination, Coordinate coordinate) {

        // Earth radius in NM
        final double EARTH_RADIUS = 3440.065;

        // Converting coordinates to radians (Assumption is Cooridinate is in degrees, so double (or floating point))
        // Departure values
        double latitude1 = Math.toRadians(departure.getLatitude());
        double longitude1 = Math.toRadians(departure.getLongitude());

        // Destination values
        double latitude2 = Math.toRadians(destination.getLatitude());
        double longitude2 = Math.toRadians(destination.getLongitude());

        // Given NOTAM point
        double latitude3 = Math.toRadians(coordinate.getLatitude());
        double longitude3 = Math.toRadians(coordinate.getLongitude());

        // This implicitly does haversine formula as well to get the great-circle path

        /*
        CROSS TRACK FORMULA - Link: https://www.movable-type.co.uk/scripts/latlong.html
        - dxt = arcsin(sin(delta_13)) * sin(theta_13 - theta_12)) * R
            - delta_13 is angular distance from departure to NOTAM
            - theta_13 is initial bearing from departure to NOTAM
            - theta_12 is initial bearing from departure to destination
            - R is the earth's radius

        This computes the shortest perpendicular distance from a point (NOTAM) to the great-circle path
        between departure and destination.
        */

        // Angular distance (delta_13) from departure and NOTAM
        double a = Math.sin((latitude3 - latitude1) / 2) * Math.sin((latitude3 - latitude1) / 2) +
            Math.cos(latitude1) * Math.cos(latitude3) *
            Math.sin((longitude3 - longitude1) / 2) * Math.sin((longitude3 - longitude1) / 2);

        double delta_13 = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Bearings (Call separate initialBearing function)
        double theta_13 = initialBearing(latitude1, longitude1, latitude3, longitude3);
        double theta_12 = initialBearing(latitude1, longitude1, latitude2, longitude2);

        // Cross track value 
        double crossTrackValue = Math.asin(Math.sin(delta_13) * Math.sin(theta_13 - theta_12)) * EARTH_RADIUS;

        // Return abs of cross track since we do not care about sides
        // We only need the magitude of the value
        // Positve one side, negative the other side
        return Math.abs(crossTrackValue);
    }

    // Initial bearing function
    // Calculates initial bearing for cross track distance
    private double initialBearing(double latitude1, double longitude1, double latitude2, double longitude2) {
            double y = Math.sin(longitude2 - longitude1) * Math.cos(latitude2);

            double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) *
                Math.cos(latitude2) * Math.cos(longitude2 - longitude1);

            return Math.atan2(y, x);
        }
    
}
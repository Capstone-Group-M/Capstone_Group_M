// This file defines coordinates
package com.notam.model;

// For regex matching
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Coordinate {

    // Variables to hold longitude and lad
    private final double latitude;
    private final double longitude;

    // Constructor (These values should be in degrees as in the returned NOTAM values)
    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and setters
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public static Coordinate parse(String coordinateString) {
        if (coordinateString == null) return null;

        // Remove whitespace for easier matching
        coordinateString = coordinateString.replaceAll("\\s+", "");

        /*
        Pattern explanation:
        - (\\d{1,2}) = latitude degrees (1-2 digits)
        - (\\d{0,2}(?:\\.\\d+)?)? = latitude minutes (optional, can have decimal)
        - ([NS]) = latitude direction
        - (\\d{1,3}) = longitude degrees (1-3 digits)
        - (\\d{0,2}(?:\\.\\d+)?)? = longitude minutes (optional, can have decimal)
        - ([EW]) = longitude direction
        */
        Pattern pattern = Pattern.compile("(\\d{1,2})(\\d{0,2}(?:\\.\\d+)?)?([NS])(\\d{1,3})(\\d{0,2}(?:\\.\\d+)?)?([EW])");
        Matcher matcher = pattern.matcher(coordinateString);

        // If it does not match, return null
        if (!matcher.matches()) return null;

        // If match, go in and parse
        try {
            // Latitude
            int latitudeDegree = Integer.parseInt(matcher.group(1));
            double latitudeMinute = 0.0;
            if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
                latitudeMinute = Double.parseDouble(matcher.group(2));
            }

            char latitudeDirection = matcher.group(3).charAt(0);

            // Longitude
            int longitudeDegree = Integer.parseInt(matcher.group(4));
            double longitudeMinute;
            if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
                longitudeMinute = Double.parseDouble(matcher.group(5));
            } else {
                longitudeMinute = 0.0;
            }
            char longitudeDirection = matcher.group(6).charAt(0);

            // Value checks
            // Cannot be equal or over 60 (Minute values are from 0-59)
            if (latitudeMinute >= 60 || longitudeMinute >= 60) { return null; }

            // Cannot be over the max value (latitude is 90 and longitude is 180)
            if (latitudeDegree > 90 || longitudeDegree > 180) { return null; }

            // Poles cannot have minutes
            if (latitudeDegree == 90 && latitudeMinute > 0) { return null; }
            if (longitudeDegree == 180 && longitudeMinute > 0) { return null; }

            // Convert to decimal degrees
            double latitude = latitudeDegree + latitudeMinute / 60.0;
            if (latitudeDirection == 'S') latitude = -latitude; // S means negative

            double longitude = longitudeDegree + longitudeMinute / 60.0;
            if (longitudeDirection == 'W') longitude = -longitude; // W means negative

            return new Coordinate(latitude, longitude);

        } catch (NumberFormatException e) {
            // Fail, return null
            return null;
        }
    }
}
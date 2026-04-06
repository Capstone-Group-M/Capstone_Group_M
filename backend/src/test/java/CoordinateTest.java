// Unit test for Coordinate.java


import org.junit.jupiter.api.Test;

import com.notam.model.Coordinate;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    // VALID CASES
    // Coordinates with minutes
    @Test
    void testValidWithMinutes() {
        Coordinate coord = Coordinate.parse("3745.5N12225.5W");
        assertNotNull(coord);
        assertEquals(37 + 45.5 / 60.0, coord.getLatitude(), 1e-6);
        assertEquals(-(122 + 25.5 / 60.0), coord.getLongitude(), 1e-6);
    }

    // Coordinates without minutes
    @Test
    void testValidWithoutMinutes() {
        Coordinate coord = Coordinate.parse("3745N12225W");
        assertNotNull(coord);
        assertEquals(37 + 45.0 / 60.0, coord.getLatitude(), 1e-6);
        assertEquals(-(122 + 25.0 / 60.0), coord.getLongitude(), 1e-6);
    }

    // Single digit degrees (e.g. 7N or 5N)
    @Test
    void testSingleDigitDegrees() {
        Coordinate coord = Coordinate.parse("5N7E");
        assertNotNull(coord);
        assertEquals(5.0, coord.getLatitude(), 1e-6);
        assertEquals(7.0, coord.getLongitude(), 1e-6);
    }

    // Leading zeros (e.g. 05N)
    @Test
    void testLeadingZeros() {
        Coordinate coord = Coordinate.parse("0705N12205W");
        assertNotNull(coord);
        assertEquals(7 + 5.0 / 60.0, coord.getLatitude(), 1e-6);   // 7.0833
        assertEquals(-(122 + 5.0 / 60.0), coord.getLongitude(), 1e-6); // -122.0833
    }

    // Southern and Western hemispheres (checks to make sure it returned negative values)
    @Test
    void testSouthAndWest() {
        Coordinate coord = Coordinate.parse("1230S04530W");
        assertNotNull(coord);
        assertEquals(-(12 + 30.0 / 60.0), coord.getLatitude(), 1e-6);
        assertEquals(-(45 + 30.0 / 60.0), coord.getLongitude(), 1e-6);
    }

    // Zero minutes
    @Test
    void testZeroMinutes() {
        Coordinate coord = Coordinate.parse("1200N06000E");
        assertNotNull(coord);
        assertEquals(12.0, coord.getLatitude(), 1e-6);
        assertEquals(60.0, coord.getLongitude(), 1e-6);
    }

    // Whitespace testing (see if it can handle spaces/whitespaces)
    @Test
    void testWhitespaceHandling() {
        Coordinate coord = Coordinate.parse(" 37 45.5 N 122 25.5 W ");
        assertNotNull(coord);
        assertEquals(37 + 45.5 / 60.0, coord.getLatitude(), 1e-6);
        assertEquals(-(122 + 25.5 / 60.0), coord.getLongitude(), 1e-6);
    }

    // The edge values of longitude (+/-180) and latitude (+/-90)
    @Test
    void testExtremeValues() {
        Coordinate northPole = Coordinate.parse("90N180E");
        assertNotNull(northPole);
        assertEquals(90.0, northPole.getLatitude(), 1e-6);
        assertEquals(180.0, northPole.getLongitude(), 1e-6);

        Coordinate southPole = Coordinate.parse("90S180W");
        assertNotNull(southPole);
        assertEquals(-90.0, southPole.getLatitude(), 1e-6);
        assertEquals(-180.0, southPole.getLongitude(), 1e-6);
    }

    // INVALID CASES

    // Null input
    @Test
    void testNullInput() {
        assertNull(Coordinate.parse(null));
    }

    // Empty string
    @Test
    void testEmptyString() {
        assertNull(Coordinate.parse(""));
    }

    // More incorrect strings
    @Test
    void testIncorrectRandomString() {
        assertNull(Coordinate.parse("ABCD")); // letters only
        assertNull(Coordinate.parse("123N456")); // missing direction for longitude
        assertNull(Coordinate.parse("123456"));  // missing both N/S/E/W
    }

    // Invalid numbers in coordinate
    @Test
    void testInvalidNumbers() {
        assertNull(Coordinate.parse("37XXN122YYW"));
    }

    // Partially valid but missing something or has extra
    @Test
    void testExtraCharactersOrMissing() {
        assertNull(Coordinate.parse("3700N12200W extra"));

        // Missing a direction
        assertNull(Coordinate.parse("3700N12200"));
    }

    // Extra with whitespace
    @Test
    void testExtraWithWhitespace() {
        assertNull(Coordinate.parse("37 45 N 122 W extra"));
    }

    // Wrap-around
    @Test
    void testWrapAround() {
        assertNull(Coordinate.parse("90 0.1 N 180 0.1 E"));
    }

    // Invalid miniutes
    @Test
    void testInvalidMinutes() {
        assertNull(Coordinate.parse("3760N12225W")); // Minute 60 in latitude
        assertNull(Coordinate.parse("3745N12260W")); // Minute 60 in longitude
    }

    // Degrees out of range
    @Test
    void testOutOfRangeDegrees() {
        assertNull(Coordinate.parse("95N122W")); // Latitude of 95
        assertNull(Coordinate.parse("37N190E")); // Longitude of 190
    }

}
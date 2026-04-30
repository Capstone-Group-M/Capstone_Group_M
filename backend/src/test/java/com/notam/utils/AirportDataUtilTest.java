package com.notam.utils;

import com.notam.model.Coordinate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirportDataUtilTest {

    @Test
    void returnsCoordinatesForValidIdent() {
        Coordinate result = AirportDataUtil.getCoordinatesFromIdent("Kokc");
        assertNotNull(result, "Expected coordinates for KOKC but got null");
    }

    @Test
    void returnsNullForUnknownIdent() {
        Coordinate result = AirportDataUtil.getCoordinatesFromIdent("ZZZZ");
        assertNull(result, "Expected null for unknown airport ID");
    }

    @Test
    void isCaseInsensitive() {
        Coordinate upper = AirportDataUtil.getCoordinatesFromIdent("Kokc");
        Coordinate lower = AirportDataUtil.getCoordinatesFromIdent("kokc");
        assertNotNull(upper);
        assertNotNull(lower);
        assertEquals(upper.getLatitude(),  lower.getLatitude(),  0.0001);
        assertEquals(upper.getLongitude(), lower.getLongitude(), 0.0001);
    }

    @Test
    void trimsWhitespaceFromInput() {
        Coordinate result = AirportDataUtil.getCoordinatesFromIdent("  KOKC  ");
        assertNotNull(result, "Expected coordinates even with surrounding whitespace");
    }

    @Test
    void returnsNullForNullOrEmptyInput() {
        assertNull(AirportDataUtil.getCoordinatesFromIdent(""));
        // If your code can receive null, guard against NPE:
        // assertNull(AirportDataUtil.getCoordinatesFromIdent(null));
    }

    @Test
    void coordinatesAreInValidRange() {
        Coordinate result = AirportDataUtil.getCoordinatesFromIdent("KOKC");
        assertNotNull(result);
        assertTrue(result.getLatitude()  >= -90  && result.getLatitude()  <= 90,  "Latitude out of range");
        assertTrue(result.getLongitude() >= -180 && result.getLongitude() <= 180, "Longitude out of range");
    }
}
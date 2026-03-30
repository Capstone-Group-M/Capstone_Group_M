package com.notam.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

class NOTAMTest {

    // ---- Constructor tests ----

    @Test
    void defaultConstructorSetsAllFieldsToNull() {
        NOTAM notam = new NOTAM();

        assertNull(notam.getId());
        assertNull(notam.getNumber());
        assertNull(notam.getSeries());
        assertNull(notam.getType());
        assertNull(notam.getAccountId());
        assertNull(notam.getIcaoLocation());
        assertNull(notam.getIssued());
        assertNull(notam.getEffectiveStart());
        assertNull(notam.getEffectiveEnd());
        assertNull(notam.getLastUpdated());
        assertNull(notam.getLocation());
        assertNull(notam.getMinimumFL());
        assertNull(notam.getMaximumFL());
        assertNull(notam.getCoordinates());
        assertNull(notam.getClassification());
        assertNull(notam.getTraffic());
        assertNull(notam.getPurpose());
        assertNull(notam.getScope());
        assertNull(notam.getSelectionCode());
        assertNull(notam.getText());
        assertNull(notam.getTranslations());
    }

    @Test
    void partialConstructorSetsIdNumberLocation() {
        NOTAM notam = new NOTAM("id-123", "A0001/25", "KJFK");

        assertEquals("id-123", notam.getId());
        assertEquals("A0001/25", notam.getNumber());
        assertEquals("KJFK", notam.getLocation());
        assertNull(notam.getSeries());
        assertNull(notam.getType());
        assertNull(notam.getIcaoLocation());
    }

    @Test
    void fullConstructorSetsAllFields() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime later = now.plusHours(6);

        NOTAM.NotamTranslation translation = new NOTAM.NotamTranslation();
        translation.setType("LOCAL");
        translation.setSimpleText("Runway closed");
        translation.setFormattedText("<b>Runway closed</b>");

        NOTAM notam = new NOTAM(
            "id-456", "A0002/25", "INTL", "N", "acct-1", "KLAX",
            now, now, later, now,
            "KLAX", "000", "999", "33.9425N/118.4081W",
            "DOM", "IFR", "NB", "AE", "QMRLC",
            "RWY 25L CLSD", List.of(translation)
        );

        assertEquals("id-456", notam.getId());
        assertEquals("A0002/25", notam.getNumber());
        assertEquals("INTL", notam.getSeries());
        assertEquals("N", notam.getType());
        assertEquals("acct-1", notam.getAccountId());
        assertEquals("KLAX", notam.getIcaoLocation());
        assertEquals(now, notam.getIssued());
        assertEquals(now, notam.getEffectiveStart());
        assertEquals(later, notam.getEffectiveEnd());
        assertEquals(now, notam.getLastUpdated());
        assertEquals("KLAX", notam.getLocation());
        assertEquals("000", notam.getMinimumFL());
        assertEquals("999", notam.getMaximumFL());
        assertEquals("33.9425N/118.4081W", notam.getCoordinates());
        assertEquals("DOM", notam.getClassification());
        assertEquals("IFR", notam.getTraffic());
        assertEquals("NB", notam.getPurpose());
        assertEquals("AE", notam.getScope());
        assertEquals("QMRLC", notam.getSelectionCode());
        assertEquals("RWY 25L CLSD", notam.getText());
        assertEquals(1, notam.getTranslations().size());
    }

    // ---- Getter/Setter tests ----

    @Test
    void setAndGetId() {
        NOTAM notam = new NOTAM();
        notam.setId("test-id");
        assertEquals("test-id", notam.getId());
    }

    @Test
    void setAndGetNumber() {
        NOTAM notam = new NOTAM();
        notam.setNumber("A1234/25");
        assertEquals("A1234/25", notam.getNumber());
    }

    @Test
    void setAndGetSeries() {
        NOTAM notam = new NOTAM();
        notam.setSeries("FDC");
        assertEquals("FDC", notam.getSeries());
    }

    @Test
    void setAndGetType() {
        NOTAM notam = new NOTAM();
        notam.setType("N");
        assertEquals("N", notam.getType());
    }

    @Test
    void setAndGetAccountId() {
        NOTAM notam = new NOTAM();
        notam.setAccountId("acct-99");
        assertEquals("acct-99", notam.getAccountId());
    }

    @Test
    void setAndGetIcaoLocation() {
        NOTAM notam = new NOTAM();
        notam.setIcaoLocation("KJFK");
        assertEquals("KJFK", notam.getIcaoLocation());
    }

    @Test
    void setAndGetIssued() {
        NOTAM notam = new NOTAM();
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));
        notam.setIssued(time);
        assertEquals(time, notam.getIssued());
    }

    @Test
    void setAndGetEffectiveStart() {
        NOTAM notam = new NOTAM();
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));
        notam.setEffectiveStart(time);
        assertEquals(time, notam.getEffectiveStart());
    }

    @Test
    void setAndGetEffectiveEnd() {
        NOTAM notam = new NOTAM();
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));
        notam.setEffectiveEnd(time);
        assertEquals(time, notam.getEffectiveEnd());
    }

    @Test
    void setAndGetLastUpdated() {
        NOTAM notam = new NOTAM();
        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("UTC"));
        notam.setLastUpdated(time);
        assertEquals(time, notam.getLastUpdated());
    }

    @Test
    void setAndGetLocation() {
        NOTAM notam = new NOTAM();
        notam.setLocation("KORD");
        assertEquals("KORD", notam.getLocation());
    }

    @Test
    void setAndGetMinimumFL() {
        NOTAM notam = new NOTAM();
        notam.setMinimumFL("000");
        assertEquals("000", notam.getMinimumFL());
    }

    @Test
    void setAndGetMaximumFL() {
        NOTAM notam = new NOTAM();
        notam.setMaximumFL("999");
        assertEquals("999", notam.getMaximumFL());
    }

    @Test
    void setAndGetCoordinates() {
        NOTAM notam = new NOTAM();
        notam.setCoordinates("40.6413N/73.7781W");
        assertEquals("40.6413N/73.7781W", notam.getCoordinates());
    }

    @Test
    void setAndGetClassification() {
        NOTAM notam = new NOTAM();
        notam.setClassification("INTL");
        assertEquals("INTL", notam.getClassification());
    }

    @Test
    void setAndGetTraffic() {
        NOTAM notam = new NOTAM();
        notam.setTraffic("VFR");
        assertEquals("VFR", notam.getTraffic());
    }

    @Test
    void setAndGetPurpose() {
        NOTAM notam = new NOTAM();
        notam.setPurpose("BO");
        assertEquals("BO", notam.getPurpose());
    }

    @Test
    void setAndGetScope() {
        NOTAM notam = new NOTAM();
        notam.setScope("W");
        assertEquals("W", notam.getScope());
    }

    @Test
    void setAndGetSelectionCode() {
        NOTAM notam = new NOTAM();
        notam.setSelectionCode("QMRLC");
        assertEquals("QMRLC", notam.getSelectionCode());
    }

    @Test
    void setAndGetText() {
        NOTAM notam = new NOTAM();
        notam.setText("TWY A CLSD");
        assertEquals("TWY A CLSD", notam.getText());
    }

    @Test
    void setAndGetTranslations() {
        NOTAM notam = new NOTAM();
        NOTAM.NotamTranslation t1 = new NOTAM.NotamTranslation();
        t1.setSimpleText("Taxiway closed");
        NOTAM.NotamTranslation t2 = new NOTAM.NotamTranslation();
        t2.setSimpleText("Runway shortened");

        notam.setTranslations(List.of(t1, t2));

        assertEquals(2, notam.getTranslations().size());
        assertEquals("Taxiway closed", notam.getTranslations().get(0).getSimpleText());
        assertEquals("Runway shortened", notam.getTranslations().get(1).getSimpleText());
    }

    // ---- NotamTranslation inner class tests ----

    @Test
    void translationSetAndGetType() {
        NOTAM.NotamTranslation t = new NOTAM.NotamTranslation();
        t.setType("LOCAL");
        assertEquals("LOCAL", t.getType());
    }

    @Test
    void translationSetAndGetSimpleText() {
        NOTAM.NotamTranslation t = new NOTAM.NotamTranslation();
        t.setSimpleText("Runway 25L closed");
        assertEquals("Runway 25L closed", t.getSimpleText());
    }

    @Test
    void translationSetAndGetFormattedText() {
        NOTAM.NotamTranslation t = new NOTAM.NotamTranslation();
        t.setFormattedText("<p>Runway 25L closed</p>");
        assertEquals("<p>Runway 25L closed</p>", t.getFormattedText());
    }

    @Test
    void translationDefaultValuesAreNull() {
        NOTAM.NotamTranslation t = new NOTAM.NotamTranslation();
        assertNull(t.getType());
        assertNull(t.getSimpleText());
        assertNull(t.getFormattedText());
    }

    // ---- Edge case tests ----

    @Test
    void settersAcceptNullValues() {
        NOTAM notam = new NOTAM();
        notam.setId("something");
        notam.setId(null);
        assertNull(notam.getId());
    }

    @Test
    void settersOverwritePreviousValues() {
        NOTAM notam = new NOTAM();
        notam.setIcaoLocation("KJFK");
        notam.setIcaoLocation("KLAX");
        assertEquals("KLAX", notam.getIcaoLocation());
    }

    @Test
    void emptyStringIsNotNull() {
        NOTAM notam = new NOTAM();
        notam.setText("");
        assertNotNull(notam.getText());
        assertEquals("", notam.getText());
    }
}

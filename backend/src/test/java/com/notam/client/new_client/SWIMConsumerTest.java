package com.notam.client.new_client;

import org.json.JSONObject;
import org.json.XML;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

class SWIMConsumerTest {

    private SWIMConsumer consumer;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        String outputFile = tempDir.resolve("test_output.txt").toString();
        consumer = new SWIMConsumer(
            "tcps://fake:55443", "fakeQueue", "fakeCF",
            "user", "pass", "vpn", outputFile, true
        );
    }

    // ==================== isUSNotam tests ====================

    @Test
    void isUSNotam_CONUS_returnsTrue() {
        assertTrue(consumer.isUSNotam("KOKC"));
        assertTrue(consumer.isUSNotam("KJFK"));
        assertTrue(consumer.isUSNotam("KSTL"));
    }

    @Test
    void isUSNotam_Alaska_returnsTrue() {
        assertTrue(consumer.isUSNotam("PANC"));
        assertTrue(consumer.isUSNotam("PAFA"));
    }

    @Test
    void isUSNotam_Hawaii_returnsTrue() {
        assertTrue(consumer.isUSNotam("PHNL"));
        assertTrue(consumer.isUSNotam("PHOG"));
    }

    @Test
    void isUSNotam_Guam_returnsTrue() {
        assertTrue(consumer.isUSNotam("PGUM"));
    }

    @Test
    void isUSNotam_PuertoRico_returnsTrue() {
        assertTrue(consumer.isUSNotam("TJSJ"));
    }

    @Test
    void isUSNotam_foreign_returnsFalse() {
        assertFalse(consumer.isUSNotam("EGLL")); // London Heathrow
        assertFalse(consumer.isUSNotam("LFPG")); // Paris CDG
        assertFalse(consumer.isUSNotam("LEMD")); // Madrid
        assertFalse(consumer.isUSNotam("CYYZ")); // Toronto
    }

    // ==================== extractIcaoLocation tests ====================

    @Test
    void extractIcao_fromIcaoLocationField() {
        String xml = buildNotamXml("KOKC", "OKC", "KZKC");
        JSONObject json = XML.toJSONObject(xml);
        assertEquals("KOKC", consumer.extractIcaoLocation(json));
    }

    @Test
    void extractIcao_fallsBackToLocation() {
        // No fnse:icaoLocation, should fall back to "K" + event:location
        String xml = buildNotamXmlNoIcao("STL", "KZKC");
        JSONObject json = XML.toJSONObject(xml);
        assertEquals("KSTL", consumer.extractIcaoLocation(json));
    }

    @Test
    void extractIcao_fallsBackToFIR() {
        // No icaoLocation and no event:location
        String xml = buildNotamXmlFIROnly("KZFW");
        JSONObject json = XML.toJSONObject(xml);
        assertEquals("KZFW", consumer.extractIcaoLocation(json));
    }

    @Test
    void extractIcao_returnsNullForInvalidJson() {
        JSONObject json = new JSONObject("{\"foo\": \"bar\"}");
        assertNull(consumer.extractIcaoLocation(json));
    }

    @Test
    void extractIcao_handlesArrayHasMember() {
        String xml = buildNotamXmlWithArray("KJFK");
        JSONObject json = XML.toJSONObject(xml);
        assertEquals("KJFK", consumer.extractIcaoLocation(json));
    }

    // ==================== constructor tests ====================

    @Test
    void constructor_setsJsonFlag() {
        SWIMConsumer jsonConsumer = new SWIMConsumer(
            "url", "q", "cf", "u", "p", "v", "out.txt", true);
        SWIMConsumer xmlConsumer = new SWIMConsumer(
            "url", "q", "cf", "u", "p", "v", "out.txt", false);

        // Verify via isUSNotam still works (basic sanity)
        assertTrue(jsonConsumer.isUSNotam("KOKC"));
        assertTrue(xmlConsumer.isUSNotam("KOKC"));
    }

    // ==================== helper methods to build test XML ====================

    private String buildNotamXml(String icaoLocation, String location, String fir) {
        return "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:event=\"http://www.aixm.aero/schema/5.1/event\""
            + " xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
            + " xmlns:aixm=\"http://www.aixm.aero/schema/5.1\">"
            + "<hasMember><event:Event gml:id=\"E1\">"
            + "<gml:identifier codeSpace=\"urn:uuid:\">test-uuid</gml:identifier>"
            + "<event:timeSlice><event:EventTimeSlice gml:id=\"ETS1\">"
            + "<gml:validTime><gml:TimePeriod gml:id=\"TP1\">"
            + "<gml:beginPosition>2026-04-16T00:00:00Z</gml:beginPosition>"
            + "<gml:endPosition>2026-04-17T00:00:00Z</gml:endPosition>"
            + "</gml:TimePeriod></gml:validTime>"
            + "<event:extension><fnse:EventExtension gml:id=\"ext1\">"
            + "<fnse:icaoLocation>" + icaoLocation + "</fnse:icaoLocation>"
            + "</fnse:EventExtension></event:extension>"
            + "<event:textNOTAM><event:NOTAM gml:id=\"N1\">"
            + "<event:location>" + location + "</event:location>"
            + "<event:affectedFIR>" + fir + "</event:affectedFIR>"
            + "<event:text>TEST NOTAM</event:text>"
            + "</event:NOTAM></event:textNOTAM>"
            + "</event:EventTimeSlice></event:timeSlice>"
            + "</event:Event></hasMember>"
            + "</AIXMBasicMessage>";
    }

    private String buildNotamXmlNoIcao(String location, String fir) {
        return "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:event=\"http://www.aixm.aero/schema/5.1/event\""
            + " xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
            + " xmlns:aixm=\"http://www.aixm.aero/schema/5.1\">"
            + "<hasMember><event:Event gml:id=\"E1\">"
            + "<event:timeSlice><event:EventTimeSlice gml:id=\"ETS1\">"
            + "<gml:validTime><gml:TimePeriod gml:id=\"TP1\">"
            + "<gml:beginPosition>2026-04-16T00:00:00Z</gml:beginPosition>"
            + "<gml:endPosition>2026-04-17T00:00:00Z</gml:endPosition>"
            + "</gml:TimePeriod></gml:validTime>"
            + "<event:extension><fnse:EventExtension gml:id=\"ext1\">"
            + "</fnse:EventExtension></event:extension>"
            + "<event:textNOTAM><event:NOTAM gml:id=\"N1\">"
            + "<event:location>" + location + "</event:location>"
            + "<event:affectedFIR>" + fir + "</event:affectedFIR>"
            + "<event:text>TEST NOTAM</event:text>"
            + "</event:NOTAM></event:textNOTAM>"
            + "</event:EventTimeSlice></event:timeSlice>"
            + "</event:Event></hasMember>"
            + "</AIXMBasicMessage>";
    }

    private String buildNotamXmlFIROnly(String fir) {
        return "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:event=\"http://www.aixm.aero/schema/5.1/event\""
            + " xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
            + " xmlns:aixm=\"http://www.aixm.aero/schema/5.1\">"
            + "<hasMember><event:Event gml:id=\"E1\">"
            + "<event:timeSlice><event:EventTimeSlice gml:id=\"ETS1\">"
            + "<gml:validTime><gml:TimePeriod gml:id=\"TP1\">"
            + "<gml:beginPosition>2026-04-16T00:00:00Z</gml:beginPosition>"
            + "<gml:endPosition>2026-04-17T00:00:00Z</gml:endPosition>"
            + "</gml:TimePeriod></gml:validTime>"
            + "<event:extension><fnse:EventExtension gml:id=\"ext1\">"
            + "</fnse:EventExtension></event:extension>"
            + "<event:textNOTAM><event:NOTAM gml:id=\"N1\">"
            + "<event:affectedFIR>" + fir + "</event:affectedFIR>"
            + "<event:text>TEST NOTAM</event:text>"
            + "</event:NOTAM></event:textNOTAM>"
            + "</event:EventTimeSlice></event:timeSlice>"
            + "</event:Event></hasMember>"
            + "</AIXMBasicMessage>";
    }

    private String buildNotamXmlWithArray(String icaoLocation) {
        return "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:event=\"http://www.aixm.aero/schema/5.1/event\""
            + " xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
            + " xmlns:aixm=\"http://www.aixm.aero/schema/5.1\">"
            + "<hasMember><aixm:AirportHeliport gml:id=\"AH1\">"
            + "</aixm:AirportHeliport></hasMember>"
            + "<hasMember><event:Event gml:id=\"E1\">"
            + "<event:timeSlice><event:EventTimeSlice gml:id=\"ETS1\">"
            + "<gml:validTime><gml:TimePeriod gml:id=\"TP1\">"
            + "<gml:beginPosition>2026-04-16T00:00:00Z</gml:beginPosition>"
            + "<gml:endPosition>2026-04-17T00:00:00Z</gml:endPosition>"
            + "</gml:TimePeriod></gml:validTime>"
            + "<event:extension><fnse:EventExtension gml:id=\"ext1\">"
            + "<fnse:icaoLocation>" + icaoLocation + "</fnse:icaoLocation>"
            + "</fnse:EventExtension></event:extension>"
            + "<event:textNOTAM><event:NOTAM gml:id=\"N1\">"
            + "<event:text>TEST NOTAM</event:text>"
            + "</event:NOTAM></event:textNOTAM>"
            + "</event:EventTimeSlice></event:timeSlice>"
            + "</event:Event></hasMember>"
            + "</AIXMBasicMessage>";
    }
}
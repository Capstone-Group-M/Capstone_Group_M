package com.notam.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;
import com.fasterxml.jackson.databind.JsonNode;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class NotamParserTest {

    private NotamParser parser;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        parser = new NotamParser(mapper);
    }

    // ========== parseDate tests ==========

    @Test
    void parseDate_validDate_returnsZonedDateTime() {
        String dateStr = "2025-08-19T17:47:00.000Z";
        ZonedDateTime result = parser.parseDate(dateStr);
        assertNotNull(result);
        assertEquals(2025, result.getYear());
        assertEquals(8, result.getMonthValue());
        assertEquals(19, result.getDayOfMonth());
    }

    @Test
    void parseDate_perm_returnsNull() {
        assertNull(parser.parseDate("PERM"));
    }

    @Test
    void parseDate_ufn_returnsNull() {
        assertNull(parser.parseDate("UFN"));
    }

    @Test
    void parseDate_null_returnsNull() {
        assertNull(parser.parseDate(null));
    }

    @Test
    void parseDate_garbage_returnsNull() {
        assertNull(parser.parseDate("not-a-date"));
    }

    // ========== traverseTreeToItems tests ==========

    @Test
    void traverseTreeToItems_validResponse_returnsItems() {
        String json = """
            {
                "items": [
                    {"properties": {"coreNOTAMData": {"notam": {"id": "1"}}}}
                ]
            }
            """;

        HttpResponse<String> response = mockResponse(200, json);
        JsonNode items = parser.traverseTreeToItems(response);

        assertTrue(items.isArray());
        assertEquals(1, items.size());
    }

    @Test
    void traverseTreeToItems_emptyItems_returnsEmptyArray() {
        String json = """
            {"items": []}
            """;

        HttpResponse<String> response = mockResponse(200, json);
        JsonNode items = parser.traverseTreeToItems(response);

        assertTrue(items.isArray());
        assertEquals(0, items.size());
    }

    @Test
    void traverseTreeToItems_badStatusCode_throws() {
        HttpResponse<String> response = mockResponse(500, "Server Error");

        assertThrows(IllegalStateException.class, () -> {
            parser.traverseTreeToItems(response);
        });
    }

    @Test
    void traverseTreeToItems_invalidJson_throws() {
        HttpResponse<String> response = mockResponse(200, "not json{{{");

        assertThrows(IllegalStateException.class, () -> {
            parser.traverseTreeToItems(response);
        });
    }

    // ========== JsonNotamToNotamObject tests ==========

    @Test
    void jsonNotamToNotamObject_allFields_populated() throws Exception {
        String json = """
            {
                "id": "NOTAM-001",
                "number": "A0001/25",
                "series": "A",
                "type": "N",
                "icaoLocation": "KJFK",
                "location": "JFK",
                "text": "RWY 04L/22R CLSD",
                "effectiveStart": "2025-06-01T00:00:00.000Z",
                "effectiveEnd": "PERM"
            }
            """;

        JsonNode node = mapper.readTree(json);
        NOTAM notam = parser.JsonNotamToNotamObject(node);

        assertEquals("NOTAM-001", notam.getId());
        assertEquals("A0001/25", notam.getNumber());
        assertEquals("A", notam.getSeries());
        assertEquals("KJFK", notam.getIcaoLocation());
        assertEquals("RWY 04L/22R CLSD", notam.getText());
        assertNotNull(notam.getEffectiveStart());
        assertNull(notam.getEffectiveEnd()); // "PERM" -> null
    }

    @Test
    void jsonNotamToNotamObject_missingFields_defaultsToNull() throws Exception {
        String json = "{}";
        JsonNode node = mapper.readTree(json);
        NOTAM notam = parser.JsonNotamToNotamObject(node);

        assertNull(notam.getId());
        assertNull(notam.getText());
        assertNull(notam.getIcaoLocation());
    }

    @Test
    void notamStatusTag_beforeEffectiveStart_returnsInactive() throws Exception {
        String json = """
            {
                "id": "NOTAM-002",
                "effectiveStart": "2026-03-20T12:00:00Z"
            }
            """;

        JsonNode node = mapper.readTree(json);
        NOTAM notam = parser.JsonNotamToNotamObject(node);

        assertEquals(
            "INACTIVE",
            notam.getStatusTag(ZonedDateTime.parse("2026-03-20T11:59:00Z"))
        );
    }

    @Test
    void notamStatusTag_onOrAfterEffectiveStart_returnsActive() throws Exception {
        String json = """
            {
                "id": "NOTAM-003",
                "effectiveStart": "2026-03-20T12:00:00Z"
            }
            """;

        JsonNode node = mapper.readTree(json);
        NOTAM notam = parser.JsonNotamToNotamObject(node);

        assertEquals(
            "ACTIVE",
            notam.getStatusTag(ZonedDateTime.parse("2026-03-20T12:00:00Z"))
        );
        assertEquals(
            "ACTIVE",
            notam.getStatusTag(ZonedDateTime.parse("2026-03-20T12:01:00Z"))
        );
    }

    // ========== parsePage integration test ==========

    @Test
    void parsePage_fullResponse_parsesList() {
        String json = """
            {
                "items": [
                    {
                        "properties": {
                            "coreNOTAMData": {
                                "notam": {
                                    "id": "N-1",
                                    "icaoLocation": "KLAX",
                                    "text": "TWY A CLSD"
                                }
                            }
                        }
                    },
                    {
                        "properties": {
                            "coreNOTAMData": {
                                "notam": {
                                    "id": "N-2",
                                    "icaoLocation": "KSFO",
                                    "text": "RWY 28R CLSD"
                                }
                            }
                        }
                    }
                ]
            }
            """;

        HttpResponse<String> response = mockResponse(200, json);
        List<NOTAM> notams = parser.parsePage(response);

        assertEquals(2, notams.size());
        assertEquals("KLAX", notams.get(0).getIcaoLocation());
        assertEquals("KSFO", notams.get(1).getIcaoLocation());
    }

    // ========== Helper ==========
    // Replace the mockResponse method with this:
    private HttpResponse<String> mockResponse(int statusCode, String body) {
        return new HttpResponse<>() {
            @Override public int statusCode() { return statusCode; }
            @Override public String body() { return body; }

            // Required by the interface but not used in our tests
            @Override public HttpHeaders headers() { return HttpHeaders.of(java.util.Map.of(), (a, b) -> true); }
            @Override public java.util.Optional<HttpResponse<String>> previousResponse() { return java.util.Optional.empty(); }
            @Override public java.net.http.HttpRequest request() { return null; }
            @Override public java.util.Optional<javax.net.ssl.SSLSession> sslSession() { return java.util.Optional.empty(); }
            @Override public java.net.URI uri() { return null; }
            @Override public java.net.http.HttpClient.Version version() { return null; }
        };
    }
    // ========== parseSWIMNotam tests ==========

    @Test
    void parseSWIMNotam_validNotam_parsesAllFields() {
        String xml = "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:event=\"http://www.aixm.aero/schema/5.1/event\""
            + " xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
            + " xmlns:aixm=\"http://www.aixm.aero/schema/5.1\">"
            + "<hasMember><event:Event gml:id=\"E1\">"
            + "<gml:identifier codeSpace=\"urn:uuid:\">test-uuid-123</gml:identifier>"
            + "<event:timeSlice><event:EventTimeSlice gml:id=\"ETS1\">"
            + "<gml:validTime><gml:TimePeriod gml:id=\"TP1\">"
            + "<gml:beginPosition>2026-04-16T02:00:00.000Z</gml:beginPosition>"
            + "<gml:endPosition>2026-04-16T02:30:00.000Z</gml:endPosition>"
            + "</gml:TimePeriod></gml:validTime>"
            + "<event:extension><fnse:EventExtension gml:id=\"ext1\">"
            + "<fnse:accountId>KLFI</fnse:accountId>"
            + "<fnse:icaoLocation>KLFI</fnse:icaoLocation>"
            + "<fnse:classification>MIL</fnse:classification>"
            + "<fnse:lastUpdated>2026-04-16T01:50:00.000Z</fnse:lastUpdated>"
            + "</fnse:EventExtension></event:extension>"
            + "<event:textNOTAM><event:NOTAM gml:id=\"N1\">"
            + "<event:series>M</event:series>"
            + "<event:number>370</event:number>"
            + "<event:type>N</event:type>"
            + "<event:location>LFI</event:location>"
            + "<event:text>AERODROME CLOSED</event:text>"
            + "<event:minimumFL>000</event:minimumFL>"
            + "<event:maximumFL>999</event:maximumFL>"
            + "<event:coordinates>3704N07621W</event:coordinates>"
            + "<event:traffic>IV</event:traffic>"
            + "<event:purpose>NBO</event:purpose>"
            + "<event:scope>A</event:scope>"
            + "<event:selectionCode>QXXXX</event:selectionCode>"
            + "<event:issued>2026-04-16T01:50:00.000Z</event:issued>"
            + "</event:NOTAM></event:textNOTAM>"
            + "</event:EventTimeSlice></event:timeSlice>"
            + "</event:Event></hasMember>"
            + "</AIXMBasicMessage>";

        JSONObject json = org.json.XML.toJSONObject(xml);
        NOTAM notam = parser.parseSWIMNotam(json);

        assertNotNull(notam);
        assertEquals("test-uuid-123", notam.getId());
        assertEquals("370", notam.getNumber());
        assertEquals("M", notam.getSeries());
        assertEquals("N", notam.getType());
        assertEquals("KLFI", notam.getAccountId());
        assertEquals("KLFI", notam.getIcaoLocation());
        assertEquals("LFI", notam.getLocation());
        assertEquals("AERODROME CLOSED", notam.getText());
        assertEquals("000", notam.getMinimumFL());
        assertEquals("999", notam.getMaximumFL());
        assertEquals("3704N07621W", notam.getCoordinates());
        assertEquals("MIL", notam.getClassification());
        assertEquals("IV", notam.getTraffic());
        assertEquals("NBO", notam.getPurpose());
        assertEquals("A", notam.getScope());
        assertEquals("QXXXX", notam.getSelectionCode());
        assertNotNull(notam.getIssued());
        assertNotNull(notam.getEffectiveStart());
        assertNotNull(notam.getEffectiveEnd());
        assertNotNull(notam.getLastUpdated());
    }

    @Test
    void parseSWIMNotam_arrayHasMember_findsEvent() {
        String xml = "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:event=\"http://www.aixm.aero/schema/5.1/event\""
            + " xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
            + " xmlns:aixm=\"http://www.aixm.aero/schema/5.1\">"
            + "<hasMember><aixm:AirportHeliport gml:id=\"AH1\">"
            + "</aixm:AirportHeliport></hasMember>"
            + "<hasMember><event:Event gml:id=\"E1\">"
            + "<gml:identifier codeSpace=\"urn:uuid:\">array-uuid</gml:identifier>"
            + "<event:timeSlice><event:EventTimeSlice gml:id=\"ETS1\">"
            + "<gml:validTime><gml:TimePeriod gml:id=\"TP1\">"
            + "<gml:beginPosition>2026-04-16T00:00:00.000Z</gml:beginPosition>"
            + "<gml:endPosition>2026-04-17T00:00:00.000Z</gml:endPosition>"
            + "</gml:TimePeriod></gml:validTime>"
            + "<event:extension><fnse:EventExtension gml:id=\"ext1\">"
            + "<fnse:accountId>KGMJ</fnse:accountId>"
            + "<fnse:icaoLocation>KGMJ</fnse:icaoLocation>"
            + "<fnse:classification>DOM</fnse:classification>"
            + "<fnse:lastUpdated>2026-04-16T01:50:00.000Z</fnse:lastUpdated>"
            + "</fnse:EventExtension></event:extension>"
            + "<event:textNOTAM><event:NOTAM gml:id=\"N1\">"
            + "<event:text>OBST TOWER LGT U/S</event:text>"
            + "<event:location>GMJ</event:location>"
            + "<event:issued>2026-04-14T23:57:00.000Z</event:issued>"
            + "</event:NOTAM></event:textNOTAM>"
            + "</event:EventTimeSlice></event:timeSlice>"
            + "</event:Event></hasMember>"
            + "</AIXMBasicMessage>";

        JSONObject json = org.json.XML.toJSONObject(xml);
        NOTAM notam = parser.parseSWIMNotam(json);

        assertNotNull(notam);
        assertEquals("array-uuid", notam.getId());
        assertEquals("KGMJ", notam.getIcaoLocation());
        assertEquals("OBST TOWER LGT U/S", notam.getText());
    }

    @Test
    void parseSWIMNotam_invalidJson_returnsNull() {
        JSONObject json = new JSONObject("{\"foo\": \"bar\"}");
        NOTAM notam = parser.parseSWIMNotam(json);
        assertNull(notam);
    }

    @Test
    void parseSWIMNotam_noEventInArray_returnsNull() {
        String xml = "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:aixm=\"http://www.aixm.aero/schema/5.1\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\">"
            + "<hasMember><aixm:AirportHeliport gml:id=\"AH1\">"
            + "</aixm:AirportHeliport></hasMember>"
            + "<hasMember><aixm:AirportHeliport gml:id=\"AH2\">"
            + "</aixm:AirportHeliport></hasMember>"
            + "</AIXMBasicMessage>";

        JSONObject json = org.json.XML.toJSONObject(xml);
        NOTAM notam = parser.parseSWIMNotam(json);
        assertNull(notam);
    }

    @Test
    void parseSWIMNotam_endDateWithESTSuffix_stripsAndParses() {
        String xml = "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:event=\"http://www.aixm.aero/schema/5.1/event\""
            + " xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
            + " xmlns:aixm=\"http://www.aixm.aero/schema/5.1\">"
            + "<hasMember><event:Event gml:id=\"E1\">"
            + "<gml:identifier codeSpace=\"urn:uuid:\">est-uuid</gml:identifier>"
            + "<event:timeSlice><event:EventTimeSlice gml:id=\"ETS1\">"
            + "<gml:validTime><gml:TimePeriod gml:id=\"TP1\">"
            + "<gml:beginPosition>2026-04-14T23:54:00.000Z</gml:beginPosition>"
            + "<gml:endPosition>2026-05-14T17:00:00.000Z</gml:endPosition>"
            + "</gml:TimePeriod></gml:validTime>"
            + "<event:extension><fnse:EventExtension gml:id=\"ext1\">"
            + "<fnse:accountId>TEST</fnse:accountId>"
            + "<fnse:icaoLocation>KTEST</fnse:icaoLocation>"
            + "<fnse:classification>DOM</fnse:classification>"
            + "<fnse:lastUpdated>2026-04-16T00:00:00.000Z</fnse:lastUpdated>"
            + "</fnse:EventExtension></event:extension>"
            + "<event:textNOTAM><event:NOTAM gml:id=\"N1\">"
            + "<event:text>TEST</event:text>"
            + "<event:issued>2026-04-14T23:57:00.000Z</event:issued>"
            + "</event:NOTAM></event:textNOTAM>"
            + "</event:EventTimeSlice></event:timeSlice>"
            + "</event:Event></hasMember>"
            + "</AIXMBasicMessage>";

        JSONObject json = org.json.XML.toJSONObject(xml);
        NOTAM notam = parser.parseSWIMNotam(json);

        assertNotNull(notam);
        assertNotNull(notam.getEffectiveEnd());
        assertEquals(2026, notam.getEffectiveEnd().getYear());
        assertEquals(5, notam.getEffectiveEnd().getMonthValue());
    }

    @Test
    void parseSWIMNotam_missingOptionalFields_stillParses() {
        String xml = "<?xml version=\"1.0\"?>"
            + "<AIXMBasicMessage xmlns:event=\"http://www.aixm.aero/schema/5.1/event\""
            + " xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\""
            + " xmlns:gml=\"http://www.opengis.net/gml/3.2\""
            + " xmlns:aixm=\"http://www.aixm.aero/schema/5.1\">"
            + "<hasMember><event:Event gml:id=\"E1\">"
            + "<gml:identifier codeSpace=\"urn:uuid:\">minimal-uuid</gml:identifier>"
            + "<event:timeSlice><event:EventTimeSlice gml:id=\"ETS1\">"
            + "<gml:validTime><gml:TimePeriod gml:id=\"TP1\">"
            + "<gml:beginPosition>2026-04-16T00:00:00.000Z</gml:beginPosition>"
            + "<gml:endPosition>2026-04-17T00:00:00.000Z</gml:endPosition>"
            + "</gml:TimePeriod></gml:validTime>"
            + "<event:extension><fnse:EventExtension gml:id=\"ext1\">"
            + "<fnse:icaoLocation>KOKC</fnse:icaoLocation>"
            + "</fnse:EventExtension></event:extension>"
            + "<event:textNOTAM><event:NOTAM gml:id=\"N1\">"
            + "<event:text>MINIMAL NOTAM</event:text>"
            + "</event:NOTAM></event:textNOTAM>"
            + "</event:EventTimeSlice></event:timeSlice>"
            + "</event:Event></hasMember>"
            + "</AIXMBasicMessage>";

        JSONObject json = org.json.XML.toJSONObject(xml);
        NOTAM notam = parser.parseSWIMNotam(json);

        assertNotNull(notam);
        assertEquals("KOKC", notam.getIcaoLocation());
        assertEquals("MINIMAL NOTAM", notam.getText());
        assertNull(notam.getSeries());
        assertNull(notam.getTraffic());
        assertNull(notam.getPurpose());
        assertNull(notam.getScope());
    }
}
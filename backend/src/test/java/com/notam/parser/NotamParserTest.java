package com.notam.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notam.model.NOTAM;
import com.fasterxml.jackson.databind.JsonNode;

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
}
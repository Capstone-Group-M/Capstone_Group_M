package com.notam.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.notam.model.NOTAM;

@SuppressWarnings("unchecked")
class FAAClientTest {

    private HttpClient mockClient;
    private FAAClient faaClient;

    @BeforeEach
    void setUp() {
        mockClient = mock(HttpClient.class);
        faaClient = new FAAClient("testId", "testSecret", mockClient);
    }

    private HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    private String singleNotamJson() {
        return """
            {
                "pageSize": 1,
                "pageNum": 1,
                "totalPages": 1,
                "totalNotamCount": 1,
                "items": [{
                    "notam": {
                        "id": "NOTAM-001",
                        "text": "TEST NOTAM",
                        "effectiveStart": "2026-03-01T00:00:00Z",
                        "effectiveEnd": "2026-04-01T00:00:00Z",
                        "coordinates": "35.2226N 097.4395W"
                    }
                }]
            }
            """;
    }

    private String emptyJson() {
        return """
            {
                "pageSize": 0,
                "pageNum": 1,
                "totalPages": 1,
                "totalNotamCount": 0,
                "items": []
            }
            """;
    }

    @Test
    void fetchAllNotams_nullLocation_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> faaClient.fetchAllNotams(null));
    }

    @Test
    void fetchAllNotams_blankLocation_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
            () -> faaClient.fetchAllNotams("   "));
    }

    @Test
    void fetchAllNotams_singlePage_returnsNotams() throws Exception {
        // Create responses FIRST, outside the stubbing chain
        HttpResponse<String> page1 = mockResponse(200, singleNotamJson());
        HttpResponse<String> empty = mockResponse(200, emptyJson());

        doReturn(page1).doReturn(empty)
            .when(mockClient).send(any(), any());

        List<NOTAM> result = faaClient.fetchAllNotams("KOKC");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void fetchAllNotams_multiplePages_aggregatesResults() throws Exception {
        HttpResponse<String> page1 = mockResponse(200, singleNotamJson());
        HttpResponse<String> page2 = mockResponse(200, singleNotamJson());
        HttpResponse<String> empty = mockResponse(200, emptyJson());

        doReturn(page1).doReturn(page2).doReturn(empty)
            .when(mockClient).send(any(), any());

        List<NOTAM> result = faaClient.fetchAllNotams("KOKC");

        assertEquals(2, result.size());
    }

    @Test
    void fetchAllNotams_serverError_throwsRuntimeException() throws Exception {
        doReturn(mockResponse(500, "Internal Server Error"))
            .when(mockClient).send(any(), any());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> faaClient.fetchAllNotams("KOKC"));

        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    void fetchAllNotams_networkFailure_throwsAfterRetries() throws Exception {
        doThrow(new IOException("Connection refused"))
            .when(mockClient).send(any(), any());

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> faaClient.fetchAllNotams("KOKC"));

        assertTrue(ex.getMessage().contains("unreachable"));
    }

    @Test
    void fetchAllNotams_rateLimited_retriesAndSucceeds() throws Exception {
        HttpResponse<String> rateLimited = mockResponse(429, "");
        HttpResponse<String> page1 = mockResponse(200, singleNotamJson());
        HttpResponse<String> empty = mockResponse(200, emptyJson());

        doReturn(rateLimited).doReturn(page1).doReturn(empty)
            .when(mockClient).send(any(), any());

        List<NOTAM> result = faaClient.fetchAllNotams("KOKC");

        assertFalse(result.isEmpty());
        verify(mockClient, times(3)).send(any(), any());
    }

}
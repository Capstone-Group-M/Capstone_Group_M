package com.notam.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.notam.model.NOTAM;

@SuppressWarnings("unchecked")
class FAAClientTest {

    private HttpClient mockClient;
    private FAAOldClient faaClient;

    @BeforeEach
    void setUp() {
        mockClient = mock(HttpClient.class);
        faaClient = new FAAOldClient("testId", "testSecret", mockClient);
    }

    private HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }

    private void setupResponseChain(HttpResponse<String>... responses) throws Exception {
        if (responses.length == 0) {
            throw new IllegalArgumentException("Must provide at least one response");
        }

        final int[] index = {0};

        when(mockClient.<String>send(any(HttpRequest.class), any(BodyHandler.class)))
                .thenAnswer(invocation -> {
                    if (index[0] < responses.length) {
                        return responses[index[0]++];
                    }
                    return responses[responses.length - 1];
                });
    }

    private String singleNotamJson() {
        return """
            {
              "items": [
                {
                  "icaoId": "KOKC",
                  "notamId": "A1234/26",
                  "issueDate": "2026-03-01T00:00:00Z",
                  "effectiveStartDate": "2026-03-01T00:00:00Z",
                  "effectiveEndDate": "2026-03-02T00:00:00Z",
                  "classification": "DOMESTIC",
                  "accountId": "TEST",
                  "location": "KOKC",
                  "facilityName": "TEST AIRPORT",
                  "type": "N",
                  "keyword": "RWY",
                  "number": "A1234/26",
                  "text": "RWY CLOSED"
                }
              ]
            }
            """;
    }

    private String emptyJson() {
        return """
            {
              "items": []
            }
            """;
    }

    @Test
    void fetchAllNotams_nullLocation_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> faaClient.fetchAllNotams(null));
    }

    @Test
    void fetchAllNotams_blankLocation_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> faaClient.fetchAllNotams("   "));
    }

    @Test
    void fetchAllNotams_singlePage_returnsNotams() throws Exception {
        setupResponseChain(
                mockResponse(200, singleNotamJson()),
                mockResponse(200, emptyJson())
        );

        List<NOTAM> result = faaClient.fetchAllNotams("KOKC");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void fetchAllNotams_multiplePages_aggregatesResults() throws Exception {
        setupResponseChain(
                mockResponse(200, singleNotamJson()),
                mockResponse(200, singleNotamJson()),
                mockResponse(200, emptyJson())
        );

        List<NOTAM> result = faaClient.fetchAllNotams("KOKC");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void fetchAllNotams_serverError_throwsRuntimeException() throws Exception {
        setupResponseChain(mockResponse(500, "Internal Server Error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> faaClient.fetchAllNotams("KOKC"));

        assertTrue(ex.getMessage().contains("FAA API error code: 500"));
    }

    @Test
    void fetchAllNotams_networkFailure_throwsAfterRetries() throws Exception {
        doThrow(new IOException("Connection refused"))
                .when(mockClient)
                .<String>send(any(HttpRequest.class), any(BodyHandler.class));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> faaClient.fetchAllNotams("KOKC"));

        assertTrue(ex.getMessage().contains("FAA API unreachable"));
    }

    @Test
    void fetchAllNotams_rateLimited_retriesAndSucceeds() throws Exception {
        setupResponseChain(
                mockResponse(429, ""),
                mockResponse(200, singleNotamJson()),
                mockResponse(200, emptyJson())
        );

        List<NOTAM> result = faaClient.fetchAllNotams("KOKC");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockClient, times(3)).<String>send(any(HttpRequest.class), any(BodyHandler.class));
    }
}
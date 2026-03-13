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
import org.mockito.stubbing.OngoingStubbing;

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

    @SafeVarargs
    private void setupResponseChain(HttpResponse<String>... responses) throws Exception {
        if (responses.length == 0) {
            throw new IllegalArgumentException("Must have 1 or more responses to mock/stub");
        }
        OngoingStubbing<HttpResponse<String>> chain = doReturn(responses[0])
            .when(mockClient).send(any(), any());
        for (int i = 1; i < responses.length; i++) {
            chain = chain.thenReturn(responses[i]);
        }
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
        setupResponseChain(
            mockResponse(200, singleNotamJson()),
            mockResponse(200, emptyJson())
        );

        List<NOTAM> result = faaClient.fetchAllNotams("KOKC");

        assertFalse(result.isEmpty());
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

        assertEquals(2, result.size());
    }

    @Test
    void fetchAllNotams_serverError_throwsRuntimeException() throws Exception {
        setupResponseChain(
            mockResponse(500, "Internal Server Error")
        );

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
        setupResponseChain(
            mockResponse(429, ""),
            mockResponse(200, singleNotamJson()),
            mockResponse(200, emptyJson())
        );

        List<NOTAM> result = faaClient.fetchAllNotams("KOKC");

        assertFalse(result.isEmpty());
        verify(mockClient, times(3)).send(any(), any());
    }

}
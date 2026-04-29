package com.notam.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.notam.model.Coordinate;
import com.notam.model.NOTAM;

public class CGIApiClientTest {

    @Mock
    private HttpClient mockClient;

    @Mock
    private HttpResponse<String> mockAuthResponse;

    @Mock
    private HttpResponse<String> mockNotamResponse;

    @Mock
    private HttpResponse<String> mockResponse;

    private CGIApiClient cgiClient;

    private static final String AUTH_RESPONSE = "{\"access_token\":\"test-token\"}";
    private static final String NOTAM_RESPONSE = """
            {
              "status": "Success",
              "data": {
                "geojson": [
                  {
                    "type": "Feature",
                    "properties": {
                      "coreNOTAMData": {
                        "notam": {
                          "id": "NMS_ID_123",
                          "number": "A0001/26",
                          "series": "A",
                          "type": "N",
                          "accountId": "KOKC",
                          "icaoLocation": "KOKC",
                          "issued": "2026-04-01T00:00:00Z",
                          "effectiveStart": "2026-04-01T00:00:00Z",
                          "effectiveEnd": "2026-05-01T00:00:00Z",
                          "text": "RWY 17L CLSD"
                        }
                      }
                    }
                  }
                ]
              }
            }
            """;
    private static final String EMPTY_RESPONSE = "{\"status\":\"Success\",\"data\":{\"geojson\":[]}}";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        cgiClient = new CGIApiClient("test-id", "test-secret", mockClient);

        when(mockAuthResponse.statusCode()).thenReturn(200);
        when(mockAuthResponse.body()).thenReturn(AUTH_RESPONSE);

        when(mockNotamResponse.statusCode()).thenReturn(200);
        when(mockNotamResponse.body()).thenReturn(NOTAM_RESPONSE);
    }

    // --- fetchAllNotams input validation ---

    @Test
    void testFetchAllNotams_nullLocation_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cgiClient.fetchAllNotams(null));
    }

    @Test
    void testFetchAllNotams_blankLocation_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> cgiClient.fetchAllNotams("   "));
    }

    // --- fetchAllNotams success ---

    @Test
    @SuppressWarnings("unchecked")
    void testFetchAllNotams_returnsNotams() throws Exception {
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockAuthResponse)
                .thenReturn(mockNotamResponse)
                .thenAnswer(inv -> {
                    HttpResponse<String> emptyResponse = mock(HttpResponse.class);
                    when(emptyResponse.statusCode()).thenReturn(200);
                    when(emptyResponse.body()).thenReturn(EMPTY_RESPONSE);
                    return emptyResponse;
                });

        List<NOTAM> notams = cgiClient.fetchAllNotams("KOKC");
        assertFalse(notams.isEmpty());
        assertEquals("NMS_ID_123", notams.get(0).getId());
        assertEquals("RWY 17L CLSD", notams.get(0).getText());
    }

    // --- authentication ---

    @Test
    @SuppressWarnings("unchecked")
    void testAuthenticationFailure_throwsException() throws Exception {
        when(mockAuthResponse.statusCode()).thenReturn(401);
        when(mockAuthResponse.body()).thenReturn("Unauthorized");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockAuthResponse);

        assertThrows(RuntimeException.class, () -> cgiClient.fetchAllNotams("KOKC"));
    }

    // --- error handling ---

    @Test
    @SuppressWarnings("unchecked")
    void testApiError_throwsException() throws Exception {
        HttpResponse<String> errorResponse = mock(HttpResponse.class);
        when(errorResponse.statusCode()).thenReturn(500);
        when(errorResponse.body()).thenReturn("Internal Server Error");

        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockAuthResponse)
                .thenReturn(errorResponse);

        assertThrows(RuntimeException.class, () -> cgiClient.fetchAllNotams("KOKC"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNetworkFailure_throwsAfterRetries() throws Exception {
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockAuthResponse)
                .thenThrow(new java.io.IOException("Network error"))
                .thenThrow(new java.io.IOException("Network error"))
                .thenThrow(new java.io.IOException("Network error"));

        assertThrows(RuntimeException.class, () -> cgiClient.fetchAllNotams("KOKC"));
    }

    // --- fetchNotamsByCoordinates input validation ---

    @Test
    void throwsOnNullCoordinate() {
        assertThrows(IllegalArgumentException.class,
                () -> cgiClient.fetchNotamsByCoordinates(null, 15));
    }

    @Test
    void throwsOnNegativeRadius() {
        assertThrows(IllegalArgumentException.class,
                () -> cgiClient.fetchNotamsByCoordinates(new Coordinate(35.39, -97.59), -1));
    }

    @Test
    void throwsOnRadiusOver100() {
        assertThrows(IllegalArgumentException.class,
                () -> cgiClient.fetchNotamsByCoordinates(new Coordinate(35.39, -97.59), 101));
    }

    // --- fetchNotamsByCoordinates success ---

    @Test
    @SuppressWarnings("unchecked")
    void throwsOnHttpError() throws Exception {
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Bad Request");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockAuthResponse)
                .thenReturn(mockResponse);

        assertThrows(RuntimeException.class,
                () -> cgiClient.fetchNotamsByCoordinates(new Coordinate(35.39, -97.59), 15));
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsEmptyListOnEmptyResponse() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(EMPTY_RESPONSE);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockAuthResponse)
                .thenReturn(mockResponse);

        List<NOTAM> result = cgiClient.fetchNotamsByCoordinates(new Coordinate(35.39, -97.59), 15);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void acceptsBoundaryRadiusZero() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(EMPTY_RESPONSE);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockAuthResponse)
                .thenReturn(mockResponse);

        assertDoesNotThrow(() -> cgiClient.fetchNotamsByCoordinates(new Coordinate(35.39, -97.59), 0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void acceptsBoundaryRadius100() throws Exception {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(EMPTY_RESPONSE);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockAuthResponse)
                .thenReturn(mockResponse);

        assertDoesNotThrow(() -> cgiClient.fetchNotamsByCoordinates(new Coordinate(35.39, -97.59), 100));
    }
}
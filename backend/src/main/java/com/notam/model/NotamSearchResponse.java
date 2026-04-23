package com.notam.model;

import java.util.List;

public class NotamSearchResponse {
    private final String departure;
    private final String destination;
    private final int count;
    private final List<NotamDto> notams;

    public NotamSearchResponse(String departure, String destination, List<NotamDto> notams) {
        this.departure = departure;
        this.destination = destination;
        this.notams = notams;
        this.count = notams.size();
    }

    public String getDeparture() {
        return departure;
    }

    public String getDestination() {
        return destination;
    }

    public int getCount() {
        return count;
    }

    public List<NotamDto> getNotams() {
        return notams;
    }
}

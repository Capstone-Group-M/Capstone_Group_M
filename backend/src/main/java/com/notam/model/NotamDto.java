package com.notam.model;

public class NotamDto {
    private final String id;
    private final String location;
    private final String effectiveStart;
    private final String effectiveEnd;
    private final String classification;
    private final String description;

    public NotamDto(
            String id,
            String location,
            String effectiveStart,
            String effectiveEnd,
            String classification,
            String description) {
        this.id = id;
        this.location = location;
        this.effectiveStart = effectiveStart;
        this.effectiveEnd = effectiveEnd;
        this.classification = classification;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public String getEffectiveStart() {
        return effectiveStart;
    }

    public String getEffectiveEnd() {
        return effectiveEnd;
    }

    public String getClassification() {
        return classification;
    }

    public String getDescription() {
        return description;
    }
}

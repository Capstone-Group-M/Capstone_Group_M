// Package groups all data classes
package com.notam.model;

// For handling date
import java.time.ZonedDateTime;
import java.util.List; // List of translations

public class NOTAM {

    // Basic identification of NOTAM 
    private String id;
    private String number;
    private String series;
    private String type;
    private String accountId;
    private String icaoLocation;

    // Times of NOTAM
    private ZonedDateTime issued;
    private ZonedDateTime effectiveStart;
    private ZonedDateTime effectiveEnd;
    private ZonedDateTime lastUpdated;

    // Location and altitude info
    private String location;
    private String minimumFL;
    private String maximumFL;
    private String coordinates;

    // Classification & purpose
    private String classification;
    private String traffic;
    private String purpose;
    private String scope;
    private String selectionCode;

    // Text fields
    private String text;
    private List<NotamTranslation> translations;

    // Full constructor with all fields
    public NOTAM(String id, String number, String series, String type, String accountId, String icaoLocation, ZonedDateTime issued, ZonedDateTime effectiveStart,
             ZonedDateTime effectiveEnd, ZonedDateTime lastUpdated, String location, String minimumFL, String maximumFL, String coordinates,
             String classification, String traffic, String purpose, String scope, String selectionCode, String text, List<NotamTranslation> translations) {

        this.id = id;
        this.number = number;
        this.series = series;
        this.type = type;
        this.accountId = accountId;
        this.icaoLocation = icaoLocation;
        this.issued = issued;
        this.effectiveStart = effectiveStart;
        this.effectiveEnd = effectiveEnd;
        this.lastUpdated = lastUpdated;
        this.location = location;
        this.minimumFL = minimumFL;
        this.maximumFL = maximumFL;
        this.coordinates = coordinates;
        this.classification = classification;
        this.traffic = traffic;
        this.purpose = purpose;
        this.scope = scope;
        this.selectionCode = selectionCode;
        this.text = text;
        this.translations = translations;
    }

    // Constructor for id, number, and location
    public NOTAM(String id, String number, String location) {
        this.id = id;
        this.number = number;
        this.location = location;
    }

    // Default constructor 
    public NOTAM() {}

    // Read and write for NOTAM fields 
    // ID
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // Number
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    // Series
    public String getSeries() { return series; }
    public void setSeries(String series) { this.series = series; }

    // Type
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    // AccountID
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    // ICAO Location
    public String getIcaoLocation() { return icaoLocation; }
    public void setIcaoLocation(String icaoLocation) { this.icaoLocation = icaoLocation; }

    // Issued Time
    public ZonedDateTime getIssued() { return issued; }
    public void setIssued(ZonedDateTime issued) { this.issued = issued; }

    // Effective Start
    public ZonedDateTime getEffectiveStart() { return effectiveStart; }
    public void setEffectiveStart(ZonedDateTime effectiveStart) { this.effectiveStart = effectiveStart; }

    // Effective End
    public ZonedDateTime getEffectiveEnd() { return effectiveEnd; }
    public void setEffectiveEnd(ZonedDateTime effectiveEnd) { this.effectiveEnd = effectiveEnd; }

    // Last updated
    public ZonedDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(ZonedDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getMinimumFL() { return minimumFL; }
    public void setMinimumFL(String minimumFL) { this.minimumFL = minimumFL; }

    public String getMaximumFL() { return maximumFL; }
    public void setMaximumFL(String maximumFL) { this.maximumFL = maximumFL; }

    public String getCoordinates() { return coordinates; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getTraffic() { return traffic; }
    public void setTraffic(String traffic) { this.traffic = traffic; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getSelectionCode() { return selectionCode; }
    public void setSelectionCode(String selectionCode) { this.selectionCode = selectionCode; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<NotamTranslation> getTranslations() { return translations; }
    public void setTranslations(List<NotamTranslation> translations) { this.translations = translations; }

    // Inner class for the translations (listed)
    public static class NotamTranslation {

        // Currently three types of translations
        private String type;
        private String simpleText;
        private String formattedText;

        // Type
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        // Simple text
        public String getSimpleText() { return simpleText; }
        public void setSimpleText(String simpleText) { this.simpleText = simpleText; }

        // Formatted text
        public String getFormattedText() { return formattedText; }
        public void setFormattedText(String formattedText) { this.formattedText = formattedText; }
    }
}

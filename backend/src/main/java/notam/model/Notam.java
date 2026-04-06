package notam.model;

public class Notam {

    private String id;
    private String location;
    private String effectiveStart;
    private String effectiveEnd;
    private String classification;
    private String description;

    public Notam(String id, String location,
                 String effectiveStart, String effectiveEnd,
                 String classification, String description) {

        this.id = id;
        this.location = location;
        this.effectiveStart = effectiveStart;
        this.effectiveEnd = effectiveEnd;
        this.classification = classification;
        this.description = description;
    }

    public String getId() { return id; }
    public String getLocation() { return location; }
    public String getEffectiveStart() { return effectiveStart; }
    public String getEffectiveEnd() { return effectiveEnd; }
    public String getClassification() { return classification; }
    public String getDescription() { return description; }
}
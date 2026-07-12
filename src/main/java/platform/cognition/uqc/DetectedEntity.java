package platform.cognition.uqc;

import java.util.Objects;

/**
 * Immutable entity detected by the Universal Query Classifier.
 * Represents a single extracted entity from user input.
 * Thread-safe by design.
 */
public final class DetectedEntity {

    public enum EntityType {
        COURSE,
        LANGUAGE,
        FRAMEWORK,
        TOPIC,
        CONCEPT,
        COMMAND,
        ACTION,
        PERSON,
        PLACE,
        TECHNOLOGY,
        UNKNOWN
    }

    private final EntityType type;
    private final String value;
    private final String normalized;
    private final double confidence;

    public DetectedEntity(EntityType type, String value, String normalized, double confidence) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.value = Objects.requireNonNull(value, "value must not be null");
        this.normalized = normalized != null ? normalized : value.toLowerCase().trim();
        this.confidence = Math.max(0.0, Math.min(1.0, confidence));
    }

    public DetectedEntity(EntityType type, String value) {
        this(type, value, value.toLowerCase().trim(), 1.0);
    }

    public EntityType getType() { return type; }
    public String getValue() { return value; }
    public String getNormalized() { return normalized; }
    public double getConfidence() { return confidence; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetectedEntity that)) return false;
        return normalized.equals(that.normalized) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, normalized);
    }

    @Override
    public String toString() {
        return "Entity{" + type + "='" + value + "', conf=" + String.format("%.2f", confidence) + "}";
    }
}
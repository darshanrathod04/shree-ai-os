package com.shreeai.os.platform.runtime.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>EvidenceItem</b>
 *
 * <p>Single structured fact produced by a kernel and attested by
 * {@code EvidenceAgent}. Each item is independent and carries its source,
 * type, value, citations, and confidence hint.</p>
 *
 * @since Sprint 18
 */
public final class EvidenceItem {

    /**
     * The origin kernel that produced this evidence.
     */
    public enum SourceType {
        KNOWLEDGE,
        REASONING,
        INFERENCE,
        PLANNING,
        MEMORY,
        REFLECTION,
        PROJECT,
        EXECUTION
    }

    private final String itemId;
    private final SourceType sourceType;
    private final String title;
    private final String content;
    private final List<String> citations;
    private final double confidenceHint;
    private final Map<String, Object> attributes;
    private final long producedAtMillis;

    private EvidenceItem(Builder b) {
        this.itemId = Objects.requireNonNull(b.itemId, "itemId must not be null");
        this.sourceType = Objects.requireNonNull(b.sourceType, "sourceType must not be null");
        this.title = Objects.requireNonNull(b.title, "title must not be null");
        this.content = Objects.requireNonNull(b.content, "content must not be null");
        this.citations = List.copyOf(b.citations);
        this.confidenceHint = clamp(b.confidenceHint);
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(b.attributes));
        this.producedAtMillis = b.producedAtMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String itemId() { return itemId; }
    public SourceType sourceType() { return sourceType; }
    public String title() { return title; }
    public String content() { return content; }
    public List<String> citations() { return citations; }
    public double confidenceHint() { return confidenceHint; }
    public Map<String, Object> attributes() { return attributes; }
    public long producedAtMillis() { return producedAtMillis; }

    private static double clamp(double v) {
        if (Double.isNaN(v)) return 0.0;
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvidenceItem that)) return false;
        return Double.compare(that.confidenceHint, confidenceHint) == 0
                && producedAtMillis == that.producedAtMillis
                && Objects.equals(itemId, that.itemId)
                && sourceType == that.sourceType
                && Objects.equals(title, that.title)
                && Objects.equals(content, that.content)
                && Objects.equals(citations, that.citations)
                && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, sourceType, title, content, citations, confidenceHint, attributes, producedAtMillis);
    }

    @Override
    public String toString() {
        return "EvidenceItem{itemId='" + itemId
                + "', sourceType=" + sourceType
                + ", title='" + title + '\''
                + ", confidenceHint=" + confidenceHint + '}';
    }

    public static final class Builder {
        private String itemId = "evi-" + java.util.UUID.randomUUID();
        private SourceType sourceType;
        private String title = "";
        private String content = "";
        private List<String> citations = List.of();
        private double confidenceHint = 0.0;
        private Map<String, Object> attributes = new LinkedHashMap<>();
        private long producedAtMillis = System.currentTimeMillis();

        public Builder itemId(String itemId) {
            this.itemId = itemId;
            return this;
        }

        public Builder sourceType(SourceType sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder citations(List<String> citations) {
            this.citations = new ArrayList<>(citations);
            return this;
        }

        public Builder addCitation(String citation) {
            if (!(this.citations instanceof ArrayList)) {
                this.citations = new ArrayList<>(this.citations);
            }
            this.citations.add(citation);
            return this;
        }

        public Builder confidenceHint(double confidenceHint) {
            this.confidenceHint = confidenceHint;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = new LinkedHashMap<>(attributes);
            return this;
        }

        public Builder addAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder producedAtMillis(long producedAtMillis) {
            this.producedAtMillis = producedAtMillis;
            return this;
        }

        public EvidenceItem build() {
            return new EvidenceItem(this);
        }
    }
}

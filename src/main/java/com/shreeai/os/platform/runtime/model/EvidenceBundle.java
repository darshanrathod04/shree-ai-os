package com.shreeai.os.platform.runtime.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>EvidenceBundle</b>
 *
 * <p>Immutable structured-facts collection produced by {@code EvidenceAgent}.
 * Each {@link EvidenceItem} is a kernel-attested fact, not a markdown string.
 * The bundle is the authoritative input for {@code VerificationAgent}.</p>
 *
 * <p><b>Architectural Responsibility (Sprint 18):</b></p>
 * <ul>
 *   <li>Encapsulates per-kernel evidence items.</li>
 *   <li>Preserves source kernel identity for verification.</li>
 *   <li>Preserves citations (for knowledge / project evidence).</li>
 *   <li>Preserves confidence hints from the producing kernel.</li>
 * </ul>
 *
 * @since Sprint 18
 */
public final class EvidenceBundle {

    private final String bundleId;
    private final List<EvidenceItem> items;
    private final Map<String, Object> bundleMetadata;
    private final long extractedAtMillis;

    private EvidenceBundle(Builder b) {
        this.bundleId = Objects.requireNonNull(b.bundleId, "bundleId must not be null");
        this.items = List.copyOf(b.items);
        this.bundleMetadata = Collections.unmodifiableMap(new LinkedHashMap<>(b.bundleMetadata));
        this.extractedAtMillis = b.extractedAtMillis;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String bundleId() { return bundleId; }
    public List<EvidenceItem> items() { return items; }
    public Map<String, Object> bundleMetadata() { return bundleMetadata; }
    public long extractedAtMillis() { return extractedAtMillis; }

    public boolean isEmpty() { return items.isEmpty(); }
    public int size() { return items.size(); }

    /**
     * @return first item of the requested type, or null.
     */
    public EvidenceItem firstOfType(EvidenceItem.SourceType type) {
        for (EvidenceItem item : items) {
            if (item.sourceType() == type) {
                return item;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EvidenceBundle that)) return false;
        return extractedAtMillis == that.extractedAtMillis
                && Objects.equals(bundleId, that.bundleId)
                && Objects.equals(items, that.items)
                && Objects.equals(bundleMetadata, that.bundleMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bundleId, items, bundleMetadata, extractedAtMillis);
    }

    @Override
    public String toString() {
        return "EvidenceBundle{bundleId='" + bundleId
                + "', itemCount=" + items.size() + '}';
    }

    public static final class Builder {
        private String bundleId = "bundle-" + java.util.UUID.randomUUID();
        private List<EvidenceItem> items = new ArrayList<>();
        private Map<String, Object> bundleMetadata = new LinkedHashMap<>();
        private long extractedAtMillis = System.currentTimeMillis();

        public Builder bundleId(String bundleId) {
            this.bundleId = bundleId;
            return this;
        }

        public Builder items(List<EvidenceItem> items) {
            this.items = new ArrayList<>(items);
            return this;
        }

        public Builder addItem(EvidenceItem item) {
            this.items.add(item);
            return this;
        }

        public Builder bundleMetadata(Map<String, Object> bundleMetadata) {
            this.bundleMetadata = new LinkedHashMap<>(bundleMetadata);
            return this;
        }

        public Builder addMetadata(String key, Object value) {
            this.bundleMetadata.put(key, value);
            return this;
        }

        public Builder extractedAtMillis(long extractedAtMillis) {
            this.extractedAtMillis = extractedAtMillis;
            return this;
        }

        public EvidenceBundle build() {
            return new EvidenceBundle(this);
        }
    }
}

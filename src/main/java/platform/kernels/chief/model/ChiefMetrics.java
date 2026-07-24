package platform.kernels.chief.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefMetrics</b>
 *
 * <p>Represents immutable orchestration metrics.
 * This value object encapsulates metrics for chief-level monitoring.</p>
 */
public final class ChiefMetrics {

    private final ChiefId chiefId;
    private final int activeOrchestrations;
    private final int completedOrchestrations;
    private final int failedOrchestrations;
    private final int activeDelegations;
    private final Instant measuredAt;
    private final Map<String, Object> metadata;

    public ChiefMetrics(
            ChiefId chiefId,
            int activeOrchestrations,
            int completedOrchestrations,
            int failedOrchestrations,
            int activeDelegations,
            Instant measuredAt,
            Map<String, Object> metadata) {
        if (chiefId == null) throw new IllegalArgumentException("ChiefMetrics chiefId must not be null");
        if (measuredAt == null) throw new IllegalArgumentException("ChiefMetrics measuredAt must not be null");
        if (metadata == null) throw new IllegalArgumentException("ChiefMetrics metadata must not be null");

        this.chiefId = chiefId;
        this.activeOrchestrations = activeOrchestrations;
        this.completedOrchestrations = completedOrchestrations;
        this.failedOrchestrations = failedOrchestrations;
        this.activeDelegations = activeDelegations;
        this.measuredAt = measuredAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ChiefId chiefId() { return chiefId; }
    public int activeOrchestrations() { return activeOrchestrations; }
    public int completedOrchestrations() { return completedOrchestrations; }
    public int failedOrchestrations() { return failedOrchestrations; }
    public int activeDelegations() { return activeDelegations; }
    public Instant measuredAt() { return measuredAt; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiefMetrics that = (ChiefMetrics) obj;
        return Objects.equals(chiefId, that.chiefId);
    }

    @Override
    public int hashCode() { return Objects.hash(chiefId); }

    @Override
    public String toString() {
        return "ChiefMetrics{chiefId=" + chiefId + ", active=" + activeOrchestrations + '}';
    }
}
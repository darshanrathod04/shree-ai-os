package com.shreeai.os.platform.kernels.chief.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DecisionResult</b>
 *
 * <p>Represents an immutable strategic decision.
 * This value object encapsulates the outcome of decision coordination.</p>
 */
public final class DecisionResult {

    private final ChiefId chiefId;
    private final boolean approved;
    private final String coordinationPath;
    private final List<String> selectedKernels;
    private final Instant decidedAt;
    private final Map<String, Object> metadata;

    public DecisionResult(
            ChiefId chiefId,
            boolean approved,
            String coordinationPath,
            List<String> selectedKernels,
            Instant decidedAt,
            Map<String, Object> metadata) {
        if (chiefId == null) throw new IllegalArgumentException("DecisionResult chiefId must not be null");
        if (coordinationPath == null || coordinationPath.trim().isEmpty())
            throw new IllegalArgumentException("DecisionResult coordinationPath must not be null or empty");
        if (selectedKernels == null) throw new IllegalArgumentException("DecisionResult selectedKernels must not be null");
        if (decidedAt == null) throw new IllegalArgumentException("DecisionResult decidedAt must not be null");
        if (metadata == null) throw new IllegalArgumentException("DecisionResult metadata must not be null");

        this.chiefId = chiefId;
        this.approved = approved;
        this.coordinationPath = coordinationPath;
        this.selectedKernels = List.copyOf(selectedKernels);
        this.decidedAt = decidedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ChiefId chiefId() { return chiefId; }
    public boolean approved() { return approved; }
    public String coordinationPath() { return coordinationPath; }
    public List<String> selectedKernels() { return selectedKernels; }
    public Instant decidedAt() { return decidedAt; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DecisionResult that = (DecisionResult) obj;
        return Objects.equals(chiefId, that.chiefId);
    }

    @Override
    public int hashCode() { return Objects.hash(chiefId); }

    @Override
    public String toString() {
        return "DecisionResult{chiefId=" + chiefId + ", approved=" + approved + "}";
    }
}
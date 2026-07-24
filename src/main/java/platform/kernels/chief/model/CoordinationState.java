package platform.kernels.chief.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>CoordinationState</b>
 *
 * <p>Represents immutable orchestration state.
 * This value object encapsulates the coordination state across kernels.</p>
 */
public final class CoordinationState {

    private final ChiefId chiefId;
    private final String coordinationStage;
    private final List<String> participatingKernels;
    private final String orchestrationLifecycle;
    private final Map<String, Object> metadata;

    public CoordinationState(
            ChiefId chiefId,
            String coordinationStage,
            List<String> participatingKernels,
            String orchestrationLifecycle,
            Map<String, Object> metadata) {
        if (chiefId == null) throw new IllegalArgumentException("CoordinationState chiefId must not be null");
        if (coordinationStage == null || coordinationStage.trim().isEmpty())
            throw new IllegalArgumentException("CoordinationState coordinationStage must not be null or empty");
        if (participatingKernels == null) throw new IllegalArgumentException("CoordinationState participatingKernels must not be null");
        if (metadata == null) throw new IllegalArgumentException("CoordinationState metadata must not be null");

        this.chiefId = chiefId;
        this.coordinationStage = coordinationStage;
        this.participatingKernels = List.copyOf(participatingKernels);
        this.orchestrationLifecycle = orchestrationLifecycle;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ChiefId chiefId() { return chiefId; }
    public String coordinationStage() { return coordinationStage; }
    public List<String> participatingKernels() { return participatingKernels; }
    public String orchestrationLifecycle() { return orchestrationLifecycle; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CoordinationState that = (CoordinationState) obj;
        return Objects.equals(chiefId, that.chiefId);
    }

    @Override
    public int hashCode() { return Objects.hash(chiefId); }

    @Override
    public String toString() {
        return "CoordinationState{chiefId=" + chiefId + ", stage='" + coordinationStage + "'}";
    }
}
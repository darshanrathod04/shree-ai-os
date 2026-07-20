package platform.kernels.chief.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DecisionContext</b>
 *
 * <p>Represents the immutable context used for strategic coordination.
 * This value object encapsulates the parameters for decision coordination.</p>
 */
public final class DecisionContext {

    private final ChiefId chiefId;
    private final String decisionType;
    private final List<String> participatingKernels;
    private final String orchestrationScope;
    private final Map<String, Object> contextualData;
    private final Map<String, Object> metadata;

    public DecisionContext(
            ChiefId chiefId,
            String decisionType,
            List<String> participatingKernels,
            String orchestrationScope,
            Map<String, Object> contextualData,
            Map<String, Object> metadata) {
        if (chiefId == null) throw new IllegalArgumentException("DecisionContext chiefId must not be null");
        if (decisionType == null || decisionType.trim().isEmpty())
            throw new IllegalArgumentException("DecisionContext decisionType must not be null or empty");
        if (participatingKernels == null) throw new IllegalArgumentException("DecisionContext participatingKernels must not be null");
        if (contextualData == null) throw new IllegalArgumentException("DecisionContext contextualData must not be null");
        if (metadata == null) throw new IllegalArgumentException("DecisionContext metadata must not be null");

        this.chiefId = chiefId;
        this.decisionType = decisionType;
        this.participatingKernels = List.copyOf(participatingKernels);
        this.orchestrationScope = orchestrationScope;
        this.contextualData = Collections.unmodifiableMap(new HashMap<>(contextualData));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ChiefId chiefId() { return chiefId; }
    public String decisionType() { return decisionType; }
    public List<String> participatingKernels() { return participatingKernels; }
    public String orchestrationScope() { return orchestrationScope; }
    public Map<String, Object> contextualData() { return contextualData; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DecisionContext that = (DecisionContext) obj;
        return Objects.equals(chiefId, that.chiefId) && Objects.equals(decisionType, that.decisionType);
    }

    @Override
    public int hashCode() { return Objects.hash(chiefId, decisionType); }

    @Override
    public String toString() {
        return "DecisionContext{chiefId=" + chiefId + ", decisionType='" + decisionType + "'}";
    }
}
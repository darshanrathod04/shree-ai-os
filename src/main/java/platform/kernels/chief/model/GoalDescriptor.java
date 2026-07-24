package platform.kernels.chief.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>GoalDescriptor</b>
 *
 * <p>Represents immutable goal metadata.
 * This value object encapsulates the parameters for goal lifecycle management.</p>
 */
public final class GoalDescriptor {

    private final ChiefId chiefId;
    private final String goalName;
    private final String lifecycleState;
    private final int priority;
    private final String planningReference;
    private final Map<String, Object> metadata;

    public GoalDescriptor(
            ChiefId chiefId,
            String goalName,
            String lifecycleState,
            int priority,
            String planningReference,
            Map<String, Object> metadata) {
        if (chiefId == null) throw new IllegalArgumentException("GoalDescriptor chiefId must not be null");
        if (goalName == null || goalName.trim().isEmpty())
            throw new IllegalArgumentException("GoalDescriptor goalName must not be null or empty");
        if (lifecycleState == null || lifecycleState.trim().isEmpty())
            throw new IllegalArgumentException("GoalDescriptor lifecycleState must not be null or empty");
        if (metadata == null) throw new IllegalArgumentException("GoalDescriptor metadata must not be null");

        this.chiefId = chiefId;
        this.goalName = goalName;
        this.lifecycleState = lifecycleState;
        this.priority = priority;
        this.planningReference = planningReference;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ChiefId chiefId() { return chiefId; }
    public String goalName() { return goalName; }
    public String lifecycleState() { return lifecycleState; }
    public int priority() { return priority; }
    public String planningReference() { return planningReference; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GoalDescriptor that = (GoalDescriptor) obj;
        return Objects.equals(chiefId, that.chiefId);
    }

    @Override
    public int hashCode() { return Objects.hash(chiefId); }

    @Override
    public String toString() {
        return "GoalDescriptor{chiefId=" + chiefId + ", goalName='" + goalName + "'}";
    }
}
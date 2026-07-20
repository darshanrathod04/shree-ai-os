package platform.kernels.chief.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DelegationResult</b>
 *
 * <p>Represents immutable delegation outcomes.
 * This value object encapsulates the result of delegating a task.</p>
 */
public final class DelegationResult {

    private final ChiefId chiefId;
    private final String taskId;
    private final String targetKernel;
    private final String delegationStatus;
    private final Instant delegatedAt;
    private final Map<String, Object> metadata;

    public DelegationResult(
            ChiefId chiefId,
            String taskId,
            String targetKernel,
            String delegationStatus,
            Instant delegatedAt,
            Map<String, Object> metadata) {
        if (chiefId == null) throw new IllegalArgumentException("DelegationResult chiefId must not be null");
        if (taskId == null || taskId.trim().isEmpty())
            throw new IllegalArgumentException("DelegationResult taskId must not be null or empty");
        if (targetKernel == null || targetKernel.trim().isEmpty())
            throw new IllegalArgumentException("DelegationResult targetKernel must not be null or empty");
        if (delegationStatus == null || delegationStatus.trim().isEmpty())
            throw new IllegalArgumentException("DelegationResult delegationStatus must not be null or empty");
        if (delegatedAt == null) throw new IllegalArgumentException("DelegationResult delegatedAt must not be null");
        if (metadata == null) throw new IllegalArgumentException("DelegationResult metadata must not be null");

        this.chiefId = chiefId;
        this.taskId = taskId;
        this.targetKernel = targetKernel;
        this.delegationStatus = delegationStatus;
        this.delegatedAt = delegatedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ChiefId chiefId() { return chiefId; }
    public String taskId() { return taskId; }
    public String targetKernel() { return targetKernel; }
    public String delegationStatus() { return delegationStatus; }
    public Instant delegatedAt() { return delegatedAt; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DelegationResult that = (DelegationResult) obj;
        return Objects.equals(chiefId, that.chiefId);
    }

    @Override
    public int hashCode() { return Objects.hash(chiefId); }

    @Override
    public String toString() {
        return "DelegationResult{chiefId=" + chiefId + ", targetKernel='" + targetKernel + "'}";
    }
}
package platform.kernels.chief.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefResponse</b>
 *
 * <p>Represents the immutable outcome of orchestration.
 * This value object encapsulates the result of an orchestration operation.</p>
 */
public final class ChiefResponse {

    private final ChiefId chiefId;
    private final boolean success;
    private final String message;
    private final DecisionResult decisionResult;
    private final DelegationResult delegationResult;
    private final CoordinationState coordinationState;
    private final Instant completedAt;
    private final Map<String, Object> metadata;

    public ChiefResponse(
            ChiefId chiefId,
            boolean success,
            String message,
            DecisionResult decisionResult,
            DelegationResult delegationResult,
            CoordinationState coordinationState,
            Instant completedAt,
            Map<String, Object> metadata) {
        if (chiefId == null) {
            throw new IllegalArgumentException("ChiefResponse chiefId must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("ChiefResponse message must not be null");
        }
        if (completedAt == null) {
            throw new IllegalArgumentException("ChiefResponse completedAt must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ChiefResponse metadata must not be null");
        }
        this.chiefId = chiefId;
        this.success = success;
        this.message = message;
        this.decisionResult = decisionResult;
        this.delegationResult = delegationResult;
        this.coordinationState = coordinationState;
        this.completedAt = completedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ChiefId chiefId() { return chiefId; }
    public boolean success() { return success; }
    public String message() { return message; }
    public DecisionResult decisionResult() { return decisionResult; }
    public DelegationResult delegationResult() { return delegationResult; }
    public CoordinationState coordinationState() { return coordinationState; }
    public Instant completedAt() { return completedAt; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiefResponse that = (ChiefResponse) obj;
        return success == that.success &&
                Objects.equals(chiefId, that.chiefId) &&
                Objects.equals(message, that.message) &&
                Objects.equals(decisionResult, that.decisionResult) &&
                Objects.equals(delegationResult, that.delegationResult) &&
                Objects.equals(coordinationState, that.coordinationState) &&
                Objects.equals(completedAt, that.completedAt) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chiefId, success, message, decisionResult, delegationResult, coordinationState, completedAt, metadata);
    }

    @Override
    public String toString() {
        return "ChiefResponse{chiefId=" + chiefId + ", success=" + success + '}';
    }
}
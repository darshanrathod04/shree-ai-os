package platform.kernels.chief.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefRequest</b>
 *
 * <p>Represents a strategic orchestration request.
 * This immutable value object encapsulates orchestration intent only.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates orchestration request parameters.</li>
 *   <li>Provides immutable orchestration context.</li>
 *   <li>Defines orchestration scope and intent.</li>
 *   <li>Contains no orchestration behavior.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value-based equality — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Domain Model</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-102, EIO-ARCH-001</p>
 *
 * @param chiefId    the orchestration identifier (must not be {@code null})
 * @param requestType the type of orchestration request (must not be {@code null} or empty)
 * @param context    the decision context (may be {@code null})
 * @param goal       the goal descriptor (may be {@code null})
 * @param payload    the request payload (must not be {@code null})
 * @param metadata   additional metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class ChiefRequest {

    private final ChiefId chiefId;
    private final String requestType;
    private final DecisionContext context;
    private final GoalDescriptor goal;
    private final Map<String, Object> payload;
    private final Map<String, Object> metadata;

    /**
     * Constructs a {@code ChiefRequest} with the specified parameters.
     *
     * @param chiefId    the orchestration identifier (must not be {@code null})
     * @param requestType the type of orchestration request (must not be {@code null} or empty)
     * @param context    the decision context (may be {@code null})
     * @param goal       the goal descriptor (may be {@code null})
     * @param payload    the request payload (must not be {@code null})
     * @param metadata   additional metadata (must not be {@code null})
     * @throws IllegalArgumentException if chiefId, requestType, payload, or metadata is {@code null}
     * @throws IllegalArgumentException if requestType is empty
     */
    public ChiefRequest(
            ChiefId chiefId,
            String requestType,
            DecisionContext context,
            GoalDescriptor goal,
            Map<String, Object> payload,
            Map<String, Object> metadata) {
        if (chiefId == null) {
            throw new IllegalArgumentException("ChiefRequest chiefId must not be null");
        }
        if (requestType == null || requestType.trim().isEmpty()) {
            throw new IllegalArgumentException("ChiefRequest requestType must not be null or empty");
        }
        if (payload == null) {
            throw new IllegalArgumentException("ChiefRequest payload must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ChiefRequest metadata must not be null");
        }

        this.chiefId = chiefId;
        this.requestType = requestType;
        this.context = context;
        this.goal = goal;
        this.payload = Collections.unmodifiableMap(new HashMap<>(payload));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns the orchestration identifier.
     *
     * @return the orchestration identifier
     */
    public ChiefId chiefId() {
        return chiefId;
    }

    /**
     * Returns the type of orchestration request.
     *
     * @return the request type
     */
    public String requestType() {
        return requestType;
    }

    /**
     * Returns the decision context, if present.
     *
     * @return the decision context, or {@code null} if not set
     */
    public DecisionContext context() {
        return context;
    }

    /**
     * Returns the goal descriptor, if present.
     *
     * @return the goal descriptor, or {@code null} if not set
     */
    public GoalDescriptor goal() {
        return goal;
    }

    /**
     * Returns an unmodifiable view of the request payload.
     *
     * @return an unmodifiable map of payload data
     */
    public Map<String, Object> payload() {
        return payload;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return an unmodifiable map of metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChiefRequest that = (ChiefRequest) obj;
        return Objects.equals(chiefId, that.chiefId) &&
                Objects.equals(requestType, that.requestType) &&
                Objects.equals(context, that.context) &&
                Objects.equals(goal, that.goal) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chiefId, requestType, context, goal, payload, metadata);
    }

    @Override
    public String toString() {
        return "ChiefRequest{" +
                "chiefId=" + chiefId +
                ", requestType='" + requestType + '\'' +
                ", context=" + context +
                ", goal=" + goal +
                ", payload=" + payload +
                ", metadata=" + metadata +
                '}';
    }
}
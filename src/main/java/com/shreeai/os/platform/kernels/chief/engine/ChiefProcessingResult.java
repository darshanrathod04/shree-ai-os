package com.shreeai.os.platform.kernels.chief.engine;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.shreeai.os.platform.kernels.chief.model.ChiefId;
import com.shreeai.os.platform.kernels.chief.model.DecisionResult;
import com.shreeai.os.platform.kernels.chief.model.CoordinationState;
import com.shreeai.os.platform.kernels.chief.model.DelegationResult;
import com.shreeai.os.platform.kernels.chief.model.GoalDescriptor;

/**
 * <b>ChiefProcessingResult</b>
 *
 * <p>Immutable value object representing the result of strategic processing.
 * This class encapsulates the outcome of orchestration processing.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates processing outcome.</li>
 *   <li>Provides immutable processing results.</li>
 *   <li>Contains no processing logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Constructor validation — rejects null arguments.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value semantics — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Engine Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-106, EIO-ARCH-001</p>
 *
 * @param chiefId            the orchestration identifier (must not be {@code null})
 * @param decision           the decision result (may be {@code null})
 * @param coordination       the coordination state (may be {@code null})
 * @param delegation         the delegation result (may be {@code null})
 * @param goals              the list of goal descriptors (must not be {@code null})
 * @param metadata           additional metadata (must not be {@code null})
 * @param processedAt        when the processing was completed (must not be {@code null})
 *
 * @since 1.0
 */
public final class ChiefProcessingResult {

    private final ChiefId chiefId;
    private final DecisionResult decision;
    private final CoordinationState coordination;
    private final DelegationResult delegation;
    private final List<GoalDescriptor> goals;
    private final Map<String, Object> metadata;
    private final Instant processedAt;

    /**
     * Constructs a {@code ChiefProcessingResult} with the specified parameters.
     *
     * @param chiefId      the orchestration identifier (must not be {@code null})
     * @param decision     the decision result (may be {@code null})
     * @param coordination the coordination state (may be {@code null})
     * @param delegation   the delegation result (may be {@code null})
     * @param goals        the list of goal descriptors (must not be {@code null})
     * @param metadata     additional metadata (must not be {@code null})
     * @param processedAt  when the processing was completed (must not be {@code null})
     * @throws IllegalArgumentException if chiefId, goals, metadata, or processedAt is {@code null}
     */
    public ChiefProcessingResult(
            ChiefId chiefId,
            DecisionResult decision,
            CoordinationState coordination,
            DelegationResult delegation,
            List<GoalDescriptor> goals,
            Map<String, Object> metadata,
            Instant processedAt) {
        if (chiefId == null) {
            throw new IllegalArgumentException("ChiefProcessingResult chiefId must not be null");
        }
        if (goals == null) {
            throw new IllegalArgumentException("ChiefProcessingResult goals must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ChiefProcessingResult metadata must not be null");
        }
        if (processedAt == null) {
            throw new IllegalArgumentException("ChiefProcessingResult processedAt must not be null");
        }

        this.chiefId = chiefId;
        this.decision = decision;
        this.coordination = coordination;
        this.delegation = delegation;
        this.goals = List.copyOf(goals);
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
        this.processedAt = processedAt;
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
     * Returns the decision result, if present.
     *
     * @return the decision result, or {@code null} if not set
     */
    public DecisionResult decision() {
        return decision;
    }

    /**
     * Returns the coordination state, if present.
     *
     * @return the coordination state, or {@code null} if not set
     */
    public CoordinationState coordination() {
        return coordination;
    }

    /**
     * Returns the delegation result, if present.
     *
     * @return the delegation result, or {@code null} if not set
     */
    public DelegationResult delegation() {
        return delegation;
    }

    /**
     * Returns an unmodifiable list of goal descriptors.
     *
     * @return unmodifiable list of goal descriptors
     */
    public List<GoalDescriptor> goals() {
        return goals;
    }

    /**
     * Returns an unmodifiable view of the metadata.
     *
     * @return unmodifiable map of metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * Returns when the processing was completed.
     *
     * @return the processing timestamp
     */
    public Instant processedAt() {
        return processedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiefProcessingResult that = (ChiefProcessingResult) obj;
        return Objects.equals(chiefId, that.chiefId) &&
                Objects.equals(decision, that.decision) &&
                Objects.equals(coordination, that.coordination) &&
                Objects.equals(delegation, that.delegation) &&
                Objects.equals(goals, that.goals) &&
                Objects.equals(metadata, that.metadata) &&
                Objects.equals(processedAt, that.processedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chiefId, decision, coordination, delegation, goals, metadata, processedAt);
    }

    @Override
    public String toString() {
        return "ChiefProcessingResult{chiefId=" + chiefId + ", processedAt=" + processedAt + '}';
    }
}
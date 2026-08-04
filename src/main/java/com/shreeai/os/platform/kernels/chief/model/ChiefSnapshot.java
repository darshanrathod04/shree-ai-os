package com.shreeai.os.platform.kernels.chief.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefSnapshot</b>
 *
 * <p>Represents an immutable orchestration snapshot.
 * This value object provides a historical representation of an orchestration state.</p>
 */
public final class ChiefSnapshot {

    private final ChiefId chiefId;
    private final ChiefRequest request;
    private final ChiefResponse response;
    private final CoordinationState coordinationState;
    private final ChiefMetrics metrics;
    private final Instant capturedAt;
    private final Map<String, Object> metadata;

    public ChiefSnapshot(
            ChiefId chiefId,
            ChiefRequest request,
            ChiefResponse response,
            CoordinationState coordinationState,
            ChiefMetrics metrics,
            Instant capturedAt,
            Map<String, Object> metadata) {
        if (chiefId == null) throw new IllegalArgumentException("ChiefSnapshot chiefId must not be null");
        if (request == null) throw new IllegalArgumentException("ChiefSnapshot request must not be null");
        if (response == null) throw new IllegalArgumentException("ChiefSnapshot response must not be null");
        if (coordinationState == null) throw new IllegalArgumentException("ChiefSnapshot coordinationState must not be null");
        if (metrics == null) throw new IllegalArgumentException("ChiefSnapshot metrics must not be null");
        if (capturedAt == null) throw new IllegalArgumentException("ChiefSnapshot capturedAt must not be null");
        if (metadata == null) throw new IllegalArgumentException("ChiefSnapshot metadata must not be null");

        this.chiefId = chiefId;
        this.request = request;
        this.response = response;
        this.coordinationState = coordinationState;
        this.metrics = metrics;
        this.capturedAt = capturedAt;
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    public ChiefId chiefId() { return chiefId; }
    public ChiefRequest request() { return request; }
    public ChiefResponse response() { return response; }
    public CoordinationState coordinationState() { return coordinationState; }
    public ChiefMetrics metrics() { return metrics; }
    public Instant capturedAt() { return capturedAt; }
    public Map<String, Object> metadata() { return metadata; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiefSnapshot that = (ChiefSnapshot) obj;
        return Objects.equals(chiefId, that.chiefId) && Objects.equals(capturedAt, that.capturedAt);
    }

    @Override
    public int hashCode() { return Objects.hash(chiefId, capturedAt); }

    @Override
    public String toString() {
        return "ChiefSnapshot{chiefId=" + chiefId + ", capturedAt=" + capturedAt + '}';
    }
}
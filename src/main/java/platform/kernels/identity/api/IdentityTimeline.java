package platform.kernels.identity.api;

import java.time.Instant;
import java.util.List;

/**
 * <b>IdentityTimeline</b>
 *
 * <p>Represents the timeline of events for an Identity within the platform.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines the contract for Identity timeline data.</li>
 *   <li>Encapsulates the chronological sequence of Identity events.</li>
 *   <li>Provides a stable API contract independent of implementation.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Identity Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is a pure data contract with no business logic.</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-106</p>
 *
 * @param identityId the unique identifier of the Identity
 * @param events the chronological list of timeline events
 * @param createdAt the timestamp when the timeline was created
 */
public record IdentityTimeline(
    String identityId,
    List<TimelineEvent> events,
    Instant createdAt
) {
    /**
     * Creates a new IdentityTimeline with validation.
     *
     * @param identityId the unique identifier of the Identity
     * @param events the chronological list of timeline events
     * @param createdAt the timestamp when the timeline was created
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public IdentityTimeline {
        if (identityId == null || identityId.isBlank()) {
            throw new IllegalArgumentException("identityId cannot be null or blank");
        }
        if (events == null) {
            throw new IllegalArgumentException("events cannot be null");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
    }

    /**
     * Represents a single event in an Identity's timeline.
     *
     * @param eventType the type of event
     * @param description a description of the event
     * @param timestamp when the event occurred
     */
    public record TimelineEvent(
        String eventType,
        String description,
        Instant timestamp
    ) {
        /**
         * Creates a new TimelineEvent with validation.
         *
         * @param eventType the type of event
         * @param description a description of the event
         * @param timestamp when the event occurred
         * @throws IllegalArgumentException if any parameter is invalid
         */
        public TimelineEvent {
            if (eventType == null || eventType.isBlank()) {
                throw new IllegalArgumentException("eventType cannot be null or blank");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("description cannot be null or blank");
            }
            if (timestamp == null) {
                throw new IllegalArgumentException("timestamp cannot be null");
            }
        }
    }
}
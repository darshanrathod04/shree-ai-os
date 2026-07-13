package platform.core.eventbus.validator;

import platform.core.eventbus.model.Event;
import platform.core.eventbus.model.EventId;
import platform.core.eventbus.model.EventMetadata;
import platform.core.eventbus.model.EventSubscriber;
import platform.core.eventbus.model.EventTopic;
import platform.core.registry.validator.ValidationResult;

import java.util.Objects;

/**
 * <b>EventValidator</b>
 *
 * <p>Validates the structural correctness of Event Bus domain models
 * within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies the structural correctness of Event Bus domain models.</li>
 *   <li>Remains completely independent from dispatching and service execution.</li>
 *   <li>Protects the Platform Language by ensuring all models meet invariants.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Characteristics:</b></p>
 * <ul>
 *   <li>Stateless — no instance fields, no mutable state.</li>
 *   <li>Deterministic — same inputs always produce same outputs.</li>
 *   <li>Thread-safe — can be called concurrently without synchronization.</li>
 *   <li>Pure validation — never dispatches events, never mutates models.</li>
 * </ul>
 *
 * <p><b>Reuses:</b> {@link platform.core.registry.validator.ValidationResult}</p>
 *
 * @see platform.core.registry.validator.ValidationResult
 */
public final class EventValidator {

    private EventValidator() {
        // Utility class — no instances
    }

    /**
     * Validates an {@link Event} for structural correctness.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Event must not be null</li>
     *   <li>EventId must exist and be valid</li>
     *   <li>Topic must exist and be valid</li>
     *   <li>Metadata must exist and be valid</li>
     *   <li>Payload must exist</li>
     *   <li>Timestamp must exist</li>
     * </ul>
     *
     * @param event the event to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateEvent(Event event) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (event == null) {
            builder.addError("Event must not be null");
            return builder.build();
        }

        // Validate EventId
        ValidationResult idResult = validateEventId(event.id());
        if (!idResult.isValid()) {
            idResult.errors().forEach(builder::addError);
        }

        // Validate Topic
        ValidationResult topicResult = validateTopic(event.topic());
        if (!topicResult.isValid()) {
            topicResult.errors().forEach(builder::addError);
        }

        // Validate Metadata
        ValidationResult metadataResult = validateMetadata(event.metadata());
        if (!metadataResult.isValid()) {
            metadataResult.errors().forEach(builder::addError);
        }

        // Validate Payload
        ValidationResult payloadResult = validatePayload(event.payload());
        if (!payloadResult.isValid()) {
            payloadResult.errors().forEach(builder::addError);
        }

        // Validate Timestamp
        if (event.timestamp() == null) {
            builder.addError("Event timestamp must not be null");
        }

        return builder.build();
    }

    /**
     * Validates an {@link EventId} for structural correctness.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>EventId must not be null</li>
     *   <li>UUID must be valid</li>
     * </ul>
     *
     * @param eventId the event ID to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateEventId(EventId eventId) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (eventId == null) {
            builder.addError("EventId must not be null");
            return builder.build();
        }

        if (eventId.value() == null) {
            builder.addError("EventId UUID must not be null");
        }

        return builder.build();
    }

    /**
     * Validates an {@link EventTopic} for structural correctness.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Topic must not be null</li>
     *   <li>Topic name must not be blank</li>
     *   <li>Topic name must not have leading/trailing spaces</li>
     *   <li>Topic name length must be <= 128 characters</li>
     * </ul>
     *
     * @param topic the topic to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateTopic(EventTopic topic) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (topic == null) {
            builder.addError("EventTopic must not be null");
            return builder.build();
        }

        String name = topic.value();

        if (name == null || name.isBlank()) {
            builder.addError("EventTopic name must not be null or blank");
            return builder.build();
        }

        if (!name.equals(name.trim())) {
            builder.addError("EventTopic name must not have leading or trailing spaces");
        }

        if (name.length() > 128) {
            builder.addError("EventTopic name must not exceed 128 characters");
        }

        return builder.build();
    }

    /**
     * Validates {@link EventMetadata} for structural correctness.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Metadata must not be null</li>
     *   <li>Publisher must exist and not be blank</li>
     *   <li>Priority must exist</li>
     *   <li>CorrelationId must exist and not be blank</li>
     *   <li>Attributes map must not be null</li>
     * </ul>
     *
     * @param metadata the metadata to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateMetadata(EventMetadata metadata) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (metadata == null) {
            builder.addError("EventMetadata must not be null");
            return builder.build();
        }

        if (metadata.publisher() == null || metadata.publisher().isBlank()) {
            builder.addError("EventMetadata publisher must not be null or blank");
        }

        if (metadata.priority() == null) {
            builder.addError("EventMetadata priority must not be null");
        }

        if (metadata.correlationId() == null || metadata.correlationId().isBlank()) {
            builder.addError("EventMetadata correlationId must not be null or blank");
        }

        if (metadata.attributes() == null) {
            builder.addError("EventMetadata attributes must not be null");
        }

        return builder.build();
    }

    /**
     * Validates an {@link EventSubscriber} for structural correctness.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Subscriber must not be null</li>
     * </ul>
     *
     * @param subscriber the subscriber to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateSubscriber(EventSubscriber subscriber) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (subscriber == null) {
            builder.addError("EventSubscriber must not be null");
        }

        return builder.build();
    }

    /**
     * Validates an event payload for structural correctness.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Payload must exist (not null)</li>
     * </ul>
     *
     * <p><b>Note:</b> Expected payload failures SHALL return {@link ValidationResult},
     * never exceptions.</p>
     *
     * @param payload the payload to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validatePayload(Object payload) {
        ValidationResult.Builder builder = ValidationResult.builder();

        if (payload == null) {
            builder.addError("Event payload must not be null");
        }

        return builder.build();
    }
}
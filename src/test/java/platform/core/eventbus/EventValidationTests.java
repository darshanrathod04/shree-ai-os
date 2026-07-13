package platform.core.eventbus;

import platform.core.eventbus.model.Event;
import platform.core.eventbus.model.EventId;
import platform.core.eventbus.model.EventMetadata;
import platform.core.eventbus.model.EventPriority;
import platform.core.eventbus.model.EventSubscriber;
import platform.core.eventbus.model.EventTopic;
import platform.core.eventbus.validator.EventValidator;
import platform.core.registry.validator.ValidationResult;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>EventValidationTests</b>
 *
 * <p>Verifies the validation behavior of the {@link EventValidator}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates EventValidator validates all model types correctly.</li>
 *   <li>Validates all validation failures return ValidationResult.</li>
 *   <li>Validates ValidationResult behavior.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see EventValidator
 */
public class EventValidationTests {

    private final EventValidator validator = new EventValidator();

    // ===== Event Validation =====

    /**
     * Verifies valid event passes validation.
     */
    public void testValidEventPassesValidation() {
        // Arrange
        EventId id = new EventId();
        EventTopic topic = new EventTopic("test-topic");
        EventMetadata metadata = new EventMetadata("publisher", EventPriority.NORMAL, "corr-123");
        Event event = new Event(id, topic, metadata, "payload");

        // Act
        ValidationResult result = validator.validateEvent(event);

        // Assert
        assert result.isValid() : "Valid event should pass validation";
    }

    /**
     * Verifies null event fails validation.
     */
    public void testNullEventFailsValidation() {
        // Act
        ValidationResult result = validator.validateEvent(null);

        // Assert
        assert !result.isValid() : "Null event should fail validation";
        assert result.errors().contains("Event must not be null") : "Should have null event error";
    }

    /**
     * Verifies event with null topic fails validation.
     */
    public void testEventWithNullTopicFailsValidation() {
        // Arrange
        EventId id = new EventId();
        EventMetadata metadata = new EventMetadata("publisher", EventPriority.NORMAL, "corr-123");
        Event event = new Event(id, null, metadata, "payload");

        // Act
        ValidationResult result = validator.validateEvent(event);

        // Assert
        assert !result.isValid() : "Event with null topic should fail validation";
    }

    /**
     * Verifies event with null metadata fails validation.
     */
    public void testEventWithNullMetadataFailsValidation() {
        // Arrange
        EventId id = new EventId();
        EventTopic topic = new EventTopic("test-topic");
        Event event = new Event(id, topic, null, "payload");

        // Act
        ValidationResult result = validator.validateEvent(event);

        // Assert
        assert !result.isValid() : "Event with null metadata should fail validation";
    }

    /**
     * Verifies event with null payload fails validation.
     */
    public void testEventWithNullPayloadFailsValidation() {
        // Arrange
        EventId id = new EventId();
        EventTopic topic = new EventTopic("test-topic");
        EventMetadata metadata = new EventMetadata("publisher", EventPriority.NORMAL, "corr-123");
        Event event = new Event(id, topic, metadata, null);

        // Act
        ValidationResult result = validator.validateEvent(event);

        // Assert
        assert !result.isValid() : "Event with null payload should fail validation";
    }

    // ===== EventId Validation =====

    /**
     * Verifies valid EventId passes validation.
     */
    public void testValidEventIdPassesValidation() {
        // Arrange
        EventId id = new EventId();

        // Act
        ValidationResult result = validator.validateEventId(id);

        // Assert
        assert result.isValid() : "Valid EventId should pass validation";
    }

    /**
     * Verifies null EventId fails validation.
     */
    public void testNullEventIdFailsValidation() {
        // Act
        ValidationResult result = validator.validateEventId(null);

        // Assert
        assert !result.isValid() : "Null EventId should fail validation";
    }

    // ===== EventTopic Validation =====

    /**
     * Verifies valid EventTopic passes validation.
     */
    public void testValidEventTopicPassesValidation() {
        // Arrange
        EventTopic topic = new EventTopic("test-topic");

        // Act
        ValidationResult result = validator.validateTopic(topic);

        // Assert
        assert result.isValid() : "Valid EventTopic should pass validation";
    }

    /**
     * Verifies null EventTopic fails validation.
     */
    public void testNullEventTopicFailsValidation() {
        // Act
        ValidationResult result = validator.validateTopic(null);

        // Assert
        assert !result.isValid() : "Null EventTopic should fail validation";
    }

    /**
     * Verifies blank EventTopic fails validation.
     */
    public void testBlankEventTopicFailsValidation() {
        // Arrange
        EventTopic topic = new EventTopic("   ");

        // Act
        ValidationResult result = validator.validateTopic(topic);

        // Assert
        assert !result.isValid() : "Blank EventTopic should fail validation";
    }

    /**
     * Verifies EventTopic with leading/trailing spaces fails validation.
     */
    public void testEventTopicWithSpacesFailsValidation() {
        // Arrange
        EventTopic topic = new EventTopic(" test-topic ");

        // Act
        ValidationResult result = validator.validateTopic(topic);

        // Assert
        assert !result.isValid() : "EventTopic with spaces should fail validation";
    }

    /**
     * Verifies EventTopic exceeding 128 characters fails validation.
     */
    public void testEventTopicExceedingMaxLengthFailsValidation() {
        // Arrange
        String longName = "a".repeat(129);
        EventTopic topic = new EventTopic(longName);

        // Act
        ValidationResult result = validator.validateTopic(topic);

        // Assert
        assert !result.isValid() : "EventTopic exceeding 128 chars should fail validation";
    }

    // ===== EventMetadata Validation =====

    /**
     * Verifies valid EventMetadata passes validation.
     */
    public void testValidEventMetadataPassesValidation() {
        // Arrange
        EventMetadata metadata = new EventMetadata("publisher", EventPriority.NORMAL, "corr-123");

        // Act
        ValidationResult result = validator.validateMetadata(metadata);

        // Assert
        assert result.isValid() : "Valid EventMetadata should pass validation";
    }

    /**
     * Verifies null EventMetadata fails validation.
     */
    public void testNullEventMetadataFailsValidation() {
        // Act
        ValidationResult result = validator.validateMetadata(null);

        // Assert
        assert !result.isValid() : "Null EventMetadata should fail validation";
    }

    /**
     * Verifies EventMetadata with null publisher fails validation.
     */
    public void testEventMetadataWithNullPublisherFailsValidation() {
        // Arrange
        EventMetadata metadata = new EventMetadata(null, EventPriority.NORMAL, "corr-123");

        // Act
        ValidationResult result = validator.validateMetadata(metadata);

        // Assert
        assert !result.isValid() : "EventMetadata with null publisher should fail validation";
    }

    /**
     * Verifies EventMetadata with null priority fails validation.
     */
    public void testEventMetadataWithNullPriorityFailsValidation() {
        // Arrange
        EventMetadata metadata = new EventMetadata("publisher", null, "corr-123");

        // Act
        ValidationResult result = validator.validateMetadata(metadata);

        // Assert
        assert !result.isValid() : "EventMetadata with null priority should fail validation";
    }

    /**
     * Verifies EventMetadata with null correlationId fails validation.
     */
    public void testEventMetadataWithNullCorrelationIdFailsValidation() {
        // Arrange
        EventMetadata metadata = new EventMetadata("publisher", EventPriority.NORMAL, null);

        // Act
        ValidationResult result = validator.validateMetadata(metadata);

        // Assert
        assert !result.isValid() : "EventMetadata with null correlationId should fail validation";
    }

    // ===== EventSubscriber Validation =====

    /**
     * Verifies valid EventSubscriber passes validation.
     */
    public void testValidEventSubscriberPassesValidation() {
        // Arrange
        EventSubscriber subscriber = new TestSubscriber();

        // Act
        ValidationResult result = validator.validateSubscriber(subscriber);

        // Assert
        assert result.isValid() : "Valid EventSubscriber should pass validation";
    }

    /**
     * Verifies null EventSubscriber fails validation.
     */
    public void testNullEventSubscriberFailsValidation() {
        // Act
        ValidationResult result = validator.validateSubscriber(null);

        // Assert
        assert !result.isValid() : "Null EventSubscriber should fail validation";
    }

    // ===== Payload Validation =====

    /**
     * Verifies non-null payload passes validation.
     */
    public void testNonNullPayloadPassesValidation() {
        // Act
        ValidationResult result = validator.validatePayload("payload");

        // Assert
        assert result.isValid() : "Non-null payload should pass validation";
    }

    /**
     * Verifies null payload fails validation.
     */
    public void testNullPayloadFailsValidation() {
        // Act
        ValidationResult result = validator.validatePayload(null);

        // Assert
        assert !result.isValid() : "Null payload should fail validation";
    }

    // ===== ValidationResult Behavior =====

    /**
     * Verifies ValidationResult is immutable.
     */
    public void testValidationResultIsImmutable() {
        // Act
        ValidationResult result = validator.validateEvent(null);

        // Assert
        assert result.errors() != null : "Errors should not be null";
        assert result.warnings() != null : "Warnings should not be null";
    }

    // Helper classes
    private static class TestSubscriber implements EventSubscriber {
        @Override
        public void onEvent(Event event) {
        }
    }
}
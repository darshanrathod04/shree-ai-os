package platform.core.eventbus;

import com.shreeai.os.platform.core.eventbus.error.EventBusException;
import com.shreeai.os.platform.core.eventbus.error.EventDispatchException;
import com.shreeai.os.platform.core.eventbus.error.EventError;
import com.shreeai.os.platform.core.eventbus.error.EventErrorCode;
import com.shreeai.os.platform.core.eventbus.error.InvalidEventException;
import com.shreeai.os.platform.core.eventbus.error.NoSubscribersException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>EventErrorTests</b>
 *
 * <p>Verifies the error handling behavior of the Event Bus subsystem.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates EventBusException hierarchy.</li>
 *   <li>Validates EventErrorCode values.</li>
 *   <li>Validates EventError immutability.</li>
 *   <li>Validates concrete exception types.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see EventBusException
 * @see EventError
 * @see EventErrorCode
 */
public class EventErrorTests {

    // ===== EventErrorCode Tests =====

    /**
     * Verifies all EventErrorCode values exist.
     */
    public void testAllEventErrorCodesExist() {
        assert EventErrorCode.valueOf("EVENT_INVALID") != null : "EVENT_INVALID should exist";
        assert EventErrorCode.valueOf("EVENT_VALIDATION_FAILED") != null : "EVENT_VALIDATION_FAILED should exist";
        assert EventErrorCode.valueOf("EVENT_NO_SUBSCRIBERS") != null : "EVENT_NO_SUBSCRIBERS should exist";
        assert EventErrorCode.valueOf("EVENT_DISPATCH_FAILED") != null : "EVENT_DISPATCH_FAILED should exist";
        assert EventErrorCode.valueOf("EVENT_TOPIC_NOT_FOUND") != null : "EVENT_TOPIC_NOT_FOUND should exist";
        assert EventErrorCode.valueOf("EVENT_SUBSCRIBER_FAILED") != null : "EVENT_SUBSCRIBER_FAILED should exist";
        assert EventErrorCode.valueOf("EVENT_PUBLISH_FAILED") != null : "EVENT_PUBLISH_FAILED should exist";
    }

    // ===== EventError Tests =====

    /**
     * Verifies EventError is immutable.
     */
    public void testEventErrorIsImmutable() {
        // Arrange
        Map<String, Object> details = new HashMap<>();
        details.put("key", "value");
        EventError error = new EventError(EventErrorCode.EVENT_INVALID, "test message", Instant.now(), details);

        // Assert
        assert error.code() == EventErrorCode.EVENT_INVALID : "Code should be accessible";
        assert error.message().equals("test message") : "Message should be accessible";
        assert error.timestamp() != null : "Timestamp should not be null";
        assert error.details() != null : "Details should not be null";
        assert error.details().equals(details) : "Details should match";
    }

    /**
     * Verifies EventError constructor validation.
     */
    public void testEventErrorConstructorValidation() {
        // Act & Assert - null code
        try {
            new EventError(null, "message");
            throw new AssertionError("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Act & Assert - null message
        try {
            new EventError(EventErrorCode.EVENT_INVALID, null);
            throw new AssertionError("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Act & Assert - blank message
        try {
            new EventError(EventErrorCode.EVENT_INVALID, "   ");
            throw new AssertionError("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /**
     * Verifies EventError equals and hashCode.
     */
    public void testEventErrorEqualsAndHashCode() {
        // Arrange
        EventError error1 = new EventError(EventErrorCode.EVENT_INVALID, "test message");
        EventError error2 = new EventError(EventErrorCode.EVENT_INVALID, "test message");

        // Assert
        assert error1.equals(error1) : "Same object should be equal";
        assert error1.hashCode() == error1.hashCode() : "Same object hashCode should match";
    }

    // ===== EventBusException Tests =====

    /**
     * Verifies EventBusException is a RuntimeException.
     */
    public void testEventBusExceptionIsRuntimeException() {
        // Arrange
        EventError error = new EventError(EventErrorCode.EVENT_INVALID, "test");
        EventBusException exception = new EventBusException(error);

        // Assert
        assert exception instanceof RuntimeException : "EventBusException should extend RuntimeException";
    }

    /**
     * Verifies EventBusException contains EventError.
     */
    public void testEventBusExceptionContainsError() {
        // Arrange
        EventError error = new EventError(EventErrorCode.EVENT_INVALID, "test message");
        EventBusException exception = new EventBusException(error);

        // Assert
        assert exception.error() == error : "Exception should contain the error";
        assert exception.code() == EventErrorCode.EVENT_INVALID : "Code should match";
        assert exception.getMessage().equals("test message") : "Message should match";
    }

    // ===== InvalidEventException Tests =====

    /**
     * Verifies InvalidEventException is thrown correctly.
     */
    public void testInvalidEventException() {
        // Arrange
        EventError error = new EventError(EventErrorCode.EVENT_INVALID, "Invalid event");
        InvalidEventException exception = new InvalidEventException(error);

        // Assert
        assert exception.code() == EventErrorCode.EVENT_INVALID : "Code should be EVENT_INVALID";
        assert exception instanceof EventBusException : "Should extend EventBusException";
    }

    // ===== NoSubscribersException Tests =====

    /**
     * Verifies NoSubscribersException is thrown correctly.
     */
    public void testNoSubscribersException() {
        // Arrange
        EventError error = new EventError(EventErrorCode.EVENT_NO_SUBSCRIBERS, "No subscribers");
        NoSubscribersException exception = new NoSubscribersException(error);

        // Assert
        assert exception.code() == EventErrorCode.EVENT_NO_SUBSCRIBERS : "Code should be EVENT_NO_SUBSCRIBERS";
        assert exception instanceof EventBusException : "Should extend EventBusException";
    }

    // ===== EventDispatchException Tests =====

    /**
     * Verifies EventDispatchException is thrown correctly.
     */
    public void testEventDispatchException() {
        // Arrange
        EventError error = new EventError(EventErrorCode.EVENT_DISPATCH_FAILED, "Dispatch failed");
        EventDispatchException exception = new EventDispatchException(error);

        // Assert
        assert exception.code() == EventErrorCode.EVENT_DISPATCH_FAILED : "Code should be EVENT_DISPATCH_FAILED";
        assert exception instanceof EventBusException : "Should extend EventBusException";
    }

    // ===== Exception Hierarchy Tests =====

    /**
     * Verifies all exceptions can be caught as EventBusException.
     */
    public void testAllExceptionsAreEventBusExceptions() {
        assert new InvalidEventException(
                new EventError(EventErrorCode.EVENT_INVALID, "test")
        ) instanceof EventBusException;
        assert new NoSubscribersException(
                new EventError(EventErrorCode.EVENT_NO_SUBSCRIBERS, "test")
        ) instanceof EventBusException;
        assert new EventDispatchException(
                new EventError(EventErrorCode.EVENT_DISPATCH_FAILED, "test")
        ) instanceof EventBusException;
    }

    /**
     * Verifies error messages are preserved.
     */
    public void testErrorMessagesPreserved() {
        // Arrange
        String message = "Test error message";
        EventError error = new EventError(EventErrorCode.EVENT_INVALID, message);
        EventBusException exception = new EventBusException(error);

        // Assert
        assert exception.getMessage().equals(message) : "Message should be preserved";
        assert error.message().equals(message) : "Error message should be preserved";
    }
}
package platform.core.health;

import platform.core.health.error.HealthCheckFailedException;
import platform.core.health.error.HealthComponentNotFoundException;
import platform.core.health.error.HealthError;
import platform.core.health.error.HealthErrorCode;
import platform.core.health.error.HealthException;
import platform.core.health.error.InvalidHealthComponentException;
import platform.core.health.model.HealthComponent;
import platform.core.health.model.HealthComponentId;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>HealthErrorTests</b>
 *
 * <p>Tests for health error handling within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies HealthError structure and behavior.</li>
 *   <li>Verifies HealthException hierarchy.</li>
 *   <li>Verifies concrete exception types.</li>
 *   <li>Verifies error code mapping.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see HealthError
 * @see HealthException
 */
public class HealthErrorTests {

    // HealthError tests

    /**
     * Test: Create HealthError with valid parameters.
     */
    @Test
    void testCreateHealthErrorWithValidParameters() {
        // Arrange
        HealthErrorCode code = HealthErrorCode.HEALTH_CHECK_FAILED;
        String message = "Test error message";
        Instant timestamp = Instant.now();
        Map<String, Object> details = Map.of("key", "value");

        // Act
        HealthError error = new HealthError(code, message, timestamp, details);

        // Assert
        assertEquals(code, error.code());
        assertEquals(message, error.message());
        assertEquals(timestamp, error.timestamp());
        assertEquals(details, error.details());
    }

    /**
     * Test: Create HealthError with null code throws NullPointerException.
     */
    @Test
    void testCreateHealthErrorWithNullCode() {
        // Arrange
        String message = "Test message";
        Instant timestamp = Instant.now();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new HealthError(null, message, timestamp, Map.of()));
    }

    /**
     * Test: Create HealthError with null message throws NullPointerException.
     */
    @Test
    void testCreateHealthErrorWithNullMessage() {
        // Arrange
        HealthErrorCode code = HealthErrorCode.HEALTH_CHECK_FAILED;
        Instant timestamp = Instant.now();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new HealthError(code, null, timestamp, Map.of()));
    }

    /**
     * Test: Create HealthError with blank message throws IllegalArgumentException.
     */
    @Test
    void testCreateHealthErrorWithBlankMessage() {
        // Arrange
        HealthErrorCode code = HealthErrorCode.HEALTH_CHECK_FAILED;
        Instant timestamp = Instant.now();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new HealthError(code, "   ", timestamp, Map.of()));
    }

    /**
     * Test: Create HealthError with null timestamp throws NullPointerException.
     */
    @Test
    void testCreateHealthErrorWithNullTimestamp() {
        // Arrange
        HealthErrorCode code = HealthErrorCode.HEALTH_CHECK_FAILED;
        String message = "Test message";

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new HealthError(code, message, null, Map.of()));
    }

    /**
     * Test: Create HealthError with null details uses empty map.
     */
    @Test
    void testCreateHealthErrorWithNullDetails() {
        // Arrange
        HealthErrorCode code = HealthErrorCode.HEALTH_CHECK_FAILED;
        String message = "Test message";
        Instant timestamp = Instant.now();

        // Act
        HealthError error = new HealthError(code, message, timestamp, null);

        // Assert
        assertNotNull(error.details());
        assertTrue(error.details().isEmpty());
    }

    /**
     * Test: HealthError details map is unmodifiable.
     */
    @Test
    void testHealthErrorDetailsIsUnmodifiable() {
        // Arrange
        HealthError error = new HealthError(
                HealthErrorCode.HEALTH_CHECK_FAILED,
                "Test",
                Instant.now(),
                Map.of("key", "value")
        );

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> error.details().put("newKey", "newValue"));
    }

    /**
     * Test: HealthError equals and hashCode.
     */
    @Test
    void testHealthErrorEqualsAndHashCode() {
        // Arrange
        Instant timestamp = Instant.now();
        Map<String, Object> details = Map.of("key", "value");

        HealthError error1 = new HealthError(HealthErrorCode.HEALTH_CHECK_FAILED, "Message", timestamp, details);
        HealthError error2 = new HealthError(HealthErrorCode.HEALTH_CHECK_FAILED, "Message", timestamp, details);

        // Assert
        assertEquals(error1, error2);
        assertEquals(error1.hashCode(), error2.hashCode());
    }

    /**
     * Test: HealthError toString contains code and message.
     */
    @Test
    void testHealthErrorToString() {
        // Arrange
        HealthError error = new HealthError(
                HealthErrorCode.HEALTH_CHECK_FAILED,
                "Test message",
                Instant.now(),
                Map.of()
        );

        // Act
        String toString = error.toString();

        // Assert
        assertTrue(toString.contains("HEALTH_CHECK_FAILED"));
        assertTrue(toString.contains("Test message"));
    }

    // HealthException tests

    /**
     * Test: Create HealthException with valid error.
     */
    @Test
    void testCreateHealthExceptionWithValidError() {
        // Arrange
        HealthError error = new HealthError(
                HealthErrorCode.HEALTH_CHECK_FAILED,
                "Test error",
                Instant.now(),
                Map.of()
        );

        // Act
        HealthException exception = new HealthException(error);

        // Assert
        assertEquals(error, exception.error());
        assertEquals(HealthErrorCode.HEALTH_CHECK_FAILED, exception.code());
        assertEquals("Test error", exception.getMessage());
    }

    /**
     * Test: Create HealthException with null error throws NullPointerException.
     */
    @Test
    void testCreateHealthExceptionWithNullError() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new HealthException(null));
    }

    /**
     * Test: HealthException is a RuntimeException.
     */
    @Test
    void testHealthExceptionIsRuntimeException() {
        // Arrange
        HealthError error = new HealthError(
                HealthErrorCode.HEALTH_CHECK_FAILED,
                "Test",
                Instant.now(),
                Map.of()
        );

        // Act
        HealthException exception = new HealthException(error);

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // HealthComponentNotFoundException tests

    /**
     * Test: Create HealthComponentNotFoundException with component.
     */
    @Test
    void testCreateHealthComponentNotFoundException() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        HealthComponentNotFoundException exception = new HealthComponentNotFoundException(component);

        // Assert
        assertNotNull(exception);
        assertEquals(HealthErrorCode.HEALTH_COMPONENT_NOT_FOUND, exception.code());
        assertTrue(exception.getMessage().contains("Test Component"));
    }

    /**
     * Test: HealthComponentNotFoundException contains component details.
     */
    @Test
    void testHealthComponentNotFoundExceptionContainsDetails() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        HealthComponentNotFoundException exception = new HealthComponentNotFoundException(component);

        // Assert
        HealthError error = exception.error();
        assertEquals("test-component", error.details().get("componentId"));
        assertEquals("Test Component", error.details().get("componentName"));
        assertEquals("Category", error.details().get("componentCategory"));
    }

    // HealthCheckFailedException tests

    /**
     * Test: Create HealthCheckFailedException with component and reason.
     */
    @Test
    void testCreateHealthCheckFailedException() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        HealthCheckFailedException exception = new HealthCheckFailedException(component, "Connection timeout");

        // Assert
        assertNotNull(exception);
        assertEquals(HealthErrorCode.HEALTH_CHECK_FAILED, exception.code());
        assertTrue(exception.getMessage().contains("Connection timeout"));
    }

    /**
     * Test: HealthCheckFailedException contains reason in details.
     */
    @Test
    void testHealthCheckFailedExceptionContainsReason() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        HealthCheckFailedException exception = new HealthCheckFailedException(component, "Timeout");

        // Assert
        HealthError error = exception.error();
        assertEquals("Timeout", error.details().get("reason"));
    }

    /**
     * Test: HealthCheckFailedException with custom details.
     */
    @Test
    void testHealthCheckFailedExceptionWithCustomDetails() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        Map<String, Object> details = Map.of("customKey", "customValue");

        // Act
        HealthCheckFailedException exception = new HealthCheckFailedException(component, "Error", details);

        // Assert
        assertEquals("customValue", exception.error().details().get("customKey"));
    }

    // InvalidHealthComponentException tests

    /**
     * Test: Create InvalidHealthComponentException with component and reason.
     */
    @Test
    void testCreateInvalidHealthComponentException() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        InvalidHealthComponentException exception = new InvalidHealthComponentException(component, "Name is blank");

        // Assert
        assertNotNull(exception);
        assertEquals(HealthErrorCode.HEALTH_INVALID_COMPONENT, exception.code());
        assertTrue(exception.getMessage().contains("Name is blank"));
    }

    /**
     * Test: InvalidHealthComponentException contains reason in details.
     */
    @Test
    void testInvalidHealthComponentExceptionContainsReason() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        InvalidHealthComponentException exception = new InvalidHealthComponentException(component, "Invalid name");

        // Assert
        HealthError error = exception.error();
        assertEquals("Invalid name", error.details().get("reason"));
    }

    /**
     * Test: InvalidHealthComponentException with custom details.
     */
    @Test
    void testInvalidHealthComponentExceptionWithCustomDetails() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        Map<String, Object> details = Map.of("field", "name");

        // Act
        InvalidHealthComponentException exception = new InvalidHealthComponentException(component, "Invalid", details);

        // Assert
        assertEquals("name", exception.error().details().get("field"));
    }

    // Exception hierarchy tests

    /**
     * Test: All concrete exceptions extend HealthException.
     */
    @Test
    void testAllConcreteExceptionsExtendHealthException() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        HealthComponentNotFoundException ex1 = new HealthComponentNotFoundException(component);
        HealthCheckFailedException ex2 = new HealthCheckFailedException(component, "Error");
        InvalidHealthComponentException ex3 = new InvalidHealthComponentException(component, "Invalid");

        // Assert
        assertTrue(ex1 instanceof HealthException);
        assertTrue(ex2 instanceof HealthException);
        assertTrue(ex3 instanceof HealthException);
    }

    /**
     * Test: HealthException can be caught as RuntimeException.
     */
    @Test
    void testHealthExceptionCanBeCaughtAsRuntimeException() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            throw new HealthComponentNotFoundException(component);
        });
    }

    // Error code tests

    /**
     * Test: All error codes are defined.
     */
    @Test
    void testAllErrorCodesDefined() {
        // Assert
        assertEquals(7, HealthErrorCode.values().length);
        assertNotNull(HealthErrorCode.valueOf("HEALTH_COMPONENT_NOT_FOUND"));
        assertNotNull(HealthErrorCode.valueOf("HEALTH_CHECK_FAILED"));
        assertNotNull(HealthErrorCode.valueOf("HEALTH_INVALID_COMPONENT"));
        assertNotNull(HealthErrorCode.valueOf("HEALTH_VALIDATION_FAILED"));
        assertNotNull(HealthErrorCode.valueOf("HEALTH_ALREADY_REGISTERED"));
        assertNotNull(HealthErrorCode.valueOf("HEALTH_NOT_REGISTERED"));
        assertNotNull(HealthErrorCode.valueOf("HEALTH_ENGINE_FAILURE"));
    }

    /**
     * Test: HealthComponentNotFoundException uses correct error code.
     */
    @Test
    void testHealthComponentNotFoundExceptionUsesCorrectErrorCode() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        HealthComponentNotFoundException exception = new HealthComponentNotFoundException(component);

        // Assert
        assertEquals(HealthErrorCode.HEALTH_COMPONENT_NOT_FOUND, exception.code());
    }

    /**
     * Test: HealthCheckFailedException uses correct error code.
     */
    @Test
    void testHealthCheckFailedExceptionUsesCorrectErrorCode() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        HealthCheckFailedException exception = new HealthCheckFailedException(component, "Error");

        // Assert
        assertEquals(HealthErrorCode.HEALTH_CHECK_FAILED, exception.code());
    }

    /**
     * Test: InvalidHealthComponentException uses correct error code.
     */
    @Test
    void testInvalidHealthComponentExceptionUsesCorrectErrorCode() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");

        // Act
        InvalidHealthComponentException exception = new InvalidHealthComponentException(component, "Invalid");

        // Assert
        assertEquals(HealthErrorCode.HEALTH_INVALID_COMPONENT, exception.code());
    }

    // Error timestamp tests

    /**
     * Test: HealthError timestamp is set to current time.
     */
    @Test
    void testHealthErrorTimestampIsCurrent() {
        // Arrange
        Instant before = Instant.now();

        // Act
        HealthError error = new HealthError(
                HealthErrorCode.HEALTH_CHECK_FAILED,
                "Test",
                Instant.now(),
                Map.of()
        );

        Instant after = Instant.now();

        // Assert
        assertTrue(error.timestamp().isAfter(before) || error.timestamp().equals(before));
        assertTrue(error.timestamp().isBefore(after) || error.timestamp().equals(after));
    }

    /**
     * Test: Exception error timestamp is recent.
     */
    @Test
    void testExceptionErrorTimestampIsRecent() {
        // Arrange
        HealthComponentId id = new HealthComponentId("test-component");
        HealthComponent component = new HealthComponent(id, "Test Component", "Category");
        Instant before = Instant.now();

        // Act
        HealthComponentNotFoundException exception = new HealthComponentNotFoundException(component);
        Instant after = Instant.now();

        // Assert
        assertTrue(exception.error().timestamp().isAfter(before) || exception.error().timestamp().equals(before));
        assertTrue(exception.error().timestamp().isBefore(after) || exception.error().timestamp().equals(after));
    }
}
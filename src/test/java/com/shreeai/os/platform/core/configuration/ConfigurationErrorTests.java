package com.shreeai.os.platform.core.configuration;

import com.shreeai.os.platform.core.configuration.error.ConfigurationError;
import com.shreeai.os.platform.core.configuration.error.ConfigurationErrorCode;
import com.shreeai.os.platform.core.configuration.error.ConfigurationException;
import com.shreeai.os.platform.core.configuration.error.ConfigurationNotFoundException;
import com.shreeai.os.platform.core.configuration.error.DuplicateConfigurationException;
import com.shreeai.os.platform.core.configuration.error.InvalidConfigurationException;

import java.time.Instant;

/**
 * <b>ConfigurationErrorTests</b>
 *
 * <p>Tests for Configuration error architecture within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies ConfigurationException behavior.</li>
 *   <li>Verifies ConfigurationError model.</li>
 *   <li>Verifies all concrete exception types.</li>
 *   <li>Verifies all error codes.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see ConfigurationException
 * @see ConfigurationError
 * @see ConfigurationErrorCode
 */
public class ConfigurationErrorTests {

    // ConfigurationException

    /**
     * Test: ConfigurationException wraps ConfigurationError.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationExceptionWrapsError() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Test error",
                Instant.now(),
                java.util.Map.of()
        );

        // Act
        ConfigurationException exception = new ConfigurationException(error);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(error, exception.error());
        org.junit.jupiter.api.Assertions.assertEquals("Test error", exception.getMessage());
    }

    /**
     * Test: ConfigurationException returns correct error code.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationExceptionReturnsErrorCode() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_DUPLICATE,
                "Duplicate error",
                Instant.now(),
                java.util.Map.of()
        );

        // Act
        ConfigurationException exception = new ConfigurationException(error);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(ConfigurationErrorCode.CONFIGURATION_DUPLICATE, exception.code());
    }

    /**
     * Test: ConfigurationException is a RuntimeException.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationExceptionIsRuntimeException() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Test error",
                Instant.now(),
                java.util.Map.of()
        );

        // Act & Assert
        ConfigurationException exception = new ConfigurationException(error);
        org.junit.jupiter.api.Assertions.assertTrue(exception instanceof RuntimeException);
    }

    /**
     * Test: ConfigurationException can be thrown and caught.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationExceptionCanBeThrown() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Thrown error",
                Instant.now(),
                java.util.Map.of()
        );

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                ConfigurationException.class,
                () -> { throw new ConfigurationException(error); }
        );
    }

    // ConfigurationError

    /**
     * Test: ConfigurationError stores all fields correctly.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationErrorStoresFields() {
        // Arrange
        ConfigurationErrorCode code = ConfigurationErrorCode.CONFIGURATION_INVALID;
        String message = "Test message";
        Instant timestamp = Instant.now();
        java.util.Map<String, Object> details = java.util.Map.of("key", "value");

        // Act
        ConfigurationError error = new ConfigurationError(code, message, timestamp, details);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(code, error.code());
        org.junit.jupiter.api.Assertions.assertEquals(message, error.message());
        org.junit.jupiter.api.Assertions.assertEquals(timestamp, error.timestamp());
        org.junit.jupiter.api.Assertions.assertEquals(details, error.details());
    }

    /**
     * Test: ConfigurationError is immutable.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationErrorIsImmutable() {
        // Arrange
        java.util.Map<String, Object> details = new java.util.HashMap<>();
        details.put("key", "value");

        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Test message",
                Instant.now(),
                details
        );

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> error.details().put("newKey", "newValue")
        );
    }

    /**
     * Test: ConfigurationError equals and hashCode.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationErrorEqualsAndHashCode() {
        // Arrange
        Instant timestamp = Instant.now();
        java.util.Map<String, Object> details = java.util.Map.of("key", "value");

        ConfigurationError error1 = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Test message",
                timestamp,
                details
        );

        ConfigurationError error2 = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Test message",
                timestamp,
                details
        );

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(error1, error2);
        org.junit.jupiter.api.Assertions.assertEquals(error1.hashCode(), error2.hashCode());
    }

    /**
     * Test: ConfigurationError with blank message throws exception.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationErrorWithBlankMessage() {
        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new ConfigurationError(
                        ConfigurationErrorCode.CONFIGURATION_INVALID,
                        "   ",
                        Instant.now(),
                        java.util.Map.of()
                )
        );
    }

    // DuplicateConfigurationException

    /**
     * Test: DuplicateConfigurationException carries the correct error code.
     */
    @org.junit.jupiter.api.Test
    void testDuplicateConfigurationException() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_DUPLICATE,
                "Duplicate key",
                Instant.now(),
                java.util.Map.of()
        );

        // Act
        DuplicateConfigurationException exception = new DuplicateConfigurationException(error);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(ConfigurationErrorCode.CONFIGURATION_DUPLICATE, exception.code());
        org.junit.jupiter.api.Assertions.assertTrue(exception instanceof ConfigurationException);
    }

    // ConfigurationNotFoundException

    /**
     * Test: ConfigurationNotFoundException carries the correct error code.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationNotFoundException() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_NOT_FOUND,
                "Key not found",
                Instant.now(),
                java.util.Map.of()
        );

        // Act
        ConfigurationNotFoundException exception = new ConfigurationNotFoundException(error);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(ConfigurationErrorCode.CONFIGURATION_NOT_FOUND, exception.code());
        org.junit.jupiter.api.Assertions.assertTrue(exception instanceof ConfigurationException);
    }

    // InvalidConfigurationException

    /**
     * Test: InvalidConfigurationException with CONFIGURATION_INVALID code.
     */
    @org.junit.jupiter.api.Test
    void testInvalidConfigurationException() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Invalid configuration",
                Instant.now(),
                java.util.Map.of()
        );

        // Act
        InvalidConfigurationException exception = new InvalidConfigurationException(error);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(ConfigurationErrorCode.CONFIGURATION_INVALID, exception.code());
        org.junit.jupiter.api.Assertions.assertTrue(exception instanceof ConfigurationException);
    }

    /**
     * Test: InvalidConfigurationException with CONFIGURATION_VALIDATION_FAILED code.
     */
    @org.junit.jupiter.api.Test
    void testInvalidConfigurationExceptionForValidationFailure() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_VALIDATION_FAILED,
                "Validation failed",
                Instant.now(),
                java.util.Map.of()
        );

        // Act
        InvalidConfigurationException exception = new InvalidConfigurationException(error);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(ConfigurationErrorCode.CONFIGURATION_VALIDATION_FAILED, exception.code());
    }

    // All Error Codes

    /**
     * Test: All error codes are defined.
     */
    @org.junit.jupiter.api.Test
    void testAllErrorCodesDefined() {
        // Assert
        org.junit.jupiter.api.Assertions.assertNotNull(ConfigurationErrorCode.CONFIGURATION_DUPLICATE);
        org.junit.jupiter.api.Assertions.assertNotNull(ConfigurationErrorCode.CONFIGURATION_NOT_FOUND);
        org.junit.jupiter.api.Assertions.assertNotNull(ConfigurationErrorCode.CONFIGURATION_INVALID);
        org.junit.jupiter.api.Assertions.assertNotNull(ConfigurationErrorCode.CONFIGURATION_VALIDATION_FAILED);
        org.junit.jupiter.api.Assertions.assertNotNull(ConfigurationErrorCode.CONFIGURATION_READ_ONLY);
        org.junit.jupiter.api.Assertions.assertNotNull(ConfigurationErrorCode.CONFIGURATION_NAMESPACE_NOT_FOUND);
        org.junit.jupiter.api.Assertions.assertNotNull(ConfigurationErrorCode.CONFIGURATION_TYPE_MISMATCH);
    }

    /**
     * Test: Error codes have correct count.
     */
    @org.junit.jupiter.api.Test
    void testErrorCodesCount() {
        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(7, ConfigurationErrorCode.values().length);
    }

    /**
     * Test: ConfigurationError toString contains useful information.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationErrorToString() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Test message",
                Instant.now(),
                java.util.Map.of()
        );

        // Act
        String toString = error.toString();

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(toString.contains("CONFIGURATION_INVALID"));
        org.junit.jupiter.api.Assertions.assertTrue(toString.contains("Test message"));
    }

    /**
     * Test: ConfigurationException getMessage returns error message.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationExceptionGetMessage() {
        // Arrange
        ConfigurationError error = new ConfigurationError(
                ConfigurationErrorCode.CONFIGURATION_INVALID,
                "Error message text",
                Instant.now(),
                java.util.Map.of()
        );

        // Act
        ConfigurationException exception = new ConfigurationException(error);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals("Error message text", exception.getMessage());
    }
}
package platform.core.configuration;

import platform.core.configuration.engine.ConfigurationResolutionEngine;
import platform.core.configuration.engine.ResolutionResult;
import platform.core.configuration.model.ConfigurationEntry;
import platform.core.configuration.model.ConfigurationKey;
import platform.core.configuration.model.ConfigurationNamespace;
import platform.core.configuration.model.ConfigurationType;

import java.time.Instant;

/**
 * <b>ConfigurationEngineTests</b>
 *
 * <p>Tests for Configuration Resolution Engine within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies successful resolution.</li>
 *   <li>Verifies null value handling.</li>
 *   <li>Verifies type mismatch handling.</li>
 *   <li>Verifies ResolutionResult behavior.</li>
 *   <li>Verifies stateless behavior.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see ConfigurationResolutionEngine
 * @see ResolutionResult
 */
public class ConfigurationEngineTests {

    private final ConfigurationResolutionEngine engine = new ConfigurationResolutionEngine();

    // Successful resolution

    /**
     * Test: Successful resolution with valid entry.
     */
    @org.junit.jupiter.api.Test
    void testSuccessfulResolution() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "test value",
                "Test description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.success());
        org.junit.jupiter.api.Assertions.assertEquals(entry, result.resolvedEntry());
        org.junit.jupiter.api.Assertions.assertEquals("test value", result.resolvedValue());
    }

    /**
     * Test: Successful resolution with integer value.
     */
    @org.junit.jupiter.api.Test
    void testSuccessfulResolutionWithInteger() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.INTEGER,
                42,
                "Test description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.success());
        org.junit.jupiter.api.Assertions.assertEquals(42, result.resolvedValue());
    }

    /**
     * Test: Successful resolution with boolean value.
     */
    @org.junit.jupiter.api.Test
    void testSuccessfulResolutionWithBoolean() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.BOOLEAN,
                true,
                "Test description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.success());
        org.junit.jupiter.api.Assertions.assertEquals(true, result.resolvedValue());
    }

    /**
     * Test: Successful resolution with double value.
     */
    @org.junit.jupiter.api.Test
    void testSuccessfulResolutionWithDouble() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.DOUBLE,
                3.14,
                "Test description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.success());
        org.junit.jupiter.api.Assertions.assertEquals(3.14, result.resolvedValue());
    }

    // Null value

    /**
     * Test: Null value returns failure.
     */
    @org.junit.jupiter.api.Test
    void testNullValueReturnsFailure() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                null,
                "Test description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.success());
        org.junit.jupiter.api.Assertions.assertNotNull(result.failureMessage());
        org.junit.jupiter.api.Assertions.assertTrue(result.failureMessage().contains("null"));
    }

    // Type mismatch

    /**
     * Test: Type mismatch returns failure.
     */
    @org.junit.jupiter.api.Test
    void testTypeMismatchReturnsFailure() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.INTEGER,
                "not an integer",
                "Test description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.success());
        org.junit.jupiter.api.Assertions.assertNotNull(result.failureMessage());
        org.junit.jupiter.api.Assertions.assertTrue(result.failureMessage().contains("type mismatch"));
    }

    /**
     * Test: Type mismatch with boolean type.
     */
    @org.junit.jupiter.api.Test
    void testTypeMismatchWithBoolean() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.BOOLEAN,
                "not a boolean",
                "Test description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.success());
    }

    /**
     * Test: Type mismatch with double type.
     */
    @org.junit.jupiter.api.Test
    void testTypeMismatchWithDouble() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.DOUBLE,
                "not a double",
                "Test description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.success());
    }

    // ResolutionResult

    /**
     * Test: ResolutionResult success factory method.
     */
    @org.junit.jupiter.api.Test
    void testResolutionResultSuccessFactory() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        Instant timestamp = Instant.now();

        // Act
        ResolutionResult result = ResolutionResult.success(entry, "value", timestamp);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.success());
        org.junit.jupiter.api.Assertions.assertEquals(entry, result.resolvedEntry());
        org.junit.jupiter.api.Assertions.assertEquals("value", result.resolvedValue());
        org.junit.jupiter.api.Assertions.assertNull(result.failureMessage());
        org.junit.jupiter.api.Assertions.assertEquals(timestamp, result.timestamp());
    }

    /**
     * Test: ResolutionResult failure factory method.
     */
    @org.junit.jupiter.api.Test
    void testResolutionResultFailureFactory() {
        // Arrange
        String failureMessage = "Test failure";
        Instant timestamp = Instant.now();

        // Act
        ResolutionResult result = ResolutionResult.failure(failureMessage, timestamp);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.success());
        org.junit.jupiter.api.Assertions.assertNull(result.resolvedEntry());
        org.junit.jupiter.api.Assertions.assertNull(result.resolvedValue());
        org.junit.jupiter.api.Assertions.assertEquals(failureMessage, result.failureMessage());
        org.junit.jupiter.api.Assertions.assertEquals(timestamp, result.timestamp());
    }

    /**
     * Test: ResolutionResult is immutable.
     */
    @org.junit.jupiter.api.Test
    void testResolutionResultIsImmutable() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        Instant timestamp = Instant.now();

        // Act
        ResolutionResult result = ResolutionResult.success(entry, "value", timestamp);

        // Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> result.details().put("key", "value")
        );
    }

    /**
     * Test: ResolutionResult equals and hashCode.
     */
    @org.junit.jupiter.api.Test
    void testResolutionResultEqualsAndHashCode() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );
        Instant timestamp = Instant.now();

        ResolutionResult result1 = ResolutionResult.success(entry, "value", timestamp);
        ResolutionResult result2 = ResolutionResult.success(entry, "value", timestamp);

        // Assert
        org.junit.jupiter.api.Assertions.assertEquals(result1, result2);
        org.junit.jupiter.api.Assertions.assertEquals(result1.hashCode(), result2.hashCode());
    }

    // Stateless behavior

    /**
     * Test: Engine is stateless - multiple calls produce independent results.
     */
    @org.junit.jupiter.api.Test
    void testEngineIsStateless() {
        // Arrange
        ConfigurationKey key1 = new ConfigurationKey("test.key1");
        ConfigurationEntry entry1 = new ConfigurationEntry(
                key1,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value1",
                "Description 1",
                false,
                Instant.now()
        );

        ConfigurationKey key2 = new ConfigurationKey("test.key2");
        ConfigurationEntry entry2 = new ConfigurationEntry(
                key2,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value2",
                "Description 2",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result1 = engine.resolve(key1, entry1);
        ResolutionResult result2 = engine.resolve(key2, entry2);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result1.success());
        org.junit.jupiter.api.Assertions.assertEquals("value1", result1.resolvedValue());
        org.junit.jupiter.api.Assertions.assertTrue(result2.success());
        org.junit.jupiter.api.Assertions.assertEquals("value2", result2.resolvedValue());
    }

    /**
     * Test: Engine can be used concurrently without issues.
     */
    @org.junit.jupiter.api.Test
    void testEngineCanBeUsedConcurrently() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");
        ConfigurationEntry entry = new ConfigurationEntry(
                key,
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(key, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.success());
    }

    /**
     * Test: Resolution with null key returns failure.
     */
    @org.junit.jupiter.api.Test
    void testResolutionWithNullKey() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "Description",
                false,
                Instant.now()
        );

        // Act
        ResolutionResult result = engine.resolve(null, entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.success());
        org.junit.jupiter.api.Assertions.assertNotNull(result.failureMessage());
    }

    /**
     * Test: Resolution with null entry returns failure.
     */
    @org.junit.jupiter.api.Test
    void testResolutionWithNullEntry() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("test.key");

        // Act
        ResolutionResult result = engine.resolve(key, null);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.success());
        org.junit.jupiter.api.Assertions.assertNotNull(result.failureMessage());
    }
}
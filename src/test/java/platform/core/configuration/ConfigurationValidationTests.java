package platform.core.configuration;

import platform.core.configuration.model.ConfigurationEntry;
import platform.core.configuration.model.ConfigurationKey;
import platform.core.configuration.model.ConfigurationNamespace;
import platform.core.configuration.model.ConfigurationType;
import platform.core.configuration.validator.ConfigurationValidator;
import platform.core.registry.validator.ValidationResult;

import java.time.Instant;

/**
 * <b>ConfigurationValidationTests</b>
 *
 * <p>Tests for configuration validation within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Verifies ConfigurationKey validation rules.</li>
 *   <li>Verifies ConfigurationNamespace validation rules.</li>
 *   <li>Verifies ConfigurationEntry validation rules.</li>
 *   <li>Verifies ConfigurationType validation.</li>
 *   <li>Verifies value validation against types.</li>
 *   <li>Verifies ValidationResult behavior.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see ConfigurationValidator
 * @see ValidationResult
 */
public class ConfigurationValidationTests {

    // ConfigurationKey validation - all methods are static

    /**
     * Test: Valid ConfigurationKey passes validation.
     */
    @org.junit.jupiter.api.Test
    void testValidConfigurationKey() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("valid.key");

        // Act
        ValidationResult result = ConfigurationValidator.validateKey(key);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
    }

    /**
     * Test: Null ConfigurationKey fails validation.
     */
    @org.junit.jupiter.api.Test
    void testNullConfigurationKey() {
        // Act
        ValidationResult result = ConfigurationValidator.validateKey(null);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
        org.junit.jupiter.api.Assertions.assertEquals(1, result.errors().size());
    }

    /**
     * Test: ConfigurationKey with leading/trailing spaces fails validation.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationKeyWithLeadingTrailingSpaces() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey(" test.key ");

        // Act
        ValidationResult result = ConfigurationValidator.validateKey(key);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    /**
     * Test: ConfigurationKey exceeding max length fails validation.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationKeyExceedingMaxLength() {
        // Arrange
        String longKey = "a".repeat(129);
        ConfigurationKey key = new ConfigurationKey(longKey);

        // Act
        ValidationResult result = ConfigurationValidator.validateKey(key);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    /**
     * Test: ConfigurationKey with invalid pattern fails validation.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationKeyWithInvalidPattern() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("invalid key!");

        // Act
        ValidationResult result = ConfigurationValidator.validateKey(key);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    // ConfigurationNamespace validation

    /**
     * Test: Valid ConfigurationNamespace passes validation.
     */
    @org.junit.jupiter.api.Test
    void testValidConfigurationNamespace() {
        // Arrange
        ConfigurationNamespace namespace = new ConfigurationNamespace("valid.namespace");

        // Act
        ValidationResult result = ConfigurationValidator.validateNamespace(namespace);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
    }

    /**
     * Test: Null ConfigurationNamespace fails validation.
     */
    @org.junit.jupiter.api.Test
    void testNullConfigurationNamespace() {
        // Act
        ValidationResult result = ConfigurationValidator.validateNamespace(null);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    /**
     * Test: ConfigurationNamespace with spaces fails validation.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationNamespaceWithSpaces() {
        // Arrange
        ConfigurationNamespace namespace = new ConfigurationNamespace("invalid namespace");

        // Act
        ValidationResult result = ConfigurationValidator.validateNamespace(namespace);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    /**
     * Test: ConfigurationNamespace exceeding max length fails validation.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationNamespaceExceedingMaxLength() {
        // Arrange
        String longNamespace = "a".repeat(129);
        ConfigurationNamespace namespace = new ConfigurationNamespace(longNamespace);

        // Act
        ValidationResult result = ConfigurationValidator.validateNamespace(namespace);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    // ConfigurationEntry validation

    /**
     * Test: Valid ConfigurationEntry passes validation.
     */
    @org.junit.jupiter.api.Test
    void testValidConfigurationEntry() {
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
        ValidationResult result = ConfigurationValidator.validateEntry(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
    }

    /**
     * Test: Null ConfigurationEntry fails validation.
     */
    @org.junit.jupiter.api.Test
    void testNullConfigurationEntry() {
        // Act
        ValidationResult result = ConfigurationValidator.validateEntry(null);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    /**
     * Test: ConfigurationEntry with null type throws at construction.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationEntryWithNullType() {
        // Act & Assert - constructor rejects null type via Objects.requireNonNull
        org.junit.jupiter.api.Assertions.assertThrows(
                NullPointerException.class,
                () -> new ConfigurationEntry(
                        new ConfigurationKey("test.key"),
                        new ConfigurationNamespace("test.namespace"),
                        null,
                        "value",
                        "Description",
                        false,
                        Instant.now()
                )
        );
    }

    /**
     * Test: ConfigurationEntry with blank description fails validation.
     */
    @org.junit.jupiter.api.Test
    void testConfigurationEntryWithBlankDescription() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                "value",
                "   ",
                false,
                Instant.now()
        );

        // Act
        ValidationResult result = ConfigurationValidator.validateEntry(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    // Value validation

    /**
     * Test: Valid string value passes validation.
     */
    @org.junit.jupiter.api.Test
    void testValidStringValue() {
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
        ValidationResult result = ConfigurationValidator.validateValue(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
    }

    /**
     * Test: Valid integer value passes validation.
     */
    @org.junit.jupiter.api.Test
    void testValidIntegerValue() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.INTEGER,
                42,
                "Description",
                false,
                Instant.now()
        );

        // Act
        ValidationResult result = ConfigurationValidator.validateValue(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
    }

    /**
     * Test: Valid boolean value passes validation.
     */
    @org.junit.jupiter.api.Test
    void testValidBooleanValue() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.BOOLEAN,
                true,
                "Description",
                false,
                Instant.now()
        );

        // Act
        ValidationResult result = ConfigurationValidator.validateValue(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
    }

    /**
     * Test: Valid double value passes validation.
     */
    @org.junit.jupiter.api.Test
    void testValidDoubleValue() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.DOUBLE,
                3.14,
                "Description",
                false,
                Instant.now()
        );

        // Act
        ValidationResult result = ConfigurationValidator.validateValue(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
    }

    /**
     * Test: Type mismatch fails validation.
     */
    @org.junit.jupiter.api.Test
    void testTypeMismatchFailsValidation() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.INTEGER,
                "not an integer",
                "Description",
                false,
                Instant.now()
        );

        // Act
        ValidationResult result = ConfigurationValidator.validateValue(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
    }

    /**
     * Test: Null value passes validation for any type.
     */
    @org.junit.jupiter.api.Test
    void testNullValuePassesValidation() {
        // Arrange
        ConfigurationEntry entry = new ConfigurationEntry(
                new ConfigurationKey("test.key"),
                new ConfigurationNamespace("test.namespace"),
                ConfigurationType.STRING,
                null,
                "Description",
                false,
                Instant.now()
        );

        // Act
        ValidationResult result = ConfigurationValidator.validateValue(entry);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
    }

    // ValidationResult

    /**
     * Test: ValidationResult for valid case has no errors.
     */
    @org.junit.jupiter.api.Test
    void testValidValidationResultHasNoErrors() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("valid.key");

        // Act
        ValidationResult result = ConfigurationValidator.validateKey(key);

        // Assert
        org.junit.jupiter.api.Assertions.assertTrue(result.isValid());
        org.junit.jupiter.api.Assertions.assertTrue(result.errors().isEmpty());
    }

    /**
     * Test: ValidationResult for invalid case has errors.
     */
    @org.junit.jupiter.api.Test
    void testInvalidValidationResultHasErrors() {
        // Arrange
        ConfigurationKey key = new ConfigurationKey("invalid key!");

        // Act
        ValidationResult result = ConfigurationValidator.validateKey(key);

        // Assert
        org.junit.jupiter.api.Assertions.assertFalse(result.isValid());
        org.junit.jupiter.api.Assertions.assertFalse(result.errors().isEmpty());
    }
}
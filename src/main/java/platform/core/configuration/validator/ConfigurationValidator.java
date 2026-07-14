package platform.core.configuration.validator;

import platform.core.configuration.model.ConfigurationEntry;
import platform.core.configuration.model.ConfigurationKey;
import platform.core.configuration.model.ConfigurationNamespace;
import platform.core.configuration.model.ConfigurationType;
import platform.core.registry.validator.ValidationResult;

import java.util.regex.Pattern;

/**
 * <b>ConfigurationValidator</b>
 *
 * <p>Validates the Platform Language for the Configuration Service within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates configuration keys, namespaces, and entries.</li>
 *   <li>Protects the Platform Language from invalid configurations.</li>
 *   <li>Never mutates models — validation is pure and read-only.</li>
 *   <li>Never stores configuration — validation is stateless.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Engineering Principle:</b> Validation protects Platform Language.
 * Validation never stores configuration. Validation never changes configuration.</p>
 *
 * @see ValidationResult
 */
public final class ConfigurationValidator {

    private static final Pattern KEY_NAMESPACE_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final int MAX_LENGTH = 128;

    /**
     * Constructs a new {@code ConfigurationValidator}.
     */
    public ConfigurationValidator() {
    }

    /**
     * Validates a configuration key.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Must not be null</li>
     *   <li>Must not be blank</li>
     *   <li>Must not have leading/trailing spaces</li>
     *   <li>Must not exceed 128 characters</li>
     *   <li>Must match pattern: ^[a-zA-Z0-9._-]+$</li>
     * </ul>
     *
     * @param key the configuration key to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateKey(ConfigurationKey key) {
        if (key == null) {
            return ValidationResult.builder()
                    .addError("ConfigurationKey must not be null")
                    .build();
        }

        String value = key.value();

        if (value.isBlank()) {
            return ValidationResult.builder()
                    .addError("ConfigurationKey must not be blank")
                    .build();
        }

        if (!value.equals(value.trim())) {
            return ValidationResult.builder()
                    .addError("ConfigurationKey must not have leading or trailing spaces")
                    .build();
        }

        if (value.length() > MAX_LENGTH) {
            return ValidationResult.builder()
                    .addError("ConfigurationKey must not exceed " + MAX_LENGTH + " characters")
                    .build();
        }

        if (!KEY_NAMESPACE_PATTERN.matcher(value).matches()) {
            return ValidationResult.builder()
                    .addError("ConfigurationKey must match pattern ^[a-zA-Z0-9._-]+$")
                    .build();
        }

        return ValidationResult.builder().build();
    }

    /**
     * Validates a configuration namespace.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Must not be null</li>
     *   <li>Must not be blank</li>
     *   <li>Must not have spaces</li>
     *   <li>Must not exceed 128 characters</li>
     *   <li>Must match pattern: ^[a-zA-Z0-9._-]+$</li>
     * </ul>
     *
     * @param namespace the configuration namespace to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateNamespace(ConfigurationNamespace namespace) {
        if (namespace == null) {
            return ValidationResult.builder()
                    .addError("ConfigurationNamespace must not be null")
                    .build();
        }

        String value = namespace.value();

        if (value.isBlank()) {
            return ValidationResult.builder()
                    .addError("ConfigurationNamespace must not be blank")
                    .build();
        }

        if (value.contains(" ")) {
            return ValidationResult.builder()
                    .addError("ConfigurationNamespace must not contain spaces")
                    .build();
        }

        if (value.length() > MAX_LENGTH) {
            return ValidationResult.builder()
                    .addError("ConfigurationNamespace must not exceed " + MAX_LENGTH + " characters")
                    .build();
        }

        if (!KEY_NAMESPACE_PATTERN.matcher(value).matches()) {
            return ValidationResult.builder()
                    .addError("ConfigurationNamespace must match pattern ^[a-zA-Z0-9._-]+$")
                    .build();
        }

        return ValidationResult.builder().build();
    }

    /**
     * Validates a configuration entry.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Key must exist</li>
     *   <li>Namespace must exist</li>
     *   <li>Type must exist</li>
     *   <li>Description must exist</li>
     *   <li>CreatedAt must exist</li>
     * </ul>
     *
     * @param entry the configuration entry to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateEntry(ConfigurationEntry entry) {
        if (entry == null) {
            return ValidationResult.builder()
                    .addError("ConfigurationEntry must not be null")
                    .build();
        }

        ValidationResult result = validateKey(entry.key());
        if (!result.isValid()) {
            return result;
        }

        result = validateNamespace(entry.namespace());
        if (!result.isValid()) {
            return result;
        }

        if (entry.type() == null) {
            return ValidationResult.builder()
                    .addError("ConfigurationType must not be null")
                    .build();
        }

        if (entry.description() == null || entry.description().isBlank()) {
            return ValidationResult.builder()
                    .addError("Configuration description must not be null or blank")
                    .build();
        }

        if (entry.createdAt() == null) {
            return ValidationResult.builder()
                    .addError("Configuration createdAt must not be null")
                    .build();
        }

        return ValidationResult.builder().build();
    }

    /**
     * Validates the value of a configuration entry against its type.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>STRING → value must be String</li>
     *   <li>INTEGER → value must be Integer</li>
     *   <li>BOOLEAN → value must be Boolean</li>
     *   <li>DOUBLE → value must be Double</li>
     *   <li>LIST → value must be List</li>
     *   <li>MAP → value must be Map</li>
     * </ul>
     *
     * <p>Note: null values are allowed for all types.</p>
     *
     * @param entry the configuration entry to validate (may be null)
     * @return the validation result
     */
    public static ValidationResult validateValue(ConfigurationEntry entry) {
        if (entry == null) {
            return ValidationResult.builder()
                    .addError("ConfigurationEntry must not be null")
                    .build();
        }

        if (entry.type() == null) {
            return ValidationResult.builder()
                    .addError("ConfigurationType must not be null")
                    .build();
        }

        Object value = entry.value();

        // null values are allowed
        if (value == null) {
            return ValidationResult.builder().build();
        }

        switch (entry.type()) {
            case STRING:
                if (!(value instanceof String)) {
                    return ValidationResult.builder()
                            .addError("Configuration value must be String for type STRING")
                            .build();
                }
                break;
            case INTEGER:
                if (!(value instanceof Integer)) {
                    return ValidationResult.builder()
                            .addError("Configuration value must be Integer for type INTEGER")
                            .build();
                }
                break;
            case BOOLEAN:
                if (!(value instanceof Boolean)) {
                    return ValidationResult.builder()
                            .addError("Configuration value must be Boolean for type BOOLEAN")
                            .build();
                }
                break;
            case DOUBLE:
                if (!(value instanceof Double)) {
                    return ValidationResult.builder()
                            .addError("Configuration value must be Double for type DOUBLE")
                            .build();
                }
                break;
            case LIST:
                if (!(value instanceof java.util.List)) {
                    return ValidationResult.builder()
                            .addError("Configuration value must be List for type LIST")
                            .build();
                }
                break;
            case MAP:
                if (!(value instanceof java.util.Map)) {
                    return ValidationResult.builder()
                            .addError("Configuration value must be Map for type MAP")
                            .build();
                }
                break;
            default:
                return ValidationResult.builder()
                        .addError("Unknown ConfigurationType: " + entry.type())
                        .build();
        }

        return ValidationResult.builder().build();
    }
}
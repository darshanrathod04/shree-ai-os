package platform.core.configuration.engine;

import platform.core.configuration.model.ConfigurationEntry;
import platform.core.configuration.model.ConfigurationKey;

import java.time.Instant;

/**
 * <b>ConfigurationResolutionEngine</b>
 *
 * <p>Resolves effective configuration values within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Resolves configuration values from entries.</li>
 *   <li>Validates resolved values against types.</li>
 *   <li>Provides resolution results with success/failure status.</li>
 *   <li>Never stores configuration — pure resolution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Engineering Principle:</b> Service owns storage. Engine owns resolution.
 * Validator owns validation. Responsibilities SHALL remain separated forever.</p>
 *
 * @see ResolutionResult
 */
public final class ConfigurationResolutionEngine {

    /**
     * Private constructor to prevent instantiation.
     */
    private ConfigurationResolutionEngine() {
    }

    /**
     * Resolves a configuration entry to its effective value.
     *
     * <p>Resolution rules:</p>
     * <ol>
     *   <li>If entry exists, return success=true</li>
     *   <li>If value is null, return success=false with failure message</li>
     *   <li>If type mismatch, return success=false with failure message</li>
     * </ol>
     *
     * @param key the configuration key (must not be null)
     * @param entry the configuration entry (must not be null)
     * @return the resolution result
     */
    public static ResolutionResult resolve(ConfigurationKey key, ConfigurationEntry entry) {
        if (key == null || entry == null) {
            return ResolutionResult.failure(
                    "ConfigurationKey and ConfigurationEntry must not be null",
                    Instant.now()
            );
        }

        // Resolution rule 1: If entry exists, return success
        // (entry is provided, so it exists)

        // Get the value
        Object value = entry.value();

        // Resolution rule 2: If value is null, return failure
        if (value == null) {
            return ResolutionResult.failure(
                    "Configuration value is null for key: " + key.value(),
                    Instant.now()
            );
        }

        // Resolution rule 3: Validate type match
        if (!isTypeMatch(entry.type(), value)) {
            return ResolutionResult.failure(
                    "Configuration value type mismatch for key: " + key.value() +
                            ". Expected type: " + entry.type() +
                            ", actual type: " + value.getClass().getName(),
                    Instant.now()
            );
        }

        // All checks passed, return success
        return ResolutionResult.success(entry, value, Instant.now());
    }

    /**
     * Checks if a value matches the expected configuration type.
     *
     * @param type the configuration type (must not be null)
     * @param value the value to check (must not be null)
     * @return {@code true} if the value matches the type, {@code false} otherwise
     */
    private static boolean isTypeMatch(platform.core.configuration.model.ConfigurationType type, Object value) {
        if (type == null || value == null) {
            return false;
        }

        return switch (type) {
            case STRING -> value instanceof String;
            case INTEGER -> value instanceof Integer;
            case BOOLEAN -> value instanceof Boolean;
            case DOUBLE -> value instanceof Double;
            case LIST -> value instanceof java.util.List;
            case MAP -> value instanceof java.util.Map;
        };
    }
}
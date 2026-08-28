package com.shreeai.os.platform.core.configuration.error;

/**
 * <b>InvalidConfigurationException</b>
 *
 * <p>Thrown when a configuration fails validation within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Indicates that a configuration is invalid or failed validation.</li>
 *   <li>Wraps a {@link ConfigurationError} with code {@link ConfigurationErrorCode#CONFIGURATION_INVALID}
 *       or {@link ConfigurationErrorCode#CONFIGURATION_VALIDATION_FAILED}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see ConfigurationErrorCode#CONFIGURATION_INVALID
 * @see ConfigurationErrorCode#CONFIGURATION_VALIDATION_FAILED
 * @see ConfigurationException
 */
public class InvalidConfigurationException extends ConfigurationException {

    /**
     * Constructs a new {@code InvalidConfigurationException} with the given error.
     *
     * @param error the configuration error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public InvalidConfigurationException(ConfigurationError error) {
        super(error);
    }
}
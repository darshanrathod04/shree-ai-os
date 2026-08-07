package com.shreeai.os.platform.core.configuration.error;

/**
 * <b>ConfigurationException</b>
 *
 * <p>The base runtime exception for all Configuration errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the base exception type for all Configuration errors.</li>
 *   <li>Wraps a {@link ConfigurationError} to provide structured error information.</li>
 *   <li>Enables consistent exception handling across the Configuration subsystem.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Note:</b> This SHALL become the ONLY base exception for the Configuration subsystem.
 * All concrete exceptions extend this class.</p>
 *
 * @see ConfigurationError
 * @see DuplicateConfigurationException
 * @see ConfigurationNotFoundException
 * @see InvalidConfigurationException
 */
public class ConfigurationException extends RuntimeException {

    private final ConfigurationError error;

    /**
     * Constructs a new {@code ConfigurationException} with the given error.
     *
     * @param error the configuration error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public ConfigurationException(ConfigurationError error) {
        super(error.message());
        this.error = error;
    }

    /**
     * Returns the configuration error associated with this exception.
     *
     * @return the configuration error
     */
    public ConfigurationError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public ConfigurationErrorCode code() {
        return error.code();
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    @Override
    public String getMessage() {
        return error.message();
    }
}
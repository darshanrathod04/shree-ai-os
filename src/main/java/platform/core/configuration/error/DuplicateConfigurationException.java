package platform.core.configuration.error;

/**
 * <b>DuplicateConfigurationException</b>
 *
 * <p>Thrown when attempting to register a configuration that already exists within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Indicates that a configuration with the same key already exists.</li>
 *   <li>Wraps a {@link ConfigurationError} with code {@link ConfigurationErrorCode#CONFIGURATION_DUPLICATE}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see ConfigurationErrorCode#CONFIGURATION_DUPLICATE
 * @see ConfigurationException
 */
public class DuplicateConfigurationException extends ConfigurationException {

    /**
     * Constructs a new {@code DuplicateConfigurationException} with the given error.
     *
     * @param error the configuration error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public DuplicateConfigurationException(ConfigurationError error) {
        super(error);
    }
}
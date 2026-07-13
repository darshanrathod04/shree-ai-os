package platform.core.configuration.error;

/**
 * <b>ConfigurationNotFoundException</b>
 *
 * <p>Thrown when a requested configuration is not found within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Indicates that the requested configuration does not exist.</li>
 *   <li>Wraps a {@link ConfigurationError} with code {@link ConfigurationErrorCode#CONFIGURATION_NOT_FOUND}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * @see ConfigurationErrorCode#CONFIGURATION_NOT_FOUND
 * @see ConfigurationException
 */
public class ConfigurationNotFoundException extends ConfigurationException {

    /**
     * Constructs a new {@code ConfigurationNotFoundException} with the given error.
     *
     * @param error the configuration error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public ConfigurationNotFoundException(ConfigurationError error) {
        super(error);
    }
}
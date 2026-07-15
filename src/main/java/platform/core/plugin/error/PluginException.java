package platform.core.plugin.error;

/**
 * <b>PluginException</b>
 *
 * <p>The base runtime exception for all Plugin errors within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Provides the base exception type for all Plugin errors.</li>
 *   <li>Wraps a {@link PluginError} to provide structured error information.</li>
 *   <li>Enables consistent exception handling across the Plugin subsystem.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Note:</b> This SHALL become the ONLY base exception for the Plugin subsystem.
 * All concrete exceptions extend this class.</p>
 *
 * @see PluginError
 * @see DuplicatePluginException
 * @see PluginNotFoundException
 * @see InvalidPluginException
 */
public class PluginException extends RuntimeException {

    private final PluginError error;

    /**
     * Constructs a new {@code PluginException} with the given error.
     *
     * @param error the plugin error (must not be null)
     * @throws IllegalArgumentException if {@code error} is {@code null}
     */
    public PluginException(PluginError error) {
        super(error.message());
        this.error = error;
    }

    /**
     * Returns the plugin error associated with this exception.
     *
     * @return the plugin error
     */
    public PluginError error() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public PluginErrorCode code() {
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
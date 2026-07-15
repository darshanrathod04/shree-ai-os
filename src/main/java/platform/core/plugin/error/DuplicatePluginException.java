package platform.core.plugin.error;

import platform.core.plugin.model.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>DuplicatePluginException</b>
 *
 * <p>Thrown when attempting to register a plugin that is already registered.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a plugin registration failed due to duplicate.</li>
 *   <li>Provides structured error information via {@link PluginError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see PluginException
 * @see PluginError
 * @see PluginErrorCode#PLUGIN_DUPLICATE
 */
public class DuplicatePluginException extends PluginException {

    /**
     * Constructs a new {@code DuplicatePluginException} for the given plugin.
     *
     * @param plugin the plugin that is already registered (must not be null)
     */
    public DuplicatePluginException(Plugin plugin) {
        this(plugin, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code DuplicatePluginException} for the given plugin with details.
     *
     * @param plugin the plugin that is already registered (must not be null)
     * @param details additional error details (must not be null)
     */
    public DuplicatePluginException(Plugin plugin, Map<String, Object> details) {
        super(createError(plugin, details));
    }

    private static PluginError createError(Plugin plugin, Map<String, Object> details) {
        String message = "Plugin already registered: " + plugin.id().value();
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("pluginId", plugin.id().value());
        errorDetails.put("pluginName", plugin.name());
        errorDetails.put("pluginVersion", plugin.version());
        return new PluginError(
                PluginErrorCode.PLUGIN_DUPLICATE,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}
package com.shreeai.os.platform.core.plugin.error;

import com.shreeai.os.platform.core.plugin.model.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>PluginNotFoundException</b>
 *
 * <p>Thrown when a requested plugin is not found in the Plugin registry.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a plugin lookup failed.</li>
 *   <li>Provides structured error information via {@link PluginError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see PluginException
 * @see PluginError
 * @see PluginErrorCode#PLUGIN_NOT_FOUND
 */
public class PluginNotFoundException extends PluginException {

    /**
     * Constructs a new {@code PluginNotFoundException} for the given plugin.
     *
     * @param plugin the plugin that was not found (must not be null)
     */
    public PluginNotFoundException(Plugin plugin) {
        this(plugin, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code PluginNotFoundException} for the given plugin with details.
     *
     * @param plugin the plugin that was not found (must not be null)
     * @param details additional error details (must not be null)
     */
    public PluginNotFoundException(Plugin plugin, Map<String, Object> details) {
        super(createError(plugin, details));
    }

    private static PluginError createError(Plugin plugin, Map<String, Object> details) {
        String message = "Plugin not found: " + plugin.id().value();
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("pluginId", plugin.id().value());
        errorDetails.put("pluginName", plugin.name());
        errorDetails.put("pluginVersion", plugin.version());
        return new PluginError(
                PluginErrorCode.PLUGIN_NOT_FOUND,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}
package com.shreeai.os.platform.core.plugin.error;

import com.shreeai.os.platform.core.plugin.model.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>InvalidPluginException</b>
 *
 * <p>Thrown when a plugin fails validation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Signals that a plugin validation failed.</li>
 *   <li>Provides structured error information via {@link PluginError}.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see PluginException
 * @see PluginError
 * @see PluginErrorCode#PLUGIN_INVALID
 */
public class InvalidPluginException extends PluginException {

    /**
     * Constructs a new {@code InvalidPluginException} for the given plugin with a reason.
     *
     * @param plugin the plugin that is invalid (must not be null)
     * @param reason the validation failure reason (must not be null or blank)
     */
    public InvalidPluginException(Plugin plugin, String reason) {
        this(plugin, reason, Collections.emptyMap());
    }

    /**
     * Constructs a new {@code InvalidPluginException} for the given plugin with reason and details.
     *
     * @param plugin the plugin that is invalid (must not be null)
     * @param reason the validation failure reason (must not be null or blank)
     * @param details additional error details (must not be null)
     */
    public InvalidPluginException(Plugin plugin, String reason, Map<String, Object> details) {
        super(createError(plugin, reason, details));
    }

    private static PluginError createError(Plugin plugin, String reason, Map<String, Object> details) {
        String message = "Invalid plugin: " + reason;
        Map<String, Object> errorDetails = new HashMap<>(details != null ? details : new HashMap<>());
        errorDetails.put("pluginId", plugin.id().value());
        errorDetails.put("pluginName", plugin.name());
        errorDetails.put("pluginVersion", plugin.version());
        errorDetails.put("reason", reason);
        return new PluginError(
                PluginErrorCode.PLUGIN_INVALID,
                message,
                java.time.Instant.now(),
                errorDetails
        );
    }
}
package com.shreeai.os.platform.core.plugin.validator;

import com.shreeai.os.platform.core.plugin.model.Plugin;
import com.shreeai.os.platform.core.plugin.model.PluginDescriptor;
import com.shreeai.os.platform.core.plugin.model.PluginId;
import com.shreeai.os.platform.core.plugin.model.PluginRequest;
import com.shreeai.os.platform.core.plugin.model.PluginState;
import com.shreeai.os.platform.core.registry.validator.ValidationResult;

/**
 * <b>PluginValidator</b>
 *
 * <p>Stateless validator that ensures every Plugin model satisfies the architectural
 * requirements before being used by the Plugin Framework.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates that Plugin models meet all structural requirements.</li>
 *   <li>Answers the question: "Is this Plugin model valid?" — it never loads or executes plugins.</li>
 *   <li>Returns structured {@link ValidationResult} supporting multiple errors in a single execution.</li>
 *   <li>Enforces the architectural invariants defined in ADD-PLT-301.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Design Constraints:</b></p>
 * <ul>
 *   <li>Stateless — all state is passed as method parameters.</li>
 *   <li>Deterministic — same inputs always produce the same result.</li>
 *   <li>No business logic — validation rules only.</li>
 *   <li>No model mutation — models are never modified.</li>
 *   <li>No plugin loading — never loads, starts, or executes plugins.</li>
 *   <li>No external access — never accesses Registry, Lifecycle, Event Bus, Configuration, or databases.</li>
 * </ul>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-301</p>
 *
 * @see ValidationResult
 * @see PluginId
 * @see Plugin
 * @see PluginDescriptor
 * @see PluginRequest
 * @see PluginState
 */
public final class PluginValidator {

    /**
     * Constructs a new {@code PluginValidator}.
     * Public to allow test instantiation.
     */
    public PluginValidator() {
    }

    /**
     * Validates a {@link PluginId}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>PluginId must not be null</li>
     *   <li>Value must not be null or blank</li>
     * </ul>
     *
     * @param pluginId the plugin identifier to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code pluginId} is null
     */
    public static ValidationResult validatePluginId(PluginId pluginId) {
        if (pluginId == null) {
            throw new NullPointerException("PluginId must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        String value = pluginId.value();
        if (value == null || value.isBlank()) {
            builder.addError("PluginId value must not be null or blank");
        }

        return builder.build();
    }

    /**
     * Validates a {@link Plugin}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Plugin must not be null</li>
     *   <li>Id must not be null</li>
     *   <li>Name must not be null or blank</li>
     *   <li>Version must not be null or blank</li>
     * </ul>
     *
     * @param plugin the plugin to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code plugin} is null
     */
    public static ValidationResult validatePlugin(Plugin plugin) {
        if (plugin == null) {
            throw new NullPointerException("Plugin must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Id must not be null
        PluginId id = plugin.id();
        if (id == null) {
            builder.addError("Plugin id must not be null");
        }

        // Name must not be null or blank
        String name = plugin.name();
        if (name == null || name.isBlank()) {
            builder.addError("Plugin name must not be null or blank");
        }

        // Version must not be null or blank
        String version = plugin.version();
        if (version == null || version.isBlank()) {
            builder.addError("Plugin version must not be null or blank");
        }

        return builder.build();
    }

    /**
     * Validates a {@link PluginDescriptor}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Descriptor must not be null</li>
     *   <li>Plugin must not be null</li>
     *   <li>State must not be null</li>
     *   <li>LoadedAt must not be null</li>
     *   <li>Provider must not be null or blank</li>
     * </ul>
     *
     * @param descriptor the plugin descriptor to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code descriptor} is null
     */
    public static ValidationResult validateDescriptor(PluginDescriptor descriptor) {
        if (descriptor == null) {
            throw new NullPointerException("PluginDescriptor must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Plugin must not be null
        Plugin plugin = descriptor.plugin();
        if (plugin == null) {
            builder.addError("PluginDescriptor plugin must not be null");
        }

        // State must not be null
        PluginState state = descriptor.state();
        if (state == null) {
            builder.addError("PluginDescriptor state must not be null");
        }

        // LoadedAt must not be null
        if (descriptor.loadedAt() == null) {
            builder.addError("PluginDescriptor loadedAt must not be null");
        }

        // Provider must not be null or blank
        String provider = descriptor.provider();
        if (provider == null || provider.isBlank()) {
            builder.addError("PluginDescriptor provider must not be null or blank");
        }

        return builder.build();
    }

    /**
     * Validates a {@link PluginRequest}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Request must not be null</li>
     *   <li>Plugin must not be null</li>
     * </ul>
     *
     * <p><b>Note:</b> The force flag is not validated as it is a boolean primitive.</p>
     *
     * @param request the plugin request to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code request} is null
     */
    public static ValidationResult validateRequest(PluginRequest request) {
        if (request == null) {
            throw new NullPointerException("PluginRequest must not be null");
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // Plugin must not be null
        if (request.plugin() == null) {
            builder.addError("PluginRequest plugin must not be null");
        }

        return builder.build();
    }

    /**
     * Validates a {@link PluginState}.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>State must not be null</li>
     * </ul>
     *
     * @param state the plugin state to validate
     * @return a {@link ValidationResult} containing any errors or warnings
     * @throws NullPointerException if {@code state} is null
     */
    public static ValidationResult validateState(PluginState state) {
        if (state == null) {
            throw new NullPointerException("PluginState must not be null");
        }

        return ValidationResult.builder().build();
    }
}
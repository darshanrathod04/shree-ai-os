package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.exceptions.ConfigurationException;
import com.shreeai.os.platform.runtime.api.Runtime;

import java.util.Objects;

/**
 * <b>ShreeBuilder</b>
 *
 * <p>Builder for creating ShreeAI instances.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public final class ShreeBuilder {

    private String apiKey = "local";
    private Runtime runtime;
    private SDKConfiguration configuration;

    ShreeBuilder() {
        // Package-private constructor
    }

    /**
     * Sets the API key.
     *
     * @param apiKey the API key
     * @return this builder
     */
    public ShreeBuilder apiKey(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
        return this;
    }

    /**
     * Sets the runtime instance.
     *
     * @param runtime the runtime
     * @return this builder
     */
    public ShreeBuilder runtime(Runtime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
        return this;
    }

    /**
     * Sets the SDK configuration.
     *
     * @param configuration the configuration
     * @return this builder
     */
    public ShreeBuilder configuration(SDKConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
        return this;
    }

    /**
     * Builds a new ShreeAI instance.
     *
     * @return a new ShreeAI instance
     * @throws ConfigurationException if the configuration is invalid
     */
    public ShreeAI build() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ConfigurationException("apiKey must not be null or blank");
        }

        SDKConfiguration effectiveConfig = configuration != null
                ? configuration
                : SDKConfiguration.builder().apiKey(apiKey).build();

        return new ShreeAI(effectiveConfig, runtime);
    }
}
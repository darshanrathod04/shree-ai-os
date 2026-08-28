package com.shreeai.os.platform.runtime.config;

import com.shreeai.os.platform.runtime.api.Runtime;

/**
 * <b>RuntimeConfiguration</b>
 *
 * <p>Immutable configuration for a {@link Runtime} instance.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Holds all configuration parameters required to initialize a Runtime.</li>
 *   <li>Provides sensible defaults for optional parameters.</li>
 *   <li>Is immutable after construction to ensure thread-safe publication.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel</p>
 *
 * @see RuntimeConfigurationBuilder
 */
public final class RuntimeConfiguration {

    private final String runtimeName;
    private final int maxConcurrentSessions;
    private final long sessionTimeoutMillis;
    private final boolean autoStartEnabled;

    private RuntimeConfiguration(Builder builder) {
        this.runtimeName = builder.runtimeName;
        this.maxConcurrentSessions = builder.maxConcurrentSessions;
        this.sessionTimeoutMillis = builder.sessionTimeoutMillis;
        this.autoStartEnabled = builder.autoStartEnabled;
    }

    /**
     * Returns the name of this Runtime instance.
     *
     * @return the runtime name
     */
    public String runtimeName() {
        return runtimeName;
    }

    /**
     * Returns the maximum number of concurrent execution sessions allowed.
     *
     * @return max concurrent sessions
     */
    public int maxConcurrentSessions() {
        return maxConcurrentSessions;
    }

    /**
     * Returns the session timeout in milliseconds.
     *
     * @return session timeout in milliseconds
     */
    public long sessionTimeoutMillis() {
        return sessionTimeoutMillis;
    }

    /**
     * Returns whether auto-start is enabled for this Runtime.
     *
     * @return true if auto-start is enabled
     */
    public boolean autoStartEnabled() {
        return autoStartEnabled;
    }

    /**
     * Creates a new builder for RuntimeConfiguration.
     *
     * @return a new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent builder for {@link RuntimeConfiguration}.
     */
    public static final class Builder {

        private String runtimeName = "default-runtime";
        private int maxConcurrentSessions = 10;
        private long sessionTimeoutMillis = 300_000L;
        private boolean autoStartEnabled = true;

        private Builder() {
        }

        /**
         * Sets the runtime name.
         *
         * @param runtimeName the runtime name
         * @return this builder
         */
        public Builder runtimeName(String runtimeName) {
            this.runtimeName = runtimeName;
            return this;
        }

        /**
         * Sets the maximum number of concurrent sessions.
         *
         * @param maxConcurrentSessions max concurrent sessions
         * @return this builder
         */
        public Builder maxConcurrentSessions(int maxConcurrentSessions) {
            this.maxConcurrentSessions = maxConcurrentSessions;
            return this;
        }

        /**
         * Sets the session timeout in milliseconds.
         *
         * @param sessionTimeoutMillis timeout in milliseconds
         * @return this builder
         */
        public Builder sessionTimeoutMillis(long sessionTimeoutMillis) {
            this.sessionTimeoutMillis = sessionTimeoutMillis;
            return this;
        }

        /**
         * Enables or disables auto-start.
         *
         * @param autoStartEnabled whether auto-start is enabled
         * @return this builder
         */
        public Builder autoStartEnabled(boolean autoStartEnabled) {
            this.autoStartEnabled = autoStartEnabled;
            return this;
        }

        /**
         * Builds a new RuntimeConfiguration.
         *
         * @return a new configuration instance
         */
        public RuntimeConfiguration build() {
            return new RuntimeConfiguration(this);
        }
    }
}
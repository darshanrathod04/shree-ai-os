package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.exceptions.ConfigurationException;
import com.shreeai.os.platform.sdk.version.SDKVersion;

import java.util.Locale;
import java.util.Objects;

/**
 * <b>SDKConfiguration</b>
 *
 * <p>Immutable configuration for the Shree AI OS SDK.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public final class SDKConfiguration {

    private final String apiKey;
    private final long timeout;
    private final Locale locale;
    private final boolean debug;
    private final String runtimeMode;
    private final String version;

    private SDKConfiguration(Builder builder) {
        this.apiKey = builder.apiKey;
        this.timeout = builder.timeout;
        this.locale = builder.locale;
        this.debug = builder.debug;
        this.runtimeMode = builder.runtimeMode;
        this.version = SDKVersion.VERSION;
    }

    public String apiKey() { return apiKey; }
    public long timeout() { return timeout; }
    public Locale locale() { return locale; }
    public boolean debug() { return debug; }
    public String runtimeMode() { return runtimeMode; }
    public String version() { return version; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String apiKey = "local";
        private long timeout = 30_000L;
        private Locale locale = Locale.getDefault();
        private boolean debug = false;
        private String runtimeMode = "LOCAL";

        private Builder() {}

        public Builder apiKey(String apiKey) {
            this.apiKey = Objects.requireNonNull(apiKey, "apiKey must not be null");
            return this;
        }

        public Builder timeout(long timeout) {
            if (timeout <= 0) {
                throw new ConfigurationException("timeout must be positive");
            }
            this.timeout = timeout;
            return this;
        }

        public Builder locale(Locale locale) {
            this.locale = Objects.requireNonNull(locale, "locale must not be null");
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public Builder runtimeMode(String runtimeMode) {
            this.runtimeMode = Objects.requireNonNull(runtimeMode, "runtimeMode must not be null");
            return this;
        }

        public SDKConfiguration build() {
            return new SDKConfiguration(this);
        }
    }
}
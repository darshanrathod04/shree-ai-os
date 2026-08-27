package com.shreeai.os.platform.llm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, canonical request contract for the Shree AI OS LLM provider SPI.
 *
 * <p>This is the single source of truth for everything a provider needs to
 * generate (or stream) a completion. It is intentionally framework-agnostic:
 * no Spring, no legacy types, so it can be exercised by unit tests without a
 * container and reused by every {@link LlmProvider} implementation.</p>
 *
 * <p>Design notes:</p>
 * <ul>
 *   <li>Streaming-first: {@code stream} defaults to {@code true} because the
 *       SPI contract is token streaming. Providers may ignore it for blocking
 *       convenience methods.</li>
 *   <li>Unknown options are forwarded verbatim via {@link #options()} so providers
 *       can read provider-specific parameters without the model growing on every
 *       new field.</li>
 * </ul>
 *
 * @since Sprint 6.2A-P1
 */
public final class LlmRequest {

    private final String model;
    private final String prompt;
    private final Double temperature;
    private final Integer maxTokens;
    private final Boolean stream;
    private final Map<String, Object> options;

    private LlmRequest(Builder builder) {
        this.model = Objects.requireNonNull(builder.model, "model must not be null");
        this.prompt = builder.prompt == null ? "" : builder.prompt;
        this.temperature = builder.temperature;
        this.maxTokens = builder.maxTokens;
        this.stream = builder.stream == null ? Boolean.TRUE : builder.stream;
        this.options = Collections.unmodifiableMap(new LinkedHashMap<>(builder.options));
    }

    /** Canonical builder entry point. */
    public static Builder builder() {
        return new Builder();
    }

    public String model() {
        return model;
    }

    public String prompt() {
        return prompt;
    }

    public Double temperature() {
        return temperature;
    }

    public Integer maxTokens() {
        return maxTokens;
    }

    public Boolean stream() {
        return stream;
    }

    /**
     * Provider-specific options. Never {@code null}, always an unmodifiable map.
     * Reserved keys used by this model: {@code temperature}, {@code maxTokens},
     * {@code stream}. All other keys are forwarded untouched.
     */
    public Map<String, Object> options() {
        return options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LlmRequest that)) {
            return false;
        }
        return Objects.equals(model, that.model)
                && Objects.equals(prompt, that.prompt)
                && Objects.equals(temperature, that.temperature)
                && Objects.equals(maxTokens, that.maxTokens)
                && Objects.equals(stream, that.stream)
                && Objects.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, prompt, temperature, maxTokens, stream, options);
    }

    @Override
    public String toString() {
        return "LlmRequest{"
                + "model='" + model + '\''
                + ", promptLength=" + (prompt == null ? 0 : prompt.length())
                + ", temperature=" + temperature
                + ", maxTokens=" + maxTokens
                + ", stream=" + stream
                + ", options=" + options
                + '}';
    }

    /* ==========================================================\
       Builder
       ========================================================== */

    public static final class Builder {

        private String model = "default";
        private String prompt = "";
        private Double temperature;
        private Integer maxTokens;
        private Boolean stream = Boolean.TRUE;
        private Map<String, Object> options = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder model(String model) {
            this.model = model == null ? "default" : model;
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt == null ? "" : prompt;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        /** Enables/disables token streaming. Defaults to {@code true} (streaming-first). */
        public Builder stream(Boolean stream) {
            this.stream = stream == null ? Boolean.TRUE : stream;
            return this;
        }

        public Builder option(String key, Object value) {
            this.options.put(key, value);
            return this;
        }

        public Builder options(Map<String, Object> options) {
            this.options = options == null ? new LinkedHashMap<>() : new LinkedHashMap<>(options);
            return this;
        }

        public LlmRequest build() {
            return new LlmRequest(this);
        }
    }
}

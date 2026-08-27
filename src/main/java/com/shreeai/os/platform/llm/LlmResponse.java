package com.shreeai.os.platform.llm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, canonical response contract for the Shree AI OS LLM provider SPI.
 *
 * <p>Holds the fully materialised {@code content} produced by collecting the
 * token stream from {@link LlmProvider#stream(LlmRequest)}, together with
 * lifecycle metadata (finish reason, usage). Like {@link LlmRequest} it is
 * framework-agnostic and container-free.</p>
 *
 * @since Sprint 6.2A-P1
 */
public final class LlmResponse {

    private final String model;
    private final String content;
    private final Boolean done;
    private final String finishReason;
    private final Map<String, Object> usage;

    private LlmResponse(Builder builder) {
        this.model = Objects.requireNonNull(builder.model, "model must not be null");
        this.content = builder.content == null ? "" : builder.content;
        this.done = builder.done == null ? Boolean.TRUE : builder.done;
        this.finishReason = builder.finishReason;
        this.usage = Collections.unmodifiableMap(new LinkedHashMap<>(builder.usage));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String model() {
        return model;
    }

    public String content() {
        return content;
    }

    public Boolean done() {
        return done;
    }

    public String finishReason() {
        return finishReason;
    }

    /**
     * Usage / token metrics. Never {@code null}, unmodifiable. Keys are
     * provider-defined (e.g. {@code "promptTokens"}, {@code "completionTokens"}).
     */
    public Map<String, Object> usage() {
        return usage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LlmResponse that)) {
            return false;
        }
        return Objects.equals(model, that.model)
                && Objects.equals(content, that.content)
                && Objects.equals(done, that.done)
                && Objects.equals(finishReason, that.finishReason)
                && Objects.equals(usage, that.usage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, content, done, finishReason, usage);
    }

    @Override
    public String toString() {
        return "LlmResponse{"
                + "model='" + model + '\''
                + ", contentLength=" + (content == null ? 0 : content.length())
                + ", done=" + done
                + ", finishReason='" + finishReason + '\''
                + ", usage=" + usage
                + '}';
    }

    /* ==========================================================\
       Builder
       ========================================================== */

    public static final class Builder {

        private String model = "default";
        private String content = "";
        private Boolean done = Boolean.TRUE;
        private String finishReason;
        private Map<String, Object> usage = new LinkedHashMap<>();

        private Builder() {
        }

        public Builder model(String model) {
            this.model = model == null ? "default" : model;
            return this;
        }

        public Builder content(String content) {
            this.content = content == null ? "" : content;
            return this;
        }

        public Builder done(Boolean done) {
            this.done = done == null ? Boolean.TRUE : done;
            return this;
        }

        public Builder finishReason(String finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public Builder usage(Map<String, Object> usage) {
            this.usage = usage == null ? new LinkedHashMap<>() : new LinkedHashMap<>(usage);
            return this;
        }

        public Builder addUsage(String key, Object value) {
            this.usage.put(key, value);
            return this;
        }

        public LlmResponse build() {
            return new LlmResponse(this);
        }
    }
}

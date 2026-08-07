package com.shreeai.os.platform.sdk;

import java.time.Instant;
import java.util.Objects;

/**
 * <b>SDKResponse</b>
 *
 * <p>Immutable response model for the Shree AI OS SDK.</p>
 *
 * <p><b>Ownership:</b> SDK</p>
 * <p><b>Version:</b> 1.0.0-V1</p>
 */
public final class SDKResponse {

    private final String answer;
    private final double confidence;
    private final boolean reasoningAvailable;
    private final String metadata;
    private final Instant timestamp;

    private SDKResponse(Builder builder) {
        this.answer = builder.answer;
        this.confidence = builder.confidence;
        this.reasoningAvailable = builder.reasoningAvailable;
        this.metadata = builder.metadata;
        this.timestamp = builder.timestamp;
    }

    public String answer() { return answer; }
    public double confidence() { return confidence; }
    public boolean reasoningAvailable() { return reasoningAvailable; }
    public String metadata() { return metadata; }
    public Instant timestamp() { return timestamp; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String answer;
        private double confidence = 0.0;
        private boolean reasoningAvailable = false;
        private String metadata = "";
        private Instant timestamp = Instant.now();

        private Builder() {}

        public Builder answer(String answer) {
            this.answer = Objects.requireNonNull(answer, "answer must not be null");
            return this;
        }

        public Builder confidence(double confidence) {
            if (confidence < 0.0 || confidence > 1.0) {
                throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
            }
            this.confidence = confidence;
            return this;
        }

        public Builder reasoningAvailable(boolean reasoningAvailable) {
            this.reasoningAvailable = reasoningAvailable;
            return this;
        }

        public Builder metadata(String metadata) {
            this.metadata = Objects.requireNonNull(metadata, "metadata must not be null");
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
            return this;
        }

        public SDKResponse build() {
            if (answer == null || answer.isBlank()) {
                throw new IllegalArgumentException("answer must not be null or blank");
            }
            return new SDKResponse(this);
        }
    }
}
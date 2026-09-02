package com.shreeai.os.platform.runtime.pipeline.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ExecutionMetadata {
    private final String requestId;
    private final long startTimeMs;
    private final Map<String, Object> customValues;

    private ExecutionMetadata(String requestId, long startTimeMs, Map<String, Object> customValues) {
        this.requestId = requestId != null ? requestId : "";
        this.startTimeMs = startTimeMs;
        this.customValues = customValues != null ? Collections.unmodifiableMap(new HashMap<>(customValues)) : Collections.emptyMap();
    }

    public String getRequestId() { return requestId; }
    public long getStartTimeMs() { return startTimeMs; }
    public Map<String, Object> getCustomValues() { return customValues; }

    /** Returns the execution id (alias of requestId). */
    public String getExecutionId() { return requestId; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String requestId;
        private long startTimeMs = System.currentTimeMillis();
        private Map<String, Object> customValues = new ConcurrentHashMap<>();

        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder executionSource(String source) { this.customValues.put("executionSource", source); return this; }
        public Builder startTimeMs(long startTimeMs) { this.startTimeMs = startTimeMs; return this; }
        public Builder addCustomValue(String key, Object value) { this.customValues.put(key, value); return this; }
        public Builder customValues(Map<String, Object> values) { this.customValues.putAll(values); return this; }

        public ExecutionMetadata build() {
            return new ExecutionMetadata(requestId, startTimeMs, customValues);
        }
    }

    @Override public String toString() {
        return "ExecutionMetadata{requestId=" + requestId + ", startTimeMs=" + startTimeMs + "}";
    }
}

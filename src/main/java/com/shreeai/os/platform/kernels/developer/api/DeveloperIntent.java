package com.shreeai.os.platform.kernels.developer.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>DeveloperIntent</b>
 *
 * <p>Immutable record of the developer's structured intent. Produced by
 * {@link DeveloperIntentAnalyzer} from a raw user request string. Holds
 * the canonical intent type, a high-level action verb, the named entity,
 * the inferred domain, and any extracted tokens (class names, file names,
 * keywords) for downstream impact analysis.</p>
 *
 * <p><b>Ownership:</b> Developer Agent (Sprint-14)</p>
 *
 * @since Sprint-14
 */
public final class DeveloperIntent {

    private final String originalRequest;
    private final DeveloperIntentType intent;
    private final String action;          // ADD_FEATURE | REFACTOR | FIX_BUG | ...
    private final String entity;          // JWT | User | Product | ...
    private final String domain;          // Spring Security | Persistence | Web | ...
    private final List<String> tokens;    // class/file names mentioned
    private final Map<String, String> attributes;
    private final double confidence;      // 0.0 - 1.0

    private DeveloperIntent(Builder b) {
        this.originalRequest = Objects.requireNonNull(b.originalRequest, "originalRequest");
        this.intent = Objects.requireNonNull(b.intent, "intent");
        this.action = b.action == null ? intent.name() : b.action;
        this.entity = b.entity == null ? "" : b.entity;
        this.domain = b.domain == null ? "General" : b.domain;
        this.tokens = List.copyOf(b.tokens == null ? List.of() : b.tokens);
        this.attributes = Map.copyOf(b.attributes == null ? Map.of() : b.attributes);
        this.confidence = Math.max(0.0, Math.min(1.0, b.confidence));
    }

    public String originalRequest() { return originalRequest; }
    public DeveloperIntentType intent() { return intent; }
    public String action() { return action; }
    public String entity() { return entity; }
    public String domain() { return domain; }
    public List<String> tokens() { return tokens; }
    public Map<String, String> attributes() { return attributes; }
    public double confidence() { return confidence; }

    /**
     * Returns a human-readable label, e.g. {@code "SECURITY \u2022 ADD_FEATURE"}.
     */
    public String label() {
        return domain.toUpperCase() + " \u2022 " + action;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String originalRequest;
        private DeveloperIntentType intent;
        private String action;
        private String entity;
        private String domain;
        private List<String> tokens;
        private Map<String, String> attributes;
        private double confidence = 0.5;

        public Builder originalRequest(String v) { this.originalRequest = v; return this; }
        public Builder intent(DeveloperIntentType v) { this.intent = v; return this; }
        public Builder action(String v) { this.action = v; return this; }
        public Builder entity(String v) { this.entity = v; return this; }
        public Builder domain(String v) { this.domain = v; return this; }
        public Builder tokens(List<String> v) { this.tokens = v; return this; }
        public Builder attributes(Map<String, String> v) { this.attributes = v; return this; }
        public Builder confidence(double v) { this.confidence = v; return this; }

        public DeveloperIntent build() {
            return new DeveloperIntent(this);
        }
    }
}

package com.shreeai.os.platform.kernels.tool.validation;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ToolValidationResult</b> — Immutable result of tool request validation.
 *
 * <p><b>Ownership:</b> Tool Kernel — Validation</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ToolValidationResult {

    private final boolean valid;
    private final List<String> violations;
    private final Instant validatedAt;
    private final Map<String, Object> metadata;

    public ToolValidationResult(
            boolean valid,
            List<String> violations,
            Instant validatedAt,
            Map<String, Object> metadata) {
        if (violations == null) throw new IllegalArgumentException("violations must not be null");
        if (validatedAt == null) throw new IllegalArgumentException("validatedAt must not be null");
        if (metadata == null) throw new IllegalArgumentException("metadata must not be null");

        this.valid = valid;
        this.violations = List.copyOf(violations);
        this.validatedAt = validatedAt;
        this.metadata = Collections.unmodifiableMap(Map.copyOf(metadata));
    }

    public boolean valid() { return valid; }
    public List<String> violations() { return violations; }
    public Instant validatedAt() { return validatedAt; }
    public Map<String, Object> metadata() { return metadata; }

    public static ToolValidationResult valid(Instant validatedAt) {
        return new ToolValidationResult(true, List.of(), validatedAt, Map.of());
    }

    public static ToolValidationResult invalid(
            List<String> violations, Instant validatedAt, Map<String, Object> metadata) {
        return new ToolValidationResult(false, violations, validatedAt, metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ToolValidationResult that = (ToolValidationResult) obj;
        return valid == that.valid &&
                violations.equals(that.violations) &&
                validatedAt.equals(that.validatedAt) &&
                metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, violations, validatedAt, metadata);
    }

    @Override
    public String toString() {
        return "ToolValidationResult{valid=" + valid +
                ", violations=" + violations + '}';
    }
}

package com.shreeai.os.platform.kernels.chief.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <b>ChiefValidationResult</b>
 *
 * <p>Immutable validation result for Chief Kernel domain models.
 * This value object encapsulates the outcome of structural validation.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Encapsulates validation outcome.</li>
 *   <li>Provides immutable validation issues and warnings.</li>
 *   <li>Contains no validation logic.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable — all fields are final.</li>
 *   <li>Defensive copying — protects mutable collections.</li>
 *   <li>Value semantics — implements equals, hashCode, toString.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel — Validation Layer</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-103, EIO-ARCH-001</p>
 *
 * @param valid    whether validation passed
 * @param issues   list of validation issues (must not be {@code null})
 * @param warnings list of validation warnings (must not be {@code null})
 * @param metadata additional metadata (must not be {@code null})
 *
 * @since 1.0
 */
public final class ChiefValidationResult {

    private final boolean valid;
    private final List<String> issues;
    private final List<String> warnings;
    private final Map<String, Object> metadata;

    /**
     * Constructs a {@code ChiefValidationResult} with the specified parameters.
     *
     * @param valid    whether validation passed
     * @param issues   list of validation issues (must not be {@code null})
     * @param warnings list of validation warnings (must not be {@code null})
     * @param metadata additional metadata (must not be {@code null})
     * @throws IllegalArgumentException if issues, warnings, or metadata is {@code null}
     */
    public ChiefValidationResult(
            boolean valid,
            List<String> issues,
            List<String> warnings,
            Map<String, Object> metadata) {
        if (issues == null) {
            throw new IllegalArgumentException("ChiefValidationResult issues must not be null");
        }
        if (warnings == null) {
            throw new IllegalArgumentException("ChiefValidationResult warnings must not be null");
        }
        if (metadata == null) {
            throw new IllegalArgumentException("ChiefValidationResult metadata must not be null");
        }

        this.valid = valid;
        this.issues = Collections.unmodifiableList(new ArrayList<>(issues));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }

    /**
     * Returns whether validation passed.
     *
     * @return {@code true} if validation passed
     */
    public boolean valid() {
        return valid;
    }

    /**
     * Returns an unmodifiable list of validation issues.
     *
     * @return unmodifiable list of issues
     */
    public List<String> issues() {
        return issues;
    }

    /**
     * Returns an unmodifiable list of validation warnings.
     *
     * @return unmodifiable list of warnings
     */
    public List<String> warnings() {
        return warnings;
    }

    /**
     * Returns an unmodifiable view of metadata.
     *
     * @return unmodifiable map of metadata
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiefValidationResult that = (ChiefValidationResult) obj;
        return valid == that.valid && Objects.equals(issues, that.issues) && Objects.equals(warnings, that.warnings) && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, issues, warnings, metadata);
    }

    @Override
    public String toString() {
        return "ChiefValidationResult{valid=" + valid + ", issues=" + issues.size() + '}';
    }
}
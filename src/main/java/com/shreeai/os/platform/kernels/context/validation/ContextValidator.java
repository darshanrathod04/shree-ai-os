package com.shreeai.os.platform.kernels.context.validation;

import com.shreeai.os.platform.kernels.context.model.Context;
import com.shreeai.os.platform.kernels.context.model.ContextId;
import com.shreeai.os.platform.kernels.context.model.ContextPriority;
import com.shreeai.os.platform.kernels.context.model.ContextScope;
import com.shreeai.os.platform.kernels.context.model.ContextState;
import com.shreeai.os.platform.kernels.context.model.ContextType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>ContextValidator</b>
 *
 * <p>A utility validator for base Context domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates base Context structure and invariants.</li>
 *   <li>Validates ContextId, ContextState, ContextPriority, ContextScope, and ContextType.</li>
 *   <li>Validates timestamps and metadata.</li>
 *   <li>Provides pure validation without side effects.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Static methods only - no instance state.</li>
 *   <li>Stateless and thread-safe.</li>
 *   <li>Pure validation - never mutates objects.</li>
 *   <li>No business logic, persistence, or side effects.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-103</p>
 *
 * @see ContextValidationResult
 */
public final class ContextValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private ContextValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates a base Context instance.
     *
     * <p>Validates all core Context fields including id, type, state, timestamps, and metadata.</p>
     *
     * @param context the Context to validate (must not be null)
     * @return the validation result
     * @throws NullPointerException if context is null
     */
    public static ContextValidationResult validate(Context context) {
        List<String> violations = new ArrayList<>();

        // Validate id
        if (context.id() == null) {
            violations.add("Context id must not be null");
        } else {
            ContextValidationResult idResult = validateContextId(context.id());
            if (!idResult.isValid()) {
                violations.addAll(idResult.getViolations());
            }
        }

        // Validate type
        if (context.type() == null) {
            violations.add("Context type must not be null");
        } else {
            ContextValidationResult typeResult = validateContextType(context.type());
            if (!typeResult.isValid()) {
                violations.addAll(typeResult.getViolations());
            }
        }

        // Validate state
        if (context.state() == null) {
            violations.add("Context state must not be null");
        } else {
            ContextValidationResult stateResult = validateContextState(context.state());
            if (!stateResult.isValid()) {
                violations.addAll(stateResult.getViolations());
            }
        }

        // Validate timestamps
        if (context.createdAt() == null) {
            violations.add("Context createdAt must not be null");
        }
        if (context.updatedAt() == null) {
            violations.add("Context updatedAt must not be null");
        } else if (context.createdAt() != null && context.updatedAt().isBefore(context.createdAt())) {
            violations.add("Context updatedAt must not be before createdAt");
        }

        // Validate metadata
        if (context.data() == null) {
            violations.add("Context data must not be null");
        }

        Map<String, Object> metadata = Map.of(
                "contextType", context.type() != null ? context.type().name() : "null"
        );

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                metadata
        );
    }

    /**
     * Validates a ContextId instance.
     *
     * @param contextId the ContextId to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateContextId(ContextId contextId) {
        List<String> violations = new ArrayList<>();

        if (contextId == null) {
            violations.add("ContextId must not be null");
        } else if (contextId.value() == null || contextId.value().isBlank()) {
            violations.add("ContextId value must not be null or blank");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of()
        );
    }

    /**
     * Validates a ContextType instance.
     *
     * @param contextType the ContextType to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateContextType(ContextType contextType) {
        List<String> violations = new ArrayList<>();

        if (contextType == null) {
            violations.add("ContextType must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of()
        );
    }

    /**
     * Validates a ContextState instance.
     *
     * @param contextState the ContextState to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateContextState(ContextState contextState) {
        List<String> violations = new ArrayList<>();

        if (contextState == null) {
            violations.add("ContextState must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of()
        );
    }

    /**
     * Validates a ContextPriority instance.
     *
     * @param contextPriority the ContextPriority to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateContextPriority(ContextPriority contextPriority) {
        List<String> violations = new ArrayList<>();

        if (contextPriority == null) {
            violations.add("ContextPriority must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of()
        );
    }

    /**
     * Validates a ContextScope instance.
     *
     * @param contextScope the ContextScope to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateContextScope(ContextScope contextScope) {
        List<String> violations = new ArrayList<>();

        if (contextScope == null) {
            violations.add("ContextScope must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of()
        );
    }

    /**
     * Validates a timestamp.
     *
     * <p>Validates that the timestamp is not null.</p>
     *
     * @param timestamp the timestamp to validate (must not be null)
     * @param fieldName the name of the field for error messages
     * @return the validation result
     */
    public static ContextValidationResult validateTimestamp(Instant timestamp, String fieldName) {
        List<String> violations = new ArrayList<>();

        if (timestamp == null) {
            violations.add(fieldName + " must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of("fieldName", fieldName)
        );
    }

    /**
     * Validates metadata map.
     *
     * <p>Validates that the metadata map is not null.</p>
     *
     * @param metadata the metadata to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateMetadata(Map<String, Object> metadata) {
        List<String> violations = new ArrayList<>();

        if (metadata == null) {
            violations.add("Metadata must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of()
        );
    }
}
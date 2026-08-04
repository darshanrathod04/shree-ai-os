package com.shreeai.os.platform.kernels.context.validation;

import com.shreeai.os.platform.kernels.context.model.ContextState;
import com.shreeai.os.platform.kernels.context.model.ContextType;
import com.shreeai.os.platform.kernels.context.model.SessionContext;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <b>SessionContextValidator</b>
 *
 * <p>A utility validator for SessionContext domain models.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Validates session lifecycle and state.</li>
 *   <li>Validates expiration and activity timestamps.</li>
 *   <li>Validates session metadata.</li>
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
 * @see SessionContext
 */
public final class SessionContextValidator {

    /**
     * Private constructor to prevent instantiation.
     */
    private SessionContextValidator() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates a SessionContext instance.
     *
     * <p>Validates all SessionContext fields including session lifecycle,
     * expiration, activity timestamps, and session metadata.</p>
     *
     * @param context the SessionContext to validate (must not be null)
     * @return the validation result
     * @throws NullPointerException if context is null
     */
    public static ContextValidationResult validate(SessionContext context) {
        List<String> violations = new ArrayList<>();

        // Validate base Context fields directly
        if (context.id() == null) {
            violations.add("Context id must not be null");
        } else {
            ContextValidationResult idResult = ContextValidator.validateContextId(context.id());
            if (!idResult.isValid()) {
                violations.addAll(idResult.getViolations());
            }
        }

        if (context.type() == null) {
            violations.add("Context type must not be null");
        } else {
            ContextValidationResult typeResult = ContextValidator.validateContextType(context.type());
            if (!typeResult.isValid()) {
                violations.addAll(typeResult.getViolations());
            }
        }

        if (context.state() == null) {
            violations.add("Context state must not be null");
        } else {
            ContextValidationResult stateResult = ContextValidator.validateContextState(context.state());
            if (!stateResult.isValid()) {
                violations.addAll(stateResult.getViolations());
            }
        }

        if (context.createdAt() == null) {
            violations.add("Context createdAt must not be null");
        }
        if (context.updatedAt() == null) {
            violations.add("Context updatedAt must not be null");
        } else if (context.createdAt() != null && context.updatedAt().isBefore(context.createdAt())) {
            violations.add("Context updatedAt must not be before createdAt");
        }
        if (context.data() == null) {
            violations.add("Context data must not be null");
        }

        // Validate type is SESSION
        if (context.type() != ContextType.SESSION) {
            violations.add("SessionContext type must be SESSION, got: " + context.type());
        }

        // Validate sessionId
        if (context.sessionId() == null || context.sessionId().isBlank()) {
            violations.add("SessionContext sessionId must not be null or blank");
        }

        // Validate userId
        if (context.userId() == null || context.userId().isBlank()) {
            violations.add("SessionContext userId must not be null or blank");
        }

        // Validate sessionStartTime
        if (context.sessionStartTime() == null) {
            violations.add("SessionContext sessionStartTime must not be null");
        }

        // Validate session lifecycle
        if (context.state() == ContextState.ACTIVE) {
            // Active session should have valid session and user IDs
            if (context.sessionId() == null || context.sessionId().isBlank()) {
                violations.add("Active session must have a valid sessionId");
            }
            if (context.userId() == null || context.userId().isBlank()) {
                violations.add("Active session must have a valid userId");
            }
        }

        // Validate activity timestamps
        if (context.sessionStartTime() != null) {
            // sessionStartTime should not be in the future
            if (context.sessionStartTime().isAfter(Instant.now())) {
                violations.add("SessionContext sessionStartTime must not be in the future");
            }

            // If createdAt exists, sessionStartTime should not be after createdAt
            if (context.createdAt() != null && context.sessionStartTime().isAfter(context.createdAt())) {
                violations.add("SessionContext sessionStartTime must not be after createdAt");
            }
        }

        // Validate snapshot consistency
        if (context.createdAt() != null && context.updatedAt() != null) {
            if (context.updatedAt().isBefore(context.createdAt())) {
                violations.add("SessionContext updatedAt must not be before createdAt");
            }
        }

        Map<String, Object> metadata = Map.of(
                "contextType", context.type() != null ? context.type().name() : "null",
                "sessionId", context.sessionId() != null ? context.sessionId() : "null",
                "userId", context.userId() != null ? context.userId() : "null",
                "sessionStartTime", context.sessionStartTime() != null ? context.sessionStartTime().toString() : "null"
        );

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                metadata
        );
    }

    /**
     * Validates session lifecycle.
     *
     * <p>Validates that the session lifecycle is valid and consistent.</p>
     *
     * @param context the SessionContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateSessionLifecycle(SessionContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("SessionContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate session ID
        if (context.sessionId() == null || context.sessionId().isBlank()) {
            violations.add("Session ID must not be null or blank");
        }

        // Validate user ID
        if (context.userId() == null || context.userId().isBlank()) {
            violations.add("User ID must not be null or blank");
        }

        // Validate state
        if (context.state() == null) {
            violations.add("Session state must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "sessionId", context.sessionId() != null ? context.sessionId() : "null",
                        "userId", context.userId() != null ? context.userId() : "null",
                        "state", context.state() != null ? context.state().name() : "null"
                )
        );
    }

    /**
     * Validates session expiration.
     *
     * <p>Validates that the session expiration is valid and consistent.</p>
     *
     * @param context the SessionContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateSessionExpiration(SessionContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("SessionContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate session start time
        if (context.sessionStartTime() == null) {
            violations.add("Session start time must not be null");
        } else if (context.sessionStartTime().isAfter(Instant.now())) {
            violations.add("Session start time must not be in the future");
        }

        // For expired sessions, validate that updatedAt is after sessionStartTime
        if (context.state() == ContextState.EXPIRED &&
                context.sessionStartTime() != null &&
                context.updatedAt() != null &&
                context.updatedAt().isBefore(context.sessionStartTime())) {
            violations.add("Expired session updatedAt must not be before sessionStartTime");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "sessionStartTime", context.sessionStartTime() != null ? context.sessionStartTime().toString() : "null",
                        "state", context.state() != null ? context.state().name() : "null"
                )
        );
    }

    /**
     * Validates activity timestamps.
     *
     * <p>Validates that activity timestamps are consistent and valid.</p>
     *
     * @param context the SessionContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateActivityTimestamps(SessionContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("SessionContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate sessionStartTime
        if (context.sessionStartTime() == null) {
            violations.add("Session start time must not be null");
        }

        // Validate timestamp consistency
        if (context.createdAt() != null && context.updatedAt() != null) {
            if (context.updatedAt().isBefore(context.createdAt())) {
                violations.add("updatedAt must not be before createdAt");
            }
        }

        // Validate sessionStartTime is not after createdAt
        if (context.sessionStartTime() != null && context.createdAt() != null &&
                context.sessionStartTime().isAfter(context.createdAt())) {
            violations.add("sessionStartTime must not be after createdAt");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "sessionStartTime", context.sessionStartTime() != null ? context.sessionStartTime().toString() : "null",
                        "createdAt", context.createdAt() != null ? context.createdAt().toString() : "null",
                        "updatedAt", context.updatedAt() != null ? context.updatedAt().toString() : "null"
                )
        );
    }

    /**
     * Validates session metadata.
     *
     * <p>Validates that session metadata is present and valid.</p>
     *
     * @param context the SessionContext to validate (must not be null)
     * @return the validation result
     */
    public static ContextValidationResult validateSessionMetadata(SessionContext context) {
        List<String> violations = new ArrayList<>();

        if (context == null) {
            violations.add("SessionContext must not be null");
            return new ContextValidationResult(false, violations, Instant.now(), Map.of());
        }

        // Validate data map
        if (context.data() == null) {
            violations.add("Session metadata (data) must not be null");
        }

        return new ContextValidationResult(
                violations.isEmpty(),
                violations,
                Instant.now(),
                Map.of(
                        "hasMetadata", context.data() != null && !context.data().isEmpty(),
                        "dataSize", context.data() != null ? context.data().size() : 0
                )
        );
    }
}
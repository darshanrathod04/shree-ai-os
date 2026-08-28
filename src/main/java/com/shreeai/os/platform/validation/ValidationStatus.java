package com.shreeai.os.platform.validation;

/**
 * Validation status outcomes for decisions.
 *
 * VALID            - Decision passed all checks
 * VALID_WITH_WARNINGS - Decision passed but has concerns
 * INVALID          - Decision failed critical checks
 * DEFERRED         - Validation postponed (insufficient data)
 * BLOCKED          - Execution blocked by policy/rule
 * UNKNOWN          - Validation could not be performed
 */
public enum ValidationStatus {
    VALID,
    VALID_WITH_WARNINGS,
    INVALID,
    DEFERRED,
    BLOCKED,
    UNKNOWN
}
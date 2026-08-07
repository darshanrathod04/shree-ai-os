package com.shreeai.os.platform.validation;

/**
 * Validation strategy types.
 *
 * RULE_BASED   - Deterministic rule checks
 * SAFETY       - Safety and policy checks
 * CONTEXT      - Context availability and consistency
 * FALLBACK     - Fallback capability validation
 * UNKNOWN      - Strategy not determined
 *
 * Future:
 * POLICY       - External policy engine integration
 * AI_ASSISTED  - LLM-assisted validation (not implemented in Sprint 5)
 */
public enum ValidationStrategy {
    RULE_BASED,
    SAFETY,
    CONTEXT,
    FALLBACK,
    UNKNOWN
}
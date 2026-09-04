package com.shreeai.os.platform.validation;

import com.shreeai.os.platform.kernels.cognitive.model.Thought;
import com.shreeai.os.platform.kernels.context.model.ConversationSession;
import com.shreeai.os.platform.kernels.context.model.ResolvedContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Decision Validator - Shadow Mode Only.
 *
 * <p>Validates decisions produced by DecisionEngine before any execution layer.
 * Uses a rule-based pipeline for validation. NEVER executes anything.
 * NEVER modifies production routing. Runs entirely in SHADOW MODE.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Orchestrate validation rules</li>
 *   <li>Collect validation outcomes</li>
 *   <li>Build immutable ValidationResult</li>
 *   <li>Log validation results</li>
 *   <li>Report metrics</li>
 * </ul>
 *
 * <h2>Non-Responsibilities</h2>
 * <ul>
 *   <li>Does NOT execute capabilities</li>
 *   <li>Does NOT modify production routing</li>
 *   <li>Does NOT change DecisionEngine behavior</li>
 *   <li>Does NOT call LLM or external services</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <ul>
 *   <li>Constructor injection only</li>
 *   <li>Immutable validation results</li>
 *   <li>No mutable static state</li>
 *   <li>Singleton safe</li>
 * </ul>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
@Component
public class DecisionValidator {

    private static final Logger log = LoggerFactory.getLogger(DecisionValidator.class);

    private final List<ValidationRule> rules;
    private final ValidationMetrics metrics;

    /**
     * Constructor injection for dependencies.
     *
     * @param rules list of validation rules (injected by Spring)
     * @param metrics validation metrics collector
     */
    public DecisionValidator(List<ValidationRule> rules, ValidationMetrics metrics) {
        // Sort rules by @Order annotation priority
        this.rules = rules != null ? 
            rules.stream()
                .sorted((r1, r2) -> {
                    int p1 = getPriority(r1);
                    int p2 = getPriority(r2);
                    return Integer.compare(p1, p2);
                })
                .collect(Collectors.toList()) : 
            List.of();
        this.metrics = metrics != null ? metrics : new ValidationMetrics();
        
        // Validate pipeline configuration
        validatePipelineConfiguration();
        
        // Log discovered rules at startup
        log.info("[VALIDATOR] ========================================================");
        log.info("[VALIDATOR] VALIDATION PIPELINE INITIALIZED");
        log.info("[VALIDATOR] RULES DISCOVERED: {}", this.rules.size());
        log.info("[VALIDATOR] RULE ORDER (by priority):");
        for (int i = 0; i < this.rules.size(); i++) {
            ValidationRule rule = this.rules.get(i);
            int priority = getPriority(rule);
            log.info("[VALIDATOR]   {}. {} (priority={})", (i + 1), rule.getRuleName(), priority);
        }
        log.info("[VALIDATOR] ========================================================");
    }
    
    /**
     * Extract priority from a rule's @Order annotation.
     */
    private int getPriority(ValidationRule rule) {
        Order order = rule.getClass().getAnnotation(Order.class);
        return order != null ? order.value() : Integer.MAX_VALUE;
    }
    
    /**
     * Validate pipeline configuration on startup.
     * Fails fast if configuration is invalid.
     */
    private void validatePipelineConfiguration() {
        if (rules.isEmpty()) {
            log.warn("[VALIDATOR] WARNING: No validation rules configured!");
            return;
        }
        
        // Check for duplicate priorities
        List<Integer> priorities = rules.stream()
            .map(this::getPriority)
            .collect(Collectors.toList());
        
        long distinctCount = priorities.stream().distinct().count();
        if (distinctCount != priorities.size()) {
            String errorMsg = String.format(
                "[VALIDATOR] FATAL: Duplicate rule priorities detected! " +
                "Each rule must have a unique @Order value. Rules: %s",
                rules.stream()
                    .map(r -> r.getRuleName() + "=" + getPriority(r))
                    .collect(Collectors.toList())
            );
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        
        // Verify priorities are strictly increasing
        for (int i = 1; i < priorities.size(); i++) {
            if (priorities.get(i) <= priorities.get(i - 1)) {
                String errorMsg = String.format(
                    "[VALIDATOR] FATAL: Rule priorities are not strictly increasing! " +
                    "Priority at position %d (%d) must be greater than priority at position %d (%d).",
                    i, priorities.get(i), i - 1, priorities.get(i - 1)
                );
                log.error(errorMsg);
                throw new IllegalStateException(errorMsg);
            }
        }
        
        log.info("[VALIDATOR] Pipeline configuration validated successfully");
    }

    /**
     * Validate a decision in shadow mode.
     *
     * @param decision the decision to validate (from DecisionEngine)
     * @param session the conversation session (may be null)
     * @param resolvedContext the resolved context (may be null)
     * @return immutable ValidationResult
     */
    public ValidationResult validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext
    ) {
        long startTime = System.nanoTime();
        String validationId = java.util.UUID.randomUUID().toString();
        String decisionId = java.util.UUID.randomUUID().toString();

        // Build trace
        ValidationTrace.Builder traceBuilder = new ValidationTrace.Builder()
                .traceId(validationId)
                .startTime(Instant.now());

        // Collect outcomes from all rules
        List<String> allWarnings = new ArrayList<>();
        List<String> allErrors = new ArrayList<>();
        boolean hasErrors = false;

        for (ValidationRule rule : rules) {
            long ruleStart = System.nanoTime();
            try {
                ValidationOutcome outcome = rule.validate(decision, session, resolvedContext, null);
                long ruleDuration = System.nanoTime() - ruleStart;

                // Add to trace
                traceBuilder.addStep(new ValidationTrace.ValidationStep(
                        rule.getRuleName(),
                        outcome.isPassed(),
                        outcome.getMessage(),
                        Instant.now(),
                        ruleDuration
                ));

                // Collect warnings and errors
                if (outcome.getWarnings() != null) {
                    allWarnings.addAll(outcome.getWarnings());
                }
                if (outcome.getErrors() != null && !outcome.getErrors().isEmpty()) {
                    allErrors.addAll(outcome.getErrors());
                    hasErrors = true;
                    metrics.recordRuleFailure(rule.getRuleName());
                }

                // Log rule execution at debug level
                if (!outcome.isPassed()) {
                    log.debug("[VALIDATOR] Rule {} failed: {}", rule.getRuleName(), outcome.getMessage());
                }

            } catch (Exception e) {
                log.error("[VALIDATOR] Rule {} threw exception: {}", rule.getRuleName(), e.getMessage(), e);
                allErrors.add("Rule execution error: " + rule.getRuleName() + " - " + e.getMessage());
                hasErrors = true;
                metrics.recordRuleFailure(rule.getRuleName());
            }
        }

        // Determine status
        ValidationStatus status;
        boolean valid;
        if (hasErrors) {
            status = ValidationStatus.INVALID;
            valid = false;
        } else if (!allWarnings.isEmpty()) {
            status = ValidationStatus.VALID_WITH_WARNINGS;
            valid = true;
        } else {
            status = ValidationStatus.VALID;
            valid = true;
        }

        // Build trace
        long totalDuration = System.nanoTime() - startTime;
        ValidationTrace trace = traceBuilder
                .endTime(Instant.now())
                .totalDurationNanos(totalDuration)
                .build();

        // Build result
        ValidationResult result = ValidationResult.builder()
                .validationId(validationId)
                .decisionId(decisionId)
                .valid(valid)
                .status(status)
                .strategy(ValidationStrategy.RULE_BASED)
                .riskLevel(ValidationResult.RiskLevel.LOW)
                .confidence(0.8)
                .warnings(allWarnings)
                .errors(allErrors)
                .metadata(java.util.Map.of(
                        "ruleCount", rules.size(),
                        "validationTime", totalDuration
                ))
                .timestamp(Instant.now())
                .trace(trace)
                .build();

        // Record metrics
        metrics.recordValidation(result, totalDuration);

        // Log validation result
        logValidation(result, decision);

        return result;
    }

    /**
     * Log validation result using SLF4J.
     */
    private void logValidation(ValidationResult result, Thought decision) {
        ValidationTrace trace = result.getTrace();
        
        if (!log.isInfoEnabled()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[VALIDATOR] ========================================================\n");
        sb.append("[VALIDATOR] VALIDATION ID: ").append(result.getValidationId()).append("\n");
        sb.append("[VALIDATOR] DECISION: ").append(decision != null ? decision.getAction() : "null").append("\n");
        sb.append("[VALIDATOR] STATUS: ").append(result.getStatus()).append("\n");
        sb.append("[VALIDATOR] VALID: ").append(result.isValid()).append("\n");
        sb.append("[VALIDATOR] RISK: ").append(result.getRiskLevel()).append("\n");
        sb.append("[VALIDATOR] CONFIDENCE: ").append(String.format("%.0f%%", result.getConfidence() * 100)).append("\n");
        sb.append("[VALIDATOR] STRATEGY: ").append(result.getStrategy()).append("\n");
        sb.append("[VALIDATOR] RULES EXECUTED: ").append(trace != null ? trace.getSteps().size() : 0).append("\n");
        sb.append("[VALIDATOR] TOTAL TIME: ").append(trace != null ? trace.getTotalDurationNanos() / 1_000_000 : 0).append("ms\n");

        if (!result.getWarnings().isEmpty()) {
            sb.append("[VALIDATOR] WARNINGS:\n");
            for (String warning : result.getWarnings()) {
                sb.append("[VALIDATOR]   - ").append(warning).append("\n");
            }
        }

        if (!result.getErrors().isEmpty()) {
            sb.append("[VALIDATOR] ERRORS:\n");
            for (String error : result.getErrors()) {
                sb.append("[VALIDATOR]   - ").append(error).append("\n");
            }
        }

        sb.append("[VALIDATOR] METADATA: ").append(result.getMetadata()).append("\n");
        sb.append("[VALIDATOR] ========================================================");

        log.info(sb.toString());
    }

    /**
     * Get validation metrics.
     */
    public ValidationMetrics getMetrics() {
        return metrics;
    }
    
    /**
     * Get registered validation rules (for testing and monitoring).
     */
    public List<ValidationRule> getRules() {
        return rules;
    }
    
    /**
     * Get rule descriptors for diagnostics.
     */
    public List<RuleDescriptor> getRuleDescriptors() {
        return rules.stream()
            .map(rule -> {
                int priority = getPriority(rule);
                String beanName = rule.getClass().getSimpleName();
                return new RuleDescriptor(
                    rule.getRuleName(),
                    priority,
                    beanName,
                    true,
                    "Validation rule: " + rule.getRuleName()
                );
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Generate pipeline report for diagnostics.
     */
    public ValidationPipelineReport generatePipelineReport() {
        return new ValidationPipelineReport(
            rules.size(),
            getRuleDescriptors(),
            Instant.now(),
            "1.0",
            "SHADOW"
        );
    }
}
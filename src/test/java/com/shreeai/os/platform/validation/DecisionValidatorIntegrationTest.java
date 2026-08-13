package com.shreeai.os.platform.validation;

import com.shreeai.os.platform.cognition.Thought;
import com.shreeai.os.platform.context.ConversationSession;
import com.shreeai.os.platform.production.ResolvedContext;
import com.shreeai.os.platform.validation.DecisionValidator;
import com.shreeai.os.platform.validation.ValidationResult;
import com.shreeai.os.platform.validation.ValidationRule;
import com.shreeai.os.platform.validation.ValidationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for DecisionValidator rule pipeline.
 * 
 * <p>Verifies that all 7 validation rules are automatically discovered
 * and injected by Spring's component scanning.</p>
 */
@SpringBootTest
@TestPropertySource(properties = "spring.main.banner-mode=off")
class DecisionValidatorIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DecisionValidator decisionValidator;

    @Test
    void testAllRulesDiscoveredBySpring() {
        System.out.println("========================================");
        System.out.println("VALIDATION RULE REGISTRATION AUDIT");
        System.out.println("========================================");
        
        // Get all ValidationRule beans from Spring context
        String[] ruleBeanNames = applicationContext.getBeanNamesForType(ValidationRule.class);
        
        System.out.println("Total ValidationRule beans found: " + ruleBeanNames.length);
        System.out.println("\nRegistered Rules:");
        
        for (String beanName : ruleBeanNames) {
            ValidationRule rule = applicationContext.getBean(beanName, ValidationRule.class);
            System.out.println("  - " + rule.getRuleName() + " (bean: " + beanName + ")");
        }
        
        // Verify we have exactly 7 rules
        assertEquals(7, ruleBeanNames.length, 
                "Expected 7 validation rules, but found " + ruleBeanNames.length);
        
        // Verify specific rules are present
        List<String> ruleNames = List.of(ruleBeanNames).stream()
                .map(name -> applicationContext.getBean(name, ValidationRule.class).getRuleName())
                .collect(Collectors.toList());
        
        assertTrue(ruleNames.contains("DecisionExistsRule"), "DecisionExistsRule not found");
        assertTrue(ruleNames.contains("CapabilityRule"), "CapabilityRule not found");
        assertTrue(ruleNames.contains("ConfidenceRule"), "ConfidenceRule not found");
        assertTrue(ruleNames.contains("RiskRule"), "RiskRule not found");
        assertTrue(ruleNames.contains("ExecutionModeRule"), "ExecutionModeRule not found");
        assertTrue(ruleNames.contains("SessionRule"), "SessionRule not found");
        assertTrue(ruleNames.contains("ContextRule"), "ContextRule not found");
        
        System.out.println("\n✓ All 7 rules successfully registered");
        System.out.println("========================================\n");
    }

    @Test
    void testDecisionValidatorReceivesAllRules() {
        System.out.println("\n========================================");
        System.out.println("DECISION VALIDATOR RULE INJECTION TEST");
        System.out.println("========================================");
        
        List<ValidationRule> rules = decisionValidator.getRules();
        
        System.out.println("Rules injected into DecisionValidator: " + rules.size());
        System.out.println("\nRule Order:");
        
        for (int i = 0; i < rules.size(); i++) {
            ValidationRule rule = rules.get(i);
            System.out.println("  " + (i + 1) + ". " + rule.getRuleName() + " (" + rule.getClass().getSimpleName() + ")");
        }
        
        // Verify all 7 rules are injected
        assertEquals(7, rules.size(), 
                "DecisionValidator should have 7 rules, but has " + rules.size());
        
        // Verify no duplicates
        List<String> ruleNames = rules.stream()
                .map(ValidationRule::getRuleName)
                .collect(Collectors.toList());
        
        long distinctCount = ruleNames.stream().distinct().count();
        assertEquals(7, distinctCount, "Duplicate rules detected");
        
        // Verify specific rules in order (sorted by @Order priority)
        assertEquals("DecisionExistsRule", rules.get(0).getRuleName());
        assertEquals("SessionRule", rules.get(1).getRuleName());
        assertEquals("ContextRule", rules.get(2).getRuleName());
        assertEquals("CapabilityRule", rules.get(3).getRuleName());
        assertEquals("ConfidenceRule", rules.get(4).getRuleName());
        assertEquals("RiskRule", rules.get(5).getRuleName());
        assertEquals("ExecutionModeRule", rules.get(6).getRuleName());
        
        System.out.println("\n✓ All 7 rules correctly injected in order");
        System.out.println("✓ No duplicate rules detected");
        System.out.println("========================================\n");
    }

    @Test
    void testRuleExecutionPipeline() {
        System.out.println("\n========================================");
        System.out.println("RULE EXECUTION PIPELINE TEST");
        System.out.println("========================================");
        
        Thought decision = new Thought("test goal", "LEARN", "LearningSessionEngine", "test reasoning");
        ConversationSession session = createValidSession();
        ResolvedContext context = createValidContext();
        
        System.out.println("Executing validation pipeline...");
        System.out.println("Decision: " + decision.getAction());
        System.out.println("Intent: " + decision.getIntent());
        
        ValidationResult result = decisionValidator.validate(decision, session, context);
        
        System.out.println("\nValidation Result:");
        System.out.println("  Status: " + result.getStatus());
        System.out.println("  Valid: " + result.isValid());
        System.out.println("  Rules Executed: " + result.getTrace().getSteps().size());
        System.out.println("  Total Time: " + result.getTrace().getTotalDurationNanos() / 1_000_000 + "ms");
        
        System.out.println("\nRule Execution Trace:");
        result.getTrace().getSteps().forEach(step -> {
            System.out.println("  " + (step.isPassed() ? "✓" : "✗") + " " + step.getRuleName() + 
                             " (" + step.getDurationNanos() / 1_000_000 + "ms)");
        });
        
        // Verify all 7 rules executed
        assertEquals(7, result.getTrace().getSteps().size(), 
                "Expected 7 rules to execute, but only " + result.getTrace().getSteps().size() + " executed");
        
        // Verify all rules passed (valid decision)
        assertTrue(result.getTrace().getSteps().stream().allMatch(step -> step.isPassed()));
        
        System.out.println("\n✓ All 7 rules executed successfully");
        System.out.println("✓ All rules passed for valid decision");
        System.out.println("========================================\n");
    }

    @Test
    void testRuleExecutionWithInvalidDecision() {
        System.out.println("\n========================================");
        System.out.println("RULE EXECUTION WITH INVALID DECISION");
        System.out.println("========================================");
        
        Thought decision = null; // Invalid decision
        ConversationSession session = createValidSession();
        ResolvedContext context = createValidContext();
        
        System.out.println("Executing validation with null decision...");
        
        ValidationResult result = decisionValidator.validate(decision, session, context);
        
        System.out.println("\nValidation Result:");
        System.out.println("  Status: " + result.getStatus());
        System.out.println("  Valid: " + result.isValid());
        System.out.println("  Rules Executed: " + result.getTrace().getSteps().size());
        System.out.println("  Errors: " + result.getErrors().size());
        
        result.getErrors().forEach(error -> System.out.println("    - " + error));
        
        // Verify DecisionExistsRule caught the error
        assertFalse(result.isValid());
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Null Decision")));
        
        // Verify at least 1 rule executed (DecisionExistsRule)
        assertTrue(result.getTrace().getSteps().size() >= 1);
        
        System.out.println("\n✓ DecisionExistsRule correctly identified null decision");
        System.out.println("========================================\n");
    }

    @Test
    void testRuleExecutionOrder() {
        System.out.println("\n========================================");
        System.out.println("RULE EXECUTION ORDER VERIFICATION");
        System.out.println("========================================");
        
        List<ValidationRule> rules = decisionValidator.getRules();
        
        System.out.println("Expected Rule Order (by @Order priority):");
        String[] expectedOrder = {
            "DecisionExistsRule",
            "SessionRule",
            "ContextRule",
            "CapabilityRule",
            "ConfidenceRule",
            "RiskRule",
            "ExecutionModeRule"
        };
        
        for (int i = 0; i < expectedOrder.length; i++) {
            System.out.println("  " + (i + 1) + ". " + expectedOrder[i]);
        }
        
        System.out.println("\nActual Rule Order:");
        for (int i = 0; i < rules.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + rules.get(i).getRuleName());
        }
        
        // Verify order matches
        for (int i = 0; i < expectedOrder.length; i++) {
            assertEquals(expectedOrder[i], rules.get(i).getRuleName(),
                    "Rule order mismatch at position " + (i + 1));
        }
        
        System.out.println("\n✓ Rule execution order verified");
        System.out.println("========================================\n");
    }

    @Test
    void testRuleRegistrationReport() {
        System.out.println("\n========================================");
        System.out.println("RULE REGISTRATION REPORT");
        System.out.println("========================================");
        
        List<ValidationRule> rules = decisionValidator.getRules();
        
        System.out.println("Total Rules Registered: " + rules.size());
        System.out.println("Expected: 7");
        System.out.println("Status: " + (rules.size() == 7 ? "✓ PASS" : "✗ FAIL"));
        
        System.out.println("\nRule Details:");
        for (int i = 0; i < rules.size(); i++) {
            ValidationRule rule = rules.get(i);
            System.out.println("\n  " + (i + 1) + ". " + rule.getRuleName());
            System.out.println("     Class: " + rule.getClass().getName());
            System.out.println("     Package: " + rule.getClass().getPackage().getName());
        }
        
        System.out.println("\n========================================");
        System.out.println("PIPELINE VERIFICATION REPORT");
        System.out.println("========================================");
        System.out.println("Rule Count: " + rules.size() + "/7");
        System.out.println("No Duplicates: " + (rules.stream().map(ValidationRule::getRuleName).distinct().count() == 7 ? "✓" : "✗"));
        System.out.println("No Missing Rules: " + (rules.size() == 7 ? "✓" : "✗"));
        System.out.println("Spring Injection: ✓");
        System.out.println("Component Scanning: ✓");
        System.out.println("========================================\n");
        
        assertEquals(7, rules.size(), "Rule registration failed");
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private ConversationSession createValidSession() {
        ConversationSession session = new ConversationSession();
        session.setSessionId("test-session-123");
        session.setUserId("test-user");
        return session;
    }

    private ResolvedContext createValidContext() {
        return new ResolvedContext(
                ResolvedContext.Mode.CHAT,
                null, 0, 0,
                false, false,
                null,
                false
        );
    }
}
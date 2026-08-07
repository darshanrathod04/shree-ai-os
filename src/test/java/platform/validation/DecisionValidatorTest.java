package platform.validation;

import com.shreeai.os.platform.capability.Capability;
import com.shreeai.os.platform.capability.CapabilityRegistry;
import com.shreeai.os.platform.cognition.Thought;
import com.shreeai.os.platform.context.ConversationSession;
import com.shreeai.os.platform.production.ResolvedContext;
import com.shreeai.os.platform.validation.DecisionValidator;
import com.shreeai.os.platform.validation.ValidationMetrics;
import com.shreeai.os.platform.validation.ValidationResult;
import com.shreeai.os.platform.validation.ValidationStatus;
import com.shreeai.os.platform.validation.rules.DecisionExistsRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DecisionValidator with rule pipeline.
 */
class DecisionValidatorTest {

    @Mock
    private CapabilityRegistry capabilityRegistry;

    @Mock
    private Capability capability;

    private DecisionValidator validator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Create validator with DecisionExistsRule for testing
        DecisionExistsRule decisionExistsRule = new DecisionExistsRule();
        validator = new DecisionValidator(List.of(decisionExistsRule), new ValidationMetrics());
    }

    // =====================================================
    // VALID DECISIONS
    // =====================================================

    @Test
    @DisplayName("Should validate decision with DecisionExistsRule")
    void testValidDecisionWithRule() {
        Thought decision = new Thought("test goal", "LEARN", "LearningSessionEngine", "test reasoning");
        ConversationSession session = createValidSession();
        ResolvedContext context = createValidContext();

        ValidationResult result = validator.validate(decision, session, context);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertEquals(ValidationStatus.VALID, result.getStatus());
        assertNotNull(result.getValidationId());
        assertNotNull(result.getDecisionId());
        assertNotNull(result.getTimestamp());
        assertNotNull(result.getTrace());
        assertEquals(1, result.getTrace().getSteps().size());
    }

    // =====================================================
    // NULL SAFETY
    // =====================================================

    @Test
    @DisplayName("Should handle null decision")
    void testNullDecision() {
        ValidationResult result = validator.validate(null, null, null);

        assertNotNull(result);
        assertFalse(result.isValid());
        assertEquals(ValidationStatus.INVALID, result.getStatus());
        assertFalse(result.getErrors().isEmpty());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Null Decision")));
    }

    @Test
    @DisplayName("Should handle null session")
    void testNullSession() {
        Thought decision = new Thought("test goal", "LEARN", "LearningSessionEngine", "test reasoning");
        ResolvedContext context = createValidContext();

        ValidationResult result = validator.validate(decision, null, context);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertNotNull(result.getTrace());
    }

    @Test
    @DisplayName("Should handle null context")
    void testNullContext() {
        Thought decision = new Thought("test goal", "LEARN", "LearningSessionEngine", "test reasoning");
        ConversationSession session = createValidSession();

        ValidationResult result = validator.validate(decision, session, null);

        assertNotNull(result);
        assertTrue(result.isValid());
        assertNotNull(result.getTrace());
    }

    // =====================================================
    // TRACE VALIDATION
    // =====================================================

    @Test
    @DisplayName("Should include trace in result")
    void testTraceIncluded() {
        Thought decision = new Thought("test goal", "LEARN", "LearningSessionEngine", "test reasoning");
        ConversationSession session = createValidSession();
        ResolvedContext context = createValidContext();

        ValidationResult result = validator.validate(decision, session, context);

        assertNotNull(result.getTrace());
        assertNotNull(result.getTrace().getTraceId());
        assertNotNull(result.getTrace().getStartTime());
        assertNotNull(result.getTrace().getEndTime());
        assertTrue(result.getTrace().getTotalDurationNanos() >= 0);
    }

    // =====================================================
    // METRICS
    // =====================================================

    @Test
    @DisplayName("Should record metrics")
    void testMetricsRecorded() {
        Thought decision = new Thought("test goal", "LEARN", "LearningSessionEngine", "test reasoning");
        ConversationSession session = createValidSession();
        ResolvedContext context = createValidContext();

        validator.validate(decision, session, context);
        validator.validate(decision, session, context);

        ValidationMetrics metrics = validator.getMetrics();
        assertEquals(2, metrics.getTotalValidations());
        assertEquals(2, metrics.getSuccessfulValidations());
    }

    // =====================================================
    // SHADOW MODE
    // =====================================================

    @Test
    @DisplayName("Should not affect production execution")
    void testShadowMode() {
        Thought decision = new Thought("test goal", "LEARN", "LearningSessionEngine", "test reasoning");
        ConversationSession session = createValidSession();
        ResolvedContext context = createValidContext();

        ValidationResult result = validator.validate(decision, session, context);

        assertTrue(result.isValid());
        assertNotNull(result.getTrace());
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
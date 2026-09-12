package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.IntentCandidate;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.PrimaryIntent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultPrimaryIntentDetector}.
 */
public class DefaultPrimaryIntentDetectorTest {

    private DefaultPrimaryIntentDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DefaultPrimaryIntentDetector();
    }

    @Test
    @DisplayName("Test 1: LEARN intent detected for learning keywords")
    void testLearnIntentDetected() {
        IntentProfile profile = detector.detect("I want to learn Java programming");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.LEARN, profile.primaryIntent(),
                "Should detect LEARN intent for 'learn' keyword");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 2: BUILD intent detected for building keywords")
    void testBuildIntentDetected() {
        IntentProfile profile = detector.detect("Help me build a web application");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.BUILD, profile.primaryIntent(),
                "Should detect BUILD intent for 'build' keyword");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 3: DEBUG intent detected for debugging keywords")
    void testDebugIntentDetected() {
        IntentProfile profile = detector.detect("I need to debug this error in my code");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.DEBUG, profile.primaryIntent(),
                "Should detect DEBUG intent for 'debug' and 'error' keywords");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 4: EXPLAIN intent detected for explanation keywords")
    void testExplainIntentDetected() {
        IntentProfile profile = detector.detect("Can you explain what quantum computing is?");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.EXPLAIN, profile.primaryIntent(),
                "Should detect EXPLAIN intent for 'explain' and 'what' keywords");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 5: COMPARE intent detected for comparison keywords")
    void testCompareIntentDetected() {
        IntentProfile profile = detector.detect("Compare Java vs Python for web development");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.COMPARE, profile.primaryIntent(),
                "Should detect COMPARE intent for 'compare' and 'vs' keywords");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 6: ANALYZE intent detected for analysis keywords")
    void testAnalyzeIntentDetected() {
        IntentProfile profile = detector.detect("Analyze the performance of this algorithm");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.ANALYZE, profile.primaryIntent(),
                "Should detect ANALYZE intent for 'analyze' and 'performance' keywords");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 7: PLAN intent detected for planning keywords")
    void testPlanIntentDetected() {
        IntentProfile profile = detector.detect("Help me plan the architecture for this project");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.PLAN, profile.primaryIntent(),
                "Should detect PLAN intent for 'plan' and 'architecture' keywords");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 8: REVIEW intent detected for review keywords")
    void testReviewIntentDetected() {
        IntentProfile profile = detector.detect("Please review this code and give feedback");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.REVIEW, profile.primaryIntent(),
                "Should detect REVIEW intent for 'review' and 'feedback' keywords");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 9: EXECUTE intent detected for execution keywords")
    void testExecuteIntentDetected() {
        IntentProfile profile = detector.detect("Run the deployment script now");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.EXECUTE, profile.primaryIntent(),
                "Should detect EXECUTE intent for 'run' and 'deployment' keywords");
        assertTrue(profile.confidence() > 0.0, "Confidence should be positive");
    }

    @Test
    @DisplayName("Test 10: Empty input returns UNKNOWN intent")
    void testEmptyInputReturnsUnknown() {
        IntentProfile profile = detector.detect("");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.UNKNOWN, profile.primaryIntent(),
                "Empty input should return UNKNOWN");
        assertEquals(0.0, profile.confidence(), "Confidence should be 0 for empty input");
    }

    @Test
    @DisplayName("Test 11: Whitespace-only input returns UNKNOWN intent")
    void testWhitespaceInputReturnsUnknown() {
        IntentProfile profile = detector.detect("   ");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.UNKNOWN, profile.primaryIntent(),
                "Whitespace-only input should return UNKNOWN");
    }

    @Test
    @DisplayName("Test 12: Confidence is always between 0.0 and 1.0")
    void testConfidenceInRange() {
        String[] inputs = {
                "learn Java", "build app", "debug error", "explain concept",
                "compare options", "analyze data", "plan project", "review code",
                "run script", "random xyz nonsense"
        };
        for (String input : inputs) {
            IntentProfile profile = detector.detect(input);
            assertTrue(profile.confidence() >= 0.0 && profile.confidence() <= 1.0,
                    "Confidence must be in [0.0, 1.0] for input: " + input);
        }
    }

    @Test
    @DisplayName("Test 13: Alternatives are sorted by confidence descending")
    void testAlternativesSortedByConfidence() {
        IntentProfile profile = detector.detect("I want to learn and build a Java application");
        List<IntentCandidate> alternatives = profile.alternatives();
        assertNotNull(alternatives, "Alternatives must not be null");
        for (int i = 0; i < alternatives.size() - 1; i++) {
            assertTrue(alternatives.get(i).confidence() >= alternatives.get(i + 1).confidence(),
                    "Alternatives should be sorted by confidence descending");
        }
    }

    @Test
    @DisplayName("Test 14: Same input returns same output (determinism)")
    void testDeterminism() {
        String input = "I want to learn Java programming";
        IntentProfile profile1 = detector.detect(input);
        IntentProfile profile2 = detector.detect(input);
        assertEquals(profile1.primaryIntent(), profile2.primaryIntent(),
                "Primary intent should be deterministic");
        assertEquals(profile1.confidence(), profile2.confidence(),
                "Confidence should be deterministic");
        assertEquals(profile1.alternatives().size(), profile2.alternatives().size(),
                "Alternatives count should be deterministic");
    }

    @Test
    @DisplayName("Test 15: Detection method is set correctly")
    void testDetectionMethod() {
        IntentProfile profile = detector.detect("learn Java");
        assertNotNull(profile.detectionMethod(), "Detection method must not be null");
        assertFalse(profile.detectionMethod().isEmpty(), "Detection method must not be empty");
    }

    @Test
    @DisplayName("Test 16: DetectedAt timestamp is set")
    void testDetectedAtSet() {
        IntentProfile profile = detector.detect("learn Java");
        assertNotNull(profile.detectedAt(), "DetectedAt must not be null");
    }

    @Test
    @DisplayName("Test 17: Case insensitivity")
    void testCaseInsensitivity() {
        IntentProfile profile1 = detector.detect("LEARN Java");
        IntentProfile profile2 = detector.detect("learn java");
        IntentProfile profile3 = detector.detect("LeArN JaVa");
        assertEquals(profile1.primaryIntent(), profile2.primaryIntent(),
                "Intent detection should be case-insensitive");
        assertEquals(profile2.primaryIntent(), profile3.primaryIntent(),
                "Intent detection should be case-insensitive");
    }

    @Test
    @DisplayName("Test 18: Unknown input returns UNKNOWN")
    void testUnknownInput() {
        IntentProfile profile = detector.detect("xyz abc 123 random");
        assertNotNull(profile, "Profile must not be null");
        assertEquals(PrimaryIntent.UNKNOWN, profile.primaryIntent(),
                "Random input should return UNKNOWN");
    }

    @Test
    @DisplayName("Test 19: Alternatives do not include primary intent")
    void testAlternativesExcludePrimary() {
        IntentProfile profile = detector.detect("I want to learn and build a Java application");
        List<IntentCandidate> alternatives = profile.alternatives();
        for (IntentCandidate candidate : alternatives) {
            assertNotEquals(profile.primaryIntent(), candidate.intent(),
                    "Alternatives should not include primary intent");
        }
    }

    @Test
    @DisplayName("Test 20: Multiple keywords increase confidence")
    void testMultipleKeywordsIncreaseConfidence() {
        IntentProfile singleKeyword = detector.detect("learn");
        IntentProfile multipleKeywords = detector.detect("learn study understand tutorial");
        assertTrue(multipleKeywords.confidence() >= singleKeyword.confidence(),
                "Multiple keywords should increase or maintain confidence");
    }
}
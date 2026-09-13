package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.ExperienceLevel;
import com.shreeai.os.platform.kernels.context.model.OutputPreference;
import com.shreeai.os.platform.kernels.context.model.PlatformType;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultConstraintExtractionEngine}.
 */
public class DefaultConstraintExtractionEngineTest {

    private DefaultConstraintExtractionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultConstraintExtractionEngine();
    }

    @Test
    @DisplayName("Test 1: Duration extracted from 'in 30 days'")
    void testDurationExtracted() {
        UserConstraints constraints = engine.extract("Learn Java in 30 days");
        assertNotNull(constraints);
        assertEquals("30 days", constraints.duration());
        assertTrue(constraints.evidence().stream().anyMatch(e -> e.field().equals("duration")));
    }

    @Test
    @DisplayName("Test 2: Duration extracted from 'within 2 weeks'")
    void testDurationWithin() {
        UserConstraints constraints = engine.extract("Complete the course within 2 weeks");
        assertNotNull(constraints);
        assertEquals("2 weeks", constraints.duration());
    }

    @Test
    @DisplayName("Test 3: Budget extracted from '₹5000'")
    void testBudgetExtracted() {
        UserConstraints constraints = engine.extract("I have a budget of ₹5000");
        assertNotNull(constraints);
        assertNotNull(constraints.budget());
        assertTrue(constraints.budget().contains("5000"));
    }

    @Test
    @DisplayName("Test 4: Budget extracted from '$200'")
    void testBudgetDollar() {
        UserConstraints constraints = engine.extract("My budget is $200");
        assertNotNull(constraints);
        assertNotNull(constraints.budget());
        assertTrue(constraints.budget().contains("200"));
    }

    @Test
    @DisplayName("Test 5: Experience level extracted for 'beginner'")
    void testExperienceBeginner() {
        UserConstraints constraints = engine.extract("I am a beginner in programming");
        assertNotNull(constraints);
        assertEquals(ExperienceLevel.BEGINNER, constraints.experience());
    }

    @Test
    @DisplayName("Test 6: Experience level extracted for 'advanced'")
    void testExperienceAdvanced() {
        UserConstraints constraints = engine.extract("I am an advanced Java developer");
        assertNotNull(constraints);
        assertEquals(ExperienceLevel.ADVANCED, constraints.experience());
    }

    @Test
    @DisplayName("Test 7: Platform extracted for 'Windows'")
    void testPlatformWindows() {
        UserConstraints constraints = engine.extract("I am using Windows");
        assertNotNull(constraints);
        assertEquals(PlatformType.WINDOWS, constraints.platform());
    }

    @Test
    @DisplayName("Test 8: Platform extracted for 'MacBook'")
    void testPlatformMac() {
        UserConstraints constraints = engine.extract("I have a MacBook");
        assertNotNull(constraints);
        assertEquals(PlatformType.MAC, constraints.platform());
    }

    @Test
    @DisplayName("Test 9: Output preference extracted for 'roadmap'")
    void testOutputPreferenceRoadmap() {
        UserConstraints constraints = engine.extract("Give me a roadmap for learning Java");
        assertNotNull(constraints);
        assertEquals(OutputPreference.ROADMAP, constraints.outputPreference());
    }

    @Test
    @DisplayName("Test 10: Output preference extracted for 'complete code'")
    void testOutputPreferenceCode() {
        UserConstraints constraints = engine.extract("I need complete code examples");
        assertNotNull(constraints);
        assertEquals(OutputPreference.CODE, constraints.outputPreference());
    }

    @Test
    @DisplayName("Test 11: Missing values remain null")
    void testMissingValuesNull() {
        UserConstraints constraints = engine.extract("Hello world");
        assertNotNull(constraints);
        assertNull(constraints.duration());
        assertNull(constraints.budget());
        assertNull(constraints.experience());
        assertNull(constraints.platform());
        assertNull(constraints.outputPreference());
    }

    @Test
    @DisplayName("Test 12: Multiple constraints together")
    void testMultipleConstraints() {
        UserConstraints constraints = engine.extract(
                "I am a beginner, I have ₹5000 budget, and I need a roadmap for learning Java in 30 days");
        assertNotNull(constraints);
        assertEquals("30 days", constraints.duration());
        assertNotNull(constraints.budget());
        assertEquals(ExperienceLevel.BEGINNER, constraints.experience());
        assertEquals(OutputPreference.ROADMAP, constraints.outputPreference());
    }

    @Test
    @DisplayName("Test 13: Same input returns same output (determinism)")
    void testDeterminism() {
        String input = "I am a beginner with $200 budget, give me a roadmap in 30 days";
        UserConstraints constraints1 = engine.extract(input);
        UserConstraints constraints2 = engine.extract(input);
        assertEquals(constraints1, constraints2);
    }

    @Test
    @DisplayName("Test 14: Empty input returns empty constraints")
    void testEmptyInput() {
        UserConstraints constraints = engine.extract("");
        assertNotNull(constraints);
        assertNotNull(constraints.evidence());
        assertTrue(constraints.evidence().isEmpty());
    }

    @Test
    @DisplayName("Test 15: Evidence list tracks extraction sources")
    void testEvidenceTracking() {
        UserConstraints constraints = engine.extract("I need a roadmap for beginners in 30 days");
        assertNotNull(constraints);
        assertTrue(constraints.evidence().size() >= 2, "Should have evidence for duration and experience");
    }
}
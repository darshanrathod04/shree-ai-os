package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.AmbiguityProfile;
import com.shreeai.os.platform.kernels.context.model.AmbiguityReason;
import com.shreeai.os.platform.kernels.context.model.AmbiguityType;
import com.shreeai.os.platform.kernels.context.model.ContextIntelligence;
import com.shreeai.os.platform.kernels.context.model.DomainCandidate;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.PrimaryDomain;
import com.shreeai.os.platform.kernels.context.model.PrimaryIntent;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultAmbiguityDetectionEngine}.
 */
public class DefaultAmbiguityDetectionEngineTest {

    private DefaultAmbiguityDetectionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new DefaultAmbiguityDetectionEngine();
    }

    /**
     * Runs the complete deterministic context chain the way ContextStage does.
     */
    private AmbiguityProfile diagnose(String input) {
        IntentProfile intent = new DefaultPrimaryIntentDetector().detect(input);
        DomainProfile domain = new DefaultDomainDetector().detect(input);
        UserConstraints constraints = new DefaultConstraintExtractionEngine().extract(input);
        GoalStructure goals = new DefaultGoalIdentificationEngine().identify(input);
        return engine.diagnose(intent, domain, constraints, goals);
    }

    private boolean hasType(AmbiguityProfile profile, AmbiguityType type) {
        return profile.reasons().stream().anyMatch(r -> r.type() == type);
    }

    private AmbiguityReason reasonOf(AmbiguityProfile profile, AmbiguityType type) {
        return profile.reasons().stream()
                .filter(r -> r.type() == type)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing reason: " + type));
    }

    @Test
    @DisplayName("Test 1: Missing platform detected for a mobile request without a platform")
    void testMissingPlatformDetected() {
        AmbiguityProfile profile = diagnose("Build a mobile app");
        assertTrue(profile.ambiguityDetected(), "Ambiguity must be detected");
        assertTrue(hasType(profile, AmbiguityType.MISSING_PLATFORM),
                "MISSING_PLATFORM must be diagnosed for a mobile request without a platform");
        assertEquals(0.50, reasonOf(profile, AmbiguityType.MISSING_PLATFORM).severity());
    }

    @Test
    @DisplayName("Test 2: Missing domain detected for a BUILD request without a domain")
    void testMissingDomainDetected() {
        AmbiguityProfile profile = diagnose("Build software");
        assertTrue(profile.ambiguityDetected());
        assertTrue(hasType(profile, AmbiguityType.MISSING_DOMAIN));
        assertEquals(0.75, reasonOf(profile, AmbiguityType.MISSING_DOMAIN).severity());
    }
    @Test
    @DisplayName("Test 3: Missing goal detected for a help request without an objective")
    void testMissingGoalDetected() {
        AmbiguityProfile profile = diagnose("Help me");
        assertTrue(profile.ambiguityDetected());
        assertTrue(hasType(profile, AmbiguityType.MISSING_GOAL));
        assertEquals(1.00, reasonOf(profile, AmbiguityType.MISSING_GOAL).severity());
    }

    @Test
    @DisplayName("Test 4: Multiple domains detected when no domain dominates")
    void testMultipleDomainsDetected() {
        AmbiguityProfile profile = diagnose("Build Flutter + React + Spring project");
        assertTrue(profile.ambiguityDetected());
        assertTrue(hasType(profile, AmbiguityType.MULTIPLE_DOMAINS),
                "Competing Flutter (MOBILE), React (WEB) and Spring (SPRING) must be flagged");
        assertEquals(0.50, reasonOf(profile, AmbiguityType.MULTIPLE_DOMAINS).severity());
    }

    @Test
    @DisplayName("Test 5: Generic request detected for bare action verbs")
    void testGenericRequestDetected() {
        assertTrue(hasType(diagnose("Build"), AmbiguityType.GENERIC_REQUEST),
                "'Build' must be a generic request");
        assertTrue(hasType(diagnose("Explain"), AmbiguityType.GENERIC_REQUEST),
                "'Explain' must be a generic request");
        assertTrue(hasType(diagnose("Create project"), AmbiguityType.GENERIC_REQUEST),
                "'Create project' must be a generic request");
    }

    @Test
    @DisplayName("Test 6: Ambiguity score is deterministic for identical input")
    void testAmbiguityScoreDeterministic() {
        double score1 = diagnose("Build software").ambiguityScore();
        double score2 = diagnose("Build software").ambiguityScore();
        assertEquals(score1, score2);
    }

    @Test
    @DisplayName("Test 7: Same input produces an identical AmbiguityProfile")
    void testSameInputProducesIdenticalOutput() {
        AmbiguityProfile profile1 = diagnose("Build a mobile app");
        AmbiguityProfile profile2 = diagnose("Build a mobile app");
        assertEquals(profile1, profile2,
                "The full profile (types, severities, scores, explanation) must be identical");
    }

    @Test
    @DisplayName("Test 8: Structurally complete request has no ambiguity")
    void testCompleteRequestHasNoAmbiguity() {
        AmbiguityProfile profile = diagnose("Explain recursion in Java");
        assertFalse(profile.ambiguityDetected(), "A complete request must not be ambiguous");
        assertEquals(0.0, profile.ambiguityScore());
        assertTrue(profile.reasons().isEmpty());
    }

    @Test
    @DisplayName("Test 9: Severity levels are locked to the four canonical values")
    void testSeverityLevelsAreLocked() {
        assertEquals(0.25, AmbiguityReason.SEVERITY_LOW);
        assertEquals(0.50, AmbiguityReason.SEVERITY_MEDIUM);
        assertEquals(0.75, AmbiguityReason.SEVERITY_HIGH);
        assertEquals(1.00, AmbiguityReason.SEVERITY_CRITICAL);
        assertThrows(IllegalArgumentException.class,
                () -> new AmbiguityReason(AmbiguityType.MISSING_GOAL, "x", 0.37),
                "Arbitrary severity decimals must be rejected");
    }
    @Test
    @DisplayName("Test 10: Ambiguity score is clamped to the 0.0 - 1.0 range")
    void testScoreIsClampedToOne() {
        IntentProfile intent = new IntentProfile(PrimaryIntent.PLAN, 0.8, List.of(),
                Instant.now(), "test");
        DomainProfile domain = new DomainProfile(PrimaryDomain.GENERAL, 0.3,
                List.of(new DomainCandidate(PrimaryDomain.WEB, 0.40),
                        new DomainCandidate(PrimaryDomain.SPRING, 0.35)),
                Instant.now(), "test");
        UserConstraints constraints = UserConstraints.empty();
        GoalStructure goals = GoalStructure.none("test");

        AmbiguityProfile profile = engine.diagnose(intent, domain, constraints, goals);
        assertEquals(1.0, profile.ambiguityScore(),
                "Multiple severities summing above 1.0 must be clamped to 1.0");
        assertTrue(profile.ambiguityDetected());
        assertTrue(hasType(profile, AmbiguityType.MISSING_DOMAIN));
    }

    @Test
    @DisplayName("Test 11: Reasons are sorted deterministically by severity descending")
    void testReasonsSortedBySeverity() {
        AmbiguityProfile profile = diagnose("Help me");
        assertEquals(AmbiguityType.MISSING_GOAL, profile.reasons().get(0).type(),
                "CRITICAL missing-goal must be listed before HIGH missing-domain");
        assertEquals(AmbiguityType.MISSING_DOMAIN, profile.reasons().get(1).type());
    }

    @Test
    @DisplayName("Test 12: Empty input is flagged as insufficient context")
    void testEmptyInputFlagsInsufficientContext() {
        AmbiguityProfile profile = diagnose("");
        assertTrue(profile.ambiguityDetected());
        assertTrue(hasType(profile, AmbiguityType.INSUFFICIENT_CONTEXT));
    }

    @Test
    @DisplayName("Test 13: ContextIntelligence aggregates the five canonical artifacts")
    void testContextIntelligenceAggregate() {
        IntentProfile intent = new IntentProfile(PrimaryIntent.LEARN, 0.8, List.of(),
                Instant.now(), "test");
        DomainProfile domain = new DomainProfile(PrimaryDomain.GENERAL, 0.3, List.of(),
                Instant.now(), "test");
        UserConstraints constraints = UserConstraints.empty();
        GoalStructure goals = GoalStructure.none("test");
        AmbiguityProfile ambiguity = AmbiguityProfile.none();

        ContextIntelligence intelligence = ContextIntelligence.of(
                intent, domain, constraints, goals, ambiguity);
        assertEquals(intent, intelligence.intentProfile());
        assertEquals(domain, intelligence.domainProfile());
        assertEquals(constraints, intelligence.constraints());
        assertEquals(goals, intelligence.goals());
        assertEquals(ambiguity, intelligence.ambiguityProfile());

        ContextIntelligence updated = intelligence.withAmbiguityProfile(ambiguity);
        assertEquals(ambiguity, updated.ambiguityProfile());
        assertEquals(ambiguity, intelligence.ambiguityProfile(),
                "The original aggregate must remain immutable");
    }

    @Test
    @DisplayName("Test 14: Diagnosis never mutates the input artifacts")
    void testEngineNeverMutatesArtifacts() {
        IntentProfile intent = new IntentProfile(PrimaryIntent.LEARN, 0.8, List.of(),
                Instant.now(), "test");
        DomainProfile domain = new DomainProfile(PrimaryDomain.GENERAL, 0.3, List.of(),
                Instant.now(), "test");
        UserConstraints constraints = UserConstraints.empty();
        GoalStructure goals = GoalStructure.none("test");

        engine.diagnose(intent, domain, constraints, goals);

        assertEquals(PrimaryIntent.LEARN, intent.primaryIntent());
        assertEquals(0.8, intent.confidence());
        assertEquals("test", intent.detectionMethod());
        assertEquals(PrimaryDomain.GENERAL, domain.primaryDomain());
        assertEquals(0.3, domain.confidence());
        assertEquals("test", domain.detectionMethod());
        assertEquals(GoalStructure.none("test").primaryGoal().title(), goals.primaryGoal().title());
        assertTrue(constraints.evidence().isEmpty());
    }
}
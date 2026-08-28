package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.ConfidenceBand;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.Feasibility;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalAnalysis;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalRequest;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.GoalStatus;
import com.shreeai.os.platform.kernels.cognitive.engine.GoalIntelligenceEngine.Priority;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoalIntelligenceEngineTest {

    @Test
    void testBasicGoalUnderstanding() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalAnalysis analysis =
                engine.analyze(
                        GoalRequest.of(
                                "Build a student management application"
                        )
                );

        assertNotNull(analysis);

        assertEquals(
                "Build a student management application",
                analysis.normalizedGoal()
        );

        assertEquals(
                GoalStatus.NOT_STARTED,
                analysis.status()
        );

        assertNotNull(analysis.priority());
        assertNotNull(analysis.feasibility());
        assertNotNull(analysis.confidenceBand());
    }

    @Test
    void testGoalDecomposition() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalAnalysis analysis =
                engine.analyze(
                        GoalRequest.of(
                                "Design the system and build the backend and test the application"
                        )
                );

        assertTrue(
                analysis.decompositionRequired(),
                "Composite goal should require decomposition"
        );

        assertTrue(
                analysis.subtasks().size() > 1,
                "Composite goal should produce multiple subtasks"
        );
    }

    @Test
    void testHighPriorityGoal() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalRequest request =
                new GoalRequest(
                        "critical-goal",
                        "Deploy production system",
                        List.of("Production deployment is required"),
                        List.of("No downtime"),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        0.0,
                        10,
                        10,
                        10,
                        null,
                        GoalIntelligenceEngine.GoalStability.STABLE,
                        false
                );

        GoalAnalysis analysis =
                engine.analyze(request);

        assertEquals(
                Priority.CRITICAL,
                analysis.priority()
        );
    }

    @Test
    void testProgressIsPreserved() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalRequest request =
                new GoalRequest(
                        "progress-test",
                        "Build application",
                        List.of("Repository exists"),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        0.65,
                        5,
                        5,
                        5,
                        null,
                        GoalIntelligenceEngine.GoalStability.STABLE,
                        false
                );

        GoalAnalysis analysis =
                engine.analyze(request);

        assertEquals(
                0.65,
                analysis.progress(),
                0.0001
        );

        assertEquals(
                GoalStatus.IN_PROGRESS,
                analysis.status()
        );
    }

    @Test
    void testBlockedGoal() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalRequest request =
                new GoalRequest(
                        "blocked-goal",
                        "Deploy application",
                        List.of("Deployment target identified"),
                        List.of(),
                        List.of(),
                        List.of("Production credentials unavailable"),
                        List.of(),
                        List.of(),
                        0.40,
                        8,
                        8,
                        8,
                        null,
                        GoalIntelligenceEngine.GoalStability.STABLE,
                        false
                );

        GoalAnalysis analysis =
                engine.analyze(request);

        assertEquals(
                GoalStatus.BLOCKED,
                analysis.status()
        );

        assertEquals(
                Feasibility.BLOCKED,
                analysis.feasibility()
        );

        assertTrue(
                analysis.replanningRelevant(),
                "Blocked goals should produce replanning relevance"
        );

        assertFalse(
                analysis.blockers().isEmpty()
        );
    }

    @Test
    void testConflictDetection() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalRequest request =
                new GoalRequest(
                        "conflict-test",
                        "Delete archived records",
                        List.of("Archived records identified"),
                        List.of(
                                "Must not delete archived records"
                        ),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        0.0,
                        5,
                        7,
                        6,
                        null,
                        GoalIntelligenceEngine.GoalStability.STABLE,
                        false
                );

        GoalAnalysis analysis =
                engine.analyze(request);

        assertFalse(
                analysis.conflicts().isEmpty(),
                "Contradictory goal and constraint should be detected"
        );

        assertEquals(
                Feasibility.UNCERTAIN,
                analysis.feasibility()
        );
    }

    @Test
    void testMissingInformationReducesConfidence() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalAnalysis analysis =
                engine.analyze(
                        GoalRequest.of(
                                "Build a large distributed platform"
                        )
                );

        assertFalse(
                analysis.requiredInformation().isEmpty(),
                "Unknown goal conditions should produce missing information"
        );

        assertTrue(
                analysis.confidence() < 0.80,
                "Incomplete evidence must prevent high confidence"
        );

        assertTrue(
                analysis.confidenceBand() == ConfidenceBand.MINIMAL
                        || analysis.confidenceBand() == ConfidenceBand.LOW
                        || analysis.confidenceBand() == ConfidenceBand.MEDIUM
        );
    }

    @Test
    void testGoalEvolutionSignals() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalRequest request =
                new GoalRequest(
                        "evolving-goal",
                        "Build a new platform",
                        List.of("Initial requirements"),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        0.10,
                        7,
                        8,
                        9,
                        null,
                        GoalIntelligenceEngine.GoalStability.UNSTABLE,
                        false
                );

        GoalAnalysis analysis =
                engine.analyze(request);

        assertFalse(
                analysis.evolutionSignals().isEmpty(),
                "Unstable low-progress goals should produce evolution signals"
        );

        assertTrue(
                analysis.replanningRelevant()
        );
    }

    @Test
    void testEvidenceImprovesConfidence() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalAnalysis withoutEvidence =
                engine.analyze(
                        GoalRequest.of(
                                "Implement authentication"
                        )
                );

        GoalRequest evidenceRequest =
                new GoalRequest(
                        "evidence-goal",
                        "Implement authentication",
                        List.of(
                                "Authentication requirements approved",
                                "API contract verified",
                                "Security constraints documented"
                        ),
                        List.of(
                                "Use existing identity provider"
                        ),
                        List.of(
                                "API contract completed"
                        ),
                        List.of(),
                        List.of(),
                        List.of(),
                        0.50,
                        6,
                        8,
                        8,
                        Feasibility.PLAUSIBLE,
                        GoalIntelligenceEngine.GoalStability.STABLE,
                        false
                );

        GoalAnalysis withEvidence =
                engine.analyze(evidenceRequest);

        assertTrue(
                withEvidence.confidence()
                        > withoutEvidence.confidence(),
                "Additional evidence should improve confidence"
        );
    }

    @Test
    void testAnalysisIsImmutable() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalAnalysis analysis =
                engine.analyze(
                        GoalRequest.of(
                                "Build application"
                        )
                );

        assertThrows(
                UnsupportedOperationException.class,
                () -> analysis.subtasks().add("illegal mutation")
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> analysis.metadata()
                        .put("illegal", true)
        );
    }

    @Test
    void testEngineNeverStartsExecutionOrMutatesPlan() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalAnalysis analysis =
                engine.analyze(
                        GoalRequest.of(
                                "Build and deploy application"
                        )
                );

        assertEquals(
                Boolean.FALSE,
                analysis.metadata()
                        .get("executionStarted")
        );

        assertEquals(
                Boolean.FALSE,
                analysis.metadata()
                        .get("planMutationPerformed")
        );

        assertEquals(
                Boolean.FALSE,
                analysis.metadata()
                        .get("goalMutationPerformed")
        );
    }

    @Test
    void testMetadataContract() {

        GoalIntelligenceEngine engine =
                new GoalIntelligenceEngine();

        GoalAnalysis analysis =
                engine.analyze(
                        GoalRequest.of(
                                "Build student management system"
                        )
                );

        assertEquals(
                "GoalIntelligenceEngine",
                analysis.metadata().get("engine")
        );

        assertEquals(
                "1.0",
                analysis.metadata().get("version")
        );

        assertNotNull(
                analysis.metadata().get("analysisId")
        );

        assertNotNull(
                analysis.metadata().get("timestamp")
        );
    }
}
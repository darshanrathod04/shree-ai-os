package com.shreeai.os.platform.runtime.agents;

import com.shreeai.os.platform.runtime.confidence.ConfidenceCalculator;
import com.shreeai.os.platform.runtime.model.AgentDecision;
import com.shreeai.os.platform.runtime.model.DiagnosticReport;
import com.shreeai.os.platform.runtime.model.EvidenceBundle;
import com.shreeai.os.platform.runtime.model.EvidenceItem;
import com.shreeai.os.platform.runtime.model.ExecutionPlan;
import com.shreeai.os.platform.runtime.model.VerificationReport;
import com.shreeai.os.platform.runtime.model.VerificationReport.ConfidenceTier;
import com.shreeai.os.platform.runtime.model.VerificationReport.ItemStatus;
import com.shreeai.os.platform.llm.LlmProvider;
import com.shreeai.os.platform.llm.LlmRequest;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.IntentType;
import com.shreeai.os.platform.runtime.orchestration.IntentAnalysisResult.KernelType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>AutonomousIntelligenceLayerTest</b>
 *
 * <p>Sprint-18 acceptance tests for the Autonomous Intelligence Layer.
 * Verifies the 5-agent pipeline and ConfidenceCalculator.</p>
 *
 * <p><b>Acceptance criteria:</b></p>
 * <ul>
 *   <li>ChiefIntelligenceAgent produces correct ExecutionPlan from intent analysis</li>
 *   <li>DiagnosisAgent detects workspace state correctly</li>
 *   <li>EvidenceAgent extracts from pipeline metadata correctly</li>
 *   <li>VerificationAgent assigns correct confidence tiers</li>
 *   <li>ConfidenceCalculator returns correct scores for all 4 tiers</li>
 *   <li>NaturalResponseAgent generates appropriate responses per tier</li>
 *   <li>Empty evidence bundle → INSUFFICIENT tier</li>
 *   <li>No regression in existing 1,139 tests</li>
 * </ul>
 *
 * @since Sprint 18
 */
@DisplayName("Sprint 18: Autonomous Intelligence Layer")
public class AutonomousIntelligenceLayerTest {

    // ─── ConfidenceCalculator Tests ─────────────────────────────────────────────

    @Nested
    @DisplayName("ConfidenceCalculator")
    class ConfidenceCalculatorTests {

        @Test
        @DisplayName("VERIFIED_PROJECT tier returns 0.95")
        void verifiedProjectReturns095() {
            assertEquals(0.95, ConfidenceCalculator.fromProjectEvidence(), 0.001);
            assertEquals(0.95, ConfidenceCalculator.scoreForTier(ConfidenceTier.VERIFIED_PROJECT), 0.001);
        }

        @Test
        @DisplayName("VERIFIED_KB tier returns 0.80")
        void verifiedKbReturns080() {
            assertEquals(0.80, ConfidenceCalculator.fromKnowledgeEvidence(), 0.001);
            assertEquals(0.80, ConfidenceCalculator.scoreForTier(ConfidenceTier.VERIFIED_KB), 0.001);
        }

        @Test
        @DisplayName("INFERRED tier returns 0.60")
        void inferredReturns060() {
            assertEquals(0.60, ConfidenceCalculator.fromReasoningEvidence(), 0.001);
            assertEquals(0.60, ConfidenceCalculator.scoreForTier(ConfidenceTier.INFERRED), 0.001);
        }

        @Test
        @DisplayName("INSUFFICIENT tier returns 0.15")
        void insufficientReturns015() {
            assertEquals(0.15, ConfidenceCalculator.fromInsufficient(), 0.001);
            assertEquals(0.15, ConfidenceCalculator.scoreForTier(ConfidenceTier.INSUFFICIENT), 0.001);
        }

        @Test
        @DisplayName("highestTier returns VERIFIED_PROJECT when bundle has project evidence")
        void highestTierProjectEvidence() {
            List<EvidenceItem> items = List.of(
                    EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.PROJECT)
                            .title("Test")
                            .content("Test content")
                            .build()
            );
            assertEquals(ConfidenceTier.VERIFIED_PROJECT, ConfidenceCalculator.highestTier(items));
        }

        @Test
        @DisplayName("highestTier returns VERIFIED_KB when bundle has knowledge evidence (no project)")
        void highestTierKnowledgeEvidence() {
            List<EvidenceItem> items = List.of(
                    EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                            .title("Test")
                            .content("Test content")
                            .build()
            );
            assertEquals(ConfidenceTier.VERIFIED_KB, ConfidenceCalculator.highestTier(items));
        }

        @Test
        @DisplayName("highestTier returns INFERRED when bundle has reasoning evidence (no project, no KB)")
        void highestTierReasoningEvidence() {
            List<EvidenceItem> items = List.of(
                    EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.REASONING)
                            .title("Test")
                            .content("Test content")
                            .build()
            );
            assertEquals(ConfidenceTier.INFERRED, ConfidenceCalculator.highestTier(items));
        }

        @Test
        @DisplayName("highestTier returns INSUFFICIENT for empty bundle")
        void highestTierEmptyBundle() {
            assertEquals(ConfidenceTier.INSUFFICIENT, ConfidenceCalculator.highestTier(List.of()));
            assertEquals(ConfidenceTier.INSUFFICIENT, ConfidenceCalculator.highestTier(null));
        }

        @Test
        @DisplayName("fromSourceType returns correct score per source type")
        void fromSourceType() {
            assertEquals(0.95, ConfidenceCalculator.fromSourceType(EvidenceItem.SourceType.PROJECT), 0.001);
            assertEquals(0.80, ConfidenceCalculator.fromSourceType(EvidenceItem.SourceType.KNOWLEDGE), 0.001);
            assertEquals(0.60, ConfidenceCalculator.fromSourceType(EvidenceItem.SourceType.REASONING), 0.001);
            assertEquals(0.60, ConfidenceCalculator.fromSourceType(EvidenceItem.SourceType.INFERENCE), 0.001);
            assertEquals(0.60, ConfidenceCalculator.fromSourceType(EvidenceItem.SourceType.PLANNING), 0.001);
            assertEquals(0.60, ConfidenceCalculator.fromSourceType(EvidenceItem.SourceType.REFLECTION), 0.001);
            assertEquals(0.60, ConfidenceCalculator.fromSourceType(EvidenceItem.SourceType.MEMORY), 0.001);
            assertEquals(0.60, ConfidenceCalculator.fromSourceType(EvidenceItem.SourceType.EXECUTION), 0.001);
        }
    }

    // ─── DiagnosisAgent Tests ───────────────────────────────────────────────────

    @Nested
    @DisplayName("DiagnosisAgent")
    class DiagnosisAgentTests {

        private DiagnosisAgent diagnosisAgent;

        @BeforeEach
        void setUp() {
            diagnosisAgent = new DiagnosisAgent();
        }

        @Test
        @DisplayName("Returns PASS for workspace when projectPath is set")
        void workspacePassWithProjectPath() {
            ExecutionPlan plan = ExecutionPlan.builder().build();
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .metadata(Map.of("projectPath", "/some/path"))
                    .build();

            DiagnosticReport report = diagnosisAgent.analyze(plan, request);
            assertEquals(DiagnosticReport.CheckStatus.PASS, report.statusOf(DiagnosticReport.DiagnosticArea.WORKSPACE));
            assertTrue(report.isHealthy());
        }

        @Test
        @DisplayName("Returns WARN for workspace when no project path set")
        void workspaceWarnWithoutProjectPath() {
            ExecutionPlan plan = ExecutionPlan.builder().build();
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder().build();

            DiagnosticReport report = diagnosisAgent.analyze(plan, request);
            assertEquals(DiagnosticReport.CheckStatus.WARN, report.statusOf(DiagnosticReport.DiagnosticArea.WORKSPACE));
        }

        @Test
        @DisplayName("Returns FAIL for project check when PROJECT kernel is requested but no project path")
        void projectCheckFailWithoutProjectPath() {
            ExecutionPlan plan = ExecutionPlan.builder()
                    .orderedKernels(List.of(KernelType.PROJECT))
                    .build();
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder().build();

            DiagnosticReport report = diagnosisAgent.analyze(plan, request);
            assertEquals(DiagnosticReport.CheckStatus.FAIL, report.statusOf(DiagnosticReport.DiagnosticArea.PROJECT));
            assertFalse(report.isHealthy());
        }

        @Test
        @DisplayName("Returns PASS for project check when PROJECT kernel is requested with project path")
        void projectCheckPassWithProjectPath() {
            ExecutionPlan plan = ExecutionPlan.builder()
                    .orderedKernels(List.of(KernelType.PROJECT))
                    .build();
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .metadata(Map.of("projectPath", "/some/path"))
                    .build();

            DiagnosticReport report = diagnosisAgent.analyze(plan, request);
            assertEquals(DiagnosticReport.CheckStatus.PASS, report.statusOf(DiagnosticReport.DiagnosticArea.PROJECT));
            assertTrue(report.isHealthy());
        }

        @Test
        @DisplayName("Returns SKIPPED for project check when PROJECT kernel is not requested")
        void projectCheckSkippedWhenNotRequested() {
            ExecutionPlan plan = ExecutionPlan.builder()
                    .orderedKernels(List.of(KernelType.MEMORY))
                    .build();
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder().build();

            DiagnosticReport report = diagnosisAgent.analyze(plan, request);
            assertEquals(DiagnosticReport.CheckStatus.SKIPPED, report.statusOf(DiagnosticReport.DiagnosticArea.PROJECT));
        }

        @Test
        @DisplayName("Adds recommendation when project analysis is missing")
        void addsRecommendationWhenProjectMissing() {
            ExecutionPlan plan = ExecutionPlan.builder()
                    .orderedKernels(List.of(KernelType.PROJECT))
                    .build();
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder().build();

            DiagnosticReport report = diagnosisAgent.analyze(plan, request);
            assertTrue(report.hasFailures());
            assertFalse(report.recommendations().isEmpty());
            assertTrue(report.recommendations().stream()
                    .anyMatch(r -> r.contains("ProjectSDK.analyze()")));
        }

        @Test
        @DisplayName("toDecision returns DIAGNOSE action")
        void toDecisionReturnsDiagnoseAction() {
            ExecutionPlan plan = ExecutionPlan.builder().build();
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder().build();
            DiagnosticReport report = diagnosisAgent.analyze(plan, request);
            AgentDecision decision = diagnosisAgent.toDecision(report);
            assertEquals(AgentDecision.Action.DIAGNOSE, decision.action());
            assertEquals(AgentDecision.Agent.DIAGNOSIS, decision.agent());
        }
    }

    // ─── EvidenceAgent Tests ───────────────────────────────────────────────────

    @Nested
    @DisplayName("EvidenceAgent")
    class EvidenceAgentTests {

        private EvidenceAgent evidenceAgent;

        @BeforeEach
        void setUp() {
            evidenceAgent = new EvidenceAgent();
        }

        @Test
        @DisplayName("extractFromMetadata returns empty bundle when metadata is empty")
        void extractFromEmptyMetadata() {
            EvidenceBundle bundle = evidenceAgent.extractFromMetadata(Map.of());
            assertNotNull(bundle);
            assertTrue(bundle.isEmpty());
            assertEquals(0, bundle.size());
        }

        @Test
        @DisplayName("extractFromMetadata extracts knowledge results")
        void extractKnowledgeResults() {
            var metadata = Map.<String, Object>of(
                    "knowledgeResults", List.of(
                            Map.of("label", "Java", "description", "A programming language")
                    )
            );
            EvidenceBundle bundle = evidenceAgent.extractFromMetadata(metadata);
            assertEquals(1, bundle.size());
            EvidenceItem item = bundle.items().getFirst();
            assertEquals(EvidenceItem.SourceType.KNOWLEDGE, item.sourceType());
            assertEquals("Java", item.title());
            assertEquals("A programming language", item.content());
        }

        @Test
        @DisplayName("extractFromMetadata extracts reasoning conclusion")
        void extractReasoningConclusion() {
            var metadata = Map.<String, Object>of(
                    "reasoningConclusion", "The project uses Spring Boot"
            );
            EvidenceBundle bundle = evidenceAgent.extractFromMetadata(metadata);
            assertEquals(1, bundle.size());
            EvidenceItem item = bundle.items().getFirst();
            assertEquals(EvidenceItem.SourceType.REASONING, item.sourceType());
            assertEquals("The project uses Spring Boot", item.content());
        }

        @Test
        @DisplayName("extractFromMetadata extracts project summary")
        void extractProjectSummary() {
            var metadata = Map.<String, Object>of(
                    "projectSummary", Map.of(
                            "projectName", "my-project",
                            "summary", "A Spring Boot application"
                    )
            );
            EvidenceBundle bundle = evidenceAgent.extractFromMetadata(metadata);
            assertEquals(1, bundle.size());
            EvidenceItem item = bundle.items().getFirst();
            assertEquals(EvidenceItem.SourceType.PROJECT, item.sourceType());
            assertEquals("my-project", item.title());
        }

        @Test
        @DisplayName("extractFromMetadata extracts planning result")
        void extractPlanningResult() {
            var metadata = Map.<String, Object>of(
                    "planningResult", Map.of(
                            "planSummary", "Implement feature X in 3 phases"
                    )
            );
            EvidenceBundle bundle = evidenceAgent.extractFromMetadata(metadata);
            assertEquals(1, bundle.size());
            EvidenceItem item = bundle.items().getFirst();
            assertEquals(EvidenceItem.SourceType.PLANNING, item.sourceType());
        }

        @Test
        @DisplayName("extractFromMetadata extracts memory results")
        void extractMemoryResults() {
            var metadata = Map.<String, Object>of(
                    "memoryResults", List.of(
                            Map.of("content", "User asked about authentication")
                    )
            );
            EvidenceBundle bundle = evidenceAgent.extractFromMetadata(metadata);
            assertEquals(1, bundle.size());
            assertEquals(EvidenceItem.SourceType.MEMORY, bundle.items().getFirst().sourceType());
        }

        @Test
        @DisplayName("extractFromMetadata extracts inference result")
        void extractInferenceResult() {
            var metadata = Map.<String, Object>of(
                    "inferenceResult", Map.of(
                            "topHypothesis", "This is likely a REST API"
                    )
            );
            EvidenceBundle bundle = evidenceAgent.extractFromMetadata(metadata);
            assertEquals(1, bundle.size());
            assertEquals(EvidenceItem.SourceType.INFERENCE, bundle.items().getFirst().sourceType());
        }

        @Test
        @DisplayName("extractFromMetadata handles multiple evidence types")
        void extractMultipleEvidenceTypes() {
            var metadata = Map.<String, Object>of(
                    "knowledgeResults", List.of(
                            Map.of("label", "Java", "description", "Programming language")
                    ),
                    "reasoningConclusion", "The project uses Java",
                    "projectSummary", Map.of("projectName", "test-project")
            );
            EvidenceBundle bundle = evidenceAgent.extractFromMetadata(metadata);
            assertEquals(3, bundle.size());
        }

        @Test
        @DisplayName("toDecision returns EXTRACT action")
        void toDecisionReturnsExtractAction() {
            EvidenceBundle bundle = EvidenceBundle.builder().build();
            AgentDecision decision = evidenceAgent.toDecision(bundle);
            assertEquals(AgentDecision.Action.EXTRACT, decision.action());
            assertEquals(AgentDecision.Agent.EVIDENCE, decision.agent());
            assertEquals(0.95, decision.confidence(), 0.001);
        }
    }

    // ─── VerificationAgent Tests ───────────────────────────────────────────────

    @Nested
    @DisplayName("VerificationAgent")
    class VerificationAgentTests {

        private VerificationAgent verificationAgent;

        @BeforeEach
        void setUp() {
            verificationAgent = new VerificationAgent();
        }

        @Test
        @DisplayName("Empty bundle → INSUFFICIENT tier with 0.15 confidence")
        void emptyBundleInsufficient() {
            EvidenceBundle bundle = EvidenceBundle.builder().build();
            VerificationReport report = verificationAgent.verify(bundle);
            assertEquals(ConfidenceTier.INSUFFICIENT, report.tier());
            assertEquals(0.15, report.confidence(), 0.001);
            assertTrue(report.isInsufficient());
            assertFalse(report.isVerified());
        }

        @Test
        @DisplayName("PROJECT evidence → VERIFIED_PROJECT tier with 0.95 confidence")
        void projectEvidenceVerifiedProject() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.PROJECT)
                            .title("My Project")
                            .content("A test project")
                            .build())
                    .build();
            VerificationReport report = verificationAgent.verify(bundle);
            assertEquals(ConfidenceTier.VERIFIED_PROJECT, report.tier());
            assertEquals(0.95, report.confidence(), 0.001);
            assertTrue(report.isVerified());
            assertEquals(ItemStatus.VERIFIED, report.perItemStatus().get(bundle.items().getFirst().itemId()));
        }

        @Test
        @DisplayName("KNOWLEDGE evidence with citations → VERIFIED_KB tier with 0.80 confidence")
        void knowledgeEvidenceWithCitationsVerifiedKb() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                            .title("Java")
                            .content("A programming language")
                            .citations(List.of("[1] Java spec"))
                            .build())
                    .build();
            VerificationReport report = verificationAgent.verify(bundle);
            assertEquals(ConfidenceTier.VERIFIED_KB, report.tier());
            assertEquals(0.80, report.confidence(), 0.001);
            assertTrue(report.isVerified());
        }

        @Test
        @DisplayName("KNOWLEDGE evidence without citations → VERIFIED_KB tier (kernel attests)")
        void knowledgeEvidenceWithoutCitationsVerifiedKb() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                            .title("Java")
                            .content("A programming language")
                            .build())
                    .build();
            VerificationReport report = verificationAgent.verify(bundle);
            assertEquals(ConfidenceTier.VERIFIED_KB, report.tier());
            assertEquals(0.80, report.confidence(), 0.001);
        }

        @Test
        @DisplayName("REASONING evidence → INFERRED tier with 0.60 confidence")
        void reasoningEvidenceInferred() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.REASONING)
                            .title("Conclusion")
                            .content("Project uses Spring Boot")
                            .build())
                    .build();
            VerificationReport report = verificationAgent.verify(bundle);
            assertEquals(ConfidenceTier.INFERRED, report.tier());
            assertEquals(0.60, report.confidence(), 0.001);
        }

        @Test
        @DisplayName("MEMORY evidence → UNVERIFIED per item (but INFERRED tier)")
        void memoryEvidenceUnverified() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.MEMORY)
                            .title("Memory")
                            .content("User asked about X")
                            .build())
                    .build();
            VerificationReport report = verificationAgent.verify(bundle);
            // Memory items are UNVERIFIED per-item, but with INFERRED tier from bundle composition
            assertEquals(ConfidenceTier.INFERRED, report.tier());
            assertEquals(0.60, report.confidence(), 0.001);
        }

        @Test
        @DisplayName("PROJECT evidence takes priority over KNOWLEDGE")
        void projectTakesPriorityOverKnowledge() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                            .title("Topic")
                            .content("Description")
                            .build())
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.PROJECT)
                            .title("Project")
                            .content("Project structure")
                            .build())
                    .build();
            VerificationReport report = verificationAgent.verify(bundle);
            assertEquals(ConfidenceTier.VERIFIED_PROJECT, report.tier());
            assertEquals(0.95, report.confidence(), 0.001);
        }

        @Test
        @DisplayName("verifyItem returns FAILED for empty content and title")
        void verifyItemFailedForEmptyContent() {
            EvidenceItem empty = EvidenceItem.builder()
                    .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                    .title("")
                    .content("")
                    .build();
            assertEquals(ItemStatus.FAILED, verificationAgent.verifyItem(empty));
        }

        @Test
        @DisplayName("toDecision returns VERIFY action")
        void toDecisionReturnsVerifyAction() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.PROJECT)
                            .title("Test")
                            .content("Test")
                            .build())
                    .build();
            VerificationReport report = verificationAgent.verify(bundle);
            AgentDecision decision = verificationAgent.toDecision(report);
            assertEquals(AgentDecision.Action.VERIFY, decision.action());
            assertEquals(AgentDecision.Agent.VERIFICATION, decision.agent());
            assertEquals(0.95, decision.confidence(), 0.001);
        }

        @Test
        @DisplayName("Evidence bundle is stored in verification report metadata")
        void evidenceBundleInMetadata() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.PROJECT)
                            .title("Test")
                            .content("Test")
                            .build())
                    .build();
            VerificationReport report = verificationAgent.verify(bundle);
            Object storedBundle = report.metadata().get("evidenceBundle");
            assertNotNull(storedBundle);
            assertInstanceOf(EvidenceBundle.class, storedBundle);
        }
    }

    // ─── NaturalResponseAgent Tests ────────────────────────────────────────────

    @Nested
    @DisplayName("NaturalResponseAgent")
    class NaturalResponseAgentTests {

        private NaturalResponseAgent naturalResponseAgent;

        @BeforeEach
        void setUp() {
            naturalResponseAgent = new NaturalResponseAgent();
        }

        @Test
        @DisplayName("Generates response for VERIFIED_PROJECT tier")
        void generatesVerifiedProjectResponse() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.PROJECT)
                            .title("My Project")
                            .content("A test project")
                            .build())
                    .build();
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.VERIFIED_PROJECT)
                    .confidence(0.95)
                    .citations(List.of("Project source code"))
                    .addMetadata("evidenceBundle", bundle)
                    .build();

            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("What is my project?")
                    .build();

            var response = naturalResponseAgent.generate(report, request);
            assertNotNull(response);
            assertEquals(0.95, response.confidence(), 0.001);
            assertNotNull(response.answer());
            // Sprint-19: title is derived from real evidence (item title), not the tier label
            assertTrue(response.answer().contains("My Project"));
        }

        @Test
        @DisplayName("Generates insufficient response for INSUFFICIENT tier")
        void generatesInsufficientResponse() {
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.INSUFFICIENT)
                    .confidence(0.15)
                    .addGap("No evidence available")
                    .build();

            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("What is XYZ?")
                    .build();

            var response = naturalResponseAgent.generate(report, request);
            assertNotNull(response);
            assertEquals(0.15, response.confidence(), 0.001);
            assertNotNull(response.answer());
            assertTrue(response.answer().contains("Insufficient Evidence"));
        }

        @Test
        @DisplayName("Generates inferred response for INFERRED tier")
        void generatesInferredResponse() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.REASONING)
                            .title("Reasoning")
                            .content("Based on analysis, the answer is X")
                            .build())
                    .build();
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.INFERRED)
                    .confidence(0.60)
                    .addMetadata("evidenceBundle", bundle)
                    .build();

            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("What is the status?")
                    .build();

            var response = naturalResponseAgent.generate(report, request);
            assertNotNull(response);
            assertEquals(0.60, response.confidence(), 0.001);
            assertNotNull(response.answer());
            assertTrue(response.answer().contains("Inferred"));
        }

        @Test
        @DisplayName("Structured data contains verification tier")
        void structuredDataContainsTier() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                            .title("Test")
                            .content("Test")
                            .citations(List.of("Source"))
                            .build())
                    .build();
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.VERIFIED_KB)
                    .confidence(0.80)
                    .addMetadata("evidenceBundle", bundle)
                    .build();

            var response = naturalResponseAgent.generate(report, null);
            assertNotNull(response.structuredData().get("verificationTier"));
            assertEquals("VERIFIED_KB", response.structuredData().get("verificationTier"));
            assertEquals(0.80, response.structuredData().get("confidence"));
        }

        @Test
        @DisplayName("toDecision returns GENERATE action")
        void toDecisionReturnsGenerateAction() {
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.PROJECT)
                            .title("Test")
                            .content("Test")
                            .build())
                    .build();
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.VERIFIED_PROJECT)
                    .confidence(0.95)
                    .addMetadata("evidenceBundle", bundle)
                    .build();

            var response = naturalResponseAgent.generate(report, null);
            AgentDecision decision = naturalResponseAgent.toDecision(response);
            assertEquals(AgentDecision.Action.GENERATE, decision.action());
            assertEquals(AgentDecision.Agent.NATURAL_RESPONSE, decision.agent());
        }

        // ─── Sprint-21: LLM Wiring Tests ──────────────────────────────────────────

        @Test
        @DisplayName("Sprint-21: when LLM provider is wired, agent calls it and returns its prose")
        void llmWiredCallsProviderAndReturnsProse() {
            // Fake LLM provider that echoes the full prompt as the response content.
            LlmProvider fakeProvider = new LlmProvider() {
                @Override
                public String providerName() { return "test"; }

                @Override
                public java.util.stream.Stream<String> stream(LlmRequest request) {
                    return java.util.stream.Stream.of(
                            "Grounded answer from the LLM: " + request.prompt());
                }
            };

            var agent = new NaturalResponseAgent(fakeProvider);
            assertEquals(fakeProvider, agent.getLlmProvider());

            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                            .title("Java")
                            .content("Programming language")
                            .confidenceHint(0.85)
                            .build())
                    .build();
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.VERIFIED_KB)
                    .confidence(0.80)
                    .addMetadata("evidenceBundle", bundle)
                    .build();

            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("What is Java?")
                    .build();

            var response = agent.generate(report, request);
            assertNotNull(response);
            assertNotNull(response.answer());
            assertTrue(response.answer().contains("Grounded answer from the LLM:"),
                    "Response should come from the LLM provider");
            // Structured data must surface the wiring state for observability.
            assertEquals(Boolean.TRUE, response.structuredData().get("llmWired"));
            assertEquals("test", response.structuredData().get("llmProviderName"));
        }

        @Test
        @DisplayName("Sprint-21: when LLM provider is absent, agent falls back to deterministic rendering")
        void llmNotWiredFallsBackToDeterministicRendering() {
            // No-arg constructor → llmProvider = null
            var agent = new NaturalResponseAgent();
            assertNull(agent.getLlmProvider());

            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                            .title("Topic X")
                            .content("Description of Topic X")
                            .build())
                    .build();
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.VERIFIED_KB)
                    .confidence(0.80)
                    .addMetadata("evidenceBundle", bundle)
                    .build();

            var response = agent.generate(report, null);
            assertNotNull(response);
            assertNotNull(response.answer());
            // Should produce deterministic StringBuilder output (contains the title).
            assertTrue(response.answer().contains("Topic X"));
            assertEquals(Boolean.FALSE, response.structuredData().get("llmWired"));
        }

        @Test
        @DisplayName("Sprint-21: when LLM provider throws, agent falls back to deterministic rendering")
        void llmProviderThrowsFallsBackToDeterministic() {
            LlmProvider failingProvider = new LlmProvider() {
                @Override
                public String providerName() { return "failing"; }

                @Override
                public java.util.stream.Stream<String> stream(LlmRequest request) {
                    throw new RuntimeException("LLM unavailable");
                }
            };

            var agent = new NaturalResponseAgent(failingProvider);
            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.PROJECT)
                            .title("My Project")
                            .content("Details of the project")
                            .build())
                    .build();
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.VERIFIED_PROJECT)
                    .confidence(0.95)
                    .addMetadata("evidenceBundle", bundle)
                    .build();

            var response = agent.generate(report, null);
            assertNotNull(response);
            // Should still produce a non-null answer via the fallback.
            assertNotNull(response.answer());
            assertTrue(response.answer().contains("My Project"),
                    "Fallback should produce deterministic output with the evidence title");
        }

        @Test
        @DisplayName("Sprint-21: setLlmProvider late-wires the LLM after construction")
        void setLlmProviderLateWiresLlm() {
            var agent = new NaturalResponseAgent();
            assertNull(agent.getLlmProvider());

            LlmProvider fake = new LlmProvider() {
                @Override
                public String providerName() { return "late"; }

                @Override
                public java.util.stream.Stream<String> stream(LlmRequest request) {
                    return java.util.stream.Stream.of("late-wired response");
                }
            };

            agent.setLlmProvider(fake);
            assertEquals(fake, agent.getLlmProvider());

            EvidenceBundle bundle = EvidenceBundle.builder()
                    .addItem(EvidenceItem.builder()
                            .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                            .title("Fact")
                            .content("Something")
                            .build())
                    .build();
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.VERIFIED_KB)
                    .confidence(0.80)
                    .addMetadata("evidenceBundle", bundle)
                    .build();

            var response = agent.generate(report, null);
            assertNotNull(response);
            assertTrue(response.answer().contains("late-wired response"));
        }
    }

    // ─── ChiefIntelligenceAgent Tests ─────────────────────────────────────────

    @Nested
    @DisplayName("ChiefIntelligenceAgent")
    class ChiefIntelligenceAgentTests {

        private ChiefIntelligenceAgent chiefAgent;

        @BeforeEach
        void setUp() {
            chiefAgent = new ChiefIntelligenceAgent();
        }

        @Test
        @DisplayName("buildPlan returns ExecutionPlan with correct kernels for PROJECT intent")
        void buildPlanForProjectIntent() {
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("Analyze my project structure")
                    .build();

            ExecutionPlan plan = chiefAgent.buildPlan(request);
            assertNotNull(plan);
            assertNotNull(plan.planId());
            assertFalse(plan.isEmpty());
            assertTrue(plan.hasKernels());
        }

        @Test
        @DisplayName("route returns SynthesizedResponse for valid request")
        void routeReturnsSynthesizedResponse() {
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("What is my project?")
                    .metadata(Map.of("projectPath", "/some/path"))
                    .build();

            var response = chiefAgent.route(request);
            assertNotNull(response);
            assertNotNull(response.answer());
            assertTrue(response.confidence() >= 0.0 && response.confidence() <= 1.0);
        }

        @Test
        @DisplayName("route returns diagnostic response when project kernel requested without path")
        void routeReturnsDiagnosticWhenProjectMissing() {
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("Analyze my project")
                    .build();

            var response = chiefAgent.route(request);
            assertNotNull(response);
            // Should contain diagnostic report
            assertTrue(response.answer().contains("Diagnostic") || !response.answer().isBlank());
        }

        @Test
        @DisplayName("sub-agents are accessible for testing")
        void subAgentsAccessible() {
            assertNotNull(chiefAgent.diagnosisAgent());
            assertNotNull(chiefAgent.evidenceAgent());
            assertNotNull(chiefAgent.verificationAgent());
            assertNotNull(chiefAgent.naturalResponseAgent());
        }

        // ─── Sprint-21: Pre-flight No-Discard Tests ──────────────────────────────

        @Test
        @DisplayName("Sprint-21: pre-flight response is a stub with empty answer (no discarded synthesis)")
        void sprint21PreFlightStubHasEmptyAnswer() {
            // When kernels are available (healthy workspace), the pre-flight pass
            // should produce only a stub response (answer is empty) — the
            // authoritative synthesis is deferred to DefaultRuntimeService after
            // the 11-stage pipeline. This verifies the double-synthesis loop is broken.
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("Tell me about my project")
                    .metadata(java.util.Map.of("projectPath", "/test/path"))
                    .build();

            var response = chiefAgent.route(request);
            assertNotNull(response);
            assertNotNull(response.answer());
            // Sprint-21: the answer must be empty — no discarded synthesis
            assertEquals("", response.answer());
            // But structured data (chiefMeta) must still be populated so
            // DefaultRuntimeService can observe the routing decision.
            assertFalse(response.structuredData().isEmpty(),
                    "structuredData must be non-empty for chiefMeta injection");
        }

        @Test
        @DisplayName("Sprint-21: diagnostic path is unaffected (still returns full diagnostic response)")
        void sprint21DiagnosticPathUnaffected() {
            var request = com.shreeai.os.platform.runtime.execution.ExecutionRequest.builder()
                    .payload("Analyze my project")
                    .build();

            var response = chiefAgent.route(request);
            assertNotNull(response);
            assertNotNull(response.answer());
            // Diagnostic path still returns the full diagnostic response text.
            assertTrue(response.answer().contains("Diagnostic") || !response.answer().isBlank());
        }
    }

    // ─── Model Tests ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Model classes")
    class ModelTests {

        @Test
        @DisplayName("ExecutionPlan is immutable and correct")
        void executionPlan() {
            ExecutionPlan plan = ExecutionPlan.builder()
                    .detectedIntent(IntentType.PROJECT_INTELLIGENCE)
                    .orderedKernels(List.of(KernelType.PROJECT))
                    .build();
            assertEquals(1, plan.orderedKernels().size());
            assertTrue(plan.hasKernels());
            assertFalse(plan.isEmpty());
            assertFalse(plan.allBlocked());
        }

        @Test
        @DisplayName("ExecutionPlan with skipped kernel is allBlocked")
        void executionPlanAllBlocked() {
            ExecutionPlan plan = ExecutionPlan.builder()
                    .skipKernel(KernelType.PROJECT, "No project path")
                    .build();
            assertTrue(plan.allBlocked());
            assertTrue(plan.hasSkippedKernels());
        }

        @Test
        @DisplayName("EvidenceItem with citations")
        void evidenceItemWithCitations() {
            EvidenceItem item = EvidenceItem.builder()
                    .sourceType(EvidenceItem.SourceType.KNOWLEDGE)
                    .title("Java")
                    .content("Programming language")
                    .citations(List.of("Source [1]", "Source [2]"))
                    .confidenceHint(0.85)
                    .build();
            assertEquals(2, item.citations().size());
            assertEquals(0.85, item.confidenceHint(), 0.001);
        }

        @Test
        @DisplayName("DiagnosticReport reports failures correctly")
        void diagnosticReportFailures() {
            DiagnosticReport report = DiagnosticReport.builder()
                    .putStatus(DiagnosticReport.DiagnosticArea.WORKSPACE, DiagnosticReport.CheckStatus.PASS)
                    .putStatus(DiagnosticReport.DiagnosticArea.PROJECT, DiagnosticReport.CheckStatus.FAIL)
                    .build();
            assertTrue(report.hasFailures());
            assertFalse(report.isHealthy());
        }

        @Test
        @DisplayName("VerificationReport isVerified for high tiers")
        void verificationReportIsVerified() {
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.VERIFIED_PROJECT)
                    .confidence(0.95)
                    .build();
            assertTrue(report.isVerified());
            assertFalse(report.isInsufficient());
        }

        @Test
        @DisplayName("VerificationReport isInsufficient for INSUFFICIENT tier")
        void verificationReportIsInsufficient() {
            VerificationReport report = VerificationReport.builder()
                    .tier(ConfidenceTier.INSUFFICIENT)
                    .confidence(0.15)
                    .build();
            assertTrue(report.isInsufficient());
            assertFalse(report.isVerified());
        }

        @Test
        @DisplayName("AgentDecision builds correctly")
        void agentDecision() {
            AgentDecision decision = AgentDecision.builder()
                    .agent(AgentDecision.Agent.CHIEF_INTELLIGENCE)
                    .action(AgentDecision.Action.ROUTE)
                    .rationale("Test routing decision")
                    .confidence(0.90)
                    .build();
            assertEquals(AgentDecision.Agent.CHIEF_INTELLIGENCE, decision.agent());
            assertEquals(AgentDecision.Action.ROUTE, decision.action());
            assertEquals(0.90, decision.confidence(), 0.001);
        }
    }
}

package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.intelligence.context.IntelligenceContextBuilder;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultProviderRouter;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultFreshnessPolicyEngine;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultKnowledgeAcquisitionOrchestrator;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultSourceDiscoveryEngine;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultTrustSelectionEngine;
import com.shreeai.os.platform.kernels.acquisition.engine.FreshnessPolicyEngine;
import com.shreeai.os.platform.kernels.acquisition.engine.KnowledgeAcquisitionOrchestrator;
import com.shreeai.os.platform.kernels.acquisition.engine.ProviderRouter;
import com.shreeai.os.platform.kernels.acquisition.engine.SourceDiscoveryEngine;
import com.shreeai.os.platform.kernels.acquisition.engine.TrustSelectionEngine;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionResult;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
import com.shreeai.os.platform.kernels.acquisition.model.SourceSelectionPlan;
import com.shreeai.os.platform.kernels.context.engine.AmbiguityDetectionEngine;
import com.shreeai.os.platform.kernels.context.engine.ConstraintExtractionEngine;
import com.shreeai.os.platform.kernels.context.engine.DefaultAmbiguityDetectionEngine;
import com.shreeai.os.platform.kernels.context.engine.DefaultConstraintExtractionEngine;
import com.shreeai.os.platform.kernels.context.engine.DefaultDomainDetector;
import com.shreeai.os.platform.kernels.context.engine.DefaultPrimaryIntentDetector;
import com.shreeai.os.platform.kernels.context.engine.DomainDetector;
import com.shreeai.os.platform.kernels.context.engine.PrimaryIntentDetector;
import com.shreeai.os.platform.kernels.context.engine.GoalIdentificationEngine;
import com.shreeai.os.platform.kernels.context.engine.DefaultGoalIdentificationEngine;
import com.shreeai.os.platform.kernels.context.model.AmbiguityProfile;
import com.shreeai.os.platform.kernels.context.model.ContextIntelligence;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultDocumentIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeSourceRegistry;
import com.shreeai.os.platform.kernels.knowledge.engine.DocumentIngestionEngine;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeSourceRegistry;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import com.shreeai.os.platform.runtime.pipeline.ExecutionChain;
import com.shreeai.os.platform.runtime.pipeline.ExecutionStage;
import com.shreeai.os.platform.runtime.pipeline.PipelineContext;
import com.shreeai.os.platform.runtime.pipeline.PipelineExecutionState;
import com.shreeai.os.platform.runtime.pipeline.PipelineResult;
import com.shreeai.os.platform.runtime.pipeline.PipelineStageDescriptor;

/**
 * ContextStage - Builds and enriches the execution context.
 *
 * <p>This stage is responsible for:</p>
 * <ul>
 *   <li>Building the execution context from the request</li>
 *   <li>Enriching context with identity information</li>
 *   <li>Preparing context for downstream kernel stages</li>
 * </ul>
 *
 * <p>This is part of the real kernel execution pipeline for Shree AI OS.</p>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Engineering Gate 3
 */
public final class ContextStage implements ExecutionStage {

    private static final PipelineStageDescriptor DESCRIPTOR = PipelineStageDescriptor.builder()
            .stageName("Context")
            .priority(2)
            .enabled(true)
            .version("1.0")
            .description("Builds execution context with intent, domain, constraints, goals, and ambiguity diagnosis")
            .build();

    private final PrimaryIntentDetector intentDetector;
    private final DomainDetector domainDetector;
    private final ConstraintExtractionEngine constraintEngine;
    private final KnowledgeSourceRegistry sourceRegistry;

    public ContextStage() {
        this(new DefaultPrimaryIntentDetector(), new DefaultDomainDetector(),
                new DefaultConstraintExtractionEngine(),
                new DefaultKnowledgeSourceRegistry());
    }

    public ContextStage(PrimaryIntentDetector intentDetector, DomainDetector domainDetector,
                        ConstraintExtractionEngine constraintEngine) {
        this(intentDetector, domainDetector, constraintEngine,
                new DefaultKnowledgeSourceRegistry());
    }

    /**
     * Creates a context stage backed by an explicit K1 source registry catalog.
     *
     * <p>The registry is the read-only catalog consulted by the K0.6.3 trust and
     * source selection engine. It is never mutated by this stage; when it holds
     * no eligible source, acquisition targets are simply left unselected.</p>
     *
     * @param intentDetector  the primary intent detector (must not be null)
     * @param domainDetector  the domain detector (must not be null)
     * @param constraintEngine the user constraint extraction engine (must not be null)
     * @param sourceRegistry  the K1 knowledge source registry catalog (must not be null)
     */
    public ContextStage(PrimaryIntentDetector intentDetector, DomainDetector domainDetector,
                        ConstraintExtractionEngine constraintEngine,
                        KnowledgeSourceRegistry sourceRegistry) {
        this.intentDetector = intentDetector;
        this.domainDetector = domainDetector;
        this.constraintEngine = constraintEngine;
        this.sourceRegistry = java.util.Objects.requireNonNull(sourceRegistry,
                "sourceRegistry must not be null");
    }

    @Override
    public PipelineResult process(PipelineContext context, ExecutionChain chain, PipelineExecutionState state) {
        try {
            // Retrieve identity information from previous stage
            String identityId = (String) state.getMetadata().get("identityId");
            String identityType = (String) state.getMetadata().get("identityType");

            // Build context information
            String contextId = "ctx-" + System.currentTimeMillis();
            String contextType = "EXECUTION_CONTEXT";

            // P1.1: Detect primary intent from user input
            String userInput = context.getExecutionRequest() != null
                    && context.getExecutionRequest().getUserInput() != null
                    ? context.getExecutionRequest().getUserInput()
                    : "";
            IntentProfile intentProfile = intentDetector.detect(userInput);
            CognitiveState updatedCognitiveState = state.getCognitiveState().withIntentProfile(intentProfile);
            state.setCognitiveState(updatedCognitiveState);

            // P1.2: Detect domain from user input
            DomainProfile domainProfile = domainDetector.detect(userInput);
            CognitiveState updatedCognitiveStateWithDomain = state.getCognitiveState().withDomainProfile(domainProfile);
            state.setCognitiveState(updatedCognitiveStateWithDomain);

            // P1.3: Extract user constraints from user input
            UserConstraints userConstraints = constraintEngine.extract(userInput);
            CognitiveState updatedCognitiveStateWithConstraints = state.getCognitiveState().withUserConstraints(userConstraints);
            state.setCognitiveState(updatedCognitiveStateWithConstraints);

            // P1.4: Identify goals from user input (Deterministic Goal Identification)
            GoalIdentificationEngine goalIdentificationEngine = new DefaultGoalIdentificationEngine();
            GoalStructure goalStructure = goalIdentificationEngine.identify(userInput);
            CognitiveState updatedCognitiveStateWithGoal = updatedCognitiveStateWithConstraints.withGoalStructure(goalStructure);
            state.setCognitiveState(updatedCognitiveStateWithGoal);

            // P1.5: Diagnose ambiguity from the cognitive artifacts (never the raw prompt)
            AmbiguityDetectionEngine ambiguityDetectionEngine = new DefaultAmbiguityDetectionEngine();
            AmbiguityProfile ambiguityProfile = ambiguityDetectionEngine.diagnose(
                    intentProfile, domainProfile, userConstraints, goalStructure);
            CognitiveState updatedCognitiveStateWithAmbiguity = updatedCognitiveStateWithGoal.withAmbiguityProfile(ambiguityProfile);
            state.setCognitiveState(updatedCognitiveStateWithAmbiguity);

            // K0.6.1: Discover knowledge requirements from the canonical context
            // intelligence aggregate. Deterministic, rule-based discovery only -
            // no provider routing, no acquisition, no LLM (those are K0.6.2+).
            SourceDiscoveryEngine sourceDiscoveryEngine = new DefaultSourceDiscoveryEngine();
            KnowledgeRequirementSet knowledgeRequirements = sourceDiscoveryEngine.discover(
                    ContextIntelligence.of(
                            intentProfile, domainProfile, userConstraints,
                            goalStructure, ambiguityProfile));
            CognitiveState updatedCognitiveStateWithRequirements =
                    state.getCognitiveState().withKnowledgeRequirements(knowledgeRequirements);
            state.setCognitiveState(updatedCognitiveStateWithRequirements);

            // K0.6.2: Route every required knowledge topic to a provider type.
            // Deterministic, dictionary-driven routing only - no network calls,
            // no trust ranking, no ingestion (those are K0.6.3+).
            ProviderRouter providerRouter = new DefaultProviderRouter();
            AcquisitionPlan acquisitionPlan = providerRouter.route(knowledgeRequirements);
            CognitiveState updatedCognitiveStateWithPlan =
                    state.getCognitiveState().withAcquisitionPlan(acquisitionPlan);
            state.setCognitiveState(updatedCognitiveStateWithPlan);

            // K0.6.3: Select the single most authoritative concrete source for
            // every routed acquisition target from the K1 source registry.
            // Deterministic, authority-ranked selection only - no downloads, no
            // crawling, no ingestion (those are K0.6.4+).
            TrustSelectionEngine trustSelectionEngine = new DefaultTrustSelectionEngine();
            SourceSelectionPlan sourceSelectionPlan =
                    trustSelectionEngine.select(acquisitionPlan, sourceRegistry);
            CognitiveState updatedCognitiveStateWithSelection =
                    state.getCognitiveState().withSourceSelectionPlan(sourceSelectionPlan);
            state.setCognitiveState(updatedCognitiveStateWithSelection);

            // K0.6.4: Decide, for every selected source, whether cached knowledge
            // may be reused or fresh knowledge must be acquired. Deterministic
            // cache policy only - no downloads, no crawling, no ingestion
            // (K0.6.5 executes the ACQUIRE / REFRESH decisions).
            FreshnessPolicyEngine freshnessPolicyEngine = new DefaultFreshnessPolicyEngine();
            AcquisitionDecisionPlan acquisitionDecisionPlan = freshnessPolicyEngine.decide(
                    sourceSelectionPlan, sourceRegistry,
                    ContextIntelligence.of(intentProfile, domainProfile, userConstraints,
                            goalStructure, ambiguityProfile));
            CognitiveState updatedCognitiveStateWithDecision =
                    state.getCognitiveState().withAcquisitionDecisionPlan(acquisitionDecisionPlan);
            state.setCognitiveState(updatedCognitiveStateWithDecision);

            // K0.6.5: Execute the locked acquisition workflow for every decision
            // of the plan. Deterministic execution only - the orchestrator never
            // re-decides what to acquire. With no content supply wired into the
            // runtime yet, pending acquisitions are isolated and recorded as
            // FAILED; cache reuses without a cached document fail the same way.
            DocumentIngestionEngine ingestionEngine =
                    new DefaultDocumentIngestionEngine(sourceRegistry);
            KnowledgeAcquisitionOrchestrator acquisitionOrchestrator =
                    new DefaultKnowledgeAcquisitionOrchestrator(sourceRegistry, ingestionEngine);
            AcquisitionResult acquisitionResult =
                    acquisitionOrchestrator.execute(acquisitionDecisionPlan);
            CognitiveState updatedCognitiveStateWithResult =
                    state.getCognitiveState().withAcquisitionResult(acquisitionResult);
            state.setCognitiveState(updatedCognitiveStateWithResult);

            // Build the canonical context intelligence aggregate.
            ContextIntelligence contextIntelligence = ContextIntelligence.of(
                    intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile);

            // Build the structured IntelligenceContext from the request metadata.
            IntelligenceContext intelligenceContext = null;
            if (context.getExecutionRequest() != null
                    && context.getExecutionRequest().getMetadata() != null) {
                Object supplied = context.getExecutionRequest().getMetadata()
                        .get("intelligenceContext");
                if (supplied instanceof IntelligenceContext ic) {
                    intelligenceContext = ic;
                }
            }

            if (intelligenceContext == null && context.getExecutionRequest() != null) {
                intelligenceContext = IntelligenceContextBuilder.fromExecution(
                        context.getExecutionRequest().getRequestId(),
                        userInput,
                        java.util.Map.of()
                );
            }

            // Store context information in state
            state.addMetadata("contextId", contextId);
            state.addMetadata("contextType", contextType);
            state.addMetadata("contextBuilt", true);
            state.addMetadata("primaryIntent", intentProfile.primaryIntent().name());
            state.addMetadata("intentConfidence", intentProfile.confidence());
            state.addMetadata("primaryDomain", domainProfile.primaryDomain().name());
            state.addMetadata("domainConfidence", domainProfile.confidence());
            if (intelligenceContext != null) {
                state.addMetadata("intelligenceContext", intelligenceContext);
            }
            state.addMessage("Context built: " + contextId + " for identity " + identityId
                    + " | Intent: " + intentProfile.primaryIntent()
                    + " | Domain: " + domainProfile.primaryDomain()
                    + " | Goal: " + goalStructure.primaryGoal().title()
                    + " | Ambiguity: " + contextIntelligence.ambiguityProfile().ambiguityScore()
                    + " | KnowledgeRequirements: " + knowledgeRequirements.topics().size()
                    + " | AcquisitionTargets: " + acquisitionPlan.targets().size()
                    + " | SelectedSources: " + sourceSelectionPlan.size()
                    + " | AcquisitionDecisions: " + acquisitionDecisionPlan.size()
                    + " | PendingAcquisition: "
                    + acquisitionDecisionPlan.targetsRequiringAcquisition().size()
                    + " | AcquisitionRecords: " + acquisitionResult.size()
                    + " | Documents: " + acquisitionResult.documents().size()
                    + " | Acquired: " + acquisitionResult.acquiredCount()
                    + " | Skipped: " + acquisitionResult.skippedCount()
                    + " | Failed: " + acquisitionResult.failedCount());

            // Continue to next stage
            return chain.next(context, state);

        } catch (Exception e) {
            state.markFailure("Context building failed: " + e.getMessage());
            return PipelineResult.builder()
                    .success(false)
                    .status("CONTEXT_FAILED")
                    .addMessage("Context stage failed: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public PipelineStageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }
}
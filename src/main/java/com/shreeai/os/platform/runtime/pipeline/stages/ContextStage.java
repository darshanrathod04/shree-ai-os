package com.shreeai.os.platform.runtime.pipeline.stages;

import com.shreeai.os.platform.intelligence.context.IntelligenceContext;
import com.shreeai.os.platform.intelligence.context.IntelligenceContextBuilder;
import com.shreeai.os.platform.kernels.acquisition.engine.DefaultSourceDiscoveryEngine;
import com.shreeai.os.platform.kernels.acquisition.engine.SourceDiscoveryEngine;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
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

    public ContextStage() {
        this.intentDetector = new DefaultPrimaryIntentDetector();
        this.domainDetector = new DefaultDomainDetector();
        this.constraintEngine = new DefaultConstraintExtractionEngine();
    }

    public ContextStage(PrimaryIntentDetector intentDetector, DomainDetector domainDetector,
                        ConstraintExtractionEngine constraintEngine) {
        this.intentDetector = intentDetector;
        this.domainDetector = domainDetector;
        this.constraintEngine = constraintEngine;
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
                    + " | KnowledgeRequirements: " + knowledgeRequirements.topics().size());

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
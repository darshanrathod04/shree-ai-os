package com.shreeai.os.platform.runtime.cognitive;

import com.shreeai.os.platform.kernels.planning.model.ReplanningResult;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionResult;
import com.shreeai.os.platform.kernels.inference.model.AlternativeSet;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
import com.shreeai.os.platform.kernels.acquisition.model.SourceSelectionPlan;
import com.shreeai.os.platform.kernels.cognitive.engine.ReflectionAnalysis;
import com.shreeai.os.platform.kernels.cognitive.model.ReasoningResult;
import com.shreeai.os.platform.kernels.context.model.AmbiguityProfile;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.inference.model.EvidencePackage;
import com.shreeai.os.platform.kernels.inference.model.InferenceResult;
import com.shreeai.os.platform.kernels.inference.model.CalibratedDecision;
import com.shreeai.os.platform.kernels.inference.model.ExplainableDecision;
import com.shreeai.os.platform.kernels.inference.model.OptimizedDecision;
import com.shreeai.os.platform.kernels.planning.model.TaskGraph;
import com.shreeai.os.platform.kernels.planning.model.ExecutionPlan;
import com.shreeai.os.platform.kernels.planning.model.ExecutablePlanningGraph;
import com.shreeai.os.platform.kernels.response.contracts.PlanningResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;


/**
 * <b>CognitiveState</b>
 *
 * <p>Immutable value object holding the cognitive artifacts produced by the
 * pipeline's cognitive segment (Reasoning → Inference → Planning → Reflection)
 * together with the reflection loop bookkeeping (iteration count and quality
 * history).</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Single source of truth for cognitive artifacts during a pipeline run
 *       (replaces scattered {@code state.addMetadata(...)} cognitive keys).</li>
 *   <li>Every mutation returns a <em>new</em> instance - the previous state is
 *       never modified, so readers always observe a consistent snapshot.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable - no method mutates this object.</li>
 *   <li>Thread-safe - all fields are deeply immutable.</li>
 *   <li>No duplicated state - reasoning/inference/planning/reflection live
 *       here and nowhere else.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime Kernel - Cognitive Segment</p>
 *
  * @param reasoning           the reasoning result (null before ReasoningStage)
 * @param inference           the inference result (null before InferenceStage)
 * @param planning            the planning response (null before PlanningStage)
 * @param reflection          the latest reflection analysis (null before
 *                            ReflectionStage)
 * @param reflectionIteration the number of completed reflection passes
 * @param qualityHistory      quality scores recorded per reflection pass
 * @param evidencePackage     the resolved evidence package (null before InferenceStage)
 * @param intentProfile       the detected primary intent (null before ContextStage)
 * @param domainProfile       the detected domain profile (null before ContextStage)
 * @param userConstraints      the extracted user constraints (null before ContextStage)
 * @param goalStructure        the identified goal structure (null before ContextStage)
 * @param ambiguityProfile     the ambiguity diagnosis (null before ContextStage)
 * @param reasoningGraph       the multi-hop reasoning graph (null before
 *                            ReasoningStage R1)
 * @param synthesisGraph       the evidence synthesis graph (null before
 *                            ReasoningStage R2)
 * @param causalGraph          the causal reasoning graph (null before
 *                            ReasoningStage R3)
 * @param verificationGraph    the self verification graph (null before
 *                            ReasoningStage R4)
 * @param uncertaintyGraph     the uncertainty modeling graph (null before
 *                            ReasoningStage R5)
 * @param knowledgeRequirements the discovered knowledge requirement set (null
 *                            before ContextStage K0.6.1)
 * @param acquisitionPlan      the routed acquisition plan (null before
 *                            ContextStage K0.6.2)
 * @param sourceSelectionPlan  the trusted source selection plan (null before
 *                            ContextStage K0.6.3)
 * @param acquisitionDecisionPlan the cache decision plan (null before
 *                            ContextStage K0.6.4)
 * @param acquisitionResult    the executed acquisition result (null before
 *                            ContextStage K0.6.5)
 * @param alternativeSet       the generated alternative set (null before
 *                            ReasoningStage I1)
 * @param optimizedDecision    the optimized decision (null before
 *                            ReasoningStage I3)
 * @param calibratedDecision   the calibrated decision (null before
 *                            ReasoningStage I4)
 * @param explainableDecision  the explainable decision (null before
 *                            ReasoningStage I5)
 */
public record CognitiveState(
        ReasoningResult reasoning,
        InferenceResult inference,
        PlanningResponse planning,
        ReflectionAnalysis reflection,
        int reflectionIteration,
        List<Double> qualityHistory,
        EvidencePackage evidencePackage,
        IntentProfile intentProfile,
        DomainProfile domainProfile,
        UserConstraints userConstraints,
        GoalStructure goalStructure,
        AmbiguityProfile ambiguityProfile,
        ReasoningGraph reasoningGraph,
        SynthesisGraph synthesisGraph,
        CausalGraph causalGraph,
        VerificationGraph verificationGraph,
        UncertaintyGraph uncertaintyGraph,
        KnowledgeRequirementSet knowledgeRequirements,
        AcquisitionPlan acquisitionPlan,
        SourceSelectionPlan sourceSelectionPlan,
        AcquisitionDecisionPlan acquisitionDecisionPlan,
        AcquisitionResult acquisitionResult,
        AlternativeSet alternativeSet,
                OptimizedDecision optimizedDecision,
        CalibratedDecision calibratedDecision,
        ExplainableDecision explainableDecision,
        TaskGraph taskGraph,
        ExecutionPlan executionPlan,
        ReplanningResult replanningResult,
        ExecutablePlanningGraph executablePlanningGraph) {

    /** Backward-compatible P2.2 constructor; no schedule exists yet. */
    public CognitiveState(
        ReasoningResult reasoning,
        InferenceResult inference,
        PlanningResponse planning,
        ReflectionAnalysis reflection,
        int reflectionIteration,
        List<Double> qualityHistory,
        EvidencePackage evidencePackage,
        IntentProfile intentProfile,
        DomainProfile domainProfile,
        UserConstraints userConstraints,
        GoalStructure goalStructure,
        AmbiguityProfile ambiguityProfile,
        ReasoningGraph reasoningGraph,
        SynthesisGraph synthesisGraph,
        CausalGraph causalGraph,
        VerificationGraph verificationGraph,
        UncertaintyGraph uncertaintyGraph,
        KnowledgeRequirementSet knowledgeRequirements,
        AcquisitionPlan acquisitionPlan,
        SourceSelectionPlan sourceSelectionPlan,
        AcquisitionDecisionPlan acquisitionDecisionPlan,
        AcquisitionResult acquisitionResult,
        AlternativeSet alternativeSet,
                OptimizedDecision optimizedDecision,
        CalibratedDecision calibratedDecision,
        ExplainableDecision explainableDecision,
        TaskGraph taskGraph) {
        this(reasoning, inference, planning, reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile, reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph, knowledgeRequirements, acquisitionPlan, sourceSelectionPlan, acquisitionDecisionPlan, acquisitionResult, alternativeSet, optimizedDecision, calibratedDecision, explainableDecision, taskGraph, null, null, null);
    }

    /** Creates a deeply-immutable CognitiveState with defensive copies. */
    public CognitiveState {
        qualityHistory = qualityHistory == null
                ? List.of()
                : List.copyOf(qualityHistory);
    }

    /**
     * Backward-compatible constructor accepting the legacy R4 shape (sixteen
     * artifacts without an uncertainty graph). The uncertainty graph is
     * initialized to {@code null}.
     *
     * @param reasoning           the reasoning result (null before ReasoningStage)
     * @param inference           the inference result (null before InferenceStage)
     * @param planning            the planning response (null before PlanningStage)
     * @param reflection          the latest reflection analysis (null before
     *                            ReflectionStage)
     * @param reflectionIteration the number of completed reflection passes
     * @param qualityHistory      quality scores recorded per reflection pass
     * @param evidencePackage     the resolved evidence package (null before InferenceStage)
     * @param intentProfile       the detected primary intent (null before ContextStage)
     * @param domainProfile       the detected domain profile (null before ContextStage)
     * @param userConstraints     the extracted user constraints (null before ContextStage)
     * @param goalStructure       the identified goal structure (null before ContextStage)
     * @param ambiguityProfile    the ambiguity diagnosis (null before ContextStage)
     * @param reasoningGraph      the multi-hop reasoning graph (null before
     *                            ReasoningStage R1)
     * @param synthesisGraph      the evidence synthesis graph (null before
     *                            ReasoningStage R2)
     * @param causalGraph         the causal reasoning graph (null before
     *                            ReasoningStage R3)
     * @param verificationGraph   the self verification graph (null before
     *                            ReasoningStage R4)
     */
    public CognitiveState(
            ReasoningResult reasoning,
            InferenceResult inference,
            PlanningResponse planning,
            ReflectionAnalysis reflection,
            int reflectionIteration,
            List<Double> qualityHistory,
            EvidencePackage evidencePackage,
            IntentProfile intentProfile,
            DomainProfile domainProfile,
            UserConstraints userConstraints,
            GoalStructure goalStructure,
            AmbiguityProfile ambiguityProfile,
            ReasoningGraph reasoningGraph,
            SynthesisGraph synthesisGraph,
            CausalGraph causalGraph,
            VerificationGraph verificationGraph) {
        this(reasoning, inference, planning, reflection, reflectionIteration,
                qualityHistory, evidencePackage, intentProfile, domainProfile,
                userConstraints, goalStructure, ambiguityProfile,
                                reasoningGraph, synthesisGraph, causalGraph, verificationGraph,
                null, null, null, null, null);
    }

    /**
     * Backward-compatible constructor accepting the R5 shape (seventeen
     * artifacts without knowledge requirements). The knowledge requirement set
     * is initialized to {@code null}.
     *
     * @param reasoning           the reasoning result (null before ReasoningStage)
     * @param inference           the inference result (null before InferenceStage)
     * @param planning            the planning response (null before PlanningStage)
     * @param reflection          the latest reflection analysis (null before
     *                            ReflectionStage)
     * @param reflectionIteration the number of completed reflection passes
     * @param qualityHistory      quality scores recorded per reflection pass
     * @param evidencePackage     the resolved evidence package (null before InferenceStage)
     * @param intentProfile       the detected primary intent (null before ContextStage)
     * @param domainProfile       the detected domain profile (null before ContextStage)
     * @param userConstraints     the extracted user constraints (null before ContextStage)
     * @param goalStructure       the identified goal structure (null before ContextStage)
     * @param ambiguityProfile    the ambiguity diagnosis (null before ContextStage)
     * @param reasoningGraph      the multi-hop reasoning graph (null before
     *                            ReasoningStage R1)
     * @param synthesisGraph      the evidence synthesis graph (null before
     *                            ReasoningStage R2)
     * @param causalGraph         the causal reasoning graph (null before
     *                            ReasoningStage R3)
     * @param verificationGraph   the self verification graph (null before
     *                            ReasoningStage R4)
     * @param uncertaintyGraph    the uncertainty modeling graph (null before
     *                            ReasoningStage R5)
     */
    public CognitiveState(
            ReasoningResult reasoning,
            InferenceResult inference,
            PlanningResponse planning,
            ReflectionAnalysis reflection,
            int reflectionIteration,
            List<Double> qualityHistory,
            EvidencePackage evidencePackage,
            IntentProfile intentProfile,
            DomainProfile domainProfile,
            UserConstraints userConstraints,
            GoalStructure goalStructure,
            AmbiguityProfile ambiguityProfile,
            ReasoningGraph reasoningGraph,
            SynthesisGraph synthesisGraph,
            CausalGraph causalGraph,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph) {
        this(reasoning, inference, planning, reflection, reflectionIteration,
                qualityHistory, evidencePackage, intentProfile, domainProfile,
                userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph,
                                uncertaintyGraph, null, null, null, null);
    }

    /**
     * Backward-compatible constructor accepting the K0.6.1 shape (eighteen
     * artifacts without an acquisition plan). The acquisition plan is
     * initialized to {@code null}.
     *
     * @param reasoning             the reasoning result (null before ReasoningStage)
     * @param inference             the inference result (null before InferenceStage)
     * @param planning              the planning response (null before PlanningStage)
     * @param reflection            the latest reflection analysis (null before
     *                              ReflectionStage)
     * @param reflectionIteration   the number of completed reflection passes
     * @param qualityHistory        quality scores recorded per reflection pass
     * @param evidencePackage       the resolved evidence package (null before InferenceStage)
     * @param intentProfile         the detected primary intent (null before ContextStage)
     * @param domainProfile         the detected domain profile (null before ContextStage)
     * @param userConstraints       the extracted user constraints (null before ContextStage)
     * @param goalStructure         the identified goal structure (null before ContextStage)
     * @param ambiguityProfile      the ambiguity diagnosis (null before ContextStage)
     * @param reasoningGraph        the multi-hop reasoning graph (null before
     *                              ReasoningStage R1)
     * @param synthesisGraph        the evidence synthesis graph (null before
     *                              ReasoningStage R2)
     * @param causalGraph           the causal reasoning graph (null before
     *                              ReasoningStage R3)
     * @param verificationGraph     the self verification graph (null before
     *                              ReasoningStage R4)
     * @param uncertaintyGraph      the uncertainty modeling graph (null before
     *                              ReasoningStage R5)
     * @param knowledgeRequirements the discovered knowledge requirement set (null
     *                              before ContextStage K0.6.1)
     */
    public CognitiveState(
            ReasoningResult reasoning,
            InferenceResult inference,
            PlanningResponse planning,
            ReflectionAnalysis reflection,
            int reflectionIteration,
            List<Double> qualityHistory,
            EvidencePackage evidencePackage,
            IntentProfile intentProfile,
            DomainProfile domainProfile,
            UserConstraints userConstraints,
            GoalStructure goalStructure,
            AmbiguityProfile ambiguityProfile,
            ReasoningGraph reasoningGraph,
            SynthesisGraph synthesisGraph,
            CausalGraph causalGraph,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph,
            KnowledgeRequirementSet knowledgeRequirements) {
        this(reasoning, inference, planning, reflection, reflectionIteration,
                qualityHistory, evidencePackage, intentProfile, domainProfile,
                userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph,
                                uncertaintyGraph, knowledgeRequirements, null, null, null);
    }

    /**
     * Backward-compatible constructor accepting the K0.6.2 shape (nineteen
     * artifacts without a source selection plan). The source selection plan is
     * initialized to {@code null}.
     *
     * @param reasoning             the reasoning result (null before ReasoningStage)
     * @param inference             the inference result (null before InferenceStage)
     * @param planning              the planning response (null before PlanningStage)
     * @param reflection            the latest reflection analysis (null before
     *                              ReflectionStage)
     * @param reflectionIteration   the number of completed reflection passes
     * @param qualityHistory        quality scores recorded per reflection pass
     * @param evidencePackage       the resolved evidence package (null before InferenceStage)
     * @param intentProfile         the detected primary intent (null before ContextStage)
     * @param domainProfile         the detected domain profile (null before ContextStage)
     * @param userConstraints       the extracted user constraints (null before ContextStage)
     * @param goalStructure         the identified goal structure (null before ContextStage)
     * @param ambiguityProfile      the ambiguity diagnosis (null before ContextStage)
     * @param reasoningGraph        the multi-hop reasoning graph (null before
     *                              ReasoningStage R1)
     * @param synthesisGraph        the evidence synthesis graph (null before
     *                              ReasoningStage R2)
     * @param causalGraph           the causal reasoning graph (null before
     *                              ReasoningStage R3)
     * @param verificationGraph     the self verification graph (null before
     *                              ReasoningStage R4)
     * @param uncertaintyGraph      the uncertainty modeling graph (null before
     *                              ReasoningStage R5)
     * @param knowledgeRequirements the discovered knowledge requirement set (null
     *                              before ContextStage K0.6.1)
     * @param acquisitionPlan       the routed acquisition plan (null before
     *                              ContextStage K0.6.2)
     */
    public CognitiveState(
            ReasoningResult reasoning,
            InferenceResult inference,
            PlanningResponse planning,
            ReflectionAnalysis reflection,
            int reflectionIteration,
            List<Double> qualityHistory,
            EvidencePackage evidencePackage,
            IntentProfile intentProfile,
            DomainProfile domainProfile,
            UserConstraints userConstraints,
            GoalStructure goalStructure,
            AmbiguityProfile ambiguityProfile,
            ReasoningGraph reasoningGraph,
            SynthesisGraph synthesisGraph,
            CausalGraph causalGraph,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph,
            KnowledgeRequirementSet knowledgeRequirements,
            AcquisitionPlan acquisitionPlan) {
        this(reasoning, inference, planning, reflection, reflectionIteration,
                qualityHistory, evidencePackage, intentProfile, domainProfile,
                userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph,
                                uncertaintyGraph, knowledgeRequirements, acquisitionPlan, null, null, null);
    }

    /**
     * Backward-compatible constructor accepting the K0.6.3 shape (twenty
     * artifacts without an acquisition decision plan). The decision plan is
     * initialized to {@code null}.
     *
     * @param reasoning             the reasoning result (null before ReasoningStage)
     * @param inference             the inference result (null before InferenceStage)
     * @param planning              the planning response (null before PlanningStage)
     * @param reflection            the latest reflection analysis (null before ReflectionStage)
     * @param reflectionIteration   the number of completed reflection passes
     * @param qualityHistory        quality scores recorded per reflection pass
     * @param evidencePackage       the resolved evidence package (null before InferenceStage)
     * @param intentProfile         the detected primary intent (null before ContextStage)
     * @param domainProfile         the detected domain profile (null before ContextStage)
     * @param userConstraints       the extracted user constraints (null before ContextStage)
     * @param goalStructure         the identified goal structure (null before ContextStage)
     * @param ambiguityProfile      the ambiguity diagnosis (null before ContextStage)
     * @param reasoningGraph        the multi-hop reasoning graph (null before R1)
     * @param synthesisGraph        the evidence synthesis graph (null before R2)
     * @param causalGraph           the causal reasoning graph (null before R3)
     * @param verificationGraph     the self verification graph (null before R4)
     * @param uncertaintyGraph      the uncertainty modeling graph (null before R5)
     * @param knowledgeRequirements the discovered knowledge requirement set (null before K0.6.1)
     * @param acquisitionPlan       the routed acquisition plan (null before K0.6.2)
     * @param sourceSelectionPlan   the trusted source selection plan (null before K0.6.3)
     */
    public CognitiveState(
            ReasoningResult reasoning,
            InferenceResult inference,
            PlanningResponse planning,
            ReflectionAnalysis reflection,
            int reflectionIteration,
            List<Double> qualityHistory,
            EvidencePackage evidencePackage,
            IntentProfile intentProfile,
            DomainProfile domainProfile,
            UserConstraints userConstraints,
            GoalStructure goalStructure,
            AmbiguityProfile ambiguityProfile,
            ReasoningGraph reasoningGraph,
            SynthesisGraph synthesisGraph,
            CausalGraph causalGraph,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph,
            KnowledgeRequirementSet knowledgeRequirements,
            AcquisitionPlan acquisitionPlan,
            SourceSelectionPlan sourceSelectionPlan) {
        this(reasoning, inference, planning, reflection, reflectionIteration,
                qualityHistory, evidencePackage, intentProfile, domainProfile,
                userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph,
                uncertaintyGraph, knowledgeRequirements, acquisitionPlan,
                                sourceSelectionPlan, null, null, null, null, null, null, null);
    }
    /**
     * Backward-compatible constructor accepting the K0.6.4 shape (twenty-one
     * artifacts without an acquisition result). The acquisition result is
     * initialized to {@code null}.
     *
     * @param reasoning              the reasoning result (null before ReasoningStage)
     * @param inference              the inference result (null before InferenceStage)
     * @param planning               the planning response (null before PlanningStage)
     * @param reflection             the latest reflection analysis (null before
     *                               ReflectionStage)
     * @param reflectionIteration    the number of completed reflection passes
     * @param qualityHistory         quality scores recorded per reflection pass
     * @param evidencePackage        the resolved evidence package (null before InferenceStage)
     * @param intentProfile          the detected primary intent (null before ContextStage)
     * @param domainProfile          the detected domain profile (null before ContextStage)
     * @param userConstraints        the extracted user constraints (null before ContextStage)
     * @param goalStructure          the identified goal structure (null before ContextStage)
     * @param ambiguityProfile       the ambiguity diagnosis (null before ContextStage)
     * @param reasoningGraph         the multi-hop reasoning graph (null before
     *                               ReasoningStage R1)
     * @param synthesisGraph         the evidence synthesis graph (null before
     *                               ReasoningStage R2)
     * @param causalGraph            the causal reasoning graph (null before
     *                               ReasoningStage R3)
     * @param verificationGraph      the self verification graph (null before
     *                               ReasoningStage R4)
     * @param uncertaintyGraph       the uncertainty modeling graph (null before
     *                               ReasoningStage R5)
     * @param knowledgeRequirements  the discovered knowledge requirement set (null
     *                               before ContextStage K0.6.1)
     * @param acquisitionPlan        the routed acquisition plan (null before
     *                               ContextStage K0.6.2)
     * @param sourceSelectionPlan    the trusted source selection plan (null before
     *                               ContextStage K0.6.3)
     * @param acquisitionDecisionPlan the cache decision plan (null before
     *                               ContextStage K0.6.4)
     */
    public CognitiveState(
            ReasoningResult reasoning,
            InferenceResult inference,
            PlanningResponse planning,
            ReflectionAnalysis reflection,
            int reflectionIteration,
            List<Double> qualityHistory,
            EvidencePackage evidencePackage,
            IntentProfile intentProfile,
            DomainProfile domainProfile,
            UserConstraints userConstraints,
            GoalStructure goalStructure,
            AmbiguityProfile ambiguityProfile,
            ReasoningGraph reasoningGraph,
            SynthesisGraph synthesisGraph,
            CausalGraph causalGraph,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph,
            KnowledgeRequirementSet knowledgeRequirements,
            AcquisitionPlan acquisitionPlan,
            SourceSelectionPlan sourceSelectionPlan,
            AcquisitionDecisionPlan acquisitionDecisionPlan) {
        this(reasoning, inference, planning, reflection, reflectionIteration,
                qualityHistory, evidencePackage, intentProfile, domainProfile,
                userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph,
                uncertaintyGraph, knowledgeRequirements, acquisitionPlan,
                                sourceSelectionPlan, acquisitionDecisionPlan, null, null, null, null, null, null);
    }
    /**
     * Backward-compatible constructor accepting the K0.6.5 shape (twenty-two
     * artifacts without an alternative set). The alternative set is
     * initialized to {@code null}.
     *
     * @param reasoning               the reasoning result (null before ReasoningStage)
     * @param inference               the inference result (null before InferenceStage)
     * @param planning                the planning response (null before PlanningStage)
     * @param reflection              the latest reflection analysis (null before
     *                                ReflectionStage)
     * @param reflectionIteration     the number of completed reflection passes
     * @param qualityHistory          quality scores recorded per reflection pass
     * @param evidencePackage         the resolved evidence package (null before InferenceStage)
     * @param intentProfile           the detected primary intent (null before ContextStage)
     * @param domainProfile           the detected domain profile (null before ContextStage)
     * @param userConstraints         the extracted user constraints (null before ContextStage)
     * @param goalStructure           the identified goal structure (null before ContextStage)
     * @param ambiguityProfile        the ambiguity diagnosis (null before ContextStage)
     * @param reasoningGraph          the multi-hop reasoning graph (null before
     *                                ReasoningStage R1)
     * @param synthesisGraph          the evidence synthesis graph (null before
     *                                ReasoningStage R2)
     * @param causalGraph             the causal reasoning graph (null before
     *                                ReasoningStage R3)
     * @param verificationGraph       the self verification graph (null before
     *                                ReasoningStage R4)
     * @param uncertaintyGraph        the uncertainty modeling graph (null before
     *                                ReasoningStage R5)
     * @param knowledgeRequirements   the discovered knowledge requirement set (null
     *                                before ContextStage K0.6.1)
     * @param acquisitionPlan         the routed acquisition plan (null before
     *                                ContextStage K0.6.2)
     * @param sourceSelectionPlan     the trusted source selection plan (null before
     *                                ContextStage K0.6.3)
     * @param acquisitionDecisionPlan the cache decision plan (null before
     *                                ContextStage K0.6.4)
     * @param acquisitionResult       the executed acquisition result (null before
     *                                ContextStage K0.6.5)
     */
    public CognitiveState(
            ReasoningResult reasoning,
            InferenceResult inference,
            PlanningResponse planning,
            ReflectionAnalysis reflection,
            int reflectionIteration,
            List<Double> qualityHistory,
            EvidencePackage evidencePackage,
            IntentProfile intentProfile,
            DomainProfile domainProfile,
            UserConstraints userConstraints,
            GoalStructure goalStructure,
            AmbiguityProfile ambiguityProfile,
            ReasoningGraph reasoningGraph,
            SynthesisGraph synthesisGraph,
            CausalGraph causalGraph,
            VerificationGraph verificationGraph,
            UncertaintyGraph uncertaintyGraph,
            KnowledgeRequirementSet knowledgeRequirements,
            AcquisitionPlan acquisitionPlan,
            SourceSelectionPlan sourceSelectionPlan,
            AcquisitionDecisionPlan acquisitionDecisionPlan,
            AcquisitionResult acquisitionResult) {
        this(reasoning, inference, planning, reflection, reflectionIteration,
                qualityHistory, evidencePackage, intentProfile, domainProfile,
                userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph,
                uncertaintyGraph, knowledgeRequirements, acquisitionPlan,
                                sourceSelectionPlan, acquisitionDecisionPlan, acquisitionResult,
                null, null, null, null, null);
    }

    /**
     * Returns the initial empty cognitive state (no artifacts, iteration 0).
     *
     * @return an empty state (never null)
     */
    public static CognitiveState empty() {
        return new CognitiveState(null, null, null, null, 0, List.of(),
                null, null, null, null, null, null, null, null, null, null,
                            null, null, null, null, null, null, null, null, null, null, null);
    }

    /**
     * Returns a new state with the given reasoning result.
     *
     * @param reasoning the reasoning result (must not be null)
     * @return a new CognitiveState (never null)
     */
    public CognitiveState withReasoning(ReasoningResult reasoning) {
        Objects.requireNonNull(reasoning, "reasoning must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new state with the given inference result.
     *
     * @param inference the inference result (must not be null)
     * @return a new CognitiveState (never null)
     */
    public CognitiveState withInference(InferenceResult inference) {
        Objects.requireNonNull(inference, "inference must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new state with the given planning response.
     *
     * @param planning the planning response (must not be null)
     * @return a new CognitiveState (never null)
     */
    public CognitiveState withPlanning(PlanningResponse planning) {
        Objects.requireNonNull(planning, "planning must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new state with the given reflection analysis and quality
     * score, advancing the reflection iteration by one.
     *
     * @param reflection the reflection analysis (must not be null)
     * @param qualityScore the quality score recorded for this pass (0.0-1.0)
     * @return a new CognitiveState (never null)
     */
        public CognitiveState withReflection(ReflectionAnalysis reflection, double qualityScore) {
        Objects.requireNonNull(reflection, "reflection must not be null");
        List<Double> history = new ArrayList<>(qualityHistory);
        history.add(qualityScore);
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration + 1, List.copyOf(history), evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns the canonical evidence package produced by the conflict resolver.
     * The evidence package is the authoritative source of evidence for the
     * inference kernel. It is not stored in metadata - it lives only inside
     * the immutable CognitiveState.
     *
     * @return the evidence package (null before InferenceStage resolves conflicts)
     */
    @Override
    public EvidencePackage evidencePackage() {
        return evidencePackage;
    }

    /**
     * Returns a new cognitive state with the given evidence package,
     * preserving all existing cognitive artifacts.
     *
     * @param pkg the resolved evidence package (must not be null)
     * @return a new CognitiveState with the evidence package set (never null)
     */
    public CognitiveState withEvidencePackage(EvidencePackage pkg) {
        Objects.requireNonNull(pkg, "evidencePackage must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, pkg, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile, reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new state with the reflection iteration advanced by one,
     * without replacing the reflection analysis.
     *
     * @return a new CognitiveState (never null)
     */
        public CognitiveState incrementReflection() {
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration + 1, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile, reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given intent profile,
     * preserving all existing cognitive artifacts.
     *
     * @param intentProfile the detected primary intent profile (must not be null)
     * @return a new CognitiveState with the intent profile set (never null)
     */
    public CognitiveState withIntentProfile(IntentProfile intentProfile) {
        Objects.requireNonNull(intentProfile, "intentProfile must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given domain profile,
     * preserving all existing cognitive artifacts.
     *
     * @param domainProfile the detected domain profile (must not be null)
     * @return a new CognitiveState with the domain profile set (never null)
     */
    public CognitiveState withDomainProfile(DomainProfile domainProfile) {
        Objects.requireNonNull(domainProfile, "domainProfile must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given user constraints,
     * preserving all existing cognitive artifacts.
     *
     * @param userConstraints the extracted user constraints (must not be null)
     * @return a new CognitiveState with the user constraints set (never null)
     */
    public CognitiveState withUserConstraints(UserConstraints userConstraints) {
        Objects.requireNonNull(userConstraints, "userConstraints must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given goal structure,
     * preserving all existing cognitive artifacts.
     *
     * @param goalStructure the identified goal structure (must not be null)
     * @return a new CognitiveState with the goal structure set (never null)
     */
    public CognitiveState withGoalStructure(GoalStructure goalStructure) {
        Objects.requireNonNull(goalStructure, "goalStructure must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given ambiguity profile,
     * preserving all existing cognitive artifacts.
     *
     * @param ambiguityProfile the ambiguity diagnosis (must not be null)
     * @return a new CognitiveState with the ambiguity profile set (never null)
     */
    public CognitiveState withAmbiguityProfile(AmbiguityProfile ambiguityProfile) {
        Objects.requireNonNull(ambiguityProfile, "ambiguityProfile must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }


    /**
     * Returns a new cognitive state with the given reasoning graph,
     * preserving all existing cognitive artifacts.
     *
     * @param reasoningGraph the multi-hop reasoning graph (must not be null)
     * @return a new CognitiveState with the reasoning graph set (never null)
     */
    public CognitiveState withReasoningGraph(ReasoningGraph reasoningGraph) {
        Objects.requireNonNull(reasoningGraph, "reasoningGraph must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }


    /**
     * Returns a new cognitive state with the given synthesis graph,
     * preserving all existing cognitive artifacts.
     *
     * @param synthesisGraph the evidence synthesis graph (must not be null)
     * @return a new CognitiveState with the synthesis graph set (never null)
     */
    public CognitiveState withSynthesisGraph(SynthesisGraph synthesisGraph) {
        Objects.requireNonNull(synthesisGraph, "synthesisGraph must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given causal graph,
     * preserving all existing cognitive artifacts.
     *
     * @param causalGraph the causal reasoning graph (must not be null)
     * @return a new CognitiveState with the causal graph set (never null)
     */
    public CognitiveState withCausalGraph(CausalGraph causalGraph) {
        Objects.requireNonNull(causalGraph, "causalGraph must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }


    /**
     * Returns a new cognitive state with the given verification graph,
     * preserving all existing cognitive artifacts.
     *
     * @param verificationGraph the self verification graph (must not be null)
     * @return a new CognitiveState with the verification graph set (never null)
     */
    public CognitiveState withVerificationGraph(VerificationGraph verificationGraph) {
        Objects.requireNonNull(verificationGraph, "verificationGraph must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }


    /**
     * Returns a new cognitive state with the given uncertainty modeling graph,
     * preserving all existing cognitive artifacts.
     *
     * @param uncertaintyGraph the uncertainty modeling graph (must not be null)
     * @return a new CognitiveState with the uncertainty graph set (never null)
     */
    public CognitiveState withUncertaintyGraph(UncertaintyGraph uncertaintyGraph) {
        Objects.requireNonNull(uncertaintyGraph, "uncertaintyGraph must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given knowledge requirement set,
     * preserving all existing cognitive artifacts.
     *
     * @param knowledgeRequirements the discovered knowledge requirement set
     *                              (must not be null)
     * @return a new CognitiveState with the requirement set set (never null)
     */
    public CognitiveState withKnowledgeRequirements(
            KnowledgeRequirementSet knowledgeRequirements) {
        Objects.requireNonNull(knowledgeRequirements,
                "knowledgeRequirements must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given acquisition plan,
     * preserving all existing cognitive artifacts.
     *
     * @param acquisitionPlan the routed acquisition plan (must not be null)
     * @return a new CognitiveState with the acquisition plan set (never null)
     */
    public CognitiveState withAcquisitionPlan(AcquisitionPlan acquisitionPlan) {
        Objects.requireNonNull(acquisitionPlan, "acquisitionPlan must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given trusted source selection
     * plan, preserving all existing cognitive artifacts.
     *
     * @param sourceSelectionPlan the trusted source selection plan (must not be
     *                            null)
     * @return a new CognitiveState with the source selection plan set (never null)
     */
    public CognitiveState withSourceSelectionPlan(SourceSelectionPlan sourceSelectionPlan) {
        Objects.requireNonNull(sourceSelectionPlan,
                "sourceSelectionPlan must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given acquisition decision plan,
     * preserving all existing cognitive artifacts.
     *
     * @param acquisitionDecisionPlan the cache decision plan (must not be null)
     * @return a new CognitiveState with the decision plan set (never null)
     */
    public CognitiveState withAcquisitionDecisionPlan(
            AcquisitionDecisionPlan acquisitionDecisionPlan) {
        Objects.requireNonNull(acquisitionDecisionPlan,
                "acquisitionDecisionPlan must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given acquisition result,
     * preserving all existing cognitive artifacts.
     *
     * @param acquisitionResult the executed acquisition result (must not be
     *                          null)
     * @return a new CognitiveState with the acquisition result set (never null)
     */
    public CognitiveState withAcquisitionResult(AcquisitionResult acquisitionResult) {
        Objects.requireNonNull(acquisitionResult,
                "acquisitionResult must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given alternative set,
     * preserving all existing cognitive artifacts.
     *
     * @param alternativeSet the generated solution alternatives (must not be
     *                       null)
     * @return a new CognitiveState with the alternative set set (never null)
     */
    public CognitiveState withAlternativeSet(AlternativeSet alternativeSet) {
        Objects.requireNonNull(alternativeSet, "alternativeSet must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given optimized decision,
     * preserving all existing cognitive artifacts.
     *
     * @param optimizedDecision the I3 decision optimization result (must not be
     *                          null)
     * @return a new CognitiveState with the optimized decision set (never null)
     */
    public CognitiveState withOptimizedDecision(OptimizedDecision optimizedDecision) {
        Objects.requireNonNull(optimizedDecision, "optimizedDecision must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given calibrated decision,
     * preserving all existing cognitive artifacts.
     *
     * @param calibratedDecision the I4 confidence calibration result (must not
     *                          be null)
     * @return a new CognitiveState with the calibrated decision set (never
     *         null)
     */
    public CognitiveState withCalibratedDecision(CalibratedDecision calibratedDecision) {
        Objects.requireNonNull(calibratedDecision, "calibratedDecision must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given explainable decision,
     * preserving all existing cognitive artifacts.
     *
     * @param explainableDecision the I5 explainable decision (must not be
     *                            null)
     * @return a new CognitiveState with the explainable decision set (never
     *         null)
     */
    public CognitiveState withExplainableDecision(ExplainableDecision explainableDecision) {
        Objects.requireNonNull(explainableDecision, "explainableDecision must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns a new cognitive state with the given task dependency graph,
     * preserving all existing cognitive artifacts.
     *
     * <p>The graph is the canonical P2.2 planning artifact and is stored
     * only here (no metadata mirror).</p>
     *
     * @param taskGraph the task dependency graph (must not be null)
     * @return a new CognitiveState with the task graph set (never null)
     */
    public CognitiveState withTaskGraph(TaskGraph taskGraph) {
        Objects.requireNonNull(taskGraph, "taskGraph must not be null");
        return new CognitiveState(reasoning, inference, planning,
                reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile,
                reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph,
                knowledgeRequirements,
                acquisitionPlan,
                sourceSelectionPlan,
                acquisitionDecisionPlan,
                acquisitionResult,
                alternativeSet,
                optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Returns the latest reflection quality score, or NaN when no reflection
     * pass has completed.
     *
     * @return the latest quality score (NaN when history is empty)
     */
    public double latestQualityScore() {
        return qualityHistory.isEmpty() ? Double.NaN : qualityHistory.get(qualityHistory.size() - 1);
    }

    @Override
    public String toString() {
        return "CognitiveState{reasoning=" + (reasoning != null)
                + ", inference=" + (inference != null)
                + ", planning=" + (planning != null)
                + ", reflection=" + (reflection != null)
                + ", reflectionIteration=" + reflectionIteration
                                 + ", qualityHistory=" + qualityHistory
                + ", evidencePackage=" + (evidencePackage != null)
                + ", intentProfile=" + intentProfile
                + ", domainProfile=" + domainProfile
                + ", userConstraints=" + userConstraints
                + ", goalStructure=" + goalStructure + ", ambiguityProfile=" + ambiguityProfile
                + ", alternativeSet=" + (alternativeSet != null)
                + ", optimizedDecision=" + (optimizedDecision != null)
                + ", calibratedDecision=" + (calibratedDecision != null)
                                + ", explainableDecision=" + (explainableDecision != null)
                + ", taskGraph=" + (taskGraph != null) + '}';
    }

    /** Returns a new state with the canonical schedule, preserving all other artifacts. */
    public CognitiveState withExecutionPlan(ExecutionPlan executionPlan) {
        Objects.requireNonNull(executionPlan, "executionPlan must not be null");
        return new CognitiveState(reasoning, inference, planning, reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile, reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph, knowledgeRequirements, acquisitionPlan, sourceSelectionPlan, acquisitionDecisionPlan, acquisitionResult, alternativeSet, optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /** Backward-compatible P2.3 constructor; no replanning result yet. */
    public CognitiveState(
        ReasoningResult reasoning,
        InferenceResult inference,
        PlanningResponse planning,
        ReflectionAnalysis reflection,
        int reflectionIteration,
        List<Double> qualityHistory,
        EvidencePackage evidencePackage,
        IntentProfile intentProfile,
        DomainProfile domainProfile,
        UserConstraints userConstraints,
        GoalStructure goalStructure,
        AmbiguityProfile ambiguityProfile,
        ReasoningGraph reasoningGraph,
        SynthesisGraph synthesisGraph,
        CausalGraph causalGraph,
        VerificationGraph verificationGraph,
        UncertaintyGraph uncertaintyGraph,
        KnowledgeRequirementSet knowledgeRequirements,
        AcquisitionPlan acquisitionPlan,
        SourceSelectionPlan sourceSelectionPlan,
        AcquisitionDecisionPlan acquisitionDecisionPlan,
        AcquisitionResult acquisitionResult,
        AlternativeSet alternativeSet,
                OptimizedDecision optimizedDecision,
        CalibratedDecision calibratedDecision,
        ExplainableDecision explainableDecision,
        TaskGraph taskGraph,
        ExecutionPlan executionPlan) {
        this(reasoning, inference, planning, reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile, reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph, knowledgeRequirements, acquisitionPlan, sourceSelectionPlan, acquisitionDecisionPlan, acquisitionResult, alternativeSet, optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, null, null);
    }

    /** Stores the replanning artifact, preserving the schedule and all other artifacts. */
    public CognitiveState withReplanningResult(ReplanningResult replanningResult) {
        Objects.requireNonNull(replanningResult, "replanningResult must not be null");
        return new CognitiveState(reasoning, inference, planning, reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile, reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph, knowledgeRequirements, acquisitionPlan, sourceSelectionPlan, acquisitionDecisionPlan, acquisitionResult, alternativeSet, optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }

    /**
     * Stores the executable planning graph, preserving the replanning
     * artifact and all other artifacts.
     *
     * @param executablePlanningGraph the execution-ready graph (must not be
     *                                null)
     * @return a new state carrying the graph (never null)
     */
    public CognitiveState withExecutablePlanningGraph(ExecutablePlanningGraph executablePlanningGraph) {
        Objects.requireNonNull(executablePlanningGraph, "executablePlanningGraph must not be null");
        return new CognitiveState(reasoning, inference, planning, reflection, reflectionIteration, qualityHistory, evidencePackage, intentProfile, domainProfile, userConstraints, goalStructure, ambiguityProfile, reasoningGraph, synthesisGraph, causalGraph, verificationGraph, uncertaintyGraph, knowledgeRequirements, acquisitionPlan, sourceSelectionPlan, acquisitionDecisionPlan, acquisitionResult, alternativeSet, optimizedDecision, calibratedDecision, explainableDecision, taskGraph, executionPlan, replanningResult, executablePlanningGraph);
    }
}

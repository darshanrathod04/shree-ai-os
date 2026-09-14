package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.UncertaintyGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;

/**
 * <b>UncertaintyModelingEngine</b>
 *
 * <p>Port for the R5 Uncertainty Modeling milestone. Consumes the integrated
 * reasoning artifacts of R1-R4 (reasoning, synthesis, causal, verification)
 * together with the K5 reliability result and produces an immutable
 * {@link UncertaintyGraph} that explains what is genuinely certain,
 * uncertain or unknown.</p>
 *
 * <p>The engine never retrieves new knowledge, never generates answers and
 * never makes decisions. It only evaluates certainty - evidence certainty,
 * knowledge gaps, conflicting reliability and overall certainty - using
 * deterministic, locked rules. No LLM, no ML, no embeddings, no probability.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R5 Uncertainty Modeling</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface UncertaintyModelingEngine {

    /**
     * Models certainty and uncertainty from the integrated reasoning artifacts.
     *
     * @param reasoningGraph    the R1 reasoning graph (never null)
     * @param synthesisGraph    the R2 synthesis graph (never null)
     * @param causalGraph       the R3 causal graph (never null)
     * @param verificationGraph the R4 verification graph (never null)
     * @param reliabilityResult the K5 reliability result (never null)
     * @return the immutable uncertainty graph (never null)
     * @throws NullPointerException if any argument is null
     */
    UncertaintyGraph model(ReasoningGraph reasoningGraph,
                           SynthesisGraph synthesisGraph,
                           CausalGraph causalGraph,
                           VerificationGraph verificationGraph,
                           ReliabilityResult reliabilityResult);
}

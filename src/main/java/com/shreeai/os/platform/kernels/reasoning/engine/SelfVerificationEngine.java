package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.VerificationGraph;

/**
 * <b>SelfVerificationEngine</b>
 *
 * <p>Port for the R4 Self Verification milestone. Consumes the R1
 * {@link ReasoningGraph}, R2 {@link SynthesisGraph} and R3
 * {@link CausalGraph} to produce an immutable
 * {@link VerificationGraph} that explains where reasoning is verified,
 * contradicted, or incomplete.
 *
 * <p>The engine never retrieves new knowledge, never reasons about
 * conclusions, and never generates answers. Verification is a quality
 * evaluation layer, not another reasoning pass.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R4 Self Verification</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface SelfVerificationEngine {

    /**
     * Verifies the integrated reasoning artifacts.
     *
     * @param reasoningGraph  the R1 reasoning graph (never null)
     * @param synthesisGraph  the R2 synthesis graph (never null)
     * @param causalGraph     the R3 causal graph (never null)
     * @return the immutable verification graph (never null)
     * @throws NullPointerException if any argument is null
     */
    VerificationGraph verify(ReasoningGraph reasoningGraph,
                             SynthesisGraph synthesisGraph,
                             CausalGraph causalGraph);
}

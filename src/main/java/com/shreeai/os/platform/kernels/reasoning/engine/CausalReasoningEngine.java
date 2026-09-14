package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.reasoning.model.CausalGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;
import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;

/**
 * <b>CausalReasoningEngine</b>
 *
 * <p>Port for the R3 Causal Reasoning milestone. Consumes the
 * {@link ReasoningGraph} (R1) and {@link SynthesisGraph} (R2) to produce
 * an immutable {@link CausalGraph} of cause-effect relationships.</p>
 *
 * <p>The engine never retrieves knowledge, never generates text and never
 * verifies conclusions. It only discovers causal structure.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R3 Causal Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface CausalReasoningEngine {

    /**
     * Discovers cause-effect relationships from the given reasoning and
     * synthesis graphs.
     *
     * @param reasoningGraph the R1 reasoning graph (never null)
     * @param synthesisGraph the R2 synthesis graph (never null)
     * @return the immutable causal graph (never null)
     * @throws NullPointerException if either argument is null
     */
    CausalGraph analyze(ReasoningGraph reasoningGraph, SynthesisGraph synthesisGraph, ConceptGraph conceptGraph);
}

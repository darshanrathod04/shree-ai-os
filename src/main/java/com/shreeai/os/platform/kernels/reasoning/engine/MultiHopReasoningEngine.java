package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;

/**
 * <b>MultiHopReasoningEngine</b>
 *
 * <p>Port for the R1 Multi-Hop Reasoning milestone. Converts a
 * {@link ReliabilityResult} (trusted, ranked evidence) and a
 * {@link com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph}
 * into an immutable {@link ReasoningGraph}: evidence nodes, concept nodes,
 * multi-hop chain edges and deterministic hypotheses.</p>
 *
 * <p>The engine never generates answers, never reasons about conclusions and
 * never transforms evidence content. It only connects trusted evidence across
 * the knowledge graph up to a deterministic maximum depth.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R1 Multi-Hop Reasoning</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface MultiHopReasoningEngine {

    /**
     * Builds a deterministic {@link ReasoningGraph} from the trusted evidence
     * in {@code reliability} and the relationship structure in
     * {@code conceptGraph}.
     *
     * @param reliability  the immutable, trust-evaluated retrieval result (never
     *                     null)
     * @param conceptGraph the immutable knowledge graph (never null)
     * @return the immutable, stably ordered reasoning graph (never null)
     * @throws NullPointerException if either argument is null
     */
    ReasoningGraph reason(ReliabilityResult reliability,
                           com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph conceptGraph);
}

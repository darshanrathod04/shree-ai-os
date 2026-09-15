package com.shreeai.os.platform.kernels.reasoning.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ConceptGraph;
import com.shreeai.os.platform.kernels.reasoning.model.ReasoningGraph;
import com.shreeai.os.platform.kernels.reasoning.model.SynthesisGraph;

/**
 * <b>EvidenceSynthesisEngine</b>
 *
 * <p>Port for the R2 Evidence Synthesis milestone. Consumes the
 * {@link ReasoningGraph} produced by R1, the original
 * {@link com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult}
 * and the {@link ConceptGraph} to produce an immutable {@link SynthesisGraph}:
 * evidence clusters, synthesized facts and provenance edges.</p>
 *
 * <p>The engine never retrieves knowledge again, never generates user-facing
 * text and never reasons about conclusions. It only merges multiple trusted
 * evidence sources into one coherent understanding.</p>
 *
 * <p><b>Ownership:</b> Reasoning Kernel - R2 Evidence Synthesis</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface EvidenceSynthesisEngine {

    /**
     * Produces a deterministic {@link SynthesisGraph} from the trusted
     * evidence, the multi-hop reasoning graph and the concept graph.
     *
     * @param reasoningGraph the R1 reasoning graph (never null)
     * @param reliability    the original reliability result carrying trust
     *                       scores (never null)
     * @param conceptGraph   the concept graph providing relationship types
     *                       (never null)
     * @return the immutable, stably ordered synthesis graph (never null)
     * @throws NullPointerException if any argument is null
     */
    SynthesisGraph synthesize(ReasoningGraph reasoningGraph,
                              com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult reliability,
                              ConceptGraph conceptGraph);
}

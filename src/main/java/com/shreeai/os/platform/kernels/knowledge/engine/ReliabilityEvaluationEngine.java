package com.shreeai.os.platform.kernels.knowledge.engine;

import com.shreeai.os.platform.kernels.knowledge.model.ReliabilityResult;
import com.shreeai.os.platform.kernels.knowledge.model.RetrievalResult;

/**
 * <b>ReliabilityEvaluationEngine</b>
 *
 * <p>The K5 contract that evaluates authority, freshness, provenance and
 * evidence quality before any reasoning occurs. It is a deterministic trust
 * evaluation engine - it never retrieves and never reasons; its single
 * responsibility is trust.</p>
 *
 * <p><b>Pipeline position:</b> Retrieval Engine → {@code RetrievalResult} →
 * this engine → {@link ReliabilityResult} → Reasoning (future). Reasoning
 * must consume the reliability result, never raw retrieval.</p>
 *
 * <p><b>Contract:</b> implementations must be stateless per evaluation,
 * thread-safe, deterministic and immutable, with no ML, no LLM and no
 * embeddings. Identical input and clock always produce identical output.</p>
 *
 * <p><b>Ownership:</b> Knowledge Kernel - K5 Reliability and Freshness</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultReliabilityEvaluationEngine
 */
public interface ReliabilityEvaluationEngine {

    /**
     * Evaluates the trust of every piece of evidence in the retrieval result.
     *
     * @param retrieval the retrieval result to annotate (must not be null)
     * @return the immutable, stably ordered trust artifact (never null; empty
     *         evidence evaluates to an empty result with overall trust 0.0)
     */
    ReliabilityResult evaluate(RetrievalResult retrieval);
}

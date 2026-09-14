package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;
import com.shreeai.os.platform.kernels.context.model.ContextIntelligence;

/**
 * <b>SourceDiscoveryEngine</b>
 *
 * <p>The K0.6.1 port of the Knowledge Acquisition Kernel. Transforms Context
 * Intelligence into a structured {@link KnowledgeRequirementSet}.</p>
 *
 * <p><b>This engine answers exactly one question:</b></p>
 * <blockquote>What knowledge is required to solve this request?</blockquote>
 *
 * <p><b>This engine must never:</b></p>
 * <ul>
 *   <li>search the web or call external APIs,</li>
 *   <li>select providers or rank sources (K0.6.2+),</li>
 *   <li>retrieve or ingest documents (K2+),</li>
 *   <li>use an LLM, embeddings or probabilistic scoring.</li>
 * </ul>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and rule-based. Identical inputs always produce structurally
 * equal requirement sets.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.1 Source Discovery</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface SourceDiscoveryEngine {

    /**
     * Discovers the knowledge requirements for the given context intelligence
     * snapshot.
     *
     * @param contextIntelligence the canonical context intelligence aggregate
     *                            (must not be null)
     * @return the canonical, immutable knowledge requirement set (never null)
     * @throws NullPointerException if contextIntelligence is null
     */
    KnowledgeRequirementSet discover(ContextIntelligence contextIntelligence);
}

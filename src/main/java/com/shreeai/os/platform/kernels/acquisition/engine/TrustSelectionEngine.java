package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.SourceSelectionPlan;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeSourceRegistry;

/**
 * <b>TrustSelectionEngine</b>
 *
 * <p>The K0.6.3 port of the Knowledge Acquisition Kernel. Transforms an
 * immutable {@link AcquisitionPlan} into a deterministic
 * {@link SourceSelectionPlan} by selecting, for every acquisition target, the
 * most authoritative concrete source available in the K1
 * {@link KnowledgeSourceRegistry}.</p>
 *
 * <p><b>This engine answers exactly one question:</b></p>
 * <blockquote>Among all available providers, which concrete source should be
 * selected?</blockquote>
 *
 * <p><b>This engine must never:</b></p>
 * <ul>
 *   <li>download content or crawl websites (K0.6.4+),</li>
 *   <li>parse documents or perform ingestion (K2),</li>
 *   <li>make network calls or call external APIs,</li>
 *   <li>use embeddings or an LLM.</li>
 * </ul>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and rule-based. They consume the registry as a read-only
 * catalog and never mutate it. Identical inputs (plan + registry state) always
 * produce structurally equal plans. Selection never throws for an empty or
 * unrelated catalog - topics that cannot be satisfied are omitted rather than
 * fabricated.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.3 Trust &amp; Source Selection</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultTrustSelectionEngine
 */
public interface TrustSelectionEngine {

    /**
     * Selects one trusted source for every acquisition target that the registry
     * can satisfy.
     *
     * @param acquisitionPlan the routed acquisition plan from K0.6.2 (must not
     *                        be null)
     * @param registry        the K1 source registry used as the available source
     *                        catalog (must not be null, read-only)
     * @return the canonical, immutable source selection plan (never null)
     * @throws NullPointerException if acquisitionPlan or registry is null
     */
    SourceSelectionPlan select(AcquisitionPlan acquisitionPlan,
                               KnowledgeSourceRegistry registry);
}
package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.SourceSelectionPlan;
import com.shreeai.os.platform.kernels.context.model.ContextIntelligence;
import com.shreeai.os.platform.kernels.knowledge.engine.KnowledgeSourceRegistry;

/**
 * <b>FreshnessPolicyEngine</b>
 *
 * <p>The K0.6.4 port of the Knowledge Acquisition Kernel. Transforms an
 * immutable {@link SourceSelectionPlan} into a deterministic
 * {@link AcquisitionDecisionPlan} by deciding, for every selected source,
 * whether cached knowledge may be reused or fresh knowledge must be
 * acquired.</p>
 *
 * <p><b>This engine answers exactly one question:</b></p>
 * <blockquote>Should Shree AI OS use cached knowledge or acquire fresh
 * knowledge?</blockquote>
 *
 * <p><b>This engine must never:</b></p>
 * <ul>
 *   <li>download content or crawl websites (K0.6.5+),</li>
 *   <li>parse documents or perform ingestion (K2),</li>
 *   <li>make network calls or call external APIs,</li>
 *   <li>mutate the source registry,</li>
 *   <li>use embeddings or an LLM.</li>
 * </ul>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and rule-based. They consume the registry as a read-only
 * catalog and never modify it. Time is read only from the injected clock, so
 * identical inputs evaluated at the same instant always produce structurally
 * equal plans. Decision making never throws for an empty selection plan or an
 * empty registry.</p>
 *
 * <p><b>Locked decision order:</b> registry lookup (&rarr; {@code ACQUIRE}),
 * status check (&rarr; {@code ACQUIRE}), age evaluation
 * (&rarr; {@code USE_CACHE} / {@code REFRESH}), then the explicit latest-request
 * override (&rarr; {@code REFRESH}).</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.4 Freshness &amp; Cache Policy</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultFreshnessPolicyEngine
 */
public interface FreshnessPolicyEngine {

    /**
     * Decides, for every selected source, whether cached knowledge is reused or
     * fresh knowledge must be acquired.
     *
     * @param selectionPlan the trusted source selection plan from K0.6.3 (must
     *                      not be null)
     * @param registry      the K1 source registry used as the read-only catalog
     *                      of source state and declared timestamps (must not be
     *                      null)
     * @param context       the context intelligence artifacts used for the
     *                      explicit latest-request check (may be null, in which
     *                      case no override is applied)
     * @return the canonical, immutable acquisition decision plan (never null)
     * @throws NullPointerException if selectionPlan or registry is null
     */
    AcquisitionDecisionPlan decide(SourceSelectionPlan selectionPlan,
                                   KnowledgeSourceRegistry registry,
                                   ContextIntelligence context);

    /**
     * Decides without any context artifacts: the explicit latest-request
     * override is not applied, every other policy stage still applies.
     *
     * @param selectionPlan the trusted source selection plan from K0.6.3 (must
     *                      not be null)
     * @param registry      the K1 source registry (must not be null)
     * @return the canonical, immutable acquisition decision plan (never null)
     * @throws NullPointerException if selectionPlan or registry is null
     */
    default AcquisitionDecisionPlan decide(SourceSelectionPlan selectionPlan,
                                           KnowledgeSourceRegistry registry) {
        return decide(selectionPlan, registry, null);
    }
}
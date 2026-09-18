package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.KnowledgeRequirementSet;

/**
 * <b>ProviderRouter</b>
 *
 * <p>The K0.6.2 port of the Knowledge Acquisition Kernel. Transforms a
 * {@link KnowledgeRequirementSet} into an immutable {@link AcquisitionPlan}.</p>
 *
 * <p><b>This engine answers exactly one question:</b></p>
 * <blockquote>Which provider type should satisfy each required knowledge
 * topic?</blockquote>
 *
 * <p><b>This engine must never:</b></p>
 * <ul>
 *   <li>search the internet or download documents (K0.6.3+),</li>
 *   <li>call APIs or perform network operations,</li>
 *   <li>rank providers by trust (K0.6.3 Trust &amp; Source Selection),</li>
 *   <li>perform ingestion (K2) or use an LLM.</li>
 * </ul>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and rule-based. Routing never fails - every topic always
 * receives a provider. Identical inputs always produce structurally equal
 * plans.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.2 Provider Router</p>
 * <p><b>Version:</b> 1.0</p>
 */
public interface ProviderRouter {

    /**
     * Routes every required knowledge topic to a provider type.
     *
     * @param requirements the canonical knowledge requirement set (must not be
     *                     null)
     * @return the canonical, immutable acquisition plan (never null)
     * @throws NullPointerException if requirements is null
     */
    AcquisitionPlan route(KnowledgeRequirementSet requirements);
}

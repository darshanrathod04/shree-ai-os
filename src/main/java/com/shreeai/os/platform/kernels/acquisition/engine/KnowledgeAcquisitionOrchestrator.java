package com.shreeai.os.platform.kernels.acquisition.engine;

import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionDecisionPlan;
import com.shreeai.os.platform.kernels.acquisition.model.AcquisitionResult;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeDocument;

import java.util.List;
import java.util.Map;

/**
 * <b>KnowledgeAcquisitionOrchestrator</b>
 *
 * <p>The K0.6.5 contract that executes the deterministic acquisition workflow
 * decided by the K0.6.4 Freshness &amp; Cache Policy Engine. The orchestrator
 * <em>never decides what to acquire</em> - that question is already answered by
 * the immutable {@link AcquisitionDecisionPlan}; its single responsibility is
 * to execute the locked decision semantics:</p>
 *
 * <table border="1">
 *   <caption>Locked execution rules</caption>
 *   <tr><th>Decision</th><th>Action</th><th>Status</th></tr>
 *   <tr><td>{@code USE_CACHE}</td><td>Reuse the existing cached document.</td>
 *       <td>{@code SKIPPED}</td></tr>
 *   <tr><td>{@code ACQUIRE}</td><td>First-time ingestion of the source.</td>
 *       <td>{@code ACQUIRED}</td></tr>
 *   <tr><td>{@code REFRESH}</td><td>Re-ingest the same source; the new document
 *       replaces the cached one.</td><td>{@code ACQUIRED}</td></tr>
 * </table>
 *
 * <p><b>Contract:</b> implementations must be stateless, thread-safe,
 * deterministic and immutable. No network calls, no LLM, no adaptive
 * heuristics. A failing target is isolated: it is recorded as {@code FAILED}
 * and the remaining targets are always executed.</p>
 *
 * <p><b>Ownership:</b> Knowledge Acquisition Kernel - K0.6.5 Acquisition Orchestrator</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultKnowledgeAcquisitionOrchestrator
 */
public interface KnowledgeAcquisitionOrchestrator {

    /**
     * Executes every decision of the given plan against the read-only cache
     * and content supply, producing the canonical acquisition result.
     *
     * @param plan                 the immutable decision plan to execute (must
     *                             not be null)
     * @param cachedDocuments      the read-only cache view of previously
     *                             ingested documents (may be null - treated as
     *                             empty; never mutated)
     * @param rawContentBySourceId the read-only supply of raw content per
     *                             source id, consumed by {@code ACQUIRE} and
     *                             {@code REFRESH} decisions (may be null -
     *                             treated as empty; never mutated)
     * @return the canonical, immutable acquisition result (never null)
     * @throws NullPointerException if plan is null
     */
    AcquisitionResult execute(AcquisitionDecisionPlan plan,
                              List<KnowledgeDocument> cachedDocuments,
                              Map<String, String> rawContentBySourceId);

    /**
     * Executes the given plan with an empty cache and an empty content supply.
     * Equivalent to {@code execute(plan, List.of(), Map.of())}.
     *
     * @param plan the immutable decision plan to execute (must not be null)
     * @return the canonical, immutable acquisition result (never null)
     * @throws NullPointerException if plan is null
     */
    AcquisitionResult execute(AcquisitionDecisionPlan plan);
}

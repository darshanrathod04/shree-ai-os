package platform.kernels.knowledge.engine;

import platform.kernels.knowledge.model.KnowledgeGraph;
import platform.kernels.knowledge.model.KnowledgeId;
import platform.kernels.knowledge.model.KnowledgeNode;
import platform.kernels.knowledge.model.KnowledgeRelationship;

/**
 * <b>KnowledgeProcessingEngine</b>
 *
 * <p>Defines the processing contract for deterministic semantic graph transformations
 * within the Knowledge Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines processing operations for deterministic semantic graph transformations.</li>
 *   <li>Processes creation, update, deletion, linking, unlinking, snapshotting, merging, and cloning.</li>
 *   <li>Returns {@link KnowledgeProcessingResult} from every operation.</li>
 *   <li>Contains no validation, no reasoning, no persistence.</li>
 *   <li>Defines processing contracts only — no implementation.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — every operation depends only on its inputs.</li>
 *   <li>Thread-safe — no mutable state.</li>
 *   <li>Deterministic — same inputs always produce same outputs.</li>
 *   <li>Pure processing — no orchestration, validation, or exception translation.</li>
 * </ul>
 *
 * <p><b>Processing Operations:</b></p>
 * <ul>
 *   <li>processCreate — Prepare creation of immutable knowledge structures.</li>
 *   <li>processUpdate — Prepare updated immutable graph state.</li>
 *   <li>processDelete — Prepare graph state after removal of knowledge entities.</li>
 *   <li>processLink — Prepare graph state after creating semantic relationships.</li>
 *   <li>processUnlink — Prepare graph state after removing semantic relationships.</li>
 *   <li>processSnapshot — Produce immutable semantic snapshots.</li>
 *   <li>processMerge — Prepare merged immutable graph state.</li>
 *   <li>processClone — Produce immutable graph copies.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-106, EIO-ARCH-001</p>
 *
 * @see platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine
 * @see platform.kernels.knowledge.engine.KnowledgeProcessingResult
 */
public interface KnowledgeProcessingEngine {

    /**
     * Processes the creation of a knowledge node within the graph.
     *
     * <p>Prepares an immutable graph state that includes the new node.
     * The node is assumed to be structurally valid — validation is the
     * responsibility of the service layer.</p>
     *
     * @param graph   the existing knowledge graph (must not be null)
     * @param node    the knowledge node to create (must not be null)
     * @return a processing result containing the updated graph state
     */
    KnowledgeProcessingResult processCreate(KnowledgeGraph graph, KnowledgeNode node);

    /**
     * Processes the update of a knowledge node within the graph.
     *
     * <p>Prepares an immutable graph state with the node's attributes updated.
     * The node identity is used to locate the existing node.</p>
     *
     * @param graph   the existing knowledge graph (must not be null)
     * @param node    the knowledge node with updated attributes (must not be null)
     * @return a processing result containing the updated graph state
     */
    KnowledgeProcessingResult processUpdate(KnowledgeGraph graph, KnowledgeNode node);

    /**
     * Processes the deletion of a knowledge node from the graph.
     *
     * <p>Prepares an immutable graph state with the specified node removed.
     * Associated relationships may also be removed as part of this operation.</p>
     *
     * @param graph       the existing knowledge graph (must not be null)
     * @param knowledgeId the identifier of the node to delete (must not be null)
     * @return a processing result containing the updated graph state
     */
    KnowledgeProcessingResult processDelete(KnowledgeGraph graph, KnowledgeId knowledgeId);

    /**
     * Processes the creation of a semantic relationship between two nodes.
     *
     * <p>Prepares an immutable graph state that includes the new relationship.
     * Does not validate relationship correctness — that belongs to future kernels.</p>
     *
     * @param graph        the existing knowledge graph (must not be null)
     * @param relationship the relationship to create (must not be null)
     * @return a processing result containing the updated graph state
     */
    KnowledgeProcessingResult processLink(KnowledgeGraph graph, KnowledgeRelationship relationship);

    /**
     * Processes the removal of a semantic relationship from the graph.
     *
     * <p>Prepares an immutable graph state with the specified relationship removed.</p>
     *
     * @param graph          the existing knowledge graph (must not be null)
     * @param relationshipId the identifier of the relationship to remove (must not be null)
     * @return a processing result containing the updated graph state
     */
    KnowledgeProcessingResult processUnlink(KnowledgeGraph graph, KnowledgeId relationshipId);

    /**
     * Processes the creation of an immutable snapshot of the knowledge graph.
     *
     * <p>Produces a processing result containing a snapshot of the graph at
     * the current point in time. The snapshot is semantically isolated from
     * subsequent mutations.</p>
     *
     * @param graph      the knowledge graph to snapshot (must not be null)
     * @param snapshotId the identifier for the snapshot (must not be null)
     * @return a processing result containing the snapshot
     */
    KnowledgeProcessingResult processSnapshot(KnowledgeGraph graph, KnowledgeId snapshotId);

    /**
     * Processes the merge of two knowledge graphs into a single graph.
     *
     * <p>Prepares an immutable merged graph state combining both graphs.
     * Does not perform conflict resolution — that belongs to future kernels.</p>
     *
     * @param baseGraph    the base knowledge graph (must not be null)
     * @param overlayGraph the overlay knowledge graph to merge (must not be null)
     * @return a processing result containing the merged graph state
     */
    KnowledgeProcessingResult processMerge(KnowledgeGraph baseGraph, KnowledgeGraph overlayGraph);

    /**
     * Processes the cloning of a knowledge graph.
     *
     * <p>Produces an immutable copy of the knowledge graph. The clone is
     * semantically identical but independently mutable through replacement.</p>
     *
     * @param graph the knowledge graph to clone (must not be null)
     * @return a processing result containing the cloned graph
     */
    KnowledgeProcessingResult processClone(KnowledgeGraph graph);
}
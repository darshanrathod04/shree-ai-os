package platform.kernels.knowledge.engine;

import platform.kernels.knowledge.model.KnowledgeGraph;
import platform.kernels.knowledge.model.KnowledgeId;
import platform.kernels.knowledge.model.KnowledgeNode;
import platform.kernels.knowledge.model.KnowledgeRelationship;
import platform.kernels.knowledge.model.KnowledgeSnapshot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <b>DefaultKnowledgeProcessingEngine</b>
 *
 * <p>The default implementation of the {@link KnowledgeProcessingEngine} contract.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Performs deterministic semantic graph transformations only.</li>
 *   <li>Creates, updates, deletes, links, unlinks, snapshots, merges, and clones immutable graph state.</li>
 *   <li>Contains no validation — validation is performed by the service layer.</li>
 *   <li>Contains no reasoning, inference, or truth evaluation.</li>
 *   <li>Returns {@link KnowledgeProcessingResult} from every operation.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless — every operation depends only on its inputs.</li>
 *   <li>Thread-safe — no mutable state, no synchronization needed.</li>
 *   <li>Deterministic — same inputs always produce same outputs.</li>
 *   <li>Pure processing — no orchestration, validation, or exception translation.</li>
 * </ul>
 *
 * <p><b>Processing Operations:</b></p>
 * <ul>
 *   <li>processCreate — Produces a graph with a new node added.</li>
 *   <li>processUpdate — Produces a graph with an existing node updated.</li>
 *   <li>processDelete — Produces a graph with a node and its relationships removed.</li>
 *   <li>processLink — Produces a graph with a new relationship added.</li>
 *   <li>processUnlink — Produces a graph with a relationship removed.</li>
 *   <li>processSnapshot — Produces an immutable graph snapshot.</li>
 *   <li>processMerge — Produces a merged graph from two input graphs.</li>
 *   <li>processClone — Produces an identical copy of the input graph.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Knowledge Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-KNW-106, EIO-ARCH-001</p>
 *
 * @see KnowledgeProcessingEngine
 * @see KnowledgeProcessingResult
 */
public final class DefaultKnowledgeProcessingEngine implements KnowledgeProcessingEngine {

    /**
     * Creates a new DefaultKnowledgeProcessingEngine.
     *
     * <p>Uses a public no-argument constructor. The engine is stateless and
     * requires no injected dependencies.</p>
     */
    public DefaultKnowledgeProcessingEngine() {
        // No-op: engine is stateless
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b> Produces a new graph that includes the existing nodes
     * and relationships plus the new node.</p>
     */
    @Override
    public KnowledgeProcessingResult processCreate(KnowledgeGraph graph, KnowledgeNode node) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(node, "node must not be null");

        List<KnowledgeNode> newNodes = new ArrayList<>(graph.getNodes());
        newNodes.add(node);

        KnowledgeGraph updatedGraph = KnowledgeGraph.of(newNodes, graph.getRelationships());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "processCreate");
        metadata.put("nodeId", node.getId().value());

        return KnowledgeProcessingResult.ofGraph(true, updatedGraph, Instant.now(), metadata);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b> Produces a new graph with the identified node replaced
     * by the updated node. All relationships are preserved.</p>
     */
    @Override
    public KnowledgeProcessingResult processUpdate(KnowledgeGraph graph, KnowledgeNode node) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(node, "node must not be null");

        List<KnowledgeNode> updatedNodes = graph.getNodes().stream()
                .map(n -> n.getId().equals(node.getId()) ? node : n)
                .collect(Collectors.toList());

        KnowledgeGraph updatedGraph = KnowledgeGraph.of(updatedNodes, graph.getRelationships());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "processUpdate");
        metadata.put("nodeId", node.getId().value());

        return KnowledgeProcessingResult.ofGraph(true, updatedGraph, Instant.now(), metadata);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b> Produces a new graph with the identified node removed.
     * All relationships referencing the removed node are also removed.</p>
     */
    @Override
    public KnowledgeProcessingResult processDelete(KnowledgeGraph graph, KnowledgeId knowledgeId) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(knowledgeId, "knowledgeId must not be null");

        // Remove the node
        List<KnowledgeNode> remainingNodes = graph.getNodes().stream()
                .filter(n -> !n.getId().equals(knowledgeId))
                .collect(Collectors.toList());

        // Remove relationships referencing the deleted node
        List<KnowledgeRelationship> remainingRelationships = graph.getRelationships().stream()
                .filter(r -> !r.getSourceNodeId().equals(knowledgeId)
                        && !r.getTargetNodeId().equals(knowledgeId))
                .collect(Collectors.toList());

        KnowledgeGraph updatedGraph = KnowledgeGraph.of(remainingNodes, remainingRelationships);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "processDelete");
        metadata.put("nodeId", knowledgeId.value());

        return KnowledgeProcessingResult.ofGraph(true, updatedGraph, Instant.now(), metadata);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b> Produces a new graph that includes the existing nodes
     * and relationships plus the new relationship.</p>
     */
    @Override
    public KnowledgeProcessingResult processLink(KnowledgeGraph graph, KnowledgeRelationship relationship) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(relationship, "relationship must not be null");

        List<KnowledgeRelationship> newRelationships = new ArrayList<>(graph.getRelationships());
        newRelationships.add(relationship);

        KnowledgeGraph updatedGraph = KnowledgeGraph.of(graph.getNodes(), newRelationships);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "processLink");
        metadata.put("relationshipId", relationship.getId().value());

        return KnowledgeProcessingResult.ofGraph(true, updatedGraph, Instant.now(), metadata);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b> Produces a new graph with the identified relationship removed.</p>
     */
    @Override
    public KnowledgeProcessingResult processUnlink(KnowledgeGraph graph, KnowledgeId relationshipId) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(relationshipId, "relationshipId must not be null");

        List<KnowledgeRelationship> remainingRelationships = graph.getRelationships().stream()
                .filter(r -> !r.getId().equals(relationshipId))
                .collect(Collectors.toList());

        KnowledgeGraph updatedGraph = KnowledgeGraph.of(graph.getNodes(), remainingRelationships);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "processUnlink");
        metadata.put("relationshipId", relationshipId.value());

        return KnowledgeProcessingResult.ofGraph(true, updatedGraph, Instant.now(), metadata);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b> Produces a snapshot result containing an immutable
     * snapshot of the graph at the current point in time.</p>
     */
    @Override
    public KnowledgeProcessingResult processSnapshot(KnowledgeGraph graph, KnowledgeId snapshotId) {
        Objects.requireNonNull(graph, "graph must not be null");
        Objects.requireNonNull(snapshotId, "snapshotId must not be null");

        KnowledgeSnapshot snapshot = KnowledgeSnapshot.of(snapshotId, graph, Instant.now(), "Processed snapshot");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "processSnapshot");
        metadata.put("snapshotId", snapshotId.value());

        return KnowledgeProcessingResult.ofSnapshot(true, snapshot, Instant.now(), metadata);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b> Produces a merged graph combining both input graphs.
     * Nodes and relationships from both graphs are included. Does not perform
     * conflict resolution — that belongs to future kernels.</p>
     */
    @Override
    public KnowledgeProcessingResult processMerge(KnowledgeGraph baseGraph, KnowledgeGraph overlayGraph) {
        Objects.requireNonNull(baseGraph, "baseGraph must not be null");
        Objects.requireNonNull(overlayGraph, "overlayGraph must not be null");

        // Merge nodes (overlay takes precedence for duplicates)
        Map<KnowledgeId, KnowledgeNode> mergedNodes = new HashMap<>();
        for (KnowledgeNode node : baseGraph.getNodes()) {
            mergedNodes.put(node.getId(), node);
        }
        for (KnowledgeNode node : overlayGraph.getNodes()) {
            mergedNodes.put(node.getId(), node);
        }

        // Merge relationships (overlay takes precedence for duplicates)
        Map<KnowledgeId, KnowledgeRelationship> mergedRelationships = new HashMap<>();
        for (KnowledgeRelationship rel : baseGraph.getRelationships()) {
            mergedRelationships.put(rel.getId(), rel);
        }
        for (KnowledgeRelationship rel : overlayGraph.getRelationships()) {
            mergedRelationships.put(rel.getId(), rel);
        }

        KnowledgeGraph mergedGraph = KnowledgeGraph.of(
                List.copyOf(mergedNodes.values()),
                List.copyOf(mergedRelationships.values()));

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "processMerge");
        metadata.put("baseNodeCount", baseGraph.getNodes().size());
        metadata.put("overlayNodeCount", overlayGraph.getNodes().size());

        return KnowledgeProcessingResult.ofGraph(true, mergedGraph, Instant.now(), metadata);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Implementation:</b> Produces a semantically identical but independent
     * copy of the input graph. Uses the graph's immutable factory to create copies.</p>
     */
    @Override
    public KnowledgeProcessingResult processClone(KnowledgeGraph graph) {
        Objects.requireNonNull(graph, "graph must not be null");

        // Create copies via the immutable factory (the factory performs List.copyOf internally)
        KnowledgeGraph clonedGraph = KnowledgeGraph.of(graph.getNodes(), graph.getRelationships());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("operation", "processClone");
        metadata.put("nodeCount", clonedGraph.getNodes().size());
        metadata.put("relationshipCount", clonedGraph.getRelationships().size());

        return KnowledgeProcessingResult.ofGraph(true, clonedGraph, Instant.now(), metadata);
    }
}
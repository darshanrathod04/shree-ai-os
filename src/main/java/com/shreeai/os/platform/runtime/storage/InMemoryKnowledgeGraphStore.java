package com.shreeai.os.platform.runtime.storage;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationship;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <b>InMemoryKnowledgeGraphStore</b>
 *
 * <p>Thread-safe default {@link KnowledgeGraphStore}. Suitable for local
 * development, unit tests, and as the graceful-degradation backend when no
 * graph database is configured. Data is not durable across restarts — durable
 * graph persistence arrives with {@code Neo4jKnowledgeGraphAdapter}.</p>
 *
 * <p><b>Ownership:</b> Runtime — Storage</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class InMemoryKnowledgeGraphStore implements KnowledgeGraphStore {

    private final Map<String, KnowledgeNode> nodes = new ConcurrentHashMap<>();
    private final Map<String, KnowledgeRelationship> relationships = new ConcurrentHashMap<>();

    @Override
    public void saveNode(KnowledgeNode node) {
        Objects.requireNonNull(node, "node must not be null");
        nodes.put(node.getId().value(), node);
    }

    @Override
    public Optional<KnowledgeNode> findNodeById(String nodeId) {
        validate(nodeId, "nodeId");
        return Optional.ofNullable(nodes.get(nodeId));
    }

    @Override
    public List<KnowledgeNode> allNodes() {
        return List.copyOf(nodes.values());
    }

    @Override
    public boolean removeNode(String nodeId) {
        validate(nodeId, "nodeId");
        return nodes.remove(nodeId) != null;
    }

    @Override
    public void saveRelationship(KnowledgeRelationship relationship) {
        Objects.requireNonNull(relationship, "relationship must not be null");
        relationships.put(relationship.getId().value(), relationship);
    }

    @Override
    public List<KnowledgeRelationship> allRelationships() {
        return List.copyOf(relationships.values());
    }

    @Override
    public boolean removeRelationship(String relationshipId) {
        validate(relationshipId, "relationshipId");
        return relationships.remove(relationshipId) != null;
    }

    private void validate(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new StorageRuntimeException(name + " must not be null or blank");
        }
    }
}

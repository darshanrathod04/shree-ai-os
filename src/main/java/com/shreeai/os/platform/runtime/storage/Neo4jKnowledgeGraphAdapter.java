package com.shreeai.os.platform.runtime.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationship;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeRelationshipType;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>Neo4jKnowledgeGraphAdapter</b>
 *
 * <p>Neo4j adapter of {@link KnowledgeGraphStore}. Persists knowledge nodes
 * ({@code (:KnowledgeNode)}) and relationships ({@code (:KnowledgeRelationship)})
 * with MERGE-based upserts. Metadata maps are stored as JSON strings because
 * Neo4j properties must be flat primitive values.</p>
 *
 * <p><b>Design:</b></p>
 * <ul>
 *   <li>Lazy driver initialization — the Neo4j driver is only constructed
 *       when the adapter is selected via configuration and first used.</li>
 *   <li>No provider hard-coding: the knowledge kernel sees only the
 *       {@link KnowledgeGraphStore} SPI.</li>
 *   <li>Session acquisition is delegated to a {@link Neo4jSessionSupplier}
 *       for testability and deployment flexibility.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Storage</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class Neo4jKnowledgeGraphAdapter implements KnowledgeGraphStore {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private static final String MERGE_NODE = """
            MERGE (n:KnowledgeNode {id: $id})
            SET n.type = $type,
                n.state = $state,
                n.scope = $scope,
                n.label = $label,
                n.description = $description,
                n.metadata = $metadata,
                n.createdAt = $createdAt,
                n.updatedAt = $updatedAt
            """;

    private static final String FIND_NODE =
            "MATCH (n:KnowledgeNode {id: $id}) RETURN properties(n) AS props";

    private static final String ALL_NODES =
            "MATCH (n:KnowledgeNode) RETURN properties(n) AS props";

    private static final String DELETE_NODE =
            "MATCH (n:KnowledgeNode {id: $id}) DETACH DELETE n RETURN count(n) > 0 AS removed";

    private static final String MERGE_RELATIONSHIP = """
            MERGE (r:KnowledgeRelationship {id: $id})
            SET r.sourceNodeId = $sourceNodeId,
                r.targetNodeId = $targetNodeId,
                r.type = $type,
                r.label = $label,
                r.metadata = $metadata,
                r.createdAt = $createdAt
            """;

    private static final String ALL_RELATIONSHIPS =
            "MATCH (r:KnowledgeRelationship) RETURN properties(r) AS props";

    private static final String DELETE_RELATIONSHIP =
            "MATCH (r:KnowledgeRelationship {id: $id}) DELETE r RETURN count(r) > 0 AS removed";

    private final Neo4jSessionSupplier sessions;
    private final String uri;
    private final String user;
    private final String password;
    private final Object driverLock = new Object();
    private volatile org.neo4j.driver.Driver lazyDriver;

    /**
     * Creates a Neo4j adapter. The driver is constructed lazily on first use.
     *
     * @param uri      Neo4j URI, e.g. {@code bolt://localhost:7687} (must not be null)
     * @param user     database user (must not be null)
     * @param password database password (must not be null)
     */
    public Neo4jKnowledgeGraphAdapter(String uri, String user, String password) {
        this.uri = Objects.requireNonNull(uri, "uri must not be null");
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
        this.sessions = () -> driver().session(SessionConfig.defaultConfig());
    }

    /**
     * Creates an adapter over a session supplier (testable variant).
     *
     * @param sessions session supplier (must not be null)
     */
    public Neo4jKnowledgeGraphAdapter(Neo4jSessionSupplier sessions) {
        this.sessions = Objects.requireNonNull(sessions, "sessions must not be null");
        this.uri = null;
        this.user = null;
        this.password = null;
    }

    private org.neo4j.driver.Driver driver() {
        org.neo4j.driver.Driver existing = lazyDriver;
        if (existing != null) {
            return existing;
        }
        synchronized (driverLock) {
            if (lazyDriver == null) {
                if (uri == null) {
                    throw new StorageRuntimeException(
                            "Cannot construct a Neo4j driver without uri/user/password configuration");
                }
                lazyDriver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
            }
            return lazyDriver;
        }
    }

    // =====================================================================
    // Node operations
    // =====================================================================

    @Override
    public void saveNode(KnowledgeNode node) {
        Objects.requireNonNull(node, "node must not be null");
        write(MERGE_NODE, Values.parameters(
                "id", node.getId().value(),
                "type", node.getType().name(),
                "state", node.getState().name(),
                "scope", node.getScope().name(),
                "label", node.getLabel(),
                "description", node.getDescription(),
                "metadata", toJson(node.getMetadata()),
                "createdAt", node.getCreatedAt().toEpochMilli(),
                "updatedAt", node.getUpdatedAt().toEpochMilli()));
    }

    @Override
    public Optional<KnowledgeNode> findNodeById(String nodeId) {
        validate(nodeId, "nodeId");
        List<KnowledgeNode> result = readNodes(FIND_NODE, Values.parameters("id", nodeId));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public List<KnowledgeNode> allNodes() {
        return readNodes(ALL_NODES, Values.parameters());
    }

    @Override
    public boolean removeNode(String nodeId) {
        validate(nodeId, "nodeId");
        return readRemoved(DELETE_NODE, Values.parameters("id", nodeId));
    }

    // =====================================================================
    // Relationship operations
    // =====================================================================

    @Override
    public void saveRelationship(KnowledgeRelationship relationship) {
        Objects.requireNonNull(relationship, "relationship must not be null");
        write(MERGE_RELATIONSHIP, Values.parameters(
                "id", relationship.getId().value(),
                "sourceNodeId", relationship.getSourceNodeId().value(),
                "targetNodeId", relationship.getTargetNodeId().value(),
                "type", relationship.getType().name(),
                "label", relationship.getLabel(),
                "metadata", toJson(relationship.getMetadata()),
                "createdAt", relationship.getCreatedAt().toEpochMilli()));
    }

    @Override
    public List<KnowledgeRelationship> allRelationships() {
        List<KnowledgeRelationship> results = new ArrayList<>();
        try (Session session = sessions.get()) {
            List<Record> records = session.executeRead(tx -> tx.run(ALL_RELATIONSHIPS).list());
            for (Record record : records) {
                Map<String, Object> props = recordMap(record);
                results.add(KnowledgeRelationship.of(
                        new KnowledgeId(string(props, "id")),
                        new KnowledgeId(string(props, "sourceNodeId")),
                        new KnowledgeId(string(props, "targetNodeId")),
                        enumValue(KnowledgeRelationshipType.class,
                                string(props, "type"),
                                KnowledgeRelationshipType.RELATED_TO),
                        string(props, "label"),
                        fromJson(string(props, "metadata")),
                        instant(props, "createdAt")));
            }
            return List.copyOf(results);
        } catch (StorageRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageRuntimeException("Neo4j relationship scan failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeRelationship(String relationshipId) {
        validate(relationshipId, "relationshipId");
        return readRemoved(DELETE_RELATIONSHIP, Values.parameters("id", relationshipId));
    }

    // =====================================================================
    // Execution helpers
    // =====================================================================

    private void write(String cypher, Value parameters) {
        try (Session session = sessions.get()) {
            session.executeWrite(tx -> {
                tx.run(cypher, parameters).consume();
                return null;
            });
        } catch (Exception e) {
            throw new StorageRuntimeException("Neo4j write failed: " + e.getMessage(), e);
        }
    }

    private List<KnowledgeNode> readNodes(String cypher, Value parameters) {
        try (Session session = sessions.get()) {
            List<Record> records = session.executeRead(tx -> tx.run(cypher, parameters).list());
            List<KnowledgeNode> nodes = new ArrayList<>();
            for (Record record : records) {
                nodes.add(toNode(recordMap(record)));
            }
            return List.copyOf(nodes);
        } catch (Exception e) {
            throw new StorageRuntimeException("Neo4j node read failed: " + e.getMessage(), e);
        }
    }

    private boolean readRemoved(String cypher, Value parameters) {
        try (Session session = sessions.get()) {
            List<Record> records = session.executeRead(tx -> tx.run(cypher, parameters).list());
            return !records.isEmpty() && records.getFirst().get("removed").asBoolean(false);
        } catch (Exception e) {
            throw new StorageRuntimeException("Neo4j delete failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> recordMap(Record record) {
        Value props = record.get("props");
        Map<String, Object> map = new HashMap<>();
        for (String key : props.keys()) {
            map.put(key, props.get(key).asObject());
        }
        return map;
    }

    // =====================================================================
    // Domain mapping
    // =====================================================================

    private KnowledgeNode toNode(Map<String, Object> props) {
        return KnowledgeNode.of(
                new KnowledgeId(string(props, "id")),
                enumValue(KnowledgeType.class, string(props, "type"), KnowledgeType.CONCEPT),
                enumValue(KnowledgeState.class, string(props, "state"), KnowledgeState.ACTIVE),
                enumValue(KnowledgeScope.class, string(props, "scope"), KnowledgeScope.GLOBAL),
                string(props, "label"),
                string(props, "description"),
                fromJson(string(props, "metadata")),
                instant(props, "createdAt"),
                instant(props, "updatedAt"));
    }

    private static String string(Map<String, Object> props, String key) {
        Object value = props.get(key);
        return value != null ? value.toString() : "";
    }

    private static Instant instant(Map<String, Object> props, String key) {
        Object value = props.get(key);
        if (value instanceof Number number) {
            return Instant.ofEpochMilli(number.longValue());
        }
        return Instant.now();
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String raw, E fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Enum.valueOf(type, raw);
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }

    private String toJson(Map<String, Object> metadata) {
        try {
            return MAPPER.writeValueAsString(metadata == null ? Map.of() : metadata);
        } catch (Exception e) {
            throw new StorageRuntimeException("Failed to serialize metadata: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<String, Object> result = MAPPER.readValue(json, MAP_TYPE);
            return result != null ? result : Map.of();
        } catch (Exception e) {
            return Map.of();
        }
    }

    private void validate(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new StorageRuntimeException(name + " must not be null or blank");
        }
    }
}

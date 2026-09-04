package com.shreeai.os.platform.runtime.storage;

/**
 * <b>KnowledgeGraphStores</b>
 *
 * <p>Configuration-driven selection point for {@link KnowledgeGraphStore}
 * implementations. Reads system properties so no kernel or runtime code ever
 * hard-codes a graph provider:</p>
 *
 * <table border="1">
 *   <caption>Supported properties</caption>
 *   <tr><th>Property</th><th>Meaning</th><th>Default</th></tr>
 *   <tr><td>{@code shree.knowledge.graph.provider}</td><td>{@code in-memory} | {@code neo4j}</td><td>{@code in-memory}</td></tr>
 *   <tr><td>{@code shree.neo4j.uri}</td><td>Neo4j URI (neo4j)</td><td>—</td></tr>
 *   <tr><td>{@code shree.neo4j.user}</td><td>database user</td><td>—</td></tr>
 *   <tr><td>{@code shree.neo4j.password}</td><td>database password</td><td>—</td></tr>
 * </table>
 *
 * <p><b>Ownership:</b> Runtime — Storage</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class KnowledgeGraphStores {

    /** System property selecting the knowledge graph store provider. */
    public static final String PROVIDER_PROPERTY = "shree.knowledge.graph.provider";

    private KnowledgeGraphStores() {
        // static factory
    }

    /**
     * Selects a store from system properties. Falls back to the in-memory
     * store for unknown names or missing Neo4j configuration — the platform
     * must never fail to boot because of storage configuration.
     *
     * @return the selected store (never null)
     */
    public static KnowledgeGraphStore selected() {
        return selected(System.getProperties());
    }

    /**
     * Selects a store from the given property source (testable variant).
     *
     * @param properties property source (must not be null)
     * @return the selected store (never null)
     */
    public static KnowledgeGraphStore selected(java.util.Properties properties) {
        String name = properties.getProperty(PROVIDER_PROPERTY, "in-memory");

        if ("neo4j".equalsIgnoreCase(name)) {
            String uri = properties.getProperty("shree.neo4j.uri");
            String user = properties.getProperty("shree.neo4j.user");
            String password = properties.getProperty("shree.neo4j.password");
            if (uri != null && !uri.isBlank() && user != null && password != null) {
                return new Neo4jKnowledgeGraphAdapter(uri, user, password);
            }
            // Missing configuration → graceful degradation to in-memory.
        }

        return new InMemoryKnowledgeGraphStore();
    }
}

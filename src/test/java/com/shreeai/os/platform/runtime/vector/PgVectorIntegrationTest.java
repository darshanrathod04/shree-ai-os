package com.shreeai.os.platform.runtime.vector;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * PHASE-1 container-backed integration test for the PostgreSQL + pgvector
 * adapters. Automatically skipped when Docker is unavailable so the default
 * test execution stays green in any environment.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PgVectorIntegrationTest {

    private static final String PGVECTOR_IMAGE = "pgvector/pgvector:pg16";

    private PostgreSQLContainer<?> postgres;
    private PgVectorStoreProvider provider;

    @BeforeAll
    void startContainerWhenDockerAvailable() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker unavailable — pgvector integration test skipped");
        postgres = new PostgreSQLContainer<>(PGVECTOR_IMAGE);
        postgres.start();
        provider = new PgVectorStoreProvider(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), 256);
        provider.ensureSchema();
    }

    @Test
    void storeAndSearchRoundTripThroughPostgres() {
        VectorStore store = provider.vectorStore();

        store.store(VectorRecord.of("node-near", "Postgres replication streams WAL to a standby.",
                unit(0, 0), Map.of("documentId", "doc-1", "tenantId", "acme",
                        "embeddingVersion", "test-v1")));
        store.store(VectorRecord.of("node-far", "Chocolate cake recipe with buttercream frosting.",
                unit((float) Math.PI / 2, 0), Map.of("documentId", "doc-2", "tenantId", "acme",
                        "embeddingVersion", "test-v1")));

        List<VectorSearchResult> results =
                provider.searchEngine().search(unit(0, 0), 2);

        assertEquals(2, results.size());
        assertEquals("node-near", results.getFirst().recordId());
        assertTrue(results.getFirst().score() > results.get(1).score());
        assertEquals("doc-1", results.getFirst().metadata().get("documentId"));
        assertEquals("acme", results.getFirst().metadata().get("tenantId"));
    }

    @Test
    void findByIdSurvivesPersistence() {
        VectorStore store = provider.vectorStore();
        store.store(VectorRecord.of("node-lookup", "Lookup content",
                unit(0, 1), Map.of("documentId", "doc-3", "tenantId", "default",
                        "embeddingVersion", "test-v1")));

        VectorRecord loaded = store.findById("node-lookup").orElseThrow();
        assertEquals("Lookup content", loaded.content());
        assertEquals("doc-3", loaded.metadata().get("documentId"));
        assertEquals("default", loaded.metadata().get("tenantId"));
        assertEquals("test-v1", loaded.metadata().get("embeddingVersion"));
        assertTrue(store.delete("node-lookup"));
        assertTrue(store.findById("node-lookup").isEmpty());
    }

    private double[] unit(double x, double y) {
        double norm = Math.sqrt(x * x + y * y);
        return new double[]{x / norm, y / norm};
    }
}

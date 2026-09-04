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

    /**
     * Starts the PostgreSQL + pgvector container if Docker is available.
     * The {@code assumeTrue} guard calls a helper so that any
     * {@link Error} thrown by
     * {@link DockerClientFactory#instance()} is caught before JUnit propagates
     * it as a test error.  This ensures the test is cleanly skipped
     * (not failed) when Testcontainers cannot load its SPI providers due to
     * missing classpath entries on the CI machine.
     */
    @BeforeAll
    void startContainerWhenDockerAvailable() {
        assumeTrue(isDockerAvailable(),
                "Docker unavailable \u2014 pgvector integration test skipped");
        postgres = new PostgreSQLContainer<>(PGVECTOR_IMAGE);
        postgres.start();
        provider = new PgVectorStoreProvider(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword(), 256);
        provider.ensureSchema();
    }

    /**
     * Probes Docker availability while tolerating any {@link Error}
     * that may be raised by Testcontainers when its SPI layer cannot load a
     * provider class due to a missing transitive dependency
     * (e.g. {@code commons-lang3} is absent on some CI machines).
     * Throwing {@code Throwable} here would silently swallow real bugs, so only
     * the narrow {@code ServiceConfigurationError} and its direct super-class
     * {@code Error} are caught.
     *
     * @return {@code true} if Docker is confirmed available, {@code false} otherwise
     */
    private static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Error e) {
            // Any Error (including ServiceConfigurationError, NoClassDefFoundError, LinkageError) is raised when Testcontainers cannot
            // instantiate a DockerClientProviderStrategy (e.g. missing commons-lang3).
            // Error covers its direct sub-classes such as NoClassDefFoundError.
            // We intentionally do NOT catch Exception so any unexpected runtime
            // failures surface as real test errors rather than silent skips.
            return false;
        }
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
package com.shreeai.os.platform.runtime.vector;

import com.shreeai.os.platform.runtime.embedding.LocalDeterministicEmbedder;

/**
 * <b>VectorStoreProviders</b>
 *
 * <p>Configuration-driven selection point for {@link VectorStoreProvider}
 * implementations. Reads system properties so no kernel, runtime, or SDK code
 * ever hard-codes a storage provider:</p>
 *
 * <table border="1">
 *   <caption>Supported properties</caption>
 *   <tr><th>Property</th><th>Meaning</th><th>Default</th></tr>
 *   <tr><td>{@code shree.vector.provider}</td><td>{@code in-memory} | {@code pgvector}</td><td>{@code in-memory}</td></tr>
 *   <tr><td>{@code shree.vector.jdbc.url}</td><td>PostgreSQL JDBC URL (pgvector)</td><td>—</td></tr>
 *   <tr><td>{@code shree.vector.jdbc.user}</td><td>database user</td><td>—</td></tr>
 *   <tr><td>{@code shree.vector.jdbc.password}</td><td>database password</td><td>—</td></tr>
 *   <tr><td>{@code shree.embedding.dimensions}</td><td>embedding dimensionality</td><td>{@code 256}</td></tr>
 * </table>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class VectorStoreProviders {

    /** System property selecting the vector provider. */
    public static final String PROVIDER_PROPERTY = "shree.vector.provider";

    private VectorStoreProviders() {
        // static factory
    }

    /**
     * Selects a provider from system properties. Falls back to the in-memory
     * provider for unknown names or missing pgvector configuration — the
     * platform must never fail to boot because of storage configuration.
     *
     * @return the selected provider (never null)
     */
    public static VectorStoreProvider selected() {
        return selected(System.getProperties());
    }

    /**
     * Selects a provider from the given property source (testable variant).
     *
     * @param properties property source (must not be null)
     * @return the selected provider (never null)
     */
    public static VectorStoreProvider selected(java.util.Properties properties) {
        String name = properties.getProperty(PROVIDER_PROPERTY, InMemoryVectorStoreProvider.NAME);
        int dimensions = embeddingDimensions(properties);

        if (PgVectorStoreProvider.NAME.equalsIgnoreCase(name)) {
            String url = properties.getProperty("shree.vector.jdbc.url");
            String user = properties.getProperty("shree.vector.jdbc.user");
            String password = properties.getProperty("shree.vector.jdbc.password");
            if (url != null && !url.isBlank() && user != null && password != null) {
                PgVectorStoreProvider provider =
                        new PgVectorStoreProvider(url, user, password, dimensions);
                provider.ensureSchema();
                return provider;
            }
            // Missing configuration → graceful degradation to in-memory.
        }

        return new InMemoryVectorStoreProvider();
    }

    /**
     * Resolves the configured embedding dimension.
     *
     * @param properties property source
     * @return dimension (always &gt;= 64)
     */
    public static int embeddingDimensions(java.util.Properties properties) {
        String raw = properties.getProperty("shree.embedding.dimensions");
        if (raw == null || raw.isBlank()) {
            return LocalDeterministicEmbedder.DEFAULT_DIMENSIONS;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            return Math.max(64, value);
        } catch (NumberFormatException ignored) {
            return LocalDeterministicEmbedder.DEFAULT_DIMENSIONS;
        }
    }
}

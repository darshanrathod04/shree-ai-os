package com.shreeai.os.platform.runtime.embedding;

import java.util.Arrays;
import java.util.Objects;

/**
 * <b>EmbeddedVector</b>
 *
 * <p>Immutable value object persisted by an {@link EmbeddingRepository}:
 * an embedding vector together with the {@code embeddingVersion} of the
 * provider that produced it (metadata-first schema).</p>
 *
 * <p><b>Ownership:</b> Runtime — Embedding</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class EmbeddedVector {

    private final double[] embedding;
    private final String embeddingVersion;

    private EmbeddedVector(double[] embedding, String embeddingVersion) {
        this.embedding = embedding;
        this.embeddingVersion = embeddingVersion;
    }

    /**
     * Creates a new embedded vector value.
     *
     * @param embedding        the embedding vector (must not be null)
     * @param embeddingVersion the provider version that produced it (must not be null)
     * @return a new immutable instance
     */
    public static EmbeddedVector of(double[] embedding, String embeddingVersion) {
        Objects.requireNonNull(embedding, "embedding must not be null");
        Objects.requireNonNull(embeddingVersion, "embeddingVersion must not be null");
        return new EmbeddedVector(Arrays.copyOf(embedding, embedding.length), embeddingVersion);
    }

    /**
     * Returns a defensive copy of the embedding vector.
     *
     * @return the embedding vector (never null)
     */
    public double[] embedding() {
        return Arrays.copyOf(embedding, embedding.length);
    }

    /**
     * Returns the embedding version that produced this vector.
     *
     * @return the embedding version (never null)
     */
    public String embeddingVersion() {
        return embeddingVersion;
    }
}

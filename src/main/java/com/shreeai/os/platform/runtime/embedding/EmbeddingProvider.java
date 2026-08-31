package com.shreeai.os.platform.runtime.embedding;

/**
 * <b>EmbeddingProvider</b>
 *
 * <p>SPI for computing semantic embeddings of text. This is the canonical,
 * provider-neutral embedding port of the platform. Kernels depend on this
 * interface only — concrete providers (local deterministic, remote API-backed)
 * are selected via configuration and injected by the composition root.</p>
 *
 * <p><b>Contract:</b></p>
 * <ul>
 *   <li>Implementations MUST be deterministic for a given {@link #version()}:
 *       the same text always produces the same vector.</li>
 *   <li>Every produced vector MUST have exactly {@link #dimensions()}
 *       components.</li>
 *   <li>Returned vectors SHOULD be L2-normalized so that cosine similarity
 *       reduces to a dot product.</li>
 *   <li>Implementations MUST be thread-safe.</li>
 *   <li>Changing the algorithm or dimension MUST be accompanied by a new
 *       {@link #version()} string so stored vectors can be invalidated
 *       ({@code embeddingVersion} metadata-first schema).</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Embedding</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public interface EmbeddingProvider {

    /**
     * Computes the embedding of the given text.
     *
     * @param text the text to embed (may be null or blank; implementations
     *             MUST return a zero vector in that case rather than throwing)
     * @return an embedding vector with exactly {@link #dimensions()} components
     */
    double[] embed(String text);

    /**
     * Returns the fixed dimensionality of all vectors produced by this provider.
     *
     * @return the embedding dimension (always &gt; 0)
     */
    int dimensions();

    /**
     * Returns the stable version identifier of this embedding implementation.
     *
     * @return the embedding version (never null or blank)
     */
    String version();
}

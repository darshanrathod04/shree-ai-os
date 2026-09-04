package com.shreeai.os.platform.runtime.embedding;

import java.util.Locale;

/**
 * <b>LocalDeterministicEmbedder</b>
 *
 * <p>Production-capable default {@link EmbeddingProvider} requiring no external
 * service. Produces stable, L2-normalized dense vectors from hashed lexical
 * features (unigrams and character trigrams), giving strong similarity for
 * lexically related texts and reasonable behavior for paraphrases.</p>
 *
 * <p><b>Properties:</b></p>
 * <ul>
 *   <li>Deterministic across JVMs (FNV-1a 32-bit feature hashing).</li>
 *   <li>Fixed dimension (default 256), L2-normalized output.</li>
 *   <li>Zero configuration, zero network dependency — the safe platform default.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Runtime — Embedding</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class LocalDeterministicEmbedder implements EmbeddingProvider {

    /** Stable version identifier — bump when the feature scheme changes. */
    public static final String VERSION = "local-deterministic-v1";

    /** Default embedding dimension. */
    public static final int DEFAULT_DIMENSIONS = 256;

    private static final int FNV_OFFSET_BASIS = 0x811c9dc5;
    private static final int FNV_PRIME = 0x01000193;

    private final int dimensions;

    /**
     * Creates an embedder with the default dimension of 256.
     */
    public LocalDeterministicEmbedder() {
        this(DEFAULT_DIMENSIONS);
    }

    /**
     * Creates an embedder with a custom dimension.
     *
     * @param dimensions the embedding dimension (must be &gt;= 64)
     */
    public LocalDeterministicEmbedder(int dimensions) {
        if (dimensions < 64) {
            throw new IllegalArgumentException("dimensions must be >= 64");
        }
        this.dimensions = dimensions;
    }

    @Override
    public double[] embed(String text) {
        double[] vector = new double[dimensions];
        if (text == null || text.isBlank()) {
            return vector;
        }

        String normalized = normalize(text);
        if (normalized.isEmpty()) {
            return vector;
        }

        // Feature 1: word unigrams (weight 1.0)
        for (String token : normalized.split(" ")) {
            if (!token.isBlank()) {
                addFeature(vector, token, 1.0);
            }
        }

        // Feature 2: character trigrams over the compacted text (weight 0.6)
        String compact = normalized.replace(" ", "");
        for (int i = 0; i + 3 <= compact.length(); i++) {
            addFeature(vector, compact.substring(i, i + 3), 0.6);
        }

        normalizeL2(vector);
        return vector;
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public String version() {
        return VERSION;
    }

    private void addFeature(double[] vector, String feature, double weight) {
        int hash = fnv1a(feature);
        int index = (hash & 0x7fffffff) % dimensions;
        // Signed hashing reduces collision bias between unrelated features.
        double sign = (hash & 0x80000000) == 0 ? 1.0 : -1.0;
        vector[index] += sign * weight;
    }

    private int fnv1a(String feature) {
        int hash = FNV_OFFSET_BASIS;
        for (int i = 0; i < feature.length(); i++) {
            hash ^= feature.charAt(i);
            hash *= FNV_PRIME;
        }
        return hash;
    }

    private void normalizeL2(double[] vector) {
        double sumSquares = 0.0;
        for (double v : vector) {
            sumSquares += v * v;
        }
        if (sumSquares <= 0.0) {
            return;
        }
        double norm = Math.sqrt(sumSquares);
        for (int i = 0; i < vector.length; i++) {
            vector[i] /= norm;
        }
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}

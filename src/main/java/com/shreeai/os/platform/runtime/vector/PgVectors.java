package com.shreeai.os.platform.runtime.vector;

/**
 * <b>PgVectors</b>
 *
 * <p>Conversion helpers between Java {@code double[]} embeddings and the
 * pgvector literal text format ({@code "[1.0,2.0,...]"}). Centralising the
 * encoding guarantees every PgVector adapter writes vectors identically.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class PgVectors {

    private PgVectors() {
        // static utility
    }

    /**
     * Encodes an embedding as a pgvector literal usable with the
     * {@code ?::vector} cast.
     *
     * @param embedding the embedding (must not be null or empty)
     * @return the pgvector literal (never null)
     */
    public static String toPgVectorLiteral(double[] embedding) {
        if (embedding == null || embedding.length == 0) {
            throw new VectorRuntimeException("embedding must not be null or empty");
        }
        StringBuilder builder = new StringBuilder(embedding.length * 10);
        builder.append('[');
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(embedding[i]);
        }
        builder.append(']');
        return builder.toString();
    }

    /**
     * Decodes a pgvector literal (as returned by {@code embedding::text}).
     *
     * @param literal the pgvector literal (must not be null or blank)
     * @return the embedding vector (never null)
     */
    public static double[] fromPgVectorLiteral(String literal) {
        if (literal == null || literal.isBlank()) {
            throw new VectorRuntimeException("pgvector literal must not be null or blank");
        }
        String trimmed = literal.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if (trimmed.isBlank()) {
            return new double[0];
        }
        String[] parts = trimmed.split(",");
        double[] vector = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Double.parseDouble(parts[i].trim());
        }
        return vector;
    }
}

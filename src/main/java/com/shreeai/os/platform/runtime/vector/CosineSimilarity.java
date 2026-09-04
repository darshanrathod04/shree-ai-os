package com.shreeai.os.platform.runtime.vector;

/**
 * <b>CosineSimilarity</b>
 *
 * <p>The single canonical cosine similarity implementation of the platform.
 * All similarity computation — in-memory search engines, semantic grounding,
 * evaluation tests — MUST delegate here; duplicated math is forbidden.</p>
 *
 * <p><b>Ownership:</b> Runtime — Vector</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class CosineSimilarity {

    private static final double EPSILON = 1e-9;

    private CosineSimilarity() {
        // static utility
    }

    /**
     * Computes the cosine similarity between two vectors.
     *
     * <p>The computation rescales both vectors by their maximum absolute
     * component (cosine is scale-invariant), which makes the result safe
     * against overflow for very large magnitudes and underflow for very
     * small ones.</p>
     *
     * @param a first vector (must not be null)
     * @param b second vector (must not be null)
     * @return similarity in [-1.0, 1.0]; 0.0 when dimensions differ or a
     *         vector has zero magnitude
     */
    public static double of(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) {
            return 0.0;
        }

        double maxA = maxAbs(a);
        double maxB = maxAbs(b);
        if (maxA == 0.0 || maxB == 0.0) {
            return 0.0;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            double x = a[i] / maxA;
            double y = b[i] / maxB;
            dot += x * y;
            normA += x * x;
            normB += y * y;
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        if (denominator <= EPSILON || !Double.isFinite(denominator)) {
            return 0.0;
        }

        double similarity = dot / denominator;
        if (!Double.isFinite(similarity)) {
            return 0.0;
        }
        return Math.max(-1.0, Math.min(1.0, similarity));
    }

    private static double maxAbs(double[] vector) {
        double max = 0.0;
        for (double v : vector) {
            double abs = Math.abs(v);
            if (abs > max) {
                max = abs;
            }
        }
        return max;
    }
}

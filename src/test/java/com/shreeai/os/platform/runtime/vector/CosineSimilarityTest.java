package com.shreeai.os.platform.runtime.vector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the canonical {@link CosineSimilarity} implementation.
 */
class CosineSimilarityTest {

    @Test
    void identicalVectorsHaveSimilarityOne() {
        double[] a = {0.3, -0.5, 0.8};
        assertEquals(1.0, CosineSimilarity.of(a, a), 1e-9);
    }

    @Test
    void orthogonalVectorsHaveZeroSimilarity() {
        assertEquals(0.0, CosineSimilarity.of(new double[]{1, 0}, new double[]{0, 1}), 1e-9);
    }

    @Test
    void oppositeVectorsHaveNegativeSimilarity() {
        double similarity = CosineSimilarity.of(new double[]{1, 2, 3}, new double[]{-1, -2, -3});
        assertEquals(-1.0, similarity, 1e-9);
    }

    @Test
    void differentDimensionsReturnZero() {
        assertEquals(0.0, CosineSimilarity.of(new double[]{1, 0}, new double[]{1, 0, 0}));
    }

    @Test
    void zeroVectorReturnsZero() {
        assertEquals(0.0, CosineSimilarity.of(new double[]{0, 0}, new double[]{1, 1}));
    }

    @Test
    void nullVectorsReturnZero() {
        assertEquals(0.0, CosineSimilarity.of(null, new double[]{1}));
        assertEquals(0.0, CosineSimilarity.of(new double[]{1}, null));
    }

    @Test
    void resultIsAlwaysWithinUnitRange() {
        double[] a = {1e300, 1e300};
        double[] b = {1e300, 1e300};
        double similarity = CosineSimilarity.of(a, b);
        assertTrue(similarity <= 1.0 && similarity >= -1.0);
    }
}

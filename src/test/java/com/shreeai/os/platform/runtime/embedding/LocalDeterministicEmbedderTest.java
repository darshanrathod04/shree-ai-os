package com.shreeai.os.platform.runtime.embedding;

import com.shreeai.os.platform.runtime.vector.CosineSimilarity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the default {@link LocalDeterministicEmbedder}.
 */
class LocalDeterministicEmbedderTest {

    private final LocalDeterministicEmbedder embedder = new LocalDeterministicEmbedder();

    @Test
    void dimensionContractIsFixed() {
        assertEquals(256, embedder.dimensions());
        assertEquals(128, new LocalDeterministicEmbedder(128).dimensions());
    }

    @Test
    void versionIsStableAndNonBlank() {
        assertEquals(LocalDeterministicEmbedder.VERSION, embedder.version());
        assertTrue(!embedder.version().isBlank());
    }

    @Test
    void sameTextProducesIdenticalVector() {
        assertArrayEquals(
                embedder.embed("Postgres replication"),
                embedder.embed("Postgres replication"));
    }

    @Test
    void nullAndBlankProduceZeroVectors() {
        assertEquals(256, embedder.embed(null).length);
        assertEquals(256, embedder.embed("").length);
        assertEquals(256, embedder.embed("   ").length);
    }

    @Test
    void vectorsAreL2Normalized() {
        double[] vector = embedder.embed("Vertical scaling adds more CPU and memory");
        double sumSquares = 0.0;
        for (double v : vector) {
            sumSquares += v * v;
        }
        assertEquals(1.0, Math.sqrt(sumSquares), 1e-9);
    }

    @Test
    void similarTextIsCloserThanUnrelatedText() {
        double[] base = embedder.embed("Postgres database replication with streaming");
        double[] similar = embedder.embed("Postgres database replication using streaming");
        double[] unrelated = embedder.embed("Chocolate cake recipe with buttercream frosting");

        double similarScore = CosineSimilarity.of(base, similar);
        double unrelatedScore = CosineSimilarity.of(base, unrelated);

        assertTrue(similarScore > unrelatedScore,
                "expected similarScore=" + similarScore + " > unrelatedScore=" + unrelatedScore);
    }

    @Test
    void rejectsTooSmallDimension() {
        assertThrows(IllegalArgumentException.class, () -> new LocalDeterministicEmbedder(16));
    }
}

package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.runtime.embedding.OnnxEmbeddingProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies Phase 2 Step 2.5:
 * - OnnxEmbeddingProvider loads real all-MiniLM-L6-v2 ONNX model (isDegraded() == false)
 * - Semantic cosine similarity distinguishes related vs unrelated technical concepts.
 */
class RuntimeWiringStep2Part3VerificationTest {

    @Test
    @DisplayName("Step 2.5: OnnxEmbeddingProvider runs real 384-dim semantic ONNX inference")
    void testOnnxEmbeddingProviderSemanticQuality() {
        OnnxEmbeddingProvider provider = new OnnxEmbeddingProvider();

        double[] vApi = provider.embed("Spring Boot REST API controller");
        double[] vJava = provider.embed("Java web endpoint service");
        double[] vCake = provider.embed("chocolate birthday cake baking recipe");

        assertFalse(provider.isDegraded(),
                "ONNX provider should not be degraded; reason=" + provider.degradationReason());
        assertNull(provider.degradationReason());
        assertEquals(384, vApi.length);

        double simRelated = dot(vApi, vJava);
        double simUnrelated = dot(vApi, vCake);

        assertTrue(simRelated > simUnrelated + 0.15,
                "Expected related cosine similarity (" + simRelated
                        + ") to exceed unrelated (" + simUnrelated + ") by >= 0.15");
    }

    private static double dot(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }
}

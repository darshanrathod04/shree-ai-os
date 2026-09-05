package com.shreeai.os.platform.runtime.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * ONNX-based sentence-embedding provider backed by the all-MiniLM-L6-v2 model.
 *
 * <p>All instances share a single ONNX session via a static holder to avoid
 * loading the ~90 MB model multiple times and exhausting native memory.  The
 * session is created once, on first use, and reused for every subsequent
 * {@code OnnxEmbeddingProvider} instance (including across test-method
 * re-initialisations that each call {@code ShreeAI.builder().build()}).
 */
public class OnnxEmbeddingProvider implements EmbeddingProvider {

    private static final int DIMENSIONS = 384;
    private static final String VERSION = "onnx-all-minilm-l6-v2-384d-v1";

    // Singleton holder: one environment + one session for all callers.
    // Using a static initializer keeps the cost at "pay once per JVM lifetime".
    private static final OnnxSessionHolder SESSION_HOLDER = new OnnxSessionHolder();

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final HuggingFaceTokenizer tokenizer;

    public OnnxEmbeddingProvider() {
        try {
            OnnxSessionHolder.SharedResources resources = SESSION_HOLDER.getOrCreate();
            this.environment = resources.environment;
            this.session = resources.session;
            this.tokenizer = resources.tokenizer;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize ONNX Embedding Provider", e);
        }
    }

    @Override
    public double[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new double[DIMENSIONS];
        }

        try {
            Encoding encoding = tokenizer.encode(text);
            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] typeIds = encoding.getTypeIds();

            long[] shape = new long[]{1, inputIds.length};

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", OnnxTensor.createTensor(environment, LongBuffer.wrap(inputIds), shape));
            inputs.put("attention_mask", OnnxTensor.createTensor(environment, LongBuffer.wrap(attentionMask), shape));
            inputs.put("token_type_ids", OnnxTensor.createTensor(environment, LongBuffer.wrap(typeIds), shape));

            try (OrtSession.Result results = session.run(inputs)) {
                float[][][] output = (float[][][]) results.get(0).getValue();
                return meanPoolingAndNormalize(output[0], attentionMask);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating ONNX embedding", e);
        }
    }

    private double[] meanPoolingAndNormalize(float[][] tokenEmbeddings, long[] attentionMask) {
        double[] sum = new double[DIMENSIONS];
        double tokenCount = 0.0;

        for (int i = 0; i < tokenEmbeddings.length; i++) {
            if (attentionMask[i] == 1) {
                tokenCount += 1.0;
                for (int d = 0; d < DIMENSIONS; d++) {
                    sum[d] += tokenEmbeddings[i][d];
                }
            }
        }

        if (tokenCount == 0.0) tokenCount = 1.0;

        // Mean pooling
        double norm = 0.0;
        for (int d = 0; d < DIMENSIONS; d++) {
            sum[d] /= tokenCount;
            norm += sum[d] * sum[d];
        }

        // L2 Normalization (so cosine similarity reduces to dot product)
        norm = Math.sqrt(norm);
        if (norm > 0.0) {
            for (int d = 0; d < DIMENSIONS; d++) {
                sum[d] /= norm;
            }
        }

        return sum;
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    @Override
    public String version() {
        return VERSION;
    }

    // -------------------------------------------------------------------------
    // Package-visible holder that maintains the shared ONNX resources.
    // Thread-safe via class-init semantics.
    // -------------------------------------------------------------------------
    static final class OnnxSessionHolder {

        // Lazily constructed and cached for the life of the JVM.
        private volatile SharedResources resources;

        SharedResources getOrCreate() throws Exception {
            // Double-checked locking with volatile read
            if (resources == null) {
                synchronized (this) {
                    if (resources == null) {
                        resources = new SharedResources();
                    }
                }
            }
            return resources;
        }

        private static final class SharedResources {
            final OrtEnvironment environment;
            final OrtSession session;
            final HuggingFaceTokenizer tokenizer;

            SharedResources() throws Exception {
                // Extract the model and tokenizer to the stable target/ directory (not a
                // random system temp file).  This avoids:
                //   (a) "not enough space on the disk" failures when the system temp is full
                //   (b) re-extraction on every test run (the files persist between runs)
                //   (c) "delete on exit" races when multiple test forks run concurrently
                Path cacheDir = ensureCacheDir();
                Path modelPath = extractTo(cacheDir, "/models/all-MiniLM-L6-v2/model.onnx", "onnx-model");
                Path tokenizerPath = extractTo(cacheDir, "/models/all-MiniLM-L6-v2/tokenizer.json", "onnx-tokenizer");

                this.environment = OrtEnvironment.getEnvironment();
                this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
                this.session = environment.createSession(modelPath.toString(), new OrtSession.SessionOptions());
            }
        }
    }

    /**
     * Returns the stable cache directory for extracted ONNX assets.
     * Uses the {@code target/shree-onnx-cache/} directory so that:
     * <ul>
     *   <li>Files persist across test runs and JVM restarts</li>
     *   <li>The directory is cleaned automatically by {@code mvn clean}</li>
     *   <li>No random temp-file name churn or {@code deleteOnExit} leaks</li>
     * </ul>
     */
    private static Path ensureCacheDir() throws IOException {
        // Resolve relative to user.dir so it works in both mvn and IDE.
        Path base = Path.of(System.getProperty("user.dir"));
        Path cacheDir = base.resolve("target").resolve("shree-onnx-cache");
        if (!Files.exists(cacheDir)) {
            Files.createDirectories(cacheDir);
        }
        return cacheDir;
    }

    /**
     * Extracts a classpath resource to the cache directory.
     * If the file already exists with a non-zero size it is reused.
     */
    private static Path extractTo(Path cacheDir, String resourcePath, String prefix) throws IOException {
        String fileName = Path.of(resourcePath).getFileName().toString();
        Path target = cacheDir.resolve(fileName);
        if (Files.exists(target) && Files.size(target) > 0) {
            return target;
        }
        try (InputStream is = OnnxEmbeddingProvider.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found on classpath: " + resourcePath);
            }
            Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }
}

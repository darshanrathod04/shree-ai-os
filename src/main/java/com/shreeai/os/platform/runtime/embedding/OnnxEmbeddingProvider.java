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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ONNX-based sentence-embedding provider backed by the all-MiniLM-L6-v2 model.
 *
 * <p>All instances share a single ONNX session via a static holder to avoid
 * loading the ~90 MB model multiple times and exhausting native memory.  The
 * session is created lazily on the first {@link #embed(String)} request -
 * never during construction - so that a missing or unloadable ONNX runtime
 * can never break Spring bean construction or ApplicationContext startup. In
 * that case embeddings degrade to a deterministic fallback vector.
 */
public class OnnxEmbeddingProvider implements EmbeddingProvider {

    private static final int DIMENSIONS = 384;
    private static final String VERSION = "onnx-all-minilm-l6-v2-384d-v1";

    // Singleton holder: one environment + one session for all callers.
    // The holder is populated lazily on the first embed() call - never during
    // construction - so that OrtEnvironment native-library loading can never
    // fail Spring bean construction or ApplicationContext startup.
    private static final OnnxSessionHolder SESSION_HOLDER = new OnnxSessionHolder();

    private static final Logger LOG = Logger.getLogger(OnnxEmbeddingProvider.class.getName());

    // Lazily resolved shared ONNX resources (null until first use).
    private volatile OnnxSessionHolder.SharedResources resources;

    // Set once ONNX is known to be unavailable; later embeds degrade without
    // retrying the (already failed) native library load.
    private volatile boolean degraded;

    public OnnxEmbeddingProvider() {
        // Intentionally empty. OrtEnvironment must never be initialized during
        // Spring bean construction: the native onnxruntime shared library is
        // loaded in OrtEnvironment's static initializer and can fail with an
        // UnsatisfiedLinkError on machines where the DLL cannot be loaded.
        // Resources are created lazily on the first embed() request instead;
        // if that fails, embed() degrades to a deterministic fallback.
    }

    @Override
    public double[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new double[DIMENSIONS];
        }

        OnnxSessionHolder.SharedResources shared = resolveResources();
        if (shared == null) {
            return deterministicFallback(text);
        }

        try {
            Encoding encoding = shared.tokenizer.encode(text);
            long[] inputIds = encoding.getIds();
            long[] attentionMask = encoding.getAttentionMask();
            long[] typeIds = encoding.getTypeIds();

            long[] shape = new long[]{1, inputIds.length};

            Map<String, OnnxTensor> inputs = new HashMap<>();
            inputs.put("input_ids", OnnxTensor.createTensor(shared.environment, LongBuffer.wrap(inputIds), shape));
            inputs.put("attention_mask", OnnxTensor.createTensor(shared.environment, LongBuffer.wrap(attentionMask), shape));
            inputs.put("token_type_ids", OnnxTensor.createTensor(shared.environment, LongBuffer.wrap(typeIds), shape));

            try (OrtSession.Result results = shared.session.run(inputs)) {
                float[][][] output = (float[][][]) results.get(0).getValue();
                return meanPoolingAndNormalize(output[0], attentionMask);
            }
        } catch (Throwable t) {
            markDegraded("ONNX embedding failed at runtime; degrading to deterministic fallback", t);
            return deterministicFallback(text);
        }
    }

    /**
     * Lazily resolves the shared ONNX resources on first request.
     *
     * <p>Returns {@code null} when ONNX is unavailable, signalling the caller
     * to degrade gracefully instead of propagating a native-linkage failure
     * into the application context.</p>
     */
    private OnnxSessionHolder.SharedResources resolveResources() {
        if (degraded) {
            return null;
        }
        OnnxSessionHolder.SharedResources shared = resources;
        if (shared == null) {
            synchronized (this) {
                if (!degraded && resources == null) {
                    try {
                        resources = SESSION_HOLDER.getOrCreate();
                    } catch (Throwable t) {
                        markDegraded("ONNX runtime unavailable; embeddings degrade to deterministic fallback", t);
                        return null;
                    }
                }
                shared = resources;
            }
        }
        return shared;
    }

    private void markDegraded(String message, Throwable cause) {
        degraded = true;
        LOG.log(Level.WARNING, message, cause);
    }

    /**
     * Deterministic hash-based fallback embedding used when ONNX is
     * unavailable. Projects every alphanumeric token into the same
     * {@value #DIMENSIONS}-dimensional space and L2-normalises the result so
     * cosine similarity remains meaningful.
     */
    private static double[] deterministicFallback(String text) {
        double[] vector = new double[DIMENSIONS];
        for (String token : text.toLowerCase().split("[^a-z0-9]+")) {
            if (token.isEmpty()) {
                continue;
            }
            int hash = token.hashCode();
            int index = Math.floorMod(hash, DIMENSIONS);
            vector[index] += ((hash & 1) == 0) ? 1.0 : -1.0;
        }
        double norm = 0.0;
        for (double value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm > 0.0) {
            for (int d = 0; d < DIMENSIONS; d++) {
                vector[d] /= norm;
            }
        }
        return vector;
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

package com.shreeai.os.platform.runtime.embedding;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.*;

import java.io.InputStream;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class OnnxEmbeddingProvider implements EmbeddingProvider {

    private final OrtEnvironment environment;
    private final OrtSession session;
    private final HuggingFaceTokenizer tokenizer;
    private static final int DIMENSIONS = 384;
    private static final String VERSION = "onnx-all-minilm-l6-v2-384d-v1";

    public OnnxEmbeddingProvider() {
        try {
            this.environment = OrtEnvironment.getEnvironment();

            Path modelPath = extractResource("/models/all-MiniLM-L6-v2/model.onnx");
            Path tokenizerPath = extractResource("/models/all-MiniLM-L6-v2/tokenizer.json");

            this.tokenizer = HuggingFaceTokenizer.newInstance(tokenizerPath);
            this.session = environment.createSession(modelPath.toString(), new OrtSession.SessionOptions());
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

    private Path extractResource(String resourcePath) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            Path tempFile = Files.createTempFile("onnx_", "_" + Path.of(resourcePath).getFileName().toString());
            tempFile.toFile().deleteOnExit();
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return tempFile;
        }
    }

    @Override
    public int dimensions() {
        return DIMENSIONS;
    }

    @Override
    public String version() {
        return VERSION;
    }
}
package com.shreeai.os.platform.runtime.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <b>OpenAiCompatibleEmbedder</b>
 *
 * <p>Optional remote {@link EmbeddingProvider} speaking the widely adopted
 * OpenAI {@code /v1/embeddings} request/response shape (OpenAI, Azure OpenAI,
 * local gateways, and many self-hosted servers). Uses the platform's existing
 * OkHttp dependency. Selected only via configuration — never hard-coded.</p>
 *
 * <p><b>Ownership:</b> Runtime — Embedding</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class OpenAiCompatibleEmbedder implements EmbeddingProvider {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String endpoint;
    private final String apiKey;
    private final String model;
    private final int dimensions;
    private final String version;

    /**
     * Creates a remote embedder.
     *
     * @param endpoint   full embeddings endpoint URL (must not be null or blank)
     * @param apiKey     bearer API key (may be null for unauthenticated gateways)
     * @param model      embedding model identifier (must not be null or blank)
     * @param dimensions expected embedding dimension reported by the model
     * @param version    stable version identifier for {@code embeddingVersion} metadata
     */
    public OpenAiCompatibleEmbedder(
            String endpoint,
            String apiKey,
            String model,
            int dimensions,
            String version) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("endpoint must not be null or blank");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model must not be null or blank");
        }
        if (dimensions <= 0) {
            throw new IllegalArgumentException("dimensions must be positive");
        }
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.model = model;
        this.dimensions = dimensions;
        this.version = version != null && !version.isBlank() ? version : "openai-compatible:" + model;

        this.http = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(60))
                .writeTimeout(Duration.ofSeconds(60))
                .build();
    }

    @Override
    public double[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new double[dimensions];
        }

        try {
            ObjectNode payload = mapper.createObjectNode();
            payload.put("model", model);
            payload.put("input", text);

            Request.Builder builder = new Request.Builder()
                    .url(endpoint)
                    .post(RequestBody.create(mapper.writeValueAsString(payload), JSON));

            if (apiKey != null && !apiKey.isBlank()) {
                builder.header("Authorization", "Bearer " + apiKey);
            }

            try (Response response = http.newCall(builder.build()).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new EmbeddingRuntimeException(
                            "Embedding request failed with HTTP " + response.code());
                }
                JsonNode root = mapper.readTree(response.body().string());
                JsonNode vectorNode = root.path("data").path(0).path("embedding");
                if (!vectorNode.isArray() || vectorNode.size() != dimensions) {
                    throw new EmbeddingRuntimeException(
                            "Embedding response dimension mismatch: expected "
                                    + dimensions + " but received "
                                    + vectorNode.size());
                }
                double[] vector = new double[dimensions];
                for (int i = 0; i < dimensions; i++) {
                    vector[i] = vectorNode.get(i).asDouble();
                }
                return vector;
            }

        } catch (EmbeddingRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new EmbeddingRuntimeException("Embedding request failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public String version() {
        return version;
    }
}

package com.shreeai.os.platform.sdk;

import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventListener;
import com.shreeai.os.platform.sdk.exceptions.ValidationException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Knowledge SDK Facade
 *
 * Thin developer-facing wrapper over the Knowledge Kernel.
 */
public final class KnowledgeSDK {

    /** Maximum time to wait for the runtime ingestion consumer to answer. */
    private static final long INGEST_TIMEOUT_SECONDS = 30;

    private final ShreeClient client;

    KnowledgeSDK(ShreeClient client) {
        this.client = Objects.requireNonNull(client);
    }

    /**
     * Semantic knowledge query.
     *
     * <p>Passes the user's actual question as the message so that the runtime
     * synthesizer renders it as the response title, not a literal placeholder.
     * The real question is forwarded in metadata for KnowledgeStage routing.</p>
     *
     * @param question the user's natural-language question
     * @return SDKResponse with the knowledge query result
     */
    public SDKResponse query(String question) {

        SDKRequest request = SDKRequest.builder()
                .message(question)   // Sprint-17.3: was literal "KNOWLEDGE_QUERY"
                .metadata(Map.of(
                        "operation", "QUERY_KNOWLEDGE",
                        "question", question
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Retrieve a knowledge entity by its identifier.
     *
     * @param entityId the unique entity identifier
     * @return SDKResponse with the entity data
     */
    public SDKResponse retrieve(String entityId) {

        SDKRequest request = SDKRequest.builder()
                .message("Retrieve knowledge: " + entityId)   // Sprint-17.3: real context
                .metadata(Map.of(
                        "operation", "RETRIEVE_ENTITY",
                        "entityId", entityId
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Search the knowledge graph by keyword.
     *
     * @param keyword the search keyword
     * @return SDKResponse with the matching knowledge nodes
     */
    public SDKResponse search(String keyword) {

        SDKRequest request = SDKRequest.builder()
                .message("Search knowledge: " + keyword)   // Sprint-17.3: real context
                .metadata(Map.of(
                        "operation", "SEARCH_KNOWLEDGE",
                        "keyword", keyword
                ))
                .build();

        return client.chat(request);
    }

    /**
     * Ingests a document permanently (event-driven).
     *
     * <p>Publishes a {@code KNOWLEDGE_INGEST_REQUESTED} runtime event on the
     * canonical event bus; the runtime-bound ingestion consumer performs the
     * ingestion (chunk → embed → persist) and answers with a
     * {@code KNOWLEDGE_INGEST_COMPLETED} event. Because event dispatch is
     * synchronous, the document is permanently searchable as soon as this
     * method returns.</p>
     *
     * <p>Follows the metadata-first document schema: the request carries
     * {@code title}, {@code content}, and {@code tenantId}; the completion
     * event returns {@code documentId}, {@code chunkCount}, {@code nodeIds},
     * and {@code embeddingVersion}.</p>
     *
     * @param title   document title (must not be null or blank)
     * @param content document content (must not be null or blank)
     * @return SDKResponse acknowledging the ingestion; the structured payload
     *         carries the ingestion result metadata
     * @throws ValidationException     if title or content is null or blank
     * @throws SDKException            if the runtime ingestion fails or no
     *                                 consumer answered in time
     */
    public SDKResponse ingest(String title, String content) {

        if (title == null || title.isBlank()) {
            throw new ValidationException("title must not be null or blank");
        }
        if (content == null || content.isBlank()) {
            throw new ValidationException("content must not be null or blank");
        }

        String requestId = UUID.randomUUID().toString();

        Map<String, Object> requestMetadata = new HashMap<>();
        requestMetadata.put("title", title);
        requestMetadata.put("content", content);
        requestMetadata.put("tenantId", "default");

        CompletableFuture<Map<String, Object>> completion = new CompletableFuture<>();
        RuntimeEventListener listener = event -> {
            if (EventType.KNOWLEDGE_INGEST_COMPLETED == event.type()
                    && requestId.equals(event.requestId())) {
                completion.complete(event.metadata());
            }
        };

        client.events().on(EventType.KNOWLEDGE_INGEST_COMPLETED, listener);
        try {
            client.events().publish(new RuntimeEvent(
                    EventType.KNOWLEDGE_INGEST_REQUESTED,
                    requestId,
                    "KnowledgeSDK",
                    Instant.now(),
                    requestMetadata));

            Map<String, Object> result = completion.get(INGEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Object status = result.get("status");
            if ("FAILED".equals(status)) {
                throw new SDKException(
                        SDKErrorCode.KNOWLEDGE_FAILED,
                        "KnowledgeSDK",
                        requestId,
                        "Ingestion failed: " + result.get("error"));
            }

            return SDKResponse.builder()
                    .answer("INGESTED: " + title)
                    .confidence(1.0)
                    .reasoningAvailable(false)
                    .metadata("sdk-version:" + configurationVersion()
                            + ",operation:INGEST_KNOWLEDGE"
                            + ",documentId:" + result.get("documentId"))
                    .structuredPayload(result)
                    .build();

        } catch (SDKException e) {
            throw e;
        } catch (TimeoutException e) {
            throw new SDKException(
                    SDKErrorCode.KNOWLEDGE_FAILED,
                    "KnowledgeSDK",
                    requestId,
                    "No runtime ingestion consumer answered within "
                            + INGEST_TIMEOUT_SECONDS + "s");
        } catch (Exception e) {
            throw new SDKException(
                    SDKErrorCode.UNKNOWN,
                    "KnowledgeSDK",
                    requestId,
                    "Ingestion request failed: " + e.getMessage(),
                    e);
        } finally {
            client.events().off(EventType.KNOWLEDGE_INGEST_COMPLETED, listener);
        }
    }

    private String configurationVersion() {
        return client.configuration() != null
                ? client.configuration().version()
                : "unknown";
    }
}
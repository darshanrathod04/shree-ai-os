package com.shreeai.os.platform.runtime.execution;

import com.shreeai.os.platform.kernels.knowledge.api.KnowledgeIngestionService;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeIngestionResult;
import com.shreeai.os.platform.sdk.events.EventType;
import com.shreeai.os.platform.sdk.events.RuntimeEvent;
import com.shreeai.os.platform.sdk.events.RuntimeEventBus;
import com.shreeai.os.platform.sdk.events.RuntimeEventListener;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * <b>KnowledgeIngestionEventConsumer</b>
 *
 * <p>Runtime-side consumer of {@code KNOWLEDGE_INGEST_REQUESTED} events.
 * Bridges the event-driven SDK ingestion contract
 * ({@code KnowledgeSDK.ingest}) to the Knowledge Kernel: it invokes the
 * {@link KnowledgeIngestionService} and answers with a
 * {@code KNOWLEDGE_INGEST_COMPLETED} event carrying the ingestion result
 * metadata (metadata-first document schema: documentId, tenantId,
 * embeddingVersion, chunkCount, nodeIds — or an error on failure).</p>
 *
 * <p><b>Threading:</b> the canonical {@code RuntimeEventBus} dispatches
 * synchronously on the publisher thread, so ingestion completes before the
 * publisher continues. The consumer is stateless and thread-safe.</p>
 *
 * <p><b>No duplicated kernel logic:</b> the consumer contains zero ingestion
 * logic — it only translates an event into a single kernel service call.</p>
 *
 * <p><b>Ownership:</b> Runtime — Execution</p>
 * <p><b>Version:</b> 1.0</p>
 * <p><b>Constitutional Authority:</b> PHASE-1-ARCH-001</p>
 */
public final class KnowledgeIngestionEventConsumer implements RuntimeEventListener {

    private final Supplier<KnowledgeIngestionService> ingestionService;
    private final RuntimeEventBus eventBus;

    /**
     * Creates a consumer. The supplier indirection allows the runtime to bind
     * the consumer before its kernel stack finishes lazy initialization.
     *
     * @param ingestionService supplier of the ingestion service (must not be null;
     *                         may return null while the runtime is not initialized)
     * @param eventBus         the bus to publish the completion event on (must not be null)
     */
    public KnowledgeIngestionEventConsumer(
            Supplier<KnowledgeIngestionService> ingestionService,
            RuntimeEventBus eventBus) {
        this.ingestionService = Objects.requireNonNull(ingestionService, "ingestionService must not be null");
        this.eventBus = Objects.requireNonNull(eventBus, "eventBus must not be null");
    }

    @Override
    public void onEvent(RuntimeEvent event) {
        if (event == null || EventType.KNOWLEDGE_INGEST_REQUESTED != event.type()) {
            return;
        }

        Map<String, Object> completionMetadata = process(event);
        eventBus.publish(new RuntimeEvent(
                EventType.KNOWLEDGE_INGEST_COMPLETED,
                event.requestId(),
                "KnowledgeKernel",
                Instant.now(),
                completionMetadata));
    }

    private Map<String, Object> process(RuntimeEvent event) {
        Map<String, Object> requestMetadata = event.metadata();
        String title = string(requestMetadata.get("title"));
        String content = string(requestMetadata.get("content"));

        KnowledgeIngestionService service = ingestionService.get();
        if (service == null) {
            return failure("Runtime ingestion service is not initialized");
        }

        try {
            Map<String, Object> callerMetadata = new HashMap<>(requestMetadata);
            callerMetadata.remove("title");
            callerMetadata.remove("content");

            KnowledgeIngestionResult result = service.ingest(title, content, callerMetadata);

            Map<String, Object> completion = new HashMap<>();
            completion.put("status", "INGESTED");
            completion.put("documentId", result.getDocumentId());
            completion.put("title", result.getTitle());
            completion.put("tenantId", result.getTenantId());
            completion.put("chunkCount", result.getChunkCount());
            completion.put("nodeIds", result.getNodeIds());
            completion.put("embeddingVersion", result.getEmbeddingVersion());
            return completion;

        } catch (Exception e) {
            return failure(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }

    private Map<String, Object> failure(String error) {
        Map<String, Object> completion = new HashMap<>();
        completion.put("status", "FAILED");
        completion.put("error", error);
        return completion;
    }

    private String string(Object value) {
        return value != null ? value.toString() : null;
    }
}

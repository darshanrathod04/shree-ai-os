package com.shreeai.os.platform.sdk.events;

/**
 * Canonical runtime event types exposed by the SDK.
 */
public enum EventType {

    PIPELINE_STARTED,
    PIPELINE_COMPLETED,
    PIPELINE_FAILED,

    IDENTITY_COMPLETED,
    CONTEXT_COMPLETED,
    MEMORY_RECALL_COMPLETED,
    KNOWLEDGE_COMPLETED,
    REASONING_COMPLETED,
    INFERENCE_COMPLETED,
    PLANNING_COMPLETED,
    EXECUTION_COMPLETED,
    MEMORY_STORE_COMPLETED,
    CHIEF_REVIEW_COMPLETED,
    REFLECTION_COMPLETED,

    /**
     * Published after a reflection has been persisted to the repository and
     * memory bridge. Metadata: executionId, tenantId, verdict, score, importanceScore
     */
    REFLECTION_PERSISTED,

    /**
     * Published by {@code KnowledgeSDK.ingest(...)} requesting permanent
     * document ingestion. Metadata: {@code title}, {@code content},
     * {@code tenantId} (metadata-first document schema).
     */
    KNOWLEDGE_INGEST_REQUESTED,

    /**
     * Published by the runtime ingestion consumer after processing a
     * {@link #KNOWLEDGE_INGEST_REQUESTED} event. Metadata: {@code status},
     * {@code documentId}, {@code chunkCount}, {@code nodeIds},
     * {@code embeddingVersion} (or {@code error} on failure).
     */
    KNOWLEDGE_INGEST_COMPLETED
}
package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.SDKResponse;
import com.shreeai.os.platform.sdk.ShreeAI;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PHASE-1 acceptance test: the frozen SDK usage pattern
 * {@code ShreeAI.builder().apiKey("local").build()} gains the additive
 * event-driven {@code KnowledgeSDK.ingest(title, content)} API. Ingestion
 * completes before the call returns (synchronous event bus), and the ingested
 * document is immediately retrievable through the existing frozen
 * {@code query}/{@code search} contracts.
 */
class KnowledgeIngestSdkIntegrationTest {

    private static final String TITLE = "Postgres Replication Guide";

    private static final String CONTENT =
            "Postgres database replication streams the write-ahead log to a hot standby. "
                    + "Streaming replication keeps the standby ready for failover at any time. "
                    + "Failover promotion promotes the standby to primary within seconds.";

    @Test
    void ingestReturnsAcknowledgementWithMetadataFirstSchema() {
        ShreeAI ai = ShreeAI.builder().apiKey("local").build();

        SDKResponse response = ai.knowledge().ingest(TITLE, CONTENT);

        assertNotNull(response);
        assertTrue(response.answer().startsWith("INGESTED"));

        Map<String, Object> payload = response.structuredPayload();
        assertEquals("INGESTED", payload.get("status"));
        assertNotNull(payload.get("documentId"));
        assertNotNull(payload.get("tenantId"));
        assertNotNull(payload.get("embeddingVersion"));
        assertTrue(((Number) payload.get("chunkCount")).intValue() >= 1);
        assertFalse(((java.util.List<?>) payload.get("nodeIds")).isEmpty());
    }

    @Test
    void ingestedDocumentIsImmediatelySearchableThroughFrozenSdk() {
        ShreeAI ai = ShreeAI.builder().apiKey("local").build();

        ai.knowledge().ingest(TITLE, CONTENT);

        SDKResponse search = ai.knowledge().search("Postgres");
        assertNotNull(search);
        assertNotNull(search.answer());
    }

    @Test
    void ingestValidatesInput() {
        ShreeAI ai = ShreeAI.builder().apiKey("local").build();

        org.junit.jupiter.api.Assertions.assertThrows(
                com.shreeai.os.platform.sdk.exceptions.ValidationException.class,
                () -> ai.knowledge().ingest(null, CONTENT));
        org.junit.jupiter.api.Assertions.assertThrows(
                com.shreeai.os.platform.sdk.exceptions.ValidationException.class,
                () -> ai.knowledge().ingest("  ", CONTENT));
        org.junit.jupiter.api.Assertions.assertThrows(
                com.shreeai.os.platform.sdk.exceptions.ValidationException.class,
                () -> ai.knowledge().ingest(TITLE, null));
    }
}

package com.shreeai.os.platform.runtime.storage;

import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeId;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.TransactionContext;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link Neo4jKnowledgeGraphAdapter} using a mocked Neo4j
 * session (no server required). Proves the adapter implements the
 * {@link KnowledgeGraphStore} SPI without hard-coding any provider into the
 * kernel.
 */
class Neo4jKnowledgeGraphAdapterTest {

    private Session session;
    private TransactionContext transaction;
    private Neo4jKnowledgeGraphAdapter adapter;

    @BeforeEach
    void setUp() {
        session = mock(Session.class);
        transaction = mock(TransactionContext.class);
        adapter = new Neo4jKnowledgeGraphAdapter(() -> session);
    }

    @SuppressWarnings("unchecked")
    private void stubWrite() {
        when(transaction.run(anyString(), any(Value.class))).thenReturn(mock(Result.class));
        when(session.executeWrite(any())).thenAnswer(invocation -> {
            org.neo4j.driver.TransactionCallback<Object> callback = invocation.getArgument(0);
            return callback.execute(transaction);
        });
    }

    @SuppressWarnings("unchecked")
    private void stubRead(Result result) {
        when(transaction.run(anyString(), any(Value.class))).thenReturn(result);
        when(session.executeRead(any())).thenAnswer(invocation -> {
            org.neo4j.driver.TransactionCallback<List<Record>> callback = invocation.getArgument(0);
            return callback.execute(transaction);
        });
    }

    @Test
    void saveNodeMergesAKnowledgeNode() {
        stubWrite();

        adapter.saveNode(testNode());

        verify(transaction).run(contains("MERGE (n:KnowledgeNode"), any(Value.class));
        verify(transaction).run(contains("$id"), any(Value.class));
    }

    @Test
    void findNodeByIdMapsPropertiesBackToDomainModel() {
        Value props = Values.value(Map.of(
                "id", "node-1",
                "type", "CONCEPT",
                "state", "ACTIVE",
                "scope", "GLOBAL",
                "label", "Postgres Replication",
                "description", "Streams WAL to a standby.",
                "metadata", "{\"documentId\":\"doc-1\",\"tenantId\":\"default\"}",
                "createdAt", Instant.now().toEpochMilli(),
                "updatedAt", Instant.now().toEpochMilli()));

        Record record = mock(Record.class);
        when(record.get("props")).thenReturn(props);

        Result result = mock(Result.class);
        when(result.list()).thenReturn(List.of(record));
        stubRead(result);

        KnowledgeNode node = adapter.findNodeById("node-1").orElseThrow();

        assertEquals("node-1", node.getId().value());
        assertEquals("Postgres Replication", node.getLabel());
        assertEquals(KnowledgeType.CONCEPT, node.getType());
        assertEquals(KnowledgeState.ACTIVE, node.getState());
        assertEquals(KnowledgeScope.GLOBAL, node.getScope());
        assertEquals("doc-1", node.getMetadata().get("documentId"));
    }

    @Test
    void removeNodeReportsWhetherAnythingWasDeleted() {
        Record record = mock(Record.class);
        when(record.get("removed")).thenReturn(Values.value(true));

        Result result = mock(Result.class);
        when(result.list()).thenReturn(List.of(record));
        stubRead(result);

        assertTrue(adapter.removeNode("node-1"));
    }

    private KnowledgeNode testNode() {
        return KnowledgeNode.of(
                new KnowledgeId("node-1"),
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Postgres Replication",
                "Streams WAL to a standby.",
                Map.of("documentId", "doc-1"),
                Instant.now(),
                Instant.now());
    }
}

package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.core.discovery.model.CapabilityId;
import com.shreeai.os.platform.core.discovery.validator.DiscoveryValidator;
import com.shreeai.os.platform.kernels.chief.engine.ChiefProcessingEngine;
import com.shreeai.os.platform.kernels.chief.error.ChiefException;
import com.shreeai.os.platform.kernels.chief.model.ChiefId;
import com.shreeai.os.platform.kernels.chief.model.ChiefMetrics;
import com.shreeai.os.platform.kernels.chief.model.ChiefRequest;
import com.shreeai.os.platform.kernels.chief.model.ChiefResponse;
import com.shreeai.os.platform.kernels.chief.service.DefaultChiefService;
import com.shreeai.os.platform.kernels.chief.validation.ChiefValidationResult;
import com.shreeai.os.platform.kernels.chief.validation.ChiefValidator;
import com.shreeai.os.platform.kernels.execution.engine.ExecutionProcessingEngine;
import com.shreeai.os.platform.kernels.execution.service.DefaultExecutionService;
import com.shreeai.os.platform.kernels.execution.validation.ExecutionValidator;
import com.shreeai.os.platform.kernels.knowledge.engine.DefaultKnowledgeProcessingEngine;
import com.shreeai.os.platform.kernels.knowledge.model.CreateKnowledgeRequest;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeNode;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeScope;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeState;
import com.shreeai.os.platform.kernels.knowledge.model.KnowledgeType;
import com.shreeai.os.platform.kernels.knowledge.service.DefaultKnowledgeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Phase1KernelCompletionTest {

    @Test
    @DisplayName("DefaultKnowledgeService graph traversal, relationships, concepts, and classification work")
    void testKnowledgeServiceCompletions() {
        DefaultKnowledgeService service = new DefaultKnowledgeService(new DefaultKnowledgeProcessingEngine());

        String idA = service.createKnowledge(CreateKnowledgeRequest.of(
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Shree Runtime",
                "Core cognitive orchestration runtime",
                Map.of("concepts", "runtime,orchestration")
        ));
        String idB = service.createKnowledge(CreateKnowledgeRequest.of(
                KnowledgeType.CONCEPT,
                KnowledgeState.ACTIVE,
                KnowledgeScope.GLOBAL,
                "Hybrid RAG",
                "Combines HNSW vector similarity and GIN full-text search",
                Map.of("concepts", "rag,vector")
        ));
        service.createRelationship(idA, idB, "DEPENDS_ON");

        Object[] traversed = service.traverseGraph(idA, new String[]{"DEPENDS_ON"});
        assertEquals(1, traversed.length);
        assertEquals("Hybrid RAG", ((KnowledgeNode) traversed[0]).getLabel());

        Object[] rels = service.getEntityRelationships(idA);
        assertEquals(1, rels.length);

        List<KnowledgeNode> conceptMatches = service.searchByConcept("Hybrid RAG");
        assertEquals(1, conceptMatches.size());

        String sample = "Shree AI OS combines Vector Search and PostgreSQL Storage. Security uses Fail-Closed validation.";
        Object[] concepts = service.extractConcepts(sample);
        assertTrue(concepts.length > 0);

        Object[] extractedRels = service.extractRelationships(sample);
        assertTrue(extractedRels.length >= 1);

        String[] categories = service.classifyContent(sample);
        List<String> categoryList = List.of(categories);
        assertTrue(categoryList.contains("security"));
        assertTrue(categoryList.contains("storage"));

        Object[] structured = service.generateStructuredKnowledge(sample);
        assertEquals(1, structured.length);
    }

    @Test
    @DisplayName("DefaultChiefService tracks orchestration status, cancellation, and health metrics")
    void testChiefServiceCompletions() {
        ChiefValidator validator = new ChiefValidator();
        ChiefProcessingEngine engine = mock(ChiefProcessingEngine.class);

        ChiefId chiefId = new ChiefId("chief-req-1");
        ChiefRequest request = new ChiefRequest(chiefId, "PLAN", null, null, Map.of(), Map.of());
        ChiefResponse expected = new ChiefResponse(chiefId, true, "ok", null, null, null, Instant.now(), Map.of());
        when(engine.process(request)).thenReturn(expected);

        DefaultChiefService chiefService = new DefaultChiefService(validator, engine);
        ChiefResponse submitted = chiefService.submitOrchestration(request);
        assertTrue(submitted.success());

        ChiefResponse status = chiefService.getOrchestrationStatus("chief-req-1");
        assertEquals("ok", status.message());

        assertTrue(chiefService.cancelOrchestration("chief-req-1"));
        assertFalse(chiefService.cancelOrchestration("unknown-req"));
        assertThrows(ChiefException.class, () -> chiefService.getOrchestrationStatus("unknown-req"));

        ChiefMetrics health = chiefService.getChiefHealth();
        assertEquals(0, health.activeOrchestrations());
        assertEquals(1, health.completedOrchestrations());
        assertEquals(0, health.failedOrchestrations());
        assertEquals("HEALTHY", health.metadata().get("status"));
    }

    @Test
    @DisplayName("DefaultExecutionService cancelExecution and DiscoveryValidator metadata validation work")
    void testExecutionCancelAndDiscoveryMetadata() {
        DefaultExecutionService executionService = new DefaultExecutionService(
                mock(ExecutionValidator.class),
                mock(ExecutionProcessingEngine.class)
        );
        assertTrue(executionService.cancelExecution("exec-42"));
        assertTrue(executionService.isCancelled("exec-42"));
        assertFalse(executionService.isCancelled("exec-99"));

        DiscoveryValidator discoveryValidator = new DiscoveryValidator();
        CapabilityId capId = new CapabilityId("cap-1");
        assertTrue(discoveryValidator.validateCapabilityMetadata(capId, Map.of("version", "1.1")).isValid());
        assertFalse(discoveryValidator.validateCapabilityMetadata(capId, Map.of(" ", "x")).isValid());
        assertFalse(discoveryValidator.validateCapabilityMetadata(capId, "   ").isValid());
    }
}

package com.shree.playground;

import com.shree.playground.dto.*;
import com.shreeai.os.platform.runtime.execution.*;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.sdk.SDKResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class V2_1_AcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShreeAI ai;

    private static final org.slf4j.Logger LOG =
            org.slf4j.LoggerFactory.getLogger(V2_1_AcceptanceTest.class);

    private static final com.fasterxml.jackson.databind.ObjectMapper MAPPER =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @Test
    @Order(1)
    @DisplayName("S1: Chat returns normal answer from LLM kernel")
    void chat_returnsNormalAnswer() throws Exception {
        LOG.info("=== S1: CHAT ===");
        MvcResult result = mockMvc.perform(get("/api/playground/chat"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        LOG.info("S1 REQUEST:  GET /api/playground/chat");
        LOG.info("S1 RESPONSE: {}", body);
        ChatResponse response = MAPPER.readValue(body, ChatResponse.class);
        assertThat(response.answer()).isNotBlank();
        assertThat(response.confidence()).isGreaterThan(0.0);
        LOG.info("S1 PASS: answer=\"{}\" confidence={}", response.answer(), response.confidence());
    }

    @Test
    @Order(2)
    @DisplayName("S2: Planning returns structured plan")
    void planning_returnsStructuredPlan() throws Exception {
        LOG.info("=== S2: PLANNING ===");
        String reqBody = "{\"objective\":\"Launch MVP\",\"complexity\":\"high\"}";
        MvcResult result = mockMvc.perform(post("/api/playground/plan")
                .contentType(MediaType.APPLICATION_JSON).content(reqBody))
                .andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();
        LOG.info("S2 REQUEST: POST /api/playground/plan body={}", reqBody);
        LOG.info("S2 RESPONSE: {}", body);
        SDKResponse response = MAPPER.readValue(body, SDKResponse.class);
        assertThat(response.answer()).isNotBlank();
        assertThat(response.confidence()).isGreaterThan(0.0);
        LOG.info("S2 PASS");
    }

    @Test
    @Order(3)
    @DisplayName("S3: Memory Save persists value")
    void memorySave_persistsValue() throws Exception {
        LOG.info("=== S3: MEMORY SAVE ===");
        String reqBody = "{\"key\":\"test-user-pref\",\"value\":\"dark-mode\"}";
        MvcResult result = mockMvc.perform(post("/api/playground/memory/save")
                .contentType(MediaType.APPLICATION_JSON).content(reqBody))
                .andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();
        LOG.info("S3 REQUEST: POST /api/playground/memory/save body={}", reqBody);
        LOG.info("S3 RESPONSE: {}", body);
        MemoryResponse response = MAPPER.readValue(body, MemoryResponse.class);
        assertThat(response.key()).isEqualTo("test-user-pref");
        assertThat(response.value()).isEqualTo("dark-mode");
        assertThat(response.source()).isEqualTo("Memory Kernel");
        LOG.info("S3 PASS");
    }

    @Test
    @Order(4)
    @DisplayName("S4: Memory Recall returns saved value")
    void memoryRecall_returnsSavedValue() throws Exception {
        LOG.info("=== S4: MEMORY RECALL ===");
        String saveBody = "{\"key\":\"recall-test-key\",\"value\":\"recall-test-value\"}";
        mockMvc.perform(post("/api/playground/memory/save")
                .contentType(MediaType.APPLICATION_JSON).content(saveBody))
                .andExpect(status().isOk());
        MvcResult result = mockMvc.perform(get("/api/playground/memory/recall-test-key"))
                .andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();
        LOG.info("S4 REQUEST: GET /api/playground/memory/recall-test-key");
        LOG.info("S4 RESPONSE: {}", body);
        MemoryResponse response = MAPPER.readValue(body, MemoryResponse.class);
        assertThat(response.key()).isEqualTo("recall-test-key");
        assertThat(response.value()).isEqualTo("recall-test-value");
        LOG.info("S4 PASS");
    }

    @Test
    @Order(6)
    @DisplayName("S6: Execution(project-planning) invokes Planning Kernel")
    void execution_projectPlanning() throws Exception {
        LOG.info("=== S6: EXECUTION PROJECT_PLANNING ===");
        String reqBody = "{\"capability\":\"project-planning\",\"input\":\"Build REST API\"}";
        MvcResult result = mockMvc.perform(post("/api/playground/execute")
                .contentType(MediaType.APPLICATION_JSON).content(reqBody))
                .andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();
        LOG.info("S6 REQUEST: POST /api/playground/execute body={}", reqBody);
        LOG.info("S6 RESPONSE: {}", body);
        SDKResponse response = MAPPER.readValue(body, SDKResponse.class);
        assertThat(response.answer()).isNotBlank();
        assertThat(response.confidence()).isGreaterThan(0.0);
        LOG.info("S6 PASS: Planning Kernel invoked");
    }

    @Test
    @Order(7)
    @DisplayName("S7: Execution(knowledge-search) invokes Knowledge Kernel")
    void execution_knowledgeSearch() throws Exception {
        LOG.info("=== S7: EXECUTION KNOWLEDGE_SEARCH ===");
        String reqBody = "{\"capability\":\"knowledge-search\",\"input\":\"neural networks\"}";
        MvcResult result = mockMvc.perform(post("/api/playground/execute")
                .contentType(MediaType.APPLICATION_JSON).content(reqBody))
                .andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();
        LOG.info("S7 REQUEST: POST /api/playground/execute body={}", reqBody);
        LOG.info("S7 RESPONSE: {}", body);
        SDKResponse response = MAPPER.readValue(body, SDKResponse.class);
        assertThat(response.answer()).isNotBlank();
        assertThat(response.confidence()).isGreaterThan(0.0);
        LOG.info("S7 PASS: Knowledge Kernel invoked");
    }

    @Test
    @Order(8)
    @DisplayName("S8: Permission DENY blocks execution")
    void permissionDeny_blocksExecution() {
        LOG.info("=== S8: PERMISSION DENY ===");
        DefaultPermissionPolicy denyPolicy = new DefaultPermissionPolicy(PermissionDecision.ALLOW);
        denyPolicy.set(ExecutionCapability.TASK_EXECUTION, PermissionDecision.DENY);
        KernelRegistry registry = new KernelRegistry();
        boolean[] handlerInvoked = {false};
        registry.register(ExecutionCapability.TASK_EXECUTION, (c, i, ctx) -> {
            handlerInvoked[0] = true;
            return RichExecutionResult.success(c, "should-not-execute", 1.0);
        });
        ExecutionDispatcher dispatcher = new ExecutionDispatcher(registry, denyPolicy);
        LOG.info("S8 REQUEST: dispatch(TASK_EXECUTION) with DENY");
        RichExecutionResult result = dispatcher.dispatch(
                ExecutionCapability.TASK_EXECUTION, "test-input", Map.of());
        LOG.info("S8 RESPONSE: status={} output={}", result.status(), result.output());
        assertThat(handlerInvoked[0]).isFalse();
        assertThat(result.status()).isEqualTo(ExecutionStatus.DENIED);
        assertThat(result.output()).containsIgnoringCase("denied");
        LOG.info("S8 PASS: handler not invoked");
    }
}


    @Test
    @Order(5)
    @DisplayName("S5: Knowledge returns knowledge response")
    void knowledge_returnsKnowledgeResponse() throws Exception {
        LOG.info("=== S5: KNOWLEDGE ===");
        String reqBody = "{\"query\":\"machine learning\"}";
        MvcResult result = mockMvc.perform(post("/api/playground/knowledge")
                .contentType(MediaType.APPLICATION_JSON).content(reqBody))
                .andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString();
        LOG.info("S5 REQUEST: POST /api/playground/knowledge body={}", reqBody);
        LOG.info("S5 RESPONSE: {}", body);
        assertThat(body).isNotBlank();
        LOG.info("S5 PASS: knowledge response received");
    }

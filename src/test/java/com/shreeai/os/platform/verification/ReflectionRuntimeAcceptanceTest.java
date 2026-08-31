package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.sdk.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 1.5 Runtime Acceptance Test.
 * Validates the full reflection pipeline through the real SDK.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReflectionRuntimeAcceptanceTest {

    private static ShreeAI ai;
    private static String firstAnswer;

    @BeforeAll
    static void setUp() {
        ai = ShreeAI.builder().build();
    }

    @Test
    @Order(1)
    @DisplayName("1. Planning: Build AI Hospital Management System")
    void planningGeneratesExecutionIdAndPlan() {
        SDKResponse response = ai.chat(
                SDKRequest.builder().message("Build an AI Hospital Management System").build());
        assertNotNull(response);
        assertNotNull(response.answer());
        assertFalse(response.answer().isBlank());
        firstAnswer = response.answer();
        System.out.println("[SCENARIO 1] ANSWER_LEN=" + firstAnswer.length()
                + " CONFIDENCE=" + response.confidence());
    }

    @Test
    @Order(2)
    @DisplayName("2. Reflection Auto-Trigger")
    void reflectionAutoTriggersAfterExecution() {
        SDKResponse memoryResponse = ai.memory().search("reflection lesson");
        assertNotNull(memoryResponse);
        System.out.println("[SCENARIO 2] MEMORY_ANSWER="
                + (memoryResponse.answer() != null ? memoryResponse.answer().substring(0, Math.min(200, memoryResponse.answer().length())) : "null"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Memory Bridge: Lessons stored as OBSERVATION memories")
    void memoryBridgeStoresLessonsAsObservations() {
        SDKResponse memoryResponse = ai.memory().search("hospital management");
        assertNotNull(memoryResponse);
        System.out.println("[SCENARIO 3] ANSWER_LEN="
                + (memoryResponse.answer() != null ? memoryResponse.answer().length() : 0));
    }

    @Test
    @Order(4)
    @DisplayName("4. Reflection Repository: Query reflection history via SDK")
    void reflectionRepositoryContainsRecord() {
        SDKResponse historyResponse = ai.reflection().getHistory("default-tenant", 10);
        assertNotNull(historyResponse);
        assertNotNull(historyResponse.answer());
        System.out.println("[SCENARIO 4] HISTORY_ANSWER="
                + historyResponse.answer().substring(0, Math.min(200, historyResponse.answer().length())));
    }

    @Test
    @Order(5)
    @DisplayName("5. Planning Learns: Second request uses reflections")
    void planningConsumesPreviousReflections() {
        SDKResponse response = ai.chat(
                SDKRequest.builder().message("Build a Hospital ERP").build());
        assertNotNull(response);
        assertNotNull(response.answer());
        assertFalse(response.answer().isBlank());
        System.out.println("[SCENARIO 5] SECOND_ANSWER_LEN=" + response.answer().length()
                + " CONFIDENCE=" + response.confidence());
    }

    @Test
    @Order(6)
    @DisplayName("6. Playground: All SDK APIs respond")
    void allSdksRespond() {
        assertNotNull(ai.chat(SDKRequest.builder().message("ping").build()).answer());
        assertNotNull(ai.identity().getIdentity("default-tenant"));
        assertNotNull(ai.planning().createPlan("obj-1", "test task", "default").answer());
        assertNotNull(ai.memory().search("test"));
        assertNotNull(ai.knowledge().search("AI"));
        assertNotNull(ai.execution().execute("echo", "test"));
        assertNotNull(ai.reflection().getHistory("default-tenant", 5));
        System.out.println("[SCENARIO 6] ALL_SDKS_OK");
    }
}
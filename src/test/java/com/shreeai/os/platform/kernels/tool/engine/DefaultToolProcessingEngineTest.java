package com.shreeai.os.platform.kernels.tool.engine;

import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;
import com.shreeai.os.platform.kernels.tool.model.ToolStatus;
import com.shreeai.os.platform.kernels.tool.model.ToolType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultToolProcessingEngine}: deterministic tool
 * execution outcomes for every tool type.
 */
class DefaultToolProcessingEngineTest {

    private final DefaultToolProcessingEngine engine = new DefaultToolProcessingEngine();

    @Test
    void gitStatusActionCompletes() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t1", ToolType.GIT, "status", Map.of()));

        assertEquals(ToolStatus.COMPLETED, result.status());
        assertEquals(Boolean.TRUE, result.getOutput("clean"));
        assertEquals(Boolean.TRUE, result.getOutput("simulated"));
    }

    @Test
    void gitCloneCarriesRepoUrl() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t2", ToolType.GIT, "clone", Map.of("repoUrl", "https://x/repo")));

        assertEquals("https://x/repo", result.getOutput("repoUrl"));
    }

    @Test
    void filesWriteReportsBytes() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t3", ToolType.FILES, "write", Map.of("content", "hello")));

        assertEquals(5, ((Number) result.getOutput("bytesWritten")).intValue());
    }

    @Test
    void filesExistsReturnsTrue() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t4", ToolType.FILES, "exists", Map.of("path", "/tmp/x")));

        assertEquals(Boolean.TRUE, result.getOutput("exists"));
    }

    @Test
    void browserNavigateCarriesUrl() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t5", ToolType.BROWSER, "navigate", Map.of("url", "https://site")));

        assertEquals("https://site", result.getOutput("url"));
    }

    @Test
    void browserClickCarriesSelector() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t6", ToolType.BROWSER, "click", Map.of("selector", "#btn")));

        assertEquals("#btn", result.getOutput("selector"));
    }

    @Test
    void dbInsertCarriesInsertedId() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t7", ToolType.DB, "insert", Map.of("id", "42")));

        assertEquals("42", result.getOutput("insertedId"));
    }

    @Test
    void dbQueryReturnsEmptyRows() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t8", ToolType.DB, "query", Map.of()));

        assertNotNull(result.getOutput("rows"));
    }

    @Test
    void unknownGitActionFails() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t9", ToolType.GIT, "rebase", Map.of()));

        assertEquals(ToolStatus.FAILED, result.status());
        assertNotNull(result.errorMessage());
    }

    @Test
    void nullRequestThrows() {
        assertThrows(IllegalArgumentException.class, () -> engine.processToolExecution(null));
    }

    @Test
    void metricsAreAttachedToSuccess() {
        ToolResult result = engine.processToolExecution(
                new ToolRequest("t10", ToolType.GIT, "status", Map.of()));

        assertNotNull(result.metrics());
        assertTrue(result.metrics().durationMs() >= 0);
    }
}

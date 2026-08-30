package com.shreeai.os.platform.kernels.tool.service;

import com.shreeai.os.platform.kernels.tool.error.ToolErrorCode;
import com.shreeai.os.platform.kernels.tool.error.ToolException;
import com.shreeai.os.platform.kernels.tool.model.ToolRequest;
import com.shreeai.os.platform.kernels.tool.model.ToolResult;
import com.shreeai.os.platform.kernels.tool.model.ToolStatus;
import com.shreeai.os.platform.kernels.tool.model.ToolType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultToolService}: orchestration, validation
 * rejection, result store and batch execution.
 */
class DefaultToolServiceTest {

    private final DefaultToolService service = new DefaultToolService();

    @Test
    void executesAGitTool() {
        ToolResult result = service.executeTool(
                new ToolRequest("t1", ToolType.GIT, "status", Map.of()));

        assertEquals(ToolStatus.COMPLETED, result.status());
        assertEquals(ToolStatus.COMPLETED, service.getToolStatus("t1"));
        assertTrue(service.getToolResult("t1") != null);
    }

    @Test
    void pendingStatusForUnknownTool() {
        assertEquals(ToolStatus.PENDING, service.getToolStatus("missing"));
        assertNull(service.getToolResult("missing"));
    }

    @Test
    void unsupportedActionIsRejectedWithValidationError() {
        ToolException ex = assertThrows(ToolException.class, () ->
                service.executeTool(
                        new ToolRequest("t2", ToolType.GIT, "rebase", Map.of())));

        assertEquals(ToolErrorCode.VALIDATION_FAILURE, ex.toolError().errorCode());
    }

    @Test
    void nullRequestThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.executeTool(null));
    }

    @Test
    void batchExecutionReturnsAllResults() {
        List<ToolResult> results = service.executeTools(List.of(
                new ToolRequest("t3", ToolType.GIT, "status", Map.of()),
                new ToolRequest("t4", ToolType.FILES, "read", Map.of()),
                new ToolRequest("t5", ToolType.DB, "query", Map.of())
        ));

        assertEquals(3, results.size());
        assertTrue(results.stream().allMatch(r -> r.status() == ToolStatus.COMPLETED));
    }

    @Test
    void isToolSupportedReflectsValidator() {
        assertTrue(service.isToolSupported(ToolType.GIT, "status"));
        assertTrue(!service.isToolSupported(ToolType.GIT, "rebase"));
        assertTrue(!service.isToolSupported(null, "status"));
    }

    @Test
    void toolsAreStoredInResultStore() {
        service.executeTool(new ToolRequest("t6", ToolType.BROWSER, "navigate", Map.of("url", "u")));
        assertEquals("u", service.getToolResult("t6").getOutput("url"));
    }
}

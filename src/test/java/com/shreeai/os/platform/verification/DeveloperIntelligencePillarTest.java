package com.shreeai.os.platform.verification;

import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult;
import com.shreeai.os.platform.kernels.developer.patch.model.PatchDiff;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan;
import com.shreeai.os.platform.sdk.ShreeAI;
import com.shreeai.os.platform.services.DeveloperController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeveloperIntelligencePillarTest {

    @Test
    @DisplayName("DeveloperController serves real diffs, compile report, and executes file rollback")
    @SuppressWarnings("unchecked")
    void testRealDiffsAndRollbackExecution(@TempDir Path tempDir) throws Exception {
        Path targetFile = tempDir.resolve("SampleService.java");
        String original = "public class SampleService { /* v1 */ }";
        String updated = "public class SampleService { /* v2 */ }\n// added line";
        Files.writeString(targetFile, updated);

        PatchDiff diff = PatchDiff.builder()
                .filePath(targetFile.toString())
                .before(original)
                .after(updated)
                .status(PatchDiff.Status.SUCCESS)
                .message("Applied method injection")
                .appliedAt(Instant.now())
                .build();

        RollbackPlan rollbackPlan = RollbackPlan.builder()
                .planId("rb-1")
                .entries(List.of(new RollbackPlan.RollbackEntry(
                        targetFile.toString(),
                        List.of(new RollbackPlan.UndoAction(
                                RollbackPlan.UndoType.RESTORE_FILE,
                                "Restore original SampleService",
                                targetFile.getFileName().toString()
                        )),
                        original
                )))
                .build();

        DeveloperExecutionResult execResult = DeveloperExecutionResult.builder()
                .executionId("exec-real-1")
                .status(DeveloperExecutionResult.Status.SUCCESS)
                .appliedDiffs(List.of(diff))
                .rollbackPlan(rollbackPlan)
                .compileReport(DeveloperExecutionResult.CompileReport.success(12, 0, List.of("Compiled cleanly")))
                .confidence(0.95)
                .build();

        try (ShreeAI ai = ShreeAI.builder().apiKey("local").build()) {
            DeveloperController controller = new DeveloperController(ai);
            Map<String, Object> recorded = controller.recordExecution(execResult);

            assertEquals("exec-real-1", recorded.get("executionId"));
            assertEquals("SUCCESS", recorded.get("status"));
            assertEquals(1, recorded.get("appliedCount"));
            assertEquals("LOW", recorded.get("riskLevel"));

            ResponseEntity<?> diffsRes = controller.getDiffs("exec-real-1");
            assertEquals(200, diffsRes.getStatusCode().value());
            Map<String, Object> diffsBody = (Map<String, Object>) diffsRes.getBody();
            assertEquals(1, diffsBody.get("totalDiffs"));

            ResponseEntity<?> singleDiff = controller.getDiff("exec-real-1", "SampleService.java");
            assertEquals(200, singleDiff.getStatusCode().value());

            ResponseEntity<?> rollbackMeta = controller.getRollback("exec-real-1");
            assertEquals(200, rollbackMeta.getStatusCode().value());
            Map<String, Object> rbBody = (Map<String, Object>) rollbackMeta.getBody();
            assertEquals(true, rbBody.get("hasRollback"));
            assertEquals(1, rbBody.get("fileCount"));

            ResponseEntity<?> rollbackExec = controller.executeRollback("exec-real-1");
            assertEquals(200, rollbackExec.getStatusCode().value());
            assertEquals(original, Files.readString(targetFile));
        }
    }
}

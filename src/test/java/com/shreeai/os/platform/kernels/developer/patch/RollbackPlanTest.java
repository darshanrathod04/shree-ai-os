package com.shreeai.os.platform.kernels.developer.patch;

import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan.RollbackEntry;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan.UndoAction;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan.UndoType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>RollbackPlanTest</b>
 *
 * <p>6 test cases for RollbackPlan.</p>
 *
 * @since Sprint-17
 */
public class RollbackPlanTest {

    @Test
    void build_withEntries_createsValidPlan() {
        RollbackEntry entry = new RollbackEntry(
                "com/example/Demo.java",
                List.of(
                        new UndoAction(UndoType.REMOVE_IMPORT, "Remove import java.util.List", "java.util.List"),
                        new UndoAction(UndoType.REMOVE_METHOD, "Remove method greet()", "greet()")
                ),
                "original content"
        );

        RollbackPlan plan = RollbackPlan.builder()
                .planId("test-plan")
                .addEntry(entry)
                .build();

        assertEquals("test-plan", plan.planId());
        assertEquals(1, plan.fileCount());
        assertEquals(2, plan.totalActions());
        assertFalse(plan.isEmpty());
    }

    @Test
    void build_withNoEntries_isEmpty() {
        RollbackPlan plan = RollbackPlan.builder().build();
        assertTrue(plan.isEmpty());
        assertEquals(0, plan.fileCount());
        assertEquals(0, plan.totalActions());
    }

    @Test
    void rollbackEntry_containsOriginalContent() {
        String original = "package com.example;\npublic class Demo {}";
        RollbackEntry entry = new RollbackEntry(
                "com/example/Demo.java",
                List.of(new UndoAction(UndoType.REMOVE_FILE, "Remove file", "Demo.java")),
                original
        );

        assertEquals(original, entry.originalContent());
        assertEquals("com/example/Demo.java", entry.filePath());
        assertEquals(1, entry.actions().size());
    }

    @Test
    void undoAction_allTypes() {
        UndoAction removeImport = new UndoAction(UndoType.REMOVE_IMPORT, "remove", "java.util.List");
        assertEquals(UndoType.REMOVE_IMPORT, removeImport.type());
        assertEquals("remove", removeImport.description());
        assertEquals("java.util.List", removeImport.target());

        UndoAction removeMethod = new UndoAction(UndoType.REMOVE_METHOD, "remove", "greet()");
        assertEquals(UndoType.REMOVE_METHOD, removeMethod.type());

        UndoAction removeField = new UndoAction(UndoType.REMOVE_FIELD, "remove", "name");
        assertEquals(UndoType.REMOVE_FIELD, removeField.type());

        UndoAction removeFile = new UndoAction(UndoType.REMOVE_FILE, "remove", "Demo.java");
        assertEquals(UndoType.REMOVE_FILE, removeFile.type());

        UndoAction restoreFile = new UndoAction(UndoType.RESTORE_FILE, "restore", "Demo.java");
        assertEquals(UndoType.RESTORE_FILE, restoreFile.type());
    }

    @Test
    void planId_defaultsToTimestamp() {
        RollbackPlan plan = RollbackPlan.builder().build();
        assertNotNull(plan.planId());
        assertTrue(plan.planId().startsWith("rollback-"));
    }

    @Test
    void plan_metadataAreAccessible() {
        RollbackPlan plan = RollbackPlan.builder()
                .metadata(Map.of("instruction", "Add JWT", "confidence", 0.85))
                .build();

        assertEquals("Add JWT", plan.metadata().get("instruction"));
        assertEquals(0.85, plan.metadata().get("confidence"));
        assertNotNull(plan.createdAt());
    }
}

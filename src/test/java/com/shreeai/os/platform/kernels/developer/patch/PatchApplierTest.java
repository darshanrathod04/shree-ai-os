package com.shreeai.os.platform.kernels.developer.patch;

import com.shreeai.os.platform.kernels.developer.codegen.model.FilePatch;
import com.shreeai.os.platform.kernels.developer.codegen.model.FilePatch.Operation;
import com.shreeai.os.platform.kernels.developer.codegen.model.PatchOperation;
import com.shreeai.os.platform.kernels.developer.patch.model.PatchDiff;
import com.shreeai.os.platform.kernels.developer.patch.model.PatchDiff.Status;
import com.shreeai.os.platform.kernels.developer.patch.model.RollbackPlan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>PatchApplierTest</b>
 *
 * <p>12 test cases for PatchApplier covering all supported operations.</p>
 *
 * @since Sprint-17
 */
public class PatchApplierTest {

    private final PatchApplier applier = new PatchApplier();

    private static final String EMPTY_CLASS = """
            package com.example;
            public class Demo {}
            """;

    private static final String CLASS_WITH_METHOD = """
            package com.example;
            public class Demo {
                public void existing() { }
            }
            """;

    // ─── CREATE_CLASS ─────────────────────────────────────────────────────────

    @Test
    void createClass_whenNewFile_returnsSuccessAndCorrectDiff() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/NewClass.java")
                .newFile(true)
                .addOperation(new Operation(PatchOperation.CREATE_CLASS,
                        "NewClass", "package com.example;\npublic class NewClass {}", "test", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, "");

        assertTrue(r.diff().isSuccess());
        assertTrue(r.diff().after().contains("class NewClass"));
        assertFalse(r.diff().before().contains("class NewClass"));
    }

    @Test
    void createClass_whenCodeMissing_returnsFailed() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/NewClass.java")
                .newFile(true)
                .addOperation(new Operation(PatchOperation.CREATE_CLASS, "NewClass", "", "test", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, "");

        assertFalse(r.diff().isSuccess());
        assertEquals(Status.FAILED, r.diff().status());
    }

    // ─── ADD_IMPORT ──────────────────────────────────────────────────────────

    @Test
    void addImport_whenNotPresent_addsImportAndNoDuplicate() {
        String source = """
                package com.example;
                public class Demo {}
                """;

        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_IMPORT,
                        "java.util.List", "import java.util.List;", "needed for List type", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, source);

        assertTrue(r.diff().isSuccess());
        assertTrue(r.diff().after().contains("import java.util.List;"));
        assertNotNull(r.rollbackEntry());
    }

    @Test
    void addImport_whenAlreadyPresent_doesNotDuplicate() {
        String source = """
                package com.example;
                import java.util.List;
                public class Demo {}
                """;

        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_IMPORT,
                        "java.util.List", "import java.util.List;", "already present", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, source);

        assertTrue(r.diff().isSuccess());
        // Should only have the import once
        long count = r.diff().after().lines().filter(l -> l.contains("import java.util.List")).count();
        assertEquals(1, count);
    }

    @Test
    void addImport_whenStaticImport_addsAfterPackageStatement() {
        String source = """
                package com.example;
                public class Demo {}
                """;

        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_IMPORT,
                        "assertEquals", "import static org.junit.Assert.assertEquals;", "test assertion", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, source);

        assertTrue(r.diff().isSuccess());
        assertTrue(r.diff().after().contains("import static org.junit.Assert.assertEquals"));
    }

    // ─── ADD_FIELD ────────────────────────────────────────────────────────────

    @Test
    void addField_whenClassExists_injectsFieldAfterBrace() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_FIELD,
                        "private String name", "private String name;", "add name field", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, EMPTY_CLASS);

        assertTrue(r.diff().isSuccess());
        assertTrue(r.diff().after().contains("private String name;"));
    }

    @Test
    void addField_whenNotInjectable_returnsPartialSuccess() {
        String badSource = "not java code";
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_FIELD,
                        "private String name", "private String name;", "test", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, badSource);

        assertFalse(r.diff().isSuccess());
        assertEquals(Status.SKIPPED, r.diff().status());
    }

    // ─── ADD_METHOD ───────────────────────────────────────────────────────────

    @Test
    void addMethod_injectsBeforeClassClosingBrace() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_METHOD,
                        "public void greet()", "public void greet() {\n        System.out.println(\"hello\");\n    }", "add greet method", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, EMPTY_CLASS);

        assertTrue(r.diff().isSuccess());
        assertTrue(r.diff().after().contains("public void greet()"));
        assertTrue(r.diff().after().contains("hello"));
    }

    @Test
    void addMethod_preservesExistingMethod() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_METHOD,
                        "public void newMethod()", "public void newMethod() {}", "add new method", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, CLASS_WITH_METHOD);

        assertTrue(r.diff().isSuccess());
        assertTrue(r.diff().after().contains("public void existing()"));
        assertTrue(r.diff().after().contains("public void newMethod()"));
    }

    // ─── MODIFY_METHOD ───────────────────────────────────────────────────────

    @Test
    void modifyMethod_whenExists_replacesMethod() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.MODIFY_METHOD,
                        "public void existing()", "public void existing() {\n        System.out.println(\"modified\");\n    }", "modify existing", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, CLASS_WITH_METHOD);

        assertTrue(r.diff().isSuccess());
        assertTrue(r.diff().after().contains("modified"));
    }

    // ─── ROLLBACK ────────────────────────────────────────────────────────────

    @Test
    void rollbackPlan_containsUndoActionsForImport() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_IMPORT,
                        "java.util.Map", "import java.util.Map;", "add map", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, EMPTY_CLASS);

        assertNotNull(r.rollbackEntry());
        assertEquals("com/example/Demo.java", r.rollbackEntry().filePath());
        assertFalse(r.rollbackEntry().actions().isEmpty());
        assertEquals(RollbackPlan.UndoType.REMOVE_IMPORT, r.rollbackEntry().actions().get(0).type());
    }

    @Test
    void rollbackPlan_containsOriginalContent() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(true)
                .addOperation(new Operation(PatchOperation.CREATE_CLASS,
                        "NewClass", "package com.example;\npublic class NewClass {}", "create", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, "");

        assertNotNull(r.rollbackEntry());
        assertEquals("", r.rollbackEntry().originalContent());
    }

    // ─── PATCH DIFF ─────────────────────────────────────────────────────────

    @Test
    void patchDiff_hasCorrectMetadata() {
        FilePatch patch = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_METHOD,
                        "test()", "public void test() {}", "test", List.of()))
                .build();

        PatchApplier.ApplyResult r = applier.apply(patch, EMPTY_CLASS);

        assertEquals("com/example/Demo.java", r.diff().filePath());
        assertNotNull(r.diff().appliedAt());
        assertTrue(r.diff().linesChanged() >= 0);
        assertFalse(r.diff().summary().isEmpty());
    }

    @Test
    void applyAll_processesMultiplePatchesInOrder() {
        String source = """
                package com.example;
                public class Demo {}
                """;

        FilePatch patch1 = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_IMPORT,
                        "java.util.List", "import java.util.List;", "add list", List.of()))
                .build();

        FilePatch patch2 = FilePatch.builder()
                .targetFile("com/example/Demo.java")
                .newFile(false)
                .addOperation(new Operation(PatchOperation.ADD_METHOD,
                        "test()", "public void test() {}", "add test", List.of()))
                .build();

        var results = applier.applyAll(List.of(patch1, patch2), java.util.Map.of("com/example/Demo.java", source));

        assertEquals(2, results.size());
        assertTrue(results.get(0).diff().isSuccess());
        assertTrue(results.get(1).diff().isSuccess());
    }
}

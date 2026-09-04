package com.shreeai.os.platform.kernels.developer.patch;

import com.shreeai.os.platform.kernels.developer.patch.model.DeveloperExecutionResult.CompileReport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <b>CompileValidationTest</b>
 *
 * <p>6 test cases for CompileValidationService.</p>
 *
 * @since Sprint-17
 */
public class CompileValidationTest {

    private final CompileValidationService service = new CompileValidationService();

    @Test
    void compile_withNullPath_returnsSkipped() {
        CompileReport r = service.compile(null);
        assertEquals(CompileReport.CompileStatus.SKIPPED, r.status());
        assertTrue(r.diagnostics().stream().anyMatch(d -> d.contains("No project path")));
    }

    @Test
    void compile_withBlankPath_returnsSkipped() {
        CompileReport r = service.compile("   ");
        assertEquals(CompileReport.CompileStatus.SKIPPED, r.status());
    }

    @Test
    void parseOutput_successExitCode() {
        String output = """
                [INFO] Scanning for projects...
                [INFO] Compiling 5 source files
                [INFO] BUILD SUCCESS
                """;
        CompileReport r = service.parseOutput(output, 0);

        assertEquals(CompileReport.CompileStatus.SUCCESS, r.status());
        assertEquals(0, r.errors());
        assertTrue(r.diagnostics().isEmpty());
    }

    @Test
    void parseOutput_failureWithErrorsAndWarnings() {
        String output = """
                [ERROR] /path/Demo.java:5: error: cannot find symbol
                [ERROR] /path/Demo.java:10: error: method not found
                [WARNING] /path/Demo.java:3: deprecated API
                """;
        CompileReport r = service.parseOutput(output, 1);

        assertEquals(CompileReport.CompileStatus.FAILURE, r.status());
        assertEquals(2, r.errors());
        assertEquals(1, r.warnings());
        assertEquals(3, r.diagnostics().size());
    }

    @Test
    void staticCheck_withBalancedBraces_returnsSuccess() {
        List<String> files = List.of(
                "package com.example;\npublic class Demo {\n    public void test() {}\n}\n",
                "package com.example;\npublic class Other {}\n"
        );
        CompileReport r = service.staticCheck(files);

        assertEquals(CompileReport.CompileStatus.SUCCESS, r.status());
        assertEquals(2, r.filesCompiled());
        assertEquals(0, r.errors());
    }

    @Test
    void staticCheck_withUnbalancedBraces_returnsFailure() {
        List<String> files = List.of(
                "package com.example;\npublic class Demo {\n    public void test() {\n}\n"
        );
        CompileReport r = service.staticCheck(files);

        assertEquals(CompileReport.CompileStatus.FAILURE, r.status());
        assertEquals(1, r.errors());
        assertTrue(r.diagnostics().stream().anyMatch(d -> d.contains("Unbalanced braces")));
    }
}

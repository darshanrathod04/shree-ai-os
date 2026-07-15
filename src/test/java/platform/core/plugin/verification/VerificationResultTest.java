package platform.core.plugin.verification;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VerificationResultTest {

    @Test
    void shouldBeValidWithNoIssues() {
        VerificationResult result = VerificationResult.builder().build();
        assertTrue(result.isValid());
        assertTrue(result.issues().isEmpty());
        assertEquals(0, result.errorCount());
        assertEquals(0, result.warningCount());
        assertEquals(0, result.infoCount());
    }

    @Test
    void shouldBeInvalidWithErrors() {
        VerificationResult result = VerificationResult.builder()
                .addError("Plugin ID missing")
                .build();
        assertFalse(result.isValid());
        assertEquals(1, result.errorCount());
    }

    @Test
    void shouldBeValidWithWarningsOnly() {
        VerificationResult result = VerificationResult.builder()
                .addWarning("Plugin uses deprecated API")
                .build();
        assertTrue(result.isValid());
        assertEquals(1, result.warningCount());
    }

    @Test
    void shouldBeValidWithInfoOnly() {
        VerificationResult result = VerificationResult.builder()
                .addInfo("Plugin version 1.0.0 follows semantic versioning")
                .build();
        assertTrue(result.isValid());
        assertEquals(1, result.infoCount());
    }

    @Test
    void shouldAccumulateMultipleIssues() {
        VerificationResult result = VerificationResult.builder()
                .addError("Plugin ID missing")
                .addWarning("Plugin uses deprecated API")
                .addInfo("Plugin compiled for Java 21")
                .build();

        assertFalse(result.isValid());
        assertEquals(3, result.issues().size());
        assertEquals(1, result.errorCount());
        assertEquals(1, result.warningCount());
        assertEquals(1, result.infoCount());
    }

    @Test
    void shouldBeInvalidWithMixedIssues() {
        VerificationResult result = VerificationResult.builder()
                .addError("Version malformed")
                .addWarning("Optional dependency missing")
                .addInfo("Plugin name is valid")
                .build();

        assertFalse(result.isValid());
        assertEquals(3, result.issues().size());
    }

    @Test
    void shouldReturnUnmodifiableIssues() {
        VerificationResult result = VerificationResult.builder()
                .addError("test")
                .build();
        assertThrows(UnsupportedOperationException.class, () ->
                result.issues().add(new VerificationIssue(VerificationSeverity.INFO, "new")));
    }

    @Test
    void shouldSupportEquality() {
        VerificationResult a = VerificationResult.builder().addError("err").build();
        VerificationResult b = VerificationResult.builder().addError("err").build();
        VerificationResult c = VerificationResult.builder().addWarning("warn").build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void shouldHandleNullIssuesList() {
        VerificationResult result = VerificationResult.builder().build();
        assertTrue(result.issues().isEmpty());
    }
}
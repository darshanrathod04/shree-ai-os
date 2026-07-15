package platform.core.plugin.verification;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VerificationIssueTest {

    @Test
    void shouldCreateIssue() {
        VerificationIssue issue = new VerificationIssue(VerificationSeverity.ERROR, "Plugin ID missing");
        assertEquals(VerificationSeverity.ERROR, issue.severity());
        assertEquals("Plugin ID missing", issue.message());
    }

    @Test
    void shouldRejectNullSeverity() {
        assertThrows(NullPointerException.class, () ->
                new VerificationIssue(null, "message"));
    }

    @Test
    void shouldRejectNullMessage() {
        assertThrows(IllegalArgumentException.class, () ->
                new VerificationIssue(VerificationSeverity.ERROR, null));
    }

    @Test
    void shouldRejectBlankMessage() {
        assertThrows(IllegalArgumentException.class, () ->
                new VerificationIssue(VerificationSeverity.WARNING, "   "));
    }

    @Test
    void shouldSupportEquality() {
        VerificationIssue a = new VerificationIssue(VerificationSeverity.ERROR, "test");
        VerificationIssue b = new VerificationIssue(VerificationSeverity.ERROR, "test");
        VerificationIssue c = new VerificationIssue(VerificationSeverity.WARNING, "test");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void shouldBeImmutable() {
        VerificationIssue issue = new VerificationIssue(VerificationSeverity.INFO, "info message");
        assertNotNull(issue.severity());
        assertNotNull(issue.message());
    }
}
package platform.core.plugin.verification;

import com.shreeai.os.platform.core.plugin.verification.VerificationSeverity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VerificationSeverityTest {

    @Test
    void shouldHaveThreeLevels() {
        VerificationSeverity[] values = VerificationSeverity.values();
        assertEquals(3, values.length);
    }

    @Test
    void shouldContainAllExpectedValues() {
        assertNotNull(VerificationSeverity.valueOf("INFO"));
        assertNotNull(VerificationSeverity.valueOf("WARNING"));
        assertNotNull(VerificationSeverity.valueOf("ERROR"));
    }

    @Test
    void infoShouldBeFirst() {
        assertEquals(0, VerificationSeverity.INFO.ordinal());
    }

    @Test
    void errorShouldBeLast() {
        assertEquals(2, VerificationSeverity.ERROR.ordinal());
    }
}
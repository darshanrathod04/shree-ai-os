package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.DomainCandidate;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.PrimaryDomain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Deterministic unit tests for {@link DefaultDomainDetector}.
 */
public class DefaultDomainDetectorTest {

    private DefaultDomainDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DefaultDomainDetector();
    }

    @Test
    @DisplayName("Test 1: JAVA domain detected for Java keywords")
    void testJavaDomainDetected() {
        DomainProfile profile = detector.detect("I want to learn Java programming");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.JAVA, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 2: SPRING domain detected for Spring keywords")
    void testSpringDomainDetected() {
        DomainProfile profile = detector.detect("Help me build a Spring Boot application");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.SPRING, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 3: DATABASE domain detected for SQL keywords")
    void testDatabaseDomainDetected() {
        DomainProfile profile = detector.detect("How do I optimize my PostgreSQL database queries?");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.DATABASE, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 4: AI domain detected for AI keywords")
    void testAIDomainDetected() {
        DomainProfile profile = detector.detect("I want to build a machine learning model with TensorFlow");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.AI, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 5: WEB domain detected for web keywords")
    void testWebDomainDetected() {
        DomainProfile profile = detector.detect("How do I use React with TypeScript for web development?");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.WEB, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 6: MOBILE domain detected for mobile keywords")
    void testMobileDomainDetected() {
        DomainProfile profile = detector.detect("I want to build an Android app with Kotlin");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.MOBILE, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 7: DEVOPS domain detected for DevOps keywords")
    void testDevOpsDomainDetected() {
        DomainProfile profile = detector.detect("How do I set up a CI/CD pipeline with Docker and Kubernetes?");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.DEVOPS, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 8: CLOUD domain detected for cloud keywords")
    void testCloudDomainDetected() {
        DomainProfile profile = detector.detect("How do I deploy to AWS using Lambda?");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.CLOUD, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 9: SECURITY domain detected for security keywords")
    void testSecurityDomainDetected() {
        DomainProfile profile = detector.detect("How do I prevent SQL injection and XSS attacks?");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.SECURITY, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 10: FINANCE domain detected for finance keywords")
    void testFinanceDomainDetected() {
        DomainProfile profile = detector.detect("How do I build a payment processing system for banking?");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.FINANCE, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 11: MEDICAL domain detected for medical keywords")
    void testMedicalDomainDetected() {
        DomainProfile profile = detector.detect("How do I build a healthcare application for patient management?");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.MEDICAL, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 12: EDUCATION domain detected for education keywords")
    void testEducationDomainDetected() {
        DomainProfile profile = detector.detect("How do I build an online learning platform for students?");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.EDUCATION, profile.primaryDomain());
        assertTrue(profile.confidence() > 0.0);
    }

    @Test
    @DisplayName("Test 13: Empty input returns UNKNOWN domain")
    void testEmptyInputReturnsUnknown() {
        DomainProfile profile = detector.detect("");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.UNKNOWN, profile.primaryDomain());
        assertEquals(0.0, profile.confidence());
    }

    @Test
    @DisplayName("Test 14: Whitespace-only input returns UNKNOWN domain")
    void testWhitespaceInputReturnsUnknown() {
        DomainProfile profile = detector.detect("   ");
        assertNotNull(profile);
        assertEquals(PrimaryDomain.UNKNOWN, profile.primaryDomain());
    }

    @Test
    @DisplayName("Test 15: Confidence is always between 0.0 and 1.0")
    void testConfidenceInRange() {
        String[] inputs = {
                "java", "spring", "database", "ai", "web", "mobile",
                "devops", "cloud", "security", "finance", "medical", "education",
                "random xyz nonsense"
        };
        for (String input : inputs) {
            DomainProfile profile = detector.detect(input);
            assertTrue(profile.confidence() >= 0.0 && profile.confidence() <= 1.0,
                    "Confidence must be in [0.0, 1.0] for input: " + input);
        }
    }

    @Test
    @DisplayName("Test 16: Detected domains are sorted by confidence descending")
    void testDetectedDomainsSortedByConfidence() {
        DomainProfile profile = detector.detect("I want to build a Spring Boot web application with React");
        List<DomainCandidate> domains = profile.detectedDomains();
        assertNotNull(domains);
        for (int i = 0; i < domains.size() - 1; i++) {
            assertTrue(domains.get(i).confidence() >= domains.get(i + 1).confidence(),
                    "Detected domains should be sorted by confidence descending");
        }
    }

    @Test
    @DisplayName("Test 17: Same input returns same output (determinism)")
    void testDeterminism() {
        String input = "I want to build a Spring Boot web application";
        DomainProfile profile1 = detector.detect(input);
        DomainProfile profile2 = detector.detect(input);
        assertEquals(profile1.primaryDomain(), profile2.primaryDomain());
        assertEquals(profile1.confidence(), profile2.confidence());
        assertEquals(profile1.detectedDomains().size(), profile2.detectedDomains().size());
    }

    @Test
    @DisplayName("Test 18: Detection method is set correctly")
    void testDetectionMethod() {
        DomainProfile profile = detector.detect("java");
        assertNotNull(profile.detectionMethod());
        assertFalse(profile.detectionMethod().isEmpty());
    }

    @Test
    @DisplayName("Test 19: DetectedAt timestamp is set")
    void testDetectedAtSet() {
        DomainProfile profile = detector.detect("java");
        assertNotNull(profile.detectedAt());
    }

    @Test
    @DisplayName("Test 20: Unknown input returns GENERAL domain")
    void testUnknownInputReturnsGeneral() {
        DomainProfile profile = detector.detect("xyz abc 123 random");
        assertNotNull(profile);
        assertTrue(profile.primaryDomain() == PrimaryDomain.GENERAL || profile.primaryDomain() == PrimaryDomain.UNKNOWN,
                "Random input should return GENERAL or UNKNOWN");
    }
}
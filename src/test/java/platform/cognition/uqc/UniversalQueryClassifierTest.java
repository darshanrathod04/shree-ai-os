package platform.cognition.uqc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UniversalQueryClassifier (Shadow Mode).
 * Verifies classification accuracy and performance.
 */
class UniversalQueryClassifierTest {

    private final UniversalQueryClassifier classifier = new UniversalQueryClassifier();

    @Test
    void testGreeting() {
        ClassificationResult result = classifier.classify("Hello");
        assertEquals("GREETING", result.getPredictedIntent());
        assertEquals(QueryCategory.GREETING, result.getQueryCategory());
        assertTrue(result.getConfidence() > 0.7);
        assertTrue(result.getProcessingTimeNanos() < 5_000_000); // < 5ms
    }

    @Test
    void testIdentity() {
        ClassificationResult result = classifier.classify("Who am I");
        assertEquals("WHO_AM_I", result.getPredictedIntent());
        assertEquals(QueryCategory.IDENTITY, result.getQueryCategory());
    }

    @Test
    void testLearningIntent() {
        ClassificationResult result = classifier.classify("Teach me Java");
        assertEquals("START_COURSE", result.getPredictedIntent());
        assertEquals(QueryCategory.LEARNING, result.getQueryCategory());
        assertFalse(result.getEntities().isEmpty());
    }

    @Test
    void testQuizIntent() {
        ClassificationResult result = classifier.classify("Quiz me");
        assertEquals("START_QUIZ", result.getPredictedIntent());
        assertEquals(QueryCategory.QUIZ, result.getQueryCategory());
    }

    @Test
    void testRoadmapIntent() {
        ClassificationResult result = classifier.classify("Create a Java roadmap");
        assertEquals("PLAN", result.getPredictedIntent());
        assertEquals(QueryCategory.ROADMAP, result.getQueryCategory());
    }

    @Test
    void testContinueCommand() {
        ClassificationResult result = classifier.classify("Continue");
        assertEquals("CONTINUE_LESSON", result.getPredictedIntent());
        assertEquals(QueryCategory.LESSON_NAV, result.getQueryCategory());
    }

    @Test
    void testNextCommand() {
        ClassificationResult result = classifier.classify("Next");
        assertEquals("CONTINUE_LESSON", result.getPredictedIntent());
        assertEquals(QueryCategory.LESSON_NAV, result.getQueryCategory());
    }

    @Test
    void testProgrammingQuery() {
        ClassificationResult result = classifier.classify("What is Java");
        assertEquals("TEACH_TOPIC", result.getPredictedIntent());
        assertEquals(QueryCategory.LEARNING, result.getQueryCategory());
    }

    @Test
    void testDebuggingQuery() {
        ClassificationResult result = classifier.classify("Debug this code");
        assertEquals("DEBUG_CODE", result.getPredictedIntent());
        assertEquals(QueryCategory.DEBUGGING, result.getQueryCategory());
    }

    @Test
    void testCodingQuery() {
        ClassificationResult result = classifier.classify("Write a Java program");
        assertEquals("CODE_GENERATION", result.getPredictedIntent());
        assertEquals(QueryCategory.CODING, result.getQueryCategory());
    }

    @Test
    void testExitCommand() {
        ClassificationResult result = classifier.classify("Exit");
        assertEquals("EXIT_COURSE", result.getPredictedIntent());
        assertEquals(QueryCategory.EXIT, result.getQueryCategory());
    }

    @Test
    void testAcknowledgment() {
        ClassificationResult result = classifier.classify("OK");
        assertEquals("ACKNOWLEDGMENT", result.getPredictedIntent());
        assertEquals(QueryCategory.ACKNOWLEDGMENT, result.getQueryCategory());
    }

    @Test
    void testThanks() {
        ClassificationResult result = classifier.classify("Thanks");
        assertEquals("ACKNOWLEDGMENT", result.getPredictedIntent());
        assertEquals(QueryCategory.ACKNOWLEDGMENT, result.getQueryCategory());
    }

    @Test
    void testEmptyInput() {
        ClassificationResult result = classifier.classify("");
        assertEquals("DEFAULT", result.getPredictedIntent());
        assertEquals(QueryCategory.UNKNOWN, result.getQueryCategory());
        assertEquals(0.0, result.getConfidence());
    }

    @Test
    void testNullInput() {
        ClassificationResult result = classifier.classify(null);
        assertEquals("DEFAULT", result.getPredictedIntent());
        assertEquals(QueryCategory.UNKNOWN, result.getQueryCategory());
    }

    @Test
    void testPerformanceUnder5ms() {
        long totalTime = 0;
        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            ClassificationResult result = classifier.classify("What is Java and how does it work");
            totalTime += result.getProcessingTimeNanos();
            assertTrue(result.getProcessingTimeNanos() < 5_000_000,
                    "Processing should be < 5ms but was " + result.getProcessingTimeNanos() / 1_000_000 + "ms");
        }
        long avgTime = totalTime / iterations;
        System.out.println("[UQC TEST] Average classification time: " + avgTime / 1_000_000 + "ms");
    }

    @Test
    void testEntityExtraction() {
        ClassificationResult result = classifier.classify("Teach me Spring Boot");
        assertFalse(result.getEntities().isEmpty());
        boolean hasCourse = result.getEntities().stream()
                .anyMatch(e -> e.getType() == DetectedEntity.EntityType.COURSE);
        assertTrue(hasCourse, "Should detect 'spring boot' as a course entity");
    }

    @Test
    void testMultiActionDetection() {
        ClassificationResult result = classifier.classify("Teach Java and then quiz me");
        assertEquals(QueryCategory.MULTI_ACTION, result.getQueryCategory());
        assertEquals("MULTI_ACTION", result.getPredictedIntent());
    }

    @Test
    void testImmutability() {
        ClassificationResult result = classifier.classify("Hello");
        assertThrows(UnsupportedOperationException.class, () -> result.getEntities().add(null));
        assertThrows(UnsupportedOperationException.class, () -> result.getMatchedRules().add(null));
    }

    @Test
    void testConfidenceBounds() {
        ClassificationResult result = classifier.classify("Hello");
        assertTrue(result.getConfidence() >= 0.0);
        assertTrue(result.getConfidence() <= 1.0);
    }

    @Test
    void testSystemQuery() {
        ClassificationResult result = classifier.classify("What time is it");
        assertEquals("SYSTEM", result.getPredictedIntent());
        assertEquals(QueryCategory.SYSTEM, result.getQueryCategory());
    }

    @Test
    void testMemoryQuery() {
        ClassificationResult result = classifier.classify("Remember what I said");
        assertEquals("MEMORY_RECALL", result.getPredictedIntent());
        assertEquals(QueryCategory.MEMORY, result.getQueryCategory());
    }
}
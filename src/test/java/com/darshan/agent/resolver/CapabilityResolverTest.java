package com.darshan.agent.resolver;

import com.darshan.agent.capability.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Capability Resolver (Shadow Mode).
 * <p>
 * Verifies:
 * - Direct resolution
 * - Priority resolution
 * - Unknown capability
 * - Multiple candidates
 * - Fallback
 * - Thread safety
 * - Performance
 * - Null safety
 * - Immutability
 * - Shadow comparison
 */
class CapabilityResolverTest {

    private CapabilityRegistry registry;
    private CapabilityResolver resolver;

    @BeforeEach
    void setUp() {
        registry = new CapabilityRegistry();
        registry.register(new ChatCapability());
        registry.register(new LearningCapability());
        registry.register(new QuizCapability());
        registry.register(new RoadmapCapability());
        resolver = new CapabilityResolver(registry);
    }

    // ── Direct Resolution ──

    @Test
    void testDirectMatchResolution() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        assertTrue(resolution.isResolved());
        assertEquals("learning", resolution.getSelectedCapability().getName());
        assertEquals(ResolutionStrategy.DIRECT_MATCH, resolution.getStrategy());
        assertEquals("START_COURSE", resolution.getMatchedIntent());
        assertEquals("LEARNING", resolution.getResolvedCategory());
    }

    @Test
    void testDirectMatchForLearningIntent() {
        CapabilityResolution resolution = resolver.resolve("CONTINUE_LESSON");

        assertTrue(resolution.isResolved());
        assertEquals("learning", resolution.getSelectedCapability().getName());
    }

    @Test
    void testDirectMatchForQuizIntent() {
        CapabilityResolution resolution = resolver.resolve("START_QUIZ");

        assertTrue(resolution.isResolved());
        assertEquals("quiz", resolution.getSelectedCapability().getName());
    }

    @Test
    void testDirectMatchForPlanIntent() {
        CapabilityResolution resolution = resolver.resolve("PLAN");

        assertTrue(resolution.isResolved());
        assertEquals("roadmap", resolution.getSelectedCapability().getName());
    }

    @Test
    void testDirectMatchForChatIntent() {
        CapabilityResolution resolution = resolver.resolve("GREETING");

        assertTrue(resolution.isResolved());
        assertEquals("chat", resolution.getSelectedCapability().getName());
    }

    // ── Priority Resolution ──

    @Test
    void testPrioritySelectionWhenMultipleCapabilitiesSupportIntent() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        // LearningCapability (priority 10) should win over any other
        assertEquals("learning", resolution.getSelectedCapability().getName());
    }

    // ── Unknown Capability ──

    @Test
    void testUnknownIntentReturnsDefaultResolution() {
        CapabilityResolution resolution = resolver.resolve("UNKNOWN_INTENT_XYZ");

        // With capabilities registered, best will get ~0.22 which is below MINIMUM_VALID_SCORE
        // Falls into DEFAULT branch -> chat capability selected
        assertTrue(resolution.isResolved());
        assertEquals("chat", resolution.getSelectedCapability().getName());
        assertEquals(ResolutionStrategy.DEFAULT, resolution.getStrategy());
    }

    @Test
    void testNullIntentReturnsUnknownResolution() {
        CapabilityResolution resolution = resolver.resolve(null);

        assertFalse(resolution.isResolved());
        assertEquals(ResolutionStrategy.UNKNOWN, resolution.getStrategy());
        assertNull(resolution.getSelectedCapability());
    }

    @Test
    void testBlankIntentReturnsUnknownResolution() {
        CapabilityResolution resolution = resolver.resolve("   ");

        assertFalse(resolution.isResolved());
        assertEquals(ResolutionStrategy.UNKNOWN, resolution.getStrategy());
    }

    // ── Multiple Candidates ──

    @Test
    void testResolutionContainsAllCandidates() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        List<CapabilityResolution.Candidate> candidates = resolution.getCandidates();
        assertNotNull(candidates);
        assertFalse(candidates.isEmpty());

        // Should contain all registered capabilities
        assertEquals(4, candidates.size());
    }

    @Test
    void testCandidatesSortedByScoreDescending() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        List<CapabilityResolution.Candidate> candidates = resolution.getCandidates();
        for (int i = 0; i < candidates.size() - 1; i++) {
            assertTrue(candidates.get(i).getScore() >= candidates.get(i + 1).getScore(),
                    "Candidates should be sorted by score descending");
        }
    }

    @Test
    void testBestCandidateIsFirstInList() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        CapabilityResolution.Candidate best = resolution.getCandidates().get(0);
        assertEquals(resolution.getSelectedCapability().getName(), best.getCapability().getName());
    }

    // ── Fallback ──

    @Test
    void testFallbackToDefaultWhenNoStrongMatch() {
        // Create a registry with only ChatCapability
        CapabilityRegistry emptyRegistry = new CapabilityRegistry();
        emptyRegistry.register(new ChatCapability());
        CapabilityResolver fallbackResolver = new CapabilityResolver(emptyRegistry);

        // ChatCapability doesn't support "SOME_RANDOM_INTENT"
        // Score will be ~0.22, below MINIMUM_VALID_SCORE, triggers DEFAULT
        CapabilityResolution resolution = fallbackResolver.resolve("SOME_RANDOM_INTENT");

        assertTrue(resolution.isResolved());
        assertEquals("chat", resolution.getSelectedCapability().getName());
        assertEquals(ResolutionStrategy.DEFAULT, resolution.getStrategy());
    }

    // ── Thread Safety ──

    @Test
    void testThreadSafety() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        boolean[] results = new boolean[threadCount * iterationsPerThread];

        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            threads[t] = new Thread(() -> {
                for (int i = 0; i < iterationsPerThread; i++) {
                    try {
                        CapabilityResolution resolution = resolver.resolve("START_COURSE");
                        results[threadIndex * iterationsPerThread + i] =
                                resolution.isResolved()
                                        && "learning".equals(resolution.getSelectedCapability().getName());
                    } catch (Exception e) {
                        results[threadIndex * iterationsPerThread + i] = false;
                    }
                }
            });
            threads[t].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // All results should be true
        for (boolean result : results) {
            assertTrue(result, "Thread-safe resolution should succeed");
        }
    }

    // ── Performance ──

    @Test
    void testPerformanceUnder500MicrosAverage() {
        long totalTime = 0;
        int iterations = 100;

        // Warmup
        resolver.resolve("START_COURSE");

        for (int i = 0; i < iterations; i++) {
            CapabilityResolution resolution = resolver.resolve("START_COURSE");
            totalTime += resolution.getProcessingTimeNanos();
        }

        long avgTime = totalTime / iterations;
        System.out.println("[RESOLVER TEST] Average resolution time: " + avgTime / 1000 + "μs");
        assertTrue(avgTime < 500_000, "Average resolution should be < 500μs but was " + avgTime / 1000 + "μs");
    }

    // ── Null Safety ──

    @Test
    void testNullCapabilityRegistryThrowsOnResolve() {
        CapabilityResolver nullResolver = new CapabilityResolver(null);
        assertThrows(NullPointerException.class, () -> nullResolver.resolve("ANY_INTENT"));
    }

    @Test
    void testEmptyRegistry() {
        CapabilityRegistry emptyRegistry = new CapabilityRegistry();
        CapabilityResolver emptyResolver = new CapabilityResolver(emptyRegistry);

        CapabilityResolution resolution = emptyResolver.resolve("ANY_INTENT");

        assertFalse(resolution.isResolved());
        assertEquals(ResolutionStrategy.UNKNOWN, resolution.getStrategy());
        assertNull(resolution.getSelectedCapability());
    }

    // ── Immutability ──

    @Test
    void testResolutionImmutability() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        // Verify getters return expected values
        assertNotNull(resolution.getSelectedCapability());
        assertNotNull(resolution.getReason());
        assertNotNull(resolution.getStrategy());
        assertNotNull(resolution.getCandidates());
        assertNotNull(resolution.getMatchedIntent());
        assertNotNull(resolution.getResolvedCategory());
        assertNotNull(resolution.getTimestamp());

        // Verify candidates list is unmodifiable
        assertThrows(UnsupportedOperationException.class, () ->
                resolution.getCandidates().add(null));
    }

    @Test
    void testCandidateImmutability() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");
        CapabilityResolution.Candidate candidate = resolution.getCandidates().get(0);

        assertNotNull(candidate.getCapability());
        assertTrue(candidate.getScore() >= 0);
        assertNotNull(candidate.getReason());
    }

    // ── Shadow Comparison ──

    @Test
    void testCompareWithProduction() {
        // This should not throw — shadow mode only
        resolver.compareWithProduction("START_COURSE", null, "LearningSessionEngine");
        resolver.compareWithProduction("GREETING", null, "GreetingSkill");
        resolver.compareWithProduction("UNKNOWN_INTENT", null, "ChatSkill");
    }

    @Test
    void testResolveWithContext() {
        CapabilityContext context = new CapabilityContext(
                "start course java",
                "START_COURSE",
                null, null, null, null, null
        );

        CapabilityResolution resolution = resolver.resolve("START_COURSE", context);

        assertTrue(resolution.isResolved());
        assertEquals("learning", resolution.getSelectedCapability().getName());
    }

    // ── Resolution Metadata ──

    @Test
    void testResolutionContainsProcessingTime() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        assertTrue(resolution.getProcessingTimeNanos() > 0,
                "Processing time should be positive");
    }

    @Test
    void testResolutionContainsReason() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        assertNotNull(resolution.getReason());
        assertFalse(resolution.getReason().isEmpty());
    }

    @Test
    void testResolutionContainsTimestamp() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");

        assertNotNull(resolution.getTimestamp());
    }

    // ── Edge Cases ──

    @Test
    void testCaseInsensitiveIntentMatchingViaScorer() {
        // CapabilityScorer uses equalsIgnoreCase, so "start_course" matches "START_COURSE"
        CapabilityResolution resolution = resolver.resolve("start_course");

        // LearningCapability supports "START_COURSE" which equalsIgnoreCase matches "start_course"
        assertTrue(resolution.isResolved());
        assertEquals("learning", resolution.getSelectedCapability().getName());
    }

    @Test
    void testAllKnownIntentsResolveSuccessfully() {
        List<String> intents = List.of(
                "START_COURSE", "CONTINUE_LESSON", "COMPLETE_LESSON", "CURRENT_LESSON",
                "TEACH_TOPIC", "REPEAT_LESSON", "EXIT_COURSE", "LESSON_PROGRESS",
                "START_QUIZ", "CONTINUE_QUIZ", "SUBMIT_ANSWER", "FINISH_QUIZ", "QUIZ_RESULT",
                "PLAN", "ROADMAP_REQUEST", "NEXT_STEP", "COMPLETE_TASK", "PROGRESS", "CURRENT_TASK",
                "GREETING", "WHO_AM_I", "DEFAULT", "FOLLOW_UP", "SMALL_TALK", "CHAT"
        );

        for (String intent : intents) {
            CapabilityResolution resolution = resolver.resolve(intent);
            assertTrue(resolution.isResolved(),
                    "Intent '" + intent + "' should resolve to a capability");
        }
    }

    @Test
    void testResolutionEqualsAndHashCode() {
        CapabilityResolution res1 = resolver.resolve("START_COURSE");
        CapabilityResolution res2 = resolver.resolve("START_COURSE");

        // Different resolutions should not be equal (different timestamps)
        assertNotEquals(res1, res2);
    }

    @Test
    void testCandidateCompareTo() {
        Capability cap1 = new ChatCapability();
        Capability cap2 = new LearningCapability();

        CapabilityResolution.Candidate c1 = new CapabilityResolution.Candidate(cap1, 0.5, "test");
        CapabilityResolution.Candidate c2 = new CapabilityResolution.Candidate(cap2, 0.8, "test");

        // Higher score should come first (descending order)
        assertTrue(c2.compareTo(c1) < 0);
        assertTrue(c1.compareTo(c2) > 0);
        assertEquals(0, c1.compareTo(
                new CapabilityResolution.Candidate(cap1, 0.5, "other")));
    }

    @Test
    void testResolutionToString() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");
        String str = resolution.toString();

        assertTrue(str.contains("learning"));
        assertTrue(str.contains("START_COURSE"));
    }

    @Test
    void testCandidateToString() {
        CapabilityResolution resolution = resolver.resolve("START_COURSE");
        CapabilityResolution.Candidate candidate = resolution.getCandidates().get(0);
        String str = candidate.toString();

        assertTrue(str.contains("Candidate"));
        assertTrue(str.contains(candidate.getCapability().getName()));
    }

    @Test
    void testEmptyRegistryReturnsUnknown() {
        CapabilityRegistry empty = new CapabilityRegistry();
        CapabilityResolver emptyResolver = new CapabilityResolver(empty);

        CapabilityResolution resolution = emptyResolver.resolve("ANY_INTENT");
        assertFalse(resolution.isResolved());
        assertEquals(ResolutionStrategy.UNKNOWN, resolution.getStrategy());
        assertNull(resolution.getSelectedCapability());
    }
}
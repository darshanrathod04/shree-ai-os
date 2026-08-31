package com.shreeai.os.platform.runtime.reflection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReflectionImportanceScorer}.
 */
class ReflectionImportanceScorerTest {

    private ReflectionImportanceScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new ReflectionImportanceScorer();
    }

    @Test
    void failureHasHighImportance() {
        int score = scorer.score("FAILURE", 0.2, List.of("Error occurred"), List.of());
        assertTrue(score >= 60, "FAILURE should have high importance, got " + score);
    }

    @Test
    void successHasLowerImportance() {
        int score = scorer.score("SUCCESS", 0.9, List.of("All good"), List.of());
        assertTrue(score < 60, "SUCCESS should have lower importance, got " + score);
    }

    @Test
    void partialIsMiddleRange() {
        int score = scorer.score("PARTIAL", 0.5, List.of("Partial"), List.of());
        assertTrue(score >= 30 && score < 80, "PARTIAL should be mid-range, got " + score);
    }

    @Test
    void moreLessonsIncreaseScore() {
        int few = scorer.score("SUCCESS", 0.8, List.of("One"), List.of());
        int many = scorer.score("SUCCESS", 0.8, List.of("One", "Two", "Three", "Four", "Five"), List.of());
        assertTrue(many > few, "More lessons should increase score");
    }

    @Test
    void novelLessonsScoreHigher() {
        List<List<String>> previous = List.of(List.of("Old lesson A"), List.of("Old lesson B"));
        int novel = scorer.score("SUCCESS", 0.8, List.of("Completely new insight"), previous);
        int repeat = scorer.score("SUCCESS", 0.8, List.of("Old lesson A"), previous);
        assertTrue(novel > repeat, "Novel lessons should score higher than repeated ones");
    }

    @Test
    void emptyPreviousLessonsMaximizesNovelty() {
        // FAILURE verdict with unprecedented lessons should be highly important
        int score = scorer.score("FAILURE", 0.2, List.of("Critical error occurred"), List.of());
        assertTrue(score > 50, "With no previous lessons, novelty is maxed for FAILURE");
    }

    @Test
    void scoreIsWithinBounds() {
        for (String verdict : List.of("SUCCESS", "PARTIAL", "FAILURE")) {
            for (double s : List.of(0.0, 0.25, 0.5, 0.75, 1.0)) {
                int score = scorer.score(verdict, s, List.of("L1", "L2"), List.of(List.of("L1")));
                assertTrue(score >= 0 && score <= 100,
                        "Score out of bounds for " + verdict + "/" + s + ": " + score);
            }
        }
    }

    @Test
    void emptyLessonsReducesDensityComponent() {
        int withLessons = scorer.score("FAILURE", 0.3, List.of("A", "B", "C"), List.of());
        int noLessons = scorer.score("FAILURE", 0.3, List.of(), List.of());
        assertTrue(withLessons > noLessons, "Lessons should increase score");
    }
}
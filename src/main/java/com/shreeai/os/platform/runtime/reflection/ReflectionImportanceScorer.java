package com.shreeai.os.platform.runtime.reflection;

import java.util.List;
import java.util.Objects;

/**
 * <b>ReflectionImportanceScorer</b>
 *
 * <p>Deterministic importance scorer that assigns a 0–100 importance score to
 * a reflection record. The score is computed from multiple weighted signals:</p>
 *
 * <ul>
 *   <li><b>Verdict weight</b> (40%): FAILURE=100, PARTIAL=60, SUCCESS=30</li>
 *   <li><b>Score delta</b> (20%): distance from neutral (0.5) amplifies importance</li>
 *   <li><b>Lesson density</b> (20%): more lessons = more actionable = more important</li>
 *   <li><b>Recurrence penalty</b> (20%): repeated similar lessons reduce novelty</li>
 * </ul>
 *
 * <p>The scorer is deterministic and LLM-free. It does not mutate any state.</p>
 *
 * <p><b>Ownership:</b> Runtime — Reflection Intelligence Layer</p>
 * <p><b>Version:</b> 1.0</p>
 */
public final class ReflectionImportanceScorer {

    private static final double VERDICT_WEIGHT = 0.40;
    private static final double SCORE_DELTA_WEIGHT = 0.20;
    private static final double LESSON_DENSITY_WEIGHT = 0.20;
    private static final double NOVELTY_WEIGHT = 0.20;

    private static final int FAILURE_BASE = 100;
    private static final int PARTIAL_BASE = 60;
    private static final int SUCCESS_BASE = 30;

    private static final int MAX_EXPECTED_LESSONS = 5;
    private static final double NEUTRAL_SCORE = 0.5;

    /**
     * Computes the importance score for a reflection.
     *
     * @param verdict          the reflection verdict (never null)
     * @param score            the quality score (0.0–1.0)
     * @param lessons          the extracted lessons (never null)
     * @param previousLessons  previous lessons for novelty comparison (may be empty, never null)
     * @return importance score (0–100)
     */
    public int score(
            String verdict,
            double score,
            List<String> lessons,
            List<List<String>> previousLessons
    ) {
        Objects.requireNonNull(verdict, "verdict must not be null");
        Objects.requireNonNull(lessons, "lessons must not be null");
        Objects.requireNonNull(previousLessons, "previousLessons must not be null");

        double verdictComponent = verdictComponent(verdict) * VERDICT_WEIGHT;
        double deltaComponent = scoreDeltaComponent(score) * SCORE_DELTA_WEIGHT;
        double densityComponent = lessonDensityComponent(lessons) * LESSON_DENSITY_WEIGHT;
        double noveltyComponent = noveltyComponent(lessons, previousLessons) * NOVELTY_WEIGHT;

        double raw = verdictComponent + deltaComponent + densityComponent + noveltyComponent;

        return clamp(Math.round(raw));
    }

    private double verdictComponent(String verdict) {
        return switch (verdict) {
            case "FAILURE" -> FAILURE_BASE;
            case "PARTIAL" -> PARTIAL_BASE;
            case "SUCCESS" -> SUCCESS_BASE;
            default -> 20;
        };
    }

    private double scoreDeltaComponent(double score) {
        double delta = Math.abs(score - NEUTRAL_SCORE);
        return delta * 100.0;
    }

    private double lessonDensityComponent(List<String> lessons) {
        if (lessons.isEmpty()) {
            return 0.0;
        }
        double ratio = Math.min(lessons.size(), MAX_EXPECTED_LESSONS) / (double) MAX_EXPECTED_LESSONS;
        return ratio * 100.0;
    }

    private double noveltyComponent(List<String> lessons, List<List<String>> previousLessons) {
        if (lessons.isEmpty()) {
            return 0.0;
        }
        if (previousLessons.isEmpty()) {
            return 100.0;
        }

        long novelCount = lessons.stream()
                .filter(lesson -> previousLessons.stream()
                        .noneMatch(prev -> prev.stream()
                                .anyMatch(p -> similarity(p, lesson) > 0.8)))
                .count();

        return (novelCount / (double) lessons.size()) * 100.0;
    }

    /**
     * Simple similarity: Jaccard index on lowercased tokens.
     */
    private double similarity(String a, String b) {
        if (a == null || b == null || a.isBlank() || b.isBlank()) {
            return 0.0;
        }
        var tokensA = tokenize(a);
        var tokensB = tokenize(b);
        if (tokensA.isEmpty() || tokensB.isEmpty()) {
            return 0.0;
        }
        var intersection = new java.util.HashSet<>(tokensA);
        intersection.retainAll(tokensB);
        var union = new java.util.HashSet<>(tokensA);
        union.addAll(tokensB);
        return union.isEmpty() ? 0.0 : intersection.size() / (double) union.size();
    }

    private java.util.Set<String> tokenize(String text) {
        return java.util.Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(s -> !s.isBlank())
                .collect(java.util.HashSet::new, java.util.HashSet::add, java.util.HashSet::addAll);
    }

    private int clamp(long value) {
        return (int) Math.max(0, Math.min(100, value));
    }
}
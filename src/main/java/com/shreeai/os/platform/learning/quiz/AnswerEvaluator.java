package com.shreeai.os.platform.learning.quiz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Pure deterministic answer evaluator.
 * CRITICAL RULES:
 * - NEVER calls Ollama or any LLM.
 * - Compares answers exactly using Java equality.
 * - Returns boolean (correct/incorrect) and the correct answer for display.
 *
 * Supports:
 * - MCQ: compares integer index (or string option text)
 * - TRUE_FALSE: compares boolean
 * - FILL_BLANK: case-insensitive string comparison after trimming
 * - CODING: future — stub for now
 */
@Component
public class AnswerEvaluator {

    private static final Logger log = LoggerFactory.getLogger(AnswerEvaluator.class);

    public AnswerEvaluator() {
        log.info("[EVALUATION] AnswerEvaluator initialized");
    }

    /**
     * Evaluate whether the submitted answer matches the correct answer.
     *
     * @param question       the quiz question (provides type and correct answer)
     * @param submittedAnswer the user's submitted answer
     * @return EvaluationResult with correctness and details
     */
    public EvaluationResult evaluate(QuizQuestion question, Object submittedAnswer) {
        Objects.requireNonNull(question, "question must not be null");

        if (submittedAnswer == null) {
            log.warn("[EVALUATION] Null answer submitted for question '{}'", question.getId());
            return new EvaluationResult(false, question.getCorrectAnswer(),
                    "No answer provided.", question.getExplanation());
        }

        boolean correct = switch (question.getType()) {
            case MCQ -> evaluateMcq(question.getCorrectAnswer(), submittedAnswer);
            case TRUE_FALSE -> evaluateTrueFalse(question.getCorrectAnswer(), submittedAnswer);
            case FILL_BLANK -> evaluateFillBlank(question.getCorrectAnswer(), submittedAnswer);
            case CODING -> evaluateCoding(question, submittedAnswer);
        };

        log.info("[EVALUATION] Question='{}' type={} correct={} answer='{}'",
                question.getId(), question.getType(), correct, submittedAnswer);

        String feedback = correct
                ? "Correct!"
                : "Incorrect. The correct answer is: " + formatAnswer(question.getCorrectAnswer());

        return new EvaluationResult(correct, question.getCorrectAnswer(), feedback, question.getExplanation());
    }

    private boolean evaluateMcq(Object correctAnswer, Object submitted) {
        // MCQ correctAnswer is stored as integer index in JSON (0-based)
        // submitted could be integer or string option text
        if (correctAnswer instanceof Number correctNum && submitted instanceof Number submittedNum) {
            return correctNum.intValue() == submittedNum.intValue();
        }
        if (correctAnswer instanceof Number correctNum && submitted instanceof String submittedStr) {
            // submitted as option text — compare against correct answer index
            return false; // string vs int mismatch
        }
        return Objects.equals(correctAnswer, submitted);
    }

    private boolean evaluateTrueFalse(Object correctAnswer, Object submitted) {
        if (correctAnswer instanceof Boolean correctBool && submitted instanceof Boolean submittedBool) {
            return correctBool == submittedBool;
        }
        // Handle string representations
        if (submitted instanceof String submittedStr) {
            boolean submittedBool = "true".equalsIgnoreCase(submittedStr.trim());
            boolean correctBool = correctAnswer instanceof Boolean
                    ? (Boolean) correctAnswer
                    : Boolean.parseBoolean(String.valueOf(correctAnswer));
            return submittedBool == correctBool;
        }
        return Objects.equals(correctAnswer, submitted);
    }

    private boolean evaluateFillBlank(Object correctAnswer, Object submitted) {
        String correct = String.valueOf(correctAnswer).trim().toLowerCase();
        String submittedStr = String.valueOf(submitted).trim().toLowerCase();
        return correct.equals(submittedStr);
    }

    private boolean evaluateCoding(QuizQuestion question, Object submitted) {
        // Stub for future coding quiz evaluation
        log.warn("[EVALUATION] CODING question type not yet implemented for '{}'", question.getId());
        return false;
    }

    private String formatAnswer(Object answer) {
        if (answer instanceof Boolean) {
            return (Boolean) answer ? "True" : "False";
        }
        if (answer instanceof Number) {
            return String.valueOf(answer);
        }
        return String.valueOf(answer);
    }

    /**
     * Immutable result of a single answer evaluation.
     */
    public static final class EvaluationResult {
        private final boolean correct;
        private final Object expectedAnswer;
        private final String feedback;
        private final String explanation;

        public EvaluationResult(boolean correct, Object expectedAnswer,
                                String feedback, String explanation) {
            this.correct = correct;
            this.expectedAnswer = expectedAnswer;
            this.feedback = feedback;
            this.explanation = explanation != null ? explanation : "";
        }

        public boolean isCorrect() { return correct; }
        public Object getExpectedAnswer() { return expectedAnswer; }
        public String getFeedback() { return feedback; }
        public String getExplanation() { return explanation; }
    }
}
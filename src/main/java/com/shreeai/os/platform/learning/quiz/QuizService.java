package com.shreeai.os.platform.learning.quiz;

import com.shreeai.os.platform.learning.adaptive.AdaptiveLearningEngine;
import com.shreeai.os.platform.learning.adaptive.StudentLearningProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for quiz business logic.
 * Handles quiz lifecycle: start, answer submission, finish, result computation.
 * Integrates with AdaptiveLearningEngine after quiz completion.
 *
 * Repository → Service → Engine architecture.
 */
@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    private final QuizRepository quizRepository;
    private final AnswerEvaluator answerEvaluator;
    private final AdaptiveLearningEngine adaptiveEngine;

    public QuizService(QuizRepository quizRepository,
                       AnswerEvaluator answerEvaluator,
                       AdaptiveLearningEngine adaptiveEngine) {
        this.quizRepository = quizRepository;
        this.answerEvaluator = answerEvaluator;
        this.adaptiveEngine = adaptiveEngine;
        log.info("[QUIZ] QuizService initialized");
    }

    /**
     * Start a quiz for a given course chapter.
     * Loads questions from curriculum and initializes QuizSession.
     *
     * @param quizSession   the per-session QuizSession to initialize
     * @param courseName    the course name
     * @param chapterNumber the 1-based chapter number
     * @return a formatted string with the first question, or error message
     */
    public String startQuiz(QuizSession quizSession, String courseName, int chapterNumber) {
        // Load questions from curriculum
        List<QuizQuestion> questions = quizRepository.loadQuizQuestions(courseName, chapterNumber);
        if (questions.isEmpty()) {
            log.warn("[QUIZ] No quiz available for {}/chapter{}", courseName, chapterNumber);
            return "No quiz available for this chapter yet.";
        }

        String quizTitle = quizRepository.loadQuizTitle(courseName, chapterNumber)
                .orElse("Chapter " + chapterNumber + " Quiz");

        // Start session
        quizSession.startQuiz(quizTitle, courseName, chapterNumber, questions);

        log.info("[QUIZ] Started '{}' for {}/chapter{} ({} questions)",
                quizTitle, courseName, chapterNumber, questions.size());

        return formatQuestion(quizSession.getCurrentQuestion(), 1, questions.size());
    }

    /**
     * Submit an answer for the current question.
     *
     * @param quizSession    the per-session QuizSession
     * @param submittedAnswer the user's answer
     * @return formatted result with feedback, or quiz completion summary
     */
    public String submitAnswer(QuizSession quizSession, Object submittedAnswer) {
        if (!quizSession.hasActiveQuiz()) {
            return "No active quiz. Say 'start quiz' to begin one.";
        }

        QuizQuestion currentQuestion = quizSession.getCurrentQuestion();
        if (currentQuestion == null) {
            return "Quiz is already completed. Say 'start quiz' to try again.";
        }

        // Evaluate the answer
        AnswerEvaluator.EvaluationResult evaluation = answerEvaluator.evaluate(currentQuestion, submittedAnswer);

        // Record the attempt
        boolean hasMore = quizSession.submitAnswer(submittedAnswer, evaluation.isCorrect());

        log.info("[QUIZ] Answer submitted: question='{}' correct={} remaining={}",
                currentQuestion.getId(), evaluation.isCorrect(), quizSession.getRemainingQuestions());

        StringBuilder sb = new StringBuilder();
        sb.append(evaluation.getFeedback()).append("\n\n");
        if (!evaluation.getExplanation().isBlank()) {
            sb.append("💡 ").append(evaluation.getExplanation()).append("\n\n");
        }

        if (hasMore) {
            // Show next question
            QuizQuestion nextQuestion = quizSession.getCurrentQuestion();
            sb.append("---\n");
            sb.append(formatQuestion(nextQuestion,
                    quizSession.getCurrentQuestionIndex() + 1,
                    quizSession.getTotalQuestions()));
        } else {
            // Quiz complete — compute result
            sb.append(formatQuizCompletion(quizSession));
        }

        return sb.toString();
    }

    /**
     * Finish the quiz early and compute the result.
     *
     * @param quizSession the per-session QuizSession
     * @return formatted quiz result
     */
    public String finishQuiz(QuizSession quizSession) {
        if (!quizSession.hasActiveQuiz()) {
            return "No active quiz to finish.";
        }

        quizSession.finish();
        log.info("[QUIZ] Quiz finished early for '{}'", quizSession.getQuizTitle());

        return formatQuizCompletion(quizSession);
    }

    /**
     * Compute the final QuizResult from the session's attempts.
     * Integrates with AdaptiveLearningEngine.
     */
    public QuizResult computeResult(QuizSession quizSession, StudentLearningProfile profile) {
        List<QuizAttempt> attempts = quizSession.getAttempts();
        int total = quizSession.getTotalQuestions();
        int correct = (int) attempts.stream().filter(QuizAttempt::isCorrect).count();

        // Derive weak/strong topics from wrong answers
        List<String> weakTopics = new ArrayList<>();
        List<String> strongTopics = new ArrayList<>();

        for (QuizAttempt attempt : attempts) {
            if (!attempt.isCorrect()) {
                weakTopics.add(quizSession.getQuizTitle() + " - Q" + attempt.getQuestionId());
            }
        }

        if (correct >= total * 0.8) {
            strongTopics.add(quizSession.getQuizTitle());
        }

        QuizResult result = new QuizResult(
                quizSession.getQuizTitle(),
                total,
                correct,
                weakTopics,
                strongTopics
        );

        log.info("[ASSESSMENT] QuizResult: title='{}' score={}% passed={} weak={} strong={}",
                result.getQuizTitle(),
                String.format("%.1f", result.getPercentage()),
                result.isPassed(),
                weakTopics.size(),
                strongTopics.size());

        // Integrate with AdaptiveLearningEngine
        if (profile != null) {
            double topicScore = result.getPercentage();
            // Record for each weak/strong topic
            for (String weak : weakTopics) {
                adaptiveEngine.recordQuizScore(profile, topicScore, weak);
            }
            if (weakTopics.isEmpty() && strongTopics.isEmpty()) {
                // No specific topic — record under quiz title
                adaptiveEngine.recordQuizScore(profile, topicScore, quizSession.getQuizTitle());
            }
            for (String strong : strongTopics) {
                adaptiveEngine.recordQuizScore(profile, topicScore, strong);
            }
            log.info("[ASSESSMENT] Adaptive profile updated with quiz score: {}%",
                    String.format("%.1f", result.getPercentage()));
        }

        return result;
    }

    /**
     * Get the current question for display without submitting.
     */
    public String getCurrentQuestion(QuizSession quizSession) {
        if (!quizSession.hasActiveQuiz()) {
            return null;
        }
        QuizQuestion question = quizSession.getCurrentQuestion();
        if (question == null) {
            return null;
        }
        return formatQuestion(question,
                quizSession.getCurrentQuestionIndex() + 1,
                quizSession.getTotalQuestions());
    }

    // ================================================================
    // Formatting helpers
    // ================================================================

    private String formatQuestion(QuizQuestion question, int questionNumber, int totalQuestions) {
        if (question == null) {
            return "No more questions.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("❓ **Question ").append(questionNumber).append("/").append(totalQuestions).append("**\n\n");
        sb.append(question.getQuestion()).append("\n\n");

        if (!question.getOptions().isEmpty()) {
            for (int i = 0; i < question.getOptions().size(); i++) {
                sb.append(i + 1).append(". ").append(question.getOptions().get(i)).append("\n");
            }
            sb.append("\nReply with the option number (1-").append(question.getOptions().size())
                    .append(") or the answer text.\n");
        } else {
            sb.append("Type your answer.\n");
        }

        sb.append("Say **'finish quiz'** to end early.");

        return sb.toString();
    }

    private String formatQuizCompletion(QuizSession quizSession) {
        // Compute result without adaptive integration (profile may be null here)
        List<QuizAttempt> attempts = quizSession.getAttempts();
        int total = quizSession.getTotalQuestions();
        int correct = (int) attempts.stream().filter(QuizAttempt::isCorrect).count();
        double percentage = total > 0 ? (double) correct / total * 100.0 : 0.0;
        boolean passed = percentage >= 60.0;

        StringBuilder sb = new StringBuilder();
        sb.append("📊 **Quiz Complete!**\n\n");
        sb.append("**").append(quizSession.getQuizTitle()).append("**\n\n");
        sb.append("✅ Correct: ").append(correct).append("/").append(total).append("\n");
        sb.append("❌ Incorrect: ").append(total - correct).append("\n");
        sb.append("📈 Score: **").append(String.format("%.1f", percentage)).append("%**\n");
        sb.append(passed ? "✅ **PASSED**" : "❌ **NEEDS REVIEW**").append("\n\n");

        if (passed) {
            sb.append("Great job! You can continue to the next lesson.\n");
        } else {
            sb.append("You may want to revise this chapter before proceeding.\n");
        }

        log.info("[QUIZ] Completed '{}' — {}% {}",
                quizSession.getQuizTitle(),
                String.format("%.1f", percentage),
                passed ? "PASSED" : "FAILED");

        return sb.toString();
    }
}
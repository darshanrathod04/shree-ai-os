package com.shreeai.os.platform.legacy.learning.quiz;

import com.shreeai.os.platform.legacy.learning.adaptive.StudentLearningProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * QuizEngine is the main entry point for quiz operations.
 * Responsibilities:
 * - Start quiz
 * - Next question
 * - Submit answer
 * - Finish quiz
 * - Return QuizResult
 *
 * Delegates to QuizService for business logic.
 * Integrates with AdaptiveLearningEngine via QuizService.
 *
 * Repository → Service → Engine architecture.
 */
@Component
public class QuizEngine {

    private static final Logger log = LoggerFactory.getLogger(QuizEngine.class);

    private final QuizService quizService;

    public QuizEngine(QuizService quizService) {
        this.quizService = quizService;
        log.info("[QUIZ] QuizEngine initialized");
    }

    /**
     * Start a quiz for a given course chapter.
     *
     * @param quizSession   the per-session QuizSession
     * @param courseName    the course name
     * @param chapterNumber the 1-based chapter number
     * @return formatted first question or error message
     */
    public String startQuiz(QuizSession quizSession, String courseName, int chapterNumber) {
        log.info("[QUIZ] Starting quiz: course='{}' chapter={}", courseName, chapterNumber);
        return quizService.startQuiz(quizSession, courseName, chapterNumber);
    }

    /**
     * Submit an answer for the current question.
     *
     * @param quizSession    the per-session QuizSession
     * @param submittedAnswer the user's answer
     * @return formatted feedback and next question, or completion summary
     */
    public String submitAnswer(QuizSession quizSession, Object submittedAnswer) {
        log.info("[QUIZ] Submitting answer for question index={}",
                quizSession.getCurrentQuestionIndex() + 1);
        return quizService.submitAnswer(quizSession, submittedAnswer);
    }

    /**
     * Finish the quiz early and compute the result.
     *
     * @param quizSession the per-session QuizSession
     * @return formatted quiz result
     */
    public String finishQuiz(QuizSession quizSession) {
        log.info("[QUIZ] Finishing quiz early: '{}'", quizSession.getQuizTitle());
        return quizService.finishQuiz(quizSession);
    }

    /**
     * Compute the final QuizResult and integrate with AdaptiveLearningEngine.
     *
     * @param quizSession the per-session QuizSession
     * @param profile     the per-session StudentLearningProfile
     * @return QuizResult with full assessment data
     */
    public QuizResult getResult(QuizSession quizSession, StudentLearningProfile profile) {
        log.info("[ASSESSMENT] Computing result for '{}'", quizSession.getQuizTitle());
        return quizService.computeResult(quizSession, profile);
    }

    /**
     * Get the current question for display without submitting.
     *
     * @param quizSession the per-session QuizSession
     * @return formatted current question, or null if no active quiz
     */
    public String getCurrentQuestion(QuizSession quizSession) {
        return quizService.getCurrentQuestion(quizSession);
    }

    /**
     * Check if there is an active quiz in the session.
     */
    public boolean hasActiveQuiz(QuizSession quizSession) {
        return quizSession != null && quizSession.hasActiveQuiz();
    }
}
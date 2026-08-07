package com.shreeai.os.platform.production;

import com.shreeai.os.platform.autonomy.GoalManager;
import com.shreeai.os.platform.context.ConversationSession;
import com.shreeai.os.platform.learning.CourseState;
import com.shreeai.os.platform.learning.quiz.QuizSession;
import com.shreeai.os.platform.planning.AutonomousPlanningEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ContextResolutionEngine determines the current active context.
 * It examines all session state and produces a single ResolvedContext
 * that becomes the source of truth for routing decisions.
 *
 * Inputs: ConversationSession, ConversationContext, CourseState,
 *         QuizSession, ExecutionPlan, GoalManager
 * Output: ResolvedContext
 *
 * Logs: [CONTEXT]
 */
@Component
public class ContextResolutionEngine {

    private static final Logger log = LoggerFactory.getLogger(ContextResolutionEngine.class);

    private final AutonomousPlanningEngine planningEngine;
    private final GoalManager goalManager;

    public ContextResolutionEngine(AutonomousPlanningEngine planningEngine,
                                    GoalManager goalManager) {
        this.planningEngine = planningEngine;
        this.goalManager = goalManager;
        log.info("[CONTEXT] ContextResolutionEngine initialized");
    }

    /**
     * Resolve the current context from session state.
     * Priority: QUIZ > LEARNING > ROADMAP > PLANNING > AUTONOMOUS > GREETING > CHAT
     */
    public ResolvedContext resolve(ConversationSession session) {
        if (session == null) {
            log.warn("[CONTEXT] Null session — returning CHAT default");
            return new ResolvedContext(ResolvedContext.Mode.CHAT, "", 0, 0,
                    false, false, "", false);
        }

        CourseState courseState = session.getCourseState();
        QuizSession quizSession = session.getQuizSession();
        boolean hasActiveCourse = courseState != null && courseState.hasActiveCourse();
        boolean hasActiveQuiz = quizSession != null && quizSession.hasActiveQuiz();
        boolean hasActiveRoadmap = planningEngine.getActivePlan().isPresent();
        String goalName = goalManager.hasGoal() && goalManager.getGoal() != null
                ? goalManager.getGoal().getDescription() : "";

        // Priority 1: Active quiz overrides everything
        if (hasActiveQuiz) {
            String courseName = quizSession.getCourseName() != null
                    ? quizSession.getCourseName() : "";
            int chapter = quizSession.getChapterNumber();
            ResolvedContext ctx = new ResolvedContext(
                    ResolvedContext.Mode.QUIZ, courseName, chapter, 0,
                    true, hasActiveRoadmap, goalName, false);
            log.info("[CONTEXT] Resolved: QUIZ mode — course='{}' chapter={}", courseName, chapter);
            return ctx;
        }

        // Priority 2: Active course learning
        if (hasActiveCourse) {
            String courseName = courseState.getCourseName();
            int chapter = courseState.getCurrentChapterIndex() + 1;
            int lesson = courseState.getCurrentLessonIndex() + 1;
            boolean complete = courseState.isCompleted();
            ResolvedContext ctx = new ResolvedContext(
                    ResolvedContext.Mode.LEARNING, courseName, chapter, lesson,
                    false, hasActiveRoadmap, goalName, complete);
            log.info("[CONTEXT] Resolved: LEARNING mode — course='{}' ch={} lesson={}",
                    courseName, chapter, lesson);
            return ctx;
        }

        // Priority 3: Active roadmap
        if (hasActiveRoadmap) {
            String planName = planningEngine.getActivePlan()
                    .map(p -> p.getGoalName()).orElse("");
            ResolvedContext ctx = new ResolvedContext(
                    ResolvedContext.Mode.ROADMAP, "", 0, 0,
                    false, true, goalName, false);
            log.info("[CONTEXT] Resolved: ROADMAP mode — goal='{}'", planName);
            return ctx;
        }

        // Priority 4: Active autonomous goal
        if (!goalName.isBlank()) {
            ResolvedContext ctx = new ResolvedContext(
                    ResolvedContext.Mode.AUTONOMOUS, "", 0, 0,
                    false, false, goalName, false);
            log.info("[CONTEXT] Resolved: AUTONOMOUS mode — goal='{}'", goalName);
            return ctx;
        }

        // Default: CHAT
        log.info("[CONTEXT] Resolved: CHAT mode (no active context)");
        return new ResolvedContext(ResolvedContext.Mode.CHAT, "", 0, 0,
                false, false, "", false);
    }
}
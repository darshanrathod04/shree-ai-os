package com.shreeai.os.platform.production;

import com.shreeai.os.platform.context.ConversationContext;
import com.shreeai.os.platform.context.ConversationSession;
import com.shreeai.os.platform.context.LessonState;
import com.shreeai.os.platform.learning.CourseState;
import com.shreeai.os.platform.learning.LearningSessionEngine;
import com.shreeai.os.platform.learning.adaptive.StudentLearningProfile;
import com.shreeai.os.platform.learning.quiz.QuizSession;
import com.shreeai.os.platform.planning.AutonomousPlanningEngine;
import com.shreeai.os.platform.skills.ChatSkill;
import com.shreeai.os.platform.skills.GreetingSkill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ResponseRouter routes intents to the correct handler based on
 * the resolved context. No routing logic should remain scattered
 * across AgentBrain.
 *
 * Handlers:
 * - GreetingSkill → GREETING
 * - ChatSkill → CHAT, DEFAULT, FOLLOW_UP, general knowledge
 * - LearningSessionEngine → START_COURSE, CONTINUE_LESSON, COMPLETE_LESSON, etc.
 * - QuizEngine → START_QUIZ, CONTINUE_QUIZ, SUBMIT_ANSWER
 * - PlanningEngine → PLAN, NEXT_STEP, COMPLETE_TASK, PROGRESS, CURRENT_TASK
 * - Identity → WHO_AM_I
 *
 * Logs: [ROUTER]
 */
@Component
public class ResponseRouter {

    private static final Logger log = LoggerFactory.getLogger(ResponseRouter.class);

    private final GreetingSkill greetingSkill;
    private final ChatSkill chatSkill;
    private final LearningSessionEngine learningSessionEngine;
    private final AutonomousPlanningEngine planningEngine;
    private final FallbackEngine fallbackEngine;
    private final ContextResolutionEngine contextResolver;

    public ResponseRouter(GreetingSkill greetingSkill,
                           ChatSkill chatSkill,
                           LearningSessionEngine learningSessionEngine,
                           AutonomousPlanningEngine planningEngine,
                           FallbackEngine fallbackEngine,
                           ContextResolutionEngine contextResolver) {
        this.greetingSkill = greetingSkill;
        this.chatSkill = chatSkill;
        this.learningSessionEngine = learningSessionEngine;
        this.planningEngine = planningEngine;
        this.fallbackEngine = fallbackEngine;
        this.contextResolver = contextResolver;
        log.info("[ROUTER] ResponseRouter initialized");
    }

    /**
     * Route an intent to the correct handler.
     *
     * @param intent         the detected intent
     * @param input          the original user input
     * @param context        the conversation context
     * @param session        the conversation session (for state access)
     * @param courseState    the course state
     * @param lessonState    the lesson state
     * @param quizSession    the quiz session
     * @param learningProfile the learning profile
     * @return the response string
     */
    public String route(String intent, String input, ConversationContext context,
                         ConversationSession session, CourseState courseState,
                         LessonState lessonState, QuizSession quizSession,
                         StudentLearningProfile learningProfile) {

        ResolvedContext resolved = contextResolver.resolve(session);
        log.info("[ROUTER] Routing intent='{}' mode={}", intent, resolved.getMode());

        // =========================================================
        // GREETING
        // =========================================================
        if ("GREETING".equals(intent)) {
            log.info("[ROUTER] → GreetingSkill");
            return greetingSkill.execute(input, context);
        }

        // =========================================================
        // IDENTITY
        // =========================================================
        if ("WHO_AM_I".equals(intent)) {
            log.info("[ROUTER] → ChatSkill (identity)");
            return chatSkill.execute(input, context);
        }

        // =========================================================
        // QUIZ INTENTS
        // =========================================================
        if ("START_QUIZ".equals(intent) || "CONTINUE_QUIZ".equals(intent)) {
            if (courseState != null && courseState.hasActiveCourse()) {
                log.info("[ROUTER] → LearningSessionEngine.startQuiz");
                return learningSessionEngine.startQuiz(courseState, quizSession);
            }
            log.info("[ROUTER] → Fallback (no active course for quiz)");
            return fallbackEngine.fallback(input, context, intent,
                    "No active course for quiz");
        }

        if ("SUBMIT_ANSWER".equals(intent)) {
            if (quizSession != null && quizSession.hasActiveQuiz()) {
                log.info("[ROUTER] → LearningSessionEngine.submitQuizAnswer");
                return learningSessionEngine.submitQuizAnswer(quizSession, input);
            }
            return "No active quiz. Say 'start quiz' to begin.";
        }

        if ("FINISH_QUIZ".equals(intent)) {
            if (quizSession != null && quizSession.hasActiveQuiz()) {
                log.info("[ROUTER] → LearningSessionEngine.finishQuiz");
                return learningSessionEngine.finishQuiz(quizSession, learningProfile);
            }
            return "No active quiz to finish.";
        }

        // =========================================================
        // COURSE LEARNING INTENTS
        // =========================================================
        if ("START_COURSE".equals(intent)) {
            log.info("[ROUTER] → LearningSessionEngine.startCourse");
            String courseName = input.replaceFirst("(?i)(start course|begin course|start learning|"
                    + "enroll in course|today we learn|today we will learn|"
                    + "i want to learn|teach me|teach me about|start|begin|learn|teach)\\s+", "").trim();
            if (courseName.isEmpty() || isGenericCoursePhrase(courseName)) {
                return "Which course would you like to start? Available: java, spring-boot";
            }
            return learningSessionEngine.startCourse(courseName, courseState);
        }

        if ("CONTINUE_LESSON".equals(intent)) {
            if (courseState != null && courseState.hasActiveCourse()) {
                log.info("[ROUTER] → LearningSessionEngine.continueLesson");
                return learningSessionEngine.continueLesson(courseState);
            }
            // Fallback: if no active course, treat as general chat
            if (fallbackEngine.isGeneralKnowledgeQuestion(input)) {
                log.info("[ROUTER] → Fallback (general knowledge)");
                return fallbackEngine.fallback(input, context, intent,
                        "No active course for continue");
            }
            return "No active course. Say 'start course <name>' to begin.";
        }

        if ("COMPLETE_LESSON".equals(intent)) {
            if (courseState != null && courseState.hasActiveCourse()) {
                log.info("[ROUTER] → LearningSessionEngine.completeLesson");
                return learningSessionEngine.completeLesson(courseState);
            }
            return "No active course. Say 'start course <name>' to begin.";
        }

        if ("CURRENT_LESSON".equals(intent)) {
            if (courseState != null && courseState.hasActiveCourse()) {
                log.info("[ROUTER] → LearningSessionEngine.currentLesson");
                return learningSessionEngine.currentLesson(courseState);
            }
            return "No active course. Say 'start course <name>' to begin.";
        }

        if ("TEACH_TOPIC".equals(intent)) {
            if (courseState != null && courseState.hasActiveCourse()) {
                log.info("[ROUTER] → LearningSessionEngine.teachTopic");
                String topic = input.replaceFirst("(?i)(explain|what is|what's|what are|"
                        + "tell me about|describe|how does|how do|what does)\\s+", "").trim();
                if (topic.isEmpty()) topic = input;
                return learningSessionEngine.teachTopic(topic, courseState);
            }
            // Fallback: general knowledge question → ChatSkill
            log.info("[ROUTER] → Fallback (no active course, general knowledge)");
            return fallbackEngine.fallback(input, context, intent,
                    "No active course for teach topic");
        }

        if ("REPEAT_LESSON".equals(intent)) {
            if (courseState != null && courseState.hasActiveCourse()) {
                log.info("[ROUTER] → LearningSessionEngine.repeatLesson");
                return learningSessionEngine.repeatLesson(courseState);
            }
            return "No active course. Say 'start course <name>' to begin.";
        }

        if ("LESSON_PROGRESS".equals(intent)) {
            if (courseState != null && courseState.hasActiveCourse()) {
                log.info("[ROUTER] → LearningSessionEngine.progress");
                return learningSessionEngine.progress(courseState);
            }
            return "No active course.";
        }

        if ("EXIT_COURSE".equals(intent)) {
            if (courseState != null && courseState.hasActiveCourse()) {
                log.info("[ROUTER] → LearningSessionEngine.exitCourse");
                return learningSessionEngine.exitCourse(courseState);
            }
            return "No active course to exit.";
        }

        // =========================================================
        // ROADMAP / PLANNING INTENTS
        // =========================================================
        if ("PLAN".equals(intent) || "ROADMAP_REQUEST".equals(intent)) {
            log.info("[ROUTER] → PlanningEngine (plan)");
            String planDescription = input.replaceFirst(
                    "(?i)(i want to become a|become a|plan|roadmap|career path|"
                    + "learning path|steps to|how do i become|how to become)\\s*", "").trim();
            if (planDescription.isEmpty()) planDescription = input;
            var plan = planningEngine.generatePlan(planDescription);
            return "📋 **Roadmap Created**\n\n" + planningEngine.getPlanSummary()
                    + "\n\nI've broken this down into milestones and tasks.";
        }

        if ("NEXT_STEP".equals(intent)) {
            log.info("[ROUTER] → PlanningEngine (next step)");
            var activePlanOpt = planningEngine.getActivePlan();
            if (activePlanOpt.isEmpty()) {
                return "No active roadmap. Say 'plan: <your goal>' to create one.";
            }
            var plan = activePlanOpt.get();
            var allTasks = plan.getAllTasks();
            var nextTaskOpt = allTasks.stream()
                    .filter(t -> !t.isCompleted() && !t.isBlocked())
                    .findFirst();
            if (nextTaskOpt.isEmpty()) {
                return "All tasks completed! 🎉 Your roadmap is done.";
            }
            var nextTask = nextTaskOpt.get();
            return "📋 **Next Task**\n\n**" + nextTask.getTitle() + "**\n"
                    + nextTask.getDescription() + "\n\n⏱️ Estimated: "
                    + (int) nextTask.getEstimatedHours() + " hours\n"
                    + "📊 Priority: " + nextTask.getPriority() + "\n"
                    + "📈 Progress: " + String.format("%.0f%%", plan.getOverallProgress());
        }

        if ("COMPLETE_TASK".equals(intent)) {
            log.info("[ROUTER] → PlanningEngine (complete task)");
            var activePlanOpt = planningEngine.getActivePlan();
            if (activePlanOpt.isEmpty()) {
                return "No active roadmap. Say 'plan: <your goal>' to create one.";
            }
            var plan = activePlanOpt.get();
            var allTasks = plan.getAllTasks();
            var nextTaskOpt = allTasks.stream()
                    .filter(t -> !t.isCompleted() && !t.isBlocked())
                    .findFirst();
            if (nextTaskOpt.isEmpty()) {
                return "All tasks completed! 🎉 Your roadmap is done.";
            }
            var taskToComplete = nextTaskOpt.get();
            planningEngine.completeTask(plan.getId(), taskToComplete.getId());
            return "✅ Task Completed: **" + taskToComplete.getTitle() + "**\n"
                    + "Progress: " + plan.getCompletedTasks() + " / "
                    + plan.getTotalTasks() + " Tasks\n"
                    + "Completion: " + String.format("%.0f%%", plan.getOverallProgress());
        }

        if ("PROGRESS".equals(intent)) {
            log.info("[ROUTER] → PlanningEngine (progress)");
            var activePlanOpt = planningEngine.getActivePlan();
            if (activePlanOpt.isEmpty()) {
                return "No active roadmap. Say 'plan: <your goal>' to create one.";
            }
            var plan = activePlanOpt.get();
            return "🎯 Goal: " + plan.getGoalName() + "\n\n"
                    + "Progress: " + plan.getCompletedTasks() + " / "
                    + plan.getTotalTasks() + " Tasks\n"
                    + "Completion: " + String.format("%.0f%%", plan.getOverallProgress());
        }

        if ("CURRENT_TASK".equals(intent)) {
            log.info("[ROUTER] → PlanningEngine (current task)");
            var activePlanOpt = planningEngine.getActivePlan();
            if (activePlanOpt.isEmpty()) {
                return "No active roadmap. Say 'plan: <your goal>' to create one.";
            }
            var plan = activePlanOpt.get();
            var allTasks = plan.getAllTasks();
            var currentTaskOpt = allTasks.stream()
                    .filter(t -> !t.isCompleted() && !t.isBlocked())
                    .findFirst();
            if (currentTaskOpt.isEmpty()) {
                return "All tasks completed! 🎉 Your roadmap is done.";
            }
            var currentTask = currentTaskOpt.get();
            return "📌 Current Task\n\n**" + currentTask.getTitle() + "**\n\n"
                    + currentTask.getDescription() + "\n\n⏱️ Estimated: "
                    + (int) currentTask.getEstimatedHours() + " Hours\n"
                    + "📊 Priority: " + currentTask.getPriority() + "\n"
                    + "📈 Progress: " + String.format("%.0f%%", plan.getOverallProgress());
        }

        // =========================================================
        // DEFAULT: ChatSkill
        // =========================================================
        log.info("[ROUTER] → ChatSkill (default)");
        return chatSkill.execute(input, context);
    }

    private boolean isGenericCoursePhrase(String text) {
        if (text == null || text.isBlank()) return true;
        String t = text.toLowerCase().trim();
        return t.contains("course") || t.contains("lesson") || t.contains("start")
                || t.contains("begin") || t.contains("learn") || t.contains("teach")
                || t.contains("today") || t.contains("we will") || t.equals("me");
    }
}
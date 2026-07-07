package com.darshan.agent.brain;

import com.darshan.agent.production.ConversationOptimizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IntentEngine {

    private static final Logger log = LoggerFactory.getLogger(IntentEngine.class);

    private final ConversationOptimizer conversationOptimizer;

    public IntentEngine(ConversationOptimizer conversationOptimizer) {
        this.conversationOptimizer = conversationOptimizer;
    }

    /**
     * Context-aware intent detection with priority ordering.
     *
     * Priority (highest to lowest):
     * 1. Identity questions
     * 2. Goal queries
     * 3. Quiz commands
     * 4. Learning commands
     * 5. Roadmap/planning commands
     * 6. Greetings
     * 7. DEFAULT (chat)
     */
    public String detectIntent(String input) {
        String rawInput = input;
        String text = input.toLowerCase().trim();

        System.out.println("[IntentEngine] RAW INPUT: '" + rawInput + "' | NORMALIZED: '" + text + "'");

        // Identity detection (highest priority)
        if (text.contains("who am i")
                || text.contains("what is my name")
                || text.contains("mera naam kya")
                || text.equals("whoami")) {
            System.out.println("[IntentEngine] DETECTED: WHO_AM_I");
            return "WHO_AM_I";
        }

        // Goal query
        if (text.contains("what are my goals")
                || text.contains("my goals")
                || text.contains("goal status")) {
            return "GOAL_QUERY";
        }

        // Summary request
        if (text.equals("summary") || text.contains("lesson summary")) {
            return "SUMMARY";
        }

        // Quiz mode
        if (text.equals("quiz me") || text.contains("quiz time") || text.equals("quiz")) {
            return "QUIZ";
        }

        // Previous chapter
        if (text.equals("previous") || text.equals("go back")
                || text.equals("back") || text.startsWith("previous ")) {
            return "PREVIOUS";
        }

        // =========================================================
        // LEARNING INTENTS — must be detected BEFORE follow-up/generic
        // =========================================================

        // REPEAT_LESSON: "repeat lesson", "repeat this lesson", "say again", "can you repeat"
        if (text.contains("repeat lesson")
                || text.contains("repeat this lesson")
                || text.equals("say again")
                || text.contains("can you repeat")
                || text.contains("repeat that")) {
            System.out.println("[IntentEngine] DETECTED: REPEAT_LESSON");
            return "REPEAT_LESSON";
        }

        // CONTINUE_LESSON: "continue lesson", "next lesson", "current lesson" etc.
        if (text.equals("continue lesson")
                || text.equals("next lesson")
                || text.equals("next chapter")
                || text.equals("continue course")
                || text.equals("go to next lesson")
                || text.equals("advance")
                || text.equals("continue")) {
            System.out.println("[IntentEngine] DETECTED: CONTINUE_LESSON");
            return "CONTINUE_LESSON";
        }

        // TEACH_TOPIC: "explain this topic", "explain", "explain topic", "what is X", "tell me about X"
        if (text.equals("explain this topic")
                || text.equals("explain topic")
                || text.startsWith("explain ")
                || text.startsWith("what is ")
                || text.startsWith("what's ")
                || text.startsWith("what are ")
                || text.startsWith("tell me about ")
                || text.startsWith("describe ")
                || text.startsWith("how does ")
                || text.startsWith("how do ")
                || text.startsWith("what does ")
                || text.equals("explain")) {
            // Only return TEACH_TOPIC if we have an active lesson (checked in AgentBrain)
            System.out.println("[IntentEngine] DETECTED: TEACH_TOPIC");
            return "TEACH_TOPIC";
        }

        // CURRENT_LESSON: "current lesson", "what lesson", "where am i"
        if (text.contains("current lesson")
                || text.contains("what lesson")
                || text.equals("where am i in the course")
                || text.contains("show lesson")
                || text.equals("what am i learning")) {
            System.out.println("[IntentEngine] DETECTED: CURRENT_LESSON");
            return "CURRENT_LESSON";
        }

        // LESSON_PROGRESS (course-specific): "course progress", "lesson progress"
        if (text.contains("course progress")
                || text.contains("lesson progress")
                || (text.contains("progress") && text.contains("course"))
                || text.equals("show course progress")) {
            System.out.println("[IntentEngine] DETECTED: LESSON_PROGRESS");
            return "LESSON_PROGRESS";
        }

        // START_COURSE — detect "learn java", "start java", "teach me java", "today we learn java" etc.
        // Must be checked BEFORE the generic LEARN branch
        if (isStartCourseIntent(text)) {
            System.out.println("[IntentEngine] DETECTED: START_COURSE");
            return "START_COURSE";
        }

        // Follow-up detection (after learning intents, before generic)
        if (isFollowUp(text)) {
            return "FOLLOW_UP";
        }

        // Generic learning intent (maps to old lesson system)
        if (text.startsWith("learn ")
                || text.contains(" i want to learn ")
                || text.contains("teach me")) {
            return "LEARN";
        }

        // Studying intent
        if (text.contains("study")) {
            return "STUDY";
        }

        if(text.contains("roadmap")) {
            return "ROADMAP_REQUEST";
        }

        // Planning/roadmap intent
        if (text.contains("become a") || text.contains("plan") || text.contains("roadmap")
                || text.contains("career path") || text.contains("learning path")
                || text.contains("steps to") || text.contains("how do i become")
                || text.contains("how to become")) {
            return "PLAN";
        }

        // Greeting
        if (text.contains("hello") || text.contains("hi") || text.contains("hey"))
            return "GREETING";

        // Time
        if (text.contains("what time") || text.contains("current time")
                || text.contains("what's the time") || text.equals("time")
                || text.contains("what is the time") || text.contains("tell me the time")
                || text.contains("kitne baje") || text.contains("time kya hai")) {
            return "TIME";
        }

        // Weather
        if (text.contains("weather"))
            return "WEATHER";

        // Reminder
        if (text.contains("remind") || text.contains("reminder"))
            return "REMINDER";

        if(text.contains("what should i do next")
                || text.contains("what next")
                || text.contains("continue roadmap")
                || text.equals("next")
                || text.equals("ok next")
                || text.equals("okay next")
                || text.equals("next please")
                || text.contains("next task")
                || text.contains("next step")) {

            return "NEXT_STEP";
        }

        // Complete task
        if (text.equals("done")
                || text.equals("completed")
                || text.equals("finished")
                || text.contains("task completed")
                || text.contains("mark complete")
                || text.equals("mark done")) {
            return "COMPLETE_TASK";
        }

        // Progress
        if (text.contains("progress")
                || text.contains("show progress")
                || text.contains("how much completed")
                || text.contains("roadmap progress")
                || text.equals("how much done")) {
            return "PROGRESS";
        }

        // Current task
        if (text.contains("current task")
                || text.contains("what am i doing")
                || text.contains("active task")
                || text.contains("what should i study")
                || text.equals("what should i learn")
                || text.equals("what to study")) {
            return "CURRENT_TASK";
        }

        // === COURSE LEARNING INTENTS (legacy patterns, kept for backward compat) ===

        // COMPLETE_LESSON: "complete", "complete lesson"
        if (text.equals("complete")
                || text.equals("complete lesson")
                || text.equals("lesson complete")
                || text.equals("lesson done")
                || text.equals("mark lesson done")
                || text.equals("done with lesson")
                || text.equals("finish lesson")
                || text.equals("i'm done")
                || text.equals("im done")) {
            return "COMPLETE_LESSON";
        }

        // EXIT_COURSE: "exit course", "leave course"
        if (text.equals("exit course")
                || text.equals("leave course")
                || text.equals("stop course")
                || text.equals("quit course")
                || text.equals("end course")
                || text.equals("exit learning")
                || text.equals("exit")) {
            return "EXIT_COURSE";
        }

        return "DEFAULT";
    }

    /**
     * Detect "start learning X", "learn X", "teach me X", "today we learn X",
     * "start X", "begin X", "teach X" where X is a known course name.
     */
    private boolean isStartCourseIntent(String text) {
        // Explicit start course patterns
        if (text.startsWith("start course ")
                || text.startsWith("begin course ")
                || text.startsWith("start learning ")
                || text.startsWith("start teaching ")
                || text.startsWith("enroll in course ")) {
            return true;
        }

        // "today we learn <topic>"
        if (text.startsWith("today we learn ")
                || text.startsWith("today we will learn ")
                || text.startsWith("today i want to learn ")
                || text.startsWith("i want to learn ")) {
            return true;
        }

        // "teach me <topic>", "teach <topic>"
        if (text.startsWith("teach me ")
                || text.startsWith("teach me about ")) {
            return true;
        }

        // "start <course_name>", "begin <course_name>" where course_name is a known course
        if (isKnownCourseStart(text, "start ")
                || isKnownCourseStart(text, "begin ")) {
            return true;
        }

        // "learn <course_name>" where course_name is a known course
        if (text.startsWith("learn ")) {
            String remainder = text.substring("learn ".length()).trim();
            if (isKnownCourse(remainder)) {
                return true;
            }
        }

        // "teach <course_name>" where course_name is a known course
        if (text.startsWith("teach ")) {
            String remainder = text.substring("teach ".length()).trim();
            if (isKnownCourse(remainder)) {
                return true;
            }
        }

        return false;
    }

    private boolean isKnownCourseStart(String text, String prefix) {
        if (text.startsWith(prefix)) {
            String remainder = text.substring(prefix.length()).trim();
            return isKnownCourse(remainder);
        }
        return false;
    }

    private boolean isKnownCourse(String topic) {
        if (topic == null || topic.isBlank()) return false;
        String t = topic.trim().toLowerCase();
        return t.equals("java")
                || t.equals("spring boot")
                || t.equals("springboot")
                || t.equals("spring")
                || t.equals("dsa")
                || t.equals("python")
                || t.equals("javascript")
                || t.equals("react")
                || t.equals("angular")
                || t.equals("docker")
                || t.equals("kubernetes")
                || t.equals("aws")
                || t.equals("sql")
                || t.equals("git")
                || t.equals("microservices")
                || t.equals("rest api")
                || t.equals("rest")
                || t.equals("design patterns")
                || t.equals("system design")
                || t.startsWith("java")
                || t.startsWith("spring");
    }

    private boolean isFollowUp(String text) {
        return text.equals("next")
                || text.equals("ok next")
                || text.equals("okay next")
                || text.equals("next please")
                || text.contains("what next")
                || text.equals("resume")
                || text.equals("go on")
                || text.equals("keep going")
                || text.equals("tell me more")
                || text.equals("expand")
                || text.equals("why")
                || text.equals("how")
                || text.equals("elaborate")
                || text.startsWith("next ")
                || text.contains("next step")
                || text.contains("next topic");
    }
}
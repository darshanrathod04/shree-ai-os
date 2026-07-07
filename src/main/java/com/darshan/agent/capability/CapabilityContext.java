package com.darshan.agent.capability;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.ConversationSession;
import com.darshan.agent.context.LessonState;
import com.darshan.agent.learning.CourseState;
import com.darshan.agent.learning.adaptive.StudentLearningProfile;

import java.util.Objects;

/**
 * Immutable context object for capability lookup.
 * Contains all data needed for capability selection.
 * No behavior — pure data carrier.
 */
public final class CapabilityContext {

    private final String input;
    private final String intent;
    private final ConversationContext conversationContext;
    private final ConversationSession session;
    private final LessonState lessonState;
    private final CourseState courseState;
    private final StudentLearningProfile learningProfile;

    public CapabilityContext(String input, String intent,
                             ConversationContext conversationContext,
                             ConversationSession session,
                             LessonState lessonState,
                             CourseState courseState,
                             StudentLearningProfile learningProfile) {
        this.input = Objects.requireNonNull(input, "input must not be null");
        this.intent = Objects.requireNonNull(intent, "intent must not be null");
        this.conversationContext = conversationContext;
        this.session = session;
        this.lessonState = lessonState;
        this.courseState = courseState;
        this.learningProfile = learningProfile;
    }

    public String getInput() { return input; }
    public String getIntent() { return intent; }
    public ConversationContext getConversationContext() { return conversationContext; }
    public ConversationSession getSession() { return session; }
    public LessonState getLessonState() { return lessonState; }
    public CourseState getCourseState() { return courseState; }
    public StudentLearningProfile getLearningProfile() { return learningProfile; }

    @Override
    public String toString() {
        return "CapabilityContext{" +
                "intent='" + intent + '\'' +
                ", input='" + input.substring(0, Math.min(30, input.length())) + '\'' +
                '}';
    }
}
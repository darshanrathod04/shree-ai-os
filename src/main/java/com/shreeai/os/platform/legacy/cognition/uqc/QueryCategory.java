package com.shreeai.os.platform.legacy.cognition.uqc;

/**
 * Extensible query category enum for Universal Query Classifier.
 * Categories represent the type of query the user is making,
 * independent of the current session state.
 *
 * Designed for future extension — add new categories here.
 */
public enum QueryCategory {

    /** General knowledge about any topic */
    GENERAL_KNOWLEDGE,
    /** Programming-specific knowledge (languages, frameworks, concepts) */
    PROGRAMMING,
    /** Learning / course-related requests */
    LEARNING,
    /** Quiz-related requests */
    QUIZ,
    /** Roadmap creation or tracking */
    ROADMAP,
    /** Task/schedule planning */
    PLANNING,
    /** Code debugging requests */
    DEBUGGING,
    /** Code generation/writing requests */
    CODING,
    /** Casual conversation, small talk */
    SMALL_TALK,
    /** Greetings (hello, hi, hey) */
    GREETING,
    /** Identity-related (who am I) */
    IDENTITY,
    /** Memory recall requests */
    MEMORY,
    /** Career advice */
    CAREER,
    /** System/utility queries (time, weather, capabilities) */
    SYSTEM,
    /** User wants to exit/stop */
    EXIT,
    /** Action acknowledgment (ok, thanks, got it) */
    ACKNOWLEDGMENT,
    /** Lesson navigation (continue, next, repeat, back) */
    LESSON_NAV,
    /** Comparison between two or more things */
    COMPARISON,
    /** Definition of a term */
    DEFINITION,
    /** Explanation of a concept */
    EXPLANATION,
    /** Multi-action query (teach Java and then quiz me) */
    MULTI_ACTION,
    /** Could not classify */
    UNKNOWN
}
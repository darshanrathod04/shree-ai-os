package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>AmbiguityType</b>
 *
 * <p>Extensible enumeration of the structural ambiguity risks that can be
 * diagnosed from a user request's cognitive artifacts (intent, domain,
 * constraints, and goals).</p>
 *
 * <p><b>Architectural Responsibility:</b> Identifies what information is
 * missing or competing in a request so that orchestrators (such as Chief
 * Intelligence in a later phase) can decide how to proceed. This enum only
 * <em>diagnoses</em> - it never resolves, infers, asks questions, or routes
 * runtime behaviour.</p>
 *
 * <p><b>Ownership:</b> Context Kernel - P1 Context Intelligence</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum AmbiguityType {

    /** A domain-reaching intent (BUILD / DEBUG / PLAN) has no detected domain. */
    MISSING_DOMAIN,

    /** A mobile request has no target platform (Android or iOS). */
    MISSING_PLATFORM,

    /** A learning request has no explicit output language. */
    MISSING_LANGUAGE,

    /** A planning request has no explicit duration. */
    MISSING_DURATION,

    /** No concrete goal could be identified from the request. */
    MISSING_GOAL,

    /** Multiple domains compete with no dominant confidence. */
    MULTIPLE_DOMAINS,

    /** The request is a bare generic action with no specific subject. */
    GENERIC_REQUEST,

    /** No identifiable context was found at all (for example empty input). */
    INSUFFICIENT_CONTEXT
}
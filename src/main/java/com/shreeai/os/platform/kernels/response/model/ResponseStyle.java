package com.shreeai.os.platform.kernels.response.model;

/**
 * ResponseStyle
 *
 * Defines the professional presentation style selected by the
 * Response Synthesizer. This influences structure only—not reasoning.
 *
 * Constitutional Rule:
 * The style MUST NEVER change factual content, only presentation.
 */
public enum ResponseStyle {

    /** Natural conversational response. */
    CHAT,

    /** Code, architecture and project audit. */
    AUDIT,

    /** Teaching and educational explanation. */
    LEARNING,

    /** Roadmaps, goals and execution plans. */
    PLANNING,

    /** Root-cause analysis and debugging. */
    DEBUG,

    /** Research and analytical reports. */
    ANALYSIS
}
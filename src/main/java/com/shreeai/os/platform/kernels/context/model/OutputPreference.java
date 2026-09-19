package com.shreeai.os.platform.kernels.context.model;

/**
 * <b>OutputPreference</b>
 *
 * <p>Defines the preferred output format for content generation.</p>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 */
public enum OutputPreference {
    /**
     * Step-by-step roadmap format.
     */
    ROADMAP,

    /**
     * Complete code implementation.
     */
    CODE,

    /**
     * Detailed explanation.
     */
    EXPLANATION,

    /**
     * Checklist format.
     */
    CHECKLIST,

    /**
     * Architecture diagram or description.
     */
    ARCHITECTURE
}
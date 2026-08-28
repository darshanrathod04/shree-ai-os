package com.shreeai.os.platform.intelligence.evidence;

/**
 * <b>EvidenceType</b>
 *
 * <p>Classification of evidence entering the Shree AI OS intelligence pipeline.</p>
 *
 * <p>Evidence types map to the natural categories of information that a developer
 * application, project analysis, code intelligence, or autonomous agent can supply.
 * Only types that fit the existing architecture are defined; no artificial types are
 * introduced.</p>
 *
 * <p><b>Ownership:</b> Intelligence Foundation</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see EvidenceItem
 */
public enum EvidenceType {

    /** A verified factual statement. */
    FACT,

    /** Direct observed property, measurement, or datum. */
    OBSERVATION,

    /** A claim made by the user or an external source (unverified). */
    USER_ASSERTION,

    /** A whole project, module, or domain artifact. */
    PROJECT_ARTIFACT,

    /** A specific file within a project. */
    FILE,

    /** Source code fragment or symbol reference. */
    CODE,

    /** Configuration file or setting value. */
    CONFIGURATION,

    /** Test execution result, assertion, or coverage datum. */
    TEST_RESULT,

    /** Log output or runtime trace line. */
    LOG,

    /** A recalled memory from the Memory Kernel. */
    MEMORY,

    /** A retrieved knowledge node from the Knowledge Kernel. */
    KNOWLEDGE,

    /** Output from a tool, command, or external service. */
    TOOL_RESULT,

    /** Observation made by the system itself during execution. */
    SYSTEM_OBSERVATION
}
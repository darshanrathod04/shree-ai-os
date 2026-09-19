package com.shreeai.os.platform.kernels.planning.model;

/**
 * <b>TaskType</b>
 *
 * <p>Classifies a {@link PlanningTask} in the P2.2 Task Dependency Graph.
 * The type is produced by deterministic template expansion - never by an
 * LLM, never by a heuristic score, and never by randomness.</p>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Immutable - enum constants are inherently immutable.</li>
 *   <li>Deterministic - the declaration order is the canonical ordinal
 *       order and must never be reordered while P2.2 graphs are persisted.</li>
 *   <li>Data-only - carries no scheduling, allocation, or execution logic.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel - P2.2 Task Dependency Graph</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @since P2.2
 * @see PlanningTask
 */
public enum TaskType {

    /** Learning task: study, read, or practise a concept. */
    LEARNING,

    /** Implementation task: build, write, or configure an artefact. */
    IMPLEMENTATION,

    /** Project task: deliver an integrated end-to-end artefact. */
    PROJECT,

    /** Review task: inspect, verify, or critique completed work. */
    REVIEW,

    /** Assessment task: measure progress, capability, or mastery. */
    ASSESSMENT
}

package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.GoalStructure;

/**
 * <b>GoalIdentificationEngine</b>
 *
 * <p>Defines the contract for identifying goals from user input.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Analyzes user input to identify what the user is trying to achieve</li>
 *   <li>Produces an immutable {@link GoalStructure} with primary and sub-goals</li>
 *   <li>Provides deterministic results for the same input</li>
 *   <li>Records evidence for each identified goal (explainability)</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless - no mutable instance state</li>
 *   <li>Thread-safe - immutable operations</li>
 *   <li>Deterministic - same input always produces same output</li>
 *   <li>No side effects - pure computation only</li>
 *   <li>No inference - only explicit goals are identified</li>
 *   <li>No planning - only identifies goals, does not generate plans</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see GoalStructure
 * @see DefaultGoalIdentificationEngine
 */
public interface GoalIdentificationEngine {

    /**
     * Identifies goals from the given user input.
     *
     * <p>Analyzes the input text and returns a {@link GoalStructure} containing
     * the identified primary goal, sub-goals, and complexity assessment.</p>
     *
     * @param userInput the user's input text (must not be null)
     * @return the identified goal structure (never null)
     * @throws NullPointerException if userInput is null
     */
    GoalStructure identify(String userInput);
}
package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;

/**
 * <b>ConstraintExtractionEngine</b>
 *
 * <p>Defines the contract for extracting explicit user constraints from natural language input.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Analyzes user input to extract explicit constraints only</li>
 *   <li>Produces an immutable {@link UserConstraints} object</li>
 *   <li>Provides deterministic results for the same input</li>
 *   <li>Records evidence for each extracted constraint (explainability)</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless - no mutable instance state</li>
 *   <li>Thread-safe - immutable operations</li>
 *   <li>Deterministic - same input always produces same output</li>
 *   <li>No side effects - pure computation only</li>
 *   <li>No inference - only explicit values are extracted</li>
 *   <li>Missing values remain null - never invent defaults</li>
 * </ul>
 *
 * <p><b>Extraction Precedence:</b></p>
 * <ul>
 *   <li>Duration: First explicit duration mentioned</li>
 *   <li>Budget: First explicit monetary value mentioned</li>
 *   <li>Experience: First explicit level mentioned</li>
 *   <li>Platform: First explicit platform mentioned</li>
 *   <li>Output Preference: First explicit request mentioned</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see UserConstraints
 * @see DefaultConstraintExtractionEngine
 */
public interface ConstraintExtractionEngine {

    /**
     * Extracts explicit user constraints from the given input.
     *
     * <p>Analyzes the input text and returns a {@link UserConstraints} object containing
     * all explicitly mentioned constraints. Missing values remain null.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param userInput the user's input text (must not be null)
     * @return the extracted user constraints (never null)
     * @throws NullPointerException if userInput is null
     */
    UserConstraints extract(String userInput);
}
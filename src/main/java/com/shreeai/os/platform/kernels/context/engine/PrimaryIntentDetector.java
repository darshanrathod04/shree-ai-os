package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.IntentProfile;

/**
 * <b>PrimaryIntentDetector</b>
 *
 * <p>Defines the contract for detecting the primary intent from user input.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Analyzes user input to determine the primary intent.</li>
 *   <li>Produces an immutable {@link IntentProfile} with ranked alternatives.</li>
 *   <li>Provides deterministic results for the same input.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Stateless - no mutable instance state.</li>
 *   <li>Thread-safe - immutable operations.</li>
 *   <li>Deterministic - same input always produces same output.</li>
 *   <li>No side effects - pure computation only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Context Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CTX-201</p>
 *
 * @see IntentProfile
 * @see DefaultPrimaryIntentDetector
 */
public interface PrimaryIntentDetector {

    /**
     * Detects the primary intent from the given user input.
     *
     * <p>Analyzes the input text and returns an {@link IntentProfile} containing
     * the detected primary intent, confidence score, and ranked alternatives.</p>
     *
     * <p><b>Thread Safety:</b> This operation is thread-safe.</p>
     *
     * <p><b>Stateless:</b> This operation depends only on its input parameters.</p>
     *
     * @param userInput the user's input text (must not be null)
     * @return the detected intent profile (never null)
     * @throws NullPointerException if userInput is null
     */
    IntentProfile detect(String userInput);
}
package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.DomainProfile;

/**
 * <b>DomainDetector</b>
 *
 * <p>Defines the contract for detecting the primary domain from user input.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Analyzes user input to determine the primary technical or business domain.</li>
 *   <li>Produces an immutable {@link DomainProfile} with ranked detected domains.</li>
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
 * @see DomainProfile
 * @see DefaultDomainDetector
 */
public interface DomainDetector {

    /**
     * Detects the primary domain from the given user input.
     *
     * <p>Analyzes the input text and returns a {@link DomainProfile} containing
     * the detected primary domain, confidence score, and ranked detected domains.</p>
     *
     * @param userInput the user's input text (must not be null)
     * @return the detected domain profile (never null)
     * @throws NullPointerException if userInput is null
     */
    DomainProfile detect(String userInput);
}
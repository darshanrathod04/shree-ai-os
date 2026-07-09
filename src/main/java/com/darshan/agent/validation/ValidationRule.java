package com.darshan.agent.validation;

import com.darshan.agent.capability.Capability;
import com.darshan.agent.capability.CapabilityRegistry;
import com.darshan.agent.cognition.Thought;
import com.darshan.agent.context.ConversationSession;
import com.darshan.agent.production.ResolvedContext;

import java.time.Instant;

/**
 * Single validation rule interface.
 *
 * <p>Each rule validates one aspect of a decision independently.
 * Rules are stateless, thread-safe, and follow Single Responsibility Principle.</p>
 *
 * <h2>Contract</h2>
 * <ul>
 *   <li>Rules must be independent — no rule knows about another</li>
 *   <li>Rules must be thread-safe — no mutable state</li>
 *   <li>Rules must be fast — no blocking, no I/O</li>
 *   <li>Rules return ValidationOutcome with pass/fail and optional warnings</li>
 * </ul>
 *
 * @author Shree AI OS Team
 * @version 1.0
 * @since Sprint 5.1
 */
public interface ValidationRule {

    /**
     * Validate a decision and return outcome.
     *
     * @param decision the decision to validate
     * @param session the conversation session (may be null)
     * @param resolvedContext the resolved context (may be null)
     * @param capabilityRegistry the capability registry for lookups
     * @return ValidationOutcome with result and optional warnings
     */
    ValidationOutcome validate(
            Thought decision,
            ConversationSession session,
            ResolvedContext resolvedContext,
            CapabilityRegistry capabilityRegistry
    );

    /**
     * Get the rule name for logging and tracing.
     */
    String getRuleName();
}
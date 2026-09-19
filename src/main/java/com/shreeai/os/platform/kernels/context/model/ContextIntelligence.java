package com.shreeai.os.platform.kernels.context.model;

import java.util.Objects;

/**
 * <b>ContextIntelligence</b>
 *
 * <p>Canonical, immutable aggregate of the five context intelligence artifacts
 * produced by the Context Kernel:</p>
 * <ol>
 *   <li>{@link IntentProfile} - why is the user asking?</li>
 *   <li>{@link DomainProfile} - what domain is it about?</li>
 *   <li>{@link UserConstraints} - under what limitations?</li>
 *   <li>{@link GoalStructure} - what must be achieved?</li>
 *   <li>{@link AmbiguityProfile} - is the request structurally complete?</li>
 * </ol>
 *
 * <p><b>Architectural Responsibility:</b> Provides one immutable snapshot that
 * context consumers (memory, knowledge, and later Chief Intelligence) can read
 * without reaching into the raw prompt. Context diagnoses reality; Chief makes
 * decisions.</p>
 *
 * <p><b>Determinism:</b> This record stores only the artifacts; it performs no
 * analysis itself.</p>
 *
 * <p><b>Ownership:</b> Context Kernel - P1 Context Intelligence</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @param intentProfile the detected intent profile (may be null before ContextStage)
 * @param domainProfile the detected domain profile (may be null before ContextStage)
 * @param constraints the extracted user constraints (may be null before ContextStage)
 * @param goals the identified goal structure (may be null before ContextStage)
 * @param ambiguityProfile the ambiguity diagnosis (may be null before ContextStage)
 */
public record ContextIntelligence(
        IntentProfile intentProfile,
        DomainProfile domainProfile,
        UserConstraints constraints,
        GoalStructure goals,
        AmbiguityProfile ambiguityProfile) {

    /**
     * Creates a ContextIntelligence from the five canonical artifacts.
     *
     * @param intentProfile the detected intent profile
     * @param domainProfile the detected domain profile
     * @param constraints the extracted user constraints
     * @param goals the identified goal structure
     * @param ambiguityProfile the ambiguity diagnosis
     * @return a new immutable ContextIntelligence (never null)
     */
    public static ContextIntelligence of(IntentProfile intentProfile,
                                         DomainProfile domainProfile,
                                         UserConstraints constraints,
                                         GoalStructure goals,
                                         AmbiguityProfile ambiguityProfile) {
        return new ContextIntelligence(intentProfile, domainProfile, constraints,
                goals, ambiguityProfile);
    }

    /**
     * Returns a new ContextIntelligence with the given ambiguity profile,
     * preserving all other artifacts.
     *
     * @param ambiguityProfile the ambiguity diagnosis (must not be null)
     * @return a new ContextIntelligence with the profile set (never null)
     */
    public ContextIntelligence withAmbiguityProfile(AmbiguityProfile ambiguityProfile) {
        Objects.requireNonNull(ambiguityProfile, "ambiguityProfile must not be null");
        return new ContextIntelligence(intentProfile, domainProfile, constraints,
                goals, ambiguityProfile);
    }

    @Override
    public String toString() {
        return "ContextIntelligence{intent=" + (intentProfile != null)
                + ", domain=" + (domainProfile != null)
                + ", constraints=" + (constraints != null)
                + ", goals=" + (goals != null)
                + ", ambiguity=" + (ambiguityProfile != null) + '}';
    }
}
package com.shreeai.os.platform.kernels.context.engine;

import com.shreeai.os.platform.kernels.context.model.AmbiguityProfile;
import com.shreeai.os.platform.kernels.context.model.DomainProfile;
import com.shreeai.os.platform.kernels.context.model.GoalStructure;
import com.shreeai.os.platform.kernels.context.model.IntentProfile;
import com.shreeai.os.platform.kernels.context.model.UserConstraints;

/**
 * <b>AmbiguityDetectionEngine</b>
 *
 * <p>Interface for the deterministic ambiguity diagnosis capability of the
 * Context Kernel.</p>
 *
 * <p><b>Architectural Responsibility:</b> Diagnoses whether a request contains
 * sufficient information to continue execution and produces one immutable
 * {@link AmbiguityProfile}.</p>
 *
 * <p><b>Boundary:</b> This engine only diagnoses. It never asks clarification
 * questions, infers missing values, modifies intent / domain / constraints /
 * goals, or decides runtime behaviour.</p>
 *
 * <p><b>Contract:</b> Implementations must be stateless, deterministic and
 * rule-based: the same input artifacts always produce an identical profile.</p>
 *
 * <p><b>Ownership:</b> Context Kernel - P1 Context Intelligence</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * @see DefaultAmbiguityDetectionEngine
 */
public interface AmbiguityDetectionEngine {

    /**
     * Diagnoses ambiguity from the already-produced cognitive artifacts.
     *
     * <p>The raw prompt must never be re-parsed here; only the supplied
     * artifacts are inspected.</p>
     *
     * @param intentProfile the detected intent profile (must not be null)
     * @param domainProfile the detected domain profile (must not be null)
     * @param constraints the extracted user constraints (must not be null)
     * @param goals the identified goal structure (must not be null)
     * @return an immutable AmbiguityProfile (never null)
     * @throws NullPointerException if any argument is null
     */
    AmbiguityProfile diagnose(IntentProfile intentProfile,
                              DomainProfile domainProfile,
                              UserConstraints constraints,
                              GoalStructure goals);
}
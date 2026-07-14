package platform.core.health.engine;

import platform.core.health.model.HealthCheck;
import platform.core.health.model.HealthComponent;
import platform.core.health.model.HealthReport;

import java.util.Optional;

/**
 * <b>HealthEvaluationEngine</b>
 *
 * <p>Evaluates the health of platform components within Shree AI OS.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Performs health evaluation for registered components.</li>
 *   <li>Answers the question: "What is the health status of this component?"</li>
 *   <li>Never validates — validation belongs to HealthValidator.</li>
 *   <li>Never coordinates — coordination belongs to HealthService.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Platform Core</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> ADD-PLT-202, ADD-PLT-205, ADD-PLT-206</p>
 *
 * <p><b>Engineering Principle:</b> HealthEvaluationEngine evaluates health.
 * HealthValidator validates structure. HealthService coordinates everything.
 * These responsibilities shall remain independent forever.</p>
 *
 * @see platform.core.health.service.HealthService
 * @see platform.core.health.validator.HealthValidator
 */
public interface HealthEvaluationEngine {

    /**
     * Evaluates the health of a specific component.
     *
     * <p>This method performs the actual health evaluation — pinging services,
     * checking metrics, and determining health status.</p>
     *
     * <p><b>Note:</b> This method SHALL throw HealthCheckFailedException if the
     * health check fails.</p>
     *
     * @param component the health component to evaluate (must not be null)
     * @param deep whether to perform a deep health check
     * @return a {@link HealthReport} containing the evaluation results
     * @throws HealthCheckFailedException if the health check fails
     */
    HealthReport evaluate(HealthComponent component, boolean deep);
}
package platform.kernels.planning.service;

import platform.kernels.planning.model.Goal;
import platform.kernels.planning.model.PlanningObjective;
import platform.kernels.planning.model.Priority;
import platform.kernels.planning.model.Schedule;
import platform.kernels.planning.model.Task;
import platform.kernels.planning.model.ValidationCriteria;

import java.util.List;

/**
 * <b>PlanningProcessingEngine</b>
 *
 * <p>Processing contract for deterministic planning operations.
 * This is a temporary interface residing in the Service package during PLAN-105.
 * In PLAN-106, this interface will be migrated to {@code platform.kernels.planning.engine}.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines processing contracts for planning operations.</li>
 *   <li>Delegates planning computation to specialized engine implementations.</li>
 *   <li>Maintains no implementation — interface only.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Planning Kernel — Service Layer (temporary)</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Invariant:</b> This is an interface with no implementation. It defines
 * processing contracts only. Actual processing logic will be implemented by
 * {@code DefaultPlanningProcessingEngine} in PLAN-106.</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-PLAN-105, EIO-ARCH-001</p>
 *
 * <p><b>Processing Responsibilities:</b></p>
 * <p>The engine performs deterministic computation only. It transforms validated
 * planning inputs into immutable processing results without introducing adaptive
 * behavior, learning, orchestration, or autonomous decision-making.</p>
 *
 * @since 1.0
 */
public interface PlanningProcessingEngine {

    /**
     * Processes a goal planning operation.
     *
     * <p>Executes deterministic goal planning computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic goal planning operations.</li>
     *   <li>Transform validated inputs.</li>
     *   <li>Generate immutable processing results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer).</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer).</li>
     *   <li>Does not decompose goals or evaluate goal achievement strategies.</li>
     * </ul>
     *
     * @param objective the planning objective (must not be {@code null})
     * @return a list of processed {@link Goal} instances
     */
    List<Goal> processGoalPlanning(PlanningObjective objective);

    /**
     * Processes a task planning operation.
     *
     * <p>Executes deterministic task planning computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic task planning operations.</li>
     *   <li>Transform validated inputs.</li>
     *   <li>Generate immutable processing results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer).</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer).</li>
     *   <li>Does not generate or sequence tasks.</li>
     * </ul>
     *
     * @param goals the list of goals to plan tasks for (must not be {@code null})
     * @return a list of processed {@link Task} instances
     */
    List<Task> processTaskPlanning(List<Goal> goals);

    /**
     * Processes a scheduling operation.
     *
     * <p>Executes deterministic scheduling computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic scheduling operations.</li>
     *   <li>Transform validated inputs.</li>
     *   <li>Generate immutable processing results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer).</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer).</li>
     *   <li>Does not optimize schedules or evaluate scheduling quality.</li>
     * </ul>
     *
     * @param tasks the list of tasks to schedule (must not be {@code null})
     * @return a processed {@link Schedule} instance
     */
    Schedule processScheduling(List<Task> tasks);

    /**
     * Processes a prioritization operation.
     *
     * <p>Executes deterministic prioritization computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic prioritization operations.</li>
     *   <li>Transform validated inputs.</li>
     *   <li>Generate immutable processing results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer).</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer).</li>
     *   <li>Does not compute priorities or evaluate priority correctness.</li>
     * </ul>
     *
     * @param tasks the list of tasks to prioritize (must not be {@code null})
     * @return a list of processed {@link Priority} instances
     */
    List<Priority> processPrioritization(List<Task> tasks);

    /**
     * Processes a plan validation operation.
     *
     * <p>Executes deterministic plan validation computation on the validated inputs.
     * This method performs structural processing and transformation only.</p>
     *
     * <p><b>Processing Responsibilities:</b></p>
     * <ul>
     *   <li>Execute deterministic plan validation operations.</li>
     *   <li>Transform validated inputs.</li>
     *   <li>Generate immutable processing results.</li>
     * </ul>
     *
     * <p><b>What This Method Does NOT Do:</b></p>
     * <ul>
     *   <li>Does not validate inputs (validation is handled by the service layer).</li>
     *   <li>Does not translate exceptions (exception translation is handled by the service layer).</li>
     *   <li>Does not validate plans semantically or evaluate plan quality.</li>
     * </ul>
     *
     * @param criteria the validation criteria (must not be {@code null})
     * @return a validation result
     */
    Object processPlanValidation(ValidationCriteria criteria);
}
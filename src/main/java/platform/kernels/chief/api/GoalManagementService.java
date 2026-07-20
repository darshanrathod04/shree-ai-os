package platform.kernels.chief.api;

import platform.kernels.chief.model.GoalDescriptor;

/**
 * <b>GoalManagementService</b>
 *
 * <p>Defines goal lifecycle contracts for the Chief Kernel.
 * This interface provides contracts for managing strategic goals across the platform
 * without implementing any goal planning algorithms.</p>
 *
 * <p><b>Architectural Responsibility:</b></p>
 * <ul>
 *   <li>Defines goal lifecycle contracts.</li>
 *   <li>Provides goal creation, update, prioritization, and retirement contracts.</li>
 *   <li>Coordinates with the Planning Kernel through public contracts only.</li>
 *   <li>Contains no planning implementation.</li>
 * </ul>
 *
 * <p><b>Design Principles:</b></p>
 * <ul>
 *   <li>Interface-only — no implementation logic.</li>
 *   <li>Technology-agnostic — no framework dependencies.</li>
 *   <li>Contract-focused — exposes only goal management contracts.</li>
 *   <li>Stateless — no mutable state.</li>
 * </ul>
 *
 * <p><b>Ownership:</b> Chief Kernel</p>
 * <p><b>Version:</b> 1.0</p>
 *
 * <p><b>Constitutional Authority:</b> EIO-CHIEF-101, EIO-ARCH-001</p>
 *
 * @since 1.0
 */
public interface GoalManagementService {

    /**
     * Creates a new strategic goal.
     *
     * <p>This operation creates a strategic goal with the specified parameters
     * and returns a goal descriptor for tracking.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> Coordinates with the Planning Kernel for goal
     * decomposition and planning logic.</p>
     *
     * @param goalDescriptor the goal descriptor (must not be {@code null})
     * @return the created goal descriptor with updated status
     * @throws IllegalArgumentException if goalDescriptor is {@code null}
     */
    GoalDescriptor createGoal(GoalDescriptor goalDescriptor);

    /**
     * Updates an existing strategic goal.
     *
     * <p>This operation updates the parameters of an existing strategic goal.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param goalDescriptor the goal descriptor with updated values (must not be {@code null})
     * @return the updated goal descriptor
     * @throws IllegalArgumentException if goalDescriptor is {@code null}
     */
    GoalDescriptor updateGoal(GoalDescriptor goalDescriptor);

    /**
     * Prioritizes a strategic goal.
     *
     * <p>This operation sets the priority of a strategic goal, influencing
     * its position in the execution queue.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * <p><b>Delegation:</b> Coordinates with the Planning Kernel for priority
     * scheduling logic.</p>
     *
     * @param goalId   the goal identifier (must not be {@code null} or empty)
     * @param priority the new priority value
     * @return the updated goal descriptor
     * @throws IllegalArgumentException if goalId is {@code null} or empty
     */
    GoalDescriptor prioritizeGoal(String goalId, int priority);

    /**
     * Retires a strategic goal.
     *
     * <p>This operation retires a strategic goal, removing it from active
     * orchestration.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param goalId the goal identifier (must not be {@code null} or empty)
     * @return the retired goal descriptor
     * @throws IllegalArgumentException if goalId is {@code null} or empty
     */
    GoalDescriptor retireGoal(String goalId);

    /**
     * Queries the current status of a strategic goal.
     *
     * <p>This operation retrieves the current status and details of a
     * strategic goal.</p>
     *
     * <p><b>Semantic Boundary:</b> This method defines the contract only.
     * Implementations will be provided in subsequent Engineering Orders.</p>
     *
     * @param goalId the goal identifier (must not be {@code null} or empty)
     * @return the goal descriptor with current status
     * @throws IllegalArgumentException if goalId is {@code null} or empty
     */
    GoalDescriptor queryGoalStatus(String goalId);
}
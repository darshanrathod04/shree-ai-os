package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.context.model.UserConstraints;
import com.shreeai.os.platform.kernels.planning.engine.DefaultResourceTimeAllocationEngine;
import com.shreeai.os.platform.kernels.planning.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTimeAllocationSmokeTest {
    @Test
    void packsIndependentTasksAndAllowsDependencyOverrun() {
        var engine = new DefaultResourceTimeAllocationEngine();
        var variables = PlanningTask.create("Java", "Variables", TaskType.LEARNING, 1);
        var loops = PlanningTask.create("Java", "Loops", TaskType.LEARNING, 2);
        var review = PlanningTask.create("Java", "Review", TaskType.REVIEW, 3);
        var methods = PlanningTask.create("Java", "Methods", TaskType.IMPLEMENTATION, 4);
        var graph = TaskGraph.of(List.of(variables, loops, review, methods), List.of(
                new TaskDependency(variables.taskId(), methods.taskId(), DependencyType.FINISH_TO_START)));
        var constraints = UserConstraints.of("1 day", null, null, null, null, null, List.of());
        var plan = engine.allocate(graph, constraints);
        assertEquals(new ExecutionPlan(2, 12, List.of(
                new ScheduledTask(variables.taskId(), 1, 3, List.of(new ResourceAllocation(ResourceType.STUDY, 3))),
                new ScheduledTask(loops.taskId(), 1, 3, List.of(new ResourceAllocation(ResourceType.STUDY, 3))),
                new ScheduledTask(review.taskId(), 1, 2, List.of(new ResourceAllocation(ResourceType.REVIEW, 2))),
                new ScheduledTask(methods.taskId(), 2, 4, List.of(new ResourceAllocation(ResourceType.CODING, 4)))
        )), plan);
        assertEquals(plan, engine.allocate(graph, constraints));
    }

    @Test
    void parsesLockedExamples() {
        var engine = new DefaultResourceTimeAllocationEngine();
        assertEquals(30, engine.determineAvailableDays(null));
        assertEquals(30, engine.determineAvailableDays("30 days"));
        assertEquals(14, engine.determineAvailableDays("2 weeks"));
        assertEquals(180, engine.determineAvailableDays("6 months"));
    }
}

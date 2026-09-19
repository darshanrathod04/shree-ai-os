package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.model.*;
import com.shreeai.os.platform.runtime.cognitive.CognitiveState;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ExecutionPlanModelTest {
    private static ScheduledTask scheduled(String id, int day, int hours) {
        return new ScheduledTask(id, day, hours, List.of(new ResourceAllocation(ResourceType.STUDY, hours)));
    }

    @Test
    void deepDefensiveCopies() {
        var resources = new ArrayList<>(List.of(new ResourceAllocation(ResourceType.STUDY, 3)));
        var task = new ScheduledTask("task", 1, 3, resources);
        var tasks = new ArrayList<>(List.of(task));
        var plan = new ExecutionPlan(1, 3, tasks);
        resources.clear();
        tasks.clear();
        assertEquals(List.of(scheduled("task", 1, 3)), plan.scheduledTasks());
        assertThrows(UnsupportedOperationException.class, () -> plan.scheduledTasks().clear());
        assertThrows(UnsupportedOperationException.class, () -> task.resources().clear());
        assertEquals(plan, new ExecutionPlan(1, 3, List.of(scheduled("task", 1, 3))));
        assertEquals(plan.hashCode(), new ExecutionPlan(1, 3, List.of(task)).hashCode());
    }

    @Test
    void validatesResourcesAndTaskBounds() {
        assertThrows(NullPointerException.class, () -> new ResourceAllocation(null, 1));
        for (int hours : new int[]{Integer.MIN_VALUE, -1, 0}) {
            assertThrows(IllegalArgumentException.class, () -> new ResourceAllocation(ResourceType.STUDY, hours));
        }
        assertEquals(Integer.MAX_VALUE, new ResourceAllocation(ResourceType.STUDY, Integer.MAX_VALUE).hours());
        assertThrows(NullPointerException.class, () -> scheduled(null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> scheduled(" ", 1, 1));
        for (int day : new int[]{Integer.MIN_VALUE, -1, 0}) {
            assertThrows(IllegalArgumentException.class, () -> scheduled("id", day, 1));
        }
        for (int hours : new int[]{-1, 0, 9, Integer.MAX_VALUE}) {
            assertThrows(IllegalArgumentException.class, () -> scheduled("id", 1, hours));
        }
        assertThrows(NullPointerException.class, () -> new ScheduledTask("id", 1, 1, null));
        assertThrows(NullPointerException.class, () -> new ScheduledTask("id", 1, 1, Arrays.asList((ResourceAllocation) null)));
        assertThrows(IllegalArgumentException.class, () -> new ScheduledTask("id", 1, 1, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ScheduledTask("id", 1, 2,
                List.of(new ResourceAllocation(ResourceType.STUDY, 1))));
        assertThrows(IllegalArgumentException.class, () -> new ScheduledTask("id", 1, 2,
                List.of(new ResourceAllocation(ResourceType.STUDY, 1), new ResourceAllocation(ResourceType.CODING, 1))));
    }

    @Test
    void validatesCanonicalPlan() {
        assertEquals(new ExecutionPlan(0, 0, List.of()), ExecutionPlan.empty());
        assertThrows(NullPointerException.class, () -> new ExecutionPlan(0, 0, null));
        assertThrows(NullPointerException.class, () -> new ExecutionPlan(0, 0, Arrays.asList((ScheduledTask) null)));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan(-1, 0, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan(0, -1, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan(30, 3, List.of(scheduled("a", 1, 3))));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan(1, 4, List.of(scheduled("a", 1, 3))));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan(2, 6,
                List.of(scheduled("a", 1, 3), scheduled("a", 2, 3))));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan(2, 6,
                List.of(scheduled("a", 2, 3), scheduled("b", 1, 3))));
        assertThrows(IllegalArgumentException.class, () -> new ExecutionPlan(1, 9,
                List.of(scheduled("a", 1, 8), scheduled("b", 1, 1))));
    }

    @Test
    void cognitiveStateRoundTripAndNullRejection() {
        var original = CognitiveState.empty();
        var plan = new ExecutionPlan(1, 3, List.of(scheduled("a", 1, 3)));
        var next = original.withExecutionPlan(plan);
        assertNull(original.executionPlan());
        assertSame(plan, next.executionPlan());
        assertThrows(NullPointerException.class, () -> original.withExecutionPlan(null));
        assertSame(plan, next.incrementReflection().executionPlan());
        assertEquals(0, next.reflectionIteration());
        assertEquals(1, next.incrementReflection().reflectionIteration());
    }

    @TestFactory
    Stream<DynamicTest> everyArtifactCopyPreservesSchedule() {
        return Arrays.stream(CognitiveState.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith("with") && !m.getName().equals("withExecutionPlan"))
                .map(method -> DynamicTest.dynamicTest(method.getName(), () -> {
                    var plan = new ExecutionPlan(1, 3, List.of(scheduled("a", 1, 3)));
                    var state = CognitiveState.empty().withExecutionPlan(plan);
                    Object[] args = Arrays.stream(method.getParameterTypes())
                            .map(type -> type == double.class ? (Object) 0.75 : mock(type)).toArray();
                    var updated = (CognitiveState) method.invoke(state, args);
                    assertSame(plan, updated.executionPlan());
                    assertSame(plan, state.executionPlan());
                }));
    }
}

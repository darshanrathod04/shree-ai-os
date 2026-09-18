package com.shreeai.os.platform.kernels.planning;

import com.shreeai.os.platform.kernels.planning.model.PlanningTask;
import com.shreeai.os.platform.kernels.planning.model.TaskType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("P2.2: PlanningTask model")
class PlanningTaskModelTest {

    @Nested
    @DisplayName("TaskType contract")
    class TaskTypeContract {
        @Test @DisplayName("TaskType declares exactly the five locked members")
        void lockedMembers() {
            assertArrayEquals(
                    new TaskType[]{TaskType.LEARNING, TaskType.IMPLEMENTATION,
                            TaskType.PROJECT, TaskType.REVIEW, TaskType.ASSESSMENT},
                    TaskType.values());
        }
        @Test @DisplayName("ordinal order is the locked declaration order")
        void ordinalOrder() {
            assertEquals(0, TaskType.LEARNING.ordinal());
            assertEquals(1, TaskType.IMPLEMENTATION.ordinal());
            assertEquals(2, TaskType.PROJECT.ordinal());
            assertEquals(3, TaskType.REVIEW.ordinal());
            assertEquals(4, TaskType.ASSESSMENT.ordinal());
        }
    }

    @Nested
    @DisplayName("Deterministic task identifiers")
    class DeterministicIds {
        @Test @DisplayName("id is a 64-char lower-case SHA-256 hex digest")
        void idFormat() {
            String id = PlanningTask.create("Java Basics", "Variables",
                    TaskType.LEARNING, 1).taskId();
            assertEquals(64, id.length());
            assertTrue(id.matches("^[0-9a-f]{64}$"));
        }
        @Test @DisplayName("Java Basics -> Variables (order 1) independent digest")
        void independentDigestJavaVariables() {
            assertEquals("9e8bfb76782a36a15c238ecde1c5b5b0db789064ee40716ccb4887c329a64982",
                    PlanningTask.create("Java Basics", "Variables",
                            TaskType.LEARNING, 1).taskId());
        }
        @Test @DisplayName("Java Basics -> Loops (order 2) independent digest")
        void independentDigestJavaLoops() {
            assertEquals("3e60b55c9900c81de4a459b1fd6be9316e0043d08e67edb83ec9a683eb8edf1e",
                    PlanningTask.create("Java Basics", "Loops",
                            TaskType.LEARNING, 2).taskId());
        }
        @Test @DisplayName("Java Basics -> Methods (order 3) independent digest")
        void independentDigestJavaMethods() {
            assertEquals("168757d7d7922cbe17b0cbcfc6b6c37f0dbf74f9d4c1f103dbdfd7daeb0cc1b3",
                    PlanningTask.create("Java Basics", "Methods",
                            TaskType.LEARNING, 3).taskId());
        }
        @Test @DisplayName("OOP -> Class (order 2) independent digest")
        void independentDigestOopClass() {
            assertEquals("a323c7f296d1b59e7439d49aac6222d08b924fdd5db50770059a46b38504bc3f",
                    PlanningTask.create("OOP", "Class",
                            TaskType.LEARNING, 2).taskId());
        }
        @Test @DisplayName("same inputs always produce identical ids")
        void sameInputsIdentical() {
            PlanningTask a = PlanningTask.create("Java Basics", "Variables",
                    TaskType.LEARNING, 1);
            PlanningTask b = PlanningTask.create("Java Basics", "Variables",
                    TaskType.LEARNING, 1);
            assertEquals(a.taskId(), b.taskId());
            assertEquals(a, b);
        }
        @Test @DisplayName("different title yields a different id")
        void differentTitleDifferentId() {
            assertNotEquals(
                    PlanningTask.create("Java Basics", "Variables", TaskType.LEARNING, 1).taskId(),
                    PlanningTask.create("Java Basics", "Loops", TaskType.LEARNING, 2).taskId());
        }
        @Test @DisplayName("different order yields a different id")
        void differentOrderDifferentId() {
            assertNotEquals(
                    PlanningTask.create("Java Basics", "Methods", TaskType.LEARNING, 1).taskId(),
                    PlanningTask.create("Java Basics", "Methods", TaskType.LEARNING, 9).taskId());
        }
        @Test @DisplayName("whitespace/case are normalised before hashing")
        void normalisationIsIdempotent() {
            String canonical = PlanningTask.create("Java Basics", "Variables",
                    TaskType.LEARNING, 1).taskId();
            assertEquals(canonical, PlanningTask.create("  Java  Basics  ", "  Variables  ",
                    TaskType.LEARNING, 1).taskId());
            assertEquals(canonical,
                    PlanningTask.computeTaskId("java basics", "variables", TaskType.LEARNING, 1));
        }
        @Test @DisplayName("different milestone name yields a different id")
        void differentMilestoneDifferentId() {
            assertNotEquals(
                    PlanningTask.create("Java Basics", "Methods", TaskType.LEARNING, 1).taskId(),
                    PlanningTask.create("OOP", "Methods", TaskType.IMPLEMENTATION, 1).taskId());
        }
        @Test @DisplayName("sha256Hex matches the JDK MessageDigest contract")
        void sha256HexIsDeterministic() {
            assertEquals(PlanningTask.sha256Hex("java basics|1|LEARNING|variables"),
                    "9e8bfb76782a36a15c238ecde1c5b5b0db789064ee40716ccb4887c329a64982");
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {
        @Test @DisplayName("order below 1 is rejected")
        void orderTooLow() {
            assertThrows(IllegalArgumentException.class,
                    () -> new PlanningTask(PlanningTask.sha256Hex("x"), "T", TaskType.LEARNING, 0));
        }
        @Test @DisplayName("negative order is rejected")
        void orderNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> new PlanningTask(PlanningTask.sha256Hex("x"), "T", TaskType.LEARNING, -1));
        }
        @Test @DisplayName("null taskId is rejected")
        void nullTaskId() {
            assertThrows(NullPointerException.class,
                    () -> new PlanningTask(null, "T", TaskType.LEARNING, 1));
        }
        @Test @DisplayName("blank taskId is rejected")
        void blankTaskId() {
            assertThrows(IllegalArgumentException.class,
                    () -> new PlanningTask("not-a-hash", "T", TaskType.LEARNING, 1));
        }
        @Test @DisplayName("uppercase hex taskId is rejected")
        void upperCaseTaskId() {
            String upper = PlanningTask.sha256Hex("x").toUpperCase(Locale.ROOT);
            assertThrows(IllegalArgumentException.class,
                    () -> new PlanningTask(upper, "T", TaskType.LEARNING, 1));
        }
        @Test @DisplayName("null title is rejected")
        void nullTitle() {
            assertThrows(NullPointerException.class,
                    () -> new PlanningTask(PlanningTask.sha256Hex("x"), null, TaskType.LEARNING, 1));
        }
        @Test @DisplayName("blank title is rejected")
        void blankTitle() {
            assertThrows(IllegalArgumentException.class,
                    () -> new PlanningTask(PlanningTask.sha256Hex("x"), "   ", TaskType.LEARNING, 1));
        }
        @Test @DisplayName("null type is rejected")
        void nullType() {
            assertThrows(NullPointerException.class,
                    () -> new PlanningTask(PlanningTask.sha256Hex("x"), "T", null, 1));
        }
        @Test @DisplayName("create() rejects null milestoneName")
        void createNullMilestone() {
            assertThrows(NullPointerException.class,
                    () -> PlanningTask.create(null, "T", TaskType.LEARNING, 1));
        }
        @Test @DisplayName("create() rejects blank title")
        void createBlankTitle() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlanningTask.create("M", " ", TaskType.LEARNING, 1));
        }
        @Test @DisplayName("create() rejects order below 1")
        void createBadOrder() {
            assertThrows(IllegalArgumentException.class,
                    () -> PlanningTask.create("M", "T", TaskType.LEARNING, 0));
        }
        @Test @DisplayName("isValidTaskId rejects null/short/upper-case, accepts valid")
        void validIdChecks() {
            String valid = PlanningTask.sha256Hex("x");
            assertFalse(PlanningTask.isValidTaskId(null));
            assertFalse(PlanningTask.isValidTaskId(""));
            assertFalse(PlanningTask.isValidTaskId("abc"));
            assertFalse(PlanningTask.isValidTaskId(valid.toUpperCase(Locale.ROOT)));
            assertTrue(PlanningTask.isValidTaskId(valid));
        }
    }

    @Nested
    @DisplayName("Canonical ordering")
    class CanonicalOrdering {
        @Test @DisplayName("orders ascending, then title, then id")
        void ordering() {
            PlanningTask t1 = PlanningTask.create("M", "Zebra", TaskType.LEARNING, 1);
            PlanningTask t2 = PlanningTask.create("M", "Apple", TaskType.LEARNING, 1);
            PlanningTask t3 = PlanningTask.create("M", "Apple", TaskType.LEARNING, 2);
            List<PlanningTask> sorted = new ArrayList<>(List.of(t1, t2, t3));
            sorted.sort(PlanningTask.canonicalOrder());
            assertEquals(t2, sorted.get(0));
            assertEquals(t1, sorted.get(1));
            assertEquals(t3, sorted.get(2));
        }
        @Test @DisplayName("id-tiebreak breaks equal milestone/title/order")
        void idTiebreak() {
            PlanningTask a = PlanningTask.create("M", "Apple1", TaskType.LEARNING, 1);
            PlanningTask b = PlanningTask.create("M", "Apple2", TaskType.LEARNING, 1);
            List<PlanningTask> sorted = new ArrayList<>(List.of(b, a));
            sorted.sort(PlanningTask.canonicalOrder());
            assertEquals(a, sorted.get(0));
            assertEquals(b, sorted.get(1));
        }
    }

    @Nested
    @DisplayName("Presentation")
    class Presentation {
        @Test @DisplayName("shortId is 12 characters")
        void shortIdLength() {
            assertEquals(12, PlanningTask.create("Java Basics", "Variables",
                    TaskType.LEARNING, 1).shortId().length());
        }
        @Test @DisplayName("toString is deterministic and contains type/title/order")
        void toStringContainsFields() {
            String s = PlanningTask.create("Java Basics", "Variables",
                    TaskType.LEARNING, 1).toString();
            assertTrue(s.contains("LEARNING"));
            assertTrue(s.contains("order=1"));
            assertTrue(s.contains("Variables"));
        }
    }
}

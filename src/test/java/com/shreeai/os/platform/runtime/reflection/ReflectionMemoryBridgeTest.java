package com.shreeai.os.platform.runtime.reflection;

import com.shreeai.os.platform.kernels.memory.api.MemoryService;
import com.shreeai.os.platform.kernels.memory.model.CreateMemoryRequest;
import com.shreeai.os.platform.kernels.memory.model.MemoryId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link ReflectionMemoryBridge}.
 */
class ReflectionMemoryBridgeTest {

    @Test
    void storeLessonsReturnsNullWhenNoMemoryService() {
        ReflectionMemoryBridge bridge = new ReflectionMemoryBridge(null);
        String result = bridge.storeLessons("t-1", "exec-1", "req-1", "FAILURE", 0.2, List.of("L1"));
        assertNull(result);
    }

    @Test
    void storeLessonsReturnsNullWhenNoLessons() {
        MemoryService service = mock(MemoryService.class);
        ReflectionMemoryBridge bridge = new ReflectionMemoryBridge(service);
        String result = bridge.storeLessons("t-1", "exec-1", "req-1", "SUCCESS", 0.8, List.of());
        assertNull(result);
        verifyNoInteractions(service);
    }

    @Test
    void storeLessonsReturnsNullWhenNullLessons() {
        MemoryService service = mock(MemoryService.class);
        ReflectionMemoryBridge bridge = new ReflectionMemoryBridge(service);
        String result = bridge.storeLessons("t-1", "exec-1", "req-1", "SUCCESS", 0.8, null);
        assertNull(result);
        verifyNoInteractions(service);
    }

    @Test
    void storeLessonsDelegatesToMemoryService() {
        MemoryService service = mock(MemoryService.class);
        when(service.createMemory(any(CreateMemoryRequest.class)))
                .thenReturn(new MemoryId("mem-123"));

        ReflectionMemoryBridge bridge = new ReflectionMemoryBridge(service);
        String result = bridge.storeLessons("t-1", "exec-1", "req-1", "FAILURE", 0.2, List.of("Error occurred"));

        assertEquals("mem-123", result);
        verify(service, times(1)).createMemory(any(CreateMemoryRequest.class));
    }

    @Test
    void storeLessonsHandlesServiceExceptionGracefully() {
        MemoryService service = mock(MemoryService.class);
        when(service.createMemory(any(CreateMemoryRequest.class)))
                .thenThrow(new RuntimeException("DB down"));

        ReflectionMemoryBridge bridge = new ReflectionMemoryBridge(service);
        String result = bridge.storeLessons("t-1", "exec-1", "req-1", "FAILURE", 0.2, List.of("Error"));

        assertNull(result); // Should not throw, just return null
    }

    @Test
    void rejectsNullTenantId() {
        MemoryService service = mock(MemoryService.class);
        ReflectionMemoryBridge bridge = new ReflectionMemoryBridge(service);
        assertThrows(NullPointerException.class, () ->
                bridge.storeLessons(null, "exec-1", "req-1", "SUCCESS", 0.8, List.of("L1")));
    }

    @Test
    void rejectsNullExecutionId() {
        MemoryService service = mock(MemoryService.class);
        ReflectionMemoryBridge bridge = new ReflectionMemoryBridge(service);
        assertThrows(NullPointerException.class, () ->
                bridge.storeLessons("t-1", null, "req-1", "SUCCESS", 0.8, List.of("L1")));
    }
}
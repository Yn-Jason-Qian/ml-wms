package com.wms.task.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskHeaderTest {

    private TaskHeader task;

    @BeforeEach
    void setUp() {
        task = new TaskHeader();
        task.setStatus(TaskHeader.Status.CREATED);
        task.setWarehouseId(1L);
        task.setTaskNo("TASK-20260623-001");
        task.setTaskType(TaskHeader.TaskType.PICK);
    }

    @Test
    void testRelease() {
        task.release();
        assertEquals(TaskHeader.Status.RELEASED, task.getStatus());
    }

    @Test
    void testAssign() {
        task.assign(100L);
        assertEquals(TaskHeader.Status.ASSIGNED, task.getStatus());
        assertEquals(100L, task.getAssignTo());
    }

    @Test
    void testStart() {
        task.assign(100L);
        task.start();
        assertEquals(TaskHeader.Status.EXECUTING, task.getStatus());
        assertNotNull(task.getStartTime());
    }

    @Test
    void testComplete() {
        task.assign(100L);
        task.start();
        task.complete();
        assertEquals(TaskHeader.Status.DONE, task.getStatus());
        assertNotNull(task.getEndTime());
    }

    @Test
    void testCancel() {
        task.assign(100L);
        task.cancel();
        assertEquals(TaskHeader.Status.CANCELLED, task.getStatus());
        assertNotNull(task.getEndTime(), "cancel should set endTime");
    }

    @Test
    void testCancelFromCreated() {
        task.cancel();
        assertEquals(TaskHeader.Status.CANCELLED, task.getStatus());
    }

    @Test
    void testCancelFromExecuting() {
        task.assign(100L);
        task.start();
        task.cancel();
        assertEquals(TaskHeader.Status.CANCELLED, task.getStatus());
    }

    @Test
    void testCompleteWithoutStart() {
        // 允许跳过 start 直接 complete（管理后台强制完成）
        task.complete();
        assertEquals(TaskHeader.Status.DONE, task.getStatus());
        assertNull(task.getStartTime(), "startTime should be null if not started");
    }

    @Test
    void testAllStatusConstants() {
        assertEquals("CREATED", TaskHeader.Status.CREATED.name());
        assertEquals("RELEASED", TaskHeader.Status.RELEASED.name());
        assertEquals("ASSIGNED", TaskHeader.Status.ASSIGNED.name());
        assertEquals("EXECUTING", TaskHeader.Status.EXECUTING.name());
        assertEquals("DONE", TaskHeader.Status.DONE.name());
        assertEquals("CANCELLED", TaskHeader.Status.CANCELLED.name());
    }

    @Test
    void testFullLifecycle() {
        assertEquals(TaskHeader.Status.CREATED, task.getStatus());

        task.release();
        assertEquals(TaskHeader.Status.RELEASED, task.getStatus());

        task.assign(200L);
        assertEquals(TaskHeader.Status.ASSIGNED, task.getStatus());

        task.start();
        assertEquals(TaskHeader.Status.EXECUTING, task.getStatus());

        task.complete();
        assertEquals(TaskHeader.Status.DONE, task.getStatus());
        assertNotNull(task.getStartTime());
        assertNotNull(task.getEndTime());
    }
}

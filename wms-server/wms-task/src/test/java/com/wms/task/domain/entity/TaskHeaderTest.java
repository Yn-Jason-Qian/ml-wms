package com.wms.task.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TaskHeaderTest {

    private TaskHeader task;

    @BeforeEach
    void setUp() {
        task = new TaskHeader();
        task.setStatus(TaskHeader.STATUS_CREATED);
        task.setWarehouseId(1L);
        task.setTaskNo("TASK-20260623-001");
        task.setTaskType("PICK");
    }

    @Test
    void testRelease() {
        task.release();
        assertEquals(TaskHeader.STATUS_RELEASED, task.getStatus());
    }

    @Test
    void testAssign() {
        task.assign(100L);
        assertEquals(TaskHeader.STATUS_ASSIGNED, task.getStatus());
        assertEquals(100L, task.getAssignTo());
    }

    @Test
    void testStart() {
        task.assign(100L);
        task.start();
        assertEquals(TaskHeader.STATUS_EXECUTING, task.getStatus());
        assertNotNull(task.getStartTime());
    }

    @Test
    void testComplete() {
        task.assign(100L);
        task.start();
        task.complete();
        assertEquals(TaskHeader.STATUS_DONE, task.getStatus());
        assertNotNull(task.getEndTime());
    }

    @Test
    void testCancel() {
        task.assign(100L);
        task.cancel();
        assertEquals(TaskHeader.STATUS_CANCELLED, task.getStatus());
        assertNotNull(task.getEndTime(), "cancel should set endTime");
    }

    @Test
    void testCancelFromCreated() {
        task.cancel();
        assertEquals(TaskHeader.STATUS_CANCELLED, task.getStatus());
    }

    @Test
    void testCancelFromExecuting() {
        task.assign(100L);
        task.start();
        task.cancel();
        assertEquals(TaskHeader.STATUS_CANCELLED, task.getStatus());
    }

    @Test
    void testCompleteWithoutStart() {
        // 允许跳过 start 直接 complete（管理后台强制完成）
        task.complete();
        assertEquals(TaskHeader.STATUS_DONE, task.getStatus());
        assertNull(task.getStartTime(), "startTime should be null if not started");
    }

    @Test
    void testAllStatusConstants() {
        assertEquals("CREATED", TaskHeader.STATUS_CREATED);
        assertEquals("RELEASED", TaskHeader.STATUS_RELEASED);
        assertEquals("ASSIGNED", TaskHeader.STATUS_ASSIGNED);
        assertEquals("EXECUTING", TaskHeader.STATUS_EXECUTING);
        assertEquals("DONE", TaskHeader.STATUS_DONE);
        assertEquals("CANCELLED", TaskHeader.STATUS_CANCELLED);
    }

    @Test
    void testFullLifecycle() {
        assertEquals(TaskHeader.STATUS_CREATED, task.getStatus());

        task.release();
        assertEquals(TaskHeader.STATUS_RELEASED, task.getStatus());

        task.assign(200L);
        assertEquals(TaskHeader.STATUS_ASSIGNED, task.getStatus());

        task.start();
        assertEquals(TaskHeader.STATUS_EXECUTING, task.getStatus());

        task.complete();
        assertEquals(TaskHeader.STATUS_DONE, task.getStatus());
        assertNotNull(task.getStartTime());
        assertNotNull(task.getEndTime());
    }
}

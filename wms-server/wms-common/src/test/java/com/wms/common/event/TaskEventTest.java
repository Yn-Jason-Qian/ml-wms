package com.wms.common.event;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.Map;

class TaskEventTest {

    @Test
    void testEventCreation() {
        TaskEvent event = new TaskEvent(this, "COMPLETED", 100L, "TASK-001", "PICK", 1L, 42L);

        assertEquals("COMPLETED", event.getEventType());
        assertEquals(100L, event.getTaskId());
        assertEquals("TASK-001", event.getTaskNo());
        assertEquals("PICK", event.getTaskType());
        assertEquals(1L, event.getWarehouseId());
        assertEquals(42L, event.getAssignTo());
    }

    @Test
    void testToMap() {
        TaskEvent event = new TaskEvent(this, "CLAIMED", 200L, "TASK-002", "PUTAWAY", 3L, 99L);

        Map<String, Object> map = event.toMap();

        assertEquals("CLAIMED", map.get("eventType"));
        assertEquals(200L, map.get("taskId"));
        assertEquals("TASK-002", map.get("taskNo"));
        assertEquals("PUTAWAY", map.get("taskType"));
        assertEquals(3L, map.get("warehouseId"));
        assertEquals(99L, map.get("assignTo"));
        assertNotNull(map.get("timestamp"));
    }

    @Test
    void testToMapWithNulls() {
        TaskEvent event = new TaskEvent(this, "NEW", 1L, null, null, null, null);

        Map<String, Object> map = event.toMap();

        assertEquals("", map.get("taskNo"));
        assertEquals("", map.get("taskType"));
        assertEquals(0L, map.get("warehouseId"));
        assertEquals(0L, map.get("assignTo"));
    }

    @Test
    void testEventTypes() {
        String[] types = {"CLAIMED", "STARTED", "COMPLETED", "CANCELLED", "NEW"};
        for (String type : types) {
            TaskEvent e = new TaskEvent(this, type, 1L, "T", "T", 1L, 1L);
            assertEquals(type, e.getEventType());
        }
    }
}

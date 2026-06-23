package com.wms.web.event;

import com.wms.common.event.TaskEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 监听领域事件，通过 STOMP WebSocket 广播到前端
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 任务事件 → 广播到 /topic/tasks
     * PDA/Web 端订阅该主题即可收到实时任务通知
     */
    @Async
    @EventListener
    public void onTaskEvent(TaskEvent event) {
        messagingTemplate.convertAndSend("/topic/tasks", event.toMap());

        if (event.getAssignTo() != null && event.getAssignTo() > 0) {
            messagingTemplate.convertAndSend(
                "/queue/tasks/" + event.getAssignTo(),
                event.toMap()
            );
        }
    }

    /**
     * 库存变更事件广播（供 Dashboard 实时更新）
     */
    public void broadcastInventoryUpdate(Long warehouseId, String changeType, String detail) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("warehouseId", warehouseId);
        payload.put("changeType", changeType);
        payload.put("detail", detail != null ? detail : "");
        payload.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/inventory", payload);
    }
}

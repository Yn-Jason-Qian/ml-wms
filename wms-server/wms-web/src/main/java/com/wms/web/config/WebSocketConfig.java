package com.wms.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP 配置
 * 用于 PDA 任务实时推送 和 Web 端 Dashboard 状态更新
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅这些前缀（服务端 → 客户端广播）
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息的前缀（客户端 → 服务端）
        registry.setApplicationDestinationPrefixes("/ws");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 端点，客户端通过此地址连接 WebSocket
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS 回退（兼容不支持 WebSocket 的环境）
    }
}

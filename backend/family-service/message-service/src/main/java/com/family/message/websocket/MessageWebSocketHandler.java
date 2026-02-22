package com.family.message.websocket;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket消息处理器
 */
@Slf4j
@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {
    
    private final ObjectMapper objectMapper;
    
    /**
     * 存储所有连接的Session
     */
    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    
    /**
     * 用户ID与Session的映射
     */
    private static final ConcurrentHashMap<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    public MessageWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("用户 {} 连接WebSocket成功, Session ID: {}", userId, session.getId());
            
            // 发送连接成功消息
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        Map.of("type", "connected", "message", "连接成功")
                )));
            } catch (Exception e) {
                log.error("发送连接成功消息失败", e);
            }
        } else {
            log.warn("未登录用户尝试连接WebSocket, Session ID: {}", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
        }
        
        sessions.add(session);
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到消息: {}", payload);
        
        try {
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.get("type");
            
            if ("ping".equals(type)) {
                // 心跳响应
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        Map.of("type", "pong", "timestamp", System.currentTimeMillis())
                )));
            } else if ("read".equals(type)) {
                // 客户端通知已读
                Long messageId = Long.valueOf(msg.get("messageId").toString());
                log.info("客户端标记消息已读: {}", messageId);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", e.getMessage());
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = getUserIdFromSession(session);
        log.error("WebSocket传输错误, User: {}, Error: {}", userId, exception.getMessage());
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        
        sessions.remove(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("用户 {} 断开WebSocket连接, Status: {}", userId, status);
        }
    }
    
    /**
     * 发送消息给指定用户
     */
    public void sendMessageToUser(Long userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.debug("发送消息给用户 {} 成功", userId);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败: {}", userId, e.getMessage());
            }
        } else {
            log.debug("用户 {} 不在线，消息将保留待推送", userId);
        }
    }
    
    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error("广播消息失败: {}", e.getMessage());
                }
            }
        }
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineCount() {
        return userSessions.size();
    }
    
    /**
     * 从Session中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        try {
            // 从URL参数中获取token
            String query = session.getUri().getQuery();
            if (StrUtil.isNotBlank(query)) {
                String[] params = query.split("\u0026");
                for (String param : params) {
                    String[] kv = param.split("=");
                    if (kv.length == 2 && "token".equals(kv[0])) {
                        String token = kv[1];
                        if (StpUtil.getLoginIdDefaultNull() != null) {
                            return StpUtil.getLoginIdAsLong();
                        }
                    }
                }
            }
            
            // 从attributes中获取
            Object userId = session.getAttributes().get("userId");
            if (userId != null) {
                return Long.valueOf(userId.toString());
            }
        } catch (Exception e) {
            log.error("获取用户ID失败: {}", e.getMessage());
        }
        return null;
    }
}

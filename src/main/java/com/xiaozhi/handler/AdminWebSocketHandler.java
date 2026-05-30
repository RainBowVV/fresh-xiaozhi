package com.xiaozhi.handler;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AdminWebSocketHandler extends TextWebSocketHandler {

    /** 管理员连接会话：sessionId -> session */
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("管理员WebSocket连接建立，sessionId={}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("管理员WebSocket连接关闭，sessionId={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("收到消息：{}", message.getPayload());
    }

    /**
     * 向所有管理员推送消息
     */
    public void sendToAll(Object data) {
        String json = JSONUtil.toJsonStr(data);
        TextMessage message = new TextMessage(json);
        sessions.forEach((id, session) -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (Exception e) {
                log.error("WebSocket推送失败，sessionId={}", id, e);
            }
        });
    }

    /**
     * 获取当前在线管理员数量
     */
    public int getOnlineCount() {
        return sessions.size();
    }
}

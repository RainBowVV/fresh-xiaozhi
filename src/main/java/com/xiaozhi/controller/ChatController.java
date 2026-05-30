package com.xiaozhi.controller;

import com.xiaozhi.annotation.RequireAuth;
import com.xiaozhi.util.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    /**
     * 发送聊天消息
     * @param message 消息内容
     * @param conversationId 会话ID
     * @return 回复消息流
     */
    @RequireAuth
    @RequestMapping("/send")
    public Flux<String> chat(String message, String conversationId) {
        log.info("chat message: {}, conversationId: {}", message, conversationId);
        Long userId = UserContext.getUserId();
        return chatClient.prompt()
                .user(message)
                .toolContext(Map.of("userId", userId))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }

    /**
     * 清除会话历史记录
     * @param conversationId 会话ID
     */
    @RequestMapping("/clear")
    public void clear(String conversationId) {
        log.info("clear conversationId: {}", conversationId);
        chatMemory.clear(conversationId);
    }
}

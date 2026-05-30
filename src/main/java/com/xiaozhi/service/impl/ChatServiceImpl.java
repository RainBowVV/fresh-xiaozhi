package com.xiaozhi.service.impl;

import com.xiaozhi.service.ChatService;
import com.xiaozhi.vo.ChatResponseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final String SYSTEM_PROMPT =
        "你是「鲜小智」生鲜平台的 AI 智能客服助手。\n" +
        "你的职责是帮助用户了解平台商品、推荐菜品、解答疑问。\n" +
        "请用中文回复，语气亲切友好，回复简洁（200字以内）。";

    private final ChatClient chatClient;

    @Override
    public ChatResponseVO chat(Long userId, String conversationId, String message) {
        log.info("[AI客服] 会话ID: {}, 用户ID: {}, 输入: {}", conversationId, userId, message);

        String response = chatClient.prompt()
            .system(SYSTEM_PROMPT)
            .user(message)
            .call()
            .content();

        log.info("[AI客服] 会话ID: {}, 输出: {}", conversationId, response);

        return new ChatResponseVO(response, conversationId);
    }
}

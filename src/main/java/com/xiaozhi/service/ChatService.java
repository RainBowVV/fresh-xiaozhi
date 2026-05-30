package com.xiaozhi.service;

import com.xiaozhi.vo.ChatResponseVO;

public interface ChatService {
    ChatResponseVO chat(Long userId, String conversationId, String message);
}

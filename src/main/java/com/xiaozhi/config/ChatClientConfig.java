package com.xiaozhi.config;

import com.xiaozhi.constant.SystemPromptConstant;
import com.xiaozhi.tools.CartTools;
import com.xiaozhi.tools.CategoryTools;
import com.xiaozhi.tools.OrderTools;
import com.xiaozhi.tools.ProductTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(30)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder,
                                 ChatMemory chatMemory,
                                 CategoryTools categoryTools,
                                 ProductTools productTools,
                                 OrderTools orderTools,
                                 CartTools cartTools) {
        return builder
                .defaultSystem(SystemPromptConstant.SERVICE_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build()
                        )
                .defaultTools(categoryTools, productTools, orderTools, cartTools)
                .build();
    }
}
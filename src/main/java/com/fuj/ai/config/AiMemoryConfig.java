package com.fuj.ai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 会话记忆相关配置。
 *
 * <p>提供带窗口上限的会话记忆组件,供多轮对话时保存/读取上下文。</p>
 *
 * @author foj-ai-service
 */
@Configuration
public class AiMemoryConfig {

    /**
     * 提供带窗口上限的会话记忆组件。
     *
     * <p>为什么限制窗口:多轮对话时,每次请求都会把历史消息一并携带发给模型,
     * 历史越长 token 消耗越大、成本越高,也可能超过模型的上下文长度导致截断或报错。
     * 这里把窗口上限设为 20 条,在"记住足够上下文"与"控制成本"之间取得平衡。</p>
     *
     * @return 窗口上限 20 条消息的 ChatMemory
     */
    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
            .maxMessages(20)
            .build();
    }
}

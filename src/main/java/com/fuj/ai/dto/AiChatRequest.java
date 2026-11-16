package com.fuj.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI 多轮对话请求体。
 *
 * <p>用于 {@code POST /ai/chat} 与 {@code POST /ai/chat/stream} 接口,
 * 承载用户提示词与会话 ID。</p>
 *
 * @author foj-ai-service
 */
public class AiChatRequest {

    /**
     * 用户提示词。
     *
     * <p>必填且不能为空白;长度限制 1~2000 字符。</p>
     */
    @NotBlank(message = "prompt 不能为空")
    @Size(min = 1, max = 2000, message = "prompt 长度必须在 1~2000 之间")
    private String prompt;

    /**
     * 会话 ID。
     *
     * <p>必填,由调用方生成 UUID 传入;同一 ID 的多轮请求共享上下文。</p>
     */
    @NotBlank(message = "conversationId 不能为空")
    private String conversationId;

    /**
     * 获取提示词。
     *
     * @return 用户提示词
     */
    public String getPrompt() {
        return prompt;
    }

    /**
     * 设置提示词。
     *
     * @param prompt 用户提示词
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * 获取会话 ID。
     *
     * @return 会话 ID
     */
    public String getConversationId() {
        return conversationId;
    }

    /**
     * 设置会话 ID。
     *
     * @param conversationId 会话 ID
     */
    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}

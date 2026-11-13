package com.fuj.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * AI 测试接口请求体。
 *
 * <p>对应 {@code POST /ai/test} 接口的入参,承载用户输入的提示词。</p>
 *
 * @author foj-ai-service
 */
public class AiTestRequest {

    /**
     * 用户提示词。
     *
     * <p>必填且不能为空白;长度限制 1~2000 字符,防止超长输入拖慢甚至拖垮模型调用。</p>
     */
    @NotBlank(message = "prompt 不能为空")
    @Size(min = 1, max = 2000, message = "prompt 长度必须在 1~2000 之间")
    private String prompt;

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
}

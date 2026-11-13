package com.fuj.ai.controller;

import com.fuj.ai.dto.AiTestRequest;
import com.fuj.ai.dto.AiTestResponse;
import com.fuj.ai.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 测试接口控制器。
 *
 * <p>暴露 {@code POST /ai/test} 接口,供人工验证大模型调用链路是否打通。</p>
 *
 * @author foj-ai-service
 */
@RestController
public class AiTestController {

    private final AiChatService aiChatService;

    /**
     * 构造 AI 测试控制器。
     *
     * @param aiChatService AI 对话服务
     */
    public AiTestController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * AI 测试接口。
     *
     * <p>接收用户提示词,调用大模型并返回回复。请求体使用 {@link Valid} 校验,
     * 校验失败时 Spring MVC 自动返回 HTTP 400。</p>
     *
     * @param request 请求体(包含必填的 prompt)
     * @return AI 调用结果(成功或降级)
     */
    @PostMapping("/ai/test")
    public AiTestResponse test(@RequestBody @Valid AiTestRequest request) {
        return aiChatService.chat(request.getPrompt());
    }
}

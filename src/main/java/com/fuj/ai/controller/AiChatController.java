package com.fuj.ai.controller;

import com.fuj.ai.dto.AiChatRequest;
import com.fuj.ai.dto.AiTestResponse;
import com.fuj.ai.service.AiChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 多轮对话控制器。
 *
 * <p>提供同步多轮对话与 SSE 流式对话两个接口。</p>
 *
 * @author foj-ai-service
 */
@RestController
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * 构造 AI 多轮对话控制器。
     *
     * @param aiChatService AI 对话服务
     */
    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * 同步多轮对话接口。
     *
     * <p>同一 conversationId 的多轮请求,AI 能记住上下文;校验失败返回 400。</p>
     *
     * @param request 请求体(含 prompt 与 conversationId)
     * @return 统一响应(成功或降级)
     */
    @PostMapping("/ai/chat")
    public AiTestResponse chat(@RequestBody @Valid AiChatRequest request) {
        return aiChatService.chatWithMemory(request.getConversationId(), request.getPrompt());
    }

    /**
     * SSE 流式多轮对话接口。
     *
     * <p>以 {@code text/event-stream} 逐段推送回复(打字机效果),由 Spring MVC
     * 自动把 {@link Flux} 元素序列化为 SSE 事件;校验失败返回 400。</p>
     *
     * @param request 请求体(含 prompt 与 conversationId)
     * @return 逐段推送的回复内容流
     */
    @PostMapping(value = "/ai/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody @Valid AiChatRequest request) {
        return aiChatService.streamChat(request.getConversationId(), request.getPrompt());
    }
}

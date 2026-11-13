package com.fuj.ai.service;

import com.fuj.ai.dto.AiTestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AI 对话服务。
 *
 * <p>封装大模型调用:注入 Spring AI 的 {@link ChatClient.Builder},构建带系统角色定义的
 * ChatClient,并提供带超时降级保护的对话方法。模型名从配置读取,代码中不写死。</p>
 *
 * @author foj-ai-service
 */
@Service
public class AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatService.class);

    /**
     * 系统角色定义。
     *
     * <p>把模型定位为 FOJ 的编程学习助手,约束其回答范围与风格,
     * 避免答非所问或直接给出完整可提交的题解。</p>
     */
    private static final String SYSTEM_PROMPT = """
            你是 FOJ 在线判题平台的 AI 编程助手。
            职责:解答算法、数据结构、Java/Python/C++ 语法、代码调试思路相关问题。
            边界:仅回答编程与算法相关话题;其他话题请礼貌引导用户回到编程学习。
            风格:简洁清晰,先给关键思路,再给必要代码;不要直接给完整可提交的题解,
            引导用户自己动手,符合学习辅助定位。""";

    /** 构建好的 ChatClient,已注入系统角色定义。 */
    private final ChatClient chatClient;

    /** 当前使用的模型名,从配置读取(代码中不写死模型名)。 */
    private final String model;

    /**
     * 构造 AI 对话服务。
     *
     * @param chatClientBuilder Spring AI 提供的 ChatClient 构造器
     * @param model             模型名(读取自 spring.ai.openai.chat.options.model)
     */
    public AiChatService(ChatClient.Builder chatClientBuilder,
                         @Value("${spring.ai.openai.chat.options.model}") String model) {
        // 只设置系统角色;模型 / 温度 / max-tokens 等参数全部交由 application.yml 管理
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
        this.model = model;
    }

    /**
     * 调用大模型进行对话。
     *
     * <p>任何异常都会被捕获并降级为固定文案返回,绝不向上抛出,
     * 保证 HTTP / MQ 链路不被 AI 调用失败所中断。</p>
     *
     * @param userPrompt 用户提示词
     * @return 成功时携带 AI 回复与模型名;失败时携带降级文案与 model="none"
     */
    public AiTestResponse chat(String userPrompt) {
        long start = System.currentTimeMillis();
        AiTestResponse response = new AiTestResponse();
        try {
            String content = chatClient.prompt().user(userPrompt).call().content();
            response.setSuccess(true);
            // 模型可能返回空内容,做一次判空兜底,避免把 null 返回给调用方
            response.setContent(content == null ? "" : content);
            response.setModel(model);
        } catch (Exception e) {
            // 降级红线:AI 链路异常时记录日志并返回占位文案,不影响调用方
            log.error("AI 调用失败,已降级返回占位文案。prompt 长度={}", userPrompt.length(), e);
            response.setSuccess(false);
            response.setContent("AI 服务暂不可用,请稍后再试");
            response.setModel("none");
        } finally {
            // 无论成功失败都记录耗时,便于观测超时与性能
            response.setCostMs(System.currentTimeMillis() - start);
        }
        return response;
    }
}

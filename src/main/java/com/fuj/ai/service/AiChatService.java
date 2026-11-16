package com.fuj.ai.service;

import com.fuj.ai.dto.AiTestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI 对话服务。
 *
 * <p>封装大模型调用:注入 Spring AI 的 {@link ChatClient.Builder},分别构建
 * 无记忆的单轮客户端与带会话记忆的多轮客户端;提供单轮 / 多轮 / 流式三种对话方式,
 * 均带超时降级保护。模型名从配置读取,代码中不写死。</p>
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

            【职责】解答算法、数据结构、Java/Python/C++ 语法、代码调试思路相关问题。

            【硬性规则,必须遵守】
            例外:用户询问与自己名字、偏好、刚才对话中提到的内容等上下文相关的问题时,
            基于历史消息正常回答,这属于正常对话,不算违规话题。
            1. 只回答编程与算法相关话题;
            2. 遇到写诗、作文、闲聊天气、时事政治等与编程无关的请求,一律拒绝,
               固定回复:"我是 FOJ 的编程助手,只能帮你解答算法和编程问题~ 有代码或算法上的疑问随时问我!";
            3. 不直接给完整可提交的题解,先给关键思路再给必要代码,引导用户自己动手。

            【示例】
            用户:帮我写一首情诗
            正确回复:我是 FOJ 的编程助手,只能帮你解答算法和编程问题~ 有代码或算法上的疑问随时问我!

            用户:帮我写一篇游记
            正确回复:我是 FOJ 的编程助手,只能帮你解答算法和编程问题~ 有代码或算法上的疑问随时问我!

            用户:快速排序怎么理解?
            正常解答:核心是分治+分区……(按职责正常回答)

            用户:你好,我叫小明,我最喜欢用 Java
            助手:你好,小明!记住了,你最喜欢用 Java。有什么编程问题需要帮忙?
            用户:我最喜欢什么语言?
            助手:你最喜欢用 Java~
            用户:帮我写一首情诗
            助手:我是 FOJ 的编程助手,只能帮你解答算法和编程问题~ 有代码或算法上的疑问随时问我!

            用户:我叫什么名字?
            助手:你刚才说过你叫小陈呀~(根据对话历史回答)
            用户:帮我写一首情诗
            助手:我是 FOJ 的编程助手,只能帮你解答算法和编程问题~

            用户:你好,我叫小李
            助手:你好,小李!有什么编程问题需要帮忙?

            用户:我叫什么名字?(此前对话中没有提过名字)
            助手:咦,你还没告诉过我你的名字呢~ 说了我就记住啦!""";

    /** 无记忆的单轮 ChatClient,仅供 {@link #chat(String)} 使用,不写会话记忆。 */
    private final ChatClient statelessChatClient;

    /** 带会话记忆的 ChatClient,供 {@link #chatWithMemory(String, String)} 与 {@link #streamChat(String, String)} 使用。 */
    private final ChatClient memoryChatClient;

    /** 当前使用的模型名,从配置读取(代码中不写死模型名)。 */
    private final String model;

    /**
     * 构造 AI 对话服务。
     *
     * @param chatClientBuilder Spring AI 提供的 ChatClient 构造器
     * @param chatMemory        会话记忆组件(窗口上限 20 条)
     * @param model             模型名(读取自 spring.ai.openai.chat.options.model)
     */
    public AiChatService(ChatClient.Builder chatClientBuilder,
                         ChatMemory chatMemory,
                         @Value("${spring.ai.openai.chat.options.model}") String model) {
        // 无状态单轮客户端:只挂系统角色,不挂记忆 advisor,
        // 避免 /ai/test 这类无 conversationId 的请求触发记忆 advisor 抛异常。
        this.statelessChatClient = chatClientBuilder.clone()
            .defaultSystem(SYSTEM_PROMPT)
            .build();
        // 带记忆客户端:追加记忆 advisor,按 conversationId 隔离上下文,仅供多轮 / 流式接口使用。
        this.memoryChatClient = chatClientBuilder.clone()
            .defaultSystem(SYSTEM_PROMPT)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
        // 模型 / 温度 / max-tokens 等参数全部交由 application.yml 管理,代码中不写死。
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
            String content = statelessChatClient.prompt().user(userPrompt).call().content();
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

    /**
     * 多轮对话(带会话记忆)。
     *
     * <p>通过 advisor 参数把本次请求绑定到指定 conversationId,使同一会话的多轮请求
     * 共享上下文;异常时降级返回占位文案,绝不向上抛出。</p>
     *
     * @param conversationId 会话 ID(由调用方生成,用于隔离不同会话的上下文)
     * @param userPrompt     用户提示词
     * @return AI 响应(成功或降级)
     */
    public AiTestResponse chatWithMemory(String conversationId, String userPrompt) {
        long start = System.currentTimeMillis();
        AiTestResponse response = new AiTestResponse();
        try {
            String content = memoryChatClient.prompt()
                .user(userPrompt)
                // 绑定会话 ID:记忆 advisor 会据此读写对应会话的历史消息
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
            response.setSuccess(true);
            response.setContent(content == null ? "" : content);
            response.setModel(model);
        } catch (Exception e) {
            // 降级红线:异常时记录日志并返回占位文案,不影响调用方
            log.error("AI 多轮对话失败,已降级返回占位文案。conversationId={}, prompt 长度={}",
                    conversationId, userPrompt.length(), e);
            response.setSuccess(false);
            response.setContent("AI 服务暂不可用,请稍后再试");
            response.setModel("none");
        } finally {
            response.setCostMs(System.currentTimeMillis() - start);
        }
        return response;
    }

    /**
     * 流式多轮对话(SSE 打字机效果)。
     *
     * <p>返回 {@link Flux} 逐段推送回复内容,由 Spring MVC 以 SSE 事件输出;
     * 出错时通过 {@code onErrorResume} 兜底,向前端推送降级文案而非 500 堆栈。</p>
     *
     * @param conversationId 会话 ID
     * @param userPrompt     用户提示词
     * @return 逐段推送的回复内容流
     */
    public Flux<String> streamChat(String conversationId, String userPrompt) {
        return memoryChatClient.prompt()
            .user(userPrompt)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .stream()
            .content()
            // 兜底:流中途出错时以 SSE 事件返回降级文案,避免把异常堆栈抛给前端
            .onErrorResume(e -> {
                log.error("AI 流式对话失败,已降级返回错误文案。conversationId={}", conversationId, e);
                return Flux.just("AI 服务暂不可用,请稍后再试");
            });
    }
}

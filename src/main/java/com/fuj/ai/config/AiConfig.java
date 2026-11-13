package com.fuj.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * AI 调用相关的基础设施配置。
 *
 * <p>核心职责:为 Spring AI 提供一个带显式超时控制的 {@link RestClient.Builder},
 * 防止大模型接口卡死时把调用方(HTTP 接口 / MQ 消费)一起拖垮。</p>
 *
 * @author foj-ai-service
 */
@Configuration
public class AiConfig {

    /**
     * 提供带连接/读取超时的 {@link RestClient.Builder}。
     *
     * <p>为什么必须显式配置超时:Spring AI 底层通过 RestClient 发起 HTTP 调用,
     * 若沿用默认客户端,连接或读取在网络异常时可能无限期阻塞,进而占满线程池、
     * 拖垮整个服务。这里显式设置连接超时 5 秒、读取超时 30 秒,保证单次 AI 调用
     * 最多阻塞 30 秒即返回,配合服务层的降级逻辑实现"不阻塞、可降级"。</p>
     *
     * <p>Spring AI 的 OpenAI 自动配置会通过 {@code ObjectProvider&lt;RestClient.Builder&gt;}
     * 拾取本 Bean 来构造底层 HTTP 客户端,因此这里定义后即对 AI 调用生效。</p>
     *
     * @return 配置了超时策略的 RestClient 构造器
     */
    @Bean
    RestClient.Builder restClientBuilder() {
        // 基于 JDK 内置 HttpClient 构建,连接超时 5 秒(仅控制建立 TCP 连接阶段)
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        // 读取超时 30 秒:控制从发出请求到收齐响应体之间的最长等待时间
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        return RestClient.builder().requestFactory(requestFactory);
    }
}

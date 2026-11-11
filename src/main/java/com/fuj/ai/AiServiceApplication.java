package com.fuj.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * foj-ai-service 应用启动类。
 *
 * <p>本服务是独立于 FOJ 主仓库的 AI 微服务,职责如下:</p>
 * <ul>
 *     <li>通过 RabbitMQ 接收判题完成事件;</li>
 *     <li>调用 DeepSeek(经 Spring AI)生成 AI 分析;</li>
 *     <li>通过 HTTP 回调 FOJ 老服务接口。</li>
 * </ul>
 *
 * @author foj-ai-service
 */
@SpringBootApplication
public class AiServiceApplication {

    /**
     * 应用入口,启动 Spring Boot 容器。
     *
     * @param args 命令行启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}

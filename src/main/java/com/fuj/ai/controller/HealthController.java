package com.fuj.ai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查控制器。
 *
 * <p>提供轻量的探活接口,用于人工或探针快速确认服务是否正常运行。</p>
 *
 * @author foj-ai-service
 */
@RestController
public class HealthController {

    /**
     * 健康检查接口。
     *
     * @return 固定的健康状态文案
     */
    @GetMapping("/health")
    public String health() {
        return "foj-ai-service running, port 8085";
    }
}

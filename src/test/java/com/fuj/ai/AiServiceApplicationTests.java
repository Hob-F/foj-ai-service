package com.fuj.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 应用上下文加载测试。
 *
 * <p>验证 Spring 容器能够正常启动并装配全部 Bean,作为项目骨架的冒烟测试。</p>
 *
 * @author foj-ai-service
 */
@SpringBootTest
class AiServiceApplicationTests {

    /**
     * 上下文加载冒烟测试。
     *
     * <p>仅验证容器可正常启动,无具体业务断言。</p>
     */
    @Test
    void contextLoads() {
    }
}

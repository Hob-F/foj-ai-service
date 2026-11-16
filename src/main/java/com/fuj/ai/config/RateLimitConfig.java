package com.fuj.ai.config;

import com.fuj.ai.ratelimit.IpRateLimiter;
import com.fuj.ai.ratelimit.RateLimitInterceptor;
import com.fuj.ai.ratelimit.RateLimitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 限流相关配置。
 *
 * <p>注册按 IP 限流拦截器,并启用限流配置属性绑定。</p>
 *
 * @author foj-ai-service
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig implements WebMvcConfigurer {

    private final IpRateLimiter ipRateLimiter;

    /**
     * 构造限流配置。
     *
     * @param ipRateLimiter IP 令牌桶限流器
     */
    public RateLimitConfig(IpRateLimiter ipRateLimiter) {
        this.ipRateLimiter = ipRateLimiter;
    }

    /**
     * 注册限流拦截器,覆盖 AI 相关的三个端点。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(ipRateLimiter))
            .addPathPatterns("/ai/chat", "/ai/chat/stream", "/ai/test");
    }
}

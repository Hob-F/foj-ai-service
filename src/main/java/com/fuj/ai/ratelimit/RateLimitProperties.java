package com.fuj.ai.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流配置属性。
 *
 * <p>对应配置前缀 {@code ai.rate-limit},可在 application.yml 中覆盖默认值。</p>
 *
 * @author foj-ai-service
 */
@ConfigurationProperties(prefix = "ai.rate-limit")
public class RateLimitProperties {

    /** 令牌桶容量:每个时间窗口允许的最大请求数,默认 10。 */
    private int capacity = 10;

    /** 令牌补充窗口长度(秒),默认 60 秒补满一桶。 */
    private int windowSeconds = 60;

    /**
     * 获取令牌桶容量。
     *
     * @return 容量
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * 设置令牌桶容量。
     *
     * @param capacity 容量
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * 获取窗口秒数。
     *
     * @return 窗口秒数
     */
    public int getWindowSeconds() {
        return windowSeconds;
    }

    /**
     * 设置窗口秒数。
     *
     * @param windowSeconds 窗口秒数
     */
    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }
}

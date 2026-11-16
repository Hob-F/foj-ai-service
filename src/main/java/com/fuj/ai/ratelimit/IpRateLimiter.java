package com.fuj.ai.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于来源 IP 的令牌桶限流器。
 *
 * <p>每个 IP 维护一个独立令牌桶:请求进来消费一个令牌,桶空即拒绝。</p>
 *
 * @author foj-ai-service
 */
@Component
public class IpRateLimiter {

    /** IP 桶数量的上限,超过则整体清空,防止 Map 无界增长导致内存泄漏。 */
    private static final int MAX_BUCKETS = 10000;

    /** 每个 IP 对应的令牌桶。 */
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /** 令牌桶容量。 */
    private final int capacity;

    /** 令牌补充窗口(秒)。 */
    private final int windowSeconds;

    /**
     * 构造限流器。
     *
     * @param properties 限流配置(容量与窗口)
     */
    public IpRateLimiter(RateLimitProperties properties) {
        this.capacity = properties.getCapacity();
        this.windowSeconds = properties.getWindowSeconds();
    }

    /**
     * 尝试为指定 IP 消费一个令牌。
     *
     * @param ip 来源 IP
     * @return true 表示放行,false 表示已超限
     */
    public boolean tryAcquire(String ip) {
        Bucket bucket = buckets.get(ip);
        if (bucket == null) {
            // 容量保护:桶数量超过上限时整体清空,用"简单全清"策略兜底防内存泄漏
            if (buckets.size() >= MAX_BUCKETS) {
                buckets.clear();
            }
            bucket = buckets.computeIfAbsent(ip, k -> newBucket());
        }
        return bucket.tryConsume(1);
    }

    /**
     * 创建一个装满容量、按窗口匀速补充的令牌桶。
     *
     * @return 新的令牌桶
     */
    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(capacity, Duration.ofSeconds(windowSeconds))
            .build();
        return Bucket.builder().addLimit(limit).build();
    }
}

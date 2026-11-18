package com.fuj.ai.service;

import com.fuj.ai.exception.BizException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 会话归属服务。
 *
 * <p>用 Redis SETNX 绑定 {@code sessionId -> userId} 的归属关系:首个访问者建档,
 * 后续访问者若不是同一用户则拒绝,防止跨用户读取他人会话上下文。</p>
 *
 * @author foj-ai-service
 */
@Service
public class SessionOwnerService {

    /** 会话归属 Redis key 前缀。 */
    private static final String KEY_PREFIX = "ai:session:owner:";

    /** 越权访问会话的错误码。 */
    private static final int FORBIDDEN_CODE = 40300;

    /** 未登录的错误码。 */
    private static final int UNAUTHORIZED_CODE = 40100;

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造会话归属服务。
     *
     * @param stringRedisTemplate 字符串 Redis 模板
     */
    public SessionOwnerService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 绑定会话归属。
     *
     * <p>key 不存在时以 SETNX 建档为当前用户;已存在且归属不是当前用户则抛 40300。</p>
     *
     * @param sessionId 会话 ID(即 conversationId)
     * @param userId    当前用户 ID
     */
    public void bindOwner(String sessionId, Long userId) {
        if (sessionId == null || sessionId.isBlank()) {
            // 无会话 ID(单轮场景)不做归属校验
            return;
        }
        if (userId == null) {
            throw new BizException(UNAUTHORIZED_CODE, "未登录或缺少身份凭证");
        }
        String key = KEY_PREFIX + sessionId;
        String uid = String.valueOf(userId);
        Boolean created = stringRedisTemplate.opsForValue().setIfAbsent(key, uid);
        if (Boolean.FALSE.equals(created)) {
            // key 已存在:比对归属,不一致则越权
            String owner = stringRedisTemplate.opsForValue().get(key);
            if (owner != null && !owner.equals(uid)) {
                throw new BizException(FORBIDDEN_CODE, "无权访问该会话");
            }
        }
    }
}

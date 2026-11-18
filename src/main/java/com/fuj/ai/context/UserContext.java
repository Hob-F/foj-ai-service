package com.fuj.ai.context;

/**
 * 当前请求用户 ID 的线程本地上下文。
 *
 * <p>由 {@code AuthInterceptor} 在请求进入时写入网关注入的可信头 {@code X-User-Id},
 * 供业务层(如会话归属校验)读取;请求结束时由拦截器 {@code afterCompletion} 清除,
 * 避免线程复用导致用户 ID 串号。</p>
 *
 * @author foj-ai-service
 */
public final class UserContext {

    /** 线程本地用户 ID。 */
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    /** 私有构造,禁止实例化。 */
    private UserContext() {
    }

    /**
     * 设置当前请求用户 ID。
     *
     * @param userId 用户 ID
     */
    public static void set(Long userId) {
        USER_ID.set(userId);
    }

    /**
     * 获取当前请求用户 ID。
     *
     * @return 用户 ID,未设置时为 null
     */
    public static Long get() {
        return USER_ID.get();
    }

    /**
     * 清除当前请求用户 ID。
     */
    public static void clear() {
        USER_ID.remove();
    }
}

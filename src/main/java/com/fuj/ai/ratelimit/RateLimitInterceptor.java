package com.fuj.ai.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 按 IP 限流拦截器。
 *
 * <p>请求进入控制器前做限流判定,超限直接返回 HTTP 429 与 JSON 提示,
 * 对同步与 SSE 请求同样生效,不让请求进入业务处理。</p>
 *
 * @author foj-ai-service
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    /** 超限时返回的固定 JSON 文案。 */
    private static final String RATE_LIMIT_BODY = """
            {"success":false,"content":"请求太频繁,请稍后再试","model":"none"}""";

    private final IpRateLimiter ipRateLimiter;

    /**
     * 构造限流拦截器。
     *
     * @param ipRateLimiter IP 令牌桶限流器
     */
    public RateLimitInterceptor(IpRateLimiter ipRateLimiter) {
        this.ipRateLimiter = ipRateLimiter;
    }

    /**
     * 请求进入前执行限流判定。
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true 放行;false 拦截并已写出 429 响应
     * @throws Exception 写出响应时的 IO 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 经网关转发后 remoteAddr 是网关地址,优先取 X-Forwarded-For 首段作为真实客户端 IP,无该头则回退 remoteAddr
        String ip = resolveClientIp(request);
        if (ipRateLimiter.tryAcquire(ip)) {
            return true;
        }
        // 超限:返回 429 + 固定 JSON,不让请求进入控制器(同步与 SSE 一视同仁)
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(RATE_LIMIT_BODY);
        return false;
    }

    /**
     * 解析真实客户端 IP。
     *
     * <p>请求经网关转发后 {@code getRemoteAddr()} 拿到的是网关地址,因此优先取
     * {@code X-Forwarded-For} 首段(最靠近客户端的地址);无该头或为空时回退
     * {@code getRemoteAddr()}。</p>
     *
     * @param request 请求
     * @return 真实客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            // 该头可能形如 "client, proxy1, proxy2",取首个元素
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

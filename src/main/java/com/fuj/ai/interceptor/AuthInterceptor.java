package com.fuj.ai.interceptor;

import com.fuj.ai.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 认证拦截器。
 *
 * <p>读取网关注入的可信头 {@code X-User-Id}:缺失/空白/非法则返回 401 并拦截;
 * 有效则写入 {@link UserContext} 供业务层使用。本服务只信任该头,不接触用户模型。</p>
 *
 * @author foj-ai-service
 */
public class AuthInterceptor implements HandlerInterceptor {

    /** 未登录时返回的固定 JSON 文案。 */
    private static final String UNAUTHORIZED_BODY = """
            {"code":40100,"message":"未登录或缺少身份凭证"}""";

    /**
     * 请求进入前读取并校验 {@code X-User-Id}。
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true 放行;false 已写出 401 并拦截
     * @throws IOException 写出响应时的 IO 异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String uid = request.getHeader("X-User-Id");
        if (uid == null || uid.trim().isEmpty()) {
            return reject(response);
        }
        Long userId;
        try {
            userId = Long.valueOf(uid.trim());
        } catch (NumberFormatException e) {
            // 网关注入的一定是数字;非数字视为非法身份,一律按未登录处理
            return reject(response);
        }
        UserContext.set(userId);
        return true;
    }

    /**
     * 请求结束后清除线程本地用户 ID,避免线程复用串号。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 写出 401 未登录响应并返回 false。
     *
     * @param response 响应
     * @return false,表示拦截
     * @throws IOException 写出响应时的 IO 异常
     */
    private boolean reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(UNAUTHORIZED_BODY);
        return false;
    }
}

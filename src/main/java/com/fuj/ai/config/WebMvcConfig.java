package com.fuj.ai.config;

import com.fuj.ai.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * <p>注册认证拦截器,对 AI 相关接口做登录态校验。</p>
 *
 * @author foj-ai-service
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册认证拦截器,覆盖现有 Controller 实际映射的 {@code /ai/**} 路径。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
            .addPathPatterns("/ai/**");
    }
}

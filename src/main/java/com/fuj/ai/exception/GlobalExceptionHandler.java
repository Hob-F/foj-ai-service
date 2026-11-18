package com.fuj.ai.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理器。
 *
 * <p>把业务异常统一转换为 {@code {code,message}} JSON;40300 映射为 HTTP 403,
 * 其余错误码映射为 HTTP 401。</p>
 *
 * @author foj-ai-service
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常。
     *
     * @param e 业务异常
     * @return 带 HTTP 状态码与 {@code {code,message}} 响应体
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Map<String, Object>> handleBizException(BizException e) {
        HttpStatus status = e.getCode() == 40300 ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", e.getCode());
        body.put("message", e.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}

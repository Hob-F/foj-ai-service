package com.fuj.ai.exception;

/**
 * 业务异常。
 *
 * <p>携带业务错误码,由 {@link GlobalExceptionHandler} 统一转换为
 * {@code {code,message}} JSON 与对应的 HTTP 状态码。</p>
 *
 * @author foj-ai-service
 */
public class BizException extends RuntimeException {

    /** 业务错误码。 */
    private final int code;

    /**
     * 构造业务异常。
     *
     * @param code    业务错误码
     * @param message 错误信息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    public int getCode() {
        return code;
    }
}

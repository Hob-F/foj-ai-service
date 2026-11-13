package com.fuj.ai.dto;

/**
 * AI 测试接口响应体。
 *
 * <p>对应 {@code POST /ai/test} 接口的返回值。无论调用成功还是失败都会返回本结构,
 * 保证 AI 链路异常时调用方仍能拿到降级结果而非收到异常。</p>
 *
 * @author foj-ai-service
 */
public class AiTestResponse {

    /** 是否调用成功。 */
    private boolean success;

    /** AI 回复内容;调用失败时为降级文案。 */
    private String content;

    /** 实际使用的模型名;调用失败时为 "none"。 */
    private String model;

    /** 调用总耗时(毫秒),用于观测超时与性能。 */
    private long costMs;

    /**
     * 获取是否成功。
     *
     * @return true 表示调用成功,false 表示已降级
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 设置是否成功。
     *
     * @param success 是否成功
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 获取回复内容。
     *
     * @return 回复内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置回复内容。
     *
     * @param content 回复内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取模型名。
     *
     * @return 模型名
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置模型名。
     *
     * @param model 模型名
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取耗时。
     *
     * @return 耗时(毫秒)
     */
    public long getCostMs() {
        return costMs;
    }

    /**
     * 设置耗时。
     *
     * @param costMs 耗时(毫秒)
     */
    public void setCostMs(long costMs) {
        this.costMs = costMs;
    }
}

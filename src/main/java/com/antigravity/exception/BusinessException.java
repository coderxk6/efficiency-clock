package com.antigravity.exception;

import com.antigravity.common.ResultCode;
import lombok.Getter;

/**
 * 业务逻辑异常
 * 当遇到不符合预期的业务流（如密码错误、修炼任务已被他人领走）时抛出。
 * GlobalExceptionHandler 会拦截此异常，并提取 code 和 message 返回给前端。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    /**
     * 基于预定义的异常枚举构造
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 基于预设计的枚举，但自定义提示信息
     */
    public BusinessException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
    }

    /**
     * 完全自定义的异常码和消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

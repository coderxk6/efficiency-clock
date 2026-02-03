package com.antigravity.common;

/**
 * API 统一响应状态码及消息枚举
 */
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 内部服务器错误 (如 NullPointer)
     */
    INTERNAL_SERVER_ERROR(500, "服务器开小差了，请稍候再试"),

    /**
     * 参数校验失败或客户端请求有误
     */
    BAD_REQUEST(400, "请求参数有误"),

    /**
     * 未授权或 Token 过期
     */
    UNAUTHORIZED(401, "登入状态已过期或未获取授权，请重新登入"),

    /**
     * 权限不足 (如游客操作高级功能)
     */
    FORBIDDEN(403, "您的等级或权限不足以执行此操作"),

    /**
     * 请求的数据不存在
     */
    NOT_FOUND(404, "请求的资源或数据不存在");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

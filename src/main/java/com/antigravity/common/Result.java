package com.antigravity.common;

import lombok.Data;

/**
 * 统一的 API 响应结果包装类
 *
 * @param <T> 响应的数据体类型
 */
@Data
public class Result<T> {

    /**
     * 响应状态码（遵循业务规范，不一定等同于 HTTP 状态码）
     */
    private Integer code;

    /**
     * 响应消息（通常是成功或错误的描述，用于前端展示）
     */
    private String message;

    /**
     * 响应主体数据
     */
    private T data;

    protected Result() {
    }

    protected Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构建成功响应（无返回值）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 构建成功响应（带返回值）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 构建失败响应（使用枚举默认信息）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 构建失败响应（自定义消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 判断当前响应是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}

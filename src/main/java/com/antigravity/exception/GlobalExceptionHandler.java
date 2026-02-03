package com.antigravity.exception;

import com.antigravity.common.Result;
import com.antigravity.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局统一异常处理控制器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 拦截业务类异常 (BusinessException)
     * 例如：手动抛出的 throw new BusinessException(400, "修炼任务不存在");
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常拦截: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 拦截 JSR-303 参数校验异常 (@Valid / @Validated)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessages = fieldErrors.stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        log.warn("参数校验不通过拦截: {}", errorMessages);
        return Result.error(ResultCode.BAD_REQUEST.getCode(), errorMessages);
    }

    /**
     * 拦截系统级别的其他未知异常 (Exception)
     * 防止底层的 SQL 或 NullPointer 直接暴露给前端
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统未知错误拦截: ", e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}

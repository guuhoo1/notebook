package com.notebook.config;

import cn.dev33.satoken.exception.NotLoginException;
import com.notebook.common.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理各类异常，返回规范的JSON响应
 *
 * @author notebook
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理Sa-Token未登录异常
     *
     * @param e 未登录异常
     * @return 错误响应
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录异常: {}", e.getMessage());
        String message = e.getMessage();
        if (message != null && message.contains("未登录")) {
            return R.fail(401, "请先登录");
        }
        return R.fail(401, "登录已过期，请重新登录");
    }

    /**
     * 处理参数校验异常（@Valid）
     *
     * @param e 参数校验异常
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return R.fail(400, message);
    }

    /**
     * 处理参数绑定异常
     *
     * @param e 绑定异常
     * @return 错误响应
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return R.fail(400, message);
    }

    /**
     * 处理非法参数异常
     *
     * @param e 非法参数异常
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return R.fail(400, e.getMessage());
    }

    /**
     * 处理运行时异常
     *
     * @param e 运行时异常
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public R<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常: ", e);
        return R.fail(500, "系统繁忙，请稍后重试");
    }

    /**
     * 处理所有未捕获的异常
     *
     * @param e 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return R.fail(500, "系统异常，请联系管理员");
    }
}

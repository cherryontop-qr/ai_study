// src/main/java/com/ai/study/config/GlobalExceptionHandler.java
package com.ai.study.config;

import com.ai.study.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientAbortException.class)
    @ResponseStatus(HttpStatus.OK) // 客户端断开连接，返回200避免日志错误
    public Result<?> handleClientAbortException(ClientAbortException e, HttpServletRequest request) {
        // 客户端主动断开连接（通常是超时或取消请求），不记录为错误
        log.debug("客户端断开连接: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error("请求超时，请稍后重试");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e) {
        log.error("服务器内部错误", e);
        return Result.error("服务器内部错误: " + e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        return Result.error("请求参数错误: " + e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }
}
package com.zzb.springbootinit.exception;

import com.zzb.springbootinit.common.BaseResponse;
import com.zzb.springbootinit.common.ErrorCode;
import com.zzb.springbootinit.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *  @RestControllerAdvice注解是 @ControllerAdvie和 @ResponseBody注解的组合，先捕获整个应用程序中抛出的异常，然后将异常处理方法的返回值将自动转换为HTTP响应的主体
 *  @ExceptionHandleri注解用于指定什么异常需要被捕获。
 * @author <a href="https://github.com/Oxidaner">Oxidaner</a>
 */
@RestControllerAdvice // 全局异常处理
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class) // 捕获业务异常
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class) // 捕获运行时异常
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}

package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.exception.PermissionDeniedException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex 业务异常
     * @return 错误结果
     */
    @ExceptionHandler(BaseException.class)
    public Result handleBaseException(BaseException ex){
        log.error("业务异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 处理SQL异常
     * @param ex 数据异常
     * @return 错误结果
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleSQLException(SQLIntegrityConstraintViolationException ex){
        log.error("SQL异常信息：{}", ex.getMessage());
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" ");
            String msg = split[2] + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }
        else {
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }
    }
    /**
     * 处理权限异常
     * @param ex 权限异常
     * @return 错误结果
     */
    @ExceptionHandler(PermissionDeniedException.class)
    public Result handlePermissionDeniedException(PermissionDeniedException ex){
        log.error("权限异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
}

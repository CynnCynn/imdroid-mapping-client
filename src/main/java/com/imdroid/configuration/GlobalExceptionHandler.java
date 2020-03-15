package com.imdroid.configuration;

import com.imdroid.pojo.bo.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Description:将所有Controller层以下抛出的异常用log记录下来
 * @Author: iceh
 * @Date: create in 2018-11-26 15:09
 * @Modified By:
 */
@Slf4j
@RestControllerAdvice
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public String businessExceptionHandler(BusinessException e) {
        log.error("业务异常", e);
        return e.getMessage();
    }

    @ExceptionHandler(RuntimeException.class)
    public String runtimeExceptionHandler(RuntimeException e) {
        log.error("运行时异常", e);
        return "运行错误";
    }

    @ExceptionHandler(Exception.class)
    public String otherExceptionHandler(RuntimeException e) {
        log.error("未发现异常:", e);
        return "未知错误";
    }

}

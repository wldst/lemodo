package com.wldst.ruder.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wldst.ruder.api.Result;

@ControllerAdvice
public class ControllerExceptionHandler {
    private Logger logger = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(value = ValidateException.class)
    @ResponseBody
    public Result<Object> validateExceptionHandler(ValidateException e) {
        logger.warn("ControllerExceptionHandler.validateExceptionHandler: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }
}


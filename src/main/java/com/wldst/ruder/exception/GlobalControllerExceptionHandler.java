package com.wldst.ruder.exception;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolationException;

/**
 *
 * @author wlst 定义全局异常处理 
 * @RestControllerAdvice 是@controlleradvice 与@ResponseBody 的组合注解
 */
@RestControllerAdvice
public class GlobalControllerExceptionHandler {
    final static Logger logger = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ExceptionHandler(value = { ConstraintViolationException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse constraintViolationException(ConstraintViolationException ex) {
	LoggerTool.error(logger,ex.getMessage(), ex);
	return new ApiErrorResponse(500, 5001, ex.getMessage());
    }

    @ExceptionHandler(value = { IllegalArgumentException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse IllegalArgumentException(IllegalArgumentException ex) {
	LoggerTool.error(logger,ex.getMessage(), ex);
	return new ApiErrorResponse(501, 5002, ex.getMessage());
    }

    @ExceptionHandler(value = { NoHandlerFoundException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse noHandlerFoundException(Exception ex) {
	LoggerTool.error(logger,ex.getMessage(), ex);
	return new ApiErrorResponse(404, 4041, ex.getMessage());
    }

    @ExceptionHandler(value = { Exception.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse unknownException(Exception ex) {
	ex.printStackTrace();
	LoggerTool.error(logger,ex.getMessage(), ex);
	return new ApiErrorResponse(500, 5002, ex.getMessage());
    }
}

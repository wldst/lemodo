package com.wldst.ruder.api;

import java.util.Map;

import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.WrappedResult;

/**
 * 处理结果
 * 
 * @author wldst
 *
 * @param <T>
 */
public class Result<T> {
    protected long code;
    protected Boolean status=false;
    protected String msg;
    private T data;
    private PageObject page; 
    protected Result() {
    }

    protected Result(long code, String message) {
	this.code = code;
	if (code == ResultCode.SUCCESS.getCode()) {
	    this.status = true;
	}
	this.msg = message;
    }

    protected Result(long code, String message, T data) {
	this.code = code;
	if (code == ResultCode.SUCCESS.getCode()) {
	    this.status = true;
	}
	
	this.msg = message;
	if(data!=null) {
	    this.data = data;
	}
    }
    
    protected Result(long code, String message, T data,PageObject page) {
	this.code = code;
	if (code == ResultCode.SUCCESS.getCode()) {
	    this.status = true;
	}
	
	this.msg = message;
	if(data!=null) {
	    this.data = data;
	}
	
	if(page!=null) {
	    this.page = page;
	}
    }
    
    public static Result<String> successMsg(String message) {
	return new Result<String>(ResultCode.SUCCESS.getCode(), message, null);
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> Result<T> success(T data) {
	return new Result<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    public static <T> Result<T> imageSucess(T data) {
	return new Result<T>(ResultCode.UPLOAD_SUCCESS.getCode(), ResultCode.UPLOAD_SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success() {
	return new Result<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功返回结果
     *
     * @param data    获取的数据
     * @param message 提示信息
     */
    public static <T> Result<T> success(T data, String message) {
	return new Result<T>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    public static <T> Result<T> wrapResult(T modelData, PageObject page, String  message) {
	return new Result<T>(ResultCode.SUCCESS.getCode(), message, modelData,page);
    }

    /**
     * 失败返回结果
     * 
     * @param errorCode 错误码
     */
    public static <T> Result<T> failed(IErrorCode errorCode) {
	return new Result<T>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败返回结果
     * 
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static <T> Result<T> failed(IErrorCode errorCode, String message) {
	return new Result<T>(errorCode.getCode(), message, null);
    }
    
    public static <T> Result<T> fail(String message) {
	return new Result<T>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果
     * 
     * @param message 提示信息
     */
    public static <T> Result<T> failed(String message) {
	return new Result<T>(ResultCode.FAILED.getCode(), message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> failed() {
	return failed(ResultCode.FAILED);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> Result<T> validateFailed() {
	return failed(ResultCode.VALIDATE_FAILED);
    }

    /**
     * 参数验证失败返回结果
     * 
     * @param message 提示信息
     */
    public static <T> Result<T> validateFailed(String message) {
	return new Result<T>(ResultCode.VALIDATE_FAILED.getCode(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> Result<T> unauthorized(T data) {
	return new Result<T>(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMessage(), data);
    }

    /**
     * 未授权返回结果
     */
    public static <T> Result<T> forbidden(T data) {
	return new Result<T>(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMessage(), data);
    }

    public String getMsg() {
	return msg;
    }

    public void setMsg(String message) {
	this.msg = message;
    }

    public T getData() {
	return data;
    }

    public void setData(T data) {
	this.data = data;
    }

    public Boolean getStatus() {
	return status;
    }

    public void setStatus(Boolean status) {
	this.status = status;
    }

    public long getCode() {
	return code;
    }

    public void setCode(long code) {
	this.code = code;
    }

    public PageObject getPage() {
        return page;
    }

    public void setPage(PageObject page) {
        this.page = page;
    }

}

package com.wldst.ruder.api;

/**
 * 返回码
 * @author wldst
 *
 */
public enum ResultCode implements IErrorCode {
    SUCCESS(200, "操作成功"),
    UPLOAD_SUCCESS(0, "上传成功"),
    FAILED(500, "操作失败"),
    VALIDATE_FAILED(404, "参数检验失败"),
    UNAUTHORIZED(401, "token失效"),
    FORBIDDEN(403, "权限不足");
    private long code;
    private String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

package com.wldst.ruder.module.workflow.exceptions;

public class BizException extends CrudBaseException {
	
	public BizException() {
	}

	public BizException(String message) {
		super(message);
	}

	public BizException(String message, Throwable newNested) {
		super(message, newNested);
	}

	public BizException(Throwable newNested) {
		super(newNested);
	}
}

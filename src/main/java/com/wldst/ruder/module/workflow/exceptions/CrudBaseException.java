package com.wldst.ruder.module.workflow.exceptions;

public class CrudBaseException extends RuntimeException {

	public CrudBaseException() {
	}

	public CrudBaseException(String message) {
		super(message);
	}

	public CrudBaseException(String message, Throwable newNested) {
		super(message, newNested);
	}

	public CrudBaseException(Throwable newNested) {
		super(newNested);
	}
}

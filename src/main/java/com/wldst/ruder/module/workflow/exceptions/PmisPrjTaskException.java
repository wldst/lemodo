package com.wldst.ruder.module.workflow.exceptions;

public class PmisPrjTaskException extends CrudBaseException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8916548032465601673L;

	public PmisPrjTaskException() {
	}

	public PmisPrjTaskException(String message) {
		super(message);
	}

	public PmisPrjTaskException(String message, Throwable newNested) {
		super(message, newNested);
	}

	public PmisPrjTaskException(Throwable newNested) {
		super(newNested);
	}
}

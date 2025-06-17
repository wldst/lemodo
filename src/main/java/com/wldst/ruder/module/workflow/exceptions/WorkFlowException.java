package com.wldst.ruder.module.workflow.exceptions;

public class WorkFlowException extends CrudBaseException {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;

	public WorkFlowException() {
	}

	public WorkFlowException(String message) {
		super(message);
	}

	public WorkFlowException(String message, Throwable newNested) {
		super(message, newNested);
	}

	public WorkFlowException(Throwable newNested) {
		super(newNested);
	}
}

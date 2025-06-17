package com.wldst.ruder.module.workflow.beans;

/**
 * 用于流程履历信息的展示
 * 
 * @author aaron
 *
 */
public class WfHistoryView extends History {
    // 任务类型
    private int taskType = 0;

    // 任务状态
    private int taskStatus = 0;

    public int getTaskType() {
	return taskType;
    }

    public void setTaskType(int taskType) {
	this.taskType = taskType;
    }

    public int getTaskStatus() {
	return taskStatus;
    }

    public void setTaskStatus(int taskStatus) {
	this.taskStatus = taskStatus;
    }
}

package com.wldst.ruder.module.workflow.beans;

import java.util.Map;

import com.wldst.ruder.util.MapTool;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程运行数据封装<br>
 * 流程推动的所有方法中均通过该对象实例进行流程运行数据的传递
 * 
 */
public class WfPc {
    // 当前任务实例ID
    public final static String currentTaskID="currentTaskID";

    // 对应的流程实例ID
    public final static String workflowID="workflowID";

    // 需要执行的决策KEY值
    public final static String decisionKey="decisionKey";

    // 需要执行的决策名
    public final static String decisionName="decisionName";

    // 下一个任务ID(主要是对应打回决策动作)
    public final static String nextTaskID="nextTaskID";

    // 下一个任务的执行人ID数组
    public final static String nextTaskExecutorIDs="nextTaskExecutorIDs";

    // 当前执行人ID
    public final static String currentExecutorID="currentExecutorID";

    // 当前执行人姓名
    public final static String currentExecutorName="currentExecutorName";

    // 任务到达当前执行人的时间
    public final static String currTaskComeDatetime="currTaskComeDatetime";

    // 任务执行批复信息
    public final static String taskExecuteConfirm="taskExecuteConfirm";

    /**
     * 得到对应的流程实例ID
     * 
     * @return MapTool.longValue(data,对应的流程实例ID.
     */
    public static long getWorkflowID(Map<String, Object> data) {
	return MapTool.longValue(data,workflowID);
    }

    /**
     * 设置对应的流程实例ID
     * 
     * @param workflowID 对应的流程实例ID.
     */
    public static void setWorkflowID(long workflowID, Map<String, Object> data) {
	data.put("workflowID",workflowID);
    }

    /**
     * 得到需要执行的决策KEY值
     * 
     * @return MapTool.longValue(data,需要执行的决策KEY值.
     */
    public static String getDecisionKey(Map<String, Object> data) {
	return MapTool.string(data,decisionKey);
    }

    /**
     * 设置需要执行的决策KEY值
     * 
     * @param decisionKey 需要执行的决策KEY值.
     */
    public static void setDecisionKey(String decisionKey, Map<String, Object> data) {
	data.put("decisionKey",decisionKey);
    }

    /**
     * 得到需要执行的决策名
     * 
     * @return MapTool.longValue(data,需要执行的决策名.
     */
    public static String getDecisionName(Map<String, Object> data) {
	return MapTool.string(data,decisionName);
    }

    /**
     * 设置需要执行的决策名
     * 
     * @param decisionName 需要执行的决策名.
     */
    public static void setDecisionName(String decisionName, Map<String, Object> data) {
	data.put("decisionName",decisionName);
    }

    /**
     * 得到下一个任务ID(主要是对应打回决策动作)
     * 
     * @return MapTool.longValue(data,下一个任务ID(主要是对应打回决策动作).
     */
    public static long getNextTaskID(Map<String, Object> data) {
	return MapTool.longValue(data,nextTaskID);
    }

    /**
     * 设置下一个任务ID(主要是对应打回决策动作)
     * 
     * @param nextTaskID 下一个任务ID(主要是对应打回决策动作).
     */
    public static void setNextTaskID(long nextTaskID, Map<String, Object> data) {
	data.put("nextTaskID",nextTaskID);
    }

    /**
     * 得到下一个任务的执行人ID数组
     * 
     * @return MapTool.longValue(data,下一个任务的执行人ID数组.
     */
    public static long[] getNextTaskExecutorIDs(Map<String, Object> data) {
	return MapTool.splitLong(data,nextTaskExecutorIDs);
    }

    /**
     * 设置下一个任务的执行人ID数组
     * 
     * @param nextTaskExecutorIDs 下一个任务的执行人ID数组.
     */
    public static void setNextTaskExecutorIDs(long[] nextTaskExecutorIDs, Map<String, Object> data) {
	data.put("nextTaskExecutorIDs",nextTaskExecutorIDs);
    }

    /**
     * 得到当前执行人ID
     * 
     * @return MapTool.longValue(data,当前执行人ID.
     */
    public static long getCurrentExecutorID(Map<String, Object> data) {
	return MapTool.longValue(data,currentExecutorID);
    }

    /**
     * 设置当前执行人ID
     * 
     * @param currentExecutorID 当前执行人ID.
     */
    public static void setCurrentExecutorID(long currentExecutorID, Map<String, Object> data) {
	data.put("currentExecutorID",currentExecutorID);
    }

    /**
     * 得到任务执行批复信息
     * 
     * @return MapTool.longValue(data,任务执行批复信息.
     */
    public static String getTaskExecuteConfirm(Map<String, Object> data) {
	return MapTool.string(data,taskExecuteConfirm);
    }

    /**
     * 设置任务执行批复信息
     * 
     * @param taskExecuteConfirm 任务执行批复信息.
     */
    public static void setTaskExecuteConfirm(String taskExecuteConfirm, Map<String, Object> data) {
	data.put("taskExecuteConfirm",taskExecuteConfirm);
    }

    /**
     * 得到当前执行人姓名
     * 
     * @return MapTool.longValue(data,当前执行人姓名.
     */
    public static String getCurrentExecutorName(Map<String, Object> data) {
	return MapTool.string(data,currentExecutorName);
    }

    /**
     * 设置当前执行人姓名
     * 
     * @param currentExecutorName 当前执行人姓名.
     */
    public static void setCurrentExecutorName(String currentExecutorName, Map<String, Object> data) {
	data.put("currentExecutorName",currentExecutorName);
    }

    /**
     * 得到当前任务实例ID
     * 
     * @return MapTool.longValue(data,当前任务实例ID.
     */
    public static long getCurrentTaskID(Map<String, Object> data) {
	return MapTool.longValue(data,currentTaskID);
    }

    /**
     * 设置当前任务实例ID
     * 
     * @param currentTaskID 当前任务实例ID.
     */
    public static void setCurrentTaskID(long currentTaskID, Map<String, Object> data) {
	data.put("currentTaskID",currentTaskID);
    }

    public static long getCurrTaskComeDatetime(Map<String, Object> data) {
	return MapTool.longValue(data,currTaskComeDatetime);
    }

    public static void setCurrTaskComeDatetime(long currTaskComeDatetime, Map<String, Object> data) {
	data.put("currTaskComeDatetime",currTaskComeDatetime);
    }

}

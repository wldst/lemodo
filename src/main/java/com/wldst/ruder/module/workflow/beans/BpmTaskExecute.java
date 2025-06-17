package com.wldst.ruder.module.workflow.beans;

import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.util.MapTool;

/**
 * 流程任务执行人信息
 * 
 * 
 */
public class BpmTaskExecute extends BpmDo{
	
	// 执行人对应任务ID
	public static String wfTaskID="taskId";

	// 对应流程实例ID
	public static String wfInstanceID="instanceID";

	// 对应执行人ID
	public static String executorID="executorID";

	// 执行人执行状态
	public static String executorStatus="executorStatus";

	// 执行人所执行动作
	public static String executorDecision="executorDecision";

	// 任务到达执行人的时间
	 

	/**
	 * 设置任务执行主键ID
	 * 
	 * @param id
	 *            任务执行主键ID.
	 */
	public static void setID(long id,Map<String, Object> data) {
		data.put(ID,id);
	}

	/**
	 * 得到执行人对应任务ID
	 * 
	 * @return 执行人对应任务ID.
	 */
	public static long getTaskID(Map<String, Object> data) {
		return MapTool.longValue(data, wfTaskID);
	}

	/**
	 * 设置执行人对应任务ID
	 * 
	 * @param wfTaskId
	 *            执行人对应任务ID.
	 */
	public static void setTaskID(long wfTaskId,Map<String, Object> data) {
		data.put(wfTaskID,wfTaskId);
	}

	/**
	 * 得到对应流程实例ID
	 * 
	 * @return 对应流程实例ID.
	 */
	public static long getInstanceID(Map<String, Object> data) {
		return MapTool.longValue(data, wfInstanceID);
	}

	/**
	 * 设置对应流程实例ID
	 * 
	 * @param wfInstanceId
	 *            对应流程实例ID.
	 */
	public static void setInstanceID(long wfInstanceId,Map<String, Object> data) {
		data.put(wfInstanceID,wfInstanceId);
	}

	/**
	 * 得到执行人执行状态
	 * 
	 * @return 执行人执行状态.
	 */
	public static int getExecutorStatus(Map<String, Object> data) {
		return MapTool.integer(data, executorStatus);
	}

	/**
	 * 设置执行人执行状态
	 * 
	 * @param status
	 *            执行人执行状态.
	 */
	public static void setExecutorStatus(int status,Map<String, Object> data) {
		data.put(executorStatus,status);
	}

	/**
	 * 得到执行人所执行动作
	 * 
	 * @return 执行人所执行动作.
	 */
	public static String getExecutorDecision(Map<String, Object> data) {
		return MapTool.string(data, executorDecision);
	}

	/**
	 * 设置执行人所执行动作
	 * 
	 * @param executorDecision
	 *            执行人所执行动作.
	 */
	public static void setExecutorDecision(String executorDecision,Map<String, Object> data) {
		data.put(executorDecision,executorDecision);
	}

	/**
	 * 得到对应执行人ID
	 * 
	 * @return 对应执行人ID.
	 */
	public static long getExecutorID(Map<String, Object> data) {
		return MapTool.longValue(data, executorID);
	}

	/**
	 * 设置对应执行人ID
	 * 
	 * @param executorId
	 *            对应执行人ID.
	 */
	public static void setExecutorID(long executorId,Map<String, Object> data) {
		data.put(executorID,executorId);
	}

	/**
	 * 获取任务到达执行人的时间
	 * @return
	 */
	public static long getTaskComeDatetime(Map<String, Object> data) {
		return MapTool.longValue(data, TASK_COME_TIME);
	}

	/**
	 * 设置任务到达执行人的时间
	 * @return
	 */
	public static void setTaskComeDatetime(long taskDatetime,Map<String, Object> data) {
		data.put(TASK_COME_TIME,taskDatetime);
	}

}

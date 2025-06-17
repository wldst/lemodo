package com.wldst.ruder.module.workflow.beans;

import java.util.Map;

import com.wldst.ruder.util.MapTool;

/**
 * 流程之普通任务扩展属性信息
 * 
* @author wldst
 */
public class TaskExtendInfo
{
   // 主键
   public static String ID="id";

   // 所属任务ID
   public static String wfTaskID="wfTaskId";

   // 所属流程实例ID
   public static String wfInstanceID="wfInstanceId";

   // 任务内部ID
   public static String innerTaskID="innerTaskId";

   // 任务执行人条件
   public static String executorCondition="executorCondition";

   /**
    * 得到主键
    * 
    * @return 主键.
    */
   public static long getID(Map<String, Object> data) {
	return MapTool.longValue(data,ID);
   }

   /**
    * 设置主键
    * 
    * @param id 主键.
    */
   public static void setID(long id, Map<String, Object> data) {
	data.put(ID,id);
   }

   /**
    * 得到所属任务ID
    * 
    * @return 所属任务ID.
    */
   public static long getWfTaskID(Map<String, Object> data) {
	return MapTool.longValue(data,wfTaskID);
   }

   /**
    * 设置所属任务ID
    * 
    * @param wfTaskID 所属任务ID.
    */
   public static void setWfTaskID(long wfTaskId, Map<String, Object> data) {
	data.put(wfTaskID,wfTaskId);
   }

   /**
    * 得到所属流程实例ID
    * 
    * @return 所属流程实例ID.
    */
   public static long getWfInstanceID(Map<String, Object> data) {
	return MapTool.longValue(data,wfInstanceID);
   }

   /**
    * 设置所属流程实例ID
    * 
    * @param wfInstanceID 所属流程实例ID.
    */
   public static void setWfInstanceID(long wfInstanceId, Map<String, Object> data) {
	data.put(wfInstanceID,wfInstanceId);
   }

   /**
    * 得到任务内部ID
    * 
    * @return 任务内部ID.
    */
   public static String getInnerTaskID(Map<String, Object> data) {
	return MapTool.string(data,innerTaskID);
   }

   /**
    * 设置任务内部ID
    * 
    * @param innerTaskID 任务内部ID.
    */
   public static void setInnerTaskID(String innerTaskId, Map<String, Object> data) {
	data.put(innerTaskID,innerTaskId);
   }

   /**
    * 得到任务执行人条件
    * 
    * @return 任务执行人条件.
    */
   public static String getExecutorCondition(Map<String, Object> data) {
	return MapTool.string(data,executorCondition);
   }

   /**
    * 设置任务执行人条件
    * 
    * @param executorCondition 任务执行人条件.
    */
   public static void setExecutorCondition(String executorCondi, Map<String, Object> data) {
	data.put(executorCondition,executorCondi);
   }
}

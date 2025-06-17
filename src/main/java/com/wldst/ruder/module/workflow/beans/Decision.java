package com.wldst.ruder.module.workflow.beans;

import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.util.MapTool;

/**
 * 流程实例之决策信息
 */
public class Decision extends BpmDo {
    // 主键
    // 对应任务ID
    public static String wfTaskID="taskID";

    // 对应流程实例ID
    public static String wfInstanceID="instanceID";

    // 决策名
    public static String decisionName="name";

    // 决策显示名
    public static String decisionViewName="viewName";

    // 决策描述
    public static String decisionDescript="descript";

    // 排序号
    public static String orderNO="orderNO";

    // 执行方式
    public static String executeType="executeType";

    /**
     * 设置主键
     * 
     * @param id 主键.
     */
     public static void setID(long id, Map<String, Object> data) {
	data.put(ID,id);
    }

    /**
     * 得到对应任务ID
     * 
     * @return 对应任务ID.
     */
     public static long getTaskID(Map<String, Object> data) {
	return MapTool.longValue(data, wfTaskID);
    }

    /**
     * 设置对应任务ID
     * 
     * @param wfTaskID 对应任务ID.
     */
     public static void setTaskID(long wfTaskId, Map<String, Object> data) {
	data.put(wfTaskID,wfTaskId);
    }

    /**
     * 得到对应流程实例ID
     * 
     * @return MapTool.longValue(data, 对应流程实例ID.
     */
     public static long getInstanceID(Map<String, Object> data) {
	return MapTool.longValue(data, wfInstanceID);
    }

    /**
     * 设置对应流程实例ID
     * 
     * @param wfInstanceID 对应流程实例ID.
     */
     public static void setInstanceID(long wfInstanceId, Map<String, Object> data) {
	data.put(wfInstanceID,wfInstanceId);
    }

    /**
     * 得到决策名
     * 
     * @return  决策名.
     */
    public static String getName(Map<String, Object> data) {
	return MapTool.string(data, decisionName);
    }

    /**
     * 设置决策名
     * 
     * @param decisionName 决策名.
     */
     public static void setName(String decName, Map<String, Object> data) {
	data.put(decisionName,decName);
    }

    /**
     * 得到决策显示名
     * 
     * @return MapTool.longValue(data, 决策显示名.
     */
     public static String getViewName(Map<String, Object> data) {
	return MapTool.string(data, decisionViewName);
    }

    /**
     * 设置决策显示名
     * 
     * @param decisionViewName 决策显示名.
     */
     public static void setViewName(String decisionVName, Map<String, Object> data) {
	data.put(decisionViewName,decisionVName);
    }

    /**
     * 得到决策描述
     * 
     * @return MapTool.longValue(data, 决策描述.
     */
     public static String getDescript(Map<String, Object> data) {
	return MapTool.string(data, decisionDescript);
    }

    /**
     * 设置决策描述
     * 
     * @param decisionDescript 决策描述.
     */
     public static void setDescript(String decisionDesc, Map<String, Object> data) {
	data.put(decisionDescript,decisionDesc);
    }

    /**
     * 得到排序号
     * 
     * @return 排序号.
     */
     public static long getOrderNO(Map<String, Object> data) {
	return MapTool.longValue(data, orderNO);
    }

    /**
     * 设置排序号
     * 
     * @param orderNO 排序号.
     */
     public static void setOrderNO(long orderNo, Map<String, Object> data) {
	data.put(orderNO,orderNo);
    }

    /**
     * 得到执行方式
     * 
     * @return 执行方式.
     */
     public static int getExecuteType(Map<String, Object> data) {
	return MapTool.integer(data, executeType);
    }

    /**
     * 设置执行方式
     *
     * @param executeType 执行方式.
     */
     public static void setExecuteType(int exeType, Map<String, Object> data) {
	data.put(executeType,exeType);
    }
}

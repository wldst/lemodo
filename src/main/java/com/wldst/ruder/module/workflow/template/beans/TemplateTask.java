package com.wldst.ruder.module.workflow.template.beans;

import java.util.Map;

import com.wldst.ruder.util.MapTool;

/**
 * 抽象流程模板任务信息
 * 
* @author wldst
 */
public class TemplateTask
{
   // 流程任务名称
    public static String wfTaskName="wfTaskName";

   // 流程任务内部ID
    public static String wfTaskInnerID="wfTaskInnerID";

   // 流程任务类型
    public static String wfTaskType="wfTaskType";

   // 任务节点位置大小信息
    public static String  shape="shape";

    /**
     * 得到流程任务名称
     * 
     * @return 流程任务名称.
     */
    public static String getWfTaskName(Map<String, Object> data)
    {
	return MapTool.string(data, wfTaskName);
    }

    /**
     * 设置流程任务名称
     * 
     * @param wfTaskName 流程任务名称.
     */
    public static void setWfTaskName(String taskName,Map<String, Object> data)
    {
	data.put(wfTaskName,taskName);
    }

    /**
     * 得到流程任务内部ID
     * 
     * @return 流程任务内部ID.
     */
    public static String getWfTaskInnerID(Map<String, Object> data)
    {
	return MapTool.string(data, wfTaskInnerID);
    }

    /**
     * 设置流程任务内部ID
     * 
     * @param wfTaskInnerID 流程任务内部ID.
     */
    public static void setWfTaskInnerID(String wfTaskInnerId,Map<String, Object> data)
    {
	data.put(wfTaskInnerID,wfTaskInnerId);
    }

    /**
     * 得到流程任务类型
     * 
     * @return 流程任务类型.
     */
    public static int getWfTaskType(Map<String, Object> data)
    {
	return MapTool.integer(data, wfTaskType);
    }

    /**
     * 设置流程任务类型
     * 
     * @param wfTaskType 流程任务类型.
     */
    public static void setWfTaskType(int taskType,Map<String, Object> data)
    {
       data.put(wfTaskType,taskType);
    }

    /**
     * 得到任务节点位置大小信息
     * 
     * @return 任务节点位置大小信息.
     */
    public static Map<String, Object> getShape(Map<String, Object> data)
    {
	return MapTool.mapObject(data,shape);
    }

    /**
     * 设置任务节点位置大小信息
     * 
     * @param shape 任务节点位置大小信息.
     */
    public static void setShape(Map<String, Object> shapeM,Map<String, Object> data)
    {
	data.put(shape,shapeM);
    }
}

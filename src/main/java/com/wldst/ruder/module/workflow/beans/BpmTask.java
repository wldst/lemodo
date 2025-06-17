package com.wldst.ruder.module.workflow.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;

/**
 * 流程实例对象之抽象任务信息
 *
 * @author wldst
 */
public class BpmTask extends BpmDo {
    // 流程任务实例ID
    // 流程任务对应流程模板内部ID
    public static String innerTaskID = "innerTaskID";

    // 流程任务类型
    public static String taskType = "taskType";

    // 流程实例ID
    public static String wfInstanceID = "instanceID";

    // 流程任务描述
    public static String taskDescript = "descript";

    // 流程任务名称
    public static String taskName = "name";

    // 任务状态
    public static String taskStatus = "taskStatus";

    // 配对任务ID
    public static String pairTaskInnerID = "pairTaskInnerID";

    // 前续任务关系列表
    public static String preRelationTaskList = "preRelationTaskList";

    // 后续任务关系列表
    public static String nextRelationTaskList = "nextRelationTaskList";

    /**
     * 得到流程任务实例ID
     *
     * @return MapTool.longValue(data, 流程任务实例ID.
     */
    public static long getID(Map<String, Object> data) {
        return id(data);
    }

    /**
     * 设置流程任务实例ID
     *
     * @param id 流程任务实例ID.
     */
    public static void setID(long id, Map<String, Object> data) {
        data.put(ID, id);
    }

    /**
     * 得到流程任务对应流程模板内部ID
     *
     * @return MapTool.longValue(data, 流程任务对应流程模板内部ID.
     */
    public static String getInnerTaskID(Map<String, Object> data) {
        return MapTool.string(data, innerTaskID);
    }

    /**
     * 设置流程任务对应流程模板内部ID
     *
     * @param innerTaskID 流程任务对应流程模板内部ID.
     */
    public static void setInnerTaskID(String innerTaskID, Map<String, Object> data) {
        data.put("innerTaskID", innerTaskID);
    }

    /**
     * 得到流程任务类型
     *
     * @return MapTool.longValue(data, 流程任务类型.
     */
    public static int getTaskType(Map<String, Object> data) {
        return MapTool.integer(data, taskType);
    }

    /**
     * 设置流程任务类型
     *
     * @param taskType 流程任务类型.
     */
    public static void setTaskType(int taskType, Map<String, Object> data) {
        data.put("taskType", taskType);
    }

    /**
     * 得到流程实例ID
     *
     * @return MapTool.longValue(data, 流程实例ID.
     */
    public static long getInstanceID(Map<String, Object> data) {
        return MapTool.longValue(data, wfInstanceID);
    }

    /**
     * 设置流程实例ID
     *
     * @param wfInstanceID 流程实例ID.
     */
    public static void setInstanceID(long wfInstanceID, Map<String, Object> data) {
        data.put("wfInstanceID", wfInstanceID);
    }

    /**
     * 得到流程任务描述
     *
     * @return MapTool.longValue(data, 流程任务描述.
     */
    public static String getDescript(Map<String, Object> data) {
        return MapTool.string(data, taskDescript);
    }

    /**
     * 设置流程任务描述
     *
     * @param taskDescript 流程任务描述.
     */
    public static void setDescript(String taskDescript, Map<String, Object> data) {
        data.put("descript", taskDescript);
    }

    /**
     * 得到流程任务名称
     *
     * @return MapTool.longValue(data, 流程任务名称.
     */
    public static String getName(Map<String, Object> data) {
        return MapTool.string(data, taskName);
    }

    /**
     * 设置流程任务名称
     *
     * @param taskName 流程任务名称.
     */
    public static void setName(String taskName, Map<String, Object> data) {
        data.put("name", taskName);
    }

    /**
     * 得到任务状态
     *
     * @return MapTool.longValue(data, 任务状态.
     */
    public static int getTaskStatus(Map<String, Object> data) {
        return MapTool.integer(data, taskStatus);
    }

    /**
     * 设置任务状态
     *
     * @param taskStatus 任务状态.
     */
    public static void setTaskStatus(int taskStatus, Map<String, Object> data) {
        data.put("taskStatus", taskStatus);
    }



}

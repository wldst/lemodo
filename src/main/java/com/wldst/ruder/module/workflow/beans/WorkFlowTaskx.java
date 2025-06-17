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
public class WorkFlowTaskx extends BpmDo {
    // // 流程任务实例ID
    // private long ID;
    //
    // // 流程任务对应流程模板内部ID
    // private String innerTaskID;
    //
    // // 流程任务类型
    // private int taskType;
    //
    // // 流程实例ID
    // private long wfInstanceID;
    //
    // // 流程任务描述
    // private String taskDescript;
    //
    // // 流程任务名称
    // private String taskName;
    //
    // // 任务状态
    // private int taskStatus;
    //
    // // 配对任务ID
    // private String pairTaskInnerID;
    //
    // // 前续任务关系列表
    // private List<Map<String,Object>> preRelationTaskList;
    //
    // // 后续任务关系列表
    // private List<Map<String,Object>> nextRelationTaskList;

    /**
     * 获取前续任务关系为真的关系对象
     * 
     * @return 关系对象
     */
    public Map<String, Object> getRealPreRelation(Map<String, Object> data) {
	Map<String, Object> retRelation = null;
	List<Map<String, Object>> preTasks = preRelationTaskList(data);
	if (preTasks != null && preTasks.size() > 0) {
	    int listSize = preTasks.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempRelation = preTasks.get(i);
		if (MapTool.integer(tempRelation, "realFlowRoadFlag") == WFEConstants.DB_BOOLEAN_TRUE) {
		    retRelation = tempRelation;
		    break;
		}
	    }
	}
	return retRelation;
    }

    /**
     * 获取后续任务关系为真的关系对象
     * 
     * @return 关系对象
     */
    public Map<String, Object> getRealNextRelation(Map<String, Object> data) {
	Map<String, Object> retRelation = null;
	List<Map<String, Object>> nexts = nextRelationTaskList(data);
	if (nexts != null && nexts.size() > 0) {
	    int listSize = nexts.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempRelation = nexts.get(i);
		if (MapTool.integer(tempRelation, "realFlowRoadFlag") == WFEConstants.DB_BOOLEAN_TRUE) {
		    retRelation = tempRelation;
		    break;
		}
	    }
	}
	return retRelation;
    }

    /**
     * 获取任务关系信息列表
     * 
     * @return 任务关系信息列表
     */
    public List<Map<String, Object>> getRelationTaskList(Map<String, Object> data) {
	List<Map<String, Object>> retList = null;
	List<Map<String, Object>> preRelationTaskList = preRelationTaskList(data);
	if (preRelationTaskList != null && preRelationTaskList.size() > 0) {
	    int listSize = preRelationTaskList.size();
	    for (int i = 0; i < listSize; i++) {
		if (retList == null) {
		    retList = new ArrayList<>();
		}
		retList.add(preRelationTaskList.get(i));
	    }
	}
	List<Map<String, Object>> nextRelationTaskList = nextRelationTaskList(data);
	if (nextRelationTaskList != null && nextRelationTaskList.size() > 0) {
	    int listSize = nextRelationTaskList.size();
	    for (int i = 0; i < listSize; i++) {
		if (retList == null) {
		    retList = new ArrayList<>();
		}
		retList.add(nextRelationTaskList.get(i));
	    }
	}
	return retList;
    }

    /**
     * 添加任务关联关系
     * 
     * @param taskRelation 关联关系
     * @param relationType 关联类型
     */
    public static void addRelationTask(Map<String, Object> data, Map<String, Object> taskRelation, int relationType) {
	if (relationType == WFEConstants.TASKRELATION_NEXT) {
	    List<Map<String, Object>> nexts = nextRelationTaskList(data);
	    if (nexts == null) {
		nexts = new ArrayList<>();
	    }
	    nexts.add(taskRelation);
	} else if (relationType == WFEConstants.TASKRELATION_PRE) {
	    List<Map<String, Object>> preRelTasks = preRelationTaskList(data);
	    if (preRelTasks == null) {
		preRelTasks = new ArrayList<>();
	    }
	    preRelTasks.add(taskRelation);
	}
    }

    public static List<Map<String, Object>> preRelationTaskList(Map<String, Object> data) {
	return MapTool.listMapObject(data, "preRelationTaskList");
    }

    public static List<Map<String, Object>> nextRelationTaskList(Map<String, Object> data) {
	return MapTool.listMapObject(data, "nextRelationTaskList");
    }

}

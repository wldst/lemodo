package com.wldst.ruder.module.workflow.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;

/**
 * 流程实例之普通任务信息
 * 
 * 
 */
public class SimpleTask extends BpmTask {
    // 流程任务之属性信息
    public static String taskProperty = "taskProperty";

    // 流程普通任务之扩展信息
    public static String taskExtendsInfoList = "taskExtendsInfoList";

    // 流程任务之执行人列表
    public static String taskExecutorList = "taskExecutorList";

    // 流程任务之决策信息列表
    public static String taskDecisionList = "taskDecisionList";

    /**
     * 根据决策key值得到决策对象
     * 
     * @param decisionKey 决策key值
     * @return 决策对象
     */
    public static Map<String, Object> getWfTaskDecision(String decisionKey, Map<String, Object> data) {
	Map<String, Object> retObj = null;
	List<Map<String, Object>> taskDecisions = getTaskDecisionList(data);
	if (taskDecisions != null) {
	    for (Map<String, Object> tempObj : taskDecisions) {
		if (MapTool.name(tempObj).equals(decisionKey)) {
		    retObj = tempObj;
		    break;
		}
	    }
	}
	return retObj;
    }

    /**
     * 得到流程任务执行人列表
     * 
     * @return 任务执行人列表
     */
    public static List<Map<String, Object>> getTaskExecutorList(Map<String, Object> data) {
	
	return MapTool.listMapObject(data, taskExecutorList);
    }

    /**
     * 判断指定用户是否在任务中存在,并根据执行状态判断
     * 
     * @param userID        指定用户ID
     * @param executeStatus 执行状态
     * @return 是否存在用户
     */
    public static Map<String, Object> existExecuteUser(long userID, int executeStatus, Map<String, Object> data) {
	Map<String, Object> retTaskExecutor = null;
	
	List<Map<String, Object>> executeUsers = MapTool.listMapObject(data,"nodeUserList");
	if(executeUsers!=null&&!executeUsers.isEmpty()) {
		    for (Map<String, Object> executor: executeUsers) {
			if (MapTool.longValue(executor, "userId") == userID) {
			    retTaskExecutor = executor;
			    break;
			}
		    }
	}else {
	    List<Map<String, Object>> tempExecutorList = getTaskExecutorList(data);

		if (tempExecutorList != null && tempExecutorList.size() > 0) {
		    int listSize = tempExecutorList.size();
		    for (int i = 0; i < listSize; i++) {
			Map<String, Object> executor = tempExecutorList.get(i);
			if (MapTool.longValue(executor, "executorID") == userID
				&& MapTool.longValue(executor, "executorStatus") == executeStatus) {
			    retTaskExecutor = executor;
			    break;
			}
		    }
		} 
	}
	
	return retTaskExecutor;
    }

    /**
     * 获取普通任务之扩展信息列表
     * 
     * @return 普通任务扩展信息列表
     */
    public static List<Map<String, Object>> getTaskExtendsInfoList(Map<String, Object> data) {
	return MapTool.listMapObject(data, taskExtendsInfoList);
    }

    /**
     * 得到任务之决策信息列表
     * 
     * @return 任务之决策信息列表
     */
    public static List<Map<String, Object>> getTaskDecisionList(Map<String, Object> data) {
	return MapTool.listMapObject(data, taskDecisionList);
    }

    /**
     * 过滤任务决策中指定的决策信息
     */
    public static List<Map<String, Object>> filterTaskDecision(String decisionNames, Map<String, Object> data) {
	List<Map<String, Object>> reList = null;
	List<Map<String, Object>> taskDecisions = getTaskDecisionList(data);
	if (null != taskDecisions && null != decisionNames) {
	    reList = new ArrayList<>();
	    int size = taskDecisions.size();
	    String[] names = decisionNames.split("|");
	    for (int i = 0; i < size; i++) {
		for (int k = 0; k < names.length; k++) {
		    Map<String, Object> tmp = taskDecisions.get(i);
		    if (!decisionName(tmp).equalsIgnoreCase(names[k])) {
			reList.add(tmp);
		    }
		}
	    }
	}
	return reList;
    }

    /**
     * 添加任务扩展信息到任务实例中
     * 
     * @param extendsInfo 任务扩展信息
     * @throws Exception
     */
    public static void addTaskExtendsInfo(Map<String, Object> extendsInfo, Map<String, Object> data)
	    throws CrudBaseException {
	if (extendsInfo == null) {
	    throw new CrudBaseException("需要添加的任务扩展信息为空,不能进行添加");
	}
	List<Map<String, Object>> taskExtends = getTaskExtendsInfoList(data);
	if (taskExtends == null) {
	    taskExtends = new ArrayList<>();
	}
	taskExtends.add(extendsInfo);
	setTaskExtendsInfoList(data, taskExtends);
    }

    public static void setTaskExtendsInfoList(Map<String, Object> data, List<Map<String, Object>> taskExtends) {
	data.put(taskExtendsInfoList, taskExtends);
    }

    /**
     * 添加任务决策信息到任务实例中
     * 
     * @param taskDecision 任务决策信息
     * @throws CrudBaseException
     */
    public static void addTaskDecision(Map<String, Object> taskDecision, Map<String, Object> data)
	    throws CrudBaseException {
	if (taskDecision == null) {
	    throw new CrudBaseException("需要添加的任务决策信息为空,不能进行添加到任务实例中");
	}
	List<Map<String, Object>> taskDecisionList2 = getTaskDecisionList(data);
	if (taskDecisionList2 == null) {
	    taskDecisionList2 = new ArrayList<>();
	}
	taskDecisionList2.add(taskDecision);
	setTaskDecisionList(data, taskDecisionList2);
    }

    public static void setTaskDecisionList(Map<String, Object> data, List<Map<String, Object>> taskDecisionList2) {
	data.put(taskDecisionList, taskDecisionList2);
    }

    /**
     * 根据策略名获取对应的策略对象信息
     * 
     * @param decisionName
     * @return
     */
    public static Map<String, Object> getWfTaskDecisionByName(String decisionName, Map<String, Object> data) {
	Map<String, Object> reDecision = null;
	List<Map<String, Object>> taskDecisions = getTaskDecisionList(data);
	if (null != decisionName && null != taskDecisions && taskDecisions.size() > 0) {
	    for (Map<String, Object> tmp : taskDecisions) {
		if (Decision.getName(tmp).equalsIgnoreCase(decisionName)) {
		    reDecision = tmp;
		    break;
		}
	    }
	}
	return reDecision;
    }

    /**
     * 添加任务执行人信息到任务实例中
     * 
     * @param taskExecutor 任务执行人
     * @throws CrudBaseException
     */
    public static void addTaskExecutor(Map<String, Object> taskExecutor, Map<String, Object> data)
	    throws CrudBaseException {
	if (taskExecutor == null) {
	    throw new CrudBaseException("需要添加的任务执行人为空,不能添加到任务实例中");
	}
	List<Map<String, Object>> executors = getTaskExecutorList(data);
	if (executors == null) {
	    executors = new ArrayList<>();
	}
	executors.add(taskExecutor);
	data.put(taskExecutorList, executors);
    }

    /**
     * 清除任务中的所有执行人信息
     */
    public static void removeAllTaskExecutor(Map<String, Object> data) {
	data.remove(taskExecutorList);
    }

    /**
     * 清除指定的任务执行人信息
     * 
     * @param taskExecutor 任务执行人信息
     */
    public static void removeTaskExecutor(Map<String, Object> taskExecutor, Map<String, Object> data) {
	List<Map<String, Object>> taskExecutorList = getTaskExecutorList(data);
	if (taskExecutor != null && taskExecutorList != null) {
	    Map<String, Object> executorDel = null;
	    for (Map<String, Object> executor : taskExecutorList) {
		if (BpmDo.id(executor) == BpmDo.id(taskExecutor)) {
		    executorDel = executor;
		    break;
		}
	    }
	    taskExecutorList.remove(executorDel);
	}
    }

    /**
     * 得到流程任务之属性信息
     * 
     * @return 流程任务之属性信息.
     */
    public static Map<String, Object> getTaskProperty(Map<String, Object> data) {
	return MapTool.mapObject(data, taskProperty);
    }

    /**
     * 设置流程任务之属性信息
     * 
     * @param taskProp 流程任务之属性信息.
     */
    public static void setTaskProperty(Map<String, Object> taskProp, Map<String, Object> data) {
	data.put(taskProperty, taskProp);
    }

}

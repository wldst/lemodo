package com.wldst.ruder.module.workflow.template.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;

/**
 * @author wldst
 * 
 */
public class Tsimple extends TemplateTask {
    /**
     * 默认构造函数
     */
    // WFEConstants.WFTASK_TYPE_SIMPLE

    // 普通任务节点的后续节点id
    public static String next="next";

    // 普通任务节点的前续节点id
    public static String previous="previous";

    // 任务描述
    public static String descript="descript";

    // 任务执行人集合
    public static String taskExecutors="taskExecutors";

    // 任务属性集合
    public static String taskProperties="taskProperties";

    // 任务决策信息集合
    public static String taskDecisions="taskDecisions";

    /**
     * 获取任务决策信息列表
     * 
     * @return 任务决策信息列表
     */
    public static List<Map<String, Object>> getTaskDecisionList(Map<String, Object> data) {
	List<Map<String, Object>> retList = null;
	Map<String, Map<String, Object>> taskDecisions = taskDecisions(data);
	if (taskDecisions != null) {
	    retList = new ArrayList<>(taskDecisions.values());
	}
	return retList;
    }

    /**
     * 获取指定的任务决策信息
     * 
     * @param decisionName 任务决策名
     * @return 任务决策信息
     */
    public static Map<String, Object> getTaskDecision(String decisionName,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskDecisions = taskDecisions(data);
	if (taskDecisions != null) {
	    return taskDecisions.get(decisionName);
	} else {
	    return null;
	}
    }

    /**
     * 清除所有任务决策信息
     */
    public static void removeAllTaskDecision(Map<String, Object> data) {
	Map<String, Map<String, Object>> taskDecisions = taskDecisions(data);
	if (taskDecisions != null) {
	    taskDecisions.clear();
	}
    }

    /**
     * 移出指定的任务决策信息
     * 
     * @param decisionName 任务决策名
     */
    public static void removeTaskDecision(String decisionName,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskDecisions = taskDecisions(data);
	if (taskDecisions != null) {
	    taskDecisions.remove(decisionName);
	}
    }

    /**
     * 增加任务决策信息
     * 
     * @param taskDecision 任务决策信息
     */
    public static void addTaskDecision(Map<String, Object> taskDecision,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskDecisions = taskDecisions(data);
	if (taskDecisions == null) {
	    taskDecisions = new HashMap<>();
	}
	taskDecisions.put(MapTool.name(taskDecision), taskDecision);
    }

    
    public static Map<String, Map<String, Object>> taskDecisions(Map<String, Object> data) {
	return MapTool.mapKeyMap(data, taskDecisions);
    }

    /**
     * 得到任务属性列表
     * 
     * @return 任务属性列表
     */
    public static List<Map<String, Object>> getTaskPropertyList(Map<String, Object> data) {
	List<Map<String, Object>> retList = null;
	Map<String, Map<String, Object>> taskProperties = taskProperties(data);
	if (taskProperties != null) {
	    retList = new ArrayList<>(taskProperties.values());
	}
	return retList;
    }

    /**
     * 获取指定的任务属性信息
     * 
     * @param propName 任务属性原名
     * @return 任务属性信息
     */
    public static Map<String, Object> getTaskProperty(String propName,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskProperties = taskProperties(data);
	if (taskProperties != null) {
	    return taskProperties.get(propName);
	} else {
	    return null;
	}
    }

    /**
     * 移出任务属性
     * 
     * @param taskOrigName 指定的任务属性原名
     */
    public static void removeTaskProperty(String taskOrigName,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskProperties = taskProperties(data);
	if (taskProperties != null) {
	    taskProperties.remove(taskOrigName);
	}
    }

    /**
     * 设置任务属性值
     * 
     * @param taskOrigName 任务属性名
     * @param value        任务属性值
     */
    public static void setTaskPropertyValue(String taskOrigName, String value,Map<String, Object> data) {
	Map<String, Object> prop = getTaskProperty(taskOrigName,data);
	if (prop == null) {
	    prop = new HashMap<>();
	    prop.put("name", WFEConstants.convertPropertyNameZh(taskOrigName));
	    prop.put("originalName", taskOrigName);
	    addTaskProperty(prop,data);
	}
	prop.put("value", value);
    }

    /**
     * 增加任务属性信息
     * 
     * @param taskProperty 任务属性信息
     */
    public static void addTaskProperty(Map<String, Object> taskProperty,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskProperties = taskProperties(data);
	if (taskProperties == null) {
	    taskProperties = new HashMap<>();
	}
	taskProperties.put(MapTool.string(taskProperty, "originalName"), taskProperty);
    }

    public static Map<String, Map<String, Object>> taskProperties(Map<String, Object> data) {
	return MapTool.mapKeyMap(data, taskProperties);
    }

    /**
     * 得到任务定义执行人信息列表
     * 
     * @return 任务执行人定义信息列表
     */
    public static List<Map<String, Object>> getTemplateTaskExecutorList(Map<String, Object> data) {
	List<Map<String, Object>> retList = null;
	 Map<String, Map<String, Object>> taskExecutors = taskExecutors(data);
	if (taskExecutors != null) {
	    retList = new ArrayList<>(taskExecutors.values());
	}
	return retList;
    }

    public static Map<String, Map<String, Object>> taskExecutors(Map<String, Object> data) {
	return MapTool.mapKeyMap(data, taskExecutors);
    }

    /**
     * 根据指定的任务执行人定义id得到任务执行人定义信息
     * 
     * @param id 任务执行人定义id
     * @return 任务执行人定义信息
     */
    public static Map<String, Object> getTemplateTaskExecutor(String id,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskExecutors = taskExecutors(data);
	if (taskExecutors != null) {
	    return taskExecutors.get(id);
	} else {
	    return null;
	}
    }

    /**
     * 删除指定的任务执行人定义信息
     * 
     * @param id 指定任务执行人定义id
     */
    public static void removeTaskExecutor(String id,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskExecutors = taskExecutors(data);
	if (taskExecutors != null) {
	    taskExecutors.remove(id);
	}
    }

    /**
     * 添加任务执行人
     * 
     * @param executor 需要添加的任务执行人
     */
    public static void addTaskExecutor(Map<String, Object> executor,Map<String, Object> data) {
	Map<String, Map<String, Object>> taskExecutors = taskExecutors(data);
	if (taskExecutors == null) {
	    taskExecutors = new HashMap<>();
	}
	taskExecutors.put(MapTool.string(executor, "id"), executor);
    }
    
    /**
     * 得到结束节点的前续节点id
     * 
     * @return 结束节点的前续节点id.
     */
    public static String getPrevious(Map<String, Object> data) {
	return MapTool.string(data, previous);
    }

    /**
     * 设置结束节点的前续节点id
     * 
     * @param previous 结束节点的前续节点id.
     */
    public static void setPrevious(String previousS, Map<String, Object> data) {
	data.put(previous, previousS);
    }
    
    /**
     * 得到开始节点的前续节点id
     * 
     * @return 开始节点的前续节点id.
     */
    public static String getNext(Map<String, Object> data) {
	return MapTool.string(data, next);
    }

    /**
     * 设置开始节点的前续节点id
     * 
     * @param previous 开始节点的前续节点id.
     */
    public static void setNext(String previousS, Map<String, Object> data) {
	data.put(next, previousS);
    }
    
    /**
     * 得到任务描述
     * 
     * @return 任务描述.
     */
    public static String getDescript(Map<String, Object> data) {
	return MapTool.string(data, descript);
    }

    /**
     * 设置任务描述
     * 
     * @param descript 任务描述.
     */
    public static void setDescript(String descript, Map<String, Object> data) {
	data.put(descript, descript);
    }

}

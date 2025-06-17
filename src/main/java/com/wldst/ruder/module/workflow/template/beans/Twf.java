package com.wldst.ruder.module.workflow.template.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.util.MapTool;

/**
 * 流程模板信息
 * 
 * @author wldst
 */
public class Twf extends BpmDo {
      
     // 流程模板名
     public static String   wfName="wfName";
    
     // 流程模板标识
     public static String   wfMark="wfMark";
    
     // 流程模板描述
     public static String   wfDescript="wfDescript";
    
     // 是否最新版本
     public static String   newestVerFlag="newestVerFlag";
    
     // 模板开始任务
      public static String startTask="startTask";
    
     // 模板结束任务
      public static String endTask="endTask";
    
     // 模板普通任务集合
      public static String simpleTasks="simpleTasks";
    
     // 模板分支任务集合
      public static String branchTasks="branchTasks";
    
     // 模板收缩任务集合
      public static String shrinkTasks="shrinkTasks";

    // 最大计数
    public static String   maxNo ="maxNo";

    public static int getMaxNo(Map<String, Object> data) {
	return MapTool.integer(data,"maxNo");
    }

    public static void setMaxNo(int maxNo, Map<String, Object> data) {
	data.put("maxNo",maxNo);
    }

    /**
     * 获取当前流程模板中的任务数
     * 
     * @return 任务数
     */
    public static int getTaskCount(Map<String, Object> data) {
	int taskCount = 0;
	if (getStartTask(data) !=null) {
	    taskCount++;
	}
	if (getEndTask(data) !=null) {
	    taskCount++;
	}
	Map<String, Map<String, Object>> simpleTasks = getSimpleTasksMap(data);
	if (simpleTasks !=null) {
	    taskCount = taskCount + simpleTasks.size();
	}
	Map<String, Map<String, Object>> branchTasks = getBranchTasksMap(data);
	if (branchTasks !=null) {
	    taskCount = taskCount + branchTasks.size();
	}
	Map<String, Map<String, Object>> shrinkTasks = getShrinkTasksMap(data);
	if (shrinkTasks !=null) {
	    taskCount = taskCount + shrinkTasks.size();
	}
	return taskCount;
    }

    /**
     * 根据任务内部ID得到任务定义信息
     * 
     * @param innerID 任务内部ID
     * @return 任务定义信息
     */
    public static Map<String, Object> getByInnerID(String innerID, Map<String, Object> data) {
	Map<String, Object> retTask = null;
	Map<String, Object> startTask = getStartTask(data);
	if (MapTool.string(startTask, TASK_INNER_ID).equals(innerID)) {
	    retTask = startTask;
	}
	if (retTask ==null) {
	    Map<String, Object> endTask = getEndTask(data);		
	    if (MapTool.string(endTask, TASK_INNER_ID).equals(innerID)) {
		retTask = endTask;
	    }
	}
	if (retTask ==null) {
	    retTask = getSimpleTask(innerID,data);
	}
	if (retTask ==null) {
	    retTask = getBranchTask(innerID,data);
	}
	if (retTask ==null) {
	    retTask = getShrinkTaskByInnerId(innerID,data);
	}
	return retTask;
    }

    /**
     * 获取流程模板收缩任务信息列表
     * 
     * @return 收缩任务信息列表
     */
    public static List<Map<String, Object>> getShrinTaskList(Map<String, Object> data) {
	List<Map<String, Object>> retList = null;
	Map<String, Map<String, Object>> shrinkTasks = getShrinkTasksMap(data);
	if (shrinkTasks !=null) {
	    retList = new ArrayList<>(shrinkTasks.values());
	}
	return retList;
    }

    /**
     * 获取指定的流程模板收缩任务信息
     * 
     * @param sTaskInnerID 收缩任务ID
     * @return 流称估摸办收缩任务信息
     */
    public static Map<String, Object> getShrinkTaskByInnerId(String sTaskInnerID, Map<String, Object> data) {
	Map<String, Map<String, Object>> shrinkTasks = getShrinkTasksMap(data);
	if (shrinkTasks !=null) {
	    return shrinkTasks.get(sTaskInnerID);
	} else {
	    return null;
	}
    }

    /**
     * 移出指定的模板收缩任务信息
     * 
     * @param sTaskInnerID 收缩任务ID
     */
    public static void removeShrinkTask(String sTaskInnerID, Map<String, Object> data) {
	Map<String, Map<String, Object>> shrinkTasks = getShrinkTasksMap(data);
	if (shrinkTasks !=null) {
	    shrinkTasks.remove(sTaskInnerID);
	}
    }

    /**
     * 增加模板收缩任务信息
     * 
     * @param shrinkTask 模板收缩任务信息
     */
    public static void addShrinkTask(Map<String, Object> shrinkTask, Map<String, Object> data) {
	Map<String, Map<String, Object>> shrinkTasks = getShrinkTasksMap(data);
	if (shrinkTasks ==null) {
	    shrinkTasks = new HashMap<>();
	}
	shrinkTasks.put(MapTool.string(shrinkTask, TASK_INNER_ID), shrinkTask);
    }

    /**
     * 得到流程模板分支任务信息列表
     * 
     * @return 分支任务信息列表
     */
    public static List<Map<String, Object>> getBranchTask(Map<String, Object> data) {
	List<Map<String, Object>> retList = null;

	Map<String, Map<String, Object>> branchTasks = getBranchTasksMap(data);
	if (branchTasks !=null) {
	    retList = new ArrayList<>(branchTasks.values());
	}
	return retList;
    }

    /**
     * 获取模板分支任务
     * 
     * @param bTaskInnerID 分支任务内部ID
     * @return 流程模板分支任务
     */
    public static Map<String, Object> getBranchTask(String bTaskInnerID, Map<String, Object> data) {
	Map<String, Map<String, Object>> branchTasks = getBranchTasksMap(data);
	if (branchTasks !=null) {
	    return (Map<String, Object>) branchTasks.get(bTaskInnerID);
	} else {
	    return null;
	}
    }

    /**
     * 移出指定的流程模板分支任务
     * 
     * @param bTaskInnerID 分支任务内部ID
     */
    public static void removeBranchTask(String bTaskInnerID, Map<String, Object> data) {
	Map<String, Map<String, Object>> branchTasks = getBranchTasksMap(data);
	if (branchTasks !=null) {
	    branchTasks.remove(bTaskInnerID);
	}
    }

    /**
     * 增加模板流程分支任务集合
     * 
     * @param branchTask 模板流程分支任务
     */
    public static void addBranchTask(Map<String, Object> branchTask, Map<String, Object> data) {
	Map<String, Map<String, Object>> branchTasks = getBranchTasksMap(data);
	if (branchTasks ==null) {
	    branchTasks = new HashMap<>();
	}
	branchTasks.put(MapTool.string(branchTask, TASK_INNER_ID), branchTask);
    }

    /**
     * 获取流程模板普通任务信息列表
     * 
     * @return 普通任务信息列表
     */
    public static List<Map<String, Object>> getSimpleTasks(Map<String, Object> data) {
	List<Map<String, Object>> retList = null;
	Map<String, Map<String, Object>> simpleTasks = getSimpleTasksMap(data);
	if (simpleTasks !=null) {
	    retList = new ArrayList<>(simpleTasks.values());
	}
	return retList;
    }

    /**
     * 获取指定的流程模板普通任务信息
     * 
     * @param sInnerID 任务内部ID
     * @return 普通任务信息
     */
    public static Map<String, Object> getSimpleTask(String sInnerID, Map<String, Object> data) {
	Map<String, Map<String, Object>> simpleTasks = getSimpleTasksMap(data);
	if (simpleTasks !=null) {
	    return simpleTasks.get(sInnerID);
	} else {
	    return null;
	}
    }

    /**
     * 移出指定的流程模板普通任务信息
     * 
     * @param sInnerID 任务内部ID
     */
    public static void removeSimpleTask(String sInnerID, Map<String, Object> data) {
	Map<String, Map<String, Object>> simpleTasks = getSimpleTasksMap(data);
	if (simpleTasks !=null) {
	    simpleTasks.remove(sInnerID);
	}
    }

    /**
     * 增加流程模板普通任务信息
     * 
     * @param sTask 普通任务信息
     */
    public static void addSimpleTask(Map<String, Object> sTask, Map<String, Object> data) {
	Map<String, Map<String, Object>> simpleTasks = getSimpleTasksMap(data);
	if (simpleTasks ==null) {
	    simpleTasks = new HashMap<>();
	}
	simpleTasks.put(MapTool.string(sTask, TASK_INNER_ID), sTask);
    }

    /**
     * 获取模板流程结束任务信息
     * 
     * @return 结束任务信息
     */
    public static Map<String, Object> getEndTask(Map<String, Object> data) {
	return MapTool.mapObject(data, endTask) ;
    }

    /**
     * 设置模板流程结束任务信息
     * 
     * @param endTask 结束任务信息
     */
    public static void setEndTask(Map<String, Object> endTaskData, Map<String, Object> data) {
	data.put(endTask, endTaskData);
    }

    /**
     * 获取模板流程开始任务
     * 
     * @return 开始任务信息
     */
    public static Map<String, Object> getStartTask(Map<String, Object> data) {
	return MapTool.mapObject(data, startTask);
    }

    /**
     * 设置模板流程的开始任务信息
     * 
     * @param startTaskData 开始任务信息
     */
    public static void setStartTask(Map<String, Object> startTaskData, Map<String, Object> data) {
	data.put(startTask,startTaskData);
    }

    public static long getID(Map<String, Object> data) {
	return id(data);
    }

    public static void setID(long iD,Map<String, Object> data) {
	data.put(ID, iD);
    }

    public static String getWfName(Map<String, Object> data) {
	return MapTool.string(data,wfName);
    }

    public static void setWfName(String wname,Map<String, Object> data) {
	 data.put(wfName,wname);
    }

    public static String getWfMark(Map<String, Object> data) {
	return MapTool.string(data,wfMark);
    }

    public static void setWfMark(String wmark,Map<String, Object> data) {
	data.put("wfMark", wmark);
    }

    public static String getWfDescript(Map<String, Object> data) {
	return MapTool.string(data,"wfDescript");
    }

    public static void setWfDescript(String descript,Map<String, Object> data) {
	data.put(wfDescript, descript);
    }

    public static int getNewestVerFlag(Map<String, Object> data) {
	return MapTool.integer(data,newestVerFlag);
    }

    public static void setNewestVerFlag(int newest, Map<String, Object> data) {
	data.put(newestVerFlag, newest);
    }

    public static Map<String, Map<String, Object>> getSimpleTasksMap(Map<String, Object> data) {
	return MapTool.mapKeyMap(data, simpleTasks);
    }

    public static void setSimpleTasks(Map<String, Map<String, Object>> sTask, Map<String, Object> data) {
	data.put("simpleTasks", sTask);
    }

    public static Map<String, Map<String, Object>> getBranchTasksMap(Map<String, Object> data) {
	return MapTool.mapKeyMap(data, branchTasks);
    }

    public static void setBranchTasks(Map<String, Map<String, Object>> branch,Map<String, Object> data) {
	data.put(branchTasks, branch);
    }

    public static Map<String, Map<String, Object>> getShrinkTasksMap(Map<String, Object> data) {
	return MapTool.mapKeyMap(data, shrinkTasks);
    }

    public static void setShrinkTasks(Map<String, Map<String, Object>> shrinkData,Map<String, Object> data) {
	data.put(shrinkTasks, shrinkData);
    }

}

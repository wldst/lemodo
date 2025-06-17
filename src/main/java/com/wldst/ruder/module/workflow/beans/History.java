package com.wldst.ruder.module.workflow.beans;

import java.util.Map;

import com.wldst.ruder.util.MapTool;

/**
 * 流程实例之历史履历信息
 * 
 * 
 */
public class History {
    // 历史履历信息ID
    public static String ID="id";

    // 履历信息所属任务ID
    public static String wfTaskID="wfTaskId";

    // 履历信息所属流程实例ID
    public static String wfInstanceID="wfInstanceId";

    // 履历信息详细
    public static String wfExecuteHistory ="wfExecuteHistory";

    // 履历产生决策
    public static String wfTaskDecision="wfTaskDecision";

    // 履历信息创建时间
    public static String historyCreateDatetime="historyCreateDatetime";

    // 履历信息任务到达时间
    public static String taskComeDatetime="taskComeDatetime";

    // 履历创建人
    public static String historyCreateEmpID="historyCreateEmpID";

    // 执行动作中文名
    public static String wfTaskDecisionNameZh="wfTaskDecisionNameZh";

    // 履历创建人名
    public static String historyCreateEmpName="historyCreateEmpName";

    /**
     * 得到历史履历信息ID
     * 
     * @return 历史履历信息ID.
     */
    public long getID(Map<String, Object> data) {
	return MapTool.integer(data,ID);
    }

    /**
     * 设置历史履历信息ID
     * 
     * @param id 历史履历信息ID.
     */
    public void setID(long id, Map<String, Object> data) {
	data.put(ID ,id);
    }

    /**
     * 得到履历信息所属任务ID
     * 
     * @return 履历信息所属任务ID.
     */
    public long getWfTaskID(Map<String, Object> data) {
	return MapTool.integer(data,wfTaskID);
    }

    /**
     * 设置履历信息所属任务ID
     * 
     * @param wfTaskID 履历信息所属任务ID.
     */
    public void setWfTaskID(long wfTaskId, Map<String, Object> data) {
	data.put(wfTaskID,wfTaskId);
    }

    /**
     * 得到 履历信息所属流程实例ID
     * 
     * @return 履历信息所属流程实例ID.
     */
    public long getWfInstanceID(Map<String, Object> data) {
	return MapTool.longValue(data,wfInstanceID);
    }

    /**
     * 设置 履历信息所属流程实例ID
     * 
     * @param wfInstanceID 履历信息所属流程实例ID.
     */
    public void setWfInstanceID(long instanceId, Map<String, Object> data) {
	data.put(wfInstanceID , instanceId);
    }

    /**
     * 得到履历信息详细
     * 
     * @return 履历信息详细.
     */
    public String getWfExecuteHistory(Map<String, Object> data) {
	return MapTool.string(data,wfExecuteHistory);
    }

    /**
     * 设置履历信息详细
     * 
     * @param wfExecuteHistory 履历信息详细.
     */
    public void setWfExecuteHistory(String history, Map<String, Object> data) {
	data.put(wfExecuteHistory,history);
    }

    /**
     * 得到履历产生决策
     * 
     * @return 履历产生决策.
     */
    public String getWfTaskDecision(Map<String, Object> data) {
	return MapTool.string(data, wfTaskDecision);
    }

    /**
     * 设置履历产生决策
     * 
     * @param wfTaskDecision 履历产生决策.
     */
    public void setWfTaskDecision(String decision, Map<String, Object> data) {
	data.put(wfTaskDecision,decision);
    }

    /**
     * 得到履历信息创建时间
     * 
     * @return 履历信息创建时间.
     */
    public long getHistoryCreateDatetime(Map<String, Object> data) {
	return MapTool.integer(data,historyCreateDatetime);
    }

    /**
     * 设置履历信息创建时间
     * 
     * @param createDatetime 履历信息创建时间.
     */
    public void setHistoryCreateDatetime(long createDatetime, Map<String, Object> data) {
	data.put(historyCreateDatetime,createDatetime);
    }

    /**
     * 得到 履历创建人
     * 
     * @return 履历创建人.
     */
    public long getHistoryCreateEmpID(Map<String, Object> data) {
	return MapTool.integer(data,historyCreateEmpID);
    }

    /**
     * 设置 履历创建人
     * 
     * @param historyCreateEmpID 履历创建人.
     */
    public void setHistoryCreateEmpID(long createEmpId, Map<String, Object> data) {
	data.put(historyCreateEmpID,createEmpId);
    }

    /**
     * 得到执行动作中文名
     * 
     * @return 执行动作中文名.
     */
    public String getWfTaskDecisionNameZh(Map<String, Object> data) {
	return MapTool.string(data,wfTaskDecisionNameZh);
    }

    /**
     * 设置执行动作中文名
     * 
     * @param wfTaskDecisionNameZh 执行动作中文名.
     */
    public void setWfTaskDecisionNameZh(String wfTaskDecisionName, Map<String, Object> data) {
	data.put(wfTaskDecisionNameZh, wfTaskDecisionName);
    }

    /**
     * 得到履历创建人名
     * 
     * @return 履历创建人名.
     */
    public String getHistoryCreateEmpName(Map<String, Object> data) {
	return MapTool.string(data,historyCreateEmpName);
    }

    /**
     * 设置履历创建人名
     * 
     * @param historyCreateEmpName 履历创建人名.
     */
    public void setHistoryCreateEmpName(String createEmpName, Map<String, Object> data) {
	data.put(historyCreateEmpName,createEmpName);
    }

    /**
     * 获取任务到达执行人的时间
     * 
     * @return
     */
    public long getTaskComeDatetime(Map<String, Object> data) {
	return MapTool.integer(data,taskComeDatetime);
    }

    /**
     * 设置任务到达执行人的时间
     * 
     * @param taskComeDatetime
     */
    public void setTaskComeDatetime(long taskCometime, Map<String, Object> data) {
	data.put(taskComeDatetime ,taskCometime);
    }

}

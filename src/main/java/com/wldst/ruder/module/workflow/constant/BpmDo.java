package com.wldst.ruder.module.workflow.constant;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.module.workflow.util.TextUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;

public class BpmDo extends WFEConstants {

    public static final String EXECUTOR_CONDITION = "executorCondition";
    public static final String TASK_INNER_ID = "taskInnerId";
    public static final String NEXT_TASK_ID = "next";
    public static final String INSTANCEID = "instanceID";
    public static final String NODE_X = "nodeX";
    public static final String NODE_Y = "nodeY";
    public static final String NODE_W = "nodeW";
    public static final String NODE_H = "nodeH";
    public static final String SHAPE = "shape";
    public static final String TASK_NAME = "taskName";
    public static final String TASK_TYPE = "taskType";
    public static final String NODE_TYPE = "nodeType";
    
    public static final String TASK_STATUS = "taskStatus";
    public static final String TASK_PROPERTY = "taskProperty";
    public static final String INVOKE_CLASS = "invokeClass";
    public static final String EXECUTE_SUB_FLOW_CLASS = "executeSubFlowClass";

    public static final String TASK_EXTENDS_INFO_LIST = "taskExtendsInfoList";
    public static final String TASK_EXECUTOR_LIST = "taskExecutorList";
    public static final String TASK_DECISION_LIST = "taskDecisionList";

    public static final String OUTPUT_CONDITION = "outputCondition";
    public static final String OUTPUT_TASK_ID = "output";
    public static final String BRANCH_OUTPUT_LIST = "branchOutputList";

    public static final String RELATION_TASK_ID = "relationTaskId";
    public static final String DESCRIPT = "descript";

    public static final String NEXT_REL_TASK_LIST = "nextRelationTaskList";
    public static final String PRE_REL_TASK_LIST = "preRelationTaskList";
    public static final String ID = "id";
    public static final String DOCTYPE = "docType";

    public static final String MANAGE_LABEL = "mLabel";
    
    public static final String BPM_HISTORY = "BpmHistory";
    public static final String BPM_TASK_EXECUTE = "BpmTaskExecute";
    public static final String BPM_TASK = "BpmTask";
    public static final String BPM_NODE = "BpmNode";
    
    

    public static String taskInnerId(Map<String, Object> mapData) {
	return string(mapData, TASK_INNER_ID);
    }

    public static String next(Map<String, Object> mapData) {
	return string(mapData, NEXT_TASK_ID);
    }

    public static String output(Map<String, Object> mapData) {
	return string(mapData, OUTPUT_TASK_ID);
    }

    public static String invokeClass(Map<String, Object> mapData) {
	return string(mapData, INVOKE_CLASS);
    }

    public static String exeSubFlowClass(Map<String, Object> mapData) {
	return string(mapData, EXECUTE_SUB_FLOW_CLASS);
    }

    // ============位置相关接口========开始=====
    public static Integer nodeX(Map<String, Object> mapData) {
	return integer(mapData, NODE_X);
    }

    public static Integer fromX(Map<String, Object> mapData) {
	return nodeX(mapData) + nodeW(mapData);
    }

    public static Integer fromXW2(Map<String, Object> mapData) {
	return nodeX(mapData) + nodeW(mapData) / 2;
    }

    public static Double fromY(Map<String, Object> mapData) {
	return Double.valueOf(nodeY(mapData) + nodeH(mapData) / 2);
    }

    public static Integer fromYW4(Map<String, Object> mapData) {
	return nodeY(mapData) + nodeW(mapData) / 4;
    }

    public static Integer toY(Map<String, Object> mapData) {
	return nodeY(mapData) + nodeH(mapData) / 2;
    }

    public static Integer toEndY(Map<String, Object> mapData) {
	return fromYW4(mapData);
    }

    public static Integer nodeY(Map<String, Object> mapData) {
	return integer(mapData, NODE_Y);
    }

    public static Integer nodeW(Map<String, Object> mapData) {
	return integer(mapData, NODE_W);
    }

    public static Integer nodeH(Map<String, Object> mapData) {
	return integer(mapData, NODE_H);
    }

    // ============位置相关接口========结束=====
    public static Integer taskType(Map<String, Object> mapData) {
	return integer(mapData, TASK_TYPE);
    }
    public static String nodeType(Map<String, Object> mapData) {
   	return string(mapData, NODE_TYPE);
    }
    public static int nodeIntType(Map<String, Object> mapData) {
   	return integer(mapData, NODE_TYPE);
    }
    
    public static Integer taskStatus(Map<String, Object> mapData) {
	return integer(mapData, TASK_STATUS);
    }

    public static Map<String, Object> shape(Map<String, Object> mapData) {
	return mapObject(mapData, SHAPE);
    }
    
    public static String idString(Map<String, Object> mapData) {
	return string(mapData, ID);
    }

    public static Long relTaskId(Map<String, Object> mapData) {
	return longValue(mapData, RELATION_TASK_ID);
    }

    public static List<Map<String, Object>> nextRelTasks(Map<String, Object> mapData) {
	return listMapObject(mapData, NEXT_REL_TASK_LIST);
    }
    public static List<Map<String, Object>> preRelTasks(Map<String, Object> mapData) {
	return listMapObject(mapData, PRE_REL_TASK_LIST);
    }

    public static List<Map<String, Object>> extendsInfo(Map<String, Object> mapData) {
	return listMapObject(mapData, TASK_EXTENDS_INFO_LIST);
    }

    public static List<Map<String, Object>> taskDecisionList(Map<String, Object> mapData) {
	return listMapObject(mapData, TASK_DECISION_LIST);
    }

    public static List<Map<String, Object>> taskExecutorList(Map<String, Object> mapData) {
	return listMapObject(mapData, TASK_EXECUTOR_LIST);
    }

    public static List<Map<String, Object>> outs(Map<String, Object> mapData) {
	return listMapObject(mapData, BRANCH_OUTPUT_LIST);
    }

    public static String outputCondition(Map<String, Object> mapData) {
	return string(mapData, OUTPUT_CONDITION);
    }

    public static String taskName(Map<String, Object> mapData) {
	return name(mapData);
    }

    public static String descript(Map<String, Object> mapData) {
	return string(mapData, DESCRIPT);
    }

    public static String executorCondition(Map<String, Object> mapData) {
	return string(mapData, EXECUTOR_CONDITION);
    }

    public static Map<String, Object> realPreRel(Map<String, Object> mapData) {
	return mapObject(mapData, "realPreRelation");
    }

    public static Map<String, Object> taskProperty(Map<String, Object> mapData) {
	return mapObject(mapData, TASK_PROPERTY);
    }

    /**
     * 链接到下一个任务
     * 
     * @param line
     * @param nextTask
     */
    public static void lineTo(JSONObject line, Map<String, Object> nextTask) {
	String nTaskInnerId = BpmDo.taskInnerId(nextTask);
	Integer taskType = BpmDo.taskType(nextTask);
	switch (taskType) {
	case WFTASK_TYPE_START:
	    line.put("to", "start_" + TextUtil.substring(nTaskInnerId, "-"));
	    break;
	case WFTASK_TYPE_END:
	    line.put("to", "control_" + TextUtil.substring(nTaskInnerId, "-"));
	    break;
	case WFTASK_TYPE_SIMPLE:
	    line.put("to", "node_" + TextUtil.substring(nTaskInnerId, "-"));
	    break;
	case WFTASK_TYPE_BRANCH:
	    line.put("to", "branch_" + TextUtil.substring(nTaskInnerId, "-"));
	    break;
	case WFTASK_TYPE_SHRINK:
	    line.put("to", "shrink_" + TextUtil.substring(nTaskInnerId, "-"));
	    break;
	}
    }

    // ===============当前运行数据接口
    // 当前任务实例ID
    public static final String RD_CURRENT_TASK_ID = "currentTaskId";

    // 对应的流程实例ID
    public static final String RD_WF_ID = "workflowID";

    // 需要执行的决策KEY值
    public static final String DECISION_KEY = "decisionKey";

    // 需要执行的决策名
    public static final String DECISION_NAME = "decisionName";

    // 下一个任务ID(主要是对应打回决策动作)
    public static final String RD_NEXT_TASK_ID = "nextTaskId";

    // 下一个任务的执行人ID数组
    public static final String RD_NEXT_TASK_EXECUTOR_IDS = "nextTaskExecutorIds";

    // 当前执行人ID
    public static final String CURRENT_EXECUTOR_ID = "currentExecutorID";

    // 当前执行人姓名
    public static final String CURRENT_EXECUTOR_NAME = "currentExecutorName";

    // 任务到达当前执行人的时间
    public static final String CURRENT_TASK_COME_TIME = "currTaskComeDatetime";

    // 任务执行批复信息
    public static final String TASK_EXECUTE_CONFIRM = "taskExecuteConfirm";

    /**
     * 当前任务实例ID
     * 
     * @param mapData
     * @return
     */
    public static Long currentTaskId(Map<String, Object> mapData) {
	return longValue(mapData, RD_CURRENT_TASK_ID);
    }

    /**
     * 对应的流程实例ID
     * 
     * @param mapData
     * @return
     */
    public static Long workflowId(Map<String, Object> mapData) {
	return longValue(mapData, RD_WF_ID);
    }

    /**
     * 需要执行的决策KEY值
     * 
     * @param mapData
     * @return
     */
    public static String decisionKey(Map<String, Object> mapData) {
	return string(mapData, DECISION_KEY);
    }

    /**
     * 需要执行的决策名
     * 
     * @param mapData
     * @return
     */
    public static String decisionName(Map<String, Object> mapData) {
	return string(mapData, DECISION_NAME);
    }

    /**
     * 下一个任务ID(主要是对应打回决策动作)
     * 
     * @param mapData
     * @return
     */
    public static Long nextTaskId(Map<String, Object> mapData) {
	return longValue(mapData, RD_NEXT_TASK_ID);
    }

    /**
     * 下一个任务的执行人ID数组
     * 
     * @param mapData
     * @return
     */
    public static long[] nextTaskExecutorIDs(Map<String, Object> mapData) {
	return splitLong(mapData, RD_NEXT_TASK_EXECUTOR_IDS);
    }

    /**
     * 当前执行人ID
     * 
     * @param mapData
     * @return
     */
    public static Long currentExecutorId(Map<String, Object> mapData) {
	return longValue(mapData, CURRENT_EXECUTOR_ID);
    }

    /**
     * 当前执行人姓名
     * 
     * @param mapData
     * @return
     */
    public static String currentExecutorName(Map<String, Object> mapData) {
	return string(mapData, CURRENT_EXECUTOR_NAME);
    }

    /**
     * 任务到达当前执行人的时间
     * 
     * @param mapData
     * @return
     */
    public static Long currTaskComeDatetime(Map<String, Object> mapData) {
	return longValue(mapData, CURRENT_TASK_COME_TIME);
    }

    /**
     * 任务执行批复信息
     * 
     * @param mapData
     * @return
     */
    public static String taskExecuteConfirm(Map<String, Object> mapData) {
	return string(mapData, TASK_EXECUTE_CONFIRM);
    }

    public static final String EXECUTOR_TASK_ID = "wfTaskId";

    // 执行人对应任务ID
    public static long executorTaskId(Map<String, Object> mapData) {
	return longValue(mapData, EXECUTOR_TASK_ID);
    }

    // 对应流程实例ID
    public static final String EXECUTOR_WF_INSTANCE_ID = "instanceID";

    /**
     * 对应流程实例ID
     * 
     * @param mapData
     * @return
     */
    public static long wfInstanceId(Map<String, Object> mapData) {
	return longValue(mapData, EXECUTOR_WF_INSTANCE_ID);
    }

    public static final String EXECUTOR_ID = "executorId";

    /**
     * 对应执行人ID
     * 
     * @param mapData
     * @return
     */
    public static long executorId(Map<String, Object> mapData) {
	return longValue(mapData, EXECUTOR_ID);
    }

    // 执行人执行状态
    public static final String EXECUTOR_STATUS = "executorStatus";

    /**
     * 执行人执行状态
     * 
     * @param mapData
     * @return
     */
    public static String executorStatus(Map<String, Object> mapData) {
	return string(mapData, EXECUTOR_STATUS);
    }

    // 执行人所执行动作
    public static final String EXECUTOR_DECISION = "executorDecision";

    /**
     * 执行人所执行动作
     * 
     * @param mapData
     * @return
     */
    public static String executorDecision(Map<String, Object> mapData) {
	return string(mapData, EXECUTOR_DECISION);
    }

    // 任务到达执行人的时间
    public static final String TASK_COME_TIME = "taskComeDateTime";

    /**
     * 任务到达执行人的时间
     * 
     * @param mapData
     * @return
     */
    public static Long taskComeTime(Map<String, Object> mapData) {
	return longValue(mapData, TASK_COME_TIME);
    }

    // 决策显示名
    public static final String DECISION_V_NAME = "decisionViewName";

    /**
     * 决策显示名
     * 
     * @param mapData
     * @return
     */
    public static String decisionViewName(Map<String, Object> mapData) {
	return string(mapData, DECISION_V_NAME);
    }

    // 决策描述
    public static final String DECISION_DESCRIPT = "decisionDescript";

    /**
     * 决策描述
     * 
     * @param mapData
     * @return
     */
    public static String decisionDescript(Map<String, Object> mapData) {
	return string(mapData, DECISION_DESCRIPT);
    }

    // 任务到达执行人的时间
    public static final String ORDER_NUM = "orderNum";

    /**
     * 排序号
     * 
     * @param mapData
     * @return
     */
    public static Long orderNum(Map<String, Object> mapData) {
	return longValue(mapData, ORDER_NUM);
    }

    public static final String EXECUTE_TYPE = "executeType";

    /**
     * 执行方式
     * 
     * @param mapData
     * @return
     */
    public static int executeType(Map<String, Object> mapData) {
	return integer(mapData, ORDER_NUM);
    }
    // ==============taskProperty

    public static final String WAIT_EXEC_SUB_FLOW = "waitExecSubFlow";

    /**
     * 执行方式
     * 
     * @param mapData
     * @return
     */
    public static int waitExecSubFlow(Map<String, Object> mapData) {
	return integer(mapData, WAIT_EXEC_SUB_FLOW);
    }

    public static final String TASK_AUTO_EXECUTE = "taskAutoExecute";

    /**
     * 执行方式
     * 
     * @param mapData
     * @return
     */
    public static int taskAutoExecute(Map<String, Object> mapData) {
	return integer(mapData, TASK_AUTO_EXECUTE);
    }

    public static void removeAllExecutor(Map<String, Object> mapData) {
	remove(mapData, TASK_EXECUTOR_LIST);
    }

    public static void clear(Map<String, Object> mapData, String key) {
	putNull(mapData, key);
    }

    // ====================workFlowData=======================
    
  

    public static final String WF_HISTORY = "wfHistory";

    public static void addWfHistory(Map<String, Object> mapData, Map<String, Object> history) {
	addMap(mapData, WF_HISTORY, history);
    }
    public static final String WF_HISTORY_LIST = "wfHistoryList";

    public static List<Map<String, Object>> wfHistoryList(Map<String, Object> mapData) {
	return listMapObject(mapData, WF_HISTORY_LIST);
    }
    

    public static final String WF_TASK_EXECUTOR = "taskExecutor";

    /**
     * 添加任务执行人员
     * 
     * @param mapData
     * @param executor
     */
    public static void addTaskExecutor(Map<String, Object> mapData, Map<String, Object> executor) {
	addMap(mapData, WF_TASK_EXECUTOR, executor);
    }

    public static final String WF_TASK_DECISION = "wfTaskDecision";

    public static Map<String, Object> wfTaskDecision(Map<String, Object> mapData, String name) {
	Map<String, Object> retObj = null;
	if(mapData==null) {
	    return retObj;
	}
	List<Map<String, Object>> taskDecisionList = listMapObject(mapData, WF_TASK_DECISION);

	if (taskDecisionList != null) {
	    int listSize = taskDecisionList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempObj = taskDecisionList.get(i);
		if (name(tempObj).equals(name)) {
		    retObj = tempObj;
		    break;
		}
	    }
	}
	return retObj;
    }

    public static Map<String, Object> realNextRelation(Map<String, Object> turnBackTask) {
	// TODO Auto-generated method stub
	return null;
    }

    public static int realFlowRoadFlag(Map<String, Object> realNext) {
	return integer(realNext, "realFlowRoadFlag");
    }

    public static int triggerSubWfFlag(Map<String, Object> workflow) {

	return integer(workflow, "triggerSubWfFlag");
    }

    public static long triggerWfInsId(Map<String, Object> workflow) {
	return integer(workflow, "triggerWfInsId");
    }

    public static final String WF_STATUS = "wfStatus";

    public static int wfStatus(Map<String, Object> workflow) {
	return integer(workflow, WF_STATUS);
    }
    // =================workflow=========================

    public static final String label = "label";

    public static String label(Map<String, Object> data) {
	return string(data, label);
    }
    
    public static String docType(Map<String, Object> data) {
   	return string(data, DOCTYPE);
       }
   
    public static final String bizDataId = "bizDataId";

    public static Long bizId(Map<String, Object> data) {
	return longValue(data, bizDataId);
    }
    public static final String TEMPLATE_MARK = "templateMark";
    public static String templateMark(Map<String, Object> data) {
   	return string(data, TEMPLATE_MARK);
       }
     
}

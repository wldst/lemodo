package com.wldst.ruder.module.workflow.util;

import com.wldst.ruder.domain.SystemDomain;
import com.wldst.ruder.util.MapTool;

/**
 * 工作流常量定义
 * 
 */
public class WFEConstants extends SystemDomain{
    /** 流程实例状态：初始 */
    public final static int WFSTATUS_INIT = 1;

    /** 流程实例状态：运行 */
    public final static int WFSTATUS_RUN = 2;

    /** 流程实例状态：暂停 */
    public final static int WFSTATUS_PAUSE = 3;

    /** 流程实例状态：终止 */
    public final static int WFSTATUS_TERMINATE = 4;

    /** 流程实例状态：挂起 */
    public final static int WFSTATUS_SUSPEND = 5;

    /** 流程实例状态：结束 */
    public final static int WFSTATUS_END = 6;
    
    public final static String NOW_TASK_IDS ="nowTaskIDs";
	public final static String NOW_TASK_NAME ="nowTaskName";


    /**
     * 将工作流状态整数值转换为中文描述
     * 
     * @param wfStatus 工作流状态值
     * @return 工作流状态中文描述
     */
    public final static String convertWfStatusZh(Integer wfStatus) {
	String retStr = "";
	if(wfStatus==null) {
	    return "初始";
	}
	switch (wfStatus) {
	case WFSTATUS_INIT:
	    retStr = "初始";
	    break;
	case WFSTATUS_RUN:
	    retStr = "运行";
	    break;
	case WFSTATUS_PAUSE:
	    retStr = "暂停";
	    break;
	case WFSTATUS_TERMINATE:
	    retStr = "终止";
	    break;
	case WFSTATUS_SUSPEND:
	    retStr = "挂起";
	    break;
	case WFSTATUS_END:
	    retStr = "结束";
	    break;
	}
	return retStr;
    }

    /** 流程任务类型：开始任务 */
    public final static String NODE_TYPE_START = "Start";

    /** 流程任务类型：结束任务 */
    public final static String NODE_TYPE_END = "End";

    /** 流程任务类型：普通任务 */
    public final static String NODE_TYPE_NORMAL = "Normal";
    /** 流程任务类型：普通任务 */
    public final static String NODE_TYPE_GATEWAY = "Gateway";

	public final static String NODE_TYPE_SHRINK = "Shrink";

	/** 流程任务类型：所有任务 */
    public final static int WFTASK_TYPE_ALL = 0;

    /** 流程任务类型：开始任务 */
    public final static int WFTASK_TYPE_START = 1;

    /** 流程任务类型：结束任务 */
    public final static int WFTASK_TYPE_END = 2;

    /** 流程任务类型：普通任务 */
    public final static int WFTASK_TYPE_SIMPLE = 3;

    /** 流程任务类型：分支任务 */
    public final static int WFTASK_TYPE_BRANCH = 4;

    /** 流程任务类型：收缩任务 */
    public final static int WFTASK_TYPE_SHRINK = 5;

    /**
     * 将流程任务类型整数值转换为流程任务类型中文描述
     * 
     * @param wfTaskType 流程任务类型整数值
     * @return 流程任务类型中文描述
     */
    public final static String convertWfTaskTypeZh(int wfTaskType) {
	String retStr = "";
	switch (wfTaskType) {
	case WFTASK_TYPE_START:
	    retStr = "开始任务";
	    break;
	case WFTASK_TYPE_END:
	    retStr = "结束任务";
	    break;
	case WFTASK_TYPE_SIMPLE:
	    retStr = "普通任务";
	    break;
	case WFTASK_TYPE_BRANCH:
	    retStr = "分支任务";
	    break;
	case WFTASK_TYPE_SHRINK:
	    retStr = "收缩任务";
	    break;
	}
	return retStr;
    }

    /** 流程任务状态：等待 */
    public final static int WFTASK_STATUS_WAIT = 1;

    /** 流程任务状态：待办 */
    public final static int WFTASK_STATUS_READY = 2;

    /** 流程任务状态：执行 */
    public final static int WFTASK_STATUS_EXECUTE = 3;

    /** 流程任务状态：结束 */
    public final static int WFTASK_STATUS_END = 4;

    /**
     * 将流程任务状态整数值转换为中文描述
     * 
     * @param wfTaskStatus 流程任务状态整数值
     * @return 流程任务状态中文描述
     */
    public final static String convertWfTaskStatusZh(int wfTaskStatus) {
	String retStr = "";
	switch (wfTaskStatus) {
	case WFTASK_STATUS_WAIT:
	    retStr = "等待";
	    break;
	case WFTASK_STATUS_READY:
	    retStr = "待办";
	    break;
	case WFTASK_STATUS_EXECUTE:
	    retStr = "执行";
	    break;
	case WFTASK_STATUS_END:
	    retStr = "结束";
	    break;
	}
	return retStr;
    }

    /** 任务关系：前续 */
    public final static int TASKRELATION_PRE = 1;

    /** 任务关系：后续 */
    public final static int TASKRELATION_NEXT = 2;

    /** 任务属性：循环结束任务节点 */
    public final static String TASK_PROP_RELOOP_END = "system_reloopEndTaskInnerID";

    /** 任务属性：所有执行人必须执行 */
    public final static String TASK_PROP_ALL_EXECUTE = "system_allExecutorExecute";

    /** 任务属性：最多的环节执行人 */
    public final static String TASK_PROP_MAX_EXECUTOR = "system_maxExecutorNumber";

    /** 任务属性：做为循环开始节点 */
    public final static String TASK_PROP_LOOP_START = "system_loopStart";

    /** 任务属性：是否等待子流程执行 */
    public final static String TASK_PROP_WAIT_SUBFLOW = "system_execSubWorkflow";

    /** 任务属性：任务是否自动执行 */
    public final static String TASK_PROP_AUTO_EXECUTE = "system_taskAutoExecute";

    /** 任务属性：子流程触发class */
    public final static String TASK_PROP_SUBFLOW_TRIGGER = "system_execSubWorkflowClass";

    /** 任务属性：任务运行时自动触发class */
    public final static String TASK_PROP_RUN_TRIGGER = "system_taskRunningInvokeClassName";

    /** 任务属性：任务触发业务URI */
    public final static String TASK_PROP_TRIGGER_BIZ_URI = "system_triggerBizUri";

    /** 任务属性：任务触发业务方式 */
    public final static String TASK_PROP_TRIGGER_BIZ_TYPE = "system_triggerBizType";
    

    /**
     * 转换任务属性为中文描述
     * 
     * @param prop 任务属性
     * @return 任务属性中文描述
     */
    public final static String convertPropertyNameZh(String prop) {
	String retStr = null;
	if (prop != null) {
	    if (prop.equals(TASK_PROP_RELOOP_END)) {
		retStr = "循环结束任务节点";
	    }
	    if (prop.equals(TASK_PROP_ALL_EXECUTE)) {
		retStr = "所有执行人必须执行";
	    }
	    if (prop.equals(TASK_PROP_MAX_EXECUTOR)) {
		retStr = "最多的环节执行人";
	    }
	    if (prop.equals(TASK_PROP_LOOP_START)) {
		retStr = "做为循环开始节点";
	    }
	    if (prop.equals(TASK_PROP_WAIT_SUBFLOW)) {
		retStr = "是否等待子流程执行";
	    }
	    if (prop.equals(TASK_PROP_AUTO_EXECUTE)) {
		retStr = "任务是否自动执行";
	    }
	    if (prop.equals(TASK_PROP_SUBFLOW_TRIGGER)) {
		retStr = "子流程触发class ";
	    }
	    if (prop.equals(TASK_PROP_RUN_TRIGGER)) {
		retStr = "任务运行时自动触发class";
	    }
	    if (prop.equals(TASK_PROP_TRIGGER_BIZ_URI)) {
		retStr = "任务触发业务URI";
	    }
	    if (prop.equals(TASK_PROP_TRIGGER_BIZ_TYPE)) {
		retStr = "任务触发业务方式";
	    }

	}
	return retStr;
    }

    /** 执行人对任务的执行状态常量定义之：未执行 */
    public final static int WF_EXEC_USERSTATE_NONE = 0;

    /** 执行人对任务的执行状态常量定义之：已执行 */
    public final static int WF_EXEC_USERSTATE_EXECED = 1;

    /**
     * 将用户执行状态转换为中文描述
     * 
     * @param execState 执行状态
     * @return 执行情况
     */
    public final static String convertUserExecStateZh(int execState) {
	String retStr = "";
	switch (execState) {
	case WF_EXEC_USERSTATE_NONE:
	    retStr = "未执行";
	    break;
	case WF_EXEC_USERSTATE_EXECED:
	    retStr = "已执行";
	    break;
	}
	return retStr;
    }

    /** 流程决策动作：提交 */
    public final static String WFDECISION_COMMIT = "commit";

    /** 流程决策动作：撤回 */
    public final static String WFDECISION_CALLBACK = "callback";

    /** 流程决策动作：打回 */
    public final static String WFDECISION_TURNBACK = "turnback";
    public final static String WFDECISION_REVERSE = "reverse";

    /** 流程决策动作：无决策 */
    public final static String WFDECISION_NONE = "none";

    /** 流程决策动作：转办 */
    public final static String WFDECISION_FORWARD = "forward";

    /** 流程决策动作：同意 */
    public final static String WFDECISION_AGREE = "agree";

    /** 流程决策动作：不同意 */
    public final static String WFDECISION_DISAGREE = "disagree";

    /** 流程决策动作：跳转 */
    public final static String WFDECISION_RELOOP = "reloop";

    /**
     * 将决策key值转换为决策中文名字
     * 
     * @param decisionKey 决策key值
     * @return 决策中文名
     */
    public final static String convertWfDecisionNameZh(String decisionKey) {
	String retStr = "";
	if (decisionKey != null && decisionKey.equals(WFDECISION_COMMIT)) {
	    retStr = "提交";
	}
	if (decisionKey != null && decisionKey.equals(WFDECISION_CALLBACK)) {
	    retStr = "撤回";
	}
	if (decisionKey != null && decisionKey.equals(WFDECISION_TURNBACK)) {
	    retStr = "打回";
	}
	if (decisionKey != null && decisionKey.equals(WFDECISION_NONE)) {
	    retStr = "无决策";
	}
	if (decisionKey != null && decisionKey.equals(WFDECISION_FORWARD)) {
	    retStr = "转办";
	}
	if (decisionKey != null && decisionKey.equals(WFDECISION_AGREE)) {
	    retStr = "同意";
	}
	if (decisionKey != null && decisionKey.equals(WFDECISION_DISAGREE)) {
	    retStr = "不同意";
	}
	if (decisionKey != null && decisionKey.equals(WFDECISION_RELOOP)) {
	    retStr = "跳转";
	}
	return retStr;
    }

    /**
     * 根据流程状态转化为流程执行情况描述
     * 
     * @param wfStatus 流程状态
     * @return 流程执行情况描述
     */
    public final static String convertWFStateForExecuteZh(int wfStatus) {
	String retStr = "【" + convertWfStatusZh(wfStatus) + "】";
	switch (wfStatus) {
	case WFSTATUS_INIT:
	    retStr = retStr + "流程具备了运行条件,还未正式运行,不能对其进行处理";
	    break;
	case WFSTATUS_RUN:
	    retStr = retStr + " 流程运行中,可进行正常的流程操作";
	    break;
	case WFSTATUS_PAUSE:
	    retStr = retStr + " 流程暂时停止运行,不能对其进行处理";
	    break;
	case WFSTATUS_TERMINATE:
	    retStr = retStr + " 流程被强制终止,不能继续运行,也不能做其他操作";
	    break;
	case WFSTATUS_SUSPEND:
	    retStr = retStr + " 流程因为某种原因出去挂起状态,不能对其进行处理";
	    break;
	case WFSTATUS_END:
	    retStr = retStr + " 流程正常结束,已不能再对其进行处理";
	    break;
	}
	return retStr;
    }

    /** 流程标签显示方式：工具条方式 */
    public final static int WFTAG_DISPLAYTYPE_TOOLBAR = 1;

    /** 流程标签显示方式：按钮方式 */
    public final static int WFTAG_DISPLAYTYPE_BTN = 2;

    /** 决策执行方式:确认和取消 */
    public final static int DECISION_EXEC_YESNO = 1;

    /** 决策执行方式:人员选择 */
    public final static int DECISION_EXEC_EMP = 2;

    /**
     * 转换决策执行方式中文描述
     * 
     * @param execType 决策执行方式
     * @return 决策执行方式中文描述
     */
    public final static String convertDecisionExecTypeZh(int execType) {
	String retStr = "";
	switch (execType) {
	case DECISION_EXEC_YESNO:
	    retStr = "确认和取消";
	    break;
	case DECISION_EXEC_EMP:
	    retStr = "人员选择";
	    break;
	}
	return retStr;
    }

    /** 流程操作:待办 */
    public final static int WFOP_READY = 1;

    /** 流程操作:已办 */
    public final static int WFOP_EXECED = 2;

    /** 流程操作:全部 */
    public final static int WFOP_ALL = 3;

    /**
     * 转换流程操作为中文描述
     * 
     * @param wfop 流程操作
     * @return 流程操作中文描述
     */
    public final static String converWfOPNameZh(int wfop) {
	String retStr = "";
	switch (wfop) {
	case WFOP_READY:
	    retStr = "待办理";
	    break;
	case WFOP_EXECED:
	    retStr = "已办理";
	    break;
	case WFOP_ALL:
	    retStr = "全部";
	    break;
	}
	return retStr;
    }

    /** 业务触发方式：页面迁移 */
    public final static int BIZTYPE_PAGE = 0;

    /** 业务触发方式：弹出窗口显示 */
    public final static int BIZTYPE_POPUP = 1;

    /**
     * 将业务触发方式转换为中文描述
     * 
     * @param bizType 业务触发方式
     * @return 中文描述
     */
    public final static String convertBizTypeZh(int bizType) {
	String retStr = "";
	switch (bizType) {
	case BIZTYPE_PAGE:
	    retStr = "页面迁移";
	    break;
	case BIZTYPE_POPUP:
	    retStr = "弹出窗口迁移";
	    break;
	}
	return retStr;
    }

    /** 布尔型数值：真 */
    public final static int DB_BOOLEAN_TRUE = 1;

    /** 布尔型数值：假 */
    public final static int DB_BOOLEAN_FALSE = 0;

    /**
     * 将布尔型数值转换为中文描述
     * 
     * @param booleanType 布尔型数值
     * @return 布尔型中文描述
     */
    public final static String convertBooleanZh(int booleanType) {
	String retStr = "";
	switch (booleanType) {
	case DB_BOOLEAN_TRUE:
	    retStr = "是";
	    break;
	case DB_BOOLEAN_FALSE:
	    retStr = "否";
	    break;
	}
	return retStr;
    }

    
}

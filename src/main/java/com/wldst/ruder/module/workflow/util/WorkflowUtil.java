package com.wldst.ruder.module.workflow.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.util.MapTool;

/**
 * 工作流操作共同方法
 * 
 * @author wldst
 */
@Component
public class WorkflowUtil extends MapTool{
    // 日志对象
    final static Logger logger = LoggerFactory.getLogger(WorkflowUtil.class);

    // 工作流操作共同方法静态实例变量
    private static WorkflowUtil instance;
    @Autowired
    private BpmInstance bpmi;
    /**
     * 屏蔽默认构造函数
     */
    private WorkflowUtil() {

    }

    /**
     * 获取工作流操作共同方法实例对象
     * 
     * @return 工作流操作共同方法实例对象
     */
    public synchronized static WorkflowUtil getInstance() {
	if (instance == null) {
	    instance = new WorkflowUtil();
	}
	return instance;
    }

    /**
     * 得到与业务数据查询语句配合使用的<br>
     * 关于获取工作流相关信息的查询字段字符串
     * 
     * @return 查询字段字符串
     */
    public String getWorkflowQueryColumns() {
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append(" WFEOBJ.BIZTABLENAME,");
	sqlBuffer.append(" WFEOBJ.TEMPLATEMARK,");
	sqlBuffer.append(" WFEOBJ.WorkflowName,");
	sqlBuffer.append(" WFEOBJ.WfInstanceID,");
	sqlBuffer.append(" WFEOBJ.WfStatus,");
	sqlBuffer.append(" WFEOBJ.TaskExecutorID,");
	sqlBuffer.append(" WFEOBJ.NowTaskIDs,");
	sqlBuffer.append(" WFEOBJ.NowInnerTaskID, ");
	sqlBuffer.append(" (case when WFEOBJ.WfStatus=6 then '已结束' else WFEOBJ.NowTaskName end) NowTaskName, ");
	sqlBuffer.append(" WFEOBJ.NowExecutorStatus ");
	return sqlBuffer.toString();
    }

    /**
     * 生成待办流程任务查询子SQL
     * 
     * @param bizDataIDColumns 对应业务数据表的主键字段
     * @param bizTableName     对应业务数据主表
     * @param executorId       制定的用户ID
     * @return 待办流程任务查询子SQL
     */
    public String getHandleTaskQuerySubSql(String bizDataIDColumns, String bizTableName, long executorId) {

	StringBuffer sqlBuffer = new StringBuffer();

	sqlBuffer.append(" JOIN (");
	sqlBuffer.append(" select WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	sqlBuffer.append(" WFE_USR.ExecutorID TaskExecutorID,");
	sqlBuffer.append(" WFE_USR.TASKCOMEDATETIME TASKCOMEDATETIME,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	sqlBuffer.append(" WFE_USR.ExecutorStatus NowExecutorStatus ");
	sqlBuffer.append(" from S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" inner join S_WFE_WfTaskExecutor WFE_USR on  WFE_USR.WfTaskID=WFE_INS.NowTaskIDs");
	sqlBuffer.append(" left join  S_WFE_WfTaskBaseInfo Now_WFE_TASK on Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(" WHERE   UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	sqlBuffer.append(" AND (WFE_INS.WfStatus= " + WFEConstants.WFSTATUS_INIT);
	sqlBuffer.append(" OR WFE_INS.WfStatus=" + WFEConstants.WFSTATUS_RUN);
	sqlBuffer.append(")");
	sqlBuffer.append(" AND WFE_USR.ExecutorID=" + executorId);
	sqlBuffer.append(" AND (Now_WFE_TASK.TaskStatus = " + WFEConstants.WFTASK_STATUS_WAIT);
	sqlBuffer.append(" OR Now_WFE_TASK.TaskStatus=" + WFEConstants.WFTASK_STATUS_READY + ")");
	sqlBuffer.append(" AND WFE_USR.ExecutorStatus = " + WFEConstants.WF_EXEC_USERSTATE_NONE);
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);
	return sqlBuffer.toString();
    }

    public String getAllTaskQuerySubSql(String bizDataIDColumns, String bizTableName, long executorId) {
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("  JOIN (");
	sqlBuffer.append(" select WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	sqlBuffer.append(" WFE_USR.ExecutorID TaskExecutorID,");
	sqlBuffer.append(" WFE_USR.TASKCOMEDATETIME TASKCOMEDATETIME,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	sqlBuffer.append(" WFE_USR.ExecutorStatus NowExecutorStatus ");
	sqlBuffer.append(" from S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" inner join S_WFE_WfTaskExecutor WFE_USR on  WFE_USR.WfTaskID=WFE_INS.NowTaskIDs");
	sqlBuffer.append(" left join  S_WFE_WfTaskBaseInfo Now_WFE_TASK on Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(" WHERE   UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	sqlBuffer.append(" AND WFE_USR.ExecutorID=" + executorId);
	sqlBuffer.append(" union ");
	sqlBuffer.append(" select distinct WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	sqlBuffer.append(" Now_Task_WFE_USR.ExecutorID TaskExecutorID,");
	sqlBuffer.append(" Now_Task_WFE_USR.TASKCOMEDATETIME TASKCOMEDATETIME,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	sqlBuffer.append(" 1 NowExecutorStatus ");
	sqlBuffer.append(" FROM S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join  S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" left join S_WFE_WfTaskBaseInfo Now_WFE_TASK on   Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(
		" left join S_WFE_WfTaskExecutor Now_Task_WFE_USR on Now_Task_WFE_USR.WfTaskID=Now_WFE_TASK.ID ");
	sqlBuffer.append(
		" where exists(select id from  S_WFE_WfInsHistory WFE_HIS where WFE_HIS.WfInstanceID=WFE_INS.ID");
	sqlBuffer.append(
		" and WFE_HIS.wftaskid != nvl(WFE_INS.NowTaskIDs,0) and nvl(Now_Task_WFE_USR.EXECUTORID,0) != WFE_HIS.HistoryCreateEmpID ");
	sqlBuffer.append(" and  WFE_HIS.HistoryCreateEmpID=" + executorId + " ) ");
	sqlBuffer.append(" and UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);
	sqlBuffer.append(" ");
	return sqlBuffer.toString();

    }

    /**
     * 生成已办流程任务查询子SQL
     * 
     * @param bizDataIDColumns 对应业务数据表的主键字段
     * @param bizTableName     对应业务数据主表
     * @param executorId       制定的用户ID
     * @return 已办流程任务查询子SQL
     */
    public String getExecutedTaskQuerySubSql(String bizDataIDColumns, String bizTableName, long executorId) {
	StringBuffer sqlBuffer = new StringBuffer();

	sqlBuffer.append(" JOIN (");
	sqlBuffer.append(" select distinct WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	sqlBuffer.append(" 0 TaskExecutorID,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	sqlBuffer.append(" 1 NowExecutorStatus ");
	sqlBuffer.append(" FROM S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join  S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" left join S_WFE_WfTaskBaseInfo Now_WFE_TASK on   Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(
		" left join S_WFE_WfTaskExecutor Now_Task_WFE_USR on Now_Task_WFE_USR.WfTaskID=Now_WFE_TASK.ID ");
	sqlBuffer.append(
		" where exists(select id from  S_WFE_WfInsHistory WFE_HIS where WFE_HIS.WfInstanceID=WFE_INS.ID");
	sqlBuffer.append(" and  WFE_HIS.HistoryCreateEmpID=" + executorId);
	sqlBuffer.append(
		" and WFE_HIS.wftaskid != nvl(WFE_INS.NowTaskIDs,0) and nvl(Now_Task_WFE_USR.EXECUTORID,0) != WFE_HIS.HistoryCreateEmpID");
	sqlBuffer.append(" ) and UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);

	return sqlBuffer.toString();
    }

    /**
     * 所有流程数据不和审批人挂钩
     * 
     * @param bizDataIDColumns
     * @param bizTableName
     * @return
     */
    public String getAllTaskQuerySubSql(String bizDataIDColumns, String bizTableName) {
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("  JOIN (");
	sqlBuffer.append(" select WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	// sqlBuffer.append(" WFE_USR.ExecutorID TaskExecutorID,");
	sqlBuffer.append(" 0 TaskExecutorID,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	// sqlBuffer.append(" WFE_USR.ExecutorStatus NowExecutorStatus ");
	sqlBuffer.append(" 0 NowExecutorStatus ");
	sqlBuffer.append(" from S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	// sqlBuffer.append(" left join S_WFE_WfTaskExecutor WFE_USR on
	// WFE_USR.WfTaskID=WFE_INS.NowTaskIDs");
	sqlBuffer.append(" left join  S_WFE_WfTaskBaseInfo Now_WFE_TASK on Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(" WHERE   UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	// sqlBuffer.append(" and WFE_MAP.BizDataID =" + bizDataIDColumns);
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);
	sqlBuffer.append(" ");
	return sqlBuffer.toString();
    }

    /**
     * Description： 所有流程数据不和审批人挂钩(取出当前流程执行人ID)<br/>
     * Date：2014-7-14 下午02:31:59 <br/>
     * Author：xbdai <br/>
     * 
     * @param bizDataIDColumns
     * @param bizTableName
     * @return
     */
    public String getAllTaskQuerySubSqlContainsExecutorId(String bizDataIDColumns, String bizTableName) {
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("  JOIN (");
	sqlBuffer.append(" select WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	sqlBuffer.append(" strcat(WFE_USR.ExecutorID) TaskExecutorID,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	// sqlBuffer.append(" WFE_USR.ExecutorStatus NowExecutorStatus ");
	sqlBuffer.append(" 0 NowExecutorStatus ");
	sqlBuffer.append(" from S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" left join S_WFE_WfTaskExecutor WFE_USR on  WFE_USR.WfTaskID=WFE_INS.NowTaskIDs");
	sqlBuffer.append(" left join  S_WFE_WfTaskBaseInfo Now_WFE_TASK on Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(" WHERE   UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	sqlBuffer.append(" group by  WFE_INS.ID  ,WFE_INS.WfStatus  ,");
	sqlBuffer.append(" WFE_MAP.BizDataID ,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName ,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID , ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName ");
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);
	sqlBuffer.append(" ");
	return sqlBuffer.toString();
    }

    /**
     * 生成待办流程任务查询子SQL不和审批人挂钩
     * 
     * @param bizDataIDColumns
     * @param bizTableName
     * @return
     */
    public String getHandleTaskQuerySubSql(String bizDataIDColumns, String bizTableName) {
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("  JOIN (");
	sqlBuffer.append(" select WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	sqlBuffer.append(" WFE_USR.ExecutorID TaskExecutorID,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	sqlBuffer.append(" WFE_USR.ExecutorStatus NowExecutorStatus ");
	sqlBuffer.append(" from S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" inner join S_WFE_WfTaskExecutor WFE_USR on  WFE_USR.WfTaskID=WFE_INS.NowTaskIDs");
	sqlBuffer.append(" left join  S_WFE_WfTaskBaseInfo Now_WFE_TASK on Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(" WHERE   UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	// sqlBuffer.append(" and WFE_MAP.BizDataID =" + bizDataIDColumns);
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);
	return sqlBuffer.toString();
    }

    /**
     * 生成已办流程任务查询子SQL不和审批人挂钩
     * 
     * @param bizDataIDColumns
     * @param bizTableName
     * @return
     */
    public String getExecutedTaskQuerySubSql(String bizDataIDColumns, String bizTableName) {
	StringBuffer sqlBuffer = new StringBuffer();

	sqlBuffer.append("  JOIN (");
	sqlBuffer.append(" select WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	sqlBuffer.append(" Now_Task_WFE_USR.ExecutorID TaskExecutorID,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	sqlBuffer.append(" Now_Task_WFE_USR.ExecutorStatus NowExecutorStatus ");
	sqlBuffer.append(" FROM S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join  S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" left join S_WFE_WfTaskBaseInfo Now_WFE_TASK on   Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(
		" left join S_WFE_WfTaskExecutor Now_Task_WFE_USR on Now_Task_WFE_USR.WfTaskID=Now_WFE_TASK.ID ");
	sqlBuffer.append(
		" where exists(select id from  S_WFE_WfInsHistory WFE_HIS where WFE_HIS.WfInstanceID=WFE_INS.ID)");
	sqlBuffer.append(" and UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	// sqlBuffer.append(" and WFE_MAP.BizDataID =" + bizDataIDColumns);
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);
	sqlBuffer.append(" ");
	return sqlBuffer.toString();
    }

    /**
     * 生成待办流程任务查询子SQL
     * 
     * @param bizDataIDColumns 对应业务数据表的主键字段
     * @param bizTableName     对应业务数据主表
     * @param executorId       制定的用户ID
     * @return 待办流程任务查询子SQL
     */
    public String getHandleTaskQuerySubSql(String bizDataIDColumns, String bizTableName, long executorId,
	    boolean isLeftJoin) {

	StringBuffer sqlBuffer = new StringBuffer();
	if (isLeftJoin) {
	    sqlBuffer.append(" left ");
	}
	sqlBuffer.append(" JOIN (");
	sqlBuffer.append(" select WFE_INS.ID WfInstanceID,WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	sqlBuffer.append(" WFE_USR.ExecutorID TaskExecutorID,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	sqlBuffer.append(" WFE_USR.ExecutorStatus NowExecutorStatus ");
	sqlBuffer.append(" from S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" inner join S_WFE_WfTaskExecutor WFE_USR on  WFE_USR.WfTaskID=WFE_INS.NowTaskIDs");
	sqlBuffer.append(" left join  S_WFE_WfTaskBaseInfo Now_WFE_TASK on Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(" WHERE   UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	sqlBuffer.append(" AND (WFE_INS.WfStatus= " + WFEConstants.WFSTATUS_INIT);
	sqlBuffer.append(" OR WFE_INS.WfStatus=" + WFEConstants.WFSTATUS_RUN);
	sqlBuffer.append(")");
	sqlBuffer.append(" AND WFE_USR.ExecutorID=" + executorId);
	sqlBuffer.append(" AND (Now_WFE_TASK.TaskStatus = " + WFEConstants.WFTASK_STATUS_WAIT);
	sqlBuffer.append(" OR Now_WFE_TASK.TaskStatus=" + WFEConstants.WFTASK_STATUS_READY + ")");
	sqlBuffer.append(" AND WFE_USR.ExecutorStatus = " + WFEConstants.WF_EXEC_USERSTATE_NONE);
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);
	return sqlBuffer.toString();
    }

    /**
     * 生成已办流程任务查询子SQL
     * 
     * @param bizDataIDColumns 对应业务数据表的主键字段
     * @param bizTableName     对应业务数据主表
     * @param executorId       制定的用户ID数组
     * @return 已办流程任务查询子SQL
     */
    /**
     * @param bizDataIDColumns
     * @param bizTableName
     * @param executorId
     * @return
     */
    public String getExecutedTaskQuerySubSql(String bizDataIDColumns, String bizTableName, long executorId,
	    boolean isLeftJoin) {

	StringBuffer sqlBuffer = new StringBuffer();
	if (isLeftJoin) {
	    sqlBuffer.append(" left ");
	}
	sqlBuffer.append(" JOIN (");
	sqlBuffer.append("SELECT DISTINCT WFE_INS.ID WfInstanceID,");
	sqlBuffer.append(" WFE_INS.WfStatus WfStatus,");
	sqlBuffer.append(" WFE_MAP.BizDataID BizDataID,");
	sqlBuffer.append(" WFE_MAP.BIZTABLENAME,");
	sqlBuffer.append(" WFE_TPL.TEMPLATEMARK,");
	sqlBuffer.append(" WFE_INS.WorkflowName WorkflowName,");
	// sqlBuffer.append(" WFE_TASK.TaskName TaskName,");
	// sqlBuffer.append(" WFE_TASK.ID WfTaskID,");
	// sqlBuffer.append(" WFE_TASK.TaskStatus TaskStatus,");
	// sqlBuffer.append(" WFE_TASK.InnerTaskID InnerTaskID,");
	sqlBuffer.append(" WFE_HIS.HistoryCreateEmpID TaskExecutorID,");
	sqlBuffer.append(" WFE_INS.NowTaskIDs, ");
	sqlBuffer.append(" Now_WFE_TASK.INNERTASKID NowInnerTaskID, ");
	sqlBuffer.append(" Now_WFE_TASK.TaskName NowTaskName, ");
	sqlBuffer.append(" Now_Task_WFE_USR.ExecutorStatus NowExecutorStatus ");

	sqlBuffer.append(" FROM S_WFE_BizWfInsMapping WFE_MAP ");
	sqlBuffer.append(" inner join  S_WFE_WfInstanceBaseInfo WFE_INS on WFE_INS.ID=WFE_MAP.WfInstanceID ");
	sqlBuffer.append(" inner join  s_wfe_workflowtemplate WFE_TPL on WFE_TPL.ID=WFE_INS.WFTEMPLATEID ");
	sqlBuffer.append(" inner join  S_WFE_WfInsHistory WFE_HIS on WFE_HIS.WfInstanceID=WFE_INS.ID ");
	sqlBuffer.append(" inner join  S_WFE_WfTaskBaseInfo WFE_TASK on WFE_HIS.WfTaskID=WFE_TASK.ID ");
	sqlBuffer.append(" left join S_WFE_WfTaskBaseInfo Now_WFE_TASK on   Now_WFE_TASK.ID=WFE_INS.NowTaskIDs ");
	sqlBuffer.append(
		" left join S_WFE_WfTaskExecutor Now_Task_WFE_USR on Now_Task_WFE_USR.WfTaskID=Now_WFE_TASK.ID and Now_Task_WFE_USR.ExecutorID="
			+ executorId);
	sqlBuffer.append(" WHERE ");
	sqlBuffer.append(" UPPER(WFE_MAP.BizTableName)='" + TextUtil.nvl(bizTableName).toUpperCase() + "'");
	// sqlBuffer.append(" AND WFE_USR.ExecutorID=" + executorId);
	// sqlBuffer.append(" AND (WFE_TASK.TaskStatus = "
	// + WFEConstants.WFTASK_STATUS_END);
	// sqlBuffer.append(")");
	sqlBuffer.append(" AND WFE_HIS.HistoryCreateEmpID in (" + executorId + ")");
	// sqlBuffer.append(" AND WFE_USR.ExecutorStatus = "
	// + WFEConstants.WF_EXEC_USERSTATE_EXECED);
	sqlBuffer.append(") WFEOBJ ");
	sqlBuffer.append(" ON WFEOBJ.BizDataID=" + bizDataIDColumns);
	sqlBuffer.append(" ");
	return sqlBuffer.toString();
    }

    /**
     * 填充业务触发URI中的特殊参数值
     * 
     * @param bizUri 需要填充的URI
     * @param bizID  制定的业务ID
     * @param taskID 制定的流程任务内部ID
     * @return 填充后的URI
     * @throws Exception
     */
    public String fillinUriParameter(String bizUri, String bizID, String taskID) throws Exception {
	String retUri = "";
	if (!TextUtil.isBlank(bizUri)) {
	    retUri = TextUtil.replaceString(bizUri, "{bizID}", bizID);
	    retUri = TextUtil.replaceString(retUri, "{taskID}", taskID);
	}
	return retUri;
    }

    /**
     * 获取触发URI中关于弹出窗口的参数部分
     * 
     * @param bizUri 触发URI
     * @return 弹出窗口参数部分
     */
    public String getPopupParameters(String bizUri) {
	String retParam = "";
	if (!TextUtil.isBlank(bizUri)) {
	    retParam = TextUtil.substringBetween(bizUri, "[", "]");
	}
	return retParam;
    }

    /**
     * 根据指定的任务，查找指定任务可打回的任务列表<br>
     * 也相当于查找任务的执行轨迹
     *
     * @param workflow    指定流程实例，包含流程的相关信息
     * @param currentTask 指定任务，需要查询其可打回的任务列表的任务
     * @param previewTask 前续任务，用于判断是否已经处理过或者作为查询的起始点
     * @return 可打回任务列表，返回一个包含可打回任务的列表
     */
    public List<Map<String, Object>> getExecutedWFSimpleTaskList(Map<String, Object> workflow, Map<String, Object> currentTask,
	    Map<String, Object> previewTask) {
	List<Map<String, Object>> retList = new ArrayList<>(); // 初始化返回的任务列表
	List<Map<String, Object>> tempList = null;

	// 如果当前任务不为空，且是简单任务类型且已经结束，则将其添加到返回列表中
	if (currentTask != null && BpmDo.taskType(currentTask) == WFEConstants.WFTASK_TYPE_SIMPLE
		&& BpmDo.taskStatus(currentTask) == WFEConstants.WFTASK_STATUS_END) {
	    retList.add(currentTask);
	}
	// 如果前续任务和当前任务相同，直接返回当前列表
	if (previewTask != null && currentTask != null
		&& id(currentTask) == id(previewTask)) {
	    return retList;
	}
	// 处理当前任务有前序任务的情况
	if (currentTask != null) {
	    // 获取当前任务的所有前序任务
	    List<Map<String, Object>> tempSubList = MapTool.listObjectMap(currentTask, "preRelationTaskList");
	    if (tempSubList != null && tempSubList.size() > 0) {
		// 遍历前序任务，查找可打回的任务
		for (int i = 0; i < tempSubList.size(); i++) {
		    Map<String, Object> sub = tempSubList.get(i);
		    Map<String, Object> preTask = bpmi.getWfTaskByID(MapTool.longValue(sub, "relationTaskId"));
		    // 判断前序任务是否为简单任务且已结束，或者根据不同的任务类型进行相应的处理
		    if (preTask != null) {
		 Integer taskType = BpmDo.taskType(preTask);
		 Integer status = BpmDo.taskStatus(preTask);
		 if (taskType == WFEConstants.WFTASK_TYPE_SIMPLE
			 && status == WFEConstants.WFTASK_STATUS_END) {
		     // 递归查询前序任务的可打回任务，并添加到返回列表中
		     tempList = getExecutedWFSimpleTaskList(workflow, preTask, previewTask);
		     if (tempList != null && tempList.size() > 0) {
			 for (int j = 0; j < tempList.size(); j++) {
			     retList.add(tempList.get(j));
			 }
		     }
		 } else if (taskType == WFEConstants.WFTASK_TYPE_SIMPLE
			 && status != WFEConstants.WFTASK_STATUS_END) {
		     // 处理未结束的简单任务情况
		     if (tempList != null && tempList.size() > 0) {
			 for (int j = 0; j < tempList.size(); j++) {
			     retList.add(tempList.get(j));
			 }
		     }
		 } else if (taskType == WFEConstants.WFTASK_TYPE_SHRINK) {
		     // 处理收缩任务类型
		     List<Map<String, Object>> shrinkSubList =  MapTool.listObjectMap(preTask, "preRelationTaskList");
		     if (shrinkSubList != null && shrinkSubList.size() > 0) {
			 for (int j = 0; j < shrinkSubList.size(); j++) {
			     Map<String, Object> shrinkSub =  shrinkSubList.get(j);
			     Map<String, Object> tempWFTAsk =
					 bpmi.getWfTaskByID(BpmDo.relTaskId(shrinkSub));
			     // 对收缩任务的前序任务进行处理，查询可打回的任务
			     if (status == WFEConstants.WFTASK_STATUS_END) {
				 tempList = getExecutedWFSimpleTaskList(workflow,
					 bpmi.getWfTaskByID(BpmDo.relTaskId(shrinkSub)),workflow);
				 if (tempList != null && tempList.size() > 0) {
				     for (int k = 0; k < tempList.size(); k++) {
					 retList.add(tempList.get(k));
				     }
				 }
			     }
			 }
		     }
		 } else if (taskType == WFEConstants.WFTASK_TYPE_BRANCH) {
		     // 处理分支任务类型
		     List<Map<String, Object>> branchSubList = MapTool.listObjectMap(preTask, "preRelationTaskList");
		     if (branchSubList != null && branchSubList.size() > 0) {
			 for (int j = 0; j < branchSubList.size(); j++) {
			     Map<String, Object> branchSub = branchSubList.get(j);
			     tempList = getExecutedWFSimpleTaskList(workflow,
					 bpmi.getWfTaskByID(BpmDo.relTaskId(branchSub)), previewTask);
			     if (tempList != null && tempList.size() > 0) {
				 for (int k = 0; k < tempList.size(); k++) {
					 retList.add(tempList.get(k));
				 }
			     }
			 }
		     }
		 } else if (taskType == WFEConstants.WFTASK_TYPE_START) {
		     // 处理开始任务类型
		     if (tempList != null && tempList.size() > 0) {
			 for (int j = 0; j < tempList.size(); j++) {
			     retList.add(tempList.get(j));
			 }
		     }
		 }
		    }
		}
	    }
	}
	return retList;
    }

    /**
     * 根据业务ID、业务表名和流程模板名获取任务的履历信息
     * 
     * @param workflow 包含业务必要信息的Map对象，用于查询流程历史记录
     * @return 返回一个包含流程历史信息的List<Map<String, Object>>集合，每个Map代表一个历史记录
     * @throws Exception 如果查询过程中发生错误，则抛出异常
     */
    public List<Map<String, Object>> getWfHistoryView(Map<String, Object> workflow) throws Exception {
	List<Map<String, Object>> reList = null;
	// 检查传入的workflow对象是否非空，且包含有效的业务类型和ID
	if (workflow != null && BpmDo.docType(workflow) != null && BpmDo.bizId(workflow) > 0) {
	    try {
		// 如果workflow非空，查询流程的历史记录和当前任务列表
		if (null != workflow) {
		    List<Map<String, Object>> hs = BpmInstance.getWfHistoryList(workflow);
		    List<Map<String, Object>> tasks = BpmInstance.getAllWfTaskList(workflow);
		    // 如果查询到历史记录和任务列表，将它们合并处理
		    if (null != hs && null != tasks) {
			reList = new ArrayList<>();
			// 遍历历史记录，为每个历史记录合并相关任务信息
			for (int i = 0; i < hs.size(); i++) {
			    Map<String, Object> hi = hs.get(i);
			    Map<String, Object> hView = new HashMap<>();
			    hView.putAll(hi);
			    // 在历史记录中查找与当前历史记录相关联的任务信息，并合并
			    for (int k = 0; k < tasks.size(); k++) {
				Map<String, Object> task = tasks.get(k);
				if (MapTool.longValue(task,"id") == MapTool.longValue(hi,"wfTaskId")) {
				    MapTool.copyValues(task,hView, "taskDescript,taskName,taskStatus,taskType");
				}
			    }
			    reList.add(hView);
			}
		    }
		}
	    } catch (Exception e) {
		LoggerTool.error(logger,"处理流程履历信息失败！");
		throw new Exception("处理流程履历信息失败！");
	    }
	}
	return reList;
    }


    /**
     * Description：生成特定节点在一定时间后才能看到数据的SQL <br/>
     * Date：2015-4-9 下午01:25:12 <br/>
     * Author：xbdai <br/>
     * 
     * @param nodeids      节点innerid
     * @param milliseconds 时间，以毫秒为单位
     * @return
     */
    public String getNodesReceiveAfterTime(String[] nodeids, long[] milliseconds) {
	StringBuffer sql = new StringBuffer();
	StringBuffer last = new StringBuffer();
	long now = Calendar.getInstance().getTimeInMillis();
	for (int i = 0; i < nodeids.length; i++) {
	    if (StringUtils.isBlank(nodeids[i])) {
		continue;
	    }

	    if (sql.length() == 0) {
		sql.append(" AND ( ");
		last.append(" OR ");
	    } else {
		sql.append(" OR ");
		last.append(" AND ");
	    }

	    sql.append(" WFEOBJ.NowInnerTaskID = " + nodeids[i] + " AND ( WFEOBJ.TASKCOMEDATETIME + "
		    + (milliseconds.length > i ? milliseconds[i] : 0) + ") < " + now);
	    last.append(" WFEOBJ.NowInnerTaskID != " + nodeids[i]);
	}
	sql.append(last);

	if (sql.indexOf("(") != -1) {
	    sql.append(" ) ");
	}
	return sql.toString();
    }

}

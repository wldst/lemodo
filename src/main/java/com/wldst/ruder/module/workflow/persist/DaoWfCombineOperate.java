package com.wldst.ruder.module.workflow.persist;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.beans.Decision;
import com.wldst.ruder.module.workflow.beans.TaskExtendInfo;
import com.wldst.ruder.module.workflow.beans.SimpleTask;
import com.wldst.ruder.module.workflow.beans.BpmTask;
import com.wldst.ruder.module.workflow.beans.TaskProperties;
import com.wldst.ruder.module.workflow.beans.BpmTaskExecute;
import com.wldst.ruder.module.workflow.beans.TaskRelation;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;

/**
 * 流程信息综合数据库操作
 * 
 * @author wldst
 */
@Component
public class DaoWfCombineOperate {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(DaoWfCombineOperate.class);
    @Autowired
    private CrudNeo4jService crudService;
    @Autowired
    private BpmInstance bpmi;
    /**
     * 获取流程任务待办信息列表
     * 
     * @param dba       数据库操作对象
     * @param userID    指定用户ID
     * @param tempMarks 流程模板唯一标识串
     * @return 待办流程任务信息列表
     * @throws DatabaseException
     */
    public List<Map<String,Object>> getWfHandleTaskList(long userID, String tempMarks) {
	List<Map<String,Object>> retList = null;
	ResultSet rs = null;

	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("SELECT E.executor_name EmpName,E.executor_id EmpID,A.ID WfID,");
	sqlBuffer.append(" H.BizDataID,C.InnerTaskID,");
	sqlBuffer.append(" A.WorkflowName,C.TaskName,");
	sqlBuffer.append(" B.ExecutorID,D.TemplateMark,");
	sqlBuffer.append(" A.WfCreateDatetime,");
	sqlBuffer.append(" G.Property_5 BizURI,");
	sqlBuffer.append(" G.Property_10 BizType");

	sqlBuffer.append(" FROM S_WFE_WfInstanceBaseInfo A,");
	sqlBuffer.append(" S_WFE_WfTaskExecutor B,");
	sqlBuffer.append(" S_WFE_WfTaskBaseInfo C,");
	sqlBuffer.append(" S_WFE_WorkFlowTemplate D,");
	sqlBuffer.append(" s_wfe_executor E,");
	sqlBuffer.append(" S_WFE_WfTaskProperties G,");
	sqlBuffer.append(" S_WFE_BizWfInsMapping H");
	sqlBuffer.append(" WHERE 1=1");

	sqlBuffer.append(" AND A.NowTaskIDs=B.WfTaskID");
	sqlBuffer.append(" AND A.WfStatus = ?");
	sqlBuffer.append(" AND B.ExecutorID = ?");
	sqlBuffer.append(" AND B.WfTaskID=C.ID");
	sqlBuffer.append(" AND D.executor_id = A.WfTemplateID");
	sqlBuffer.append(" AND B.ExecutorStatus = ?");
	sqlBuffer.append(" AND (C.TaskStatus = ?");
	sqlBuffer.append(" OR C.TaskStatus=?)");
	sqlBuffer.append(" AND E.executor_id=A.WfCreateEmpID");
	// sqlBuffer.append(" AND E.Status = ?");
	sqlBuffer.append(" AND C.ID=G.WfTaskID");
	sqlBuffer.append(" AND A.ID=H.WfInstanceID");

	if (tempMarks != null && tempMarks.trim().length() > 0) {
	    sqlBuffer.append(" AND D.TemplateMark IN (?)");
	}
	List<Object> params = new ArrayList<Object>();
	params.add(WFEConstants.WFSTATUS_RUN);
	params.add(userID);
	params.add(WFEConstants.WF_EXEC_USERSTATE_NONE);
	params.add(WFEConstants.WFTASK_STATUS_READY);
	params.add(WFEConstants.WFTASK_STATUS_WAIT);
	// params.add(WFEConstants.DB_BOOLEAN_TRUE);
	if (tempMarks != null && tempMarks.trim().length() > 0) {
	    params.add(tempMarks);
	}

	return retList;
    }

    /**
     * 获取流程实例ID
     * 
     * @param dba        数据库操作对象
     * @param bizDataID  业务数据ID
     * @param bizTabName 业务数据表名
     * @return 流程实例ID @
     */
    public long getWorkflowInstanceID(long bizDataID, String bizTabName) {
	long retLong = 0;
	ResultSet rs = null;

	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("SELECT WfInstanceID");
	sqlBuffer.append(" FROM S_WFE_BizWfInsMapping");
	sqlBuffer.append(" WHERE BizDataID=?");
	sqlBuffer.append(" AND BizTableName=?");
	
	String querywfId="match(n:"+bizTabName+")-[]->() where id(n)="+bizDataID +" return n.wfInstanceID ";
	
	List<Map<String,Object>> dataList = crudService.cypher(querywfId);
	if (dataList != null && dataList.size() > 0) {
	    Map dataMap = (Map) dataList.get(0);
	    retLong = NumberUtil.parseLong((String) dataMap.get("wfInstanceID"), 0);
	}
	return retLong;
    }

    /**
     * 从数据库中获取流程实例对象
     *
     * @param wfInstanceID 流程实例ID
     * @return 流程实例对象
     * @throws Exception
     */
    public Map<String,Object> getBpmiById(long wfInstanceID) {
	Map<String, Object> nodeMapById = crudService.getNodeMapById(wfInstanceID);
	return nodeMapById;
    }

}

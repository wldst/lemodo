package com.wldst.ruder.module.workflow.persist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.neo4j.driver.exceptions.DatabaseException;

import com.thoughtworks.xstream.core.ReferenceByIdMarshaller.IDGenerator;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;


/**
 * 流程实例基本信息之数据库操作<br>
 * 只提供对流程实例的数据库操作
 * 
* @author wldst
 */
public class DaoWfInstanceBaseInfo {
	// 日志对象
	private static Logger logger = LoggerFactory.getLogger(DaoWfInstanceBaseInfo.class);

	/**
	 * 检索流程实例对象列表
	 * 
	 * @param dba
	 *            数据库操作对象
	 * @param wfTempMark
	 *            流程模板唯一标识
	 * @return 流程实例列表
	 * @throws DatabaseException
	 */
	public void srchWfInstanceList(String wfTempMark, long srchBizDataID,
			int wfStatus, long createEmpID) {
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("SELECT B.ID,A.TemplateMark,B.WorkflowName,");
		sqlBuffer.append(" B.WfStatus,C.TaskName,D.executor_name EmpName,");
		sqlBuffer.append(" B.WfCreateDatetime,B.TriggerSubWfFlag,");
		sqlBuffer.append(" E.BizTableName,E.BizDataID");
		sqlBuffer.append(" FROM S_WFE_WorkFlowTemplate A LEFT JOIN ");
		sqlBuffer.append(" S_WFE_WfInstanceBaseInfo B ON A.ID=B.WfTemplateID");
		sqlBuffer.append(" LEFT JOIN S_WFE_WfTaskBaseInfo C ON B.NowTaskIDs=C.ID,");
		sqlBuffer.append(" S_WFE_EXECUTOR D,");
		sqlBuffer.append(" S_WFE_BizWfInsMapping E");
		sqlBuffer.append(" WHERE A.TemplateMark='" + wfTempMark + "'");
		sqlBuffer.append(" AND B.WfCreateEmpID=D.EXECUTOR_ID");
		sqlBuffer.append(" AND B.ID IS NOT NULL");
		sqlBuffer.append(" AND B.ID = E.WfInstanceID");
		if (srchBizDataID > 0) {
			sqlBuffer.append(" AND E.BizDataID=" + srchBizDataID);
		}
		if (wfStatus > 0) {
			sqlBuffer.append(" AND B.WfStatus=" + wfStatus);
		}
		if (createEmpID > 0) {
			sqlBuffer.append(" AND B.WfCreateEmpID=" + createEmpID);
		}

//		retList = this.pagedForSql(sqlBuffer.toString(), null);
//		return retList;
	}


	

	/**
	 * 删除指定的流程实例信息
	 * 
	 * @param dba
	 *            数据库操作对象
	 * @param wfInsID
	 *            流程实例ID
	 * @return 删除操作执行结果 @
	 */
	public boolean delete(long wfInsID) {
		if (wfInsID <= 0) {
			throw new CrudBaseException("流程实例数据不正确,主键为空,不能进行相关操作");
		}
		boolean retFlag = false;

		StringBuffer sqlBuffer2 = new StringBuffer();
		sqlBuffer2.append("DELETE FROM S_WFE_BizWfInsMapping");
		sqlBuffer2.append(" WHERE WfInstanceID=?");
		Object[] obj2 = { wfInsID };
//		this.updateWithSql(sqlBuffer2.toString(), obj2);

		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("DELETE FROM S_WFE_WfInstanceBaseInfo");
		sqlBuffer.append(" WHERE ID=?");

		Object[] obj = { wfInsID };
//		this.updateWithSql(sqlBuffer.toString(), obj);

		retFlag = true;
		return retFlag;
	}
}

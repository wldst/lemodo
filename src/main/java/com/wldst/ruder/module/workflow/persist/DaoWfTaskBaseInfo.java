
package com.wldst.ruder.module.workflow.persist;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.neo4j.driver.exceptions.DatabaseException;

import com.thoughtworks.xstream.core.ReferenceByIdMarshaller.IDGenerator;
import com.wldst.ruder.module.workflow.beans.BpmTask;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;

/**
 * 流程任务基本信息数据库操作
 * 
* @author wldst
 * 
 */
public class DaoWfTaskBaseInfo{
	// 日志对象
	private static Logger logger = LoggerFactory.getLogger(DaoWfTaskBaseInfo.class);


	/**
	 * 删除流程实例的所有任务信息
	 * 
	 * @param dba
	 *            数据库操作对象
	 * @param wfInstanceID
	 *            流程实例主键
	 * @return 删除操作执行结果 @
	 */
	public boolean deleteWfAll(long wfInstanceID) {
		if (wfInstanceID <= 0) {
			throw new CrudBaseException("流程实例数据不正确,不能进行相关操作");
		}
		boolean retFlag = false;
		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append("DELETE FROM S_WFE_WfTaskBaseInfo");
		sqlBuffer.append(" WHERE WfInstanceID=?");

		Object[] obj = { wfInstanceID };

//		this.updateWithSql(sqlBuffer.toString(), obj);
		retFlag = true;
		return retFlag;
	}
}

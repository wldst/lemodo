package com.wldst.ruder.module.workflow.persist;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;

/**
 * @author wldst
 * 
 */
public class DefaultWFTaskExecutorQuery {

    public List<Map<String,Object>> queryXmlWFTaskExecutorPageList(long[] executorIdArray) throws CrudBaseException {
	List<Map<String,Object>> retList = null;
	if (executorIdArray == null || executorIdArray.length <= 0) {
	    return null;
	}
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("SELECT EXECUTOR_ID ID,EXECUTOR_NAME EmpName ");
	sqlBuffer.append(" FROM S_WFE_EXECUTOR ");
	sqlBuffer.append(" WHERE EXECUTOR_ID IN ");
	return retList;
    }

    public List<Map<String,Object>> queryXmlWFTaskExecutorList(long[] executorIdArray) throws CrudBaseException {
	if (executorIdArray == null || executorIdArray.length <= 0) {
	    return null;
	}
	List<Map<String,Object>> retList = null;
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("SELECT EXECUTOR_ID ID,EXECUTOR_NAME EmpName ");
	sqlBuffer.append(" FROM S_WFE_EXECUTOR ");
	sqlBuffer.append(" WHERE EXECUTOR_ID IN ");

	return retList;
    }

}

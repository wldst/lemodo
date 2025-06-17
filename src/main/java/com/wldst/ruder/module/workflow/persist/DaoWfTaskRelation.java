package com.wldst.ruder.module.workflow.persist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.neo4j.driver.exceptions.DatabaseException;

import com.thoughtworks.xstream.core.ReferenceByIdMarshaller.IDGenerator;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.workflow.beans.SimpleTask;
import com.wldst.ruder.module.workflow.beans.TaskRelation;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.util.NumberUtil;

/**
 * 流程任务之间的关系数据库操作
 * 
 * @author wldst
 */
@Component
public class DaoWfTaskRelation {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(DaoWfTaskRelation.class);

    @Autowired
    private CrudNeo4jService crudService;

    /**
     * 删除流程之任务关系数据
     * 
     * @param dba          数据库操作对象
     * @param wfInstanceID 流程实例ID
     * @return 删除操作执行结果 @
     */
    public boolean deleteWfAll(long wfInstanceID) {
	if (wfInstanceID <= 0) {
	    throw new CrudBaseException("主键不正确,不能进行相关操作");
	}
	boolean retFlag = false;
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("DELETE FROM S_WFE_WfTaskRelation");
	sqlBuffer.append(" WHERE WfInstanceID=?");

	Object[] obj = { wfInstanceID };

	// this.updateWithSql(sqlBuffer.toString(), obj);
	retFlag = true;
	return retFlag;
    }

    public List<Map<String, Object>> findTaskRelationList(long wfInstanceId, int preOrNext, int realFlow) {
	if (0 >= wfInstanceId) {
	    throw new CrudBaseException("未指定流程实例ID参数");
	}
	List<Map<String, Object>> retList = new ArrayList<>();
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append(
		"SELECT distinct b.* FROM S_WFE_WFTASKRELATION a LEFT JOIN S_WFE_WFTASKBASEINFO b ON a.wftaskid = b.ID ");
	sqlBuffer.append(" WHERE b.TASKTYPE = 3 AND b.TASKSTATUS = 4 AND a.WFINSTANCEID =" + wfInstanceId);
	if (1 == preOrNext || 2 == preOrNext) {
	    sqlBuffer.append(" AND a.PRENEXTTYPE =" + preOrNext);
	}
	// if(0 == realFlow || 1 == realFlow){
	// sqlBuffer.append(" AND a.REALFLOWROADFLAG ="+realFlow);
	// }
	sqlBuffer.append(" ORDER BY b.id ");

	List<Map<String, Object>> qryList =  crudService.cypher(sqlBuffer.toString());
	// this.queryForListWithSql(sqlBuffer.toString());
	if (qryList != null && qryList.size() > 0) {
	    for (Map<String, Object> task: qryList) {
		 retList.add(task);
	    }
	}

	return retList;
    }
}

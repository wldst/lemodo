package com.wldst.ruder.crud.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.auth.service.UserAdminService;

@Service
public class CrudCypherService extends RuleConstants {

    final static Logger debugLog = LoggerFactory.getLogger("debugLogger");
    final static Logger logger = LoggerFactory.getLogger(CrudCypherService.class);
    final static Logger resultLog = LoggerFactory.getLogger("reportsLogger");

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUserNeo4jService cun;

    @Autowired
    private UserAdminService adminService;

    /**
     * 构造函数，用于初始化 CrudCypherService。
     */
    public CrudCypherService() {
    }

    /**
     * 获取管理员服务。
     * @return 返回管理员服务实例。
     */
    public UserAdminService getAdminService() {
	return adminService;
    }

    /**
     * 设置管理员服务。
     * @param adminService 管理员服务实例。
     */
    public void setAdminService(UserAdminService adminService) {
	this.adminService = adminService;
    }

    /**
     * 添加关系，结束节点通过逗号分隔。
     * @param rel 关系类型。
     * @param startId 起始节点ID。
     * @param endId 结束节点ID，多个ID通过逗号分隔。
     */
    @ServiceLog(description="添加关系，结束节点，逗号分隔")
    public void addRel(String rel, String startId, String endId) {
	// 参数校验
	if (startId == null || endId == null || rel == null || "".equals(rel.trim()) || "".equals(startId.trim())
		|| "".equals(endId.trim())) {
	    return;
	}
	// 对结束节点ID进行分割，然后为每个节点添加关系
	for (String ei : endId.split(",")) {
	    String createRelation = Neo4jOptCypher.createRelation(rel, startId, ei);
	    neo4jService.execute(createRelation);
	}
    }

    /**
     * 给特定起点和终点添加关系，添加之前先清理开始节点的特定关系。
     * @param rel 关系类型。
     * @param relName 关系名称。
     * @param startId 起始节点ID。
     * @param endId 结束节点ID。
     */
    @ServiceLog(description="给特定起点和终点添加关系，添加之前先清理开始节点的特定关系")
    public void addRel(String rel,String relName, String startId, String endId) {
	// 参数校验
	if (startId == null || endId == null || rel == null || "".equals(rel.trim()) || "".equals(startId.trim())
		|| "".equals(endId.trim())) {
	    return;
	}
	for (String ei : endId.split(",")) {
	    String createRelation = Neo4jOptCypher.createRelation(rel,relName, startId, ei);
	    neo4jService.execute(createRelation);
	}
    }
    /**
     * 给特定起点和终点添加关系，添加之前先清理开始节点的特定关系。
     * @param rel 关系类型。
     * @param relName 关系名称。
     * @param startId 起始节点ID。
     * @param endIds 结束节点ID列表。
     */
    @ServiceLog(description="给特定起点和终点添加关系，添加之前先清理开始节点的特定关系")
    public void addRel(String rel,String relName, String startId, List<Long> endIds) {
	// 参数校验
	if (startId == null || endIds == null || rel == null || "".equals(rel.trim()) || "".equals(startId.trim())
		|| endIds.size()<1) {
	    return;
	}
	// 先删除起始节点与特定关系的旧连接
	String delRelation = Neo4jOptCypher.delRel(rel, Long.valueOf(startId),null);
	neo4jService.execute(delRelation);
	// 为每个结束节点添加新关系
	for (Long ei: endIds) {
	    String createRelation = Neo4jOptCypher.createRelation(rel,relName, Long.valueOf(startId), ei);
	    neo4jService.execute(createRelation);
	}
    }
    /**
     * 添加关系。
     * @param rel 关系类型。
     * @param startId 起始节点ID。
     * @param endId 结束节点ID。
     */
    @ServiceLog(description="添加关系")
    public void addRel(String rel, Long startId, Long endId) {
	// 参数校验
	if (startId == null || endId == null || rel == null) {
	    return;
	}
	// 检查关系是否已存在
	String existRelation = Neo4jOptCypher.existRelation(rel, startId, endId);
	String relExist = neo4jService.getOne(existRelation,"relExist");
	boolean notExist = relExist==null||!Boolean.valueOf(relExist);
	if(notExist) {
	    String createRelation = Neo4jOptCypher.createRelation(rel, startId, endId);
	    neo4jService.execute(createRelation);
	}

    }
    /**
     * 带参数Map的添加关系。
     * @param rel 关系类型。
     * @param startId 起始节点ID。
     * @param endId 结束节点ID。
     * @param param 关系属性参数。
     */
    @ServiceLog(description="带参数Map的添加关系")
    public void addRel(String rel, Long startId, Long endId, Map<String, Object> param) {
	// 参数校验
	if (startId == null || endId == null || rel == null) {
	    return;
	}

	String existRelation = Neo4jOptCypher.existRelation(rel, startId, endId,param);
	String relExist = neo4jService.getOne(existRelation,"relExist");
	boolean notExist = relExist==null||!Boolean.valueOf(relExist);
	if(notExist) {
	    // 检查关系是否已存在
	    String existr = Neo4jOptCypher.existRelation(rel, startId, endId);
	    String hasRel = neo4jService.getOne(existr,"relExist");
	    boolean hasRelTrue = hasRel==null||!Boolean.valueOf(hasRel);
	    if(!hasRelTrue) {// 如果存在旧关系，则先删除
		String removeRelation = Neo4jOptCypher.delRel(rel, startId, endId);
		neo4jService.execute(removeRelation);
	    }
	    // 设置关系属性中缺失的名称
	    if(name(param)==null) {
		param.put(NAME, name(cun.getMetaDataById(endId))) ;
	    }
	    String createRelation = Neo4jOptCypher.createRelation(rel, startId, endId,param);
	    neo4jService.execute(createRelation);
	}

    }
    /**
     * 给当前账号和特定节点添加关系。
     * @param rel 关系类型。
     * @param endId 结束节点ID。
     */
    @ServiceLog(description="给当前账号和特定节点添加关系")
    public void addRel(String rel, String endId) {
	// 参数校验
	if (endId == null || rel == null || "".equals(rel.trim()) || "".equals(endId.trim())) {
	    return;
	}
	// 获取当前用户ID
	Long currentUserId = adminService.getCurrentPasswordId();
	String createRelation = Neo4jOptCypher.createRelation(rel, currentUserId, endId);
	neo4jService.execute(createRelation);
    }
    /**
     * 给当前账号添加关系，带关系属性。
     * @param rel 关系类型。
     * @param param 关系属性参数。
     */
    @ServiceLog(description="给当前账号添加关系，带关系属性")
    public void addCurentActionRel(String rel, Map<String, Object> param) {
	// 获取结束节点ID
	Long endId = longValue(param,"endId");
	if (endId == null || rel == null || "".equals(rel.trim())) {
	    return;
	}
	param.remove("endId");
	param.remove(LABEL);
	param.put("createTime", Calendar.getInstance().getTimeInMillis());
	// 获取当前用户ID
	Long currentUserId = adminService.getCurrentPasswordId();
	String endLabel = neo4jService.getNodeLabelByNodeId(endId);
	String relNum = rel+"Count";
	String isExist = "match (a:Password)-[r:"+rel+"]->(b:"+endLabel+") WITH  count(r) as "+relNum+" RETURN "+relNum;
	List<Map<String, Object>> query = neo4jService.queryByCypher(isExist);
	Object object = query.get(0).get(relNum);
//	crudNeo4jSevice.query("match (a:Password)-[r:"+rel+"]->(b:"+endLabel+") remove r");
//	Long dataId = longValue(param,"dataId");
//	if(dataId!=null) {
	    String createRelation = Neo4jOptCypher.createRelation(rel, currentUserId, endId,
		    param);
	    neo4jService.execute(createRelation);
	    Map<String,Object> update = new HashMap<>();
	    update.put(relNum, object);
	    neo4jService.saveById(endId,relNum,object);
//	}
    }

}

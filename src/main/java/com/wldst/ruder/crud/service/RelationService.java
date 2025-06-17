package com.wldst.ruder.crud.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.module.auth.service.UserAdminService;

@Service
public class RelationService extends RuleConstants {

    final static Logger debugLog = LoggerFactory.getLogger("debugLogger");
    final static Logger logger = LoggerFactory.getLogger(RelationService.class);
    final static Logger resultLog = LoggerFactory.getLogger("reportsLogger");

    private CrudNeo4jService neo4jService;

    private CrudUserNeo4jService cun;

    private UserAdminService adminService;

    /**
     * Neo4JDriver creates and inserts the query to Neo4j instance
     */
    @Autowired
    public RelationService( @Lazy CrudNeo4jService neo4jService, @Lazy CrudUserNeo4jService cun,  @Lazy UserAdminService adminService) {
        this.neo4jService=neo4jService;
        this.cun=cun;
        this.adminService=adminService;
    }

    public UserAdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(UserAdminService adminService) {
        this.adminService = adminService;
    }

    @ServiceLog(description = "添加关系，结束节点，逗号分隔")
    public void addRel(String rel, String startId, String endId) {
        if (startId == null || endId == null || rel == null || "".equals(rel.trim()) || "".equals(startId.trim())
                || "".equals(endId.trim())) {
            return;
        }
        for (String ei : endId.split(",")) {
            String createRelation = Neo4jOptCypher.createRelation(rel, startId, ei);
            neo4jService.execute(createRelation);
        }
    }

    /**
     * 判断关系的方向
     *
     * @param relation
     * @param startLabel
     * @param endLabel
     * @return
     */
    public Boolean isDifferentDirctionRel(String relation, String startLabel, String endLabel) {
        String cypher = "Match(n:RelationDefine{reLabel:\"" + relation + "\"}) where n.startLabel  IN ['" + startLabel + "', '" + endLabel + "'] OR n.endLabel  IN ['" + startLabel + "', '" + endLabel + "'] return n";
        List<Map<String, Object>> relDefine = neo4jService.cypher(cypher);
        boolean reverse = false;
        if (relDefine != null && !relDefine.isEmpty()) {
            for (Map<String, Object> reli : relDefine) {
                if (string(reli, "startLabel").equals(endLabel)
                        && string(reli, "endLabel").equals(startLabel)) {
                    reverse = true;
                }
            }
        }
        return reverse;
    }


    @ServiceLog(description = "给特定起点和终点添加关系，添加之前先清理开始节点的特定关系")
    public void addRel(String rel, String relName, String startId, String endId) {
        if (startId == null || endId == null || rel == null || "".equals(rel.trim()) || "".equals(startId.trim())
                || "".equals(endId.trim())) {
            return;
        }
        for (String ei : endId.split(",")) {
            String createRelation = Neo4jOptCypher.createRelation(rel, relName, startId, ei);
            neo4jService.execute(createRelation);
        }
    }

    @ServiceLog(description = "给特定起点和终点添加关系，添加之前先清理开始节点的特定关系")
    public void addRel(String rel, String relName, Long startId, Long endId) {
        if (startId == null || endId == null || rel == null || "".equals(rel.trim())) {
            return;
        }
        String createRelation = Neo4jOptCypher.createRelation(rel, relName, startId, endId);
        neo4jService.execute(createRelation);
    }

    @ServiceLog(description = "给特定起点和终点添加关系，添加之前先清理开始节点的特定关系")
    public void addRel(String rel, String relName, String startId, List<Long> endIds) {
        if (startId == null || endIds == null || rel == null || "".equals(rel.trim()) || "".equals(startId.trim())
                || endIds.size() < 1) {
            return;
        }
        String delRelation = Neo4jOptCypher.delRel(rel, Long.valueOf(startId), null);
        neo4jService.execute(delRelation);
        for (Long ei : endIds) {
            String createRelation = Neo4jOptCypher.createRelation(rel, relName, Long.valueOf(startId), ei);
            neo4jService.execute(createRelation);
        }
    }

    @ServiceLog(description = "给特定起点和终点添加关系，添加之前先清理开始节点的特定关系")
    public void addRel(String rel, String relName, Long startId, List<Long> endIds) {
        if (startId == null || endIds == null || rel == null || "".equals(rel.trim())
                || endIds.size() < 1) {
            return;
        }
        String delRelation = Neo4jOptCypher.delRel(rel, startId, null);
        neo4jService.execute(delRelation);
        for (Long ei : endIds) {
            String createRelation = Neo4jOptCypher.createRelation(rel, relName, startId, ei);
            neo4jService.execute(createRelation);
        }
    }

    @ServiceLog(description = "添加关系")
    public void addRel(String rel, Long startId, Long endId) {
        if (startId == null || endId == null || rel == null) {
            return;
        }
        String existRelation = Neo4jOptCypher.existRelation(rel, startId, endId);
        String relExist = neo4jService.getOne(existRelation, "relExist");
        boolean notExist = relExist == null || !Boolean.valueOf(relExist);
        if (notExist) {
            String createRelation = Neo4jOptCypher.createRelation(rel, startId, endId);
            neo4jService.execute(createRelation);
        }

    }

    public Map<String, Object> addRel2(String rel, Long startId, Long endId) {
        if (startId == null || endId == null || rel == null) {
            return null;
        }
        String existRelation = Neo4jOptCypher.existRelation(rel, startId, endId);
        String relExist = neo4jService.getOne(existRelation, "relExist");
        boolean notExist = relExist == null || !Boolean.valueOf(relExist);
        if (notExist) {
            String createRelation = Neo4jOptCypher.createRelation(rel, startId, endId);
            neo4jService.execute(createRelation);
        }
        String queryRel = Neo4jOptCypher.queryRel(rel, startId, endId);
        return neo4jService.getOne(queryRel);
    }

    public void addRels(String rel, Long startId, List<Long> endId, Map<String, Object> param) {
        for (Long ei : endId) {
            addRel(rel, startId, ei, param);
        }
    }

    public void addRels(String rel, List<Long> startIds, Long endId, Map<String, Object> param) {
        for (Long ei : startIds) {
            addRel(rel, ei, endId, param);
        }
    }

    @ServiceLog(description = "带参数Map的添加关系")
    public void addRel(String rel, Long startId, Long endId, Map<String, Object> param) {
        if (startId == null || endId == null || rel == null) {
            return;
        }

        String existRelation = Neo4jOptCypher.existRelation(rel, startId, endId, param);
        String relExist = neo4jService.getOne(existRelation, "relExist");
        boolean notExist = relExist == null || !Boolean.valueOf(relExist);
        if (notExist) {
            String existr = Neo4jOptCypher.existRelation(rel, startId, endId);
            String hasRel = neo4jService.getOne(existr, "relExist");
            boolean hasRelTrue = hasRel == null || !Boolean.valueOf(hasRel);
            if (!hasRelTrue) {//存在先删除关系数据
                String removeRelation = Neo4jOptCypher.delRel(rel, startId, endId);
                neo4jService.execute(removeRelation);
            }
            if (name(param) == null) {
                param.put(NAME, name(cun.getMetaDataById(endId)));
            }
            String createRelation = Neo4jOptCypher.createRelation(rel, startId, endId, param);
            neo4jService.execute(createRelation);
        }

    }

    public Map<String, Object> addRel2(String rel, Long startId, Long endId, Map<String, Object> param) {
        if (startId == null || endId == null || rel == null) {
            return null;
        }

        String existRelation = Neo4jOptCypher.existRelation(rel, startId, endId, param);
        String relExist = neo4jService.getOne(existRelation, "relExist");
        boolean notExist = relExist == null || !Boolean.valueOf(relExist);
        if (notExist) {
            String existr = Neo4jOptCypher.existRelation(rel, startId, endId);
            String hasRel = neo4jService.getOne(existr, "relExist");
            boolean hasRelTrue = hasRel == null || !Boolean.valueOf(hasRel);
            if (!hasRelTrue) {//存在先删除关系数据
                String removeRelation = Neo4jOptCypher.delRel(rel, startId, endId);
                neo4jService.execute(removeRelation);
            }
            if (name(param) == null) {
                param.put(NAME, name(cun.getMetaDataById(endId)));
            }
            String createRelation = Neo4jOptCypher.createRelation(rel, startId, endId, param);
            neo4jService.execute(createRelation);
        }
        String queryRel = Neo4jOptCypher.queryRel(rel, startId, endId, param);
        return neo4jService.getOne(queryRel);
    }

    @ServiceLog(description = "给当前账号和特定节点添加关系")
    public void addRel(String rel, String endId) {
        if (endId == null || rel == null || "".equals(rel.trim()) || "".equals(endId.trim())) {
            return;
        }
        Long currentUserId = adminService.getCurrentPasswordId();
        String createRelation = Neo4jOptCypher.createRelation(rel, currentUserId, endId);
        neo4jService.execute(createRelation);
    }

    @ServiceLog(description = "给当前账号添加关系，带关系属性")
    public void addCurentActionRel(String rel, Map<String, Object> param) {
        Long endId = longValue(param, "endId");
        if (endId == null || rel == null || "".equals(rel.trim())) {
            return;
        }
        param.remove("endId");
        param.remove(LABEL);
        param.put("createTime", Calendar.getInstance().getTimeInMillis());
        Long currentUserId = adminService.getCurrentPasswordId();
        String endLabel = neo4jService.getNodeLabelByNodeId(endId);
        String relNum = rel + "Count";
        String isExist = "match (a:Password)-[r:" + rel + "]->(b:" + endLabel + ") WITH  count(r) as " + relNum + " RETURN " + relNum;
        List<Map<String, Object>> query = neo4jService.queryByCypher(isExist);
        Object object = query.get(0).get(relNum);
//	    crudNeo4jSevice.cypher("match (a:Password)-[r:"+rel+"]->(b:"+endLabel+") remove r");
//	    Long dataId = longValue(param,"dataId");
//	    if(dataId!=null) {
        String createRelation = Neo4jOptCypher.createRelation(rel, currentUserId, endId,
                param);
        neo4jService.execute(createRelation);
        Map<String, Object> update = new HashMap<>();
        update.put(relNum, object);
        neo4jService.saveById(endId, relNum, object);
//	    }

    }

    public void validRelate(String relation, Long startId, Long endId, Map<String, Object> props) {
        String mapString = mapString(props);
        String cypher = "MATCH (s)-[r:" + relation
                + "{" + mapString + "}]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " return r";
        List<Map<String, Object>> query = neo4jService.cypher(cypher);
        if (query != null && query.size() > 0) {
            if (query.size() > 1) {
                delRelation(relation, startId, endId, mapString);
                addRel(relation, startId, endId, props);
            }
        } else {
            addRel(relation, startId, endId, props);
        }

    }

    public void delRelation(String relation, Long startId, Long endId, Map<String, Object> props) {
        String mapString = mapString(props);
        String delCypher = "MATCH (s)-[r:" + relation
                + "{" + mapString + "}]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " DELETE r";
        neo4jService.execute(delCypher);
    }

    public void del2Relation(String relation, Long startId, Long endId, Map<String, Object> props) {
        String mapString = mapString(props);
        String delCypher = "MATCH (s)-[r:" + relation
                + "{" + mapString + "}]-(e)  where id(s)=" + startId + " and id(e)=" + endId + " DELETE r";
        neo4jService.execute(delCypher);
    }

    public void del2Relation(String relation, Long startId, Long endId) {
        String delCypher = "MATCH (s)-[r:" + relation
                + "]-(e)  where id(s)=" + startId + " and id(e)=" + endId + " DELETE r";
        neo4jService.execute(delCypher);
    }

    public void delRelation(Map<String, Object> param) {
        Long startId = longValue(param, "startId");
        Long endId = longValue(param, "endId");
        String relation = string(param, "relation");
        String startLabel = string(param, "startLabel");
        String endLabel = string(param, "endLabel");
        Boolean reverse = bool(param, "reverse");
        StringBuilder sb = new StringBuilder();
        sb.append("MATCH (s");
        if (reverse) {
            if (endLabel != null) {
                sb.append(":").append(endLabel);
            }
            if (startLabel != null) {
                sb.append(")<-[r:" + relation + "]-(e:").append(startLabel).append(")");
            } else {
                sb.append(")<-[r:" + relation + "]-(e)");
            }
            sb.append(" where id(e)=").append(startId).append(" and id(s)=").append(endId).append(" DELETE r");
        } else {
            if (startLabel != null) {
                sb.append(":").append(startLabel);
            }
            if (endLabel != null) {
                sb.append(")-[r:" + relation + "]->(e:").append(endLabel).append(")");
            } else {
                sb.append(")-[r:" + relation + "]->(e)");
            }
            sb.append(" where id(s)=").append(startId).append(" and id(e)=").append(endId).append(" DELETE r");
        }
        neo4jService.execute(sb.toString());
    }

    public void delRelation(String relation, Long startId, Long endId) {
        String delCypher = "MATCH (s)-[r:" + relation
                + "]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " DELETE r";
        neo4jService.execute(delCypher);
    }

    public void delRelation(String relation, Long startId, Long endId, String mapString) {
        String delCypher = "MATCH (s)-[r:" + relation
                + "{" + mapString + "}]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " DELETE  r";
        neo4jService.execute(delCypher);
    }

    public void del2Relation(String relation, Long startId, Long endId, String mapString) {
        String delCypher = "MATCH (s)-[r:" + relation
                + "{" + mapString + "}]-(e)  where id(s)=" + startId + " and id(e)=" + endId + " DELETE r";
        neo4jService.execute(delCypher);
    }
    

    public List<Map<String, Object>> relationQuery(String label, Map<String,Object> vo) {
         
        List<Map<String, Object>> relationAndEndNodeDataList = null;
        try {
            // 尝试获取外出关系数据
            relationAndEndNodeDataList = neo4jService.getOutRelations(vo, label);
            
        } catch (NumberFormatException e) {
            // 记录异常信息
            LoggerTool.error(logger,e.getMessage(), e);
        }
        return relationAndEndNodeDataList;
    }
   
}

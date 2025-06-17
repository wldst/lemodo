package com.wldst.ruder.module.state.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.NodeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.SystemDomain;

import static com.wldst.ruder.domain.StatusDomain.STATE_STEP;

@Service
public class StateServiceImpl extends NodeTool implements StateService {


    private CrudNeo4jService neo4jService;

    private RelationService relationService;

    public StateServiceImpl(CrudNeo4jService neo4jService, RelationService relationService) {
        this.neo4jService = neo4jService;
        this.relationService = relationService;
    }

    @Override
    public void setStatus(Long nodeId, Long statusId) {
        relationService.addRel(STATUS, nodeId, statusId);
    }

    @Override
    public Map<String, Object> nextStatus(Long nodeId) {
        Map<String, Object> propMapByNodeId = neo4jService.getPropLabelByNodeId(nodeId);
        String label = String.valueOf(propMapByNodeId.get(NODE_LABEL));
        List<Map<String, Object>> query = listStatus(label);
        Map<Long, Integer> statusOrMap = new HashMap<>();
        Integer order = 0;
        for (Map<String, Object> si : query) {
            Long sId = (Long) si.get(NODE_ID);
            statusOrMap.put(sId, order);
            order++;
        }

        Long object = getCurentStatusId(nodeId, label);
        Long nextId = null;
        if (statusOrMap.containsKey(object)) {
            Integer integer = statusOrMap.get(object) + 1;
            if (integer < query.size() && integer >= 0) {
                Map<String, Object> map2 = query.get(integer);
                nextId = (Long) map2.get(NODE_ID);
                relationService.addRel(STATUS, nodeId, nextId);
                return map2;
            }
        }

        return null;
    }

    private Long getCurentStatusId(Long nodeId, String label) {
        String currentStatus = "match(m:" + label + ")-[r:" + STATUS + "]->(st:" + STATE_STEP + ")" + " where  id(m)=" + nodeId + " return id(st) AS id,st.name AS name ORDER BY r.createTime DESC";
        List<Map<String, Object>> query2 = neo4jService.cypher(currentStatus);
        Map<String, Object> map = query2.get(0);
        Long object = (Long) map.get(NODE_ID);
        return object;
    }

    @Override
    public Map<String, Object> preStatus(Long nodeId) {
        Map<String, Object> propMapByNodeId = neo4jService.getPropLabelByNodeId(nodeId);
        String label = String.valueOf(propMapByNodeId.get(NODE_LABEL));
        // 查询状态机状态
        List<Map<String, Object>> query = listStatus(label);
        Map<Long, Integer> statusOrMap = new HashMap<>();
        Integer order = 0;
        for (Map<String, Object> si : query) {
            Long sId = (Long) si.get(NODE_ID);
            statusOrMap.put(sId, order);
            order++;
        }

        Long currentStatusId = getCurentStatusId(nodeId, label);
        Long nextId = null;
        if (statusOrMap.containsKey(currentStatusId)) {
            Integer integer = statusOrMap.get(currentStatusId) - 1;
            if (integer < query.size() && integer >= 0) {
                Map<String, Object> map2 = query.get(integer);
                nextId = (Long) map2.get(NODE_ID);
                relationService.addRel(STATUS, nodeId, nextId);
                return map2;
            }
        }
        return null;
    }

    /**
     * 获取当前类的状态机的状态列表 //查询状态机状态 String queryString =
     * "match(m)-[r:status]->(sm:"+STATE_MACHINE+")-[r1:childrens]->(st:"+STATE_STEP+")"
     * + " where m.label="+label+" return id(st) AS id,st.name AS name ORDER BY
     * st.order"; List<Map<String, Object>> query = neo4jService.cypher(queryString);
     *
     * @param label
     * @return
     */
    @Override
    public List<Map<String, Object>> listStatus(String label) {
        String queryString = "MATCH (po:" + META_DATA + "{label:\"" + label + "\"})-[*1..3]->(e:" + STATE_STEP
                + ") return e.id,e.code,e.name,e.value";
        // String queryString =
        // "match(m:"+META_DATA+")-[r:status]->(sm:"+STATE_MACHINE+")-[r1:childrens]->(st:"+STATE_STEP+")"
        // + " where m.label=\""+label+"\" return id(st) AS id,st.name AS name ORDER BY
        // st.order";
        return neo4jService.cypher(queryString);
    }

    @Override
    public Map<String, Object> lastStatus(Long nodeId) {
        Map<String, Object> propMapByNodeId = neo4jService.getPropLabelByNodeId(nodeId);
        String label = String.valueOf(propMapByNodeId.get(NODE_LABEL));
        List<Map<String, Object>> query = listStatus(label);
        Map<String, Object> map2 = query.get(query.size() - 1);
        Long nextId = (Long) map2.get(NODE_ID);
        relationService.addRel(STATUS, nodeId, nextId);
        return null;
    }

    @Override
    public Map<String, Object> firstStatus(Long nodeId) {
        Map<String, Object> propMapByNodeId = neo4jService.getPropLabelByNodeId(nodeId);
        String label = String.valueOf(propMapByNodeId.get(NODE_LABEL));
        Map<String, Object> map2 = listStatus(label).get(0);
        Long nextId = (Long) map2.get(NODE_ID);
        relationService.addRel(STATUS, nodeId, nextId);
        return null;
    }

    @Override
    public Map<String, Object> currentStatus(Long nodeId) {
        // 查询状态机状态
        String queryString = "match(m)-[r:status]->(st:" + STATE_STEP + ")" + " where id(m)=" + nodeId
                + " return id(st) AS id,st.code,st.name AS name ORDER BY r.createTime DESC ";
        List<Map<String, Object>> query = neo4jService.cypher(queryString);
        if (query == null || query.isEmpty()) {
            return null;
        }
        return query.get(0);
    }

    @Override
    public String statusCode(Map<String, Object> task) {
        return code(currentStatus(id(task)));
    }

    public Long statusId(Map<String, Object> task) {
        return id(currentStatus(id(task)));
    }

    public String statusName(Map<String, Object> task) {
        return name(currentStatus(id(task)));
    }

    @Override
    public void initStatus(Long nodeId) {
        Map<String, Object> currentStatus = currentStatus(nodeId);
        if (currentStatus == null) {
            Map<String, Object> propMapByNodeId = neo4jService.getPropLabelByNodeId(nodeId);
            String label = label(propMapByNodeId);
            Map<String, Object> map2 = listStatus(label).get(0);
            Long nextId = id(map2);
            relationService.addRel(STATUS, nodeId, nextId);
        }

    }

    public void updateStatus(Long nodeId, Map<String, Object> currentStatus) {
        if (currentStatus != null) {
            neo4jService.delRelation(nodeId, STATUS);
            relationService.addRel(STATUS, nodeId, id(currentStatus));
        }
    }

    public void updateStatus(Long nodeId, Long statusId) {
        if (statusId != null) {
            neo4jService.delRelation(nodeId, STATUS);
            relationService.addRel(STATUS, nodeId, statusId);
        }
    }


    /**
     * 更新状态
     *
     * @param label
     * @param vo
     */
    @Override
    public void statusRefresh(String label, Map<String, Object> vo, long id2) {
        Map<String, Object> currentStatus = currentStatus(id2);
        List<Map<String, Object>> listStatus = listStatus(label);
        if (status(vo) == null) {
            if (listStatus != null && !listStatus.isEmpty() && currentStatus == null) {
                initStatus(id2);
            }
            return;
        }

        if (currentStatus != null) {
            String code2 = code(currentStatus);
            if (code2 == null || !status(vo).equals(code2)) {
                for (Map<String, Object> si : listStatus) {
                    if (status(vo).equals(code(si))||status(vo).equals(value(si))) {
                        vo.put(STATUS,value(si));
                        updateStatus(id(vo), id(si));
                    }
                }
            }
        } else {
            for (Map<String, Object> si : listStatus) {
                if (status(vo).equals(value(si))||status(vo).equals(value(si))) {
                    vo.put(STATUS,value(si));
                    updateStatus(id(vo), id(si));
                }
            }
        }

    }

}

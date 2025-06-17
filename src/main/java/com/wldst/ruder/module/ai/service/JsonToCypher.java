package com.wldst.ruder.module.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.wldst.ruder.util.MapTool.*;

@Service
public class JsonToCypher {
    @Autowired
    private CrudNeo4jService neo4jService;

    public StringBuilder parseJsonToCypher(String json) {
        JSONObject jsonObj = JSON.parseObject(json);

        // 解析操作类型
        String operationType = string(jsonObj,"operationType");

// 构建Cypher查询
        StringBuilder cypherBuilder = new StringBuilder();

        // 解析参数
        Map<String,Object> parameters = mapObject(jsonObj,"parameters");
        List<String> questionsToUser = listStr(jsonObj, "questionsToUser");
        if(questionsToUser!=null&&!questionsToUser.isEmpty()){
            answerQustion(questionsToUser,parameters);
        }else{
            if ("query".equals(operationType)) {
                query(cypherBuilder,  parameters);
            }
            else if ("save".equals(operationType)) {
                save(cypherBuilder,  parameters);
            }else if ("create".equals(operationType)) {
                save(cypherBuilder,  parameters);
            }
            else if ("update".equals(operationType)) {
                update(cypherBuilder,  parameters);
            }else if ("delete".equals(operationType)) {
                delete(cypherBuilder,  parameters);
            }
        }
        return cypherBuilder;
    }

    private void answerQustion(List<String> questionsToUser,Map<String,Object> parameters) {
        String label = label(parameters);
        Map<String,Object> map = newMap();
        if(questionsToUser !=null&&!questionsToUser.isEmpty()){
            for(String question : questionsToUser){
                if(question.endsWith("是否存在？请提供详细信息以便我们进行定义。")){
                    String name =question.split(" 是否存在")[0];
                    Map<String, Object> attMapBy = neo4jService.getAttMapBy(NAME, name, label);
                    if(attMapBy==null||attMapBy.isEmpty()){
                        map.put(name,false);
                    }
                    map.put(name,attMapBy);
                }
            }
        }
    }

    private void save(StringBuilder cypherBuilder, Map<String, Object> parameters) {
        Map<String,Object> set = mapObject(parameters,"set");
        Map<String,Object> conditions = mapObject(parameters,"conditions");
        neo4jService.save(set,label(parameters));
    }

    private void delete(StringBuilder cypherBuilder, Map<String, Object> parameters) {
        Map<String,Object> conditions = mapObject(parameters,"conditions");
        if(id(conditions)==null){
            neo4jService.removeNodeByPropAndLabel(conditions,label(parameters));
        }else{
            neo4jService.delete(id(conditions));
        }
    }

    /**
     * `relationship`: 新增的关系操作：
     *         - `type`: `"居住于"` 表示用户与地址之间的关系类型。
     *         - `toLabel`: `"Address"` 指定地址节点的标签。
     *         - `toCondition`: 用于匹配特定的地址节点，例如匹配城市为 `"北京市"` 的地址。
     * @param cypherBuilder
     * @param parameters
     */
    private void update(StringBuilder cypherBuilder, Map<String, Object> parameters) {
        Map<String,Object> set = mapObject(parameters,"set");
        Map<String,Object> conditions = mapObject(parameters,"conditions");
        List<Map<String, Object>> node = neo4jService.queryBy(conditions, label(parameters));
        if (node.isEmpty()){
            return;
        }
        Map<String, Object> object = node.get(0);
        object.putAll(set);
        neo4jService.save(object,label(parameters));
    }

    private void merge(StringBuilder cypherBuilder, Map<String, Object> parameters) {

    }

    /**
     * {
     *   "operationType": "query",
     *   "entities": {
     *     "nodes": [
     *       {
     *         "label": "人物",
     *         "name": "刘强",
     *         "exists": false,
     *         "attributes": {}
     *       }
     *     ],
     *     "relationships": []
     *   },
     *   "questionsToUser": [
     *     "刘强 是否存在？请提供详细信息以便我们进行定义。"
     *   ],
     *   "result": null
     * }
     * @param cypherQuery
     * @param parameters
     */

    private static void query(StringBuilder cypherQuery,  Map<String,Object> parameters) {
        String label = label(parameters);
        String property = string(parameters,"property");
        String statistic = string(parameters,"statistic");
        String action = string(parameters,"action");
        Map<String,Object> conditions = mapObject(parameters,"conditions");

        cypherQuery.append("MATCH (n");

        if (!label.equals("*")) {
            cypherQuery.append(":").append(label);
        }
        cypherQuery.append( ")");
        if (conditions != null && !conditions.isEmpty()) {
            cypherQuery.append(" WHERE ");
            for (Map.Entry<String, Object> entry : conditions.entrySet()) {
                cypherQuery.append("n.").append(entry.getKey());
                cypherQuery.append(" = ").append("'").append(entry.getValue()).append("'");
            }
        }

        if ("count".equals(action)) {
            cypherQuery.append(" RETURN count(n)");
        }else{

        }
    }
}

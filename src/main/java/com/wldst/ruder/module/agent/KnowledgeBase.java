package com.wldst.ruder.module.agent;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.util.MapTool;

public class KnowledgeBase extends MapTool{
    // 模拟知识库，这里使用一个简单的HashMap来存储已知信息
    private Map<String, Object> knowledgeBase = new HashMap<>();
    private static CrudNeo4jService neo4jService=null;

    public KnowledgeBase() {
        // 可以在这里添加更多的知识库条目
        if(neo4jService==null){
            neo4jService=(CrudNeo4jService) SpringContextUtil.getBean(CrudNeo4jService.class);
        }
    }

    public KnowledgeBase(String keyword) {
        // 可以在这里添加更多的知识库条目
        if(neo4jService==null){
            neo4jService=(CrudNeo4jService)SpringContextUtil.getBean(CrudNeo4jService.class);
        }
        //查询元数据
        List<Map<String, Object>> maps=neo4jService.searchMeataDataBy(keyword);
        knowledgeBase.put("元数据", maps);

        List<Map<String, Object>> datas=neo4jService.searchDataBy(keyword);
        knowledgeBase.put("实例数据", datas);
        //逐个获取上下文，选中元数据，或者实例数据后，更新其上下文
        List<Map<String, Object>> relations=neo4jService.searchDataBy(keyword);
        knowledgeBase.put("上下文", relations);


        List<Map<String, Object>> ks=neo4jService.cypher("MATCH(n:Knowledge) where n.keyword='"+keyword+"' return n");
        for(Map<String, Object> ki : ks){
            String keyword1=string(ki, "keyword");
            String content=string(ki, "content");
            knowledgeBase.put(keyword1, content);
        }

    }

    // 检索知识的函数，根据查询返回相应的知识信息
    public String retrieveKnowledge(String query) {
        return (String) knowledgeBase.getOrDefault(query, "没有找到相关信息");
    }
}

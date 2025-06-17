package com.wldst.ruder.module.parse.impl;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.LemodoApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.QuestionAskDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.NetHandleUtil;
import com.wldst.ruder.util.RestApi;


/**
 * 问答
 *
 * @return
 */
@Component
public class AskBaseNodeInfo extends QuestionAskDomain implements MsgProcess {

    final static Logger logger = LoggerFactory.getLogger(AskBaseNodeInfo.class);
    @Autowired
    private RestApi restApi;
    @Value("${http.port}")
    private Integer port;
    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "AskBaseNodeInfo")
    public Object process(String msg, Map<String, Object> context) {
	// 并行概率执行
	String chatPrefix = "ab-";
	if (!msg.startsWith(chatPrefix)) {
	    return null;
	}
//	context.put(USED, true);
//	msg = msg.trim().replaceAll("：", ":");
	String[] qa = msg.split("-");	
	msg = qa[1];
	
	String who =null;
	if(qa.length>2) {
	    who = qa[2];
	}
	
//	Map<String, Object> template = neo4jService.getOneMapById(Long.valueOf(msg), "PromptTemplate");
	String cypher = "match(n:PromptTemplate) where n.name contains '"+msg+"' or n.code ='"+msg+"' return n";
	 List<Map<String, Object>> propts = neo4jService.cypher(cypher);
	 Map<String, Object> template = userSelect(context, propts);
	
	String[] nodeIds =  splitValue(template, "node");
	
	//判断是什么类型，如果是ID直接获取node（name），字段属性，关联字段。属性改如何取值？
	for(String ni:nodeIds) {
	    String nodeLabel = neo4jService.getNodeLabelByNodeId(Long.valueOf(ni));
	    Map<String, Object> oneMapById = neo4jService.getOneMapById(Long.valueOf(ni));
	   if(META_DATA.equals(nodeLabel)) {
	       relations(context, template,oneMapById);
	   }else {
	       String relations = relations(oneMapById,template, nodeLabel);
	       ask(context, template, oneMapById, relations);
	   }	    
	}
	return context;
    }
    
    public Map callChatGlm(Map<String, Object> context,String backGround) {	
	Map<String, Object> md = neo4jService.getAttMapBy(NAME, "aiChat", "api");
//	Map<String, Object> md = neo4jService.getAttMapBy(NAME, "aiChat-local", "api");
	String params = string(md, "params");
	String serverUrl = string(md, "url");
	restApi.setServerUrl(serverUrl);
	JSONObject parseObject = JSON.parseObject(params);
	parseObject.put("myId", string(context, "MyId"));
	parseObject.put("text", backGround);
	parseObject.put("clientAPI","http://"+NetHandleUtil.getLocalIpAddress()+":"+port+ LemodoApplication.MODULE_NAME+"//aiclient/answer");
	Map postApi = restApi.postApi(parseObject, Map.class);
	return postApi;
    }

    
}

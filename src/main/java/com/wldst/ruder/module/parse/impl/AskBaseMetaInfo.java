package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.NetHandleUtil;
import com.wldst.ruder.util.RestApi;

/**
 * 问答
 * 
 * @param msg
 * @return
 */
//@Component
//public class AskBaseMetaInfo extends ParseExcuteDomain implements MsgProcess {
//
//    final static Logger logger = LoggerFactory.getLogger(AskBaseMetaInfo.class);
//    @Autowired
//    private RestApi restApi;
//    @Value("${http.port}")
//    private Integer port;
//    /**
//     * 查询谁的属性是什么，关系有哪些
//     * 
//     * @param msg
//     * @return
//     */
//    @Override
//    @ServiceLog(description = "QABaseNodeInfo")
//    public Object process(String msg, Map<String, Object> context) {
//	// 并行概率执行
//	String chatPrefix = "qa:";
//	if (!msg.startsWith(chatPrefix)) {
//	    return null;
//	}
////	context.put(USED, true);
//	msg = msg.trim().replaceAll("：", ":");
//
//	String[] qa = msg.split(":");
//	
//	msg = qa[1];
//	
//	Map<String, Object> template = neo4jService.getOneMapById(Long.valueOf(msg), "PromptTemplate");
//	String question = string(template,"question");
//	String[] nodeIds =  splitValue(template, question);
//	String[] relationLabels =  splitValue(template, "relationLabel");
//	//判断是什么类型，如果是ID直接获取node（name），字段属性，关联字段。属性改如何取值？
//	for(String ni:nodeIds) {
//	    String nodeLabel = neo4jService.getNodeLabelByNodeId(Long.valueOf(ni));
//	    Map<String, Object> oneMapById = neo4jService.getOneMapById(Long.valueOf(ni));
//	   
//		//获取用户所有的关系
//	    String relations = relations(oneMapById,nodeLabel);
//	    String backGround="用户"+name(oneMapById)+"的关系数据："+relations+","+question;
//	    GptChat gc = (GptChat) SpringContextUtil.getBean("gptChat");
//	    context.put(USED, false);
//	    Object process = gc.process("Chat:"+backGround, context);
//	    
//	    Map<String,Object> a2 = newMap();
////	    answer
//	    a2.put("answer",process);
//	    a2.put("question",backGround);
//	    neo4jService.save(a2, "askAnswer");
//	}
//	return context;
//	
//	
////	Map postApi = callChatGlm(context);
////
////	return string(postApi, DATA);
//    }
//    public Map callChatGlm(Map<String, Object> context,String backGround) {
//	
//	Map<String, Object> md = neo4jService.getAttMapBy(NAME, "aiChat", "api");
////	Map<String, Object> md = neo4jService.getAttMapBy(NAME, "aiChat-local", "api");
//	String params = string(md, "params");
//	String serverUrl = string(md, "url");
//	restApi.setServerUrl(serverUrl);
//	JSONObject parseObject = JSON.parseObject(params);
//	parseObject.put("myId", string(context, "MyId"));
//	parseObject.put("text", backGround);
//	parseObject.put("clientAPI","http://"+NetHandleUtil.getLocalIpAddress()+":"+port+LemodoApplication.MODULE_NAME+"//aiclient/answer");
//	Map postApi = restApi.postApi(parseObject, Map.class);
//	return postApi;
//    }
//    public String relations(Map<String,Object> dataBy,String label) {
//	// 读取关联数据
//	List<String> rels = new ArrayList<>();
//	    List<Map<String, Object>> outRelations = neo4jUService.getOutRelations(dataBy, label);
//	    for(Map<String, Object> oi: outRelations) {
//		Map<String, Object> mapObject = mapObject(oi,"endNodeProperties");
//		Map<String, Object> relProps = mapObject(oi,"relProps");
//		List<String> endLabels = listStr(oi,"endLabels");
//		if(!META_DATA.equals(label(mapObject))&& !endLabels.contains(META_DATA)&&endLabels.size()==1) {
//		    String label2 = label(relProps);
//		    StringBuilder sb = new StringBuilder();
//		    sb.append("("+name(dataBy)+":"+label+jsonString(dataBy)+")-["+name(relProps));
////		    sb.append("("+name(dataBy)+":"+label+")-["+name(relProps));
//		    if(label2!=null) {
//			sb.append(":"+label2);
//		    }
//		    sb.append("]->("+name(mapObject)+":"+endLabels.get(0)+jsonString(mapObject)+")");
////			 sb.append("]->("+name(mapObject)+":"+endLabels.get(0)+")");
//			rels.add(sb.toString());
//		}
//	    }
//	   
//	    List<Map<String, Object>> inRelations = neo4jUService.getInRelations(dataBy, label);
//	    for(Map<String, Object> oi: inRelations) {
//		Map<String, Object> mapObject = mapObject(oi,"endNodeProperties");
//		Map<String, Object> relProps = mapObject(oi,"relProps");
//		List<String> endLabels = listStr(oi,"endLabels");
//		if(!META_DATA.equals(label(mapObject))&& endLabels.contains(META_DATA)&&endLabels.size()==1) {
//		    String label2 = label(relProps);
//		    StringBuilder sb = new StringBuilder();
//		    sb.append("("+name(dataBy)+":"+label+prop(dataBy,label)+")<-["+name(relProps));
//		    if(label2!=null) {
//			sb.append(":"+label2);
//		    }
//		    sb.append("]-("+name(mapObject)+":"+endLabels.get(0)+prop(mapObject,endLabels.get(0))+")");
//			rels.add(sb.toString());
//		}
//	    }
//	    
//	    return String.join(",", rels);
//    }
//    
//    public String prop(Map<String,Object> dataBy,String label) {
//	JSONObject vo = new JSONObject();
//	vo.put("poId", label);
//	// 查询自定义字段数据
//	List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
//	for (Map<String, Object> fi : fieldInfoList) {
//	    Object object = fi.get("field");
//	    dataBy.put();
//	}
//	
//	return jsonString(dataBy);
//    }
//    
//}

package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.LemodoApplication;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.domain.QuestionAskDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.openai.ChatQuestion;
import com.wldst.ruder.util.NetHandleUtil;
import com.wldst.ruder.util.RestApi;

/**
 *
 * @return
 */
@Component
public class CareerDesign extends QuestionAskDomain implements MsgProcess {

    final static Logger logger = LoggerFactory.getLogger(CareerDesign.class);
    @Autowired
    private RestApi restApi;
    @Value("${http.port}")
    private Integer port;
    protected static List<String> preFix= Arrays.asList("careerDesign", "职业生涯设计", "职业规划设计", "智慧职业设计");// 唤醒词
    /**
     * 查询谁的属性是什么，关系有哪些
     *
     * @return
     */
    @Override
    @ServiceLog(description = "careerDesign")
    public Object process(String msgx, Map<String, Object> context) {
	String msg = msgx.trim().replaceAll("：", ":");
	// 并行概率执行
	for(String si:preFix) {
	    String chatPrefix = si+":";
		if (!msg.startsWith(chatPrefix)) {
		    continue;
		}
		msg = msg.split(":")[1];
		Map<String, Object> user = neo4jService.getAttMapBy(NAME, msg, "User");   
		//获取用户所有的关系
		String relations = relations(user,"User");
		String backGround="请使用人资相关模型来进行数据分析，给用"+msg+"做一下职业规划，他的属性，关系数据有："+relations+"。";
		GptChat gc = (GptChat) SpringContextUtil.getBean("gptChat");
		context.put(USED, false);
		Object process = gc.process("Chat:"+backGround, context);
		handleResult(context,user,  msgx, backGround);
//		return localGLM(context, backGround);
	}
	return null;
    }
    
    public void handleResult(Map<String, Object> context, Map<String, Object> oneMapById,
	    String question,String backGround) {
	ChatQuestion object = (ChatQuestion) context.get("qa");
	// answer
	if (object == null) {
	    return;
	}
	String answer2 = object.getAnswer();
	Map<String, Object> a2 = newMap();

	a2.put("answer", answer2);
	a2.put("question", backGround);
	a2.put(NAME, name(oneMapById) + question);

	Node save = neo4jService.save(a2, "askAnswer");

	relationService.addRel("careerSchedule", question, id(oneMapById), save.getId());
    }
    public Object localGLM(Map<String, Object> context, String backGround) {
	Map<String, Object> md = neo4jService.getAttMapBy(NAME, "aiChat", "api");
//	Map<String, Object> md = neo4jService.getAttMapBy(NAME, "aiChat-local", "api");
	String params = string(md, "params");
	String serverUrl = string(md, "url");
	restApi.setServerUrl(serverUrl);
	JSONObject parseObject = JSON.parseObject(params);
	parseObject.put("myId", string(context, "MyId"));
	parseObject.put("text", backGround);
	parseObject.put("clientAPI","http://"+NetHandleUtil.getLocalIpAddress()+":"+port+LemodoApplication.MODULE_NAME+"//aiclient/answer");
	Map postApi = restApi.postApi(parseObject, Map.class);

	return string(postApi, DATA);
    }
    
    public Object serverGLM3(Map<String, Object> context, String backGround) {
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

	return string(postApi, DATA);
    }
    public String relations(Map<String,Object> dataBy,String label) {
	List<String> rels = new ArrayList<>();
	Set<String> sensitiveData = neo4jService.getSensitiveData();
	    List<Map<String, Object>> outRelations = neo4jUService.getOutRelations(dataBy, label);
	    for(Map<String, Object> oi: outRelations) {
		Map<String, Object> mapObject = mapObject(oi,"endNodeProperties");
		Map<String, Object> relProps = mapObject(oi,"relProps");
		List<String> endLabels = listStr(oi,"endLabels");
		Boolean sense=false;
		for(String endNodeLabel : endLabels) {
		    if(sensitiveData.contains(endNodeLabel)) {
			sense=true;
		     }
		}
		if(sense) {
		    continue;
		}
		
		if(!META_DATA.equals(label(mapObject))&& !endLabels.contains(META_DATA)&&endLabels.size()==1) {
		    String label2 = label(relProps);
		    StringBuilder sb = new StringBuilder();
//		    sb.append("("+name(dataBy)+":"+label+jsonString(dataBy)+")-["+name(relProps));
		    sb.append("("+name(dataBy)+":"+label+")-["+name(relProps));
		    if(label2!=null) {
			sb.append(":"+label2);
		    }
//		    sb.append("]->("+name(mapObject)+":"+endLabels.get(0)+jsonString(mapObject)+")");
			sb.append("]->("+name(mapObject)+":"+endLabels.get(0)+")");
			rels.add(sb.toString());
		}
	    }
	   
	    List<Map<String, Object>> inRelations = neo4jUService.getInRelations(dataBy, label);
	    for(Map<String, Object> oi: inRelations) {
		Map<String, Object> mapObject = mapObject(oi,"endNodeProperties");
		Map<String, Object> relProps = mapObject(oi,"relProps");
		List<String> endLabels = listStr(oi,"endLabels");
		Boolean sense=false;
		for(String endNodeLabel : endLabels) {
		    if(sensitiveData.contains(endNodeLabel)) {
			sense=true;
		     }
		}
		if(sense) {
		    continue;
		}
		
		if(!META_DATA.equals(label(mapObject))&& endLabels.contains(META_DATA)&&endLabels.size()==1) {
		    String label2 = label(relProps);
		    StringBuilder sb = new StringBuilder();
		    sb.append("("+name(dataBy)+":"+label+jsonString(dataBy)+")<-["+name(relProps));
		    if(label2!=null) {
			sb.append(":"+label2);
		    }
		    sb.append("]-("+name(mapObject)+":"+endLabels.get(0)+jsonString(mapObject)+")");
			rels.add(sb.toString());
		}
	    }
	    
	    return String.join(",", rels);
    }
    
}

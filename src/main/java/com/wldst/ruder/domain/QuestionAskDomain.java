package com.wldst.ruder.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.util.LoggerTool;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.module.parse.impl.GptChat;
import com.wldst.ruder.openai.ChatQuestion;

/**
 * 解析聊天数据，并根据数据更新或者查询数据
 */
@Component
public class QuestionAskDomain extends ParseExcuteDomain {
    final static Logger logger = LoggerFactory.getLogger(QuestionAskDomain.class);

    public void ask(Map<String, Object> context, Map<String, Object> template, Map<String, Object> oneMapById,
	    String relations) {
	String question = string(template, "question");

	String backGround = "请给" + name(oneMapById)+"进行"+question + "，他的关系数据有：" + relations ;
	GptChat gc = (GptChat) SpringContextUtil.getBean("gptChat");
	context.put(USED, false);
	Object process = gc.process("Chat:" + backGround, context);
	relateResult(context, template, oneMapById, question, backGround);
    }

    public void relateResult(Map<String, Object> context, Map<String, Object> template, Map<String, Object> oneMapById,
	    String question, String backGround) {
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
	a2.put(CREATETIME,Calendar.getInstance().getTimeInMillis());
	Node save = neo4jService.save(a2, "askAnswer");

	relationService.addRel(code(template), question, id(oneMapById), save.getId());
    }

    public void relations(Map<String, Object> context, Map<String, Object> template, Map<String, Object> metaData) {
	String metaLabel = label(metaData);
	//敏感对象
//	Set<String> sensitiveData = neo4jService.getSensitiveData();
	// 读取关联数据
	List<Map<String, Object>> listAllByLabel = neo4jService.listAllByLabel(metaLabel);
	for (Map<String, Object> mi : listAllByLabel) {
	    Map<String, Object> copyWithKeys = copyWithKeys(mi, columns(metaData));
	    String ri = relations(copyWithKeys, template, metaLabel);
	    ask(context, template, mi, ri);
	}
    }

    public String relations(Map<String, Object> dataBy, Map<String, Object> template, String label) {
	// 读取关联数据
	Set<String> relLabels = splitValue2Set(template, "relationLabels");
	Set<String> sensitiveData = neo4jService.getSensitiveData();
	List<String> rels = new ArrayList<>();
	List<Map<String, Object>> outRelations = neo4jUService.getOutRelations(dataBy, label);
	if (outRelations != null && !outRelations.isEmpty()) {
	    for (Map<String, Object> oi : outRelations) {
		Map<String, Object> endObject = mapObject(oi, "endNodeProperties");
		Map<String, Object> relProps = mapObject(oi, "relProps");
		List<String> endLabels = listStr(oi, "endLabels");
		String label2 = label(relProps);
		String endNodeLabel = label(endObject);
		if(sensitiveData.contains(endNodeLabel)) {
		    continue; 
		}
		if (relLabels != null && !relLabels.isEmpty() && !relLabels.contains(label2)) {
		    continue;
		}
		if (!META_DATA.equals(endNodeLabel) && !endLabels.contains(META_DATA) && endLabels.size() == 1) {

		    StringBuilder sb = new StringBuilder();
		    sb.append("(" + name(dataBy) + ":" + label + seeProp(dataBy, label) + ")-[" + name(relProps));
		    // sb.append("("+name(dataBy)+":"+label+")-["+name(relProps));
		    if (label2 != null) {
			sb.append(":" + label2);
		    }
		    sb.append("]->(" + name(endObject) + ":" + endLabels.get(0) + seeProp(endObject,endLabels.get(0)) + ")");
		    // sb.append("]->("+name(mapObject)+":"+endLabels.get(0)+")");
		    rels.add(sb.toString());
		}
	    }
	}

	List<Map<String, Object>> inRelations = neo4jUService.getInRelations(dataBy, label);
	if (inRelations != null && !inRelations.isEmpty()) {

	    for (Map<String, Object> oi : inRelations) {
		Map<String, Object> endObject = mapObject(oi, "endNodeProperties");
		Map<String, Object> relProps = mapObject(oi, "relProps");
		List<String> endLabels = listStr(oi, "endLabels");
		String label2 = label(relProps);
		if (relLabels != null && !relLabels.isEmpty() && !relLabels.contains(label2)) {
		    continue;
		}
		String endNodeLabel = label(endObject);
		if(sensitiveData.contains(endNodeLabel)) {
		    continue; 
		}
		if (!META_DATA.equals(endNodeLabel) && endLabels.contains(META_DATA) && endLabels.size() == 1) {
		    StringBuilder sb = new StringBuilder();
		    sb.append("(" + name(dataBy) + ":" + label + seeProp(dataBy, label) + ")<-[" + name(relProps));
		    if (label2 != null) {
			sb.append(":" + label2);
		    }
		    sb.append(
			    "]-(" + name(endObject) + ":" + endLabels.get(0) + seeProp(endObject, endLabels.get(0)) + ")");
		    rels.add(sb.toString());
		}
	    }
	}

	return String.join(",", rels);
    }

    public String seeProp(Map<String, Object> data, String metaLabel) {
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(LABEL, metaLabel, META_DATA);
	Map<String, Object> copyWithKeys = copyWithKeys(data,string(attMapBy,COLUMNS));
	
	Map<String, Object> copy = copyWithoutKeys(copyWithKeys, "creator,createTime,updator,updateTime");
	Set<String> shortCol = shortCols(metaLabel);
	
	JSONObject vo = new JSONObject();
	vo.put("poId", metaLabel);
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
	for (Map<String, Object> fi : fieldInfoList) {
	    String field = string(fi, "field");
	    if(shortCol!=null&&!shortCol.isEmpty()&&!shortCol.contains(field)) {
		continue;
	    }
	    Object type = fi.get("type");
	    if (type != null) {
		try {
		    Long value2 = longValue(copy, field);
		    if (value2 != null) {
			String name = neo4jService.getById(value2, NAME);
			if(name!=null) {
			    copy.put(field, name);
			}else {
			    Map<String, Object> dataRelate = neo4jService.getPropLabelByNodeId(value2);
			    Set<String> shortColx = shortCols(label(dataRelate));
			    String see =  string(dataRelate,String.valueOf(shortColx.toArray()[0]));
			    copy.put(field, see);
			}
		    }
		} catch (Exception e) {
		    LoggerTool.info(logger,field + "==");
		}
	    }
	}

	return jsonString(copy);
    }

    public Set<String> shortCols(String metaLabel) {
	Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, metaLabel, META_DATA);
	Set<String> shortCol = splitValue2Set(metaData, "shortShow");
	return shortCol;
    }
    
   

}

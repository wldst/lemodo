package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 * 查询谁的属性是什么，关系有哪些
 * 
 * @param msg
 * @return
 */
@Component
public class QuerySomeOf extends ParseExcuteDomain implements MsgProcess {
    
    final static Logger logger = LoggerFactory.getLogger(QuerySomeOf.class);
    public List<String> getRelationQuery() {
	List<String> query = new ArrayList<>();
	query.add("可以访问哪些");
	query.add("能访问哪些");
	query.add("有哪些");
	query.add("有什么");
	query.add("是什么");
	query.add("是哪些");
	query.add("是");
	query.add("有");
	return query;
    }
    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "查询谁的属性是什么，关系有哪些，并返回答案，在Context中有是否执行")
    public String process(String msg, Map<String, Object> context) {
	StringBuilder sb = new StringBuilder();
	msg = msg.trim().replaceAll("\\?", "").replaceAll("？", "");
	// 并行概率执行
	List<String> query = getRelationQuery();
//	userContext
	String startQuery = null;
	String usedOkQuery = null;
	context.put(USED, false);
	for (String si : query) {
	    if(bool(context, USED)) {
		break;
	    }
	    
	    if (sb.length() < 1 && msg.contains(si)) {
		if (usedOkQuery != null && !usedOkQuery.startsWith(si)) {
		    continue;
		}
		context.put(USED, true);
		String[] queryPart = msg.split(si);
		startQuery = queryPart[0];
		if(startQuery.isBlank()) {
		    context.put(USED, false);return null;
		}
		if (queryPart.length > 1) {
		    context.put("answer", startQuery + si.substring(0, 1) + queryPart[1]);
		} else {
		    context.put("answer", startQuery + si.substring(0, 1));
		}

		List<Map<String, Object>> datas = null;
		// xx的xx是，xx的ss有
		for (String owni : ownWords) {
		    if (startQuery.contains(owni)) {
			context.put("hasOwni", true);
			LoggerTool.info(logger,msg + "包含所属关键字:" + owni);
			String[] owns = startQuery.split(owni);
			String startName = owns[0];
			String por = owns[1];// 获取元数据信息
			Map<String, String> metaNameCol = getMetaData();
			for (Map<String, Object> oMdi : getMetaDataByName(startName)) {
			    String metaColi = metaNameCol.get(por);
			    if (metaColi != null) {
				String string = string(oMdi, metaColi);
				return string;
			    }
			}

			datas = queryPropRelOf(context, sb, startQuery, usedOkQuery, si, owni);
		    }
		}
		if (!bool(context, "hasOwni") && queryPart.length > 1) {
		    String startName = queryPart[0];
		    String endName = null;
		    if (queryPart[1].length() > 0) {
			// 党员有xx的哪些xx：党员有待办的那些权限
			for (String owni : ownWords) {
			    if (queryPart[1].contains(owni)) {
				LoggerTool.info(logger,"rightHasOwni:" + queryPart[1]);
				context.put("rightHasOwni", true);
				String[] owns = queryPart[1].split(owni);
				endName = owns[0];
				String por = owns[1];// 获取元数据信息
				if (endName != null) {
				    context.put("endName", endName);
				}
				if (por != null) {
				    por = replaceQueryWord(por);
				    // context.put("rName", por);
				}
				datas = queryPropOrRelOf(sb, usedOkQuery, si, startName, endName, por,
					context);
			    }
			}

			if (!bool(context, "rightHasOwni")) {
			    LoggerTool.info(logger,"right not HasOwni:" + queryPart[1]);
			    // xx有哪些ss
			    if (queryPart[1].startsWith("哪些")) {
				endName = replaceQueryWord(queryPart[1]);
			    }
			    if (queryPart[1].startsWith("个")) {
				endName = queryPart[1].replaceFirst("个", "");
			    }
			    context.put("endName", endName);
			    if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
				continue;
			    }
			    endName = string(context, "endName");
			    if(endName!=null) {
				endName = endName.trim().replaceAll("\\?", "").replaceAll("？", "");
				    if (endName.trim()
					    .length() > 1) {
					Map<String, Object> endMeta = getData(endName, META_DATA, context);
					if (endMeta != null) {
					    // 直接关系数据，一层关系
					    String zhiJieRelData = getZhiJieRelation(msg, context, startQuery,
						    startName, endMeta);
					    // 获取直接关系
					    datas = neo4jUService.cypher(zhiJieRelData);
					    if (datas != null && datas.size() > 0) {
						seeEndNode(sb, datas);
					    } else {
						if (datas == null) {
						    datas = new ArrayList<>();
						}
						queryRelPathEnds(sb, startQuery, datas, endName, endMeta, context);
					    }
					} else {
					    LoggerTool.info(logger,"endMeta is null to see relation");
					    seeRelation(sb, endName, startName, context);
					}
				    } else {
					seeRelation(sb, endName, startName, context);
				    }
			    }
			}
		    } else {
			seeRelation(sb, endName, startName, context);
			Map<String, Object> selectedOne = selectedData(context, startName);
			datas = neo4jUService.cypher(
				"MATCH (n)-[r*1..3]->(m) WHERE id(n)=" + id(selectedOne) + "  RETURN distinct m ");
			seeEndNode(sb, datas);
		    }
		}

		if (datas != null && !datas.isEmpty()) {
		    usedOkQuery = si;
		}
	    }
	}
	if (context.get(USED).equals(true)) {
	    if (sb.toString().length() < 1) {
		return string(context, "answer") + "，答：系统中无此信息\n";
	    }

	    String answerQ = "\n" + string(context, "answer") + ":<BR>";
	    if (bool(context, "hasOwni")) {
		answerQ = "\n" + string(context, "answer") + ":\n";
	    }
	    return answerQ + sb.toString();
	}

	return null;
    }
    
    /**
     * xx的xx是，xx的ss有
     * 
     * @param context
     * @param sb
     * @param startQuery
     * @param usedOkQuery
     * @param si
     * @param datas
     * @param owni
     * @return
     */
    public List<Map<String, Object>> queryPropRelOf(Map<String, Object> context, StringBuilder sb, String startQuery,
	    String usedOkQuery, String si, String owni) {
	List<Map<String, Object>> datas = new ArrayList<>();
	String[] owns = startQuery.split(owni);
	String startName = owns[0];
	String por = owns[1];// 获取元数据信息

	if (containLabelInfo(startName)) {
	    LoggerTool.info(logger,startName + "包含括号");
	    Map<String, Object> onlyContext = new HashMap<>();
	    startName = onlyName(onlyContext, startName);
	    String dataLabel = string(onlyContext, "dataLabel");
	    Map<String, Object> mapObject = mapObject(onlyContext, "dataMd");
	    String coli = null;
	    Map<String, String> nameColumn = nameColumn(mapObject);
	    if (nameColumn.get(por) != null) {
		coli = nameColumn.get(por);
	    }
	    if (coli != null) {// 获取自定义字段，关联字段数据信息
		seeProperty(sb, startName, coli, mapObject, context);
	    } else {// 查询关系
		if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
		    return datas;
		}
		if (datas.isEmpty()) {
		    datas = neo4jUService.cypher(
			    "MATCH (n:" + dataLabel + ")-[r]->(m) WHERE r.name = '" + por + "'  return distinct m ");
		    seeEndNode(sb, datas);
		}
		if (datas == null || datas.isEmpty()) {
		    Map<String, Object> endMeta = getData(por, META_DATA, context);
		    datas = neo4jUService.cypher("MATCH (n:" + dataLabel + ")-[r]->(m:" + label(endMeta)
			    + ") WHERE r.name = '" + por + "'  RETURN m ");
		    seeEndNode(sb, datas);
		}
	    }
	} else {
	    for (Map<String, Object> oMdi : getMetaDataByName(startName)) {
		String coli = null;
		Map<String, String> nameColumn = nameColumn(oMdi);
		if (nameColumn.get(por) != null) {
		    coli = nameColumn.get(por);
		}
		if (coli != null) {// 获取自定义字段，关联字段数据信息
		    seeProperty(sb, startName, coli, oMdi, context);
		} else {// 查询关系
		    if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
			continue;
		    }
		    Map<String, Object> selectedOne = selectedData(context, startName);
			if(selectedOne==null){
				continue;
			}
		    if (datas == null || datas.isEmpty()) {
			datas = neo4jUService.cypher("MATCH (n)-[r]->(m) WHERE id(n)=" + id(selectedOne) + " and r.name = '"
				+ por + "'  return distinct m ");
			seeEndNode(sb, datas);
		    }
		    if (datas == null || datas.isEmpty()) {
			Map<String, Object> endMeta = getData(por, META_DATA, context);
			datas = neo4jUService.cypher("MATCH (n)-[r]->(m:" + label(endMeta) + ") where id(n)="
				+ id(selectedOne) + "  RETURN m ");
			seeEndNode(sb, datas);
		    }
		}
	    }
	}

	return datas;
    }
    
    public String getZhiJieRelation(String msg, Map<String, Object> conversationContext, String startQuery,
	    String startName, Map<String, Object> endMeta) {
	String zhiJieRelData = null;
	if (containLabelInfo(startName)) {
	    Map<String, Object> onlyContext = new HashMap<>();
	    startName = onlyName(onlyContext, startName);
	    String dataLabel = string(onlyContext, "dataLabel");
	    // Map<String, Object> mapObject = mapObject(onlyContext, "dataMd");
	    String preId = "MATCH (n:" + dataLabel + ")-[r:HAS_PERMISSION]-(m:" + label(endMeta) + ")  where n.name='"
		    + startQuery + "'   return distinct id(n) AS id";
	    List<Map<String, Object>> preNode = neo4jUService.cypher(preId);

	    if (preNode != null && !preNode.isEmpty()) {
		if (msg.endsWith("有哪些角色")) {
		    if (preNode.size() > 1) {
			zhiJieRelData = userSelectQueryPermissionEnd(conversationContext, startName, endMeta,
				dataLabel);
		    } else {
			zhiJieRelData = "MATCH (n:" + dataLabel + ")-[r:HAS_PERMISSION]-(m:" + label(endMeta)
				+ ")  where n.name='" + startQuery + "'   return distinct m ";
		    }
		} else {
		    if (preNode.size() > 1) {
			zhiJieRelData = userSelectQueryEnd(conversationContext, startName, endMeta, dataLabel);
		    } else {
			zhiJieRelData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
				+ "'   return distinct m ";
		    }
		}
	    }
	} else {
	    // 预先判断满足查询条件的数据是否存在
	    String preLabel = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
		    + "' with  n unwind labels(n) as x  return distinct x";
	    List<Map<String, Object>> preLabels = neo4jUService.cypher(preLabel);

	    if (preLabels != null && !preLabels.isEmpty()) {
		if (preLabels.size() > 1) {// 多种节点，需要用户选择数据
		    zhiJieRelData = userSelectAuthEnd(msg, conversationContext, startName, endMeta, preLabels);
		} else {
		    zhiJieRelData = oneLableUserSelectedStart(conversationContext, startQuery, startName, endMeta,
			    preLabels);
		}
	    } else {
		zhiJieRelData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
			+ "'   return distinct m ";
	    }

	}
	return zhiJieRelData;
    }
    public String userSelectQueryEnd(Map<String, Object> conversationContext, String startName,
	    Map<String, Object> endMeta, String dataLabel) {
	Map<String, Object> selectedOne = null;
	String zhiJieRelData = null;
	if (dataLabel != null) {
	    selectedOne = getData(startName, dataLabel, conversationContext);
	    Long startId = id(selectedOne);
	    zhiJieRelData = "MATCH (n:" + dataLabel + ")-[r]-(m:" + label(endMeta) + ")  where id(n)=" + startId
		    + "  return distinct m ";
	}
	return zhiJieRelData;
    }
    public String userSelectQueryPermissionEnd(Map<String, Object> conversationContext, String startName,
	    Map<String, Object> endMeta, String dataLabel) {
	String zhiJieRelData;
	Map<String, Object> selectedOne;
	if (dataLabel != null) {
	    selectedOne = getData(startName, dataLabel, conversationContext);
	    Long startId = id(selectedOne);
	    zhiJieRelData = "MATCH (n:" + dataLabel + ")-[r:HAS_PERMISSION]-(m:" + label(endMeta) + ")  where id(n)="
		    + startId + "  return distinct m ";
	} else {
	    selectedOne = getData(startName, conversationContext);
	    Long startId = id(selectedOne);
	    zhiJieRelData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + label(endMeta) + ")  where id(n)=" + startId
		    + "  return distinct m ";
	}
	return zhiJieRelData;
    }
    
    /**
     * 有确定的Label。返回查询结果
     * 
     * @param conversationContext
     * @param startQuery
     * @param startName
     * @param endMeta
     * @param preLabels
     * @return
     */
    public String oneLableUserSelectedStart(Map<String, Object> conversationContext, String startQuery,
	    String startName, Map<String, Object> endMeta, List<Map<String, Object>> preLabels) {
	String zhiJieRelData = null;
	String oneLabel = string(preLabels.get(0), "x");
	String preId = "MATCH (n:" + oneLabel + ")-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
		+ "'   return distinct id(n) AS id";
	List<Map<String, Object>> preNode = neo4jUService.cypher(preId);
	if (preNode != null && !preNode.isEmpty()) {
	    // 有多条数据，用户选择，否则直接返回
	    if (preNode.size() > 1) {
		Long startId = getStartIdBy(conversationContext, startName, oneLabel);
		zhiJieRelData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where id(n)=" + startId
			+ "  return distinct m ";
	    } else {
		zhiJieRelData = "MATCH (n:" + oneLabel + ")-[r]-(m:" + label(endMeta) + ")  where n.name='" + startQuery
			+ "'   return distinct m ";
	    }
	}
	return zhiJieRelData;
    }
    
    /**
     * 查询间接关系，多关系
     * 
     * @param sb
     * @param startQuery
     * @param datas
     * @param endName
     * @param endMeta
     */
    public void queryRelPathEnds(StringBuilder sb, String startQuery, List<Map<String, Object>> datas, String endName,
	    Map<String, Object> endMeta, Map<String, Object> context) {
	String queryData;
	Boolean hasPermission = endName.contains("权限");
	if (hasPermission) {// 权限类型的关系数据，某一大类的关系，多个类型的关系遍历查询。
	    LoggerTool.info(logger,"hasPermission:" + endName);
	    queryEndsOfAllRelationType(sb, startQuery, datas, endMeta, context);
	} else {// 不是权限类型的关系，终点明确，起点明确。

	    // START n=node(*),m=node(*)
	    // MATCH p=n-[r*1..]-m
	    // WITH count(p) AS totalPaths,n,m
	    // WHERE totalPaths>1
	    // RETURN n,m,totalPaths
	    // LIMIT 2

	    // 间接关系、属性
	    Map<String, Object> data2 = getData(startQuery, context);
	    if (data2 == null) {
		return;
	    }
	    LoggerTool.info(logger,"getData:" + mapString(data2));
	    queryData = "MATCH (n)-[r*2..3]-(m:" + label(endMeta) + ")  where id(n)=" + id(data2);
	    if (label(endMeta).equals(META_DATA)) {
		queryData = queryData + " and m.label<>'" + META_DATA + "'";
	    }

	    queryData = queryData + " return distinct m ";

	    List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
	    if (auths.size() > 0) {
		for (Map<String, Object> ai : auths) {
		    ai.put("HAS_", name(ai));
		    datas.add(ai);
		}
	    }
	    if (datas != null && datas.size() > 0) {
		seeEndNode(sb, datas);
	    }

	}
    }

    /**
     * 
     * @param sb
     * @param startQuery
     * @param datas
     * @param endMeta    类型：关联终点的关系类型的定义元数据
     */
    public void queryEndsOfAllRelationType(StringBuilder sb, String startQuery, List<Map<String, Object>> datas,
	    Map<String, Object> endMeta, Map<String, Object> context) {
	String queryData = "MATCH (m:" + label(endMeta) + ") return distinct m";

	Map<String, Object> data2 = getData(startQuery, context);
	if (data2 == null) {
	    return;
	}
	List<Map<String, Object>> ends = neo4jUService.cypher(queryData);
	for (Map<String, Object> ei : ends) {
	    List<Map<String, Object>> authOfRi = new ArrayList<>();
	    String endCode = code(ei);
	    boolean endCodeNotNull = endCode != null && !"null".equals(endCode);
	    queryData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + META_DATA + ")  where id(n)=" + id(data2);
	    if (endCodeNotNull) {
		queryData = queryData + " OR r.code contains '" + endCode + "'";
	    }

	    queryData = queryData + " return distinct m ";

	    List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
	    if (auths != null && !auths.isEmpty()) {
		authOfRi.addAll(auths);
	    }

	    queryData = "MATCH (n)-[r:HAS_PERMISSION]->(ro:Role)-[r1:HAS_PERMISSION]->(m:" + META_DATA
		    + ")  where  id(n)=" + id(data2);

	    if (endCodeNotNull) {
		queryData = queryData + " AND r1.code contains '" + endCode + "'";
	    }

	    queryData = queryData + " return distinct m ";
	    List<Map<String, Object>> auth2s = neo4jUService.cypher(queryData);
	    if (auth2s != null && !auth2s.isEmpty()) {
		authOfRi.addAll(auth2s);
	    }
	    if (authOfRi.size() > 0) {
		for (Map<String, Object> ai : authOfRi) {
		    ai.put("HAS_PERMISSION", name(ei));
		    datas.add(ai);
		}
	    }
	}
	if (datas.size() > 0) {
	    seeEndNodeAuth(sb, datas);
	}
    }
    
    /**
     * //党员(元数据)有xx的哪些xx：xx有待办的那些权限
     * 
     * @param sb
     * @param usedOkQuery
     * @param si
     * @param datas
     * @param startName
     * @param endName
     * @param por
     * @return
     */
    public List<Map<String, Object>> queryPropOrRelOf(StringBuilder sb, String usedOkQuery, String si, String startName,
	    String endName, String por, Map<String, Object> context) {
	List<Map<String, Object>> datas = new ArrayList<>();
	List<Map<String, Object>> endMetas = getMetaDataByName(endName);
	String dataLabel = null;
	if (containLabelInfo(startName)) {// 带元数据信息的开始节点
	    LoggerTool.info(logger,"startName containLabelInfo:" + startName);
	    Map<String, Object> onlyContext = new HashMap<>();
	    startName = onlyName(onlyContext, startName);
	    dataLabel = string(onlyContext, "dataLabel");
	}
	Map<String, Object> selectedOne = selectedData(context, startName);
	LoggerTool.info(logger,"startName:{} metaInfo:{} ,selectedOne:{}", startName, dataLabel, mapString(selectedOne));
	for (Map<String, Object> oMdi : endMetas) {
	    if (datas.size() > 0) {
		return datas;
	    }
	    Map<String, String> nameColumn = nameColumn(oMdi);
	    String coli = null;
	    if (nameColumn.get(por) != null) {
		coli = nameColumn.get(por);
	    }
	    if (coli != null) {// 获取自定义字段，关联字段数据信息
		seeProperty(sb, startName, coli, oMdi, context);
	    } else {// 查询关系
		if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
		    continue;
		}

		Long startId = id(selectedOne);
		if (datas.isEmpty()) {
		    String relQuery = "MATCH (n)-[r]->(m:" + label(oMdi) + ") WHERE id(n)=" + startId
			    + "  AND r.name = '" + por + "' or r.code='" + por + "'  return r ";
		    if (dataLabel != null) {
			relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:" + label(oMdi) + ") WHERE id(n)=" + startId
				+ " AND r.name = '" + por + "' or r.code='" + por + "'  return r ";
		    }
		    collectData(sb, datas, relQuery);
		}
		if (datas.isEmpty()) {

		    String relQuery = "MATCH (n)-[r]->(m:MetaData) WHERE id(n)=" + startId + " and( r.name = '" + por
			    + "' OR r.code='" + por + "' and m.label='" + label(oMdi) + "'  RETURN  r ";
		    if (dataLabel != null) {
			relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:MetaData) WHERE id(n)=" + startId
				+ " and( r.name = '" + por + "' OR r.code='" + por + "' and m.label='" + label(oMdi)
				+ "'  RETURN  r ";
		    }
		    collectData(sb, datas, relQuery);
		}

		if (datas.isEmpty()) {
		    List<Map<String, Object>> metaDataByName2 = getMetaDataByName(por);
		    for (Map<String, Object> ri : metaDataByName2) {
			List<Map<String, Object>> listAllByLabel = neo4jUService.listAllByLabel(label(ri));
			for (Map<String, Object> rdatai : listAllByLabel) {
			    String relQuery = "MATCH (n)-[r]->(m:MetaData) WHERE id(n)=" + startId + " and  r.code='"
				    + code(rdatai) + "' and m.label='" + label(oMdi) + "'  RETURN  r ";
			    if (dataLabel != null) {
				relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:MetaData) WHERE id(n)=" + startId
					+ " and  r.code='" + code(rdatai) + "' and m.label='" + label(oMdi)
					+ "'  RETURN  r ";
			    }
			    List<Map<String, Object>> datasi = neo4jUService.cypher(relQuery);
			    if (datasi != null && !datasi.isEmpty()) {
				datas.addAll(datasi);
				if (sb.length() > 0) {
				    sb.append("、");
				}
				sb.append(name(rdatai));
			    }
			}
		    }
		}
	    }
	}
	return datas;
    }
    
    public void seeRelation(StringBuilder sb, String si, String startName, Map<String, Object> userContext) {

	if (containLabelInfo(startName)) {// 包含Label信息
	    LoggerTool.info(logger,"startName contains MetaInfo :{}", startName);
	    Map<String, Object> onlyContext = new HashMap<>();
	    startName = onlyName(onlyContext, startName);
	    String dataLabel = string(onlyContext, "dataLabel");
	    Map<String, Object> oneData = getData(startName, dataLabel, userContext);
	    if (oneData != null && !oneData.isEmpty()) {
		String relation = "MATCH(n:" + dataLabel + ")-[r]-(m) where id(n)=" + id(oneData)
			+ " return type(r) AS rCode,r.name AS rName,m";
		List<Map<String, Object>> itsRelation = neo4jUService.cypher(relation);
		for (Map<String, Object> ri : itsRelation) {
		    String rName = string(ri, "rName");
		    if (rName != null && rName.contains(si)) {
			if (sb.length() > 0) {
			    sb.append("、");
			}
			sb.append(name(ri));
		    }
		}
	    }
	} else {
	    Map<String, Object> oneData = selectedData(userContext, startName);
	    if (oneData != null && !oneData.isEmpty()) {
		LoggerTool.info(logger,"startName:{} is {}", startName, mapString(oneData));

		String relation = "MATCH(n)-[r]-(m) where id(n)=" + id(oneData)
			+ " return type(r) AS rCode,r.name AS rName,m";
		List<Map<String, Object>> itsRelation = neo4jUService.cypher(relation);
		for (Map<String, Object> ri : itsRelation) {
		    String rName = string(ri, "rName");
		    if (rName != null && rName.contains(si)) {
			if (sb.length() > 0) {
			    sb.append("、");
			}
			sb.append(name(ri));
		    }
		}
	    }
	}
    }
}

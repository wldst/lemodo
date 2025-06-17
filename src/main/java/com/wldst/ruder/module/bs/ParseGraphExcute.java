package com.wldst.ruder.module.bs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.LoggerTool;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.domain.VoiceOperateDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.parse.handle.BeanShellHandle;
import com.wldst.ruder.util.CrudUtil;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * 直接读取图数据，然后根据查询条件返回数据。
 * 联调多的时候是否，按路径，返回起点确认。
 * 或者终点确认。
 * 关系确认。
 */
@Component
public class ParseGraphExcute extends VoiceOperateDomain {
    final static Logger logger = LoggerFactory.getLogger(ParseGraphExcute.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUserNeo4jService neo4jUService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudUtil crudUtil; 
    @Autowired
    private BeanShellHandle bsHandle;
	@Autowired
	private RelationService relationService;
    private Map<String, Map<String, Object>> context = new HashMap<>();
    List<String> stackQuit = Arrays.asList("退出", "返回", "返回上一级");// 唤醒词
    List<String> operateStack = Arrays.asList("操作", "使用", "进入", "得到");// 唤醒词

    List<String> getUseWords = Arrays.asList("操作", "使用", "进入", "打开", "获取", "得到", "获取最新的", "关于", "拿到", "找到");
    // 修改前缀
    List<String> newUpdate = Arrays.asList("把", "将", "被", "修改", "更新", "update");

    List<String> updates = Arrays.asList("保存", "更新", "update", "save");
    List<String> auth = Arrays.asList("给", "将");
    List<String> authAdd = Arrays.asList("添加", "增加", "授予", "授权");
    // 新的关系
    List<String> newRelation = Arrays.asList("创建关系", "新增关系", "添加关系", "添加联系", "新建关系", "有关系", "更新关系");
    // 新建节点
    List<String> newNode = Arrays.asList("创建", "新增", "新建", "添加", "有", "new");

    List<String> manageNode = Arrays.asList("管理", "处理", "列表", "查询");

    // 所属
    List<String> ownWords = Arrays.asList("的", "地", "得", "所属", "隶属的");
    // 动词读取关系
    List<String> actionWords = Arrays.asList("做", "干", "读", "听", "说", "学", "想", "写", "完成", "接龙");
    // 谓词
    List<String> kEqualv = Arrays.asList("是", "有", "等于", "叫", "为", "=");
    // 获取关系，修改关系属性
    List<String> relProp = Arrays.asList("关系属性");
    List<String> relName = Arrays.asList("朋友", "孩子", "父亲", "上级", "下级", "后序", "前序");
    // 是某某关系
    List<String> isRel = Arrays.asList("是", "在", "一起", "一同", "俩", "两个");
    List<String> andRel = Arrays.asList("和", "跟", "与", "、", " AND ", " and ", " && ");
    List<String> between = Arrays.asList("之间的");
    // 删除信息
    List<String> removes = Arrays.asList("删除", "去除", "注销", "清理", "清除", "处理掉", "delete", "remove");
    List<String> relationDel = Arrays.asList("禁止", "删除", "注销", "清除", "去掉", "去除", "delete", "remove", "del");
    private static String zuoKuohao = "(";
    private static String cnLeftKuoHao = "（";
    private static String rightKuohao = ")";
    private static String cnRightKuoHao = "）";
 
    public Long getId(String label, String key, String value) {
	return neo4jService.getNodeId(key, value, label);
    }

    public Map<String, Object> getNode(String label, String key, String value) {
	return neo4jService.getAttMapBy(key, value, label);
    }
    /**
     * 自定义句式，句式识别
     * 
     * @param voiceInfo
     */
    public void parseText(Map<String, Object> voiceInfo) {
	String commandText = string(voiceInfo, "text");
	parseAndExcute(commandText);
    }

    public String formatCmd(String commandText) {
	JSONArray parseArray = JSON.parseArray(commandText);
	StringBuilder cmds = new StringBuilder();
	for (Object oi : parseArray.toArray()) {
	    JSONObject joi = (JSONObject) oi;
	    String string = joi.getString("onebest");
	    cmds.append(string);
	}
	try {
	    String string = clearVoiceWord(cmds.toString());
	    return string;
	} catch (Exception e) {
	    e.printStackTrace();
	    return "会话异常";
	}
    }

    /**
     * 替换字段所有匹配
     * 
     * @param label 标签
     * @param field 字段
     * @param from  源
     * @param to    目标
     */
    public void replaceAll(String label, String field, String from, String to) {
	String cypher = "Match(n:" + label + ") where n." + field + " CONTAINS '" + from + "' return n";
	List<Map<String, Object>> query = neo4jService.cypher(cypher);
	for (Map<String, Object> mi : query) {
	    String fieldContent = string(mi, field);
	    String newValue = fieldContent.replaceAll(from, to);
	    if (!newValue.equals(fieldContent)) {
		Map<String, Object> copyWithKeys = copyWithKeys(mi, field);
		copyWithKeys.put(field, newValue);
		neo4jService.update(copyWithKeys, id(mi));
	    }
	}
    }

    /**
     * 替换单个字段的内容
     * 
     * @param label 标签
     * @param field 字段
     * @param from  源
     * @param to    目标
     */
    public void replace(String label, String field, String from, String to) {
	String cypher = "Match(n:" + label + ") return n";
	List<Map<String, Object>> query = neo4jService.cypher(cypher);
	if (query == null) {
	    return;
	}
	for (Map<String, Object> mi : query) {
	    String fieldContent = string(mi, field);
	    // from=from.replaceAll("\\.","\\\\.");
	    String newValue = fieldContent.replace(from, to);
	    if (!newValue.equals(fieldContent)) {
		Map<String, Object> copyWithKeys = copyWithKeys(mi, field);
		copyWithKeys.put(field, newValue);
		neo4jService.update(copyWithKeys, id(mi));
	    }
	}
    }

    public void replaceValue(String label, String field, String from, String to) {
	if (from.equals(to)) {
	    return;
	}
	String cypher = "Match(n:" + label + "{\"" + field + "\":\"" + from + "\"}) return n";
	List<Map<String, Object>> query = neo4jService.cypher(cypher);
	if (query == null) {
	    return;
	}
	for (Map<String, Object> mi : query) {
	    if (!from.equals(to)) {
		Map<String, Object> copyWithKeys = copyWithKeys(mi, field);
		copyWithKeys.put(field, to);
		neo4jService.update(copyWithKeys, id(mi));
	    }
	}
    }

    /**
     * 解析命令自然语言，中文 第一步：翻译语句，转换为节点关系的操作。返回前端，点击确认。 翻译的结果存入命令执行记录中。记录操作人信息。谁发送的需求。
     * 第二步，根据确认信息，直接执行相关语句，并返回结果。查询直接查询，删除，更新需要进行确认。
     * 
     * @param commandText
     * @return
     */
    public Map<String, Object> parseTalkMsg(String commandText) {
	Map<String, Object> parseAndexcute = new HashMap<>();
	try {
	    String msg = clearVoiceWord(commandText);
		// huoq唤醒词：给{用户或者角色}{添加},什么的什么权限，
		// 将{用户或者角色}的什么的什么权限,删除。
	    Map<String,Object> myContext = new HashMap<>();
	    Object returnValue = bsHandle.parse(msg, myContext);
	    
	    if(returnValue!=null) {
		 recordExcute(commandText, returnValue);
		 parseAndexcute.put("status", "200");
		 parseAndexcute.put("data", returnValue);
		 return parseAndexcute;
	    }
	    
	    List<Map<String, Object>> parseAuthTalkAndexcute = parseAuthTalkAndexcute(commandText,
		    adminService.getCurrentPasswordId() + "");
	    parseAndexcute.put("status", "200");
	    parseAndexcute.put("data", parseAuthTalkAndexcute);
	    if(!parseAuthTalkAndexcute.isEmpty()) {
		 recordExcute(commandText, parseAuthTalkAndexcute);
	    } 
	    return parseAndexcute;
	} catch (Exception e) {
	    e.printStackTrace();
	    parseAndexcute.put("msg", "会话异常");
	    parseAndexcute.put("status", "error");
	    parseAndexcute.put("error", e.getMessage());
	    return parseAndexcute;
	}
    }

    public void recordExcute(String commandText, Object parseAuthTalkAndexcute) {
	Map<String,Object> ret = new HashMap<>();
	    ret.put("sentence", commandText);
	    ret.put(STATUS, "200");
	    ret.put("result", parseAuthTalkAndexcute);
	    ret.put("userName", adminService.getCurrentName());
	    neo4jUService.saveByBody(ret, "ChatSentence");
    }

    public Map<String, Object> parseAndExcute(String commandText) {
	Map<String, Object> parseAndexcute = new HashMap<>();
	if (commandText.contains("{") && commandText.contains(":") && commandText.contains("}")) {
	    JSONArray parseArray = JSON.parseArray(commandText);
	    StringBuilder cmds = new StringBuilder();
	    for (Object oi : parseArray.toArray()) {
		JSONObject joi = (JSONObject) oi;
		String string = joi.getString("onebest");
		cmds.append(string);
	    }
	    try {
		Long currentUserId = adminService.getCurrentPasswordId();
		parseAndexcute(cmds.toString(), currentUserId + "");
	    } catch (Exception e) {
		e.printStackTrace();
		parseAndexcute.put("msg", "会话异常");
		parseAndexcute.put("status", "error");
		parseAndexcute.put("error", e.getMessage());
		return parseAndexcute;
	    }
	} else {
	    try {
		parseAndexcute = parseAndexcute(commandText, adminService.getCurrentPasswordId() + "");
		return parseAndexcute;
	    } catch (Exception e) {
		parseAndexcute.put("msg", "会话异常");
		parseAndexcute.put("status", "error");
		parseAndexcute.put("error", e.getMessage());
		return parseAndexcute;
	    }
	}
	return null;
    }

    /**
     * 解析权限命令
     * 
     * @param message
     * @param sessionId
     * @return
     */
    public List<Map<String, Object>> parseAuthTalkAndexcute(String message, String sessionId) {
	List<Map<String, Object>> handleResult = new ArrayList<>();
	Map<String, Object> context = new HashMap<>();
	context.put("used", false);
	// 替换掉声音助词
	String msg = clearVoiceWord(message);
	// huoq唤醒词：给{用户或者角色}{添加},什么的什么权限，
	// 将{用户或者角色}的什么的什么权限,删除。
	
	
	handleStartWithGei(msg, context);
	if (context.get("used").equals(true)) {
	    return handleResult;
	}
	handleStartWithJiang(msg, context);
	if (context.get("used").equals(true)) {
	    return handleResult;
	}
	// 默认的唤醒词
	handleResult = handleStartOpen(msg, context);
	if (context.get("used").equals(true) || handleResult != null && !handleResult.isEmpty()) {
	    return handleResult;
	}
	handleResult = handleStartManage(msg, context);
	if (context.get("used").equals(true) || handleResult != null && !handleResult.isEmpty()) {
	    return handleResult;
	} else {
	    handleResult = new ArrayList<>();
	}
	// 处理增删改查元数据，实例数据，处理增删改查关系数据
	handleStartCreateRel(msg, context);
	handleDelete(msg, context);
	handleUpdate(msg, context);
	if (context.get("used").equals(true)) {
	    handleResult.add(result("\n" + msg + "已执行\n"));
	    return handleResult;
	}
	// 查询
	String someOf = querySomeOfA(msg, context);
	if (context.get("used").equals(true) || someOf != null && !someOf.isBlank()) {
	    handleResult.add(result(someOf));
	    return handleResult;
	}

	if (msg.endsWith("\\?") || msg.endsWith("？")) {
	    String handleAandB = queryAandB(msg, context);
	    if (context.get("used").equals(true) || handleAandB != null && !handleAandB.isBlank()) {
		handleResult.add(result(handleAandB));
		return handleResult;
	    }
	}

	handleResult.add(result(msg));
	return handleResult;
    }

    /**
     * 打开xx, 打开xx（元数据信息）
     * 
     * @param msg
     * @param context
     * @return
     */
    public List<Map<String, Object>> handleStartOpen(String msg, Map<String, Object> context) {
	List<Map<String, Object>> data = new ArrayList<>();
	String prefix = "打开";
	if (msg.startsWith(prefix)) {// 根据角色权限，账号权限，来确定打开范围
	    context.put("used", true);
	    msg = msg.replaceFirst(prefix, "");
	    if (containLabelInfo(msg)) {
		String[] split = msg.split("\\(");
		if (split.length < 2) {
		    split = msg.split(cnLeftKuoHao);
		}
		String meta = null;
		if (split[1].endsWith(rightKuohao)) {
		    meta = split[1].replace(rightKuohao, "");
		} else if (split[1].endsWith(cnRightKuoHao)) {
		    meta = split[1].replace(cnRightKuoHao, "");
		}
		List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
		for (Map<String, Object> mi : metaDataByName) {
		    List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
		    if (dataBy != null) {
			for (Map<String, Object> di : dataBy) {
			    di.put("name", name(di) + "（" + name(mi) + "）");
			    processOpenData(label(mi), data, di);
			}
		    }
		}
	    } else {
		handleOpen(msg, "resource", data);
		if (data.isEmpty()) {
		    handleOpen(msg, "App", data);
		}
	    }
	}
	return data;
    }

    public List<Map<String, Object>> handleStartManage(String msg, Map<String, Object> context) {
	List<Map<String, Object>> data = new ArrayList<>();
	String prefix = "管理";
	if (msg.startsWith(prefix)) {// 根据角色权限，账号权限，来确定打开范围
	    handleManage(msg, META_DATA, data, prefix);
	}
	return data;
    }

    public List<Map<String, Object>> handleStartExcute(String msg, Map<String, Object> context) {
	List<Map<String, Object>> data = new ArrayList<>();
	String prefix = "执行";
	if (msg.startsWith(prefix)) {// 执行脚本，根据名称或者code来查找脚本
	    handleExecute(msg, data, prefix);
	}
	return data;
    }

    public String queryAandB(String msg, Map<String, Object> context) {
	StringBuilder sb = new StringBuilder();
	msg = msg.trim().replaceAll("\\?", "").replaceAll("？", "");
	List<String> query = new ArrayList<>();
	query.add("有什么关系");
	query.add("有什么");
	query.add("是什么关系");
	query.add("是否可达");
	for (String si : query) {
	    if (msg.endsWith(si)) {// 根据角色权限，账号权限，来确定打开范围
		context.put("used", true);
		String string = msg.split(si)[0];
		String handleABPath = handleABPath(string);
		if (handleABPath != null) {
		    sb.append(handleABPath);
		}
	    }
	}
	return sb.toString();
    }

    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    public String querySomeOfA(String msg, Map<String, Object> context) {
	StringBuilder sb = new StringBuilder();
	msg = msg.trim().replaceAll("\\?", "").replaceAll("？", "");
	// 并行概率执行
	List<String> query = getRelationQuery();

	String startQuery = null;
	String usedOkQuery = null;
	for (String si : query) {
	    if (!bool(context, "used") && sb.length() < 1 && msg.contains(si)) {
		if (usedOkQuery != null && usedOkQuery.startsWith(si)) {
		    break;
		}
		context.put("used", true);
		String[] queryPart = msg.split(si);
		startQuery = queryPart[0];
		if(queryPart.length>1) {
		    context.put("answer", startQuery + si.substring(0, 1) + queryPart[1]);
		}else {
		    context.put("answer", startQuery + si.substring(0, 1) );
		}
		

		List<Map<String, Object>> datas = null;
		// xx的xx是，xx的ss有
		for (String owni : ownWords) {
		    if (startQuery.contains(owni)) {
			context.put("hasOwni", true);
			
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
				datas = queryPropOrRelOf(sb, usedOkQuery, si, startName, endName, por);
			    }
			}

			if (!bool(context, "rightHasOwni")) {
			    // xx有哪些ss
			    if (queryPart[1].startsWith("哪些")) {
				endName = replaceQueryWord(queryPart[1]);
			    }
			    context.put("endName", queryPart[1]);
			    if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
				continue;
			    }
			    endName = string(context, "endName");
			    endName = endName.trim().replaceAll("\\?", "").replaceAll("？", "");
			    if (endName.length() > 1) {
				Map<String, Object> endMeta = getData(endName, META_DATA);
				if (endMeta != null) {
				    String queryData = null;
				    if (containLabelInfo(startName)) {
					Map<String, Object> onlyContext = new HashMap<>();
					startName = onlyName(onlyContext, startName);
					String dataLabel = string(onlyContext, "dataLabel");
					// Map<String, Object> mapObject = mapObject(onlyContext, "dataMd");

					if (msg.endsWith("有哪些角色")) {
					    if (dataLabel != null) {
						queryData = "MATCH (n:" + dataLabel + ")-[r:HAS_PERMISSION]-(m:"
							+ label(endMeta) + ")  where n.name='" + startQuery
							+ "'  return distinct m ";
					    } else {
						queryData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + label(endMeta)
							+ ")  where n.name='" + startQuery + "'  return distinct m ";
					    }
					} else {
					    if (dataLabel != null) {
						queryData = "MATCH (n:" + dataLabel + ")-[r]-(m:" + label(endMeta)
							+ ")  where n.name='" + startQuery + "'  return distinct m ";
					    } else {
						queryData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where n.name='"
							+ startQuery + "'  return distinct m ";
					    }
					}
				    } else {
					if (msg.endsWith("有哪些角色")) {

					    queryData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + label(endMeta)
						    + ")  where n.name='" + startQuery + "'  return distinct m ";

					} else {

					    queryData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where n.name='"
						    + startQuery + "'  return distinct m ";
					}
				    }

				    datas = neo4jUService.cypher(queryData);
				    if (datas != null && datas.size() > 0) {
					seeEndNode(sb, datas);
				    } else {
					queryRelationEnds(sb, startQuery, datas, endName, endMeta);
				    }
				} else {
				    seeRelation(sb, endName, startName);
				}
			    } else {
				seeRelation(sb, endName, startName);
			    }
			}
		    } else {
			seeRelation(sb, endName, startName);
			datas = neo4jUService
				.cypher("MATCH (n)-[r*1..3]->(m) WHERE n.name='" + startQuery + "'  RETURN distinct m ");
			seeEndNode(sb, datas);
		    }
		}

		if (datas != null && !datas.isEmpty()) {
		    usedOkQuery = si;
		}
	    }
	}

	if (sb.toString().length() < 1) {
	    return string(context, "answer") + "，答：系统中无此信息\n";
	}

	String answerQ = "\n" + string(context, "answer") + ":<BR>";
	if (bool(context, "hasOwni")) {
	    answerQ = "\n" + string(context, "answer") + ":\n";
	}
	return answerQ + sb.toString();
    }

    public String onlyName(Map<String, Object> context, String startName) {
	String name = getMetaInfo(startName, zuoKuohao, context);
	if (startName.equals(name)) {
	    name = getMetaInfo(startName, cnLeftKuoHao, context);
	}
	return name;
    }
    /**
     * 获取某些对象的某些资源
     * @param sb
     * @param startQuery
     * @param datas
     * @param endName
     * @param endMeta
     */
    public void getSrOfSb(StringBuilder sb, String startQuery, List<Map<String, Object>> datas, String endName,
	    Map<String, Object> endMeta) {
	String queryData;
	Boolean hasPermission = endName.contains("权限");
	queryData = "MATCH (m:" + label(endMeta) + ") return distinct m";
	List<Map<String, Object>> ends = neo4jUService.cypher(queryData);

	if (hasPermission) {
	    for (Map<String, Object> ei : ends) {
		List<Map<String, Object>> authOfRi = new ArrayList<>();
		queryData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + META_DATA + ")  where n.name='" + startQuery
			+ "' and r.code='" + code(ei) + "' return distinct m ";
		List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
		if (auths != null && !auths.isEmpty()) {
		    authOfRi.addAll(auths);
		}

		queryData = "MATCH (n)-[r:HAS_PERMISSION]->(ro:Role)-[r1:HAS_PERMISSION]->(m:" + META_DATA
			+ ")  where n.name='" + startQuery + "' and r1.code=\"" + code(ei) + "\"  return distinct m ";
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
	} else {
	    for (Map<String, Object> ri : ends) {
		List<Map<String, Object>> itsPropRel = new ArrayList<>();
		queryData = "MATCH (n)-[r]-(m:" + META_DATA + ")  where n.name='" + startQuery + "' and r.code='"
			+ code(ri) + "' return distinct m ";
		List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
		if (auths != null && !auths.isEmpty()) {
		    itsPropRel.addAll(auths);
		}

		queryData = "MATCH (n)-[r]->(ro:Role)-[r1]->(m:" + META_DATA + ")  where n.name='" + startQuery
			+ "' and r1.code=\"" + code(ri) + "\"  return distinct m ";
		List<Map<String, Object>> auth2s = neo4jUService.cypher(queryData);
		if (auth2s != null && !auth2s.isEmpty()) {
		    itsPropRel.addAll(auth2s);
		}
		if (itsPropRel.size() > 0) {
		    for (Map<String, Object> ai : itsPropRel) {
			ai.put("HAS_", name(ri));
			datas.add(ai);
		    }
		}
	    }
	    if (datas.size() > 0) {
		seeEndNodePropRel(sb, datas);
	    }
	}
    }

    public void queryRelationEnds(StringBuilder sb, String startQuery, List<Map<String, Object>> datas, String endName,
	    Map<String, Object> endMeta) {
	String queryData;
	Boolean hasPermission = endName.contains("权限");
	queryData = "MATCH (m:" + label(endMeta) + ") return distinct m";
	List<Map<String, Object>> ends = neo4jUService.cypher(queryData);

	if (hasPermission) {
	    for (Map<String, Object> ei : ends) {
		List<Map<String, Object>> authOfRi = new ArrayList<>();
		queryData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + META_DATA + ")  where n.name='" + startQuery
			+ "' and r.code='" + code(ei) + "' return distinct m ";
		List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
		if (auths != null && !auths.isEmpty()) {
		    authOfRi.addAll(auths);
		}

		queryData = "MATCH (n)-[r:HAS_PERMISSION]->(ro:Role)-[r1:HAS_PERMISSION]->(m:" + META_DATA
			+ ")  where n.name='" + startQuery + "' and r1.code=\"" + code(ei) + "\"  return distinct m ";
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
	} else {
	    for (Map<String, Object> ri : ends) {
		List<Map<String, Object>> itsPropRel = new ArrayList<>();
		queryData = "MATCH (n)-[r]-(m:" + META_DATA + ")  where n.name='" + startQuery + "' and r.code='"
			+ code(ri) + "' return distinct m ";
		List<Map<String, Object>> auths = neo4jUService.cypher(queryData);
		if (auths != null && !auths.isEmpty()) {
		    itsPropRel.addAll(auths);
		}

		queryData = "MATCH (n)-[r]->(ro:Role)-[r1]->(m:" + META_DATA + ")  where n.name='" + startQuery
			+ "' and r1.code=\"" + code(ri) + "\"  return distinct m ";
		List<Map<String, Object>> auth2s = neo4jUService.cypher(queryData);
		if (auth2s != null && !auth2s.isEmpty()) {
		    itsPropRel.addAll(auth2s);
		}
		if (itsPropRel.size() > 0) {
		    for (Map<String, Object> ai : itsPropRel) {
			ai.put("HAS_", name(ri));
			datas.add(ai);
		    }
		}
	    }
	    if (datas.size() > 0) {
		seeEndNodePropRel(sb, datas);
	    }
	}
    }

    /**
     * xx的xx是，xx的ss有
     * 
     * @param context
     * @param sb
     * @param startQuery
     * @param usedOkQuery
     * @param si
     * @param owni
     * @return
     */
    public List<Map<String, Object>> queryPropRelOf(Map<String, Object> context, StringBuilder sb, String startQuery,
	    String usedOkQuery, String si, String owni) {
	List<Map<String, Object>> datas = new ArrayList<>();
	String[] owns = startQuery.split(owni);
	String startName = owns[0];
	String por = owns[1];// 获取元数据信息
	if (por != null) {
	    context.put("endName", por);
	}

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
		seeProperty(sb, startName, coli, mapObject);
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
		    Map<String, Object> endMeta = getData(por, META_DATA);
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
		    seeProperty(sb, startName, coli, oMdi);
		} else {// 查询关系
		    if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
			continue;
		    }
		    if (datas == null) {
			datas = neo4jUService
				.cypher("MATCH (n)-[r]->(m) WHERE r.name = '" + por + "'  return distinct m ");
			seeEndNode(sb, datas);
		    }
		    if (datas == null) {
			Map<String, Object> endMeta = getData(por, META_DATA);
			datas = neo4jUService.cypher(
				"MATCH (n)-[r]->(m:" + label(endMeta) + ") WHERE r.name = '" + por + "'  RETURN m ");
			seeEndNode(sb, datas);
		    }
		}
	    }
	}

	return datas;
    }

    /**
     * //党员(元数据)有xx的哪些xx：xx有待办的那些权限
     * 
     * @param sb
     * @param usedOkQuery
     * @param si
     * @param startName
     * @param endName
     * @param por
     * @return
     */
    public List<Map<String, Object>> queryPropOrRelOf(StringBuilder sb, String usedOkQuery, String si, String startName,
	    String endName, String por) {
	List<Map<String, Object>> datas = new ArrayList<>();
	List<Map<String, Object>> endMetas = getMetaDataByName(endName);
	String dataLabel = null;
	if (containLabelInfo(startName)) {// 带元数据信息的开始节点
	    Map<String, Object> onlyContext = new HashMap<>();
	    startName = onlyName(onlyContext, startName);
	    dataLabel = string(onlyContext, "dataLabel");
	}

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
		seeProperty(sb, startName, coli, oMdi);
	    } else {// 查询关系
		if (usedOkQuery != null && si.startsWith(usedOkQuery)) {
		    continue;
		}

		if (datas.isEmpty()) {
		    String relQuery = "MATCH (n)-[r]->(m:" + label(oMdi) + ") WHERE n.name='" + startName
			    + "' AND r.name = '" + por + "' or r.code='" + por + "'  return r ";
		    if (dataLabel != null) {
			relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:" + label(oMdi) + ") WHERE n.name='" + startName
				+ "' AND r.name = '" + por + "' or r.code='" + por + "'  return r ";
		    }
		    collectData(sb, datas, relQuery);
		}
		if (datas.isEmpty()) {

		    String relQuery = "MATCH (n)-[r]->(m:MetaData) WHERE n.name='" + startName + "' and( r.name = '"
			    + por + "' OR r.code='" + por + "' and m.label='" + label(oMdi) + "'  RETURN  r ";
		    if (dataLabel != null) {
			relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:MetaData) WHERE n.name='" + startName
				+ "' and( r.name = '" + por + "' OR r.code='" + por + "' and m.label='" + label(oMdi)
				+ "'  RETURN  r ";
		    }
		    collectData(sb, datas, relQuery);
		}

		if (datas.isEmpty()) {
		    List<Map<String, Object>> metaDataByName2 = getMetaDataByName(por);
		    for (Map<String, Object> ri : metaDataByName2) {
			List<Map<String, Object>> listAllByLabel = neo4jUService.listAllByLabel(label(ri));
			for (Map<String, Object> rdatai : listAllByLabel) {
			    String relQuery = "MATCH (n)-[r]->(m:MetaData) WHERE n.name='" + startName
				    + "' and  r.code='" + code(rdatai) + "' and m.label='" + label(oMdi)
				    + "'  RETURN  r ";
			    if (dataLabel != null) {
				relQuery = "MATCH (n:" + dataLabel + ")-[r]->(m:MetaData) WHERE n.name='" + startName
					+ "' and  r.code='" + code(rdatai) + "' and m.label='" + label(oMdi)
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

    public void collectData(StringBuilder sb, List<Map<String, Object>> datas, String relQuery) {
	List<Map<String, Object>> datasi = neo4jUService.cypher(relQuery);
	if (datasi != null && !datasi.isEmpty()) {
	    datas.addAll(datasi);
	    seeEndNode(sb, datasi);
	}
    }

    public String replaceQueryWord(String por) {
	if (por.startsWith("哪些")) {

	    por = por.replaceFirst("哪些", "");
	}
	return por;
    }

    /**
     * 根据名称获取元数据信息
     * 
     * @param metaName
     * @return
     */
    public List<Map<String, Object>> getMetaDataByName(String metaName) {
	List<Map<String, Object>> ownerMetas;

	String getMetaInfo = " MATCH (m:MetaData) where  m.name='" + metaName + "'  return distinct m";
	ownerMetas = neo4jUService.cypher(getMetaInfo);
	if (ownerMetas != null && !ownerMetas.isEmpty()) {
	    return ownerMetas;
	}
	getMetaInfo = "MATCH (n) WHERE n.name = '" + metaName + "' unwind labels(n) AS x return x";
	ownerMetas = neo4jUService.cypher(getMetaInfo);
	List<Object> obs = new ArrayList<>();
	for (Map<String, Object> omi : ownerMetas) {
	    String labeli = string(omi, "x");
	    obs.add(labeli);
	}

	if (!obs.isEmpty()) {
	    getMetaInfo = " MATCH (m:MetaData) where  m.label in (" + joinStr(obs) + " ) return distinct m";
	    ownerMetas = neo4jUService.cypher(getMetaInfo);
	}

	return ownerMetas;
    }
    public Map<String, String> getMetaData() {
	String getMetaInfo = " MATCH (m:MetaData) where  m.label='" + META_DATA + "'  return distinct m";
	List<Map<String, Object>> ownerMetas = neo4jService.cypher(getMetaInfo);
	if (ownerMetas != null && !ownerMetas.isEmpty()) {
	    return nameColumn(ownerMetas.get(0));
	}
	return null;
    }

    public void seeRelation(StringBuilder sb, String si, String startName) {

	if (containLabelInfo(startName)) {
	    Map<String, Object> onlyContext = new HashMap<>();
	    startName = onlyName(onlyContext, startName);
	    String dataLabel = string(onlyContext, "dataLabel");

	    Map<String, Object> oneData = getData(startName, dataLabel);
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
	    Map<String, Object> oneData = getOneData(startName);
	    if (oneData != null && !oneData.isEmpty()) {
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

    public List<String> getRelationQuery() {
	List<String> query = new ArrayList<>();
	query.add("可以访问哪些");
	query.add("能访问哪些");
	query.add("有哪些");
	query.add("有什么");
	query.add("有多少");
	query.add("是什么");
	query.add("是多少");
	query.add("是哪些");
	query.add("是");
	query.add("有");
	return query;
    }

    public void seeEndNodeAuth(StringBuilder sb, List<Map<String, Object>> datas) {
	if (datas != null && !datas.isEmpty()) {
	    StringBuilder sbx = new StringBuilder();
	    for (Map<String, Object> di : datas) {
		if (sbx.length() > 0) {
		    sbx.append("、");
		}
		sbx.append(neo4jUService.seeNode(di) + "的" + string(di, "HAS_PERMISSION"));
	    }
	    if (sbx.length() > 0) {
		sb.append(sbx.toString());
	    }
	}
    }

    public void seeEndNodePropRel(StringBuilder sb, List<Map<String, Object>> datas) {
	if (datas != null && !datas.isEmpty()) {
	    StringBuilder sbx = new StringBuilder();
	    for (Map<String, Object> di : datas) {
		if (sbx.length() > 0) {
		    sbx.append("、");
		}
		sbx.append(neo4jUService.seeNode(di) + "的" + string(di, "HAS_"));
	    }
	    if (sbx.length() > 0) {
		sb.append(sbx.toString());
	    }
	}
    }

    public void seeEndNode(StringBuilder sb, List<Map<String, Object>> datas) {
	if (datas != null && !datas.isEmpty()) {
	    StringBuilder sbx = new StringBuilder();
	    for (Map<String, Object> di : datas) {
		if (sbx.length() > 0) {
		    sbx.append("、");
		}
		sbx.append(neo4jUService.seeNode(di));
	    }
	    if (sbx.length() > 0) {
		sb.append(sbx.toString());
	    }
	}
    }

    public void seeProperty(StringBuilder sb, String startName, String coli, Map<String, Object> oMdi) {
	List<Map<String, Object>> datas;
	String getRelPropi = "MATCH (f:Field)   WHERE  f.field='" + coli + "' and f.objectId=" + id(oMdi)
		+ " return f.type,f.valueField";
	List<Map<String, Object>> fieldInfo = neo4jUService.cypher(getRelPropi);
	if (!fieldInfo.isEmpty()) {
	    Map<String, Object> fi = fieldInfo.get(0);
	    String relTypeLabel = string(fi, "type");
	    String relTypeValue = string(fi, "valueField");

	    String getPropi = "MATCH (n:" + label(oMdi) + ") " + " WHERE n.name = '" + startName + "' return n." + coli;
	    datas = neo4jUService.cypher(getPropi);
	    if (datas != null && !datas.isEmpty()) {
		if (datas.get(0).get(coli) == null)
		    return;
		try {
		    Long coliValue = longValue(datas.get(0), coli);

		    String getRealValue = "MATCH (n:" + relTypeLabel + ") " + " WHERE id(n) = " + coliValue
			    + "   return n";
		    if (!ID.equals(relTypeValue)) {
			getRealValue = "MATCH (n:" + relTypeLabel + ") " + " WHERE n." + relTypeValue + " = "
				+ coliValue + "   return n";
		    }
		    datas = neo4jUService.cypher(getRealValue);
		    if (datas != null && !datas.isEmpty()) {
			sb.append(neo4jUService.seeNode(datas.get(0)));
		    }
		} catch (Exception e) {
		    sb.append(string(datas.get(0), coli));
		}
	    }
	} else {
	    String getPropi = "MATCH (n:" + label(oMdi) + ") " + " WHERE n.name = '" + startName + "' return n." + coli;
	    datas = neo4jUService.cypher(getPropi);
	    if (datas != null) {
		String coliValue = string(datas.get(0), coli);
		sb.append(coliValue);
	    }
	}
    }

    public String getABPath(String msg) {
	Long idOfStart = null;
	Long idOfEnd = null;

	boolean useAnd = false;
	for (String qie : andRel) {
	    if (msg.contains(qie)) {
		String[] resourceAuth = msg.split(qie);
		idOfStart = getIdOfData(resourceAuth[0]);
		idOfEnd = getIdOfData(resourceAuth[1]);
		useAnd = true;
	    }
	}
	if (useAnd) {
	    return adminService.showPathInfo(idOfStart, idOfEnd);
	}
	return null;
    }

    public String handleABPath(String msg) {
	Long idOfStart = null;
	Long idOfEnd = null;

	boolean useAnd = false;
	for (String qie : andRel) {
	    if (msg.contains(qie)) {
		String[] resourceAuth = msg.split(qie);
		for (String ri : resourceAuth) {
		    if (containLabelInfo(ri)) {
			Map<String, Object> onlyContext = new HashMap<>();
			ri = onlyName(onlyContext, ri);
			String dataLabel = string(onlyContext, "dataLabel");
			Map<String, Object> data2 = getData(ri, dataLabel);
			Long idOfRoleOrUser2 = id(data2);
			if (idOfStart != null) {
			    idOfEnd = idOfRoleOrUser2;
			} else {
			    idOfStart = idOfRoleOrUser2;
			}
			if (META_DATA.equals(dataLabel)) {
			    data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + dataLabel);
			}else {
			  //document
			  data2.put("url",LemodoApplication.MODULE_NAME + "/layui/" + dataLabel + "/" + id(data2) + "/documentRel");
			}
		    } else {
			Map<String, Object> mdData = getData(ri, META_DATA);

			if (mdData != null) {
			    idOfEnd = id(mdData);
			    if (META_DATA.equals(label(mdData))) {
				 mdData.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(mdData));
			    }else {
				//document
				mdData.put("url",LemodoApplication.MODULE_NAME + "/layui/" + label(mdData) + "/" + id(mdData) + "/documentRel");
			    }
			} else {
			    Long idOfRoleOrUser2 = getIdOfRoleOrUser(ri);
			    if (idOfStart != null) {
				idOfEnd = idOfRoleOrUser2;
			    } else {
				idOfStart = idOfRoleOrUser2;
			    }
			}
		    }

		}
		useAnd = true;
	    }
	}
	if (useAnd) {
	    return adminService.showPathInfo(idOfStart, idOfEnd);
	}
	return null;
    }

    public void handleExecute(String msg, List<Map<String, Object>> data, String prefix) {
	String obj = msg.replaceFirst(prefix, "");
	boolean useAnd = false;
	for (String qie : andRel) {
	    if (obj.contains(qie)) {
		String[] scripts = msg.split(qie);
		for (String si : scripts) {
		    Map<String, Object> script = getOneData(si);
		    if (script != null) {
			data.add(script);
		    }
		}
		useAnd = true;
	    }
	}
	if (!useAnd) {
	    Map<String, Object> data2 = getOneData(obj);
	    if (data2 != null) {
		data.add(data2);
	    }
	}

	for (Map<String, Object> di : data) {
	    String string = string(di, "Content");
	    Interpreter in = new Interpreter();
	    try {
		in.set("so", this);
		// 得有一个文档说明：
		in.set("repo", neo4jService);
		in.eval(string);
	    } catch (EvalError e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	}
    }

    public void handleManage(String msg, String labelOf, List<Map<String, Object>> data, String prefix) {
	String obj = msg.replaceFirst(prefix, "");
	boolean useAnd = false;
	for (String qie : andRel) {
	    if (obj.contains(qie)) {
		String[] resourceAuth = msg.split(qie);
		for (String ri : resourceAuth) {
		    Map<String, Object> mdData = getData(ri, labelOf);

		    if (mdData != null) {
			if (META_DATA.equals(labelOf)) {
			    mdData.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(mdData));
			}
			data.add(mdData);
		    }
		}
		useAnd = true;
	    }
	}
	if (!useAnd) {
	    Map<String, Object> data2 = getData(obj, labelOf);
	    if (data2 != null) {
		if (META_DATA.equals(labelOf)) {
		    data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(data2));
		}
		data.add(data2);
	    }
	}
    }

    public void handleOpen(String msg, String labelOf, List<Map<String, Object>> data) {
	boolean useAnd = false;
	for (String qie : andRel) {
	    if (msg.contains(qie)) {
		String[] resourceAuth = msg.split(qie);
		for (String ri : resourceAuth) {
		    Map<String, Object> mdData = getData(ri, labelOf);
		    processOpenData(labelOf, data, mdData);
		}
		useAnd = true;
	    }
	}
	if (!useAnd) {
	    Map<String, Object> data2 = getData(msg, labelOf);
	    processOpenData(labelOf, data, data2);
	}
    }

    public void processOpenData(String labelOf, List<Map<String, Object>> data, Map<String, Object> data2) {
	if (data2 != null) {
	    if (url(data2) == null) {
		// 判断权限，只读和修改权限

		data2.put("url",
			LemodoApplication.MODULE_NAME + "/layui/" + label(data2) + "/" + id(data2) + "/documentRel");

	    }
	    if (META_DATA.equals(labelOf)) {
		data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(data2));
	    }
	    data.add(data2);
	}
    }

    /**
     * 将{用户或者角色}的什么的什么权限,删除。
     * 
     * @param msg
     */
    public void handleStartWithJiang(String msg, Map<String, Object> context) {
	String prefix2 = "将";
	if (msg.startsWith(prefix2)) {
	    context.put("used", true);
	    // 获取默认数据：
	    for (String deli : relationDel) {
		if (msg.contains(deli)) {
		    String[] dels = msg.split(deli);
		    String objectResourceAuth = dels[0].replaceFirst(prefix2, "");
		    Boolean useOwn=false;
		    //将xxx删除
		    for (String oi : ownWords) {
			//资源权限删除
			if (objectResourceAuth.contains(oi)) {
			    useOwn=true;
			    String[] resourceAuth = objectResourceAuth.split(oi);
			    String objectStr = resourceAuth[0];
			    boolean useBetween = false;
			    for(String ci:between) {
				if(objectStr.endsWith(ci)) {
				    objectStr=objectStr.replaceFirst(ci, "");
				    String betweenRel = resourceAuth[1];
				    if(betweenRel.endsWith("关系")){
					betweenRel=betweenRel.replaceFirst("关系", "");
					 
					 
					 List<Long> startIds = new ArrayList<>();
					    for (String qie : andRel) {
						if (objectStr.contains(qie)) {
						    String[] objs = objectStr.split(qie);
						    for (String obji : objs) {
							if (containLabelInfo(obji)) {
							    Map<String, Object> onlyContext = new HashMap<>();
							    obji = onlyName(onlyContext, obji);
							    String dataLabel = string(onlyContext, "dataLabel");
							    Map<String, Object> data2 = getData(obji, dataLabel);
							    startIds.add(id(data2));
							} else {
							    startIds.add(getIdOfData(obji));
							}
						    }
						    if(objs.length==2&&startIds.size()==2) {
							if("".equals(betweenRel)) {
							    deleteRel(startIds.get(0), startIds.get(1));
							}else {
							    deleteRel(startIds.get(0), startIds.get(1));
							}
							
						    }
						}
					    }
					    
					 
				    }else {
					
				    }
					return ;
				}
			    }
			    if(useBetween) {
				return;
			    }
			    
			    boolean useAnd = false;
			    List<Long> startIds = new ArrayList<>();
			    for (String qie : andRel) {
				if (objectStr.contains(qie)) {
				    String[] objs = objectStr.split(qie);
				    for (String obji : objs) {
					if (containLabelInfo(obji)) {
					    Map<String, Object> onlyContext = new HashMap<>();
					    obji = onlyName(onlyContext, obji);
					    String dataLabel = string(onlyContext, "dataLabel");
					    Map<String, Object> data2 = getData(obji, dataLabel);
					    startIds.add(id(data2));
					} else {
					    startIds.add(getIdOfRoleOrUser(obji));
					}
				    }
				    useAnd = true;
				}
			    }
			    Long startId = null;
			    if (!useAnd) {
				if (containLabelInfo(objectStr)) {
				    Map<String, Object> onlyContext = new HashMap<>();
				    objectStr = onlyName(onlyContext, objectStr);
				    String dataLabel = string(onlyContext, "dataLabel");
				    Map<String, Object> data2 = getData(objectStr, dataLabel);
				    startId = id(data2);
					if (resourceAuth.length > 2) {
					    startDelRel(resourceAuth[1], resourceAuth[2], startId);
					} else {
					    startDelRel(resourceAuth[0], resourceAuth[1], startId);
					}
				} else {
				    startId = getIdOfRoleOrUser(objectStr);
					if (resourceAuth.length > 2) {
					    startDelRel(resourceAuth[1], resourceAuth[2], startId);
					} else {
					    startDelRel(resourceAuth[0], resourceAuth[1], startId);
					}
				}
				
				

			    } else {
				for (Long si : startIds) {
				    if (resourceAuth.length > 2) {
					startDelRel(resourceAuth[1], resourceAuth[2], si);
				    } else {
					startDelRel(resourceAuth[0], resourceAuth[1], si);
				    }
				}
			    }

			}
		    }
		    
//		    if(!useOwn) {
//			
//		    }
		}
	    }
	}
    }

    /**
     * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
     * 
     * @param msg
     */
    public void handleStartWithGei(String msg, Map<String, Object> context) {
	if (msg.startsWith("给")) {
	    context.put("used", true);
	    // 获取默认数据：
	    boolean use = false;
	    for (String addi : authAdd) {
		if (msg.contains(addi)) {
		    use = true;
		    String[] addJuzi = msg.split(addi);
		    // 租户数据授权？该如何授予权限？
		    String userRole = addJuzi[0].replaceFirst("给", "");
		    boolean useAnd = false;
		    List<Long> startIds = new ArrayList<>();
		    for (String qie : andRel) {
			if (userRole.contains(qie)) {
			    String[] resourceAuth = addJuzi[1].split(qie);
			    for (String ri : resourceAuth) {
				if (containLabelInfo(ri)) {
				    Map<String, Object> onlyContext = new HashMap<>();
				    ri = onlyName(onlyContext, ri);
				    String dataLabel = string(onlyContext, "dataLabel");
				    Map<String, Object> data2 = getData(ri, dataLabel);
				    startIds.add(id(data2));
				} else {
				    Long idOfRoleOrUser = getIdOfRoleOrUser(ri);
				    startIds.add(idOfRoleOrUser);
				}

			    }
			    useAnd = true;
			}
		    }
		    if (!useAnd) {

			if (containLabelInfo(userRole)) {
			    Map<String, Object> onlyContext = new HashMap<>();
			    userRole = onlyName(onlyContext, userRole);
			    String dataLabel = string(onlyContext, "dataLabel");
			    Map<String, Object> data2 = getData(userRole, dataLabel);
			    startIds.add(id(data2));
			} else {
			    Long startId = getIdOfRoleOrUser(userRole);
			    startAddAuth(addJuzi[1], startId);
			}

		    } else {
			for (Long si : startIds) {
			    startAddAuth(addJuzi[1], si);
			}
		    }
		}
	    }
	    if (use) {
		return;
	    }

	    for (String deli : relationDel) {
		if (msg.contains(deli)) {
		    String[] dels = msg.split(deli);
		    String userRole = dels[0].replaceFirst("给", "");

		    boolean useAnd = false;
		    List<Long> startIds = new ArrayList<>();
		    for (String qie : andRel) {
			if (userRole.contains(qie)) {
			    String[] resourceAuth = dels[1].split(qie);
			    for (String ri : resourceAuth) {
				if (containLabelInfo(ri)) {
				    Map<String, Object> onlyContext = new HashMap<>();
				    ri = onlyName(onlyContext, ri);
				    String dataLabel = string(onlyContext, "dataLabel");
				    Map<String, Object> data2 = getData(ri, dataLabel);
				    startIds.add(id(data2));
				} else {
				    startIds.add(getIdOfRoleOrUser(ri));
				}
			    }
			    useAnd = true;
			}
		    }
		    Long startId = null;
		    Long endId = null;
		    String relCode = null;
		    if (!useAnd) {

			if (containLabelInfo(userRole)) {
			    Map<String, Object> onlyContext = new HashMap<>();
			    userRole = onlyName(onlyContext, userRole);
			    String dataLabel = string(onlyContext, "dataLabel");
			    Map<String, Object> data2 = getData(userRole, dataLabel);
			    startDelRel(dels[1], id(data2));
			} else {
			    startId = getIdOfRoleOrUser(userRole);
			    startDelRel(dels[1], startId);
			}

		    } else {
			for (Long si : startIds) {
			    startDelRel(dels[1], si);
			}
		    }

		}
	    }
	}
    }

    /**
     * 1、A和B是关系C 是朋友关系，恋人，同学，师徒，上级，下级，队友，校友 2、A是B的C /小明是小李的老师，好朋友，兄弟，父母，债务人，买受人
     * 
     * 
     * A和xxx与xx2是关系1 可以访问动作。 A可以/能：看见\访问\读写\更新\删除 B。 未读 已读 A昨天干了什么 今天要干什么？ 出现了什么问题？
     * 
     * @param msg
     * @param context
     */
    public void handleStartCreateRel(String msg, Map<String, Object> context) {
	if (msg.startsWith("创建关系")) {
	    context.put("used", true);
	    // 获取默认数据：
	    for (String isRel : kEqualv) {
		if (msg.contains(isRel)) {
		    String[] startEndOfRel = msg.split(isRel);
		    // 租户数据授权？该如何授予权限？
		    String start = startEndOfRel[0].replaceFirst("创建关系", "");
		    boolean useAnd = false;
		    List<Long> startIds = new ArrayList<>();
		    String rightOfIs = startEndOfRel[1];
		    for (String qie : andRel) {
			if (start.contains(qie)) {
			    String[] starts = rightOfIs.split(qie);
			    for (String ri : starts) {

				if (containLabelInfo(rightOfIs)) {
				    addStartId(startIds, ri);
				} else {
				    startIds.add(getIdOfData(ri));
				}
			    }
			    useAnd = true;
			}
		    }
		    Long startId = null;
		    if (!useAnd) {
			if (containLabelInfo(rightOfIs)) {
			    addStartMetaRel2End(start, rightOfIs);
			} else {
			    startId = getIdOfData(start);
			    startAddRel(rightOfIs, startId);
			}
		    } else {
			if (startIds.size() == 2) {
			    createRel(startIds.get(0), startIds.get(1), rightOfIs);
			}
			if (startIds.size() > 2) {
			    for (Long objectId : startIds) {
				for (Long otherId : startIds) {
				    if (!objectId.equals(otherId)) {
					createRel(objectId, otherId, rightOfIs);
				    }
				}
			    }
			}
		    }
		}
	    }
	}
    }

    /**
     * 添加关系，开始节点句子中包含(元数据信息)
     * 
     * @param start
     * @param rightOfIs
     */
    public void addStartMetaRel2End(String start, String rightOfIs) {
	String[] split = start.split("\\(");
	if (split.length < 2) {
	    split = start.split(cnLeftKuoHao);
	}
	String meta = null;
	if (split[1].endsWith(rightKuohao)) {
	    meta = split[1].replace(rightKuohao, "");
	} else if (split[1].endsWith(cnRightKuoHao)) {
	    meta = split[1].replace(cnRightKuoHao, "");
	}
	List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
	for (Map<String, Object> mi : metaDataByName) {
	    List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
	    for (Map<String, Object> di : dataBy) {
		startAddRel(rightOfIs, id(di));
	    }
	}
    }

    /**
     * 收集包含括号（元数据）的开始节点ID
     * 
     * @param startIds
     * @param start
     */
    public void addStartId(List<Long> startIds, String start) {
	String[] split = start.split("\\(");
	if (split.length < 2) {
	    split = start.split(cnLeftKuoHao);
	}
	String meta = null;
	if (split[1].endsWith(rightKuohao)) {
	    meta = split[1].replace(rightKuohao, "");
	} else if (split[1].endsWith(cnRightKuoHao)) {
	    meta = split[1].replace(cnRightKuoHao, "");
	}
	List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
	for (Map<String, Object> mi : metaDataByName) {
	    List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
	    for (Map<String, Object> di : dataBy) {
		startIds.add(id(di));
	    }
	}
    }

    /**
     * 处理以删除为开头的语句,先处理删除数据，再处理删除关系。
     * 
     * @param msg
     * @param context
     */
    public void handleDelete(String msg, Map<String, Object> context) {
	for (String deli : relationDel) {
	    if (msg.startsWith(deli)) {
		context.put("used", true);
		String delContent = msg.replaceFirst(deli, "");
		     
			Long idOfData = getIdOfData(delContent);
			if (idOfData != null) {// 删除数据
			    neo4jUService.execute("MATCH(n) where id(n)=" + idOfData + " delete n");
			} else {
			    List<Map<String, Object>> dataByXx = neo4jUService.getDataBy(delContent);
			    if (dataByXx != null && dataByXx.size() > 1) {
				// 多个的情况盖如和处理：和前端进行确认
				return;
			    }
			    handelDelNode(delContent);
			    handelDelRelOrProp(delContent);
			} 
		

	    }
	}
    }

    /**
     * update xx's dd =yy 更新小明的性别为女 更新小白的性别为女
     * 
     * 更新小白和李白的性别为女
     * 
     * @param msg
     * @param context
     */
    public void handleUpdate(String msg, Map<String, Object> context) {
	for (String updatei : updates) {
	    if (msg.startsWith(updatei)) {
		context.put("used", true);
		// 租户数据授权？该如何授予权限？
		String noPrefix = msg.replaceFirst(updatei, "");
		// 找到等于
		for (String eqi : kEqualv) {
		    if (noPrefix.contains(eqi)) {
			String[] startEndOfRel = noPrefix.split(eqi);
			// 租户数据授权？该如何授予权限？
			handleLeftAndRight(startEndOfRel[0], startEndOfRel[1]);
		    }
		}
	    }
	}
    }

    /**
     * 处理等式的左右两边
     * 
     * @param left
     * @param rightOfIs
     */
    public void handleLeftAndRight(String left, String rightOfIs) {
	for (String oi : ownWords) {
	    if (left.contains(oi)) {
		String[] leftBelong = left.trim().split(oi);
		String objectStr = leftBelong[0];
		boolean useAnd = false;
		List<Long> startIds = new ArrayList<>();
		for (String qie : andRel) {
		    if (objectStr.contains(qie)) {
			String[] lefts = objectStr.split(qie);
			for (String li : lefts) {
			    if (containLabelInfo(li)) {
				Map<String, Object> onlyContext = new HashMap<>();
				li = onlyName(onlyContext, li);
				String dataLabel = string(onlyContext, "dataLabel");
				Map<String, Object> data2 = getData(li, dataLabel);
				startIds.add(id(data2));
			    }else {
				Long idOfData = getIdOfData(li);
				    if (idOfData != null) {
					startIds.add(idOfData);
				    }
			    }
			    
			    
			}
			useAnd = true;
		    }
		}

		if (!useAnd) {
		    if (containLabelInfo(objectStr)) {
			Map<String, Object> onlyContext = new HashMap<>();
			objectStr = onlyName(onlyContext, objectStr);
			String dataLabel = string(onlyContext, "dataLabel");
			Map<String, Object> data2 = getData(objectStr, dataLabel);
			startIds.add(id(data2));
		    }else {
        		    Long startId = getIdOfData(objectStr);
        		    if (startId != null) {
        			startIds.add(startId);
        		    }
		    }
		}

		String propOrRel = leftBelong[1];

		for (Long startId : startIds) {
		    Map<String, String> nameColById = neo4jUService.getNameColById(startId);
		    String coli = nameColById.get(propOrRel);
		    if (coli != null) {
			// 判断字段是否是关联字段。管理字段根据
			JSONObject vo = new JSONObject();
			Map<String, Object> metaDataById = neo4jUService.getMetaDataById(startId);
			String labelPo = label(metaDataById);
			vo.put("poId", labelPo);
			// 查询自定义字段数据
			List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
			if (fieldInfoList != null && !fieldInfoList.isEmpty()) {
			    Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
			    for (Map<String, Object> fi : fieldInfoList) {
				Object object = fi.get(FIELD);
				object = object == null ? fi.get(ID) : object;
				customFieldMap.put(String.valueOf(object), fi);
			    }
			    Map<String, Object> field = customFieldMap.get(coli);

			    if (field != null) {
				String type = String.valueOf(field.get("type"));
				if ("true".equals(field.get("isPo"))) {// 更新关联字段
				    List<Map<String, Object>> dataBy = neo4jUService.getDataBy(type, rightOfIs);
				    if (dataBy != null) {
					String id2 = string(dataBy.get(0), ID);
					neo4jUService.updateBy(startId, coli, id2);
				    }
				}
			    }
			} else {// 更新属性
			    neo4jUService.updateBy(startId, coli, rightOfIs);
			}
		    } else {
			// 没有字段？就不需要处理，更新元数据，是否需要新增字段，抑或是
			startAddRel(rightOfIs, startId);
		    }
		}
	    }
	}
    }

    public boolean handelDelNode(String xx) {
	boolean useAnd = false;
	List<Long> objIds = new ArrayList<>();
	for (String qie : andRel) {
	    if (xx.contains(qie)) {
		String[] starts = xx.split(qie);
		for (String ri : starts) {
		    if (containLabelInfo(ri)) {
			Map<String, Object> onlyContext = new HashMap<>();
			ri = onlyName(onlyContext, ri);
			String dataLabel = string(onlyContext, "dataLabel");
			Map<String, Object> data2 = getData(ri, dataLabel);
			Long idOfData= id(data2);
			objIds.add(idOfData);
		    }else {
			objIds.add(getIdOfData(ri));
		    }		    
		}
		useAnd = true;
	    }
	}
	if (useAnd && objIds.size() > 0) {
	    neo4jUService.execute("MATCH (n) WHERE ID(n) IN [" + joinLong(objIds) + "] DETACH DELETE n ");
	}
	return useAnd;
    }

    /**
     * 删除关系或者删除属性
     * 
     * @param xx
     * @return
     */
    public boolean handelDelRelOrProp(String xx) {
	boolean useAnd = false;
	boolean useOwni = false;
	for (String qie : andRel) {
	    if (xx.contains(qie)) {
		String[] ands = xx.split(qie);
		for (String ai : ands) {
		    for (String oi : ownWords) {
			if (xx.contains(oi)) {
			    useOwni = true;
			    String[] orpi = ai.split(oi);
			    
			    
			    Map<String, Object> oneData = getOneData(orpi[0]);
			    Long owniId = id(oneData);
			    Map<String, String> nameColById = neo4jUService.getNameColById(owniId);

			    String col = nameColById.get(orpi[1]);
			    if (col != null) {
				neo4jUService.execute("MATCH (n) WHERE ID(n)  =" + owniId + " REMOVE n." + col);
			    } else {
				Long idOfMd = getIdOfMd(orpi[1]);
				if (idOfMd == null) {
				    Long idOfData = getIdOfData(orpi[1]);
				    deleteRel(owniId, idOfData);
				} else {
				    deleteRel(owniId, idOfMd);
				}
			    }
			}
		    }
		}
		useAnd = true;
	    }
	}

	if (!useAnd) {
	    for (String oi : ownWords) {
		if (xx.contains(oi)) {
		    useOwni = true;
		    String[] orpi = xx.split(oi);
		    Map<String, Object> oneData = getOneData(orpi[0]);
		    Long owniId = id(oneData);
		    Map<String, String> nameColById = neo4jUService.getNameColById(owniId);

		    String col = nameColById.get(orpi[1]);
		    if (col != null) {
			neo4jUService.execute("MATCH (n) WHERE ID(n)  =" + owniId + " REMOVE n." + col);
		    } else {
			Long idOfMd = getIdOfMd(orpi[1]);
			if (idOfMd == null) {
			    Long idOfData = getIdOfData(orpi[1]);
			    deleteRel(owniId, idOfData);
			} else {
			    deleteRel(owniId, idOfMd);
			}
		    }
		}

	    }
	}

	return useAnd;
    }

    public void startDelRel(String delJuzi, Long startId) {
	Long endId;
	for (String owni : ownWords) {
	    if (delJuzi.contains(owni)) {
		String[] resourceAuth = delJuzi.split(owni);
		String resource = resourceAuth[0];
		endId = getIdOfMd(resource);
		String auth = resourceAuth[1];
		deleteAuthRel(startId, endId, auth);
	    }
	}
    }

    public void startDelRel(String resource, String auth, Long startId) {
	Long endId = getIdOfMd(resource);
	if (endId == null || endId.equals(startId)) {
	    if (containLabelInfo(auth)) {
		Map<String, Object> onlyContext = new HashMap<>();
		auth = onlyName(onlyContext, auth);
		String dataLabel = string(onlyContext, "dataLabel");
//		 Map<String, Object> mapObject = mapObject(onlyContext, "dataMd");
		 endId =  getIdOfData(auth, dataLabel);
	 }else {
	     endId = getIdOfData(auth);
	 }
	    
	}
	deleteAuthRel(startId, endId, auth);

    }
    @ServiceLog(description="删除权限关系，开始节点和结束节点，权限关系")
    public void deleteAuthRel(Long startId, Long endId, String auth) {
	String relCode;
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, auth, "permission");
	relCode = code(authMap);
	String cypher = " MATCH(s)-[r:" + relCode + "{name:\"" + auth + "\"}]->(e)  where id(s)=" + startId
		+ " and id(e)=" + endId + " delete r";
	neo4jService.execute(cypher);
    }
    @ServiceLog(description="删除关系，开始节点和结束节点的所有关系")
    public void deleteRel(Long startId, Long endId) {
	String cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " delete r";
	neo4jService.execute(cypher);
    }
    @ServiceLog(description="删除关系，开始节点和结束节点的某个关系")
    public void deleteRel(Long startId, Long endId,String name) {
	String cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " delete r";
	if(name!=null&&!"".equals(name.trim())) {
	    cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and r.name=\""+name+"\" and id(e)=" + endId + " delete r";
	}
	neo4jService.execute(cypher);
    }

    /**
     * 添加关系
     * 
     * @param rightOfIs
     * @param startId
     */
    @ServiceLog(description="给开始节点添加关系")
    public void startAddRel(String rightOfIs, Long startId) {
	Long endId;
	boolean hasOwniWord = false;
	for (String owni : ownWords) {
	    if (rightOfIs.contains(owni)) {
		hasOwniWord = true;
		String[] whoSResource = rightOfIs.split(owni);
		String who = whoSResource[0];

		String propOrRel = whoSResource[1];

		if (containLabelInfo(who)) {
		    addAuthRelWithLabelInfo(startId, who, propOrRel, zuoKuohao);
		    addAuthRelWithLabelInfo(startId, who, propOrRel, cnLeftKuoHao);
		} else {
		    endId = getIdOfMd(who);
		    createRel(startId, endId, propOrRel);
		}
	    }
	}
	if (!hasOwniWord) {
	    // 不应该出现A是B？
	    endId = neo4jUService.getIdBy(rightOfIs);
	    if (endId != null) {// 有关系节点
		createRel(startId, endId, rightOfIs);
	    }
	}
    }

    /**
     * 给开始节点添加xxxxxxxxxx
     * 
     * @param rightOfIs
     * @param startId
     */
    @ServiceLog(description="给开始节点添加权限")
    public void startAddAuth(String rightOfIs, Long startId) {
	Long endId;
	boolean hasOwniWord = false;

	for (String owni : ownWords) {// 谁的什么
	    if (rightOfIs.contains(owni)) {
		hasOwniWord = true;
		String[] whoSResource = rightOfIs.split(owni);
		String who = whoSResource[0];

		String por = whoSResource[1];
		if (containLabelInfo(who)) {// 包含Label信息，
		    addAuthRelWithLabelInfo(startId, zuoKuohao, who, por);
		    addAuthRelWithLabelInfo(startId, cnLeftKuoHao, who, por);
		} else {
		    endId = getIdOfMd(who);
		    addAuthRel(startId, endId, por);
		}
	    }
	}
	if (!hasOwniWord) {
	    // 不应该出现A是B？
	    if (containLabelInfo(rightOfIs)) {
		relateEndWithLabelInfo(rightOfIs, startId, zuoKuohao);
		relateEndWithLabelInfo(rightOfIs, startId, cnLeftKuoHao);
	    } else {
		endId = neo4jUService.getIdBy(rightOfIs);
		if (endId != null) {
		    addAuthRel(startId, endId, rightOfIs);
		}
	    }
	}
    }

    /**
     * 关联终点包含括号括起来的标签Label信息
     * 
     * @param rightOfIs
     * @param startId
     * @param zuoKuohao2
     */
    @ServiceLog(description="给开始节点添加关系，终点有元数据标识")
    public void relateEndWithLabelInfo(String rightOfIs, Long startId, String zuoKuohao2) {
	if (rightOfIs.contains(zuoKuohao2)) {
	    String[] split = rightOfIs.split(zuoKuohao2);
	    String meta = null;
	    if (split[1].endsWith(rightKuohao)) {
		meta = split[1].replace(rightKuohao, "");
	    }
	    if (split[1].endsWith(cnRightKuoHao)) {
		meta = split[1].replace(cnRightKuoHao, "");
	    }
	    List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
	    for (Map<String, Object> mi : metaDataByName) {
		List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
		for (Map<String, Object> di : dataBy) {
		    addAuthRel(startId, id(di), meta);
		}
	    }
	}
    }

    /**
     * 给开始节点添加权限信息，包含标签，Label信息
     * 
     * @param startId
     * @param zuoKuohao2
     * @param who
     * @param por
     */
    @ServiceLog(description="给开始节点添加权限关系，终点有元数据信息，带着终点和气属性或者关系参数")
    public void addAuthRelWithLabelInfo(Long startId, String zuoKuohao2, String who, String por) {
	if (who.contains(zuoKuohao2)) {
	    String[] split = who.split(zuoKuohao2);
	    String meta = null;
	    if (split[1].endsWith(rightKuohao)) {
		meta = split[1].replace(rightKuohao, "");
	    }
	    if (split[1].endsWith(cnRightKuoHao)) {
		meta = split[1].replace(cnRightKuoHao, "");
	    }
	    List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
	    for (Map<String, Object> mi : metaDataByName) {
		List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
		for (Map<String, Object> di : dataBy) {
		    addAuthRel(startId, id(di), por);
		}
	    }
	}
    }

    @ServiceLog(description="带着括号的字符串，获取其元数据")
    public String getMetaInfo(String zuoKuohao2, String who, Map<String, Object> context) {
	if (who.contains(zuoKuohao2)) {
	    String[] split = who.split(zuoKuohao2);
	    String meta = null;
	    if (split[1].endsWith(rightKuohao)) {
		meta = split[1].replace(rightKuohao, "");
	    }
	    if (split[1].endsWith(cnRightKuoHao)) {
		meta = split[1].replace(cnRightKuoHao, "");
	    }
	    List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
	    for (Map<String, Object> mi : metaDataByName) {
		List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
		for (Map<String, Object> di : dataBy) {
		    context.put("dataLabel", label(di));
		    context.put("dataMd", di);
		}
	    }
	    return meta;
	}
	return null;
    }

    /**
     * 判断句子是否包含左右括号，包括中引文括号。
     * 
     * @param rightOfIs
     * @return 包含括号返回true
     */
    public boolean containLabelInfo(String rightOfIs) {
	Boolean metaStart = rightOfIs.contains(zuoKuohao) || rightOfIs.contains(cnLeftKuoHao);
	Boolean metaEnd = rightOfIs.contains(rightKuohao) || rightOfIs.contains(cnRightKuoHao);
	boolean hasMetaInfo = metaStart && metaEnd;
	return hasMetaInfo;
    }

    public void createRel(Long startId, Long endId, String auth) {
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, auth, "RelationDefine");
	if (authMap == null) {
	    return;
	}
	String relCode = string(authMap, "reLabel");
	String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId
		+ " create (s)-[:HAS_PERMISSION{name:\"" + auth + "\",code:\"" + relCode + "\"}]->(e)";
	neo4jService.execute(cypher);
    }

    public void delRel(Long startId, Long endId, String auth) {
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, auth, "RelationDefine");
	if (authMap == null) {
	    return;
	}
	String relCode = string(authMap, "reLabel");
	String cypher = "MATCH (s)-[r:" + relCode + "{name:\"" + auth + "\"}]->(e) where id(s)=" + startId
		+ " and id(e)=" + endId + " delete r";
	neo4jService.execute(cypher);
    }

    /**
     * 给开始节点和结束节点添加权限关系
     * 
     * @param startId
     * @param endId
     * @param auth
     */
    public void addAuthRel(Long startId, Long endId, String auth) {
	String relCode;
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, auth, "permission");
	if (authMap != null) {
	    relCode = code(authMap);
	    String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId
		    + " create (s)-[:HAS_PERMISSION{code:\"" + relCode + "\",name:\"" + auth + "\"}]->(e)";
	    neo4jService.execute(cypher);
	    return;
	} else {
	    Map<String, Object> metaDataById = neo4jUService.getMetaDataById(endId);
	    String code2 = code(metaDataById);
	    StringBuilder cypher = new StringBuilder(
		    "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId + " create (s)-[:HAS_PERMISSION{");
	    if (code2 != null) {
		cypher.append("code:\"" + code2 + "\",");
	    }
	    cypher.append("name:\"" + name(metaDataById) + "\"}]->(e)");
	    neo4jService.execute(cypher.toString());
	}

    }

    public Long getIdOfMd(String resource) {
	Long startId = null;
	List<Map<String, Object>> metaDataBy = neo4jUService.getMetaDataBy(resource);
	if (metaDataBy.size() >= 1) {
	    startId = id(metaDataBy.get(0));
	}
	return startId;
    }

    public String getLabelOfMd(String resource) {
	String labelData = null;
	List<Map<String, Object>> metaDataBy = neo4jUService.getMetaDataBy(resource);
	if (metaDataBy.size() == 1) {
	    labelData = label(metaDataBy.get(0));
	}
	return labelData;
    }

    public Long getIdOfRoleOrUser(String resource) {
	Long startId = null;
	List<Map<String, Object>> metaDataBy = neo4jUService.getDataBy("Role", resource);
	if (!metaDataBy.isEmpty() && metaDataBy.size() > 0) {
	    startId = id(metaDataBy.get(0));
	} else {
	    metaDataBy = neo4jUService.getDataBy("User", resource);
	    if (!metaDataBy.isEmpty() && metaDataBy.size() > 0) {
		startId = id(metaDataBy.get(0));
	    }
	}
	return startId;
    }

    public Map<String, Object> getData(String name, String labelOf) {
	List<Map<String, Object>> metaDataBy = neo4jUService.getDataBy(labelOf, name);
	if (metaDataBy.isEmpty() || metaDataBy.size() < 1) {
	    return null;
	}
	return metaDataBy.get(0);
    }
    public Long getIdOfData(String name, String labelOf) {
	return id(getData(name,labelOf));
    }

    public Map<String, Object> getOneData(String name) {
	List<Map<String, Object>> metaDataBy = neo4jUService.getDataBy(name);
	if (metaDataBy.isEmpty() || metaDataBy.size() < 1 || metaDataBy.size() > 1) {
	    return null;
	}
	return metaDataBy.get(0);
    }

    public Long getIdOfData(String resource) {
	return id(getOneData(resource));
    }

    /**
     * 默认增删改查
     * 
     * @param message
     */
    public Map<String, Object> parseAndexcute(String message, String sessionId) {
	// 替换掉声音助词
	String msg = clearVoiceWord(message);
	// huoq唤醒词：
	// 默认的唤醒词
	if (msg.length() <= 10) {
	    // 获取默认数据：
	    boolean use = false;
	    List<String> xx = new ArrayList<>();
	    xx.addAll(getUseWords);
	    xx.addAll(stackQuit);
	    xx.addAll(operateStack);
	    xx.addAll(newUpdate);
	    xx.addAll(newRelation);
	    xx.addAll(newNode);
	    xx.addAll(andRel);
	    xx.addAll(removes);
	    xx.addAll(ownWords);
	    xx.addAll(kEqualv);
	    xx.addAll(relProp);
	    xx.addAll(relName);
	    xx.addAll(isRel);
	    xx.addAll(manageNode);

	    for (String ni : xx) {
		if (msg.startsWith(ni)) {
		    use = true;
		}
	    }
	    if (!use) {
		Map<String, Object> parseNoReservedWord = parseNoReservedWord(sessionId, msg);
		if (parseNoReservedWord != null) {
		    return parseNoReservedWord;
		}
	    }

	}

	Map<String, Object> operateMeta = enterMeta(sessionId, msg);
	if (operateMeta != null) {
	    return operateMeta;
	}
	Map<String, Object> manage = manage(sessionId, msg);
	if (manage != null) {
	    return manage;
	}
	Map<String, Object> operateObject = useIt(sessionId, msg);
	if (operateObject != null) {
	    return operateObject;
	}

	Map<String, Object> handleStartWithTa = handleStartWithTa(sessionId, msg);
	if (handleStartWithTa != null) {
	    return handleStartWithTa;
	}
	for (String ni : removes) {
	    Map<String, Object> delObject = deleteOne(msg, ni, sessionId);
	    if (delObject != null) {
		return delObject;
	    }
	}
	//
	for (String hi : relProp) {
	    addRelationProp(msg, hi, sessionId);
	}
	return singleSentence(msg, sessionId);
    }

    /**
     * 解析没有使用关键字的句子
     * 
     * @param sessionId
     * @param msg
     * @return
     */
    private Map<String, Object> parseNoReservedWord(String sessionId, String msg) {
	Map<String, Object> myContext = getMyContext(sessionId);
	Map<String, Object> data = new HashMap<>();

	// 当前节点为空，或者当前节点名称有
	Map<String, Object> metaMap = mapObject(myContext, OPERATE_META);
	if (metaMap == null) {
	    return newFromMeta(sessionId, msg);
	} else {// 当前已有对象，则获取当前的标签，查询当前元数据的实例数据
	    String operateLabel = getOperateLabel(sessionId);
	    Map<String, Object> objectNode = getNode(operateLabel, "name", msg.trim());
	    if (objectNode != null) {// 在当前的元数据下查询到实例数据，精确查找实例数据
		setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);

		Map<String, Object> objectShowCol = neo4jUService.onlyShowCol(objectNode, operateLabel);
		if (objectShowCol == null) {
		    try {
			crudUtil.simplification(objectNode);
			deSensitive(operateLabel, objectNode);
			Map<String, String> colHeader = neo4jUService.getColHeadById(id(objectNode));
			objectShowCol = visualData(colHeader, objectNode);
		    } catch (DefineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		if (label(objectShowCol) == null) {
		    objectShowCol.put(LABEL, operateLabel);
		}
		neo4jUService.visulRelation(objectShowCol);
		StringBuilder sb = new StringBuilder();
		nodeDataRelView(sb, objectShowCol);
		data.put("data", sb.toString());
		return data;
	    } else {// 当前的元数据中，没有精确的实例数据
		// 重新走一遍查询，去掉当前：元数据，实例数据，标签
		clearMetaObject(myContext);
		return newFromMeta(sessionId, msg);
	    }
	}
    }

    private Map<String, Object> parseObjectWord(String sessionId, String msg) {
	Map<String, Object> myContext = getMyContext(sessionId);
	Map<String, Object> data = new HashMap<>();

	// 当前节点为空，或者当前节点名称有
	Map<String, Object> metaMap = mapObject(myContext, OPERATE_META);
	if (metaMap == null) {
	    return newFromMeta(sessionId, msg);
	} else {// 当前已有对象，则获取当前的标签，查询当前元数据的实例数据
	    String operateLabel = getOperateLabel(sessionId);
	    Map<String, Object> objectNode = getNode(operateLabel, "name", msg.trim());
	    if (objectNode != null) {// 在当前的元数据下查询到实例数据，精确查找实例数据
		setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);

		Map<String, Object> objectShowCol = neo4jUService.onlyShowCol(objectNode, operateLabel);
		if (objectShowCol == null) {
		    try {
			crudUtil.simplification(objectNode);
			deSensitive(operateLabel, objectNode);
			Map<String, String> colHeader = neo4jUService.getColHeadById(id(objectNode));
			objectShowCol = visualData(colHeader, objectNode);
		    } catch (DefineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
		if (label(objectShowCol) == null) {
		    objectShowCol.put(LABEL, operateLabel);
		}
		neo4jUService.visulRelation(objectShowCol);
		StringBuilder sb = new StringBuilder();
		nodeDataRelView(sb, objectShowCol);
		data.put("data", sb.toString());
		return data;
	    } else {// 当前的元数据中，没有精确的实例数据
		// 重新走一遍查询，去掉当前：元数据，实例数据，标签
		clearMetaObject(myContext);
		return newFromMeta(sessionId, msg);
	    }
	}
    }

    /**
     * 根据实例数据，返回元数据
     * 
     * @param di
     * @return
     */
    public String metaInfoByData(Map<String, Object> di) {
	Map<String, Object> metaData = neo4jUService.getAttMapBy(LABEL, label(di), META_DATA);
	return metaDataView(metaData);
    }

    /**
     * 可视化元数据，链接元数据
     * 
     * @param metaData
     * @return
     */
    public String metaDataView(Map<String, Object> metaData) {
	String metai = "<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/MetaData/documentRead?id="
		+ id(metaData) + "')\">" + name(metaData) + "</a>";
	return metai;
    }

    private Map<String, Object> newFromMeta(String sessionId, String msg) {
	// 全局查询元数据
	Map<String, Object> metaNode = getNode(META_DATA, "name", msg.trim());
	if (metaNode != null) {// 精确找到元数据
	    String label = label(metaNode);
	    getInto(label, sessionId);
	    setMayConextProp(OPERATE_META, metaNode, sessionId);
	} else {// 没有精确的元数据
	    // 查询所有名称或者编码与查询字段匹配的所有节点。
	    String queryByNameOrCode = "Match(n) where n.name  CONTAINS '" + msg + "' OR n.code  CONTAINS '" + msg
		    + "' return n";
	    List<Map<String, Object>> queryList = neo4jService.cypher(queryByNameOrCode);
	    Map<String, Object> data = new HashMap<>();
	    // crudUtil.simplifiList(queryList);
	    neo4jUService.visualRelList(queryList);
	    StringBuilder sb = new StringBuilder();
	    for (Map<String, Object> di : queryList) {
		if (sb.length() > 0) {
		    sb.append("\n");
		}
		nodeDataRelView(sb, di);
	    }
	    data.put("data", sb.toString());
	    return data;
	}
	// 只显示设置过的可视化字段
	Map<String, Object> metaShowCol = neo4jUService.onlyShowCol(metaNode, label(metaNode));
	if (metaShowCol != null) {
	    neo4jUService.visulRelation(metaShowCol);
	    return metaShowCol;
	}
	// 没有设置显示的数据，默认的数据显示功能。
	crudUtil.simplification(metaNode);
	neo4jUService.visulRelation(metaNode);

	return metaNode;
    }

    public void nodeDataRelView(StringBuilder sb, Map<String, Object> di) {
	metaAndDataView(sb, di);
	String string = string(di, "出关系");
	if (string != null) {
	    sb.append("\n" + name(di) + "->" + string);
	}
	String r = string(di, "入关系");
	if (r != null) {
	    sb.append("\n" + r + "->" + name(di));
	}
    }

    /**
     * 根据实例数据读取实例数据，元数据，
     * 
     * @param sb
     * @param di
     */
    public void metaAndDataView(StringBuilder sb, Map<String, Object> di) {
	String label2 = label(di);
	if (label2 != null) {
	    sb.append(metaInfoByData(di));// 元数据链接
	    sb.append(":");
	    String nodeName;
	    if (label2.equals(LABEL_FIELD)) {
		nodeName = "【" + string(di, FIELD) + "】";
	    } else {
		String name2 = name(di);
		if (label2.equals(META_DATA)) {
		    nodeName = "【" + label2 + "】";
		    if (name2 != null || "null".equals(name2)) {
			nodeName = nodeName + name2;
		    }
		} else {
		    nodeName = neo4jUService.showNode(di, label2);
		}
	    }

	    sb.append("<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/" + label2 + "/documentRead?id="
		    + id(di) + "')\">" + nodeName + "</a>");// 实例数据连接
	}
    }

    /**
     * 清理对象和元数据，pop
     * 
     * @param myContext
     */
    private void clearMetaObject(Map<String, Object> myContext) {
	myContext.remove(OPERATE_META);
	myContext.remove(OPERATE_OBJECT);
	myContext.remove(OPERATE_LABEL);

    }

    private void deSensitive(String label, Map<String, Object> di) throws DefineException {
	String[] sensitiveColumn = crudUtil.getSensitiveColumn(label);
	if (sensitiveColumn != null && sensitiveColumn.length > 0) {
	    for (String si : sensitiveColumn) {
		String string = string(di, si);
		String dsValue = string.substring(0, 1);
		di.put(si, dsValue + "***");
	    }
	}
    }

    private Map<String, Object> visualData(Map<String, String> colHeader, Map<String, Object> di)
	    throws DefineException {
	Map<String, Object> copy = copy(di);
	for (Entry<String, String> si : colHeader.entrySet()) {
	    if (di.containsKey(si.getKey())) {
		copy.put(si.getValue(), di.get(si.getKey()));
	    }

	}
	return copy;
    }

    /**
     * 简化字段
     *
     * @throws DefineException
     */

    private Map<String, Object> handleStartWithTa(String sessionId, String string) {
	if (getOperateLabel(sessionId) != null && getOperateObject(sessionId) != null
		|| getOperateRelation(sessionId) != null) {
	    List<String> heSheIt = Arrays.asList("他", "她", "它");
	    for (String hi : heSheIt) {
		// 新增关系 xx and xx is xxRel

		for (String ni : andRel) {
		    addTaRealtion(string, hi + ni, sessionId);
		}
		// 新增关系 xx 是 bb de xxRel
		for (String ni : kEqualv) {
		    addIsTaDeRealtion(string, hi + ni, sessionId);
		}
		// 新增属性 o的xxx是bbb
		for (String ni : ownWords) {
		    Map<String, Object> addTaDeProp = addTaDeProp(string, hi + ni, sessionId);
		    if (addTaDeProp != null) {
			return addTaDeProp;
		    }
		}
	    }
	}

	return null;
    }

    public Map<String, Object> multiParseAndexcute(String string2, String sessionId) {
	// 替换掉声音助词
	String string = clearVoiceWord(string2);
	// huoq唤醒词：
//	neo4jService.listDataByLabel("w");

	Map<String, Object> operateMeta = enterMeta(sessionId, string);
	if (operateMeta != null) {
	    return operateMeta;
	}

	Map<String, Object> operateObject = useIt(sessionId, string);
	if (operateObject != null) {
	    return operateObject;
	}
	handleStartWithTa(sessionId, string);
	List<String> relProp = Arrays.asList("关系属性");
	for (String hi : relProp) {
	    addRelationProp(string, hi, sessionId);
	}
	return singleSentence(string, sessionId);
    }

    private Map<String, Object> enterMeta(String sessionId, String string) {
	Map<String, Object> operateMeta = null;

	for (String ni : getUseWords) {
	    operateMeta = useSomething(string, ni, sessionId);
	    if (operateMeta != null) {
		return operateMeta;
	    }
	}
	return operateMeta;
    }

    private Map<String, Object> manage(String sessionId, String string) {
	Map<String, Object> operateMeta = new HashMap<>();

	for (String ni : manageNode) {
	    operateMeta = manageSomething(string, ni, sessionId);
	    if (operateMeta != null) {
		return operateMeta;
	    }
	}
	return operateMeta;
    }

    private Map<String, Object> useIt(String sessionId, String string) {
	Map<String, Object> operateObject = null;
	for (String ni : getUseWords) {
	    operateObject = useObject(string, ni, sessionId);
	    if (operateObject != null) {
		return operateObject;
	    }
	}
	return null;
    }

    /**
     * 批量操作，同一种操作，CUD
     * 
     * @param string
     * @param sessionId
     * @return
     */
    private Map<String, Object> singleSentence(String string, String sessionId) {
	List<Map<String, Object>> dataCreate = new ArrayList<>();
	for (String ni : newNode) {
	    List<Map<String, Object>> createSomething = createSomething(string, ni, sessionId);
	    dataCreate.addAll(createSomething);
	}
	if (dataCreate != null && !dataCreate.isEmpty()) {
	    Map<String, Object> data = new HashMap<>();
	    data.put("data", dataCreate);
	    return data;
	}
	List<Map<String, Object>> updateCreate = new ArrayList<>();

	for (String ni : newUpdate) {
	    List<Map<String, Object>> updates = updateSomething(string, ni);
	    updateCreate.addAll(updates);
	}
	if (updateCreate != null && !updateCreate.isEmpty()) {
	    Map<String, Object> data = new HashMap<>();
	    data.put("data", dataCreate);
	    return data;
	}

	for (String ni : newRelation) {
	    addSomeRealtion(string, ni, sessionId);
	}

	for (String ni : removes) {
	    deleteSomething(string, ni, sessionId);
	}
	return null;
    }

    private String clearVoiceWord(String string2) {
	String replaceAll = string2.replaceAll("嗯", "");
	List<String> wcSet = Arrays.asList("诶", "呃", "乌", "阿", "偌", "得", "叱", "吓", "吁", "呔", "呐", "呜", "呀", "呵", "哎",
		"咄", "咍", "呣", "呶", "呸", "呦", "哈", "咳", "哑", "咦", "哟", "咨", "啊", "唉", "唗", "哦", "哼", "唦", "喏", "啧", "嗏",
		"喝", "嗟", "喂", "喔", "嗄", "嗳", "嗤", "嘟", "嗨", "嗐", "嗯", "嘘", "嘿", "噢", "嘻", "噫", "嚄", "嚯", "於", "欸", "恶",
		"究竟", "终究", "他妈的", "日你的妈");
	for (String ni : wcSet) {
	    if (string2.indexOf("ni") > -1) {
		replaceAll = replaceAll.replaceAll(ni, "");
	    }
	}

	return replaceAll;
    }

    /**
     * 创建相关的事情
     * 
     * @param string
     */
    private List<Map<String, Object>> createSomething(String string, String newCreate, String sessionId) {
	string = clearFuhao(string);
	List<Map<String, Object>> createObjects = new ArrayList<>();
	if (string.indexOf(newCreate) > -1) {
	    String[] create = string.split(newCreate);
	    for (String createObject : create) {
		if (createObject != null && !"".equals(createObject.trim()) && !createObject.startsWith("关系")) {
		    Map<String, Object> createOne = createOne(createObject, sessionId);
		    if (createOne != null) {
			createObjects.add(createOne);
		    }
		}
	    }
	}
	if (createObjects.size() == 1) {
	    setMayConextProp(OPERATE_OBJECT, createObjects.get(0), sessionId);
	}

	return createObjects;
    }

    /**
     * 操作：元数据（用户，项目）。 更新某个东西 do（将，把）xxx的sss修改为（成）bb
     * 
     * @param string
     * @param updateWord
     */
    private List<Map<String, Object>> updateSomething(String string, String updateWord) {
	string = clearFuhao(string);
	List<Map<String, Object>> updateObjects = new ArrayList<>();
	if (string.indexOf(updateWord) > -1) {
	    String[] updates = string.split(updateWord);
	    String operatObject = updates[0];
	    if (operatObject != null && operatObject.startsWith("操作")) {
		String label = operatObject.replace("操作", "");
		if (label.length() > 1) {
		    for (String updateObject : updates) {
			if (updateObject != null && !"".equals(updateObject.trim()) && !updateObject.startsWith("关系")) {
			    List<Map<String, Object>> updateOne = updateOne(updateObject, label);
			    updateObjects.addAll(updateOne);
			}
		    }
		}
	    }
	}
	return updateObjects;
    }

    /**
     * 
     * @param string
     * @param useWordi
     */
    private Map<String, Object> useSomething(String string, String useWordi, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(useWordi) > -1) {
	    if (string != null && string.startsWith(useWordi)) {
		String[] split = string.split(useWordi);
		if (split.length > 2) {// 进入操作，进入操作
		    for (String si : split) {

		    }
		} else {
		    String metaName = string.replace(useWordi, "");
		    if (metaName.length() > 1) {
			Map<String, Object> metaNode = getNode(META_DATA, "name", metaName);
			if (metaNode != null) {
			    String label = label(metaNode);
			    getInto(label, sessionId);
			    setMayConextProp(OPERATE_META, metaNode, sessionId);
			}
			return metaNode;
		    }
		}
	    }
	}
	return null;
    }

    private Map<String, Object> manageSomething(String string, String manageWordi, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(manageWordi) > -1) {
	    if (string != null && string.startsWith(manageWordi)) {
		String[] split = string.split(manageWordi);
		if (split.length > 2) {// 进入操作，进入操作

		} else {
		    String metaName = string.replace(manageWordi, "");
		    if (metaName.length() > 1) {
			String queryByNameOrCode = "Match(n:MetaData) where n.name  CONTAINS '" + metaName
				+ "' OR n.code  CONTAINS '" + metaName + "'  OR n.label  CONTAINS '" + metaName
				+ "' return n";
			List<Map<String, Object>> queryList = neo4jService.cypher(queryByNameOrCode);
			List<String> manageHref = new ArrayList<>();
			if (queryList != null && !queryList.isEmpty()) {
			    setMayConextProp(OPERATE_META_LIST, queryList, sessionId);
			    for (Map<String, Object> mi : queryList) {

				String xxi = "<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/po/" + label(mi)
					+ "')\"> 【" + name(mi) + "】</a>";
				if (manageHref.size() > 0) {
				    manageHref.add("、" + xxi);
				} else {
				    manageHref.add(xxi);
				}
			    }
			}
			Map<String, Object> data = new HashMap<>();
			data.put("data", manageHref);
			return data;
		    }
		}
	    }
	}
	return null;
    }

    private Map<String, Object> useObject(String string, String useWordi, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(useWordi) > -1) {
	    if (string != null && string.startsWith(useWordi)) {
		String objectName = string.replace(useWordi, "");
		String trimName = objectName.trim();
		if (trimName.length() > 1) {
		    Map<String, Object> objectNode = getNode(getOperateLabel(sessionId), "name", trimName);
		    if (objectNode != null) {
			setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);
		    }
		    return objectNode;
		}
	    }
	}
	return null;
    }

    private void getInto(String label, String sessionId) {
	Map<String, Object> myContext = getMyContext(sessionId);
	myContext.put(OPERATE_LABEL, label);
    }

    private void setMayConextProp(String key, Object value, String sessionId) {
	getMyContext(sessionId).put(key, value);
    }

    private Map<String, Object> getMyContext(String sessionId) {
	Map<String, Object> myContext = context.get(sessionId);
	if (myContext == null) {
	    myContext = new HashMap<>();
	    context.put(sessionId, myContext);
	}
	return myContext;
    }

    private String getMyKey() {
	String currentUserName = adminService.getCurrentAccount();
	String currentUserId = adminService.getCurrentPasswordId() + "";
	String userkey = currentUserId + "-" + currentUserName;
	return userkey;
    }

    private String getOperateLabel(String sessionId) {
	return string(getMyContext(sessionId), OPERATE_LABEL);
    }

    private String getColumnByHeader(String headeri, String sessionId) {
	Map<String, Object> metaMap = mapObject(getMyContext(sessionId), OPERATE_META);
	return getColByHeader(metaMap, headeri);
    }

    private Map<String, Object> getOperateObject(String sessionId) {
	return mapObject(getMyContext(sessionId), OPERATE_OBJECT);
    }

    private Map<String, Object> getOperateRelation(String sessionId) {
	return mapObject(getMyContext(sessionId), OPERATE_RELATION);
    }

    private void addSomeRealtion(String string, String newRelWord, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(newRelWord) > -1) {
	    String[] create = string.split(newRelWord);
	    for (String createObject : create) {
		if (createObject != null && !"".equals(createObject.trim()) && createObject.startsWith("关系")) {
		    addOneRelation(createObject, sessionId);
		}
	    }
	}
    }

    private void addTaRealtion(String string, String newRelWord, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(newRelWord) > -1 && string.startsWith(newRelWord)) {
	    String[] create = string.split(newRelWord);
	    if (create[1] != null && !"".equals(create[1].trim())) {
		addTaAndXIsRelation(create[1], sessionId);
	    }
	}
    }

    private void addIsTaDeRealtion(String string, String newRelWord, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(newRelWord) > -1 && string.startsWith(newRelWord)) {
	    String[] relOne = string.split(newRelWord);
	    if (relOne[1] != null && !"".equals(relOne[1].trim())) {
		isTadeXRelation(relOne[1], sessionId);
	    }
	}
    }

    private Map<String, Object> addTaDeProp(String string, String newRelWord, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(newRelWord) > -1 && string.startsWith(newRelWord)) {
	    String[] prop = string.split(newRelWord);
	    if (prop[1] != null && !"".equals(prop[1].trim())) {
		return addTadeProp(prop[1], sessionId);
	    }
	}
	return null;
    }

    private void addRelationProp(String string, String newRelWord, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(newRelWord) > -1 && string.startsWith(newRelWord)) {
	    String[] prop = string.split(newRelWord);
	    if (prop[1] != null && !"".equals(prop[1].trim())) {
		addRelDeProp(prop[1], sessionId);
	    }
	}
    }

    private void addRelDeProp(String itIs, String sessionId) {
	String key = "";
	String value = "";

	for (String ui : kEqualv) {
	    if (itIs.indexOf(ui) > -1) {
		String[] prop = itIs.split(ui);
		key = prop[0].trim();
		value = prop[1].trim();

		Map<String, Object> nodeMap = getOperateRelation(sessionId);
		if (nodeMap != null) {
		    Map<String, Object> data = new HashMap<>();
		    data.put(key, value);
		    Long startId = id(nodeMap);
		    neo4jService.saveRelById(startId, nodeMap);
		}
	    }
	}

    }

    private Map<String, Object> addTadeProp(String itIs, String sessionId) {
	String key = "";
	String value = "";
	Map<String, Object> startNode = getOperateObject(sessionId);

	for (String ui : kEqualv) {
	    if (itIs.indexOf(ui) > -1) {
		String[] prop = itIs.split(ui);
		key = prop[0].trim();
		// 判断可以是否是关系，如朋友，父亲，上级，下级，后续，前序。
		Map<String, Object> endNode = null;
		if (relName.contains(key)) {
		    // 添加关系,同类中找
		    endNode = getNode(getOperateLabel(sessionId), NAME, value);
		} else {// 跨元数据关系
		    Map<String, Object> node = getNode(META_DATA, NAME, key);
		    if (node != null) {
			endNode = getNode(label(node), NAME, value);
		    }
		}
		if (endNode != null) {
		    Relationship addRel = null;
			    relationService.addRel(key, string(startNode, ID), string(endNode, ID));
		    if (addRel != null) {
			return addRel.getAllProperties();
		    }
		}

		value = prop[1].trim();

		if (startNode != null) {
		    String columnKey = getColumnByHeader(key, sessionId);
		    startNode.put(columnKey, value);
		    neo4jService.saveById(string(startNode, ID), startNode);
		    return startNode;
		}
	    }
	}
	return null;

    }

    /**
     * 是xxx的朋友
     * 
     * @param itIs
     * @param sessionId
     */
    private void isTadeXRelation(String itIs, String sessionId) {
	String endName = "";
	String relName = "";

	for (String ui : ownWords) {
	    if (itIs.indexOf(ui) > -1) {
		String[] prop = itIs.split(ui);
		endName = prop[0].trim();
		relName = prop[1].trim();

		addRelateInCurrentMeta(sessionId, endName, relName);
	    }
	}

    }

    /**
     * 在当前元数据中添加关系
     * 
     * @param sessionId
     * @param endName
     * @param relName
     */
    private void addRelateInCurrentMeta(String sessionId, String endName, String relName) {
	Map<String, Object> endNode = getNode(getOperateLabel(sessionId), NAME, endName);

	Map<String, Object> startNode = getOperateObject(sessionId);
	if (endNode != null && startNode != null) {
	    Map<String, Object> data = new HashMap<>();
	    data.put(NAME, relName);
	    Long endId = id(endNode);
	    Long startId = id(startNode);
	    relationService.addRel(relName, startId, endId, data);
	    setMayConextProp(OPERATE_RELATION, data, sessionId);
	}
    }

    /**
     * ta和tb是朋友
     * 
     * @param itIs
     * @param sessionId
     */
    private void addTaAndXIsRelation(String itIs, String sessionId) {
	String endName = "";
	String relName = "";
	for (String ui : isRel) {
	    if (itIs.indexOf(ui) > -1) {
		String[] prop = itIs.split(ui);
		endName = prop[0].trim();
		relName = prop[1].trim();

		Map<String, Object> endNode = getNode(getOperateLabel(sessionId), "name", endName);

		Map<String, Object> startNode = getOperateObject(sessionId);
		if (endNode != null && startNode != null) {
		    Map<String, Object> data = new HashMap<>();
		    data.put(NAME, relName);
		    relationService.addRel(relName, id(startNode), id(endNode), data);
		}
	    }
	}

    }

    /**
     * xx的relation是bb xx是bb的relation
     * 
     * 操作关系：同类关系，不同元数据关系。 关系属性。 给用户xx添加一个账号。账号是名称全拼。
     * 
     * 方向关系：出关系，入关系。 开始节点是sss叫sp,ta的属性x是dd 结束节点是eee叫ep,ta的属性x是dd 关系属性： 开始时间，结束时间，状态
     * 如何写脚本识别？
     * 
     * @param createObject
     */
    private void addOneRelation(String createObject, String sessionId) {
	String[] subject = createObject.split("的");
	String so = subject[1];
	String si = subject[1];
	String relationName = "";
	String relEndObject = "";
	for (String ki : kEqualv) {
	    if (si.indexOf(ki) > -1) {
		String[] prop = si.split(ki);
		relationName = prop[0].trim();
		relEndObject = prop[1].trim();
		// relEndObject = splitByPronoun(relEndObject, sessionId);

		Map<String, Object> node = getNode(META_DATA, "name", relationName);
		if (node != null) {
		    String label = label(node);
		    Map<String, Object> data = new HashMap<>();
		    data.put(NAME, relEndObject);
		    data.put(LABEL, label);
		    Node saveByBody = neo4jService.saveByBody(data, label);
		    break;
		}
	    }
	}

    }

    /**
     * 属性中文名是叫为等于xx的xx
     * 
     * @param createObject
     */
    private Map<String, Object> recognitDifferntObject(String createObject) {
	String[] subject = createObject.split("的");
	String so = subject[1];
	String metaName = subject[1];
	String key = "";
	String value = "";
	for (String ni : kEqualv) {
	    if (so.indexOf("ni") > -1) {
		String[] prop = so.split(ni);
		key = prop[0].trim();
		value = prop[1].trim();

		Map<String, Object> metaNode = getNode(META_DATA, "name", metaName);

		if (metaNode != null) {
		    String label = label(metaNode);
		    Map<String, String> nameColumn = nameColumn(metaNode);
		    String keyCode = nameColumn.get(key);
		    return getNode(label, keyCode, value);
		}
	    }
	}
	return null;

    }

    /**
     * xxx的属性kkkk修改为vvv
     * 
     * @param createObject
     * @param metaName
     */
    private List<Map<String, Object>> updateOne(String createObject, String metaName) {
	String[] subject = createObject.split("的");
	String objectName = subject[0];
	String propName = subject[1];
	propName = clearFuhao(propName);
	List<String> wcSet = Arrays.asList("修改为", "修改成", "改为", "改成", "变更为", "变更成", "更新成", "更新为", "刷新成", "刷新为");
	String key = null;
	String value = null;
	for (String ni : wcSet) {
	    if (propName.indexOf(ni) > -1) {
		String[] prop = propName.split(ni);
		key = prop[0].trim();
		value = prop[1].trim();
	    }
	}
	List<Map<String, Object>> retUpdate = new ArrayList<>();
	Map<String, Object> metaNode = getNode(META_DATA, "name", metaName);
	if (metaNode != null) {
	    String label = label(metaNode);
	    Map<String, Object> objectNode = getNode(label, "name", objectName);
	    if (objectNode != null) {
		Long id = id(objectNode);
		Map<String, Object> data = new HashMap<>();
		String colByHeader = getColByHeader(metaNode, key);
		data.put(colByHeader, value);
		neo4jService.update(data, id);
		retUpdate.add(data);
	    }
	}
	return retUpdate;
    }

    private Map<String, Object> createOne(String createObject, String sessionId) {
	String propName = getPropName(createObject);
	propName = clearFuhao(propName);
	String metaName = "";
	String name = "";
	Map<String, Object> node = null;
	if (propName.indexOf("叫") > -1) {
	    String[] prop = propName.split("叫");
	    metaName = prop[0].trim();
	    name = prop[1].trim();
	    name = splitByPronoun(name, sessionId);
	    node = getNode(META_DATA, "name", metaName);
	} else {

	    String operateLabel = getOperateLabel(sessionId);
	    Map<String, Object> data = new HashMap<>();
	    data.put(NAME, propName);
	    data.put(LABEL, operateLabel);
	    Node saveByBody = neo4jService.saveByBody(data, operateLabel);
	    return data;
	}
	if (node != null) {
	    String label = label(node);
	    Map<String, Object> data = new HashMap<>();
	    data.put(NAME, name);
	    data.put(LABEL, label);
	    Node saveByBody = neo4jService.saveByBody(data, label);
	    return data;
	}
	return null;
    }

    private String getPropName(String createObject) {
	if (createObject.indexOf("个") < 0) {// 没有两次
	    return createObject;
	}
	String[] subject = createObject.split("一个");
	String propName = subject[1];
	return propName;
    }

    private String clearFuhao(String propName) {
	propName = propName.replaceAll(",", "");
	propName = propName.replaceAll("，", "");
	propName = propName.replaceAll("、", "");
	propName = propName.replaceAll("。", "");
	propName = propName.replaceAll("<div><br></div>", "");
	propName = propName.replaceAll("</pre>", "");

	return propName;
    }

    private Map<String, Object> deleteOne(String string, String delKey, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(delKey) > -1) {
	    String[] dels = string.split(delKey);
	    String delObject = dels[1];
	    String objName = "";
	    if (delObject.indexOf("个") > -1) {
		String[] subject = delObject.split("个");
		objName = subject[1];
	    } else {
		objName = delObject;
	    }

	    String name = objName;
	    String label = getOperateLabel(sessionId);
	    Map<String, Object> data = new HashMap<>();
	    data.put(NAME, name);
	    neo4jService.removeNodeByPropAndLabel(data, label);
	    if (string(data, ID) != null) {
		return data;
	    }
	}
	return null;
    }

    private void deleteSomething(String string, String delKey, String sessionId) {
	string = clearFuhao(string);
	if (string.indexOf(delKey) > -1) {
	    String[] create = string.split(delKey);
	    String createObject = create[1];
	    String propName = "";
	    if (createObject.indexOf("个") > -1) {
		String[] subject = createObject.split("一个");
		propName = subject[1];
	    } else {
		propName = createObject;
	    }

	    String metaName = "";
	    String name = "";
	    if (propName.indexOf("叫") > -1) {
		String[] prop = propName.split("叫");
		metaName = prop[0].trim();
		name = prop[1].trim();
		name = splitByPronoun(name, sessionId);
	    } else {
		metaName = splitByPronoun(propName, sessionId);
	    }

	    Map<String, Object> node = getNode(META_DATA, "name", metaName);
	    String label = label(node);
	    Map<String, Object> data = new HashMap<>();
	    data.put(NAME, name);
	    data.put(LABEL, label);
	    neo4jService.removeNodeByPropAndLabel(data, label);
	}
    }

    private String splitByPronoun(String name, String sessionId) {
	if (name.indexOf("他") > -1) {
	    String[] split = name.split("他");
	    name = split[0];
	    parseAndexcute(split[1], sessionId);
	}
	if (name.indexOf("它") > -1) {
	    String[] split = name.split("它");
	    name = split[0];
	    parseAndexcute(split[1], sessionId);
	}
	if (name.indexOf("她") > -1) {
	    String[] split = name.split("她");
	    name = split[0];
	    parseAndexcute(split[1], sessionId);
	}
	return name;
    }

}

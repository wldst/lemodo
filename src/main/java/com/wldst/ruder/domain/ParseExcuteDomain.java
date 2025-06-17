package com.wldst.ruder.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.util.LoggerTool;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.module.ws.web.ContextServer;
import com.wldst.ruder.util.CrudUtil;

/**
 * 解析聊天数据，并根据数据更新或者查询数据
 */
@Component
public class ParseExcuteDomain extends VoiceOperateDomain {
    final static Logger logger = LoggerFactory.getLogger(ParseExcuteDomain.class);
    @Autowired
    protected CrudNeo4jService neo4jService;
    @Autowired
    protected RelationService relationService;
    @Autowired
    protected CrudUserNeo4jService neo4jUService;
    @Autowired
    protected ObjectService objectService;
    @Autowired
    protected UserAdminService adminService;
    @Autowired
    protected CrudUtil crudUtil;
    @Autowired
    protected BeanShellService bs;
    

    private Map<String, Map<String, Object>> context = new HashMap<>();
    protected static List<String> stackQuit = Arrays.asList("退出", "返回", "返回上一级");// 唤醒词
    protected static List<String> operateStack = Arrays.asList("操作", "使用", "进入", "得到");// 唤醒词

    protected static List<String> getUseWords = Arrays.asList("操作", "使用", "获取", "得到", "获取最新的", "关于", "拿到", "找到");
    // 修改前缀
    protected static List<String> newUpdate = Arrays.asList("把", "将", "被", "修改", "更新", "update");

    protected static List<String> updates = Arrays.asList("保存", "更新", "update", "save");
    protected static List<String> help = Arrays.asList("help", "帮助文档", "帮助", "帮帮忙", "说明");
    protected static List<String> auth = Arrays.asList("给", "将");
    protected static List<String> authAdd = Arrays.asList("添加", "增加", "授予", "授权");
    // 新的关系
    protected static List<String> newRelation = Arrays.asList("创建关系", "新增关系", "添加关系", "添加联系", "新建关系", "有关系", "更新关系", "保存关系");
    // 新建节点
    protected static List<String> newNode = Arrays.asList("创建", "新增", "保存", "新建", "添加", "new", "create", "save");

    protected static List<String> openWord = Arrays.asList("打开", "文档", "查看", "详情");
    protected static List<String> me = Arrays.asList("我", "me", "wo", "俺");
    protected static List<String> manageNode = Arrays.asList("管理", "manage", "操作", "处理", "列表");
    protected static List<String> upperWords = Arrays.asList("大写", "upper", "upperCase", "toUpperCacse");
    protected static List<String> lowerWords = Arrays.asList("小写", "lower", "lowerCase", "toLowerCase");
    
    // 所属
    protected static List<String> ownWords = Arrays.asList("的", "地", "得", "所属", "隶属的");
    // 动词读取关系
    protected static List<String> actionWords = Arrays.asList("做", "干", "读", "听", "说", "学", "想", "写", "完成", "接龙");
    // 谓词
    protected static List<String> kEqualv = Arrays.asList("是", "有", "等于", "叫", "为", "=");
    // 获取关系，修改关系属性
    protected static List<String> relProp = Arrays.asList("关系属性");
    protected static List<String> relName = Arrays.asList("属于", "签字", "朋友", "孩子", "父亲", "上级", "下级", "后序", "前序");

    protected static List<String> need = Arrays.asList("需要", "去", "need", "要");
    protected static List<String> count = Arrays.asList("有多少", "how much", "多少");
    protected static List<String> countWord = Arrays.asList("个", "条", "次", "头", "只", "伙", "辆", "种", "丛", "场", "扎", "顿", "对");
    protected static List<String> done = Arrays.asList("完成", "签名", "填写", "填报", "报名");
    // 是某某关系
    protected static List<String> isRel = Arrays.asList("是", "在", "一起", "一同", "俩", "两个");
    protected static List<String> andRel = Arrays.asList("和", "跟", "与", "、", " AND ", " and ", " && ");
    protected static List<String> between = Arrays.asList("之间的");
    // 删除信息
    protected static List<String> removes = Arrays.asList("删除", "去除", "注销", "清理", "清除", "处理掉", "delete", "remove");
    protected static List<String> deleteWords = Arrays.asList("禁止", "删除", "注销", "清除", "去掉", "去除", "delete", "remove", "del");
    protected static List<String> deleteRels = Arrays.asList("禁止关系", "删除关系", "注销关系", "清除关系", "去掉关系", "去除关系", "deleteRel", "removeRel",
	    "delRel");

    protected static String zuoKuohao = "(";
    protected static String cnLeftKuoHao = "（";
    protected static String rightKuohao = ")";
    protected static String cnRightKuoHao = "）";
    protected static String USED = "used";

    @ServiceLog(description = "根据Label，key，value获取Id")
    public Long getId(String label, String key, String value) {
	return neo4jService.getNodeId(key, value, label);
    }

    @ServiceLog(description = "根据Label，key，value获取节点")
    public Map<String, Object> getNode(String label, String key, String value) {
	return neo4jService.getAttMapBy(key, value, label);
    }

    public void used(Map<String, Object> context) {
	context.put(USED, true);
    }
    public Boolean isUsed(Map<String, Object> context) {
	return bool(context,USED);
    }
    /**
     * 格式化命令，清理口语助词
     * 
     * @param commandText
     * @return
     */
    @ServiceLog(description = "格式化命令，清理口语助词")
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
    @ServiceLog(description = "查找Label为label所有node，遍历node.field包含from的数据，并替换内容中from的字符串为to")
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
    @ServiceLog(description = "查找Label为label所有节点，遍历node.field包含from的数据，并替换内容中from的字符串为to")
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

    @ServiceLog(description = "精确查找Node的label为label参数,且field=from的节点，迭代这些节点，并替换字段内容from为to")
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

     
    @ServiceLog(description = "记录执ChatSys行结果")
    public void recordExcute(String commandText, Object parseAuthTalkAndexcute) {
	Map<String, Object> ret = new HashMap<>();
	ret.put("sentence", commandText);
	ret.put(STATUS, "200");
	ret.put("result", parseAuthTalkAndexcute);
	ret.put("userName", adminService.getCurrentName());
	neo4jUService.saveByBody(ret, "ChatSentence");
    }

      

    public void resetConversation(Map<String, Object> userContext) {
	userContext.put(USED, false);
	userContext.remove("currentName");
	userContext.remove("which");
    }
  

    public Map<String, Object> getDataOfKuohao(String msgx) {
	String meta = null;
	String[] split = msgx.split("\\(");
	if (split.length < 2) {
	    split = msgx.split(cnLeftKuoHao);
	}
	String msg = split[1];

	if (msg.endsWith(rightKuohao)) {
	    meta = msg.replace(rightKuohao, "");
	} else if (msg.endsWith(cnRightKuoHao)) {
	    meta = msg.replace(cnRightKuoHao, "");
	}
	Map<String, Object> data = newMap();
	data.put("split", String.join(",", split));
	data.put("meta", meta);

	return data;
    }

    public Map<String, Object> getOrSelectMetaData(String msg, Map<String, Object> context) {
	List<Map<String, Object>> metaDataByName = getMetaDataByName(msg);
	context.put("currentName", msg);
	Map<String, Object> mi = userSelect(context, metaDataByName);
	return mi;
    }

    public List<Map<String, Object>> relOf(Long idStart, Long idEnd) {
	return neo4jUService
		.cypher("MATCH (a)-[r]-(b) where id(a)=" + idStart + " and id(b)=" + idEnd + " return distinct r");
    }
    

    public Long getStartIdBy(Map<String, Object> conversationContext, String startName, String oneLabel) {
	Map<String, Object> selectedOne = getData(startName, oneLabel, conversationContext);
	Long startId = id(selectedOne);
	return startId;
    }

    public String userSelectAuthEnd(String msg, Map<String, Object> conversationContext, String startName,
	    Map<String, Object> endMeta, List<Map<String, Object>> preLabels) {
	String zhiJieRelData = null;

	Long startId = getStartIdBy(conversationContext, startName, preLabels);
	if (msg.endsWith("有哪些角色")) {
	    zhiJieRelData = "MATCH (n)-[r:HAS_PERMISSION]-(m:" + label(endMeta) + ")  where id(n)=" + startId
		    + " return distinct m ";
	} else {
	    zhiJieRelData = "MATCH (n)-[r]-(m:" + label(endMeta) + ")  where id(n)=" + startId + "  return distinct m ";
	}
	return zhiJieRelData;
    }

    public Long getStartIdBy(Map<String, Object> conversationContext, String startName,
	    List<Map<String, Object>> preLabels) {
	Map<String, Object> selectedOne = selectedData(conversationContext, startName, preLabels);
	Long startId = id(selectedOne);
	return startId;
    }

    

    /**
     * 
     * @param context
     * @param startName
     * @return
     */
    @ServiceLog(description = "StartName包含（元数据信息），返回去除元数据的名称，并在Context中，返回 context.put(\"dataLabel\", label(di));\n"
	    + "		    context.put(\"dataMd\", di);信息")
    public String onlyName(Map<String, Object> context, String startName) {
	String name = getMetaInfo(startName, zuoKuohao, context);
	if (startName.equals(name)) {
	    name = getMetaInfo(startName, cnLeftKuoHao, context);
	}
	return name;
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
    @ServiceLog(description = "根据参数获取元数据信息")
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

    /**
     * 获取元数据的定义信息，并返回字段表头信息
     * 
     * @return
     */
    @ServiceLog(description = "获取元数据的定义信息，并返回字段表头信息")
    public Map<String, String> getMetaData() {
	String getMetaInfo = " MATCH (m:MetaData) where  m.label='" + META_DATA + "'  return distinct m";
	List<Map<String, Object>> ownerMetas = neo4jService.cypher(getMetaInfo);
	if (ownerMetas != null && !ownerMetas.isEmpty()) {
	    return nameColumn(ownerMetas.get(0));
	}
	return null;
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

    public void seeProperty(StringBuilder sb, String startName, String coli, Map<String, Object> oMdi,
	    Map<String, Object> context) {
	List<Map<String, Object>> datas;
	Map<String, Object> selectedOne = selectedData(context, startName);
	String getRelPropi = "MATCH (f:Field)   WHERE  f.field='" + coli + "' and f.objectId=" + id(oMdi)
		+ " return f.type,f.valueField";
	List<Map<String, Object>> fieldInfo = neo4jUService.cypher(getRelPropi);
	Long startId = id(selectedOne);
	if (!fieldInfo.isEmpty()) {
	    Map<String, Object> fi = fieldInfo.get(0);
	    String relTypeLabel = string(fi, "type");
	    String relTypeValue = string(fi, "valueField");

	    String getPropi = "MATCH (n:" + label(oMdi) + ") " + " WHERE id(n)=" + startId + " return n." + coli;
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
	    String getPropi = "MATCH (n:" + label(oMdi) + ") " + " WHERE id(n)=" + startId + " return n." + coli;
	    datas = neo4jUService.cypher(getPropi);
	    if (datas != null) {
		String coliValue = string(datas.get(0), coli);
		sb.append(coliValue);
	    }
	}
    }
 

    /**
     * 选择数据，当一个数据出现分支时，需要用户进行判断，具体执行哪一个分支。
     *
     * @param context
     * @param si
     */
    protected Map<String, Object> selectedData(Map<String, Object> context, String si) {
	List<Map<String, Object>> sis = getData(si);
	context.put("currentName", si);
	return userSelect(context, sis);
    }
    
    protected Map<String, Object> moreSelectedData(Map<String, Object> context, String si) {
	List<Map<String, Object>> sis = queryData(si);
	context.put("currentName", si);
	context.put("selectList", sis);
	
	return userSelect(context, sis);
    }

    protected Map<String, Object> selectedData(Map<String, Object> context, String si,
	    List<Map<String, Object>> preLabels) {
	List<String> listMap2List = listMap2ListString("x", preLabels);
	List<Map<String, Object>> dataBy = neo4jUService.getDataBy(listMap2List, si);

	context.put("currentName", si);
	return userSelect(context, dataBy);
    }

    public Map<String, Object> userSelect(Map<String, Object> context, List<Map<String, Object>> sis) {
	if (sis != null && !sis.isEmpty()) {
	    if (sis.size() > 1) {
		Object converation = context.get(WebSocketDomain.CONVERSATION);
		if (converation != null) {
		    LoggerTool.info(logger,"出现多个选项，需要用户选择，且用户已连上Websocket");
		    ContextServer cs = (ContextServer) converation;
		    try {
			String object = string(context, "currentName");
			if (object == null) {
			    object = "";
			}
			cs.sendMessage("\n\n\n" + object + "有多选，请选择：\n<br>" + options(sis) + "\n\n\n");
			Integer selected = integer(context, "which");
			int count = 0;

			while (selected == null && count < 50) {
			    try {
				Thread.sleep(1000);
				LoggerTool.info(logger,"等待用户选择");
				selected = integer(context, "which");
				count++;
			    } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			}
			context.remove("currentName");
			context.remove("which");
			if (selected == null) {
			    return null;
			}
			return sis.get(selected);
		    } catch (IOException e1) {
			e1.printStackTrace();
			LoggerTool.error(logger,e1.getMessage(), e1);
		    }
		} else {
		    return sis.get(0);
		}

	    } else {
		return sis.get(0);
	    }

	}
	return null;
    }

    private String options(List<Map<String, Object>> sis) {
	StringBuilder sb = new StringBuilder();
	int i = 0;
	for (Map<String, Object> mi : sis) {
	    if (sb.length() > 0) {
		sb.append(" 、 ");
	    }
	    String seeNode = neo4jUService.seeNode(mi);
	    sb.append("<input type=radio onclick=\"mySelect('" + i + "')\">" + (i + 1) + "【" + label(mi) + "】" + id(mi)
		    + seeNode + "</input>");
	    i++;
	}
	return sb.toString();
    }
 

    

    public Long replacePronoun(Map<String, Object> context, String userRole) {
	boolean useMe = false;
	Long startId = null;
	for (String mi : me) {
	    if (userRole.equals(mi)) {
		startId = longValue(context, "MyId");
		useMe = true;
	    }
	}
	if (!useMe) {
	    startId = getIdOfRoleOrUser(userRole);
	}
	return startId;
    }

    public String answer(Map<String, Object> context) {
	return string(context,"answer");
    }

    public boolean createOneRel(Map<String, Object> context, boolean created, String end, String rel, Long startId) {
	Long endId = getIdOfData(end, context);
	String relCode = null;
	// 判断是否存在中文和应为关系名称和代码
	Map<String, Object> propsMap = null;
	int rightx = rel.indexOf("}");
	int leftX = rel.indexOf("{");
	if (leftX >= 0 && rightx > 0) {
	    String props = rel.substring(leftX, rightx + 1);
	    propsMap = JSON.parseObject(props);
	    rel = rel.split("\\{")[0];
	}

	if (rel.contains("(") && rel.contains(")")) {
	    String[] codePart = rel.split("\\(");
	    relCode = codePart[1].replaceAll("\\)", "");
	    rel = codePart[0];

	} else if (!rel.contains("(") && !rel.contains(")")) {
	    Map<String, Object> data2 = getData(rel, context);
	    if (data2 != null) {
		relCode = string(data2, "reLabel");
	    }
	}
	if (endId != null) {
	    updateRelDefine(startId, endId, rel, relCode);
	    if (propsMap != null && !propsMap.isEmpty()) {
		createRel(startId, endId, rel, relCode, propsMap);
	    } else {
		createRel(startId, endId, rel, relCode);
	    }

	    created = true;
	    context.put(USED, true);
	}
	return created;
    }

    /**
     * 清理大括号
     * 
     * @param split2
     * @return
     */
    public String clearBigkh(String split2) {
	return split2.replaceAll("\\}", "").replaceAll("\\{", "");
    }
 

    /**
     * 添加关系，开始节点句子中包含(元数据信息)
     * 
     * @param start
     * @param rightOfIs
     */
    public void addStartMetaRel2End(String start, String rightOfIs, Map<String, Object> conversation) {
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
		startAddRel(rightOfIs, id(di), name(mi), conversation);
	    }
	}
    }

    /**
     * 删除关系
     * 
     * @param start
     * @param rightOfIs
     * @param conversation
     */
    public void delStartMetaRel2End(String start, String rightOfIs, Map<String, Object> conversation) {
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
		startDelRel(rightOfIs, id(di), name(mi), conversation);
	    }
	}
    }

    /**
     * 收集包含括号（元数据）的开始节点ID
     * 
     * @param startIds
     * @param start
     */
    public void addStartId(List<Long> startIds, String start, Map<String, Object> context) {
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
	Map<String, Object> si = userSelect(context, metaDataByName);
	List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(si), split[0]);
	for (Map<String, Object> di : dataBy) {
	    startIds.add(id(di));
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
    private void handleUpdate(String msg, Map<String, Object> context) {
	for (String updatei : updates) {
	    if (msg.startsWith(updatei)) {
		context.put(USED, true);
		// 租户数据授权？该如何授予权限？
		String noPrefix = msg.replaceFirst(updatei, "");
		// 找到等于
		for (String eqi : kEqualv) {
		    if (noPrefix.contains(eqi)) {
			String[] startEndOfRel = noPrefix.split(eqi);
			// 租户数据授权？该如何授予权限？
			handleLeftAndRight(startEndOfRel[0], startEndOfRel[1], context);
		    }
		}
		for (String eqi : relName) {
		    if (noPrefix.contains(eqi)) {
			String[] startEndOfRel = noPrefix.split(eqi);
			// 租户数据授权？该如何授予权限？
			addStartMetaRel2End(startEndOfRel[0], startEndOfRel[1], context);
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
    private void handleLeftAndRight(String left, String rightOfIs, Map<String, Object> context) {
	boolean leftHasOi = false;
	for (String oi : ownWords) {
	    if (left.contains(oi)) {
		leftHasOi = true;
		String[] leftBelong = left.trim().split(oi);
		String objectStr = leftBelong[0];
		List<Long> startIds = parseObjectsId(context, objectStr);

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
			// 没有字段则直接添加关系
			startAddRel(rightOfIs, startId, propOrRel, context);
		    }
		}
	    }
	}
	if (!leftHasOi) {
	    // 没有字段则直接添加关系
	    List<Long> startIds = parseObjectsId(context, left);
	    for (Long si : startIds) {
		startAddRel(rightOfIs, si, context);
	    }
	}
    }

    /**
     * 解析字符串中包含的对象，并返回去对象Id列表
     * 
     * @param context
     * @param objectStr
     * @return
     */
    public List<Long> parseObjectsId(Map<String, Object> context, String objectStr) {
	boolean useAnd;
	List<Long> startIds = new ArrayList<>();
	useAnd = handleAnd(context, objectStr, startIds);

	if (!useAnd) {
	    if (containLabelInfo(objectStr)) {
		Map<String, Object> onlyContext = new HashMap<>();
		objectStr = onlyName(onlyContext, objectStr);
		String dataLabel = string(onlyContext, "dataLabel");
		Map<String, Object> data2 = getData(objectStr, dataLabel, context);
		startIds.add(id(data2));
	    } else {
		Long startId = getIdOfData(objectStr, context);
		if (startId != null) {
		    startIds.add(startId);
		}
	    }
	}
	return startIds;
    }

    /**
     * 针对字符串处理是否包含与、和等关键字。
     * 
     * @param context
     * @param objectStr
     * @param startIds
     * @return
     */
    public boolean handleAnd(Map<String, Object> context, String objectStr, List<Long> startIds) {
	boolean useAnd = false;
	for (String qie : andRel) {
	    if (objectStr.contains(qie)) {
		String[] lefts = objectStr.split(qie);
		for (String li : lefts) {
		    if (containLabelInfo(li)) {
			Map<String, Object> onlyContext = new HashMap<>();
			li = onlyName(onlyContext, li);
			String dataLabel = string(onlyContext, "dataLabel");
			Map<String, Object> data2 = getData(li, dataLabel, context);
			startIds.add(id(data2));
		    } else {
			Long idOfData = getIdOfData(li, context);
			if (idOfData != null) {
			    startIds.add(idOfData);
			}
		    }

		}
		useAnd = true;
	    }
	}
	return useAnd;
    }

    private boolean handelDelNode(String xx, Map<String, Object> funContext) {
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
			Map<String, Object> data2 = getData(ri, dataLabel, funContext);
			Long idOfData = id(data2);
			objIds.add(idOfData);
		    } else {
			objIds.add(getIdOfData(ri, funContext));
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

     

    

   

    

    @ServiceLog(description = "删除关系，开始节点和结束节点的所有关系")
    public void deleteRel(Long startId, Long endId) {
	String cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " delete r";
	neo4jService.execute(cypher);
    }

    @ServiceLog(description = "删除关系，开始节点和结束节点的某个关系")
    public void deleteRel(Long startId, Long endId, String name) {
	String cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and id(e)=" + endId + " delete r";
	if (name != null && !"".equals(name.trim())) {
	    cypher = " MATCH(s)-[r]->(e)  where id(s)=" + startId + " and r.name=\"" + name + "\" and id(e)=" + endId
		    + " delete r";
	}
	neo4jService.execute(cypher);
    }

    /**
     * 添加关系
     * 
     * @param rightOfIs
     * @param startId
     */
    @ServiceLog(description = "给开始节点添加关系")
    public void startAddRel(String rightOfIs, Long startId, Map<String, Object> userContext) {
	Long endId;
	boolean rigthHasOwniWord = false;
	for (String owni : ownWords) {
	    if (rightOfIs.contains(owni)) {
		rigthHasOwniWord = true;
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
	if (!rigthHasOwniWord) {
	    // 获取结束节点的名称
	    if (containLabelInfo(rightOfIs)) {
		startAddRel(startId, cnLeftKuoHao, rightOfIs);
		startAddRel(startId, zuoKuohao, rightOfIs);
	    } else {
		// 不应该出现A是B？
		endId = id(getData(rightOfIs, userContext));
		if (endId != null) {// 有关系节点
		    createRel(startId, endId, rightOfIs);
		}
	    }

	}
    }

    public void startDelRel(String rightOfIs, Long startId, Map<String, Object> userContext) {
	Long endId;
	boolean rigthHasOwniWord = false;
	for (String owni : ownWords) {
	    if (rightOfIs.contains(owni)) {
		rigthHasOwniWord = true;
		String[] whoSResource = rightOfIs.split(owni);
		String who = whoSResource[0];

		String propOrRel = whoSResource[1];

		if (containLabelInfo(who)) {
		    delAuthRelWithLabelInfo(startId, who, propOrRel, zuoKuohao);
		    delAuthRelWithLabelInfo(startId, who, propOrRel, cnLeftKuoHao);
		} else {
		    endId = getIdOfMd(who);
		    delRel(startId, endId, propOrRel);
		}
	    }
	}
	if (!rigthHasOwniWord) {
	    // 获取结束节点的名称
	    if (containLabelInfo(rightOfIs)) {
		startDelRel(startId, cnLeftKuoHao, rightOfIs);
		startDelRel(startId, zuoKuohao, rightOfIs);
	    } else {
		// 不应该出现A是B？
		endId = id(getData(rightOfIs, userContext));
		if (endId != null) {// 有关系节点
		    delRel(startId, endId, rightOfIs);
		}
	    }

	}
    }

    public void startAddRel(String rightOfIs, Long startId, String relName, Map<String, Object> userContext) {
	Long endId;
	boolean rightHasOwniWord = false;
	for (String owni : ownWords) {
	    if (rightOfIs.contains(owni)) {
		rightHasOwniWord = true;
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
	if (!rightHasOwniWord) {
	    // 不应该出现A是B？
	    endId = id(getData(rightOfIs, userContext));
	    if (endId != null) {// 有关系节点
		createRel(startId, endId, relName);
	    }
	}
    }

    /**
     * 删除关系
     * 
     * @param rightOfIs
     * @param startId
     * @param relName
     * @param userContext
     */
    public void startDelRel(String rightOfIs, Long startId, String relName, Map<String, Object> userContext) {
	Long endId;
	boolean rightHasOwniWord = false;
	for (String owni : ownWords) {
	    if (rightOfIs.contains(owni)) {
		rightHasOwniWord = true;
		String[] whoSResource = rightOfIs.split(owni);
		String who = whoSResource[0];

		String propOrRel = whoSResource[1];

		if (containLabelInfo(who)) {
		    delAuthRelWithLabelInfo(startId, who, propOrRel, zuoKuohao);
		    delAuthRelWithLabelInfo(startId, who, propOrRel, cnLeftKuoHao);
		} else {
		    endId = getIdOfMd(who);
		    deleteRel(startId, endId, propOrRel);
		}
	    }
	}
	if (!rightHasOwniWord) {
	    // 不应该出现A是B？
	    endId = id(getData(rightOfIs, userContext));
	    if (endId != null) {// 有关系节点
		deleteRel(startId, endId, relName);
	    }
	}
    }

    /**
     * 给开始节点添加xxxxxxxxxx
     * 
     * @param rightOfIs
     * @param startId
     */
    @ServiceLog(description = "给开始节点添加权限")
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
    @ServiceLog(description = "给开始节点添加关系，终点有元数据标识")
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
    @ServiceLog(description = "给开始节点添加权限关系，终点有元数据信息，带着终点和气属性或者关系参数")
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

    /**
     * 删除权限关系
     * 
     * @param startId
     * @param zuoKuohao2
     * @param who
     * @param por
     */
    public void delAuthRelWithLabelInfo(Long startId, String zuoKuohao2, String who, String por) {
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
		    delRel(startId, id(di), por);
		}
	    }
	}
    }

    /**
     * 
     * @param startId
     * @param zuoKuohao2
     * @param who
     */
    public void startAddRel(Long startId, String zuoKuohao2, String who) {
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
		    createRel(startId, id(di), meta);
		}
	    }
	}
    }

    /**
     * 删除关系
     * 
     * @param startId
     * @param zuoKuohao2
     * @param who
     */
    public void startDelRel(Long startId, String zuoKuohao2, String who) {
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
		    delRel(startId, id(di), meta);
		}
	    }
	}
    }

    @ServiceLog(description = "带着括号的字符串，获取其元数据")
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
	return who;
    }

    /**
     * 判断句子是否包含左右括号，包括中引文括号。
     * 
     * @param msg
     * @return 包含括号返回true
     */
    @ServiceLog(description = "判断是否包含括号")
    public boolean containLabelInfo(String msg) {
	Boolean metaStart = msg.contains(zuoKuohao) || msg.contains(cnLeftKuoHao);
	Boolean metaEnd = msg.contains(rightKuohao) || msg.contains(cnRightKuoHao);
	boolean hasMetaInfo = metaStart && metaEnd;
	return hasMetaInfo;
    }

    @ServiceLog(description = "给开始、结束节点添加关系：参数Long startId, Long endId, String relName")
    public void createRel(Long startId, Long endId, String relName) {
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
	if (authMap == null) {
	    return;
	}
	String relCode = string(authMap, "reLabel");
	String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId + " create (s)-[:" + relCode
		+ "{name:\"" + relName + "\",code:\"" + relCode + "\"}]->(e)";
	neo4jService.execute(cypher);
    }

    public void createRel(Long startId, Long endId, String relName, String relCode) {
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
	if (authMap != null) {
	    relCode = string(authMap, "reLabel");
	}
	
	String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId + " create (s)-[:" + relCode
		+ "{name:\"" + relName + "\",code:\"" + relCode + "\"}]->(e)";
	neo4jService.execute(cypher);
    }

    public void createRel(Long startId, Long endId, String relName, String relCode, Map<String, Object> props) {
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
	if (authMap != null) {
	    relCode = string(authMap, "reLabel");
	}
	props.put(CODE, relCode);
	props.put(NAME, relName);
	// +"{name:\"" + relName + "\",code:\"" + relCode + "\"}
	String mapString = mapString(props);

	String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId + " create (s)-[:" + relCode
		+ "{" + mapString + "}]->(e)";
	neo4jService.execute(cypher);
    }

    /**
     * 更新关系定义信息
     * 
     * @param startId
     * @param endId
     * @param relName
     * @param relCode
     */
    public void updateRelDefine(Long startId, Long endId, String relName, String relCode) {
	String endLabel = neo4jUService.getLabelByNodeId(endId);
	String startLabel = neo4jUService.getLabelByNodeId(startId);
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");

	Map<String, Object> map = newMap();
	// id,reLabel,name,startLabel,endLabel
	// 编码,标签,名称,关系方,被关系方,查询语句,查询列
	map.put("startLabel", startLabel);
	map.put("endLabel", endLabel);
	map.put("reLabel", relCode);
	map.put(NAME, relName);
	if (authMap == null) {
	    neo4jUService.saveByBody(map, "RelationDefine");
	} else {
	    boolean startNe = !string(authMap, "startLabel").equals(startLabel);
	    boolean endLabelNE = !string(authMap, "endLabel").equals(endLabel);
	    if (startNe || endLabelNE) {
		neo4jUService.saveByBody(map, "RelationDefine");
	    }
	}
    }

    public void delRel(Long startId, Long endId, String relName, String relCode) {
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
	if (authMap != null) {
	    relCode = string(authMap, "reLabel");
	}
	String cypher = "MATCH (s)-[r:" + relCode + "{name:\"" + relName + "\",code:\"" + relCode + "\"}]->(e) "
		+ " where id(s)=" + startId + " and id(e)=" + endId + " delete r";
	neo4jService.execute(cypher);
    }

    @ServiceLog(description = "给开始、结束节点删除关系：参数Long startId, Long endId, String relName")
    public void delRel(Long startId, Long endId, String relName) {
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "RelationDefine");
	if (authMap == null) {
	    return;
	}
	String relCode = string(authMap, "reLabel");
	String cypher = "MATCH (s)-[r:" + relCode + "{name:\"" + relName + "\"}]->(e) where id(s)=" + startId
		+ " and id(e)=" + endId + " delete r";
	neo4jService.execute(cypher);
    }

    /**
     * 给开始节点和结束节点添加权限关系
     * 
     * @param startId
     * @param endId
     * @param relName
     */
    @ServiceLog(description = "给开始、结束节点添加权限关系：参数Long startId, Long endId, String relName")
    public void addAuthRel(Long startId, Long endId, String relName) {
	String relCode;
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, relName, "permission");
	if (authMap != null) {
	    relCode = code(authMap);
	    String cypher = "MATCH (s),(e) where id(s)=" + startId + " and id(e)=" + endId
		    + " create (s)-[:HAS_PERMISSION{code:\"" + relCode + "\",name:\"" + relName + "\"}]->(e)";
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

    @ServiceLog(description = "根据参数获取元数据的Id")
    public Long getIdOfMd(String resource) {
	Long startId = null;
	List<Map<String, Object>> metaDataBy = neo4jUService.getMetaDataBy(resource);
	if (metaDataBy.size() >= 1) {
	    startId = id(metaDataBy.get(0));
	}
	return startId;
    }

    @ServiceLog(description = "根据参数获取元数据的Label")
    public String getLabelOfMd(String resource) {
	String labelData = null;
	List<Map<String, Object>> metaDataBy = neo4jUService.getMetaDataBy(resource);
	if (metaDataBy.size() == 1) {
	    labelData = label(metaDataBy.get(0));
	}
	return labelData;
    }

    @ServiceLog(description = "根据参数获取用户或者角色的ID")
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

    @ServiceLog(description = "根据参数：name，label，获取数据")
    public Map<String, Object> getData(String name, String labelOf, Map<String, Object> context) {
	List<Map<String, Object>> metaDataBy = neo4jUService.getDataBy(labelOf, name);
	return userSelect(context, metaDataBy);
    }

    @ServiceLog(description = "根据参数：name，获取数据,Context中用户选择信息")
    public Map<String, Object> getData(String name, Map<String, Object> context) {
	// String dataOfKuohao = getDataOfKuohao(name);
	List<Map<String, Object>> data = new ArrayList<>();
	if (containLabelInfo(name)) {
	    Map<String, Object> dataOfKuohao = getDataOfKuohao(name);
	    String meta = string(dataOfKuohao, "meta");
	    String[] split = strArray(dataOfKuohao, "split");
	    List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
	    for (Map<String, Object> mi : metaDataByName) {
		
		List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
		if (dataBy != null) {
		    context.put("metaName",name(mi));
		    data.addAll(dataBy);
		}
	    }
	} else {
	    data = neo4jUService.getDataBy(name);
	}
	if(data.size()==1||isSameList(data)) {
	    return data.get(0);
	}
	return userSelect(context, data);
    }
    
    @ServiceLog(description = "根据参数：name，获取数据,Context中用户选择信息")
    public List<Map<String, Object>> getDatas(String name, Map<String, Object> context) {
	// String dataOfKuohao = getDataOfKuohao(name);
	List<Map<String, Object>> data = new ArrayList<>();
	if (containLabelInfo(name)) {
	    Map<String, Object> dataOfKuohao = getDataOfKuohao(name);
	    String meta = string(dataOfKuohao, "meta");
	    String[] split = strArray(dataOfKuohao, "split");
	    List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
	    for (Map<String, Object> mi : metaDataByName) {
		List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
		if (dataBy != null) {
		    data.addAll(dataBy);
		}
	    }
	} else {
	    data = neo4jUService.getDataBy(name);
	}
	return data;
    }

    @ServiceLog(description = "根据参数：name，label，获取数据的ID")
    public Long getIdOfData(String name, String labelOf, Map<String, Object> context) {
	return id(getData(name, labelOf, context));
    }

    public List<Map<String, Object>> getData(String name) {
	return neo4jUService.getDataBy(name);
    }
    public List<Map<String, Object>> queryData(String name) {
	return neo4jUService.queryDataBy(name);
    }

    @ServiceLog(description = "根据参数：name，获取第一个数据的ID")
    public Long getIdOfData(String resource, Map<String, Object> context) {
	for (String mi : me) {
	    if (resource.equals(mi)) {
		return longValue(context, "MyId");
	    }
	}
	return id(getData(resource, context));
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
	String metai = "<a href=\"javascript:;\" onclick=\"window.open('"+ LemodoApplication.MODULE_NAME+"/layui/MetaData/documentRead?id="
		+ id(metaData) + "')\">" + name(metaData) + "</a>";
	return metai;
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
     * 根据会话ID 获取上下文
     * 
     * @param sessionId
     * @return
     */
    public Map<String, Object> getMyContext(String sessionId) {
	Map<String, Object> myContext = context.get(sessionId);
	if (myContext == null) {
	    myContext = new HashMap<>();
	    context.put(sessionId, myContext);
	}
	if (myContext.get("MyId") == null) {
	    myContext.put("MyId", sessionId);
	}
	return myContext;
    }

    public Map<String, Map<String, Object>> getContextmap() {
	return context;
    }


    protected String getOperateLabel(Map<String, Object> sessionId) {
	return string(sessionId, OPERATE_LABEL);
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

    

    public String getPropName(String createObject) {
	if (createObject.indexOf("个") < 0) {// 没有两次
	    return createObject;
	}
	String[] subject = createObject.split("一个");
	String propName = subject[1];
	return propName;
    }

    public String clearFuhao(String propName) {
	propName = propName.replaceAll(",", "");
	propName = propName.replaceAll("，", "");
	propName = propName.replaceAll("、", "");
	propName = propName.replaceAll("。", "");
	propName = propName.replaceAll("<div><br></div>", "");
	propName = propName.replaceAll("</pre>", "");

	return propName;
    }
 


}

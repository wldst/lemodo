package com.wldst.ruder.module.parse.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import com.wldst.ruder.util.LoggerTool;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;

/**
 * label: Cart name: 购物车 columns: isNotEmpty, storeGoods, invalidGoodItems,
 * isAllSelected, selectedGoodsCount, totalAmount, totalDiscountAmount headers:
 * 非空, 商店商品, 无效商品项, 全部选中, 选中商品数量, 总金额, 总折扣金额 添加元数据
 * 
 * @param msg
 * @param context
 */
@Component
public class JsonParse extends ParseExcuteDomain implements MsgProcess {
    final static Logger logger = LoggerFactory.getLogger(JsonParse.class);

    /**
     * 解析元数据执行
     * 
     * @param msg
     * @param context
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {
	if (isUsed(context)) {
	    return null;
	}
	for (String ni : newNode) {

	    if (!msg.startsWith(ni)) {
		continue;
	    }
	    if (isUsed(context)) {
		return null;
	    }
	    int indexOf = msg.indexOf("{");
	    int index2 = msg.indexOf("[");
	    if(indexOf<0) {
		continue;
	    }
	    String createSomeBody = msg.substring(0, indexOf);
	    if (index2 > 0 && index2 < indexOf) {
		createSomeBody = msg.substring(0, index2);
		indexOf = index2;
	    }

	    String label = createSomeBody.replaceAll(ni, "").trim();

	    Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	    if (md == null || md.isEmpty()) {
		md = neo4jService.getAttMapBy(NAME, label, META_DATA);		
		if (md == null) {
		    continue;
		}
		label=label(md);
	    }
	    try {
		String text = msg.substring(indexOf);
		text = text.replaceAll("'", "\"");
		text = text.replaceAll("\n", "");
		text = text.replaceAll("\t", "");
		text = text.replaceAll("\r", "");
		text = text.replaceAll(" ", "");
		if (text.startsWith("[")) {
		    List<Object> maps = toListMap(text);
		    for (Object mi : maps) {
			if (mi instanceof Map m) {
			    prorcessOne(context, label, md, m);
			}
		    }
		    // List<Map<String, Object>> maps = toListMap(text);
		    // for (Map<String, Object> mi : maps) {
		    // prorcessOne(context, label, md, mi);
		    // }
		    return maps;
		}
		if (text.startsWith("{")) {
		    Map<String, Object> parse = toMap2(text);
		    prorcessOne(context, label, md, parse);
		    return parse;
		}

	    } catch (Exception e) {
		LoggerTool.error(logger,e.getMessage(), e);
		return null;
	    }

	}
	return null;
    }

    public void prorcessOne(Map<String, Object> context, String label, Map<String, Object> md,
	    Map<String, Object> parse) {
	String one = neo4jService.getOne(
		"Match(t:TreeDefine) where t.mdLabel='" + label + "' return t.parentIdField AS parentIdField",
		"parentIdField");
	Map<String, Object> copy = copy(parse);
	if (one != null) {
	    clearTreeData(md, copy, one);
	}

	// JSON.parse
	List<Map<String, Object>> nexts = neo4jService.getOneRelationList(id(md), "vo");
	Long nodeId = addAndRelate(md, label(md), copy, nexts);
	parse.put(ID,nodeId);
	used(context);
	if (one != null) {
	    parentTree(md, label, parse, one);
	}
    }

    public List<Map<String, Object>> parseList(String content) throws IOException {
	JSONArray allGoods = JSON.parseArray(content);

	// Convert to Java List<Map>
	List<Map<String, Object>> list = new ArrayList<>();
	for (int i = 0; i < allGoods.size(); i++) {
	    Map<String, Object> map = new HashMap<>();
	    JSONObject item = allGoods.getJSONObject(i);
	    ;
	    for (Entry<String, Object> ei : item.entrySet()) {
		String key = ei.getKey();
		Object value = ei.getValue();
		map.put(key, value);
	    }
	    list.add(map);
	}
	return list;
    }

    public void clearTreeData(Map<String, Object> md, Map<String, Object> parsex, String one) {
	List<Map<String, Object>> listMap = listMapObject(parsex, REL_TYPE_CHILDREN);
	if (listMap != null && !listMap.isEmpty()) {
	    for (Map<String, Object> mi : listMap) {
		mi.remove(one);
		clearTreeData(md, mi, one);
	    }
	}
    }

    public void parentTree(Map<String, Object> md, String label, 
	    Map<String, Object> parsex, String one) {
	
	List<Map<String, Object>> listMap = listMapObject(parsex, REL_TYPE_CHILDREN);
	if (listMap != null && !listMap.isEmpty()) {
	    for (Map<String, Object> mi : listMap) {
		Long parentId = id(parsex);
		mi.put(one, parentId);
		Map<String, Object> node = copyWithKeys(mi, columns(md));
		Node saveByBody = neo4jUService.saveByBody(node, label);
		long childId = saveByBody.getId();
		mi.put(ID,childId);
		Map<String,Object> data = newMap();
		data.put(one, parentId);
		data.put(NAME, "子节点");
		if(parentId.equals(childId)) {
		    continue;
		}
		relationService.addRel(REL_TYPE_CHILDREN,  parentId,childId, data);
		parentTree(md, label, mi, one);
	    }
	}
    }

    public Long addAndRelate(Map<String, Object> md, String label, 
	    Map<String, Object> parsex,
	    List<Map<String, Object>> nexts) {
	String[] mdColumns = columns(md);
	Map<String, Object> parse = copyWithKeys(parsex, mdColumns);
	Map<String, Object> validParam = validParam(parse, mdColumns);
	Node saveByBody = neo4jUService.saveByBody(validParam, label);
	Long startId = saveByBody.getId();
	parsex.put(ID, startId);
	Map<String, Object> keyProps = newMap();
	if (nexts != null && nexts.size() > 0) {
	    Map<String, Object> keys = newMap();
	    for (Map<String, Object> ri : nexts) {
		Map<String, Object> mapObject = mapObject(ri, RELATION_PROP);
		Map<String, Object> endObject = mapObject(ri, RELATION_ENDNODE_PROP);
		String key = string(mapObject, "prop");
		Object ei = validParam.get(key);
		keyProps.put(key, mapObject);
		if (ei == null) {
		    continue;
		}
		String endLabel = label(endObject);
		List<Map<String, Object>> subRels = neo4jService.getOneRelationList(id(endObject), "vo");
		if (ei instanceof List l) {
		    List<Long> ds = new ArrayList(l.size());
		    keys.put(key, ds);
		    for (int i = 0; i < l.size(); i++) {
			Map<String, Object> di = (Map<String, Object>) l.get(i);
			Long mid = addAndRelate(endObject, endLabel, di, subRels);
			ds.add(mid);
		    }
		}
		if (ei instanceof Map m) {
		    Long mid = addAndRelate(endObject, endLabel, m, subRels);
		    keys.put(key, mid);
		}
	    }

	    for (Entry<String, Object> ki : keys.entrySet()) {
		String propKey = ki.getKey();
		Object value2 = ki.getValue();
		if (value2 == null) {
		    continue;
		}
		if (value2 instanceof Long propId) {
		    // neo4jUService.
		    relationService.addRel(propKey, startId, propId);
		}
		if (value2 instanceof List props) {
		    Map<String, Object> mapObject = (Map<String, Object>) keyProps.get(propKey);
		    for (Object oi : props) {
			if (oi instanceof Long propId) {
			    relationService.addRel(propKey, startId, propId, mapObject);
			}
		    }
		}
	    }
	}
	return startId;
    }

    private Map<String, Object> validParam(Map<String, Object> vo, String[] columns) {
	Map<String, Object> param = new HashMap<>();
	for (String ci : columns) {
	    Object value2 = vo.get(ci);
	    if (value2 != null && !"".equals(value2) && !"null".equals(value2)) {
		param.put(ci, value2);
	    }
	}
	return param;
    }

    public static String clearValue(String string) {
	if (string == null || "null".equals(string)) {
	    return null;
	}
	if (string.startsWith("\"") || string.startsWith("'") || string.endsWith("\"") || string.endsWith("'")) {
	    string = string.substring(1, string.length() - 1);
	    return string;
	}
	return string;
    }

    public List<Object> toListMap(String text) {
	String sentences[] = text.split(",");

	Stack<Object> sk = new Stack<>();
	Map<String, Object> pc = initListPC(sk);

	// 数组还存在，则要加入到数组列表中。
	for (String si : sentences) {
	    LoggerTool.info(logger,"=input==" + si);
	    String row = si.trim();
	    handleRow(sk, pc, row);
	}
	return listObject(pc, "rootList");
    }
    public Map<String,Object> toMap2(String text) {
	String sentences[] = text.split(",");

	Stack<Object> sk = new Stack<>();
	Map<String, Object> pc = initObjectPC(sk);

	// 数组还存在，则要加入到数组列表中。
	for (String si : sentences) {
	    LoggerTool.info(logger,"=input==" + si);
	    String row = si.trim();
	    handleRow(sk, pc, row);
	}
	return mapObject(pc, "tempObject");
    }

    // 处理一行数据，逗号分隔的
    public void handleRow(Stack<Object> sk, Map<String, Object> pc, String row) {
	if (row.equals("[")) {
	    // 处理之前的Map：收集
	    pushList(sk, pc);
	    return;
	}
	if (row.equals("{")) {
	    pushMap(sk, pc);
	    return;
	}

	if (row.equals("]")) {
	    fetchParentOfList(sk, pc);
	    return;
	}
	if (row.equals("}")) {
	    LoggerTool.info(logger," before ending Map ,statck size is" + sk.size());
	    sk.pop();
	    LoggerTool.info(logger," after ended Map ,statck size is" + sk.size());
	    getParentOfMap(sk, pc);
	    return;
	}
	// string value
	if (isStringValue(row)) {
	    listStr(pc, "tempList").add(row);
	    return;
	}
	// new open
	while (row.startsWith("[") || row.startsWith("{")) {
	    if (row.startsWith("[")) {
		pushList(sk, pc);
		row = row.substring(1);
		if (row.startsWith("{")) {
			pushMap(sk, pc);
			List<Map<String, Object>> listMapObject = listMapObject(pc, "tempList");
			if (listMapObject != null) {
			    listMapObject.add(mapObject(pc,"tempObject"));
			}
			row = row.substring(1);
		    }
	    }
	    if (row.startsWith("{")) {
		pushMap(sk, pc);
		
		row = row.substring(1);
	    }
	}
	// row data
	if (isStringValue(row)) {
	    listStr(pc, "tempList").add(row);
	    return;
	}

	if (row.indexOf(":") > 0) {
	    // 对象
	    if (row.indexOf(":{") > 0) {
		parseStartKV(sk, pc, row);
		return;
	    }
	    // 列表
	    if (row.indexOf(":[") > 0) {
		parseListProp(sk, pc, row);
		return;
	    }

	    int endList = row.indexOf("]");
	    int endObject = row.indexOf("}");

	    Map<String, Object> mapObject = mapObject(pc, "tempObject");
	    if(mapObject==null) {
		LoggerTool.info(logger,row);
	    }
	    if (endList < 0 && endObject < 0) {
		// 没有结束标志
		cKvPut(mapObject, getKv(row));
		return;
	    }
	    if (endList > 0 && endObject > 0) {
		// 同时有结束标志
		if (endList > endObject) {
		    String content = row.substring(0, endObject - 1);
		    row = row.substring(endObject);
		    cKvPut(mapObject, getKv(content));
		} else {
		    String listContent = row.substring(0, endList - 1);
		    row = row.substring(endList);
		    if (isStringValue(listContent)) {
			listStr(pc, "tempList").add(listContent);
		    }
		}
	    }

	    if (endList < 0 && endObject > 0) {
		// 只有}结束标志
		String content = row.substring(0, endObject - 1);
		row = row.substring(endObject);
		cKvPut(mapObject, getKv(content));
	    }

	    if (endList > 0 && endObject < 0) {
		// 只有]结束标志
		String listContent = row.substring(0, endList - 1);
		row = row.substring(endList);
		if (isStringValue(listContent)) {
		    listStr(pc, "tempList").add(listContent);
		}
	    } 
	}

	// close
	LoggerTool.info(logger," before close  row==="+row);
	
	if (row.equals("]")) {
	    fetchParentOfList(sk, pc);
	    return;
	}
	if (row.equals("}")) {
	    LoggerTool.info(logger," before ending Map ,statck size is" + sk.size());
	    sk.pop();
	    LoggerTool.info(logger," after ended Map ,statck size is" + sk.size());
	    getParentOfMap(sk, pc);
	    return;
	}
	
	while (row.startsWith("}") || row.startsWith("]")) {
	    if (row.startsWith("}")) {
		sk.pop();// 结束map
		if (!sk.isEmpty() && sk.size() > 1) {
		    if (!sk.isEmpty()) {
			getParentOfMap(sk, pc);
		    }
		}
	    }
	    if (row.startsWith("]")) {
		LoggerTool.info(logger,"========end List:" + string(pc, "propKey"));
		if (row.startsWith("\"") && row.endsWith("\"]")) {
		    list(pc, "tempList").add(row.substring(0, row.length() - 2));
		    fetchParentOfList(sk, pc);
		    continue;
		}
		if (row.length() > 1) {
		    fetchParentOfList(sk, pc);
		}
	    }
	    row = row.substring(1);
	}
    }

    public void parseListProp(Stack<Object> sk, Map<String, Object> pc, String row) {
	String[] split = row.split(":\\[");
	pc.put("propKey", split[0].replaceAll("\"", "").replaceAll("'", ""));
	pushList(sk, pc);
	String valuePartTrim = split[1].trim();
	handleRow(sk, pc, valuePartTrim);
    }

    public void parseStartKV(Stack<Object> sk, Map<String, Object> pc, String part) {
	String[] split = part.split(":\\{");

	String newPropMapKey = split[0].replaceAll("\"", "").replaceAll("'", "");
	pc.put("tempObject", pushMap(pc, sk, newPropMapKey));

	String valuePart = split[1];
	handleRow(sk, pc, valuePart);
    }

    /**
     * Map<String, Object> pc  pc.put("tempObject",
     * tempObject);//当前对象 pc.put("tempList", tempList);//当前列表 pc.put("propKey",
     * propKey);//属性Key pc.put("sk", sk);//栈
     * 
     * @param sk
     * @return
     */
    public Map<String, Object> initListPC(Stack<Object> sk) {

//	String propKey = null;
	List<Object> tempList = new ArrayList<>();

	Map<String, Object> pc = new HashMap<>();
	pc.put("rootList", tempList);
//	pc.put("propKey", propKey);
	
	pc.put("sk", sk);

	return pc;
    }

    public Map<String, Object> initObjectPC(Stack<Object> sk) {

	String propKey = null;
	Map<String, Object> tempObject = newMap();

	Map<String, Object> pc = new HashMap<>();
	pc.put("rootObject", tempObject);
	pc.put("propKey", propKey);
	pc.put("sk", sk);

	sk.push(tempObject);
	return pc;
    }

    public List<Object> pushList(Stack<Object> sk, Map<String, Object> pc) {
	Map<String, Object> tempObject = mapObject(pc, "tempObject");
	List<Object> dataList = new ArrayList<>();
	List<Object> tempList =null;
	if(sk.isEmpty()) {
	    tempList = listObject(pc,"rootList");
	    pc.put("tempList",  listObject(pc,"rootList"));
	}else {
	    tempList = (List<Object>) sk.push(dataList);
		pc.put("tempList", tempList);
		String propKey = string(pc, "propKey");

		if (propKey != null && tempObject != null) {
		    LoggerTool.info(logger,"=======new==list====:" + propKey);
		    tempObject.put(propKey, dataList);
		} else {
		    LoggerTool.info(logger,"=======new==list====");
		}
	}
	return tempList;
    }

    public Map<String, Object> pushMap(Stack<Object> sk, Map<String, Object> pc) {
	Map<String, Object> nm = newMap();
	pc.put("tempObject", nm);
	
	List<Map<String, Object>> listMapObject = listMapObject(pc, "tempList");
	if (listMapObject != null) {
	    listMapObject.add(nm);
	}else {
	    if(sk.isEmpty()) {
		List<Map<String, Object>> rootList = listMapObject(pc, "rootList");
		rootList.add(nm);
	    }
	}
	
	sk.push(nm);
	return nm;
    }

    public Map<String, Object> pushMap(Map<String, Object> pc, Stack<Object> sk, String propKey) {
	Map<String, Object> tempObject = mapObject(pc, "tempObject");
	Map<String, Object> nm = newMap();
	
	if(tempObject!=null&&propKey!=null) {
	    tempObject.put(propKey, nm);
	    LoggerTool.info(logger,"\n=======new==map====:" + propKey);
	}
	if (sk.size()==1) { 
		if(mapObject(pc, "rootObject")!=null) {
			if(tempObject!=null&&propKey!=null) {
			mapObject(pc, "rootObject").put(propKey, nm);
			}
		 } 
	    if(listMapObject(pc, "rootList")!=null) {
		listMapObject(pc, "rootList").add(nm);
	    }
	}
	
	pc.put("tempObject", nm);
	sk.push(nm);
	return nm;
    }

    public void getParentOfMap(Stack<Object> sk, Map<String, Object> status) {
	if(!sk.isEmpty()) {
	    Object parent = popParent(sk);
		setCurrent(parent, status);
	}
    }

    public void setCurrent(Object parent, Map<String, Object> status) {
	if (parent instanceof Map m) {
	    status.put("tempObject", m);
	    status.put("tempList", null);
	}
	if (parent instanceof List l) {
	    status.put("tempList", l);
	}
    }

    public void fetchParentOfList(Stack<Object> sk, Map<String, Object> status) {
	
	Object parent = popParentOfList(sk);
	setCurrent(parent, status);
    }

    public boolean isUrlValue(String split) {
	return split.indexOf("https:") > 0 || split.indexOf("http:") > 0;
    }

    public boolean isStringValue(String split) {
	return split.startsWith("\"") && split.endsWith("\"")||split.startsWith("'") && split.endsWith("'");
    }

    public Map<String, Object> toMap(String text) {
	String props[] = text.split(",");
	Map<String, Object> kv = newMap();
	Map<String, Object> tempObject = newMap();
	Object top = null;
	List<Object> tempList = new ArrayList<>();
	// Map<Integer, String> kvMap = new HashMap<>();

	String propKey = null;
	Stack<Object> sk = new Stack<>();

	Map<String, Object> pc = new HashMap<>();
	pc.put("tempObject", tempObject);
	pc.put("tempList", tempList);
	pc.put("propKey", propKey);
	pc.put("sk", sk);

	sk.push(kv);
	// 数组还存在，则要加入到数组列表中。
	int i = 0;
	boolean listOpen = false;
	for (String ki : props) {
	    LoggerTool.info(logger,"=input=============" + ki);
	    if (ki.trim().startsWith("{")) {
		if (i != 0) {
		    LoggerTool.debug(logger,"========new map======propKey:" + propKey);
		    // 处理之前的Map：收集
		    Map<String, Object> nm = newMap();
		    pc.put("tempObject", nm);
		    if (listOpen) {
			tempList.add(nm);
		    }
		    sk.push(nm);
		} else {
		    tempObject = kv;
		    sk.push(kv);
		}
		if (ki.length() > 1 && ki.contains(":")) {
		    cKvPut(mapObject(pc, "tempObject"), getKv(ki));
		    continue;
		}
		ki = ki.substring(1);
	    }
	    i++;
	    if (ki.endsWith("}")) {
		if (!sk.isEmpty()) {
		    if (ki.length() > 1 && ki.contains(":")) {
			String[] pkv = getKv(ki);
			cKvPut(mapObject(pc, "tempObject"), pkv);
		    }
		    top = sk.pop();// 结束map
		    if (!sk.isEmpty()) {
			top = sk.pop();
			LoggerTool.debug(logger,"========end map:");
			if (top instanceof Map m) {
			    tempObject = m;// 获取上级Map
			    tempObject = m;
			}
			if (top instanceof List m) {
			    tempObject = null;
			    sk.push(top);
			}
		    } else {
			LoggerTool.debug(logger,"========top map:");
			tempObject = kv;
		    }
		} else {
		    tempObject = kv;
		    if (ki.length() > 1 && ki.contains(":")) {
			String[] pkv = getKv(ki);
			cKvPut(mapObject(pc, "tempObject"), pkv);
		    }
		    LoggerTool.info(logger,"into top map:");
		}
		continue;
	    }
	    if (ki.endsWith("]")) {
		LoggerTool.debug(logger,"========end List:" + propKey);

		if (ki.length() > 1 && ki.contains(":") && ki.endsWith("}]")) {
		    String[] pkv = getKv(ki);
		    cKvPut(mapObject(pc, "tempObject"), pkv);
		    listOpen = false;
		    int indexOf = ki.indexOf("}");
		    if (indexOf > 0) {
			String ends = ki.substring(indexOf);
			if (ends.length() > 0) {
			    while (ends.length() > 0 && (ends.startsWith("}") || ends.startsWith("]"))) {
				if (ends.startsWith("}")) {
				    if (!sk.isEmpty()) {
					top = sk.pop();
					if (top instanceof Map m) {
					    tempObject = m;
					    tempList.add(m);
					}
					if (top instanceof List l) {
					    tempList = l;
					    LoggerTool.error(logger,"never into :top instanceof List l ");
					}
				    } else {
					tempObject = kv;
				    }
				}
				if (ends.startsWith("]")) {
				    if (!sk.isEmpty()) {
					fetchParentOfList(sk, pc);
					continue;
				    } else {
					tempObject = kv;
				    }
				    propKey = null;
				}
				ends = ends.substring(1);
			    }
			}
		    }
		} else {
		    fetchParentOfList(sk, pc);
		    continue;
		}

		continue;
	    }
	    if (ki.indexOf(":") > 0) {
		if (ki.indexOf("{") < 0 && ki.indexOf("[") < 0) {
		    if (tempObject == null) {
			tempObject = kv;
		    }
		    cKvPut(mapObject(pc, "tempObject"), getKv(ki));
		    continue;
		}
		int xx = ki.indexOf(":{");
		if (xx > 0) {
		    String[] split = ki.trim().split(":\\{");
		    String newPropMapKey = split[0].replaceAll("\"", "");
		    Map<String, Object> nm = newMap();
		    if (!sk.isEmpty()) {
			tempObject.put(newPropMapKey, nm);
		    } else {
			tempObject = kv;
			kv.put(newPropMapKey, nm);
		    }
		    LoggerTool.info(logger,"\n=======new==map====:" + newPropMapKey);

		    tempObject = (Map<String, Object>) sk.push(nm);
		    if (ki.endsWith(":{")) {
			continue;
		    } else {
			String[] propkv = split[1].split(":");
			cKvPut(mapObject(pc, "tempObject"), propkv);
		    }
		}

		int indexOf = ki.indexOf(":[");
		if (indexOf > 0) {
		    String[] split = ki.trim().split(":\\[");
		    propKey = split[0].replaceAll("\"", "");
		    List<Map<String, Object>> nL = new ArrayList<>();
		    tempList = (List<Object>) sk.push(nL);
		    listOpen = true;
		    tempObject.put(propKey, nL);
		    LoggerTool.info(logger,"=======new==list====:" + propKey);

		    String trim = split[1].trim();
		    if (trim.startsWith("{") && trim.indexOf(":") > 0) {
			// 处理之前的Map：收集
			Map<String, Object> nm = newMap();
			tempList.add(nm);
			pc.put("tempObject", nm);
			sk.push(nm);
			String[] pkv = getKv(trim.substring(1));
			cKvPut(mapObject(pc, "tempObject"), pkv);
			LoggerTool.info(logger,ki + "\n=======new==list==node==:" + propKey);
		    }
		}
	    }
	}
	return kv;
    }

    public String[] getKv(String ki) {
	String[] pkv = ki.split(":");
	if(pkv.length>2) {
	    int indexOf = ki.indexOf(":");
	    pkv[1]=ki.substring(indexOf);
	}
	return pkv;
    }

    public void cKvPut(Map<String, Object> tempObject, String[] pkv) {
	LoggerTool.info(logger,"====================>               put:{},{}", clearValue(pkv[0].trim()),
		clearValue(pkv[1].trim()));
	tempObject.put(clearValue(pkv[0].trim()), clearValue(pkv[1].trim()));
    }

    public Object popParentOfList(Stack<Object> sk) {
	
	LoggerTool.info(logger," before popParentOfList statck size=" + sk.size());
	if (!sk.isEmpty()) {
	    Object top = sk.pop();
	    while (top instanceof Map m && !sk.isEmpty() && sk.size() > 1) {
		if (!sk.isEmpty() && sk.size() > 1) {
		    top = sk.pop();
		}
	    }
	    if (top instanceof List l) {
		if (!sk.isEmpty()) {
		    LoggerTool.info(logger," after popParentOfList statck size=" + sk.size());
		    return popParent(sk);
		}else {
		    LoggerTool.info(logger," after popParentOfList statck size=" + sk.size()+"ok its the end");
		    return l;
		}
	    }
	}
	return null;
    }

    public Object popParent(Stack<Object> sk) {
	Object top = sk.pop();
	sk.push(top);
	return top;
    }

    public String trimList(String columns) {
	String[] columnsx = columns.split(",");
	List<String> columnsSet = new ArrayList<>();
	for (String key : columnsx) {
	    columnsSet.add(key.trim());
	}
	return String.join(",", columnsSet);
    }

}

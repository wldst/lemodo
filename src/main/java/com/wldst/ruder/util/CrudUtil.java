package com.wldst.ruder.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;

/**
 * 注入Context环境下的通用工具类 MKUtil
 * 
 * @author liuqiang
 * @date 2019年9月19日 下午2:24:08
 * @version V1.0
 */
@Service
public class CrudUtil extends MapTool {


    private CrudNeo4jService neo4jService;
	@Autowired
    public CrudUtil(@Lazy CrudNeo4jService neo4jService){
        this.neo4jService=neo4jService;
    }

    public String getAsLabel(Integer i) {
	int a1 = 97;
	Character ca = (char) (a1 + i);
	return ca.toString();
    }

    /**
     * 补全主键
     * 
     * @param po
     * @return
     */
    public String completePK(Map<String, Object> po, String crudkey, String crudColumns, String crudHeader) {
	Object key = po.get(crudkey);
	Object columnsStr = po.get(crudColumns);

	Object headerObject = po.get(crudHeader);

	String crudKey = null;
	String header = null;
	if (headerObject != null) {
	    header = String.valueOf(headerObject);
	}

	if (columnsStr != null) {
	    String column = String.valueOf(columnsStr);
	    String[] columns = getColumns(column);
	    crudKey = String.valueOf(key);
	    if (StringUtils.isNotBlank(crudKey)) {
		Set<String> cSet = new HashSet<>();
		for (String ci : columns) {
		    cSet.add(ci);
		}

		if (!cSet.contains(crudKey)) {
		    column = crudKey + "," + column;
		    header = "编码," + header;
		}
	    } else {
		if (columns.length > 0) {
		    crudKey = columns[0];
		}
		if (!"id".equalsIgnoreCase(crudKey)) {
		    crudKey = "id";
		    column = crudKey + "," + column;
		    header = "编码," + header;
		}
	    }
	    po.put(crudColumns, column);
	}
	po.put(crudHeader, header);
	return crudKey;
    }
    
    /**
	 * 无码规则化
	 * @param vo
	 */
	public void wumaRegular(JSONObject vo) {
	    clearColumns(vo, HEADER);
	    
	    String headers = string(vo,HEADER);
	    
	    headers.split(",");	
	    String[] split = headers.split(",");
	    String[] cs = new String[split.length+1];
	    cs[0]=ID;
	    for(int i=1;i<=split.length;i++) {
	        cs[i]=WUMA_COLUMN+i;
	    }
	    if(headers.startsWith("编码,")) {
		    vo.put(HEADER,headers);
	    }else {
		    vo.put(HEADER,"编码,"+headers);
	    }
	    
	    String columnWuma = String.join(",",cs);
	    
	    vo.put(COLUMNS,columnWuma);
	    
	    vo.put(KEY,ID);
	    Long voID = id(vo);
	    if(voID!=null&&("".equals(label(vo).trim())||label(vo)==null)) {
		vo.put(LABEL,WUMA_CODE+voID);
	    }
	    vo.put("isManage",ON);
	}
	
	public void wumaDataRegular(Map<String,Object> vo) {
	    clearColumns(vo, HEADER);
	    String headers = string(vo,HEADER);
	    
	    String[] split = headers.split(",");
	    String[] cs = new String[split.length+1];
	    cs[0]=ID;
	    for(int i=1;i<=split.length;i++) {
	        cs[i]=WUMA_COLUMN+i;
	    }
	    if(headers.startsWith("编码,")) {
		    vo.put(HEADER,headers);
	    }else {
		    vo.put(HEADER,"编码,"+headers);
	    }
	    
	    String columnWuma = String.join(",",cs);
	    
	    vo.put(COLUMNS,columnWuma);
	    
	    vo.put(KEY,ID);
	    Long voID = id(vo);
	    if(voID!=null) {
		String nodeLabelByNodeId = neo4jService.getNodeLabelByNodeId(voID);
		if(nodeLabelByNodeId==null &&("".equals(label(vo).trim())||label(vo)==null)) {
			vo.put(LABEL,WUMA_CODE+voID);
		    }
	    }
	    
	    vo.put("isManage",ON);
	}

    /**
     * pageTotal
     * 
     * @author liuqiang
     * @date 2019年9月18日 下午6:15:50
     * @version V1.0
     * @param query
     */
    public Integer total(String query) {
	if (query.contains("delete")) {
	    return 0;
	}
	if (query.contains("return")) {
	    String[] split = query.split("return");
	    String trim = split[1].trim().split(",")[0];
	    String count = "";
	    String varNode = null;
	    if (trim.indexOf("id(") >= 0) {
		String[] split2 = trim.split("id\\(");
		String startWith = split2[1];
		varNode = startWith.split("\\)")[0];

		count = " return count(" + varNode + ")";
	    } else {
		varNode = trim.split("\\.")[0];
		count = " return count(" + varNode + ")";
	    }

	    List<Map<String, Object>> ret = neo4jService.cypher(split[0] + count);
	    if (ret == null || ret.isEmpty()) {
		return 0;
	    }
	    Integer total = Integer.valueOf(String.valueOf(ret.get(0).get("count(" + varNode + ")")));
	    return total;
	}
	return 0;
    }

	public Integer total(String query,Map<String, Object> params) {
		if (query.contains("delete")) {
			return 0;
		}
		if (query.contains("return")) {
			String[] split = query.split("return");
			String trim = split[1].trim().split(",")[0];
			String count = "";
			String varNode = null;
			if (trim.indexOf("id(") >= 0) {
				String[] split2 = trim.split("id\\(");
				String startWith = split2[1];
				varNode = startWith.split("\\)")[0];

				count = " return count(" + varNode + ")";
			} else {
				varNode = trim.split("\\.")[0];
				count = " return count(" + varNode + ")";
			}

			List<Map<String, Object>> ret = neo4jService.query(split[0] + count,params);
			if (ret == null || ret.isEmpty()) {
				return 0;
			}
			Integer total = Integer.valueOf(String.valueOf(ret.get(0).get("count(" + varNode + ")")));
			return total;
		}
		return 0;
	}

    public Integer totalVo(String query) {
	if (query.contains("delete")) {
	    return 0;
	}
	if (query.contains("return")) {
	    String[] split = query.split("return");
	    String count = " return count(a)";
	    List<Map<String, Object>> ret = neo4jService.cypher(split[0] + count);
	    if (ret == null || ret.isEmpty()) {
		return 0;
	    }
	    Integer total = Integer.valueOf(String.valueOf(ret.get(0).get("count(a)")));
	    return total;
	}
	return 0;
    }

    /**
     * 校验分页对象
     * 
     * @author liuqiang
     * @date 2019年9月20日 下午4:16:44
     * @version V1.0
     * @param vo
     * @return
     */
    public PageObject validatePage(JSONObject vo) {
	if (vo.containsKey("limit")) {
	    return layUIPage(vo);
	}
	PageObject page = null;
	if (vo.containsKey("page")) {
	    JSONObject jsonPage = null;

	    try {
		jsonPage = vo.getJSONObject("page");
		if (jsonPage == null) {
		    page = new PageObject();
		    page.setPageNum(1);
		    page.setPageSize(20);
		} else {
		    page = JSONMapUtil.json2Object(jsonPage, new PageObject());
		}

	    } catch (ClassCastException e) {
		String pageParam = MapTool.string(vo, "page");
		Integer pageNum = Integer.valueOf(pageParam);
		if (pageNum != null && pageNum > 0) {
		    page = new PageObject();
		    page.setPageNum(pageNum);
		    page.setPageSize(20);
		    return page;
		}
	    }

	} else if (vo.containsKey("pageSize") && vo.containsKey("pageNum")) {
	    page = new PageObject();
	    page.setPageNum(vo.getInteger("pageNum"));
	    page.setPageSize(vo.getInteger("pageSize"));
	    vo.remove("pageNum");
	    vo.remove("pageSize");
	}else if (vo.containsKey("current") && vo.containsKey("size")) {
	    page = new PageObject();
	    page.setPageNum(vo.getInteger("current"));
	    page.setPageSize(vo.getInteger("size"));
	    vo.remove("current");
	    vo.remove("size");
	} else {
	    page = new PageObject();
	    page.setPageNum(1);
	    page.setPageSize(20);
	    return page;
	}
	if(page.getPageNum()==0) {
	    page.setPageNum(1);
	}
	vo.remove("page");
	return page;
    }
    
    public PageObject validatePage(Map<String,Object> vo) {
	if (vo.containsKey("limit")) {
	    return layUIPage(vo);
	}
	PageObject page = null;
	if (vo.containsKey("page")) {
		try {
		String pageParam = MapTool.string(vo, "page");
		Integer pageNum = Integer.valueOf(pageParam);
		if (pageNum != null && pageNum > 0) {
			page = new PageObject();
			page.setPageNum(pageNum);
			page.setPageSize(20);
			return page;
		}
		} catch (ClassCastException e) {
			JSONObject jsonPage = null;
			jsonPage = json(vo,"page");
			if (jsonPage == null) {
				page = new PageObject();
				page.setPageNum(1);
				page.setPageSize(20);
			} else {
				page = JSONMapUtil.json2Object(jsonPage, new PageObject());
			}
		}



	} else if (vo.containsKey("pageSize") && vo.containsKey("pageNum")) {
	    page = new PageObject();
	    page.setPageNum(integer(vo,"pageNum"));
	    page.setPageSize(integer(vo,"pageSize"));
	    vo.remove("pageNum");
	    vo.remove("pageSize");
	}else if (vo.containsKey("current") && vo.containsKey("size")) {
	    page = new PageObject();
	    page.setPageNum(integer(vo,"current"));
	    page.setPageSize(integer(vo,"size"));
	    vo.remove("current");
	    vo.remove("size");
	} else {
	    page = new PageObject();
	    page.setPageNum(1);
	    page.setPageSize(20);
	    return page;
	}
	if(page.getPageNum()==0) {
	    page.setPageNum(1);
	}
	vo.remove("page");
	return page;
    }

    public PageObject layUIPage(JSONObject vo) {
	Integer pageSize = vo.getInteger("limit");
	Integer pageNum = vo.getInteger("page");
	JSONObject jsonPage = new JSONObject();
	PageObject page = null;
	if (pageSize == null || pageNum == null) {
	    page = new PageObject();
	    page.setPageNum(1);
	    page.setPageSize(10);
	} else {
	    jsonPage.put("pageNum", pageNum);
	    jsonPage.put("pageSize", pageSize);
	    page = JSONMapUtil.json2Object(jsonPage, new PageObject());
	}
	vo.remove("page");
	vo.remove("limit");
	if(page.getPageNum()==0) {
	    page.setPageNum(1);
	}
	return page;
    }
    
    public PageObject layUIPage(Map<String,Object> vo) {
	Integer pageSize = integer(vo,"limit");
	Integer pageNum = integer(vo,"page");
	JSONObject jsonPage = new JSONObject();
	PageObject page = null;
	if (pageSize == null || pageNum == null) {
	    page = new PageObject();
	    page.setPageNum(1);
	    page.setPageSize(10);
	} else {
	    jsonPage.put("pageNum", pageNum);
	    jsonPage.put("pageSize", pageSize);
	    page = JSONMapUtil.json2Object(jsonPage, new PageObject());
	}
	vo.remove("page");
	vo.remove("limit");
	if(page.getPageNum()==0) {
	    page.setPageNum(1);
	}
	return page;
    }

    public String relationQuery(String label, String relation, JSONObject vo, PageObject page) throws DefineException {
	Map<String, Object> relationDef = neo4jService.getAttMapBy(LABEL, relation, REALTION);
	if (relationDef == null || relationDef.isEmpty()) {
	    throw new DefineException("关系" + relation + "未定义！");
	}

	Object startl = relationDef.get("start");
	Object endl = relationDef.get("End");

	JSONObject startNode = vo.getJSONObject("start");
	startNode = validateObjectAndLabel(startl, startNode);

	JSONObject endObject = vo.getJSONObject("end");
	endObject = validateObjectAndLabel(endl, endObject);

	String query = Neo4jOptCypher.relationsPage(startNode, relation, endObject, page);
	return query;
    }

    public String oneEndRelationQuery(String label, String endLabel, JSONObject vo) throws DefineException {
	return oneEndRelationQuery(label, endLabel, vo, null);
    }

    /**
     * 自定起始Label的关系查询
     * 
     * @param label
     * @param endLabel
     * @param vo
     * @param page
     * @return
     * @throws DefineException
     */
    public String oneEndRelationQuery(String label, String endLabel, JSONObject vo, PageObject page)
	    throws DefineException {
	Map<String, Object> end = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
	if (end == null || end.isEmpty()) {
	    throw new DefineException( endLabel + "未定义！");
	}
	Map<String, Object> start = neo4jService.getAttMapBy(LABEL, label, META_DATA);

	if (start == null || start.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}

	JSONObject startNode = vo.getJSONObject("start");
	startNode = validateObjectAndLabel(label, startNode);

	JSONObject endObject = vo.getJSONObject("end");
	endObject = validateObjectAndLabel(endLabel, endObject);

	String query = Neo4jOptCypher.relationsPage(startNode, endObject, page);
	String string = MapTool.string(vo, "KeyWord");
	if (string != null) {
	    StringBuilder conCypher = new StringBuilder();
	    String[] split = query.split(" return ");
	    conCypher.append(split[0]);
	    conCypher.append(" where (n.code  CONTAINS '" + string + "' or n.name CONTAINS '" + string + "')");
	    conCypher.append(" return " + split[1]);
	    return conCypher.toString();
	}
	return query;
    }

    public String relations(String label, String relation) throws DefineException {
	Map<String, Object> relationDef = neo4jService.getAttMapBy(LABEL, relation, REALTION);
	if (relationDef == null || relationDef.isEmpty()) {
	    throw new DefineException("关系" + label + "未定义！");
	}
	JSONObject endObject = validateObjectAndLabel(relation, new JSONObject());

	StringBuilder relations = Neo4jOptCypher.relations(null, relation, endObject);
	return relations.toString();
    }

    public String relationAdd(String label, String relation, JSONObject vo, PageObject page) throws DefineException {
	Map<String, Object> relationDef = neo4jService.getAttMapBy(LABEL, relation, REALTION);
	if (relationDef == null || relationDef.isEmpty()) {
	    throw new DefineException("关系" + label + "未定义！");
	}

	Object startl = relationDef.get("start");
	Object endl = relationDef.get("End");

	JSONObject startNode = vo.getJSONObject("start");
	startNode = validateObjectAndLabel(startl, startNode);

	JSONObject endObject = vo.getJSONObject("end");
	endObject = validateObjectAndLabel(endl, endObject);

	return Neo4jOptCypher.addRelation(startNode, relation, endObject).toString();
    }

    public String relationCreatCypher(String label, String relation, JSONObject vo, PageObject page)
	    throws DefineException {
	Map<String, Object> relationDef = neo4jService.getAttMapBy(LABEL, relation, REALTION);
	if (relationDef == null || relationDef.isEmpty()) {
	    throw new DefineException("关系" + label + "未定义！");
	}

	Object startl = relationDef.get("start");
	Object endl = relationDef.get("End");

	JSONObject startNode = vo.getJSONObject("start");
	startNode = validateObjectAndLabel(startl, startNode);

	JSONObject relationNode = vo.getJSONObject("relation");
	relationNode = validateObjectAndLabel(relation, relationNode);

	JSONObject endObject = vo.getJSONObject("end");
	endObject = validateObjectAndLabel(endl, endObject);

	return Neo4jOptCypher.addRelation(startNode, relation, endObject).toString();
    }

    public String delRelation(String label, String relation, JSONObject vo) throws DefineException {
	Map<String, Object> relationDef = neo4jService.getAttMapBy(LABEL, relation, REALTION);
	if (relationDef == null || relationDef.isEmpty()) {
	    throw new DefineException("关系" + label + "未定义！");
	}
	Object startl = relationDef.get("start");
	Object endl = relationDef.get("End");

	JSONObject startNode = vo.getJSONObject("start");
	startNode = validateObjectAndLabel(startl, startNode);

	JSONObject endObject = vo.getJSONObject("end");
	endObject = validateObjectAndLabel(endl, endObject);
	return Neo4jOptCypher.delRelations(startNode, relation, endObject).toString();
    }

    /**
     * 校验对象
     * 
     * @author liuqiang
     * @date 2019年9月20日 下午5:09:09
     * @version V1.0
     * @param endl
     * @param nodeObject
     * @return
     */
    private JSONObject validateObjectAndLabel(Object endl, JSONObject nodeObject) {
	if (nodeObject == null) {
	    nodeObject = new JSONObject();
	}
	if (endl != null && !"null".equals(endl)) {
	    nodeObject.put(LABEL, String.valueOf(endl));
	}
	return nodeObject;
    }

    private JSONObject validateObject(JSONObject nodeObject) {
	if (nodeObject == null) {
	    nodeObject = new JSONObject();
	}

	return nodeObject;
    }

    public String[] getMdColumns(String label) throws DefineException {
	if ("po".equals(label.toLowerCase())) {
	    label = META_DATA;
	}
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    String changeLabel = "match (n:Po) remove n:Po set n:" + META_DATA;
	    neo4jService.execute(changeLabel);
	}
	String[] columns = null;
	if (po != null) {
	    String retColumns = String.valueOf(po.get("columns"));
	    return getColumns(retColumns);
	} else {
	    return columns;
	}
    }
    
    public String[] getWuMaColumn(String label) throws DefineException {
	if ("po".equals(label.toLowerCase())) {
	    label = META_DATA;
	}
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, WUMA);
	if (po == null || po.isEmpty()) {
	    String changeLabel = "match (n:WUMA) remove n:WUMA set n:" + WUMA;
	    neo4jService.execute(changeLabel);
	}
	String[] columns = null;
	if (po != null) {
	    String retColumns = String.valueOf(po.get("columns"));
	    return getColumns(retColumns);
	} else {
	    return columns;
	}
    }

    public String[] getShortColumn(String label) throws DefineException {
	return getSomeColumns(label, "ShortData");
    }

    public String[] getSensitiveColumn(String label) throws DefineException {
	return getSomeColumns(label, "SensitiveData");
    }

    public void simplifiList(List<Map<String, Object>> dis){
	for(Map<String, Object> di:dis) {
	    simplification(di);
	}
    }
    public void simplification(Map<String, Object> di){
	if(di==null) return;
	for (String si : NEED_SIMPLE.split(",")) {
	    di.remove(si);
	}
    }

    public String[] getBasicColumn(String label) throws DefineException {
	return getSomeColumns(label, "BasicData");
    }

    private String[] getSomeColumns(String label, String type) {
	String shortColumnsQuery = "match (n:MetaData)-[r]->(m:" + type + ") where n.label='" + label
		+ "' return m.columns AS columns";
	List<Map<String, Object>> shortColumns = neo4jService.cypher(shortColumnsQuery);
	if (shortColumns == null||shortColumns.isEmpty()) {
	    return null;
	}
	String retColumns = MapTool.string(shortColumns.get(0), "columns");
	return getColumns(retColumns);
    }

    public Set<String> getPoColumnSet(String label) throws DefineException {
	Set<String> poColSet = new HashSet<>();
	String[] poColumn = getMdColumns(label);
	if(poColumn!=null&&poColumn.length>0) {
	    for (String ci : poColumn) {
		    poColSet.add(ci);
		}
	}
	
	return poColSet;
    }

    public String[] getTableColumn(String table) throws DefineException {
	Map<String, Object> po = neo4jService.getAttMapBy("tableName", table, DataBaseDomain.LABLE_TABLE);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(table + "未定义！");
	}
	String retColumns = String.valueOf(po.get("columns"));
	String[] columns = getColumns(retColumns);
	return columns;
    }

    /*
     * public String[] getDomainColumn(String label) throws DefineException {
     * Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, DOMAIN);
     * if(po==null||po.isEmpty()){ throw new DefineException(label+"未定义！"); } String
     * retColumns = String.valueOf(po.get("columns")); String[] columns =
     * getColumns(retColumns); return columns; }
     */
    /**
     * 替换列中的逗号
     * 
     * @param vo
     */
    public void clearColumnOrHeader(JSONObject vo) {
	clearColumns(vo, COLUMNS);
	clearColumns(vo, HEADER);
	clearColumns(vo, "shortShow");
	clearColumns(vo, "show");
	clearColumns(vo, "searchColumn");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void clearColumns(Map<String,Object> vo, String key) {
	Object columnsObject = vo.get(key);
	if (columnsObject != null) {
	    if(columnsObject instanceof String retColumns) {
		    columnsClear(vo, key, retColumns);
	    }
	    
	    if(columnsObject instanceof List cs) {		 
		String retColumns=String.join(",", cs);
		columnsClear(vo, key, retColumns);
	    }
	    
	}
    }

    private void columnsClear(Map<String, Object> vo, String key, String retColumns) {
	if(retColumns.contains("[")||retColumns.contains("]")) {
	retColumns = retColumns.replaceAll("\\[","").replaceAll("\\]","");
	}
	if (retColumns.contains(",") || retColumns.contains("，")) {
	retColumns = retColumns.replaceAll("，", ",");// 替换逗号
	retColumns = retColumns.replaceAll("， ", ",");// 替换逗号
	} else {
	retColumns = retColumns.replaceAll("\r\n", ",");// 替换换行
	}
	if(retColumns.trim().indexOf(" ")>0) {
	String[] split = retColumns.split(",");
	String[] split2 = new String[split.length];
	for(int i=0;i<split.length;i++) {
	   split2[i]=split[i].trim();
	}
	vo.put(key, String.join(",", split2));
	}else {
	vo.put(key, retColumns);
	}
    }

    public String[] getViewColumn(String label) throws DefineException {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, VIEW);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	String retColumns = String.valueOf(po.get("columns"));
	String[] columns = getColumns(retColumns);
	return columns;
    }

    public String[] getColumns(String retColumns) {
	if (retColumns.contains("，")) {
	    retColumns = retColumns.replace("，", ",");
	}
	String[] columns = retColumns.split(",");
	return columns;
    }

    public Boolean isColumnsNotEmpty(JSONObject endObject) {
	for (Entry<String, Object> eni : endObject.entrySet()) {
	    Object value = eni.getValue();
	    if (value != null && !String.valueOf(value).isEmpty()) {
		return true;
	    }
	}
	return false;
    }

    /**
     * 
     * 
     * @author liuqiang
     * @date 2019年9月20日 下午2:30:21
     * @version V1.0
     * @param label
     * @return
     */
    public Integer countByLabel(String label) {
	List<Map<String, Object>> ret = neo4jService.cypher("match (n : " + label + ") return count(n) ");
	Integer total = Integer.valueOf(String.valueOf(ret.get(0).get("count(n)")));
	return total;
    }

}

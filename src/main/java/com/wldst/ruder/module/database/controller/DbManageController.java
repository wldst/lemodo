package com.wldst.ruder.module.database.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.util.*;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.module.database.service.DbShowService;
import com.wldst.ruder.module.database.util.DBOptUtil;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.exception.DefineException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/dbmanage")
public class DbManageController extends DataBaseDomain {

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private DbShowService dbShowService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private DbInfoService dbInfoGather;
    
    /**
     * 视图定义
     * 
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/viewDefine", method = { RequestMethod.GET, RequestMethod.POST })
    public String viewDefine(Model model, @PathVariable("po") String label, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);
	dbShowService.viewDefine(model, po);
	return "layui/viewDefine";
    }

    /**
     * 关系节点
     * 
     * @param model
     * @param poLabel
     * @param endLabel
     * @param request
     * @return
     * @throws Exception
     */
	@RequestMapping(value = "/{po}/{endLabel}", method = { RequestMethod.GET, RequestMethod.POST })
    public String poRelations(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("endLabel") String endLabel, HttpServletRequest request) throws Exception {

	Map<String, Object> endPo = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);

	if (!"tree".equals(endLabel)) {
	    if (endPo == null || endPo.isEmpty()) {
		throw new DefineException(endLabel + "未定义！");
	    }
	}

	ModelUtil.setKeyValue(model, endPo);
	/*
	 * String query = crudUtil.relations(label, relation); JSONArray query2 =
	 * neo4jService.relation(query); ModelUtil.setKeyValue(model, po);
	 */

	/*
	 * JSONObject vo = new JSONObject(); vo.put("poName", poLabel);
	 * objectService.query(vo, poLabel);
	 */
	if (endLabel.equals("Field") || endLabel.equals("FieldValidate") || endLabel.equals("SpiderField")) {
	    Map<String, Object> poMap = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	    if (poMap == null) {
		return "poField";
	    }
	    Object columns = poMap.get("columns");
	    Object headers = poMap.get("header");
	    List<Map<String, Object>> columnMapList = new ArrayList<>();

	    if (columns != null && headers != null) {
		String[] codes = String.valueOf(columns).split(",");
		String[] names = String.valueOf(headers).split(",");
		if (codes.length == names.length) {
		    for (int i = 0; i < codes.length; i++) {
			Map<String, Object> columnMap = new HashMap<>();
			columnMap.put("name", names[i]);
			columnMap.put("code", codes[i]);
			columnMapList.add(columnMap);
		    }
		}
	    }
	    if (!columnMapList.isEmpty()) {
		if (endLabel.equals("FieldValidate")) {
		    fieldValidate(model, endPo, columnMapList);
		} else {
		    field(model, endPo, columnMapList);
		}
	    } else {
		table2(model, endPo);
	    }

	    return "poField";
	}

	if ("tree".equals(endLabel)) {
	    endPo = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	    ModelUtil.setKeyValue(model, endPo);
	    tabList(model, endPo);
	    String setting = "{callback: {\n onClick: childList\n }}";
	    model.addAttribute("setting", setting);
	    Map<String, Object> tree = neo4jService.getWholeTree(poLabel);
	    JSONArray zNodesList = new JSONArray();
	    zNodesList.add(tree);
	    model.addAttribute("zNodes", zNodesList);
	    return "instanceTree";
	}

	table2(model, endPo);
	return "poSelect";
    }

    private void field(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	dbShowService.field(model, po, columnMapList);
    }

    private void fieldValidate(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	dbShowService.fieldValidate(model, po, columnMapList);
    }

    @ResponseBody
    @RequestMapping(value = "/{tableName}/update", method = { RequestMethod.GET, RequestMethod.POST })
    public WrappedResult update(Model model, @PathVariable("tableName") String tableName, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> tableMap = neo4jService.getAttMapBy(TABEL_NAME, tableName, LABLE_TABLE);
	if (tableMap == null || tableMap.isEmpty()) {
	    throw new DefineException(tableName + "未定义！");
	}
	Map<String, Object> tableMetaInfo = dbInfoGather.tableMetaInfo(tableName);
	if (tableMetaInfo == null || tableMetaInfo.isEmpty() || splitValue(tableMetaInfo, COLUMNS) == null) {
	    String dbType = dbInfoGather.getDbType();
	    String query = DBOptUtil.createTable(dbType, tableMap);
	    Boolean excute = false;
	    try {
		excute = dbInfoGather.excute(query);
		if (excute) {
		    return ResultWrapper.wrapResult(true, excute, null, QUERY_SUCCESS);
		}
	    } catch (Exception e) {
		e.printStackTrace();
		ResultWrapper.wrapResult(false, null, null, e.getMessage());
	    }
	} else {
	    // dbInfoGather.getDbType();
	    oracleUpdateTable(tableName, tableMap, tableMetaInfo);
	    return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
	}

	return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
    }

    private void oracleUpdateTable(String tableName, Map<String, Object> tableMap, Map<String, Object> tableMetaInfo) {
	String[] columns = columns(tableMetaInfo);
	String[] columnTypes = splitValue(tableMetaInfo, COLUMN_TYPE, "=t=");
	String[] columnSizes = splitValue(tableMetaInfo, COLUMN_SIZE);

	String[] columnsNew = columns(tableMap);
	String[] columnTypesNew = splitColumnValue(tableMap, COLUMN_TYPE, "=t=");
	String[] columnSizesNew = splitValue(tableMap, COLUMN_SIZE);

	Set<String> columnsNewSet = columnSet(tableMap);
	Set<String> columnsSet = columnSet(tableMetaInfo);

	Set<Integer> del = new HashSet<>();
	Set<Integer> update = new HashSet<>();
	Set<Integer> add = new HashSet<>();

	// add
	compare(columnsNew, columnsSet, add);
	// del
	if (columns != null) {
	    compare(columns, columnsNewSet, del);
	}
	// change
	compareUpdate(columnsNew, columnsSet, update, columnSizesNew, columnTypesNew, columnTypes, columnSizes);

	if (!add.isEmpty()) {
	    for (int ui : add) {
		// 1、添加表字段
		// alter table 表名 add 字段名 类型(值)
		// 示例：alter table user add name varchar(40);
		String columnTypei = columnTypesNew[ui];
		if (!columnTypei.contains("(")) {
		    columnTypei = columnTypei + "(" + columnSizesNew[ui] + ")";
		}
		String addsql = " alter table " + tableName + " add " + columnsNew[ui] + " " + columnTypei;
		excuteSQl(addsql);
	    }
	}
	if (!del.isEmpty()) {
	    for (int ui : del) {
		// 2、删除表字段
		// alter table 表名 drop 字段名
		String delSql = " alter table " + tableName + " drop column " + columns[ui];
		excuteSQl(delSql);
	    }
	}

	if (!update.isEmpty()) {
	    for (int ui : update) {
		// 3、字段名更名
		// alter table 表名 rename 老字段名 to 新字段名
		// 示例：alter table user rename oldname to newname;
		// if(columnsNew) {
		// String delSql=" alter table "+tableName+" drop "+columns[ui];
		// excuteSQl(delSql);
		// }
		// 4、更改字段类型
		// alter table 表名 alter 字段 类型;
		// 示例：alter table user alter name varchar(50);
		String columnTypei = columnTypesNew[ui];
		if (!columnTypei.contains("(")) {
		    columnTypei = columnTypei + "(" + columnSizesNew[ui] + ")";
		}
		String updateSql = " alter table " + tableName + " alter " + columnsNew[ui] + " " + columnTypesNew[ui];
		excuteSQl(updateSql);
	    }
	}
    }

    private WrappedResult excuteSQl(String query) {
	Boolean excute = false;
	try {
	    excute = dbInfoGather.excute(query);
	    if (excute) {
		return ResultWrapper.wrapResult(excute, null, null, "成功");
	    }
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return ResultWrapper.wrapResult(false, null, null, e.getMessage());
	}
	return ResultWrapper.wrapResult(false, null, null, "失败");
    }

    private void compare(String[] columnsNew, Set<String> columnsSet, Set<Integer> add) {
	int k = 0;
	for (String ci : columnsNew) {
	    if (!columnsSet.contains(ci) && !columnsSet.contains(ci.toUpperCase())) {
		add.add(k);
	    }
	    k++;
	}
    }

    /*columnSizesNew,columnTypesNew,columnTypes,columnSizes
    */
    private void compareUpdate(String[] columnsNew, Set<String> columnsSet, Set<Integer> update,
	    String[] columnSizesNew, String[] columnTypesNew, String[] columnTypes, String[] columnSizes) {
	int k = 0;
	for (String ci : columnsNew) {
	    if (columnsSet.contains(ci)) {
		String type = columnTypes[k];
		String newType = columnTypesNew[k];

		if (columnSizesNew == null) {
		    if (type == null && newType != null || !type.equals(newType)) {
			update.add(k);
		    }
		} else {
		    String newSize = columnSizesNew[k];
		    String size = columnSizes[k];
		    if (type == null && newType != null || !type.equals(newType) || !newSize.equals(size)) {
			update.add(k);
		    }
		}
	    }
	    k++;
	}
    }

    @RequestMapping(value = "/{tableName}", method = { RequestMethod.GET, RequestMethod.POST })
    public String instance(Model model, @PathVariable("tableName") String tableName, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> tableMap = neo4jService.getAttMapBy(TABEL_NAME, tableName, LABLE_TABLE);
	if (tableMap == null || tableMap.isEmpty()) {
	    throw new DefineException(tableName + "未定义！");
	}
	if (tableMap.get(NAME) == null) {
	    tableMap.put("name", tableName);
	}
	ModelUtil.setKeyValue(model, tableMap);

	dbShowService.table2(model, tableMap, true);

	dbShowService.tableToolBtn(model, tableName, tableMap);

	return "table/tableInstance";
    }
    
    @RequestMapping(value = "/sql", method = { RequestMethod.GET, RequestMethod.POST })
    public String sql(Model model, @RequestBody String sqlMap, HttpServletRequest request)
	    throws Exception {
	
	List<Map<String, Object>> query =null;
	String sql = string((Map)JSONUtils.parse(sqlMap), "SQL");
	sql = sql.toLowerCase();
	Map<String, Object> sqlMetaInfo = dbInfoGather.sqlMetaInfo(sql);
	
	if(sql.trim().startsWith("select")) {
	    query = dbInfoGather.query(sql);
	}
	
	if(sql.startsWith("update")||sql.startsWith("delete")||sql.startsWith("alter ")) {
	    dbInfoGather.excute(sql);
	}
//	dbShowService
	
//	dbShowService.tableToolBtn(model, tableName, tableMap);

	return "table/sqlResult";
    }
    
    @RequestMapping(value = "/sqlExecute", method = { RequestMethod.GET, RequestMethod.POST })
    public String sqlExecute(Model model, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> dataMap = new HashMap<>();
	dataMap.put(NAME, "sqlExecute");
	dataMap.put("SQL", " select * from test22 ");
	Node saveByBody = neo4jService.saveByBody(dataMap, label_dataSet);
	model.addAttribute("sqlId", saveByBody.getId());
	model.addAttribute("label", label_dataSet);
	
	dbShowService.defaultLayui(model, dataMap);
	ModelUtil.setKeyValue(model, dataMap);
	return "table/sqlExecute";
    }
    
    @RequestMapping(value = "/cqlExecute", method = { RequestMethod.GET, RequestMethod.POST })
    public String cqlExecute(Model model, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> dataMap = new HashMap<>();
	dataMap.put(NAME, "cqlExecute");
	dataMap.put(CYPHER, " match(n) return n.id,n.name ");
	Node saveByBody = neo4jService.saveByBody(dataMap, CYPHER_ACTION);
	model.addAttribute("cypherId", saveByBody.getId());
	model.addAttribute("label", CYPHER_ACTION);
	
	dbShowService.defaultLayui(model, dataMap);
	ModelUtil.setKeyValue(model, dataMap);
	return "table/cqlExecute";
    }
    @RequestMapping(value = "/dataImport", method = { RequestMethod.GET, RequestMethod.POST })
    public String dataImport(Model model, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> dataMap = new HashMap<>();
	dataMap.put(NAME, "dataImport");
	model.addAttribute("label", DATA_IMPORT);
	model.addAttribute("data", "{}");
	
	dbShowService.defaultLayui(model, dataMap);
	ModelUtil.setKeyValue(model, dataMap);
	return "table/dataImport";
    }
    
    @RequestMapping(value = "/query", method = { RequestMethod.GET, RequestMethod.POST })
    public String query(Model model, @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
	String sql = string(vo, "sql");
	String param = string(vo, "param");
//	dbShowService.table2(model, tableMap, true);
	List<Map<String, Object>> retData =null;
	if(sql.startsWith("select")) {
	    if(vo.size()==1) {
		Map<String, Object> sqlMetaInfo = dbInfoGather.sqlMetaInfo(sql);
		retData = dbInfoGather.query(param);		
	    }else {
		retData = dbInfoGather.prepareQuery(param, vo, null, null);
	    }
	}
	
	if(sql.startsWith("update")||sql.startsWith("delete")||sql.startsWith("alter ")) {
	    if(vo.size()==1) {
		dbInfoGather.excute(param);
	    }else {
		dbInfoGather.prepareExcute(param, vo, null, null);
	    }
	}
	
	
//	dbShowService.tableToolBtn(model, tableName, tableMap);

	return "table/sqlResult";
    }
    @ResponseBody
    @RequestMapping(value = "/query/{sqlId}/data", method = { RequestMethod.GET, RequestMethod.POST })
    public WrappedResult  queryData(Model model, @PathVariable("sqlId") String sqlId,@RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
	PageObject page = null;
	if(vo.isEmpty()) {
	    page = new PageObject();
	    page.setPageNum(1);
	    page.setPageSize(30);	    
	}else {
	    page = crudUtil.validatePage(vo);
	}
	
	
	Map<String, Object> dataSetDefine = neo4jService.getAttMapBy(LABEL, label_dataSet, META_DATA);
	if (dataSetDefine == null || dataSetDefine.isEmpty()) {
	    throw new DefineException(label_dataSet + "未定义！");
	}
	Map<String, Object> poMap = neo4jService.getPropMapByNodeId(Long.valueOf(sqlId));
	if (poMap == null || poMap.isEmpty()) {
	    throw new DefineException(sqlId + "不存在！");
	}
	String sql = string(poMap, "SQL");
	String pageSql=DBOptUtil.sqlPage(dbInfoGather.getDbType(), page, sql);
	String param = string(poMap, "params");
//	dbShowService.table2(model, tableMap, true);
	List<Map<String, Object>> retData =null;
//	JSONObject paramJSON = JSON.parseObject(param);
	if(sql.startsWith("select")) {	    
	    if(!poMap.containsKey("params")) {
		retData = dbInfoGather.query(pageSql);		
	    }else {
		retData = dbInfoGather.prepareQuery(pageSql, param);
	    }
	    if(retData!=null) {
		page.setTotal(dbInfoGather.count(DBOptUtil.total(sql)));
	    }
	    return ResultWrapper.wrapResult(true, retData, page, QUERY_SUCCESS);
	}
	
	if(sql.startsWith("update")||sql.startsWith("delete")||sql.startsWith("alter ")) {
	    if(!poMap.containsKey("params")) {
		dbInfoGather.excute(sql);
	    }else {
		dbInfoGather.prepareExcute(sql, param, null, null);
	    }
	}
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }
    
    @RequestMapping(value = "/queryBySqlId/{sqlId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String queryBySqlId(Model model, @PathVariable("sqlId") String sqlId, HttpServletRequest request)
	    throws Exception {
	String params = "params";
	Map<String, Object> dataSetDefine = neo4jService.getAttMapBy(LABEL, label_dataSet, META_DATA);
	if (dataSetDefine == null || dataSetDefine.isEmpty()) {
	    throw new DefineException(label_dataSet + "未定义！");
	}
	Map<String, Object> poMap = neo4jService.getPropMapByNodeId(Long.valueOf(sqlId));
	if (poMap == null || poMap.isEmpty()) {
	    throw new DefineException(sqlId + "不存在！");
	}
	String sql = string(poMap, "SQL").trim();
	
	String param = string(poMap, params);
//	dbShowService.table2(model, tableMap, true);
	Map<String, Object> sqlMetaInfo=null;
	
	if(sql.startsWith("select")) {
	    if(param==null||param.isEmpty()) {
		sqlMetaInfo = dbInfoGather.sqlMetaInfo(sql);
	    }else {
		sqlMetaInfo = dbInfoGather.sqlMetaInfo(sql,param.split(","));
	    }
	}
	
	if(sql.startsWith("update")||sql.startsWith("delete")||sql.startsWith("alter ")) {
	    if(!poMap.containsKey(params)) {
		if(param==null||param.isBlank()) {
		    dbInfoGather.excute(sql);
		}else {
			dbInfoGather.prepareExcute(sql, param.split(","));
		}
	    }else {
		String param1 = string(poMap, params);
		JSONObject paramJSON = JSON.parseObject(param1);
		List<String> keys = splitValue2List(poMap, COLUMNS);
		dbInfoGather.prepareExcute(sql, paramJSON, keys, null);
	    }
	}
	String columns = string(poMap, COLUMNS);
	String headers = string(poMap, HEADER);
	String[] split = sql.split("from");
	 
	if(split.length<2) {
	    split = sql.split("FROM");
	}
	
	if(split[0].contains(",")) {
	    List<String> cols= new ArrayList<>();
	    List<String> colsH= new ArrayList<>();
	    columns=split[0].replace("select", "").replace("SELECT", "").trim();
	    if(columns.contains(" as ")) {
		String[] columnsx = columns.split(",");
		for(String si: columnsx) {
		    if(si.contains(" as ")) {
			String[] split2 = si.split("as");
			cols.add(split2[1].trim());
			colsH.add(split2[0].trim());
		    }else {
			cols.add(si);
			colsH.add(si);
		    }
		}
		 poMap.put(COLUMNS, String.join(",", cols));
		 poMap.put(HEADER, String.join(",", cols));
	    }else {
		poMap.put(HEADER, columns);
		poMap.put(COLUMNS, columns);
	    }
	    
	   
	    if(sqlMetaInfo!=null&&!sqlMetaInfo.isEmpty()) {
		    StringBuilder sb=new StringBuilder();
		    StringBuilder sb2=new StringBuilder();
		    for(String ki:cols) {
			String object = string(sqlMetaInfo,ki.toUpperCase());
			if(!sb2.isEmpty()) {
			    sb2.append(",");
			}else
			if("VARCHAR".equals(object)) {
			    sb2.append("String");
			}else
			if("INT".equals(object)) {
			    sb2.append("Integer");
			}else
			if("DATETIME".equals(object)) {
			    sb2.append("Date");
			}else
			if("NUMBER".equals(object)) {
			    sb2.append("BigDecimal");
			}else {
			    sb2.append("String");
			}
			
		    }
		    poMap.put(COLUMN_TYPE, sb2.toString());
		    
	    }
	}else {
	    if(sqlMetaInfo!=null&&!sqlMetaInfo.isEmpty()) {
		    StringBuilder sb=new StringBuilder();
		    StringBuilder sb2=new StringBuilder();
		    for(Entry<String, Object> ei: sqlMetaInfo.entrySet()) {
			if(!sb.isEmpty()) {
			    sb.append(",");
			    sb2.append(",");
			}
			sb.append(ei.getKey());
			sb2.append("String");
		    }
		    
		    
		    poMap.put(COLUMNS, sb.toString());
		    poMap.put(COLUMN_TYPE, sb2.toString());
		    poMap.put(HEADER, sb.toString());
		}
	}
	
	
	model.addAttribute("domainId", string(poMap, ID));
	dbShowService.table2(model, poMap, false);
	
//	dbShowService.tableToolBtn(model, tableName, tableMap);

	return "table/sqlResult";
    }

    @ResponseBody
    @RequestMapping(value = "/{id}/toTable", method = { RequestMethod.GET, RequestMethod.POST })
    public WrappedResult toTable(Model model, @PathVariable("id") String poId, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> poMap = neo4jService.getPropMapByNodeId(Long.valueOf(poId));
	if (poMap == null || poMap.isEmpty()) {
	    throw new DefineException(poId + "对象不存在！");
	}
	String tableName = name(poMap);

	Map<String, Object> tableObject = neo4jService.getAttMapBy(TABEL_NAME, tableName, LABLE_TABLE);
	if (tableObject != null || !tableObject.isEmpty()) {
	    throw new DefineException(tableName + "已存在！");
	}
	Map<String, Object> table = new HashMap<>();

	List<Map<String, Object>> listAllByLabel = neo4jService.listAllByLabel(COLUMN_TYPE_DICT);

	neo4jService.saveByBody(table, LABLE_TABLE);

	String query = DBOptUtil.createTable(dbInfoGather.getDbType(), table);
	Boolean excute = false;
	try {
	    excute = dbInfoGather.excute(query);
	    if (excute) {
		return ResultWrapper.wrapResult(true, excute, null, QUERY_SUCCESS);
	    }
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    ResultWrapper.wrapResult(false, null, null, e.getMessage());
	}

	return ResultWrapper.wrapResult(true, "", null, QUERY_SUCCESS);
    }
    
    
    

    /**
     * 编辑表单
     * 
     * @param model
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{tableName}/form", method = { RequestMethod.GET, RequestMethod.POST })
    public String editForm(Model model, @PathVariable("tableName") String tableName, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> tableMap = neo4jService.getAttMapBy(TABEL_NAME, tableName, LABLE_TABLE);
	if (tableMap == null || tableMap.isEmpty()) {
	    throw new DefineException(tableName + "未定义！");
	}
	ModelUtil.setKeyValue(model, tableMap);
	dbShowService.editForm(model, tableMap, true);
	dbShowService.columnInfo(model, tableMap, false);
	String idString = String.valueOf(tableMap.get(ID));
	// embedListParse(model, idString);

	Map<String, Object> i3Data = neo4jService.getThirdInterface(idString);
	if (i3Data != null) {
	    ModelUtil.setKeyValue(model, i3Data);
	    return "table/editForm3";
	}
	return "table/editForm";
    }

    /**
     * @Describe:列表数据展现
     * @param model
     * @param po
     * @throws DefineException
     */
    private void table2(Model model, Map<String, Object> po) {
	dbShowService.table2(model, po, false);
    }

    private void tabList(Model model, Map<String, Object> po) {
	dbShowService.table2(model, po, true);
    }
}

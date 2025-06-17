package com.wldst.ruder.module.database.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.domain.LayUIDomain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jDriver;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.crud.service.ViewService;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.StringGet;

import static com.wldst.ruder.domain.DataBaseDomain.TABEL_NAME;

@Service
public class DbShowService extends LayUIDomain{
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private CrudNeo4jDriver driver;
    @Autowired
    private ViewService vService;
    @Autowired
    private DbInfoService dbInfoGather;

    /**
     * table：展现列表和表单，关系tab
     * 
     * @param model
     * @param po
     * @param useTab
     */
    public void table2(Model model, Map<String, Object> po, boolean useTab) {
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    // List<String> props = new ArrayList<>();
	    List<Map<String, String>> customType = new ArrayList<>();
	    Map<String, Boolean> layUseInfo = formFieldTemplate(model, po, columnArray, headers, customType);
	    useLayModule(model, useTab, layUseInfo);
	    tableColumnTemplate(model, columnArray, headers, customType);
	} else {
	    Map<String, Boolean> layUseInfo = new HashMap<>();
	    useLayModule(model, useTab, layUseInfo);
	}
    }

    public void defaultLayui(Model model, Map<String, Object> po) {
	Map<String, Boolean> layUseInfo = new HashMap<>();
	useLayModule(model, true, layUseInfo);
    }

    public void columnInfo(Model model, Map<String, Object> po, boolean useTab) {
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    List<Map<String, String>> customType = new ArrayList<>();
	    tableColumnTemplate(model, columnArray, headers, customType);
	}
    }

    public void viewDefine(Model model, Map<String, Object> po) {
	if (po.containsKey(HEADER)) {
	    String label = String.valueOf(po.get(LABEL));
	    StringBuilder sb = new StringBuilder();
	    String endPoName = String.valueOf(po.get(NAME));

	    sb.append("<div class=\"layui-form-item\" >");
	    sb.append("<label class=\"layui-form-label\">" + endPoName + "</label>");
	    sb.append("<div class=\"layui-input-block\">");
	    sb.append(vService.getFieldCheckList(label, po));
	    sb.append(" </div> </div>");

	    sb.append(formTabList(label));
	    appendSubmitResetBtn(sb);
	    model.addAttribute("formContent", sb.toString());
	    fieldFormTabJs(model);
	}
    }

    /**
     * 
     * @param model
     * @param po
     * @param useTab
     */
    public void editForm(Model model, Map<String, Object> po, boolean useTab) {
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    // List<String> props = new ArrayList<>();
	    Map<String, Boolean> layUseInfo = formField(model, po, columnArray, headers);
	    useLayModule(model, useTab, layUseInfo);
	}
    }

    /**
     * 实体表单列表
     * 
     * @param label
     * @return
     */
    public String formTabList(String label) {
	List<Map<String, Object>> relationDefineList = neo4jService.queryRelationDefine("startLabel", label);
	StringBuilder sBuilder = new StringBuilder();
	for (Map<String, Object> ri : relationDefineList) {
	    Object endLabel = ri.get(END_LABEL);
	    Object relLabelObj = ri.get(RELATION_LABEL);
	    if (endLabel != null) {
		String eLabel = String.valueOf(endLabel);
		String relLabel = String.valueOf(relLabelObj);
		Map<String, Object> endiPo = neo4jService.getAttMapBy(LABEL, eLabel, META_DATA);
		if (endiPo != null && !endiPo.isEmpty()) {
		    String relationName = String.valueOf(ri.get(NAME));
		    String name = String.valueOf(endiPo.get(NAME));
		    if (!relationName.equals(name)) {
			relationName = relationName + "(" + String.valueOf(endiPo.get(NAME)) + ")";
		    }

		    sBuilder.append("<div class=\"layui-form-item\" >");
		    sBuilder.append("<label class=\"layui-form-label\">" + relationName + "</label>");
		    sBuilder.append("<div class=\"layui-input-block\">");
		    sBuilder.append(vService.getFieldCheckList(relLabel + "_" + eLabel, endiPo));
		    sBuilder.append(" </div> </div>");
		}
	    }
	}
	return sBuilder.toString();
    }

    public void module(Model model) {
	useLayModule(model, true, new HashMap<>());
    }

    /**
     * 
     * @param model
     * @param po
     * @param columnMapList 当前对象的字段列表
     */
    public void field(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    // List<String> props = new ArrayList<>();

	    Map<String, Boolean> layUseInfo = formField2(model, po, columnArray, headers, columnMapList);

	    useLayModule(model, true, layUseInfo);

	    tableInfo(model, columnArray, headers);
	}
    }

    /**
     * 字段校验
     * 
     * @param model
     * @param po
     * @param columnMapList
     */
    public void fieldValidate(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    // List<String> props = new ArrayList<>();

	    Map<String, Boolean> layUseInfo = formField2(model, po, columnArray, headers, columnMapList);

	    useLayModule(model, false, layUseInfo);

	    tableInfo(model, columnArray, headers);
	}
    }

    /**
     * form 根据定义信息组装表单字段，自定义和常规字段
     * 
     * @param model
     * @param po
     * @param columnArray
     * @param headers
     * @return
     */
    private Map<String, Boolean> formField(Model model, Map<String, Object> po, String[] columnArray,
	    String[] headers) {
	Map<String, Boolean> boolMap = new HashMap<>();
	JSONObject vo = new JSONObject();
	String labelPo = label(po);
	vo.put("poId", labelPo);
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
	List<Map<String, Object>> validateList = objectService.getBy(vo, "FieldValidate");
	Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
	for (Map<String, Object> fi : fieldInfoList) {
	    Object object = fi.get(FIELD);
	    object = object == null ? fi.get(ID) : object;
	    customFieldMap.put(String.valueOf(object), fi);
	}

	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item\">");
	List<Map<String, String>> customType = new ArrayList<>();
	boolean hasDateField = false;
	if (validateList != null && !validateList.isEmpty()) {
	    Map<String, Map<String, Object>> fieldValidateMap = new HashMap<>(validateList.size());
	    for (Map<String, Object> fi : validateList) {
		Object object = fi.get(FIELD);
		object = object == null ? fi.get(ID) : object;
		validatorDetail(fi);

		fieldValidateMap.put(String.valueOf(object), fi);
	    }
	    hasDateField = fieldHandleWithStatus(columnArray, headers, customFieldMap, fieldValidateMap, sb, customType,
		    labelPo);
	} else {
	    hasDateField = fieldWithStatusHandle(columnArray, headers, customFieldMap, sb, customType, labelPo);
	}

	boolMap.put(HAS_DATE_FIELD, hasDateField);
	Map<String, Boolean> layUseInfo = customFormField(model, sb, customType, null);
	boolMap.putAll(layUseInfo);
	sb.append("</div>");
	appendSubmitResetBtn(sb);
	// 总的表单内容
	model.addAttribute("formContent", sb.toString());
	return boolMap;
    }

    private Map<String, Boolean> formFieldTemplate(Model model, Map<String, Object> po,
	    String[] columnArray,
	    String[] headers, List<Map<String, String>> customType) {
	Map<String, Boolean> boolMap = new HashMap<>();
	JSONObject vo = new JSONObject();
	String tableName = string(po, TABEL_NAME);
	vo.put("poId", tableName);
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = new ArrayList<>();
	List<Map<String, Object>> validateList = new ArrayList<>();
	Map<String, Map<String, Object>> customFieldMap = new HashMap<>(columnArray.length);

	String[] columnType = StringGet.split(string(po, COLUMN_TYPE));
	String[] nullAble = splitValue(po, COLUMN_NULL_ABLE);
	String[] columnSize = splitValue(po, COLUMN_SIZE);
//	Map<String, String> columnTypeDictMapx = new HashMap<>();
//	if (columnTypeDictMapx.isEmpty()) {
//	    String dbType = dbInfoGather.getDbType();
//	    if (dbType == null) {
//		dbType = "oracle";
//	    }
//	    String[] columns = { dbType, "java" };
//
//	    List<Map<String, Object>> columnsBy = objectService.getColumnsBy(null, "columnTypeDict", columns);
//	    for (Map<String, Object> typeMap : columnsBy) {
//		String string = string(typeMap, dbType);
//		String string2 = string(typeMap, "java");
//		if(string==null||string2==null) {
//		    continue;
//		}
//		columnTypeDictMapx.put(string, string2);
//	    }
//	}
	int k = 0;
	if (columnType != null && columnType.length > 0) {
	    for (String tyi : columnType) {
		if (tyi == null || !tyi.contains("(") && tyi.endsWith(")")) {
		    continue;
		}

		String columnLength = StringGet.getColumnSize(tyi);
		if ("".equals(columnLength)) {
		    Map<String, Object> ciMap = new HashMap<>();
		    if (tyi.contains("time") || tyi.contains("date")) {
			ciMap.put(FIELD, columnArray[k]);
			ciMap.put("type", tyi);
			ciMap.put(SHOW_TYPE, "date");
		    }
		    customFieldMap.put(columnArray[k], ciMap);
		} else if (Integer.valueOf(columnLength) > 50) {
		    Map<String, Object> ciMap = new HashMap<>();
		    ciMap.put(FIELD, columnArray[k]);
		    ciMap.put("type", tyi);
		    if (Integer.valueOf(columnLength) > 128) {
			 ciMap.put(SHOW_TYPE, "line");
		    }
		    if (Integer.valueOf(columnLength) > 300) {
			ciMap.put(SHOW_TYPE, "textArea");
		    }
		    if (Integer.valueOf(columnLength) > 1000) {
			ciMap.put(SHOW_TYPE, "textEditor");
		    }
		    customFieldMap.put(columnArray[k], ciMap);
		}
		k++;
	    }
	    // 长度校验
	    lengthValidator(columnArray, validateList, columnType, columnSize);

	}

	// 空校验
	nullAbleValdator(columnArray, validateList, nullAble);

	// 获取模板信息，table列中的个性信息
	templateOfColumn(fieldInfoList, customFieldMap);

	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item\">");

	boolean hasDateField = false;
	if (validateList != null && !validateList.isEmpty()) {
	    Map<String, Map<String, Object>> fieldValidateMap = new HashMap<>(validateList.size());
	    for (Map<String, Object> fi : validateList) {
		Object object = fi.get(FIELD);
		object = object == null ? fi.get(ID) : object;
		validatorDetail(fi);
		mappingFiledValdator(fieldValidateMap, fi, object);
	    }
	    hasDateField = fieldHandleWithStatus(columnArray, headers, customFieldMap, fieldValidateMap, sb, customType,
		    tableName);
	} else {
	    hasDateField = fieldWithStatusHandle(columnArray, headers, customFieldMap, sb, customType, tableName);
	}

	boolMap.put(HAS_DATE_FIELD, hasDateField);
	Map<String, Boolean> layUseInfo =searchAllFieldHandle(model, sb, columnArray, headers,customFieldMap);
//	 customFieldHandle(model, sb, customType, null);
	boolMap.putAll(layUseInfo);
	sb.append("</div>");
	appendSubmitResetBtn(sb);
	// 总的表单内容
	model.addAttribute("formContent", sb.toString());
	return boolMap;
    }

    private void lengthValidator(String[] columnArray, List<Map<String, Object>> validateList, String[] columnType,
	    String[] columnSize) {
	if (columnSize == null) {
	    int i = 0;
	    for (String tyi : columnType) {
		String columnLength = StringGet.getColumnSize(tyi);
		if (columnLength != null && !columnLength.trim().isEmpty()) {
		    Map<String, Object> ddMap = new HashMap<>();
		    ddMap.put(FIELD, columnArray[i]);
		    ddMap.put(COLUMN_VALIDATOR, "maxLength=\"" + columnLength + "\"");
		    validateList.add(ddMap);
		}
		i++;
	    }
	}
    }

    private void nullAbleValdator(String[] columnArray, List<Map<String, Object>> validateList, String[] nullAble) {
	if (nullAble == null) {
	    return;
	}
	for (int k = 0; k < nullAble.length; k++) {
	    if ("NO".equalsIgnoreCase(nullAble[k])) {
		Map<String, Object> ddMap = new HashMap<>();
		ddMap.put(FIELD, columnArray[k]);
		ddMap.put(COLUMN_VALIDATOR, "notNull");
		validateList.add(ddMap);
	    }
	}
    }

    private void mappingFiledValdator(Map<String, Map<String, Object>> fieldValidateMap, Map<String, Object> fi,
	    Object object) {
	String fieldCode = String.valueOf(object);
	if (fieldValidateMap.containsKey(fieldCode)) {
	    Map<String, Object> fvMap = fieldValidateMap.get(fieldCode);
	    // 融合多条 校验规则
	    fi.put(COLUMN_VALIDATOR,
		    string(fi, COLUMN_VALIDATOR) + "  " + string(fvMap, COLUMN_VALIDATOR));
	    fieldValidateMap.put(fieldCode, fi);
	} else {
	    fieldValidateMap.put(fieldCode, fi);
	}
    }

    private void validatorDetail(Map<String, Object> fi) {
	Object validator = fi.get(COLUMN_VALIDATOR);
	JSONObject query = new JSONObject();
	query.put(CODE, validator);
	String[] columString = { COLUMN_VALIDATOR };
	List<Map<String, Object>> by = objectService.getColumnsBy(query, "InputValidate", columString);
	if (by != null && !by.isEmpty()) {
	    fi.putAll(by.get(0));
	}
    }

    private void templateOfColumn(List<Map<String, Object>> fieldInfoList,
	    Map<String, Map<String, Object>> customFieldMap) {
	for (Map<String, Object> fi : fieldInfoList) {
	    Object object = fi.get(FIELD);
	    object = object == null ? fi.get(ID) : object;
	    String query = "match (n:Field)-[r:template]->(m:LayuiTemplate) where id(n)=" + fi.get(ID)
		    + "  return m.templateId,m.content";
	    List<Map<String, Object>> query2 = neo4jService.cypher(query);
	    for (Map<String, Object> templat : query2) {
		fi.put(TABLE_TEMPLATE_ID, templat.get(TABLE_TEMPLATE_ID));
		fi.put(TABLE_TEMPLATE_CONTENT, templat.get("content"));
	    }
	    customFieldMap.put(String.valueOf(object), fi);
	}
    }

    /**
     * 字段表单
     * 
     * @param model
     * @param po
     * @param columnArray
     * @param headers
     * @return
     */
    private Map<String, Boolean> formField2(Model model, Map<String, Object> po, String[] columnArray, String[] headers,
	    List<Map<String, Object>> selectOptions) {
	JSONObject vo = new JSONObject();
	String labelPo = label(po);
	vo.put("poId", labelPo);
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
	Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
	String key = FIELD;
	for (Map<String, Object> fi : fieldInfoList) {
	    Object object = fi.get(key);// 获取字段
	    object = object == null ? fi.get(ID) : object;
	    if (object != null) {
		customFieldMap.put(String.valueOf(object), fi);
	    }
	}
	fieldSelect(columnArray, headers, customFieldMap, key);
	fieldSelect(columnArray, headers, customFieldMap, "valueField");

	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item\">");
	List<Map<String, String>> customType = new ArrayList<>();
	boolean hasDateField = fieldFormHandle(columnArray, headers, customFieldMap, sb, customType);
	addPo(sb);
	Map<String, Boolean> customFieldHandle = customFieldHandle(model, sb, customType, selectOptions);
	customFieldHandle.put("hasDateField", hasDateField);
	sb.append("</div>");

	appendSubmitResetBtn(sb);
	// 总的表单内容
	model.addAttribute("formContent", sb.toString());
	return customFieldHandle;
    }

    private void appendSubmitResetBtn(StringBuilder sb) {
	sb.append("<div class=\"layui-form-item\">");
	sb.append("<div class=\"layui-input-block\">");
	sb.append("  <button class=\"layui-btn\" lay-submit lay-filter=\"submit-form\">提交</button>");
	sb.append("  <button type=\"reset\" class=\"layui-btn layui-btn-primary\">重置</button>");
	sb.append("</div>");
	sb.append(" </div>");
    }

    private void addPo(StringBuilder sb) {
	sb.append("<input type=\"button\" class=\"layui-btn layui-btn-primary\" id=\"relatePo\" value=\"关联类型\" />");
    }

    /**
     * 配置字段时，配置为选择当前Po的字段
     * 
     * @param columnArray
     * @param headers
     * @param customFieldMap
     * @param key
     */
    private void fieldSelect(String[] columnArray, String[] headers, Map<String, Map<String, Object>> customFieldMap,
	    String key) {
	Map<String, Object> map = new HashMap<>();
	map.put("type", "poColumns");
	map.put(SHOW_TYPE, "select");
	map.put(NAME, headers[1]);
	map.put(CODE, columnArray[1]);
	map.put("isPo", false);
	customFieldMap.put(key, map);
    }

    /**
     * 静态字段展现
     *
     * @param po
     * @param columnArray
     * @param headers
     * @return
     */
    private String staticFormField(Map<String, Object> po, String[] columnArray, String[] headers,
	    Map<String, Object> propMap) {
	JSONObject fieldQuery = new JSONObject();
	fieldQuery.put("poId", po.get(LABEL));
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = objectService.query(fieldQuery, "Field");
	Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
	for (Map<String, Object> fi : fieldInfoList) {
	    customFieldMap.put(String.valueOf(fi.get(ID)), fi);
	}
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item\">");
	List<Map<String, String>> customTypeList = new ArrayList<>();
	staticFieldHandle(columnArray, headers, customFieldMap, sb, customTypeList, propMap);

	staticCustomFieldHandle(sb, customTypeList);
	// 总的表单内容
	return sb.toString();
    }

    /**
     * 静态字段处理
     * 
     * @param columnArray
     * @param headers
     * @param fiMap
     * @param sb
     * @param customType
     * @return
     */
    private void staticFieldHandle(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
	    StringBuilder sb, List<Map<String, String>> customType, Map<String, Object> propMap) {
	for (int i = 0, k = 0; i < headers.length; i++) {
	    // 收集自定义字段信息
	    String code = columnArray[i];
	    Map<String, Object> field = fiMap.get(code);
	    String name = headers[i];
	    Object value = propMap.get(code);
	    if (field != null && !field.isEmpty()) {
		String type = String.valueOf(field.get("type"));
		Map<String, String> typeiMap = new HashMap<>();
		typeiMap.put("type", type);
		typeiMap.put(NAME, name);
		typeiMap.put(CODE, code);
		typeiMap.put("isPo", String.valueOf(field.get("isPo")));
		String showType = getShowType(field);

		typeiMap.put(SHOW_TYPE, showType);
		typeiMap.put("value", String.valueOf(value));
		customType.add(typeiMap);
	    } else {
		// 默认字段处理
		if (k > 1 && k % 3 == 0) {
		    sb.append("</div><div  class=\"layui-form-item\">");
		}

		sb.append(layFormItem(code, name, value, null));
		k++;
	    }
	}

	sb.append("</div>");
    }

    /**
     * 处理静态字段
     *
     * @param sb
     * @param customType
     */
    private void staticCustomFieldHandle(StringBuilder sb, List<Map<String, String>> customType) {
	for (Map<String, String> ctypei : customType) {
	    String isHide = ctypei.get("isHide");
	    if (isHide != null && isHide.equals("on")) {
		continue;
	    }
	    if (Boolean.valueOf(ctypei.get("isPo"))) {
		staticHandlePoType(ctypei, sb);
	    } else {
		staticHandleShowType(ctypei, sb);
	    }
	}
    }

    /**
     * render自定义字段Layui风格
     * 
     * @param model
     * @param sb
     * @param customType
     */
    private Map<String, Boolean> customFieldHandle(Model model, StringBuilder sb, List<Map<String, String>> customType,
	    List<Map<String, Object>> selectOptions) {
	Map<String, Boolean> fieldUseJsMap = new HashMap<>();
	StringBuilder js = new StringBuilder();
	StringBuilder switchJs = new StringBuilder();
	StringBuilder textEditorJs = new StringBuilder();
	StringBuilder searchHtml = new StringBuilder();
	StringBuilder searchValueJs = new StringBuilder();
	StringBuilder formVerifyJs = new StringBuilder();
	searchValueJs.append("var searchForm={}\n");
	int k = 0;
	boolean statusInSearch = false;
	Set<String> codeAttList = new HashSet<>();
	String label = String.valueOf(model.getAttribute("label"));
	for (Map<String, String> ctypei : customType) {
	    String fieldType = ctypei.get("type");
	    String name = ctypei.get(NAME);
	    String code = ctypei.get(CODE);
	    if (code.equals("status")) {
		statusInSearch = true;
	    }
	    String showType = ctypei.get(SHOW_TYPE);

	    String isSearchInput = ctypei.get("isSearchInput");

	    String isHide = ctypei.get("isHide");
	    if (isHide != null && isHide.equals("true")) {
		continue;
	    }
	    String fieldString = "";
	    confirmUseLayInfo(fieldUseJsMap, textEditorJs, codeAttList, code, showType);

	    textEditorVerify(formVerifyJs, code, showType);

	    String searchField = "";
	    if (fieldType.equals("poColumns")) {
		if ("select".equalsIgnoreCase(showType)) {
		    fieldString = poSelectType(ctypei, selectOptions);
		}
	    } else if (Boolean.valueOf(ctypei.get("isPo"))) {
		fieldString = handlePoType(ctypei, switchJs);
	    } else {
		fieldString = handleShowType(ctypei, js, switchJs);
		searchField = handleSearchShowType(ctypei, js, switchJs);
	    }
	    if (StringUtils.isNotBlank(isSearchInput) && "on".equalsIgnoreCase(isSearchInput)) {
		searchInput(searchValueJs, code);
		if ("status".equals(code)) {
		    statusSelect(searchHtml, label, code + "Reload", name);
		} else {
		    if (StringUtils.isBlank(fieldString)) {
			fieldString = layFormItem(code, name);
			searchHtml.append(layFormItem(code + "Reload", name));
		    } else {
			if (StringUtils.isNotBlank(searchField)) {
			    searchHtml.append(searchField);
			} else {
			    String search = fieldString.replaceAll(code, code + "Reload");
			    searchHtml.append(search);
			}
		    }
		}
	    }

	    // 默认字段处理
	    if (k != 0 && k % 3 == 0) {
		sb.append("</div><div  class=\"layui-form-item\">");
	    }
	    k++;
	    sb.append(fieldString);
	}

	js.append(switchJs.toString());
	js.append("\n});");

	if (!statusInSearch) {
	    searchInput(searchValueJs, "status");
	    String attribute = String.valueOf(model.getAttribute(COLUMNS));
	    if (attribute.indexOf(",status") > 0) {
		statusSelect(searchHtml, label, "statusReload", "状态");
	    }
	}

	if (searchHtml.isEmpty()) {// 如果搜索为空，则加一个默认的名称
	    searchHtml.append(layFormItem(KEY_WORD, "关键字"));
	    searchKeyWord(searchValueJs);
	}
	model.addAttribute("searcForm", searchHtml.toString());
	model.addAttribute("getSearchValue", searchValueJs.toString());
	model.addAttribute("renderSearchForm", "\nform.render('select');\n");

	modelCodeAtt(model, codeAttList);
	model.addAttribute("layField", js.toString());
	textEditorHandle(model, textEditorJs, formVerifyJs);
	return fieldUseJsMap;
    }

    private Map<String, Boolean> searchAllFieldHandle(Model model, StringBuilder sb,
	    String[] columnArray,
	    String[] headers,
	    Map<String, Map<String, Object>> customFieldMap) {
	Map<String, Boolean> fieldUseJsMap = new HashMap<>();
	StringBuilder js = new StringBuilder();
	StringBuilder switchJs = new StringBuilder();
	StringBuilder textEditorJs = new StringBuilder();
	StringBuilder searchHtml = new StringBuilder();
	StringBuilder searchValueJs = new StringBuilder();
	StringBuilder formVerifyJs = new StringBuilder();
	searchValueJs.append("var searchForm={}\n");
	int k = 0;
	boolean statusInSearch = false;
	Set<String> codeAttList = new HashSet<>();
	String label = String.valueOf(model.getAttribute("label"));
	
	for (int i=0;i<columnArray.length;i++ ) {
	   
	    String name = headers[i];
	    String code = columnArray[i];
	    Map<String, Object> mapData = customFieldMap.get(code);
	    if(mapData==null) {
		mapData = newMap();
	    }
	    if(code(mapData)==null) {
		mapData.put(CODE, code);
	    }
	    if(name(mapData)==null) {
		mapData.put(NAME, name);
	    }
	    Map<String, String> ctypei = toMapString2(mapData);
	    String showType = ctypei.get(SHOW_TYPE);
	    if(showType==null||"".equals(showType)) {
		showType="";
	    }
	    if (code.equals("status")) {
		statusInSearch = true;
	    }
	   
	    String fieldString = "";
	    confirmUseLayInfo(fieldUseJsMap, textEditorJs, codeAttList, code, showType);

	    textEditorVerify(formVerifyJs, code, showType);
	    fieldString = handleShowType(ctypei, js, switchJs);
	    String searchField = handleSearchShowType(ctypei, js, switchJs);
	    searchInput(searchValueJs, code);
	    if ("status".equals(code)) {
		statusSelect(searchHtml, label, code + "Reload", name);
	    } else {
		if (StringUtils.isBlank(fieldString)) {
		    fieldString = layFormItem(code, name);
		    searchHtml.append(layFormItem(code + "Reload", name));
		} else {
		    if (StringUtils.isNotBlank(searchField)) {
			searchHtml.append(searchField);
		    } else {
			String search = fieldString.replaceAll(code, code + "Reload");
			searchHtml.append(search);
		    }
		}
	    }

	    // 默认字段处理
	    if (k != 0 && k % 3 == 0) {
		sb.append("</div><div  class=\"layui-form-item\">");
	    }
	    k++;
	    sb.append(fieldString);
	}

	js.append(switchJs.toString());
	js.append("\n});");

	if (!statusInSearch) {
	    searchInput(searchValueJs, "status");
	    String attribute = String.valueOf(model.getAttribute(COLUMNS));
	    if (attribute.indexOf(",status") > 0) {
		statusSelect(searchHtml, label, "statusReload", "状态");
	    }
	}

	if (searchHtml.isEmpty()) {// 如果搜索为空，则加一个默认的名称
	    searchHtml.append(layFormItem(KEY_WORD, "关键字"));
	    searchKeyWord(searchValueJs);
	}
	model.addAttribute("searcForm", searchHtml.toString());
	model.addAttribute("getSearchValue", searchValueJs.toString());
	model.addAttribute("renderSearchForm", "\nform.render('select');\n");

	modelCodeAtt(model, codeAttList);
	model.addAttribute("layField", js.toString());
	textEditorHandle(model, textEditorJs, formVerifyJs);
	return fieldUseJsMap;
    }

    private void confirmUseDateTime(Map<String, Boolean> fieldUseJsMap, String showType) {
	if ("datetime".equalsIgnoreCase(showType) || "date".equalsIgnoreCase(showType)
		|| "time".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.put("hasDateField", true);
	}
    }

    private void confirmUsedFile(Map<String, Boolean> fieldUseJsMap, String showType) {
	if ("fileUpload".equalsIgnoreCase(showType) || "file".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.put("hasFile", true);
	}
    }

    private Map<String, Boolean> customFormField(Model model, StringBuilder sb, List<Map<String, String>> customType,
	    List<Map<String, Object>> selectOptions) {
	Map<String, Boolean> fieldUseJsMap = new HashMap<>();
	StringBuilder js = new StringBuilder();
	StringBuilder switchJs = new StringBuilder();
	StringBuilder textEditorJs = new StringBuilder();
	StringBuilder formVerifyJs = new StringBuilder();
	int k = 0;
	Set<String> codeAttList = new HashSet<>();
	String label = String.valueOf(model.getAttribute("label"));

	for (Map<String, String> ctypei : customType) {
	    String fieldType = ctypei.get("type");
	    String name = ctypei.get(NAME);
	    String code = ctypei.get(CODE);
	    String showType = ctypei.get(SHOW_TYPE);

	    String isHide = ctypei.get("isHide");
	    if (isHide != null && isHide.equals("true")) {
		continue;
	    }
	    String fieldString = "";
	    confirmUseLayInfo(fieldUseJsMap, textEditorJs, codeAttList, code, showType);
	    textEditorVerify(formVerifyJs, code, showType);
	    if (fieldType.equals("poColumns")) {
		if ("select".equalsIgnoreCase(showType)) {
		    fieldString = poSelectType(ctypei, selectOptions);
		}
	    } else if (Boolean.valueOf(ctypei.get("isPo"))) {
		fieldString = handlePoType(ctypei, switchJs);
	    } else {
		fieldString = handleShowType(ctypei, js, switchJs);
	    }

	    // 默认字段处理
	    if (k != 0 && k % 3 == 0) {
		sb.append("</div><div  class=\"layui-form-item\">");
	    }

	    k++;
	    sb.append(fieldString);
	}

	js.append(switchJs.toString());
	js.append("\n});");

	modelCodeAtt(model, codeAttList);
	model.addAttribute("layField", js.toString());
	textEditorHandle(model, textEditorJs, formVerifyJs);
	return fieldUseJsMap;
    }

    private void textEditorVerify(StringBuilder formVerifyJs, String code, String showType) {
	if (showType.equalsIgnoreCase("textEditor")) {
	    formVerify(formVerifyJs, code);
	}
    }

    private void textEditorHandle(Model model, StringBuilder textEditorJs, StringBuilder formVerifyJs) {
	if (textEditorJs.length() > 0) {
	    model.addAttribute("textEditorValue", textEditorJs.toString());
	    model.addAttribute(FORM_VERIFY_JS, formVerifyJs.toString());
	    model.addAttribute(EDIT_INDEX, "var " + EDIT_INDEX + "={};");
	}
    }

    /**
     * 判断lay模块使用情况
     * 
     * @param fieldUseJsMap
     * @param textEditorJs
     * @param codeAttList
     * @param code
     * @param showType
     */
    private void confirmUseLayInfo(Map<String, Boolean> fieldUseJsMap, StringBuilder textEditorJs,
	    Set<String> codeAttList, String code, String showType) {
	confirmUseDateTime(fieldUseJsMap, showType);
	confirmUsedFile(fieldUseJsMap, showType);
	confirmeTextEditor(fieldUseJsMap, textEditorJs, code, showType);
	confirmUseCode(fieldUseJsMap, codeAttList, code, showType);
    }

    private void confirmUseCode(Map<String, Boolean> fieldUseJsMap, Set<String> codeAttList, String code,
	    String showType) {
	if ("javaCode".equalsIgnoreCase(showType) || "htmlCode".equalsIgnoreCase(showType)
		|| "c++Code".equalsIgnoreCase(showType) || "javaScriptCode".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.put(CODE, true);
	    codeAttList.add(code);
	}
    }

    private void confirmeTextEditor(Map<String, Boolean> fieldUseJsMap, StringBuilder textEditorJs, String code,
	    String showType) {
	if ("textEditor".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.put(HAS_TEXT_EDITOR, true);
	    textEditorJs.append("\n if(data." + code + "!=undefined){\n" + "$('#" + code + "').val(data." + code
		    + ");\n" + " " + EDIT_INDEX + "['" + code + "'] = layedit.build('" + code + "');\n " + " }\n");
	}
    }

    private void modelCodeAtt(Model model, Set<String> codeAttList) {
	if (!codeAttList.isEmpty()) {
	    StringBuilder codeSb = new StringBuilder();
	    StringBuilder codeInit = new StringBuilder();
	    for (String codei : codeAttList) {
		codeSb.append(" $('" + codei + "').html(rowi." + codei + ")");
		codeInit.append(" $('" + codei + "').html(data['" + codei + "'])");
	    }
	    model.addAttribute("codeSet", codeSb.toString());
	    model.addAttribute("codeInit", codeInit.toString());
	}
    }

    private String getShowType(Map<String, Object> ctypei) {
	String showType = String.valueOf(ctypei.get(SHOW_TYPE));

	if (CommonUtil.isNumber(showType)) {
	    Map<String, Object> nodePropertiesById = driver.getNodePropertiesById(Long.parseLong(showType));
	    showType = String.valueOf(nodePropertiesById.get(CODE));
	}
	return showType;
    }

    /**
     * 字段定义表单处理
     * 
     * @param columnArray
     * @param headers
     * @param customFieldMap
     * @param sb
     * @param customType
     * @return
     */
    private boolean fieldFormHandle(String[] columnArray, String[] headers,
	    Map<String, Map<String, Object>> customFieldMap, StringBuilder sb, List<Map<String, String>> customType) {
	boolean hasDateField = false;
	for (int i = 0, k = 0; i < headers.length; i++) {
	    // 收集自定义字段信息
	    Map<String, Object> customField = customFieldMap.get(columnArray[i]);
	    if (customField != null && !customField.isEmpty()) {
		String type = String.valueOf(customField.get("type"));
		Map<String, String> typeiMap = new HashMap<>();
		typeiMap.put("type", type);
		typeiMap.put(NAME, headers[i]);
		typeiMap.put(CODE, columnArray[i]);
		Object valueField = customField.get("valueField");
		if (valueField != null && !"null".equals(valueField)) {
		    typeiMap.put("valueField", String.valueOf(valueField));
		}
		typeiMap.put("isPo", String.valueOf(customField.get("isPo")));
		String showType = getShowType(customField);
		typeiMap.put(SHOW_TYPE, showType);
		if ("date".equals(showType)) {
		    hasDateField = true;
		}
		customType.add(typeiMap);
	    } else {
		// 默认字段处理
		if (k > 1 && k % 3 == 0) {
		    sb.append("</div><div  class=\"layui-form-item\">");
		}
		sb.append(layFormItem(columnArray[i], headers[i]));
		k++;
	    }
	}
	return hasDateField;
    }

    private boolean fieldWithStatusHandle(String[] columnArray, String[] headers,
	    Map<String, Map<String, Object>> fiMap, StringBuilder sb, List<Map<String, String>> customType,
	    String poLabel) {
	boolean hasDateField = false;
	for (int i = 0, k = 0; i < columnArray.length; i++) {
	    // 收集自定义字段信息
	    String columni = columnArray[i];

	    if ("".equals(columni.trim())) {
		continue;
	    }
	    Map<String, Object> field = fiMap.get(columni);
	    String headeri = headers[i];
	    if (field != null && !field.isEmpty()) {
		String showType = string(field, "showType");
		if (showType != null && !showType.isEmpty()) {
		    hasDateField = handleCustomColumn(customType, hasDateField, columni, field, headeri);
		    if (columni.equals("status")) {
			statusSelect(sb, poLabel, columni, headeri);
			k++;
		    }
		}
		if (!string(field, "isSearchInput").isEmpty()) {
		    handleCustomColumn(customType, hasDateField, columni, field, headeri);
		}
	    }
	    k = columnHtml(sb, poLabel, k, columni, headeri);
	}
	return hasDateField;
    }

    private boolean handleCustomColumn(List<Map<String, String>> customType, boolean hasDateField, String columni,
	    Map<String, Object> field, String headeri) {
	String type = String.valueOf(field.get("type"));
	Map<String, String> typeiMap = new HashMap<>();
	typeiMap.put("type", type);
	typeiMap.put(NAME, headeri);
	typeiMap.put(CODE, columni);
	typeiMap.put("isPo", String.valueOf(field.get("isPo")));
	typeiMap.put("valueField", String.valueOf(field.get("valueField")));
	typeiMap.put(COLUMN_VALIDATOR, String.valueOf(field.get(COLUMN_VALIDATOR)));

	String showType = String.valueOf(field.get(SHOW_TYPE));
	Object object = field.get("isSearchInput");
	if (null != object) {
	    typeiMap.put("isSearchInput", String.valueOf(object));
	}
	switchOnHandle(field, typeiMap);
	copyTemplateInfo(field, typeiMap);
	typeiMap.put(SHOW_TYPE, showType);
	if ("date".equals(showType)) {
	    hasDateField = true;
	}
	customType.add(typeiMap);
	return hasDateField;
    }

    private int columnHtml(StringBuilder sb, String poLabel, int k, String columni, String headeri) {
	// 默认字段处理
	if (k > 1 && k % 3 == 0) {
	    sb.append("</div><div  class=\"layui-form-item\">");
	}
	if (columni.equals("status")) {
	    statusSelect(sb, poLabel, columni, headeri);
	} else {
	    sb.append(layFormItem(columni, headeri));
	}

	k++;
	return k;
    }

    private void statusSelect(StringBuilder sb, String poLabel, String columni, String headeri) {
	String query = Neo4jOptCypher.getStatusList(poLabel);
	List<Map<String, Object>> selectList = neo4jService.cypher(query);
	Map<String, String> ctypei = new HashMap<>();
	ctypei.put(NAME, headeri);
	ctypei.put(CODE, columni);
	sb.append(poSelectType(ctypei, selectList));
    }

    private boolean fieldHandleWithStatus(String[] columnArray, String[] headers,
	    Map<String, Map<String, Object>> fiMap, Map<String, Map<String, Object>> fivMap, StringBuilder sb,
	    List<Map<String, String>> customType, String poLabel) {
	boolean hasDateField = false;
	for (int i = 0, k = 0; i < headers.length; i++) {
	    // 收集自定义字段信息
	    String columni = columnArray[i];
	    Map<String, Object> field = fiMap.get(columni);
	    Map<String, Object> vfield = fivMap.get(columni);
	    String headeri = headers[i];
	    if (field != null && !field.isEmpty()) {
		Object showTypeobj = field.get(SHOW_TYPE);
		if (showTypeobj == null || "".equals(showTypeobj)) {
		    if (k > 1 && k % 3 == 0) {
			sb.append("</div><div  class=\"layui-form-item\">");
		    }
		    if (columni.equals("status")) {
			statusSelect(sb, poLabel, columni, headeri);
		    } else {
			formFieldString(sb, columni, vfield, headeri);
		    }
		    k++;
		} else {
		    String showType = String.valueOf(showTypeobj);
		    String type = String.valueOf(field.get("type"));
		    Map<String, String> typeiMap = new HashMap<>();
		    typeiMap.put("type", type);
		    typeiMap.put(NAME, headeri);
		    typeiMap.put(CODE, columni);
		    typeiMap.put("isPo", String.valueOf(field.get("isPo")));
		    typeiMap.put("valueField", String.valueOf(field.get("valueField")));
		    if (vfield != null) {
			typeiMap.put(COLUMN_VALIDATOR, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
		    }
		    copyTemplateInfo(field, typeiMap);

		    Object object = field.get("isSearchInput");
		    if (null != object) {
			typeiMap.put("isSearchInput", String.valueOf(object));
		    }
		    switchOnHandle(field, typeiMap);

		    typeiMap.put(SHOW_TYPE, showType);
		    if ("date".equals(showType)) {
			hasDateField = true;
		    }
		    customType.add(typeiMap);
		}
	    } else {
		// 默认字段处理
		if (k > 1 && k % 3 == 0) {
		    sb.append("</div><div  class=\"layui-form-item\">");
		}
		if (columni.equals("status")) {
		    statusSelect(sb, poLabel, columni, headeri);
		} else {
		    formFieldString(sb, columni, vfield, headeri);
		}

		k++;
	    }
	}
	return hasDateField;
    }

    private void formFieldString(StringBuilder sb, String columni, Map<String, Object> vfield, String headeri) {
	if (vfield != null) {
	    sb.append(layFormItem(columni, headeri, String.valueOf(vfield.get(COLUMN_VALIDATOR))));
	} else {
	    sb.append(layFormItem(columni, headeri, null));
	}
    }

    private void switchOnHandle(Map<String, Object> field, Map<String, String> typeiMap) {
	handleBooleanColumn(field, typeiMap, "isHide");
	handleBooleanColumn(field, typeiMap, "disabled");
	handleBooleanColumn(field, typeiMap, "readOnly");
    }

    private void handleBooleanColumn(Map<String, Object> field, Map<String, String> typeiMap, String columnKey) {
	if (getBoolean(field, columnKey)) {
	    typeiMap.put(columnKey, "true");
	}
    }

    private void copyTemplateInfo(Map<String, Object> field, Map<String, String> typeiMap) {
	copyTemplateContent(field, typeiMap, TABLE_TEMPLATE_ID);
	copyTemplateContent(field, typeiMap, TABLE_TEMPLATE_CONTENT);
    }

    private void copyTemplateContent(Map<String, Object> field, Map<String, String> typeiMap, String key) {
	Object templateContent = field.get(key);
	if (templateContent != null && !"".equals(templateContent)) {// 表格模板
	    typeiMap.put(key, String.valueOf(templateContent));
	}
    }

    private boolean fieldHandle1(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
	    Map<String, Map<String, Object>> fivMap, List<Map<String, String>> customType) {
	boolean hasDateField = false;
	for (int i = 0, k = 0; i < headers.length; i++) {
	    // 收集自定义字段信息
	    Map<String, Object> field = fiMap.get(columnArray[i]);
	    Map<String, Object> vfield = fivMap.get(columnArray[i]);
	    if (field != null && !field.isEmpty()) {
		String type = String.valueOf(field.get("type"));
		Map<String, String> typeiMap = new HashMap<>();
		typeiMap.put("type", type);
		typeiMap.put(NAME, headers[i]);
		typeiMap.put(CODE, columnArray[i]);
		typeiMap.put("isPo", String.valueOf(field.get("isPo")));
		typeiMap.put("valueField", String.valueOf(field.get("valueField")));
		if (vfield != null) {
		    typeiMap.put(COLUMN_VALIDATOR, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
		}

		String showType = String.valueOf(field.get(SHOW_TYPE));
		Object object = field.get("isSearchInput");
		if (null != object) {
		    typeiMap.put("isSearchInput", String.valueOf(object));
		}
		switchOnHandle(field, typeiMap);
		copyTemplateInfo(field, typeiMap);
		typeiMap.put(SHOW_TYPE, showType);
		if ("date".equals(showType)) {
		    hasDateField = true;
		}
		customType.add(typeiMap);
	    }
	}
	return hasDateField;
    }

    /**
     * table 列定义
     * 
     * @param model
     * @param columnArray
     * @param headers
     */
    private void tableInfo(Model model, String[] columnArray, String[] headers) {
	List<Map<String, String>> cols = new ArrayList<>();
	for (int i = 0, k = 0; i < headers.length; i++) {
	    Map<String, String> piMap = new HashMap<>();
	    piMap.put(CODE, "{field:'" + columnArray[i] + "', sort: true}");
	    piMap.put(NAME, headers[i]);
	    piMap.put(FIELD, columnArray[i]);
	    cols.add(piMap);
	}
	model.addAttribute("cols", cols);
	model.addAttribute("colCodes", columnArray);
    }

    /**
     * 添加table的模板字段
     * 
     * @param model
     * @param columnArray
     * @param headers
     */
    private void tableColumnTemplate(Model model, String[] columnArray, String[] headers,
	    List<Map<String, String>> customType) {
	Map<String, String> tempalteInfoMap = new HashMap<>();
	Map<String, String> tempalteContentMap = new HashMap<>();

	for (Map<String, String> ctypei : customType) {
	    String value = ctypei.get(TABLE_TEMPLATE_ID);
	    if (value != null && !value.trim().equals("")) {
		tempalteInfoMap.put(ctypei.get(CODE), value);
		tempalteContentMap.put(ctypei.get(CODE), ctypei.get(TABLE_TEMPLATE_CONTENT));
	    }
	}
	if (!tempalteContentMap.isEmpty() && tempalteContentMap.size() > 0) {
	    StringBuffer sBuffer = new StringBuffer();
	    for (Entry<String, String> ei : tempalteContentMap.entrySet()) {
		// 替换模板中的字段
		String value = ei.getValue();
		if (!ei.getKey().equals("unicode")) {
		    value = value.replace("{{d.unicode}}", "{{d." + ei.getKey() + "}}");
		}
		sBuffer.append(value);
	    }
	    String htmlUnescape = HtmlUtils.htmlUnescape(sBuffer.toString());
	    htmlUnescape = htmlUnescape.replaceAll("<p>", "");
	    htmlUnescape = htmlUnescape.replaceAll("</p>", "");
	    model.addAttribute("tempalteContent", htmlUnescape);
	} else {
	    model.addAttribute("tempalteContent", " ");
	}
	List<Map<String, String>> cols = new ArrayList<>();
	for (int i = 0, k = 0; i < headers.length; i++) {
	    Map<String, String> piMap = new HashMap<>();
	    String column = columnArray[i];
	    piMap.put(CODE, "{field:'" + column + "', sort: true}");
	    piMap.put(NAME, headers[i]);
	    Boolean find = false;
	    String title = " code label name title content desc remark Key columns header address time ";
	    String widthValue = "minWidth: 200,maxWidth: 250,edit: 'text',";
	    find = widthValue(piMap, column, find, title, widthValue);

	    
	    if (!find) {
		if (column.equals(ID)) {
		    String widthValue1 = "width: '80',fixed: 'left',";
		    piMap.put("width", widthValue1);
		}else {
		    piMap.put("width", "minWidth: 80,maxWidth: 250,");
		}
	    }

	    piMap.put(FIELD, column);
	    String string = tempalteInfoMap.get(column);
	    if (string != null && !string.trim().equals("")) {
		piMap.put("templat", ",templet:'#" + string + "'");
	    } else {
		piMap.put("templat", " ");
	    }
	    cols.add(piMap);
	}

	model.addAttribute("cols", cols);
	model.addAttribute("colCodes", columnArray);
    }

    private Boolean widthValue(Map<String, String> piMap, String column, Boolean find, String title,
	    String widthValue) {
	String[] split = title.split(" ");
	for (String key : split) {
	    if ("".equals(key.trim())) {
		continue;
	    }
	    if (column.toLowerCase().startsWith(key.toLowerCase())) {
		piMap.put("width", widthValue);
		find = true;
		break;
	    }
	}
	return find;
    }

    /**
     * laymodule使用组装
     * 
     * @param model
     * @param useTab
     */
    private void useLayModule(Model model, Boolean useTab, Map<String, Boolean> boolMap) {
	StringBuilder layUse = new StringBuilder();
	layUseJs(layUse, boolMap, useTab);
	model.addAttribute(LAY_USE, layUse.toString());
    }

    /**
     * handle 字段展示类型
     *
     * @param js
     * @param switchJs
     */
    private String handleShowType(Map<String, String> ctypei, StringBuilder js, StringBuilder switchJs) {
	String fieldType = ctypei.get("type");
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String valueField = ctypei.get("valueField");
	String validator = ctypei.get(COLUMN_VALIDATOR);
	String showType = ctypei.get(SHOW_TYPE);
	String fieldString = "";
	if ("textarea".equalsIgnoreCase(showType)) {
	    fieldString = textArea(name, code, validator, getReadOnly(ctypei));
	}
	if ("textEditor".equalsIgnoreCase(showType)) {
	    fieldString = textArea(name, code, validator, getReadOnly(ctypei));
	    js.append(" layedit.set({uploadImage: {url: '"+ LemodoApplication.MODULE_NAME+"/file/uploadImage',type:'post',success: function(data){ \r\n"
	    	+ "               console.log(data); \r\n"
	    	+ "            }}});\n");
	    js.append(" var " + code + "Index = layedit.build('" + code + "');\n");
	}
	if ("line".equalsIgnoreCase(showType)) {
	    fieldString = layFormLine(code, name, null);
	}

	if ("password".equalsIgnoreCase(showType)) {
	    fieldString = password(name, code, validator);
	}
	if ("date".equalsIgnoreCase(showType)) {
	    fieldString = date(name, code, validator, getDisabled(ctypei));
	    js.append(" \n" + " laydate.render({\n" + "	 elem: '#" + code + "'\n" + " });\n");
	}
	if ("switch".equalsIgnoreCase(showType)) {
	    fieldString = switchOn(ctypei, "ON|OFF", switchJs);
	}
	if ("datetime".equalsIgnoreCase(showType)) {
	    fieldString = dateTime(name, code, validator, getDisabled(ctypei));
	    js.append(" \n" + " laydate.render({\n" + "	 elem: '#" + code + "',type: 'datetime'\n" + " });\n");
	}
	if ("time".equalsIgnoreCase(showType)) {
	    fieldString = dateTime(name, code, validator, getDisabled(ctypei));
	    js.append(" \n" + " laydate.render({\n" + "	 elem: '#" + code + "',type: 'time'\n" + " });\n");
	}
	if ("fileUpload".equalsIgnoreCase(showType) || "file".equalsIgnoreCase(showType)) {
	    String url = LemodoApplication.MODULE_NAME+"/file/upload";
	    if (code.equals("driverFile")) {
		url = LemodoApplication.MODULE_NAME+"/file/uploadDriver";
	    }

	    js.append(" upload.render({\r\n" + "		      elem: '#" + code + "Choose',url: '" + url
		    + "'\r\n" + "       ,accept: 'file' //普通文件\r\n" + "       ,exts: 'jar' //只允许上传jar文件\r\n"
		    + "      ,auto: false,bindAction: '#" + code + "UpBtn',done: function(res){\r\n"
		    + "		       $('#" + code
		    + "').val(res.data); layer.msg('上传成功');\r\n 		      }\r\n"
		    + "		    });");

	    fieldString = fileUpload(name, code, validator);
	}
	if ("iconFont".equalsIgnoreCase(showType)) {
	    fieldString = iconFont(name, code);
	}

	if ("javascriptCode".equalsIgnoreCase(showType) || "javaCode".equalsIgnoreCase(showType)) {
	    fieldString = javaCode(name, code);
	    js.append(" layui.code({");
	    js.append(" 	  title: '代码' ");
	    js.append(" 	  ,skin: 'java'");
	    js.append(" 	});");
	}
	return fieldString;
    }

    private Boolean getBoolean(Map<String, Object> ctypei, String key) {
	Object switchValue = ctypei.get(key);
	if (switchValue == null || switchValue.equals("") || switchValue.equals("off") || switchValue.equals("null")) {
	    return false;
	}
	if (String.valueOf(switchValue).equals("on")) {
	    return true;
	}
	return Boolean.valueOf(String.valueOf(switchValue));
    }

    private Boolean getReadOnly(Map<String, String> ctypei) {
	String readOnly = ctypei.get("readOnly");
	if (readOnly == null || readOnly.equals("")) {
	    return false;
	}
	return Boolean.valueOf(readOnly);
    }

    private Boolean getDisabled(Map<String, String> ctypei) {
	String disabled = ctypei.get("disabled");
	if (disabled == null || disabled.equals("")) {
	    return false;
	}
	return Boolean.valueOf(disabled);
    }

    /**
     * 查询表单字段
     * 
     * @param ctypei
     * @param js
     * @param switchJs
     * @return
     */
    private String handleSearchShowType(Map<String, String> ctypei, StringBuilder js, StringBuilder switchJs) {
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE) + "Reload";
	String validator = "";

	String showType = ctypei.get(SHOW_TYPE);
	String fieldString = "";
	if ("textarea".equalsIgnoreCase(showType)) {
	    fieldString = textArea(name, code, validator, getReadOnly(ctypei));
	}
	if ("textEditor".equalsIgnoreCase(showType)) {
	    fieldString = textArea(name, code, validator, getReadOnly(ctypei));
	    js.append(" layedit.set({uploadImage: {url: '"+LemodoApplication.MODULE_NAME+"/file/uploadImage',type:'post',success: function(data){ \r\n"
	    	+ "               console.log(data); \r\n"
	    	+ "            }}});\n");
	    js.append(" var " + code + "Index = layedit.build('" + code + "');\n");
	}
	if ("line".equalsIgnoreCase(showType)) {
	    fieldString = layFormLine(code, name, null);
	}
	if ("password".equalsIgnoreCase(showType)) {
	    fieldString = password(name, code, validator);
	}
	if ("date".equalsIgnoreCase(showType)) {
	    fieldString = date(name, code, validator, getReadOnly(ctypei));
	    js.append(" \n" + " laydate.render({\n" + "	 elem: '#" + code + "'\n"
		    + ",trigger: 'click',format:'yyyy-MM-dd' });");
	}
	if ("switch".equalsIgnoreCase(showType)) {
	    fieldString = switchOn(ctypei, "ON|OFF", switchJs);
	}
	if ("datetime".equalsIgnoreCase(showType)) {
	    fieldString = dateTime(name, code, validator, getDisabled(ctypei));
	    js.append(" \n" + " laydate.render({\n" + "	 elem: '#" + code + "'\n"
		    + ",trigger: 'click' ,format:'yyyy-MM-dd HH:mm:ss'" + "            ,type:'datetime' });");
	}
	return fieldString;
    }

    private void staticHandleShowType(Map<String, String> ctypei, StringBuilder sb) {

	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String showType = ctypei.get(SHOW_TYPE);
	String value = ctypei.get("value");
	String validator = ctypei.get(COLUMN_VALIDATOR);
	String fieldString = "";
	if ("textarea".equalsIgnoreCase(showType)) {
	    fieldString = textArea(name, code, value, validator, getReadOnly(ctypei));
	}
	if ("password".equalsIgnoreCase(showType)) {
	    // password(name,code, sb);
	}
	if ("date".equalsIgnoreCase(showType)) {
	    fieldString = date(name, code, value, validator, getDisabled(ctypei));

	}
	if ("switch".equalsIgnoreCase(showType)) {
	    fieldString = switchOn(ctypei, "ON|OFF", null);
	}
	if ("datetime".equalsIgnoreCase(showType)) {
	    fieldString = dateTime(name, code, value, validator, getDisabled(ctypei));
	}
    }

    /**
     * 收集处理Po类型
     *
     * @param switchJs
     */
    private String handlePoType(Map<String, String> ctypei, StringBuilder switchJs) {
	String fieldType = ctypei.get("type");
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String showType = ctypei.get(SHOW_TYPE);
	String valueField = ctypei.get("valueField");
	List<Map<String, Object>> selectList = selectList(ctypei, fieldType, valueField);
	String fieldString = "";
	if ("select".equalsIgnoreCase(showType)) {
	    fieldString = poSelectType(ctypei, selectList);
	}
	// if ("iconFont".equalsIgnoreCase(showType)) {
	// fieldString = poSelectIconFont(ctypei, selectList);
	// }
	if ("manage".equalsIgnoreCase(showType)) {
	    fieldString = manage(name, code);
	    manageClick(code, "管理" + name, LemodoApplication.MODULE_NAME+"/manage/" + fieldType, switchJs);
	}
	if ("window".equalsIgnoreCase(showType)) {
	    fieldString = manage(name, code);
	    manageClick(code, "选择" + name, LemodoApplication.MODULE_NAME+"/manage/" + fieldType + "/Po", switchJs);
	}
	if ("iconFont".equalsIgnoreCase(showType)) {
	    fieldString = manage(name, code);
	    iconSelectClick(code, "选择" + name, LemodoApplication.MODULE_NAME+"/objectRel/" + fieldType + "/iconFont", switchJs);
	}
	if ("checkBox".equalsIgnoreCase(showType)) {
	    fieldString = poCheckBox(ctypei, selectList);
	}
	if ("radio".equalsIgnoreCase(showType)) {
	    fieldString = poRadio(ctypei, selectList);
	}
	if ("switch".equalsIgnoreCase(showType)) {
	    fieldString = poSwitchOn(ctypei, selectList, switchJs);
	}
	return fieldString;
    }

    /**
     * 获取某个类的所有数据，id,xx,name
     * 
     * @param ctypei
     * @param fieldType
     * @param valueField
     * @return
     */
    private List<Map<String, Object>> selectList(Map<String, String> ctypei, String fieldType, String valueField) {
	String[] columns = SELECT_COLUMN.split(",");
	if (ctypei.containsKey("valueField") && !valueField.trim().equals("") && !valueField.equals(ID)
		&& !valueField.equals(CODE)) {
	    String string = "id," + valueField + ",name";
	    columns = string.split(",");
	}

	String query = Neo4jOptCypher.queryObj(null, fieldType, columns);
	List<Map<String, Object>> selectList = neo4jService.cypher(query);
	return selectList;
    }

    /**
     * 弹出层选择对象，获取当前选择对象数据
     *
     * @param code
     * @param switchJs
     */
    private void manageClick(String code, String title, String url, StringBuilder switchJs) {

	switchJs.append("layui.$('#" + code + "').on('click', function(data){\n");

	switchJs.append("     layer.open({\n");
	switchJs.append("      type: 2,\n");
	switchJs.append("      anim: 0,\n");
	switchJs.append("      shade: 0,\n");
	switchJs.append("      maxmin: true,\n");
	switchJs.append("      title: '" + title + "',\n");
	switchJs.append("      area: ['100%', '100%'],\n");
	switchJs.append("      btn:['关闭'],\n");
	switchJs.append("      yes:function(index,layero)\n");
	switchJs.append("      {\n");
	switchJs.append("      var body = layer.getChildFrame('body', index);\n");
	switchJs.append("      var selected = body.find('#selectObj').val();\n");
	// switchJs.append(" var selectedName = body.find('#selectObjName').val();\n");

	// switchJs.append(" $(\"#relationObj\").val(selectedName);\n");
	switchJs.append("      $(\'#" + code + "').val(selected);\n");
	switchJs.append("      	      	  close()\n");
	switchJs.append("      	          //index为当前层索引\n");
	switchJs.append("      	          layer.close(index)\n");
	switchJs.append("      },\n");
	switchJs.append("      cancel:function(){//右上角关闭毁回调\n");
	switchJs.append("      	     	 close()\n");
	switchJs.append("      	     	 var index = parent.layer.getFrameIndex(data.name); //先得到当前iframe层的索引\n");
	switchJs.append("      	     		parent.layer.close(index); //再执行关闭\n");
	switchJs.append("      },\n");
	switchJs.append("      zIndex: layer.zIndex //重点1\n");
	switchJs.append("      ,success: function(layero, index){\n");
	switchJs.append("      	        layer.setTop(layero); //重点2\n");
	switchJs.append("               var body = layer.getChildFrame('body', index);\n");
	switchJs.append("               var objId=body.find('#objId');\n");
	switchJs.append("               if(objId!=null&&currentNode!=null){          \n");
	switchJs.append("		   			if(currentNode.id!=undefined){");
	switchJs.append("						objId.val(currentNode.id); \n");
	switchJs.append("					}else if(currentNode.code!=undefined){");
	switchJs.append("        			     objId.val(currentNode.code);     ");
	switchJs.append("					}}        \n");

	switchJs.append("      },		\n");
	switchJs.append("      content: '" + url + "'\n");
	switchJs.append("      	     });\n");
	switchJs.append("      	    });\n");
    }

    private void iconSelectClick(String code, String title, String url, StringBuilder clickJs) {

	clickJs.append("layui.$('#" + code + "').on('click', function(data){\n");

	clickJs.append("     layer.open({\n");
	clickJs.append("      type: 2,\n");
	clickJs.append("      anim: 0,\n");
	clickJs.append("      shade: 0,\n");
	clickJs.append("      maxmin: true,\n");
	clickJs.append("      title: '" + title + "',\n");
	clickJs.append("      area: ['100%', '100%'],\n");
	clickJs.append("      btn:['关闭'],\n");
	clickJs.append("      yes:function(index,layero)\n");
	clickJs.append("      {\n");
	clickJs.append("      var body = layer.getChildFrame('body', index);\n");
	clickJs.append("      var selected = body.find('#selectObj').val();\n");
	clickJs.append("      var selectValue = body.find('#selectValue').val();\n");
	// switchJs.append(" var selectedName = body.find('#selectObjName').val();\n");

	// clickJs.append(" $(\"#selectValue\").val(selectValue);\n");
	// clickJs.append(" $(\"#relationObj\").val(selectedName);\n");
	clickJs.append("      $(\'#" + code + "').val(selectValue);\n");
	clickJs.append("      	      	  close()\n");
	clickJs.append("      	          //index为当前层索引\n");
	clickJs.append("      	          layer.close(index)\n");
	clickJs.append("      },\n");
	clickJs.append("      cancel:function(){//右上角关闭毁回调\n");
	clickJs.append("      	     	 close()\n");
	clickJs.append("      	     	 var index = parent.layer.getFrameIndex(data.name); //先得到当前iframe层的索引\n");
	clickJs.append("      	     		parent.layer.close(index); //再执行关闭\n");
	clickJs.append("      },\n");
	clickJs.append("      zIndex: layer.zIndex //重点1\n");
	clickJs.append("      ,success: function(layero, index){\n");
	clickJs.append("      	        layer.setTop(layero); //重点2\n");
	clickJs.append("               var body = layer.getChildFrame('body', index);\n");
	clickJs.append("               var objId=body.find('#objId');\n");
	clickJs.append("               if(objId!=null&&currentNode!=null){          \n");
	clickJs.append("		   			if(currentNode.id!=undefined){");
	clickJs.append("						objId.val(currentNode.id); \n");
	clickJs.append("					}else if(currentNode.code!=undefined){");
	clickJs.append("        			     objId.val(currentNode.code);     ");
	clickJs.append("					}}        \n");

	clickJs.append("      },		\n");
	clickJs.append("      content: '" + url + "'\n");
	clickJs.append("      	     });\n");
	clickJs.append("      	    });\n");
    }

    private void staticHandlePoType(Map<String, String> ctypei, StringBuilder sb) {
	String fieldType = ctypei.get("type");
	String showType = ctypei.get(SHOW_TYPE);

	String[] columns = SELECT_COLUMN.split(",");
	String query = Neo4jOptCypher.queryObj(null, fieldType, columns);
	List<Map<String, Object>> selectList = neo4jService.cypher(query);
	String fieldHtml = "";
	if ("select".equalsIgnoreCase(showType)) {
	    fieldHtml = poSelectType(ctypei, selectList);
	}
	if ("checkBox".equalsIgnoreCase(showType)) {
	    fieldHtml = poCheckBox(ctypei, selectList);
	}
	if ("radio".equalsIgnoreCase(showType)) {
	    fieldHtml = poRadio(ctypei, selectList);
	}
	if ("switch".equalsIgnoreCase(showType)) {
	    fieldHtml = poSwitchOn(ctypei, selectList, null);
	}
	sb.append(fieldHtml);
    }

    /**
     * 生成layuse相关代码
     * 
     * @param layUse
     */
    private void layUseJs(StringBuilder layUse, Map<String, Boolean> layUseMap, Boolean useTab) {
	Set<String> modules = new HashSet<String>();
	modules.add("form");
	// modules.add("'laytpl'");

	modules.add("table");
	List<String> declares = new ArrayList<>();
	declares.add(" form = layui.form\n");
	declares.add(" ,table = layui.table\n");
	// declares.add(" ,laytpl = layui.laytpl\n");

	declares.add(" ,layer = layui.layer;\n");
	if (useTab) {
	    useModule(modules, declares, "element");
	}
	if (layUseMap.containsKey(HAS_TEXT_EDITOR) && layUseMap.get(HAS_TEXT_EDITOR)) {
	    useModule(modules, declares, "layedit");
	}

	if (layUseMap.containsKey(HAS_FILE) && layUseMap.get(HAS_FILE)) {
	    useModule(modules, declares, "upload");
	}

	if (layUseMap.containsKey(HAS_DATE_FIELD) && layUseMap.get(HAS_DATE_FIELD)) {
	    useModule(modules, declares, "laydate");
	}
	if (layUseMap.containsKey(CODE) && layUseMap.get(CODE)) {
	    useModule(modules, declares, CODE);
	}

	String jsGlobalParam = "\n var layer,form,table,crudTable;";
	String join = "'" + String.join("','", modules) + "'";
	layUse.append(jsGlobalParam + "\n " + layuiConfig + ".use([" + join + "], function(){\n"
		+ String.join(" ", declares));
    }

    /**
     * 字段表单Tabjs
     * 
     * @param model
     */
    private void fieldFormTabJs(Model model) {
	StringBuilder layUse = new StringBuilder();
	Set<String> modules = new HashSet<String>();
	modules.add("'form'");
	List<String> declares = new ArrayList<>();
	declares.add(" layer = layui.layer;\n");
	declares.add(" form = layui.form;\n");
	useModule(modules, declares, "element");
	String jsGlobalParam = "\n var layer,form;";

	layUse.append(jsGlobalParam + "\n " + layuiConfig + ".use([" + String.join(",", modules) + "], function(){\n"
		+ String.join(" ", declares));
	model.addAttribute(LAY_USE, layUse.toString());
	model.addAttribute("layField", "});");
    }

    private void useModule(Set<String> modules, List<String> declares, String module) {
	modules.add(module);
	declares.add("var " + module + " = layui." + module + ";\n");
    }

    /**
     * 开关
     *
     */
    private String poSwitchOn(Map<String, String> ctypei, List<Map<String, Object>> selectList,
	    StringBuilder switchJs) {
	List<String> switchText = new ArrayList<>(2);
	for (Map<String, Object> opti : selectList) {
	    // switchText.add(opti.get(ID));
	    switchText.add(String.valueOf(opti.get(NAME)));
	}
	return switchOn(ctypei, String.join("|", switchText), switchJs);
    }

    private String switchOn(Map<String, String> ctypei, String text, StringBuilder switchJs) {
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String value = ctypei.get("value");

	StringBuilder sb = new StringBuilder();
	// sb.append("</div><div class=\"layui-form-item\">");
	sb.append("<label class=\"layui-form-label\">" + name + "</label>");
	// sb.append("<div class=\"layui-input-block\">");
	sb.append("	<div class=\"layui-input-inline\">");
	sb.append("<input id=\"" + code + "\" name=\"" + code + "\" type=\"checkbox\" ");
	if (value != null && text.startsWith(value)) {
	    sb.append(" checked=\"true\" ");
	} else {
	    sb.append(" value=\"off\" ");
	}

	sb.append("lay-filter=\"" + code + "\" lay-skin=\"switch\" lay-text=\"" + text + "\">");
	sb.append("</div>");
	if (switchJs != null && switchJs.indexOf("form.on('switch(" + code + ")'") < 0) {
	    switchJs.append("\n form.on('switch(" + code + ")', function(data){");
	    switchJs.append("\n this.value=this.checked==true ? 'on' : 'off'");
	    switchJs.append("\n  });\n");
	}
	return sb.toString();
    }

    private String date(String name, String code, String validator, Boolean disabled) {
	return dateTime(name, code, null, "yyyy-MM-dd", validator, disabled);
    }

    private String dateTime(String name, String code, String validator, Boolean disabled) {
	return dateTime(name, code, null, "yyyy-MM-dd HH:mm:ss", validator, disabled);
    }

    private String date(String name, String code, String value, String validator, Boolean readOnly) {
	return dateTime(name, code, value, "yyyy-MM-dd", validator, readOnly);
    }

    private String dateTime(String name, String code, String value, String validator, Boolean disabled) {
	return dateTime(name, code, value, "yyyy-MM-dd HH:mm:ss", validator, disabled);
    }

    private String dateTime(String name, String code, String value, String formate, String validator,
	    Boolean disabled) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div class=\"layui-inline\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-inline\">");
	sb.append("	<input class=\"layui-input dateInput\" id=\"" + code + "\" name=\"" + code + "\"");
	addValidator(validator, sb);
	if (disabled) {
	    sb.append(" disabled=\"true\"");
	}
	sb.append(" type=\"text\" placeholder=\"" + formate + "\">");
	if (value != null) {
	    sb.append(value);
	}
	sb.append("</input> </div> </div>");
	return sb.toString();
    }

    private void addValidator(String validator, StringBuilder sb) {
	if (StringUtils.isNoneBlank(validator) && !"null".equalsIgnoreCase(validator.trim())) {
	    sb.append(validator);
	}
    }

    /**
     * 单选框
     * @param selectList
     */
    private String poRadio(Map<String, String> ctypei, List<Map<String, Object>> selectList) {
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String value = ctypei.get("value");

	StringBuilder sb = new StringBuilder();
	sb.append("<div class=\"layui-form-item\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-block\">");
	for (Map<String, Object> opti : selectList) {
	    sb.append(" <input name=\"" + code + "\" title=\"" + opti.get(NAME) + "\"");
	    Object object = opti.get(ID);
	    if (value != null && value.equals(object)) {
		sb.append(" checked=\"true\" ");
	    }
	    sb.append(" value=\"" + object + "\"type=\"radio\">");
	}
	sb.append(" </div> </div>");
	return sb.toString();
    }

    /**
     * 复选框
     *
     * @param selectList
     */
    private String poCheckBox(Map<String, String> ctypei, List<Map<String, Object>> selectList) {
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String value = ctypei.get("value");

	StringBuilder sb = new StringBuilder();
	sb.append("<div class=\"layui-form-item\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-block\">");
	for (Map<String, Object> opti : selectList) {
	    sb.append(" <input name=\"" + code + "[" + opti.get(ID) + "]\" title=\"" + opti.get(NAME) + "\" ");
	    Object object = opti.get(ID);
	    if (value != null && value.equals(object)) {
		sb.append(" checked=\"true\" ");
	    }
	    sb.append("type=\"checkbox\">");
	}
	sb.append(" </div> </div>");
	return sb.toString();
    }

    private String poSelect(Map<String, String> ctypei, List<Map<String, Object>> selectList) {
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String value = ctypei.get("value");

	StringBuilder sb = new StringBuilder();
	sb.append("<div class=\"layui-form-item\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-block\">");
	for (Map<String, Object> opti : selectList) {
	    sb.append(" <input name=\"" + code + "[" + opti.get(ID) + "]\" title=\"" + opti.get(NAME) + "\" ");
	    Object object = opti.get(ID);
	    if (value != null && value.equals(object)) {
		sb.append(" checked=\"true\" ");
	    }
	    sb.append("type=\"checkbox\">");
	}
	sb.append(" </div> </div>");
	return sb.toString();
    }

    /**
     * 选择对象列表
     * 
     * @param ctypei
     * @param selectList
     * @return
     */
    private String poSelectType(Map<String, String> ctypei, List<Map<String, Object>> selectList) {
	String name = ctypei.get(NAME);
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-inline \">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-inline\">");
	addSelect(ctypei, sb, selectList);
	sb.append(" </div> </div>");
	return sb.toString();
    }

    private String poSelectIconFont(Map<String, String> ctypei, List<Map<String, Object>> selectList) {
	String name = ctypei.get(NAME);
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-inline \">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-inline\">");
	addSelectIconFont(ctypei, sb, selectList);
	sb.append(" </div> </div>");
	return sb.toString();
    }

    public String addSelect(Map<String, String> ctypei, List<Map<String, Object>> selectList) {
	StringBuilder sb = new StringBuilder();
	addSelect(ctypei, sb, selectList);
	return sb.toString();
    }

    /**
     * 添加select
     *
     * @param sb
     * @param selectList
     */
    private void addSelect(Map<String, String> ctypei, StringBuilder sb, List<Map<String, Object>> selectList) {
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String value = ctypei.get("value");
	String valueField = ctypei.get("valueField");
	sb.append(" <select name=\"" + code + "\" id=\"" + code + "\" ");

	String msgString = "请选择" + name;
	if (selectList.size() > 15) {
	    sb.append(" lay-search=\"\" ");
	    msgString = "直接选择或搜索选择";
	}
	// lay-verify=\"required\"
	sb.append(">");
	sb.append(addSelectOption(selectList, value, msgString, valueField));
	sb.append(" </select>");
    }

    private void addSelectIconFont(Map<String, String> ctypei, StringBuilder sb, List<Map<String, Object>> selectList) {
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String value = ctypei.get("unicode");
	String valueField = ctypei.get("valueField");
	sb.append(" <select name=\"" + code + "\" id=\"" + code + "\" ");

	String msgString = "请选择" + name;
	if (selectList.size() > 15) {
	    sb.append(" lay-search=\"\" ");
	    msgString = "直接选择或搜索选择";
	}
	// lay-verify=\"required\"
	sb.append(">");
	sb.append(addSelectIFOption(selectList, value, msgString, valueField));
	sb.append(" </select>");
    }

    /**
     * 添加选择选项
     *
     * @param selectList
     * @param value
     * @param msgString
     */
    private String addSelectOption(List<Map<String, Object>> selectList, String value, String msgString,
	    String valueField) {
	StringBuilder sb = new StringBuilder();
	sb.append("    <option value=\"\">" + msgString + "</option>");
	for (Map<String, Object> opti : selectList) {
	    sb.append("   <option ");
	    Object oValueobject = "";
	    if (valueField != null) {
		oValueobject = opti.get(valueField);
	    } else {
		oValueobject = opti.get(CODE);
	    }

	    if (value != null && value.equals(oValueobject)) {
		sb.append(" checked=\"true\" ");
	    }
	    sb.append(" value=\"" + oValueobject + "\">" + opti.get(NAME) + "</option>");
	}
	return sb.toString();
    }

    private String addSelectIFOption(List<Map<String, Object>> selectList, String value, String msgString,
	    String valueField) {
	StringBuilder sb = new StringBuilder();
	sb.append("    <option value=\"\">" + msgString + "</option>");
	for (Map<String, Object> opti : selectList) {
	    sb.append("   <option ");
	    Object oValueobject = "";
	    if (valueField != null) {
		oValueobject = opti.get(valueField);
	    } else {
		oValueobject = opti.get(CODE);
	    }

	    if (value != null && value.equals(oValueobject)) {
		sb.append(" checked=\"true\" ");
	    }
	    sb.append(" value=\"" + oValueobject + "\"><i class=\"layui-icon\">" + oValueobject + "</i></option>");
	}
	return sb.toString();
    }

    /**
     * 添加选择选项
     * 
     * @param selectList
     * @param msgString
     * @return
     */
    public String addSelectOption(List<Map<String, Object>> selectList, String msgString, String valueField) {
	return addSelectOption(selectList, null, msgString, valueField);
    }

    public String addSelectOption(List<Map<String, Object>> selectList, String msgString) {
	return addSelectOption(selectList, msgString, null);
    }

    /**
     * 返回文本域
     * 
     * @param name
     * @param code
     */
    private String textArea(String name, String code, String validator, Boolean readOnly) {
	return textArea(name, code, null, validator, readOnly);
    }

    private String manage(String name, String code) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-inline\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");

	sb.append(" <div class=\"layui-input-inline\">");
	sb.append("<input type=\"button\" id=\"" + code
		+ "\" class=\"layui-btn layui-btn-primary \" value=\"查看\" lay-event=\"manage\"></input>");
	sb.append("  </div></div>");
	return sb.toString();
    }

    private String textArea(String name, String code, Object value, String validator, Boolean readOnly) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item  layui-form-text\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-block\">");
	sb.append("       <textarea id=\"" + code + "\" name=\"" + code + "\" class=\"layui-textarea\"  lay-verify=\""
		+ code + "\" onscroll=\"this.rows++;\"");
	addValidator(validator, sb);
	if (readOnly != null && readOnly) {
	    sb.append(" readOnly=true ");
	}
	sb.append("placeholder=\"请输入" + name + "\">");
	if (value != null) {
	    sb.append(String.valueOf(value));
	}
	sb.append(" </textarea>");
	sb.append("  </div></div>");
	return sb.toString();
    }

    private String javaCode(String name, String code) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item  layui-form-text\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-block\">");
	sb.append(" <pre class=\"layui-code\" id=\"" + code + "\" name=\"" + code + "\" >");
	sb.append("</pre>");
	sb.append("  </div></div>");
	return sb.toString();
    }

    private String iconFont(String name, String code) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item  layui-form-text\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + " <i class=\"layui-icon\" id=\"" + code
		+ "Icon\">&#xe60c;</i></label>");
	sb.append("	<div class=\"layui-input-inline\">");
	sb.append("		<input name=\"" + code + "\" class=\"layui-input\" id=\"" + code + "\"");
	sb.append("			placeholder=\"请输入" + name + "\" autocomplete=\"off\" >");
	sb.append("	</div></div>");
	return sb.toString();
    }

    private String password(String name, String code, String validator) {
	StringBuilder sb = new StringBuilder();

	sb.append("<div  class=\"layui-form-item\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-inline\">");
	sb.append("       <input id=\"" + code + "\" name=\"" + code
		+ "\" type=\"password\" class=\"layui-input\" placeholder=\"请输入" + name
		+ "\" lay-verify=\"pass\"></input>");
	sb.append("  </div> <div class=\"layui-form-mid layui-word-aux\">请填写6到12位密码</div></div>");
	return sb.toString();
    }

    private String fileUpload(String name, String code, String validator) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append("""
		<div class="layui-upload">
		<input type="hidden"
		""");
	sb.append("id=\"" + code + "\"");
	addValidator(validator, sb);
	sb.append("""
			>
		<button type="button" class="layui-btn layui-btn-normal"
		""");
	sb.append("id=\"" + code + "Choose\"");
	sb.append("""
		>选择文件</button>
		<button type="button" class="layui-btn"
		""");
	sb.append("id=\"" + code + "UpBtn\"");
	sb.append("""
		  >开始上传</button>
		</div>
			""");

	return sb.toString();
    }

    /**
     * <label class=\"layui-form-label\" th:text=\""+name+"\"></label>: <div
     * class=\"layui-input-inline\"> <input th:name=\""+code+"\"
     * class=\"layui-input\" th:id=\""+code+"\" placeholder=\"请输入"+name+"\"
     * autocomplete=\"off\"> </div>
     * 
     * @param code
     * @param name
     * @return
     */
    public String layFormItem(String code, String name, String validator) {
	return layFormItem(code, name, null, validator);
    }

    public String layFormItem(String code, String name) {
	return layFormItem(code, name, null, null);
    }

    public String layFormLine(String code, String name, Object value) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item  layui-form-text\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append("	<div class=\"layui-input-block\">");
	sb.append("		<input name=\"" + code + "\" class=\"layui-input\" id=\"" + code + "\"");
	sb.append("			placeholder=\"请输入" + name + "\" autocomplete=\"off\" ");
	if (value != null) {
	    sb.append("value=\"" + String.valueOf(value) + "\"");
	}
	sb.append(">	</div></div>");
	return sb.toString();
    }

    public String layFormItem(String code, String name, Object value, String validator) {
	StringBuilder sb = new StringBuilder();
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append("	<div class=\"layui-input-inline\">");
	sb.append("		<input name=\"" + code + "\" class=\"layui-input\" id=\"" + code + "\"");
	addValidator(validator, sb);

	sb.append("			placeholder=\"请输入" + name + "\" autocomplete=\"off\" ");
	if (value != null) {
	    sb.append("value=\"" + String.valueOf(value) + "\"");
	}
	sb.append(">	</div>");
	return sb.toString();
    }

    /**
     * 只读属性
     * 
     * @param code
     * @param name
     * @param value
     * @return
     */
    public String layReadOnlyFormItem(String code, String name, Object value) {
	StringBuilder sb = new StringBuilder();
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append("	<div class=\"layui-input-inline\">");
	sb.append("		<input name=\"" + code + "\" class=\"layui-input\" id=\"" + code + "\"");

	sb.append("		readOnly=true autocomplete=\"off\" ");
	if (value != null) {
	    sb.append("value=\"" + String.valueOf(value) + "\"");
	}
	sb.append(">	</div>");
	return sb.toString();
    }

    /**
     * 添加按钮,列表判断是否有管理按钮
     * 
     * @param model
     * @param label
     * @param tableMap
     */
    public void tableToolBtn(Model model, String label, Map<String, Object> tableMap) {
	String entityString = "match (n:DbTable) -[r]->(e:layTableToolOpt) where id(n)=" + tableMap.get(ID)
		+ " return e";
	List<Map<String, Object>> btnList = neo4jService.cypher(entityString);

	StringBuilder opt = new StringBuilder();

	StringBuilder toolbarOpt = new StringBuilder();
	StringBuilder toolFun = new StringBuilder();
	StringBuilder activLogic = new StringBuilder();
	StringBuilder toolBarActiveLogic = new StringBuilder();

	boolean removeBtn = false;
	for (Map<String, Object> btni : btnList) {
	    if (BTN_REMOVE.equals(btni.get(NODE_CODE))) {
		removeBtn = true;
	    }
	    addBtn(opt, toolFun, activLogic, btni);
	}
	if (!removeBtn) {
	    addOneBtn(BTN_REMOVE, opt, toolFun, activLogic);
	}
	// 是否可管理
	Object object = tableMap.get("isManage");
	if (object != null && "on".equals(object)) {
	    addOneBtn("manageBtn", toolbarOpt, toolFun, toolBarActiveLogic);
	    addOneBtn("fieldBtn", opt, toolFun, activLogic);
	}
	addOneBtn(BTN_TABLE_CREATE, toolbarOpt, toolFun, toolBarActiveLogic);
	addOneBtn("delListBtn", toolbarOpt, toolFun, toolBarActiveLogic);
	addOneBtn("documentBtn", opt, toolFun, activLogic);
	model.addAttribute("opt", opt.toString());
	model.addAttribute(OPT_TOOL_BAR, toolbarOpt.toString());
	model.addAttribute(TOOL_FUN, toolFun.toString());
	model.addAttribute(ACTIVE_LOGIC, activLogic.toString());
	model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());

    }

    /**
     * 详情按钮列表
     * 
     * @param model
     * @param label
     * @param instancesOnly
     */
    public void tableToolBtn(Model model, String label, Boolean instancesOnly) {
	String queryString = "match (n:" + label + ") -[r]->(e:layTableToolOpt)  return e";
	List<Map<String, Object>> btnList = neo4jService.cypher(queryString);
	StringBuilder opt = new StringBuilder();
	StringBuilder toolbarOpt = new StringBuilder();
	StringBuilder toolFun = new StringBuilder();
	StringBuilder activLogic = new StringBuilder();

	for (Map<String, Object> btni : btnList) {
	    opt.append(btni.get("Html"));
	    toolFun.append(btni.get("JavaScript"));
	    activLogic.append(btni.get("btnAcitive"));
	}
	if (!instancesOnly) {
	    addOneBtn(BTN_REMOVE, opt, toolFun, activLogic);
	} else {
	    addOneBtn(BTN_REMOVE_REL, opt, toolFun, activLogic);
	}

	model.addAttribute("opt", opt.toString());
	model.addAttribute(OPT_TOOL_BAR, toolbarOpt.toString());
	model.addAttribute(TOOL_FUN, toolFun.toString());
	model.addAttribute(ACTIVE_LOGIC, activLogic.toString());
    }

    /**
     * 添加一个按钮
     *
     */
    private void addOneBtn(String btnKey, StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic) {
	Map<String, Object> btnMap = neo4jService.getAttMapBy(NODE_CODE, btnKey, LAYUI_TABLE_TOOL_BTN);
	addBtn(opt, toolFun, activLogic, btnMap);
    }

    private void addBtn(StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic,
	    Map<String, Object> manageBtnMap) {
	Object html = manageBtnMap.get("Html");
	if (html != null) {
	    opt.append(html);
	}
	Object javascript = manageBtnMap.get("JavaScript");
	if (javascript != null) {
	    toolFun.append(javascript);
	}
	Object activeLogic = manageBtnMap.get("btnAcitive");
	if (activeLogic != null) {
	    activLogic.append(activeLogic);
	}
    }

}

package com.wldst.ruder.crud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.LemodoApplication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.LayUIDomain;
import com.wldst.ruder.domain.StepDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.StringGet;

@Service
public class FormShowService extends StepDomain {
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private CrudNeo4jDriver driver;
    @Autowired
    private ViewService vService;

    /**
     * table：展现列表和表单，关系tab
     * 
     * @param model
     * @param po
     * @param useTab
     */
    public void showPoMangePage(Model model, Map<String, Object> po, boolean useTab) {
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    // List<String> props = new ArrayList<>();
	    List<Map<String, String>> customType = new ArrayList<>();
	    Set<String> layUseInfo = formFieldTemplate(model, po, columnArray, headers, customType);
	    layUseInfo.add("dropdown");
	    useLayModule(model, useTab, layUseInfo);
	    tableListColumnTemplate(model, columnArray, headers, customType);
	}
    }

    public void columnInfo(Model model, Map<String, Object> po, boolean useTab) {
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    List<Map<String, String>> customType = new ArrayList<>();
	    tableListColumnTemplate(model, columnArray, headers, customType);
	}
    }

    /**
     * 
     * @param model
     * @param po
     */
    public Set<String> editForm(Model model, Map<String, Object> po) {
	Set<String> layUseInfo = new HashSet<>();
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    layUseInfo = formField(model, po, columnArray, headers);
	}
	return layUseInfo;
    }

    /**
     * 
     * @param poLabeli
     * @return
     * @throws DefineException
     */
    public String stepForm(Model model, String poLabeli, Set<String> layUseInfo) throws DefineException {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, poLabeli, META_DATA);
	if (po == null || po.isEmpty()) {
//	    if(poLabeli.equals("Po")) {
//		po = neo4jService.getAttMapBy(LABEL, META_DATA, META_DATA);
//	    }else {
		    throw new DefineException(poLabeli + "未定义！");
//	    }
	}
	layUseInfo.addAll(editForm(model, po));
	return (String) model.getAttribute("formContent");
    }

    /**
     * 实体表单列表
     * 
     * @param label
     * @param vo
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
	useLayModule(model, true, new HashSet<>());
    }

    /**
     * 展现静态表单
     * 
     * @param model
     * @param po
     * @param useTab
     */
    public String staticForm(Map<String, Object> po, Map<String, Object> propMap) {
	if (po.containsKey(HEADER)) {
	    String retColumns = String.valueOf(po.get(COLUMNS));
	    String header = String.valueOf(po.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    // List<String> props = new ArrayList<>();
	    return staticFormField(po, columnArray, headers, propMap);
	}
	return "";
    }

	private Set<String> formListField(Model model, Map<String, Object> po, String[] columnArray, String[] headers) {
		Set<String> layModule = new HashSet<>();
		JSONObject vo = new JSONObject();
		String labelPo = label(po);
		vo.put("poId", labelPo);
		// 查询自定义字段数据
		List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
		List<Map<String, Object>> validateList = objectService.getBy(vo, "FieldValidate");
		Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
		for (Map<String, Object> fi : fieldInfoList) {
			Object object = fi.get("field");
			object = object == null ? fi.get("id") : object;
			customFieldMap.put(String.valueOf(object), fi);
		}
		Map<String, String> fieldHtmlMap = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		sb.append("<div  class=\"layui-form-item\">");
		List<Map<String, String>> customType = new ArrayList<>();
		if (validateList != null && !validateList.isEmpty()) {
			Map<String, Map<String, Object>> fieldValidateMap = new HashMap<>(validateList.size());
			for (Map<String, Object> fi : validateList) {
				Object object = fi.get("field");
				object = object == null ? fi.get("id") : object;
				Object validator = fi.get(COLUMN_VALIDATOR);
				JSONObject query = new JSONObject();
				query.put(CODE, validator);
				String[] columString = { COLUMN_VALIDATOR };
				List<Map<String, Object>> by = objectService.getColumnsBy(query, "InputValidate", columString);
				if (by != null && !by.isEmpty()) {
					fi.putAll(by.get(0));
				}

				fieldValidateMap.put(String.valueOf(object), fi);
			}
			fieldHtmlMap = fieldHandleWithStatus(columnArray, headers, customFieldMap, fieldValidateMap, customType,
					labelPo);
		} else {
			fieldHtmlMap = fieldWithStatusHandle(columnArray, headers, customFieldMap, customType, labelPo);
		}
		isUseDate(columnArray, layModule, customFieldMap);

		fieldHtmlMap.putAll(customFormField(model, layModule, customType, null));

		for (String ci : columnArray) {
			sb.append("<div  class=\"layui-form-item\">");
			sb.append(fieldHtmlMap.get(ci));
			sb.append("</div>");
		}
		appendSubmitResetBtn(sb);
		// 总的表单内容
		model.addAttribute("formContent", sb.toString());
		return layModule;
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
    private Set<String> formField(Model model, Map<String, Object> po, String[] columnArray, String[] headers) {
	Set<String> layModule = new HashSet<>();
	JSONObject vo = new JSONObject();
	String labelPo = label(po);
	vo.put("poId", labelPo);
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
	List<Map<String, Object>> validateList = objectService.getBy(vo, "FieldValidate");
	Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
	for (Map<String, Object> fi : fieldInfoList) {
	    Object object = fi.get("field");
	    object = object == null ? fi.get("id") : object;
	    customFieldMap.put(String.valueOf(object), fi);
	}
	Map<String, String> fieldHtmlMap = new HashMap<>();
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item\">");
	List<Map<String, String>> customType = new ArrayList<>();
	if (validateList != null && !validateList.isEmpty()) {
	    Map<String, Map<String, Object>> fieldValidateMap = new HashMap<>(validateList.size());
	    for (Map<String, Object> fi : validateList) {
		Object object = fi.get("field");
		object = object == null ? fi.get("id") : object;
		Object validator = fi.get(COLUMN_VALIDATOR);
		JSONObject query = new JSONObject();
		query.put(CODE, validator);
		String[] columString = { COLUMN_VALIDATOR };
		List<Map<String, Object>> by = objectService.getColumnsBy(query, "InputValidate", columString);
		if (by != null && !by.isEmpty()) {
		    fi.putAll(by.get(0));
		}

		fieldValidateMap.put(String.valueOf(object), fi);
	    }
	    fieldHtmlMap = fieldHandleWithStatus(columnArray, headers, customFieldMap, fieldValidateMap, customType,
		    labelPo);
	} else {
	    fieldHtmlMap = fieldWithStatusHandle(columnArray, headers, customFieldMap, customType, labelPo);
	}
	isUseDate(columnArray, layModule, customFieldMap);

	fieldHtmlMap.putAll(customFormField(model, layModule, customType, null));

	for (String ci : columnArray) {
	    sb.append("<div  class=\"layui-form-item\">");
	    sb.append(fieldHtmlMap.get(ci));
	    sb.append("</div>");
	}
	appendSubmitResetBtn(sb);
	// 总的表单内容
	model.addAttribute("formContent", sb.toString());
	return layModule;
    }

    private void isUseDate(String[] columnArray, Set<String> boolMap, Map<String, Map<String, Object>> customFieldMap) {
	for (String columni : customFieldMap.keySet()) {
	    Map<String, Object> field = customFieldMap.get(columni);
	    if (field != null) {
		String showType = string(field, show_type);
		if ("date".equals(showType)) {
		    boolMap.add("hasDateField");
		}
	    }
	}
    }

    /**
     * 表单字段展现信息加载
     * 
     * @param model
     * @param po
     * @param columnArray
     * @param headers
     * @param customType
     * @return
     */
    private Set<String> formFieldTemplate(Model model, Map<String, Object> po, String[] columnArray, String[] headers,
	    List<Map<String, String>> customType) {
	Set<String> boolMap = new HashSet<>();
	JSONObject vo = new JSONObject();
	String labelPo = label(po);
	vo.put("poId", labelPo);
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");

	List<Map<String, Object>> validateList = objectService.getBy(vo, "FieldValidate");
	Map<String, Map<String, Object>> mapFieldInfo = mapFieldInfo(fieldInfoList);

	StringBuilder sb = new StringBuilder();

	if (validateList != null && !validateList.isEmpty()) {
	    Map<String, Map<String, Object>> fieldValidateMap = new HashMap<>(validateList.size());
	    for (Map<String, Object> fi : validateList) {
		Object object = fi.get("field");
		object = object == null ? fi.get("id") : object;
		Object validator = fi.get(COLUMN_VALIDATOR);
		JSONObject query = new JSONObject();
		query.put(CODE, validator);
		String[] columString = { COLUMN_VALIDATOR };
		List<Map<String, Object>> by = objectService.getColumnsBy(query, "InputValidate", columString);
		if (by != null && !by.isEmpty()) {
		    fi.putAll(by.get(0));
		}

		fieldValidateMap.put(String.valueOf(object), fi);
	    }
	    fieldHandleWithStatus(columnArray, headers, mapFieldInfo, fieldValidateMap, customType, labelPo);
	} else {
	    fieldWithStatusHandle(columnArray, headers, mapFieldInfo, customType, labelPo);
	}

	isUseDate(columnArray, boolMap, mapFieldInfo);

	Set<String> layUseInfo = showCustomField(model, sb, customType, null);
	boolMap.addAll(layUseInfo);
	sb.append("</div>");
	appendSubmitResetBtn(sb);
	// 总的表单内容
	model.addAttribute("formContent", sb.toString());
	return boolMap;
    }

    /**
     * 获取字段模板信息
     * 
     * @param fieldInfoList
     * @return
     */
    private Map<String, Map<String, Object>> mapFieldInfo(List<Map<String, Object>> fieldInfoList) {
	Map<String, Map<String, Object>> customFieldTempateMap = new HashMap<>(fieldInfoList.size());
	for (Map<String, Object> fi : fieldInfoList) {
	    Object object = fi.get("field");
	    object = object == null ? fi.get("id") : object;
	    String query = "match (n:Field)-[r:template]->(m:LayuiTemplate) where id(n)=" + fi.get("id")
		    + "  return m.templateId,m.content";
	    List<Map<String, Object>> query2 = neo4jService.cypher(query);
	    for (Map<String, Object> templat : query2) {
		fi.put(TABLE_TEMPLATE_ID, templat.get(TABLE_TEMPLATE_ID));
		fi.put(TABLE_TEMPLATE_CONTENT, templat.get("content"));
	    }
	    customFieldTempateMap.put(String.valueOf(object), fi);
	}
	return customFieldTempateMap;
    }

    private void documentField(Model model, Map<String, Object> po, String[] columnArray, String[] headers) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-form-item\">");
	fieldDisableHandle(columnArray, headers, sb);

	sb.append("</div>");
	// 总的表单内容
	model.addAttribute("formContent", sb.toString());
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
	map.put(show_type, "select");
	map.put(NAME, headers[1]);
	map.put(CODE, columnArray[1]);
	map.put(is_po, false);
	customFieldMap.put(key, map);
    }

    /**
     * 静态字段展现
     * 
     * @param model
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
	    customFieldMap.put(String.valueOf(fi.get("id")), fi);
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
		typeiMap.put(is_po, String.valueOf(field.get(is_po)));
		String showType = getShowType(field);

		typeiMap.put(show_type, showType);
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
     * @param model
     * @param sb
     * @param customType
     */
    private void staticCustomFieldHandle(StringBuilder sb, List<Map<String, String>> customType) {
	for (Map<String, String> ctypei : customType) {
	    String isHide = ctypei.get("isHide");
	    if (isHide != null && isHide.equals("on")) {
		continue;
	    }
	    if (Boolean.valueOf(ctypei.get(is_po))) {
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
    private Set<String> showCustomField(Model model, StringBuilder sb, List<Map<String, String>> customType,
	    List<Map<String, Object>> selectOptions) {
	Set<String> fieldUseJsMap = new HashSet<>();
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
	    String fieldHtml = null;
	    String fieldType = ctypei.get("type");
	    String name = ctypei.get(NAME);
	    String code = ctypei.get(CODE);
	    if (code.equals("status")) {
		statusInSearch = true;
	    }
	    String showType = ctypei.get(show_type);

	    String isSearchInput = ctypei.get(is_search_input);

	    String isHide = ctypei.get("isHide");
	    if (isHide != null && isHide.equals("true")) {
		continue;
	    }
	    confirmUseLayInfo(fieldUseJsMap, textEditorJs, codeAttList, code, showType);
	    if (showType.equalsIgnoreCase("textEditor")) {
		formVerify(formVerifyJs, code);
	    }
	    String searchField = "";
	    if (fieldType.equals("poColumns")) {
		if ("select".equalsIgnoreCase(showType)) {
		    fieldHtml = poSelectType(ctypei, selectOptions);
		}
	    } else if (Boolean.valueOf(ctypei.get(is_po))) {
		fieldHtml = handlePoType(ctypei, switchJs);
	    } else {
		fieldHtml = handleShowType(ctypei, js, switchJs);
		searchField = handleSearchShowType(ctypei, js, switchJs);
	    }
	    if (StringUtils.isNotBlank(isSearchInput) && "on".equalsIgnoreCase(isSearchInput)) {
		searchInput(searchValueJs, code);
		if ("status".equals(code)) {
		    searchHtml.append(statusFieldSelect(label, code + "Reload", name));
		} else {
		    if (StringUtils.isBlank(fieldHtml)) {
			fieldHtml = layFormItem(code, name);
			searchHtml.append(layFormItem(code + "Reload", name));
		    } else {
			if (StringUtils.isNotBlank(searchField)) {
			    searchHtml.append(searchField);
			} else {
			    String search = fieldHtml.replaceAll(code, code + "Reload");
			    searchHtml.append(search);
			}
		    }
		}
	    }

	    sb.append(fieldHtml);
	}

	js.append(switchJs.toString());
	js.append("\n});");

	if (!statusInSearch) {
	    searchInput(searchValueJs, "status");
	    String attribute = String.valueOf(model.getAttribute(COLUMNS));
	    if (attribute.indexOf(",status") > 0) {
		statusFieldSelect(label, "statusReload", "状态");
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
	if (textEditorJs.length() > 0) {
	    model.addAttribute("textEditorValue", textEditorJs.toString());
	    model.addAttribute(FORM_VERIFY_JS, formVerifyJs.toString());
	    model.addAttribute(EDIT_INDEX, "var " + EDIT_INDEX + "={};");
	}
	return fieldUseJsMap;
    }

    private void confirmUseDateTime(Set<String> fieldUseJsMap, String showType) {
	if ("datetime".equalsIgnoreCase(showType) || "date".equalsIgnoreCase(showType)
		|| "time".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.add("hasDateField");
	}
    }

    private void confirmUsedFile(Set<String> fieldUseJsMap, String showType) {
	if ("fileUpload".equalsIgnoreCase(showType) || "file".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.add("hasFile");
	}
    }

    private Map<String, String> customFormField(Model model, Set<String> layUseInfo,
	    List<Map<String, String>> customType, List<Map<String, Object>> selectOptions) {
	StringBuilder js = new StringBuilder();
	StringBuilder switchJs = new StringBuilder();
	StringBuilder textEditorJs = new StringBuilder();
	Map<String, String> fieldHtmlMap = new HashMap<>();
	int k = 0;
	Set<String> codeAttList = new HashSet<>();
	String label = String.valueOf(model.getAttribute("label"));
	for (Map<String, String> ctypei : customType) {
	    String fieldType = ctypei.get("type");
	    String name = ctypei.get(NAME);
	    String code = ctypei.get(CODE);
	    String showType = ctypei.get(show_type);

	    String isHide = ctypei.get("isHide");
	    if (isHide != null && isHide.equals("true")) {
		continue;
	    }
	    String fieldHtml = null;
	    confirmUseLayInfo(layUseInfo, textEditorJs, codeAttList, code, showType);

	    if (fieldType.equals("poColumns")) {
		if ("select".equalsIgnoreCase(showType)) {
		    fieldHtml = poSelectType(ctypei, selectOptions);
		}
	    } else if (Boolean.valueOf(ctypei.get(is_po))) {
		fieldHtml = handlePoType(ctypei, switchJs);
	    } else {
		fieldHtml = handleShowType(ctypei, js, switchJs);
	    }
	    if (fieldHtml != null) {
		fieldHtmlMap.put(code, fieldHtml);
	    }
	}

	js.append(switchJs.toString());
	js.append("\n});");

	modelCodeAtt(model, codeAttList);
	model.addAttribute("layField", js.toString());
	if (textEditorJs.length() > 0) {
	    model.addAttribute("textEditorValue", textEditorJs.toString());
	}
	return fieldHtmlMap;
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
    private void confirmUseLayInfo(Set<String> fieldUseJsMap, StringBuilder textEditorJs, Set<String> codeAttList,
	    String code, String showType) {
	confirmUseDateTime(fieldUseJsMap, showType);
	confirmUsedFile(fieldUseJsMap, showType);
	confirmeTextEditor(fieldUseJsMap, textEditorJs, code, showType);
	confirmUseCode(fieldUseJsMap, codeAttList, code, showType);
    }

    private void confirmUseCode(Set<String> fieldUseJsMap, Set<String> codeAttList, String code, String showType) {
	if ("javaCode".equalsIgnoreCase(showType) || "htmlCode".equalsIgnoreCase(showType)
		|| "c++Code".equalsIgnoreCase(showType) || "javaScriptCode".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.add(CODE);
	    codeAttList.add(code);
	}
    }

    private void confirmeTextEditor(Set<String> fieldUseJsMap, StringBuilder textEditorJs, String code,
	    String showType) {
	if ("textEditor".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.add(HAS_TEXT_EDITOR);
	    textEditorJs.append("\n if(data." + code + "!=undefined){" + "$('#" + code + "').val(data." + code + ");"
		    +" layedit.set({uploadImage: {url: '"+ LemodoApplication.MODULE_NAME+"/file/uploadImage',type:'post'success: function(data){ \r\n"
		    + "               console.log(data); \r\n"
		    + "            }}});\n"
		    + " layedit.build('" + code + "'); " + " }\n");
	}
    }

    private void modelCodeAtt(Model model, Set<String> codeAttList) {
	if (!codeAttList.isEmpty()) {
	    StringBuilder codeSb = new StringBuilder();
	    StringBuilder codeInit = new StringBuilder();
	    for (String codei : codeAttList) {
		codeSb.append(" $('#" + codei + "').html(rowi['" + codei + "'])");
		codeInit.append(" $('#" + codei + "').html(data['" + codei + "'])");
	    }
	    model.addAttribute("codeSet", codeSb.toString());
	    model.addAttribute("codeInit", codeInit.toString());
	}
    }

    private String getShowType(Map<String, Object> ctypei) {
	String showType = String.valueOf(ctypei.get(show_type));

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
		Object valueField = customField.get(value_field);
		if (valueField != null && !"null".equals(valueField)) {
		    typeiMap.put(value_field, String.valueOf(valueField));
		}
		typeiMap.put(is_po, String.valueOf(customField.get(is_po)));
		String showType = getShowType(customField);
		typeiMap.put(show_type, showType);
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

    /**
     * field默认字段render，自定义字段信息收集
     * 
     * @param columnArray
     * @param headers
     * @param fiMap
     * @param sb
     * @param customType
     * @return
     */
    private boolean fieldHandle(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
	    StringBuilder sb, List<Map<String, String>> customType) {
	boolean hasDateField = false;
	for (int i = 0, k = 0; i < headers.length; i++) {
	    // 收集自定义字段信息
	    String columni = columnArray[i];
	    Map<String, Object> field = fiMap.get(columni);
	    String headeri = headers[i];

	    if (field != null && !field.isEmpty()) {
		String showType = String.valueOf(field.get(show_type));
		if ("date".equals(showType)) {
		    hasDateField = true;
		}
		addCustomColumn(customType, columni, field, headeri);
	    } else {
		// 默认字段处理
		if (k > 1 && k % 3 == 0) {
		    sb.append("</div><div  class=\"layui-form-item\">");
		}
		sb.append(layFormItem(columni, headeri));

		k++;
	    }
	}
	return hasDateField;
    }

    private Map<String, String> fieldWithStatusHandle(String[] columnArray, String[] headers,
	    Map<String, Map<String, Object>> fiMap, List<Map<String, String>> customType, String poLabel) {
	Map<String, String> fieldHtmlMap = new HashMap<>();
	for (int i = 0; i < headers.length; i++) {
	    String fieldHtml = null;

	    // 收集自定义字段信息
	    String columni = columnArray[i];
	    if ("".equals(columni.trim())) {
		continue;
	    }
	    Map<String, Object> field = fiMap.get(columni);

	    String headeri = headers[i];

	    if (field != null && !field.isEmpty()) {
		String showType = string(field, show_type);

		if (showType != null && !showType.isEmpty()) {
		    addCustomColumn(customType, columni, field, headeri);
		    if (columni.equals("status")) {
			fieldHtml = statusFieldSelect(poLabel, columni, headeri);
		    }
		} else if (!string(field, is_search_input).isEmpty()) {
		    addCustomColumn(customType, columni, field, headeri);
		}
	    } else {
		if (columni.equals("status")) {
		    fieldHtml = statusFieldSelect(poLabel, columni, headeri);
		} else {
		    fieldHtml = layFormItem(columni, headeri);
		}
	    }
	    if (fieldHtml != null) {
		fieldHtmlMap.put(columni, fieldHtml);
	    }
	}
	return fieldHtmlMap;
    }

    private void addCustomColumn(List<Map<String, String>> customType, String columni, Map<String, Object> field,
	    String headeri) {
	String type = String.valueOf(field.get("type"));
	Map<String, String> typeiMap = new HashMap<>();
	typeiMap.put("type", type);
	typeiMap.put(NAME, headeri);
	typeiMap.put(CODE, columni);
	typeiMap.put(is_po, String.valueOf(field.get(is_po)));
	typeiMap.put(value_field, String.valueOf(field.get(value_field)));
	typeiMap.put(COLUMN_VALIDATOR, String.valueOf(field.get(COLUMN_VALIDATOR)));

	String showType = String.valueOf(field.get(show_type));
	Object object = field.get(is_search_input);
	if (null != object) {
	    typeiMap.put(is_search_input, String.valueOf(object));
	}
	switchOnHandle(field, typeiMap);
	copyTemplateInfo(field, typeiMap);
	typeiMap.put(show_type, showType);

	customType.add(typeiMap);
    }

    /**
     * 状态字段用Select，下拉选
     * 
     * @param sb
     * @param poLabel
     * @param columni
     * @param headeri
     */
    private String statusFieldSelect(String poLabel, String columni, String headeri) {
	String query = Neo4jOptCypher.getStatusList(poLabel);
	List<Map<String, Object>> selectList = neo4jService.cypher(query);
	Map<String, String> ctypei = new HashMap<>();
	ctypei.put(NAME, headeri);
	ctypei.put(CODE, columni);
	return poSelectType(ctypei, selectList);
    }

    private boolean fieldDisableHandle(String[] columnArray, String[] headers, StringBuilder sb) {
	boolean hasDateField = false;
	for (int i = 0, k = 0; i < headers.length; i++) {
	    // 默认字段处理
	    if (k > 1 && k % 3 == 0) {
		sb.append("</div><div  class=\"layui-form-item\">");
	    }
	    sb.append(layReadOnlyFormItem(columnArray[i], headers[i], null));
	    k++;
	}
	return hasDateField;
    }

    private boolean fieldHandle1(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
	    List<Map<String, String>> customType) {
	boolean hasDateField = false;
	for (int i = 0, k = 0; i < headers.length; i++) {
	    // 收集自定义字段信息
	    Map<String, Object> field = fiMap.get(columnArray[i]);
	    if (field != null && !field.isEmpty()) {
		String type = String.valueOf(field.get("type"));
		Map<String, String> typeiMap = new HashMap<>();
		typeiMap.put("type", type);
		typeiMap.put(NAME, headers[i]);
		typeiMap.put(CODE, columnArray[i]);
		typeiMap.put(is_po, String.valueOf(field.get(is_po)));
		typeiMap.put(value_field, String.valueOf(field.get(value_field)));
		typeiMap.put(COLUMN_VALIDATOR, String.valueOf(field.get(COLUMN_VALIDATOR)));

		String showType = String.valueOf(field.get(show_type));
		Object object = field.get(is_search_input);
		if (null != object) {
		    typeiMap.put(is_search_input, String.valueOf(object));
		}
		switchOnHandle(field, typeiMap);
		copyTemplateInfo(field, typeiMap);
		typeiMap.put(show_type, showType);
		if ("date".equals(showType)) {
		    hasDateField = true;
		}
		customType.add(typeiMap);
	    }
	}
	return hasDateField;
    }

    private Map<String, String> fieldHandle(String[] columnArray, String[] headers,
	    Map<String, Map<String, Object>> fiMap, Map<String, Map<String, Object>> fivMap, StringBuilder sb,
	    List<Map<String, String>> customType) {
	Map<String, String> fieldHtmlMap = new HashMap<>();
	for (int i = 0, k = 0; i < headers.length; i++) {
	    String fieldHtml = null;
	    // 收集自定义字段信息
	    String columni = columnArray[i];
	    Map<String, Object> field = fiMap.get(columni);
	    Map<String, Object> vfield = fivMap.get(columni);
	    String headeri = headers[i];
	    if (field != null && !field.isEmpty()) {
		addCustomField(customType, columni, field, vfield, headeri);
	    } else {
		fieldHtml = fieldString(columni, vfield, headeri);
	    }
	    if (fieldHtml != null) {
		fieldHtmlMap.put(columni, headeri);
	    }
	}
	return fieldHtmlMap;
    }

    /**
     * 收集自定义字段
     * 
     * @param customType
     * @param columni
     * @param field
     * @param vfield
     * @param headeri
     */
    private void addCustomField(List<Map<String, String>> customType, String columni, Map<String, Object> field,
	    Map<String, Object> vfield, String headeri) {
	String type = String.valueOf(field.get("type"));
	Map<String, String> typeiMap = new HashMap<>();
	typeiMap.put("type", type);
	typeiMap.put(NAME, headeri);
	typeiMap.put(CODE, columni);
	typeiMap.put(is_po, String.valueOf(field.get(is_po)));
	typeiMap.put(value_field, String.valueOf(field.get(value_field)));
	if (vfield != null) {
	    typeiMap.put(COLUMN_VALIDATOR, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
	}
	copyTemplateInfo(field, typeiMap);
	String showType = String.valueOf(field.get(show_type));
	Object object = field.get(is_search_input);
	if (null != object) {
	    typeiMap.put(is_search_input, String.valueOf(object));
	}
	switchOnHandle(field, typeiMap);
	typeiMap.put(show_type, showType);
	customType.add(typeiMap);
    }

    /**
     * 渲染字段，并收集自定义字段信息。对状态字段进行处理。
     * 
     * @param columnArray
     * @param headers
     * @param columMapField
     * @param validFieldMap
     * @param sb
     * @param customType
     * @param poLabel
     * @return
     */
    private Map<String, String> fieldHandleWithStatus(String[] columnArray, String[] headers,
	    Map<String, Map<String, Object>> columMapField, Map<String, Map<String, Object>> validFieldMap,
	    List<Map<String, String>> customType, String poLabel) {
	Map<String, String> fieldHtmlMap = new HashMap<>();
	for (int i = 0; i < headers.length; i++) {
	    String fieldHtml = null;
	    // 收集自定义字段信息
	    String columni = columnArray[i];
	    Map<String, Object> vfield = validFieldMap.get(columni);

	    Map<String, Object> fieldInfo = columMapField.get(columni);
	    String headeri = headers[i];

	    if (fieldInfo != null && !fieldInfo.isEmpty()) {
		Object showTypeobj = fieldInfo.get(show_type);
		if (showTypeobj != null && !"".equals(showTypeobj)) {
		    addCustomFileInfo(customType, columni, fieldInfo, vfield, headeri);
		} else {
		    fieldHtml = fieldHtml(poLabel, columni, vfield, headeri);
		}
	    } else {
		fieldHtml = fieldHtml(poLabel, columni, vfield, headeri);
	    }
	    if (fieldHtml != null) {
		fieldHtmlMap.put(columni, fieldHtml);
	    }
	}
	return fieldHtmlMap;
    }

    private String fieldHtml(String poLabel, String columni, Map<String, Object> vfield, String headeri) {
	if (columni.equals("status")) {
	    return statusFieldSelect(poLabel, columni, headeri);
	} else {
	    return fieldString(columni, vfield, headeri);
	}
    }

    /**
     * 添加自定义字段
     * 
     * @param customType
     * @param columni
     * @param fieldInfo
     * @param vfield
     * @param headeri
     * @param showType
     */
    private void addCustomFileInfo(List<Map<String, String>> customType, String columni, Map<String, Object> fieldInfo,
	    Map<String, Object> vfield, String headeri) {
	String type = String.valueOf(fieldInfo.get("type"));
	Map<String, String> typeiMap = new HashMap<>();
	typeiMap.put("type", type);
	typeiMap.put(NAME, headeri);
	typeiMap.put(CODE, columni);
	typeiMap.put(is_po, String.valueOf(fieldInfo.get(is_po)));
	typeiMap.put(value_field, String.valueOf(fieldInfo.get(value_field)));
	if (vfield != null) {
	    typeiMap.put(COLUMN_VALIDATOR, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
	}
	copyTemplateInfo(fieldInfo, typeiMap);

	Object object = fieldInfo.get(is_search_input);
	if (null != object) {
	    typeiMap.put(is_search_input, String.valueOf(object));
	}
	switchOnHandle(fieldInfo, typeiMap);
	typeiMap.put(show_type, string(fieldInfo, show_type));

	customType.add(typeiMap);
    }

    /**
     * 表单字段
     * 
     * @param sb
     * @param columni
     * @param vfield
     * @param headeri
     */
    private String fieldString(String columni, Map<String, Object> vfield, String headeri) {
	if (vfield != null) {
	    return layFormItem(columni, headeri, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
	} else {
	    return layFormItem(columni, headeri, null);
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

    /**
     * 复制模板信息
     * 
     * @param field
     * @param typeiMap
     */
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
		typeiMap.put(is_po, String.valueOf(field.get(is_po)));
		typeiMap.put(value_field, String.valueOf(field.get(value_field)));
		if (vfield != null) {
		    typeiMap.put(COLUMN_VALIDATOR, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
		}

		String showType = String.valueOf(field.get(show_type));
		Object object = field.get(is_search_input);
		if (null != object) {
		    typeiMap.put(is_search_input, String.valueOf(object));
		}
		switchOnHandle(field, typeiMap);
		copyTemplateInfo(field, typeiMap);
		typeiMap.put(show_type, showType);
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
	    piMap.put("field", columnArray[i]);
	    cols.add(piMap);
	}
	model.addAttribute("cols", cols);
	model.addAttribute("colCodes", columnArray);
    }

    /**
     * 添加tableList的模板字段
     * 
     * @param model
     * @param columnArray
     * @param headers
     */
    private void tableListColumnTemplate(Model model, String[] columnArray, String[] headers,
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
		if (!ei.getKey().equals(unicode)) {
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
	List<Map<String, String>> shortCols = new ArrayList<>();
	String shortColumns = "id code label value name title desc remark status";
	String dont = " desc remark content detail field type size create update modify";

	for (int i = 0, k = 0; i < headers.length; i++) {
	    Map<String, String> piMap = new HashMap<>();
	    String column = columnArray[i];
	    piMap.put(CODE, "{field:'" + column + "', sort: true}");
	    piMap.put(NAME, headers[i]);
	    customWidth(piMap, column);

	    piMap.put("field", column);
	    String string = tempalteInfoMap.get(column);
	    if (string != null && !string.trim().equals("")) {
		piMap.put("templat", ",templet:'#" + string + "'");
	    } else {
		piMap.put("templat", " ");
	    }
	    if (containColumn(column, shortColumns)) {
		shortCols.add(piMap);
	    }
	    cols.add(piMap);
	}
	if (shortCols.size() > 6) {
	    model.addAttribute("cols", shortCols);
	} else {
	    model.addAttribute("cols", cols);
	}
	model.addAttribute("colCodes", columnArray);
    }

    private void customWidth(Map<String, String> piMap, String column) {
	Boolean find = false;
	String title = " code label name title remark Key columns header ";
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

    private Boolean containColumn(String column, String title) {
	Boolean find = false;
	String[] split = title.split(" ");
	for (String key : split) {
	    if ("".equals(key.trim())) {
		continue;
	    }
	    if (column.toLowerCase().contains(key.toLowerCase())) {
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
     * @param hasDateField
     */
    public void useLayModule(Model model, Boolean useTab, Set<String> boolMap) {
	StringBuilder layUse = new StringBuilder();
	layUseJs(layUse, boolMap, useTab);
	model.addAttribute(LAY_USE, layUse.toString());
    }

    /**
     * handle 字段展示类型
     * 
     * @param sb
     * @param js
     * @param switchJs
     * @param name
     * @param code
     * @param showType
     */
    private String handleShowType(Map<String, String> ctypei, StringBuilder js, StringBuilder switchJs) {
	String fieldType = ctypei.get("type");
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String valueField = ctypei.get(value_field);
	String validator = ctypei.get(COLUMN_VALIDATOR);
	String showType = ctypei.get(show_type);
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
	if ("iconPicker".equalsIgnoreCase(showType)) {
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

	String showType = ctypei.get(show_type);
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
	String showType = ctypei.get(show_type);
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
     * @param sb
     * @param switchJs
     * @param fieldType
     * @param name
     * @param code
     * @param showType
     */
    private String handlePoType(Map<String, String> ctypei, StringBuilder switchJs) {
	String fieldType = ctypei.get("type");
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String showType = ctypei.get(show_type);
	String valueField = ctypei.get(value_field);
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
	    fieldString = selectFromWindow(name, code);
	    selectWindowClick(code, "选择" + name, LemodoApplication.MODULE_NAME+"/manage/" + fieldType + "/Po", switchJs);
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
	if (ctypei.containsKey(value_field) && !valueField.trim().equals("") && !valueField.equals("id")
		&& !valueField.equals(CODE)) {
	    String string = "id," + valueField + ",name";
	    columns = string.split(",");
	}

	String query = Neo4jOptCypher.queryObj(null, fieldType, columns);
	List<Map<String, Object>> selectList = neo4jService.cypher(query);
	return selectList;
    }

    private void selectWindowClick(String code, String title, String url, StringBuilder switchJs) {

	switchJs.append("layui.$('#" + code + "').on('click', function(data){\n");
	switchJs.append("""
		layer.open({
		             	      type: 2,
		             	      anim: 0,
		             	      shade: 0,
		             	      maxmin: true,
		    """);
	switchJs.append("     title: '" + title + "',\n");
	switchJs.append("""
		      area: ['100%', '100%'],

		             	      btn:['关闭'],
		             	      yes:function(index,layero)
		             	      {
		             	      var body = layer.getChildFrame('body', index);
		             	      var selected = body.find('#selectObj').val();
		             	      var selectedName = body.find('#selectObjName').val();
		""");
	switchJs.append("      $(\'#" + code + "').val(selected);\n");
	switchJs.append("""
		         			close()
		         	      	          //index为当前层索引
		         	      	          layer.close(index)
		         	      },
		         	      cancel:function(){//右上角关闭毁回调
		         	      	     	close()
		         	      	     	var index = parent.layer.getFrameIndex(data.name);
		         	      	     	parent.layer.close(index);
		         	      },
		         	      zIndex: layer.zIndex //重点1
		         	      ,success: function(layero, index){
		         	      	 layer.setTop(layero); //重点2
		         	         var body = layer.getChildFrame('body', index);
		         	         var objId=body.find('#objId');
		         	         if(objId!=null&&currentNode!=null){
		         			if(currentNode.id!=undefined){
		         			   objId.val(currentNode.id);
		         			}else if(currentNode.code!=undefined){
		         	        	   objId.val(currentNode.code);
		         			}
		         		}
		         	      },
		""");
	switchJs.append("      content: '" + url + "'\n");
	switchJs.append("      	     });\n");
	switchJs.append("      	    });\n");
    }

    /**
     * 弹出层选择对象，获取当前选择对象数据
     * 
     * @param name
     * @param code
     * @param sbbBuilder
     * @param text
     * @param switchJs
     * @param value
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
	String showType = ctypei.get(show_type);

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
     * @param hasDateField
     */
    private void layUseJs(StringBuilder layUse, Set<String> layUseMap, Boolean useTab) {
	Set<String> modules = new HashSet<String>();
	modules.add("form");
	// modules.add("'laytpl'");
	modules.add("table");

	List<String> declares = new ArrayList<>();
	declares.add(" form = layui.form\n");
	declares.add(" ,$ = layui.$\n");
	declares.add(" ,table = layui.table\n");
	// declares.add(" ,laytpl = layui.laytpl\n");

	declares.add(" ,layer = layui.layer\n");

	if (useTab) {
	    useModule(modules, declares, "element");
	}
	if (layUseMap.contains(HAS_TEXT_EDITOR)) {
	    useModule(modules, declares, "layedit");
	}

	if (layUseMap.contains("hasFile")) {
	    useModule(modules, declares, "upload");
	}

	String dropDown = "dropdown";
	useIt(layUseMap, modules, declares, dropDown);
	useIt(layUseMap, modules, declares, CODE);
	useIt(layUseMap, modules, declares, "$");
	useIt(layUseMap, modules, declares, STEP);

	if (layUseMap.contains("hasDateField")) {
	    useModule(modules, declares, "laydate");
	}

	String jsGlobalParam = "\n var layer,crudTable";
	if (!modules.isEmpty()) {
	    jsGlobalParam = jsGlobalParam + "," + String.join(",", modules);
	}
	jsGlobalParam = jsGlobalParam + ";";
	String join = "'"+String.join("','", modules)+"'";
	layUse.append(jsGlobalParam + "\n " + layuiConfig + ".use([" + join + "], function(){\n"
		+ String.join(" ", declares));
	if (modules.contains(dropDown)) {
	    layUse.append(",dropDown=dropdown\n");
	}
    }

    private void useIt(Set<String> layUseMap, Set<String> modules, List<String> declares, String key) {
	if (layUseMap.contains(key)) {
	    useModule(modules, declares, key);
	}
    }

    private void useModule(Set<String> modules, List<String> declares, String module) {
	modules.add(module);
	declares.add(" ," + module + " = layui." + module + "\n");
    }

    /**
     * 开关
     * 
     * @param name
     * @param code
     * @param sbbBuilder
     */
    private String poSwitchOn(Map<String, String> ctypei, List<Map<String, Object>> selectList,
	    StringBuilder switchJs) {
	List<String> switchText = new ArrayList<>(2);
	for (Map<String, Object> opti : selectList) {
	    // switchText.add(opti.get("id"));
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
     * 
     * @param name
     * @param code
     * @param sb
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
	    Object object = opti.get("id");
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
     * @param name
     * @param code
     * @param sb
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
	    sb.append(" <input name=\"" + code + "[" + opti.get("id") + "]\" title=\"" + opti.get(NAME) + "\" ");
	    Object object = opti.get("id");
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
	    sb.append(" <input name=\"" + code + "[" + opti.get("id") + "]\" title=\"" + opti.get(NAME) + "\" ");
	    Object object = opti.get("id");
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

    public String addSelect(Map<String, String> ctypei, List<Map<String, Object>> selectList) {
	StringBuilder sb = new StringBuilder();
	addSelect(ctypei, sb, selectList);
	return sb.toString();
    }

    /**
     * 添加select
     * 
     * @param name
     * @param code
     * @param sb
     * @param selectList
     * @param value
     */
    private void addSelect(Map<String, String> ctypei, StringBuilder sb, List<Map<String, Object>> selectList) {
	String name = ctypei.get(NAME);
	String code = ctypei.get(CODE);
	String value = ctypei.get("value");
	String valueField = ctypei.get(value_field);
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
	String value = ctypei.get(unicode);
	String valueField = ctypei.get(value_field);
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
     * @param sb
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
     * @param sbbBuilder
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
		+ "\" class=\"layui-btn layui-btn-primary \" value=\"管理\" lay-event=\"manage\"></input>");
	sb.append("  </div></div>");
	return sb.toString();
    }

    private String selectFromWindow(String name, String code) {
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-inline\">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");

	sb.append(" <div class=\"layui-input-inline\">");
	sb.append("<input type=\"button\" id=\"" + code
		+ "\" class=\"layui-btn layui-btn-primary \" value=\"选择\" lay-event=\"selectWindow\"></input>");
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
	// <button type="button" class="layui-btn" id="
	// """);
	//
	// sb.append(code+"\" ");
	//
	// sb.append("""
	// ><i class="layui-icon"></i>上传文件</button>
	// <input class="layui-upload-file" type="file" accept name="file">
	// """);
	// sb.append(" </div>");

	return sb.toString();
    }

    /**
     * layui 表单字段 <label class=\"layui-form-label\" th:text=\""+name+"\"></label>:
     * <div class=\"layui-input-inline\"> <input th:name=\""+code+"\"
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
     * 添加按钮,列表判断是否有管理按钮 更多操作：
     * <table class="layui-table">
     * <tbody>
     * <tr>
     * <td>列表 1</td>
     * <td><button class="layui-btn layui-btn-sm demolist" data-id=
     * "111">更多操作</button></td>
     * </tr>
     * </tbody>
     * </table>
     * <script> layui.use('dropdown', function(){ var dropdown = layui.dropdown ,$ =
     * layui.jquery;
     * 
     * dropdown.render({ elem: '.demolist' ,data: [{ title: 'item 1' ,id: 'aaa' }]
     * ,click: function(data, othis){ var elem = $(this.elem) ,listId =
     * elem.data('id'); //表格列表的预埋数据 layer.msg('得到表格列表的 id：'+ listId +'，下拉菜单 id：'+
     * data.id); } }); }); </script>
     * 
     * @param model
     * @param label
     * @param po
     */
    public List<Map<String, Object>> tableToolBtn(Model model, String label, Map<String, Object> po) {
	List<Map<String, Object>> btnList = listBtn(po);
	List<Map<String, Object>> tableHeadBtnList = listToolBarBtn(po);
	StringBuilder opt = new StringBuilder();
	StringBuilder toolbarOpt = new StringBuilder();

	StringBuilder toolFun = new StringBuilder();
	StringBuilder dropDownFun = new StringBuilder();
	StringBuilder dropDownItems = new StringBuilder();
	StringBuilder activLogic = new StringBuilder();
	StringBuilder toolBarActiveLogic = new StringBuilder();

	boolean removeBtn = false;
	boolean documentBtn = false;

	if (btnList.size() > 0) {
	    for (Map<String, Object> btni : btnList) {
		if ("removeBtn".equals(btni.get(NODE_CODE))) {
		    removeBtn = true;
		}
		if ("documentBtn".equals(btni.get(NODE_CODE))) {
		    documentBtn = true;
		}
		addBtn(opt, toolFun, activLogic, btni);
	    }

	    if (tableHeadBtnList.size() > 0) {
		btnList.removeAll(tableHeadBtnList);
		for (Map<String, Object> thBtni : tableHeadBtnList) {
		   addBtn(toolbarOpt, toolFun, toolBarActiveLogic, thBtni);
		}
	    }

	    if (!removeBtn) {
		addOneDropDown(dropDownItems, "removeBtn");
		addOneBtn("removeBtn", opt, toolFun, activLogic);
	    }
	    if (!documentBtn) {
		addOneDropDown(dropDownItems, "documentBtn");
		addOneBtn("documentBtn", opt, toolFun, activLogic);
	    }
	    dropDownItem(btnList, dropDownItems);
	} else {
	    addDefaultDropDown(dropDownItems);
	    addDefaultBtn(opt, toolFun, activLogic);
	}

	// 是否可管理
	addManageBtn(po, opt, toolbarOpt, toolFun, activLogic, toolBarActiveLogic);
	
	addDefaultTableHeadBtn(toolbarOpt, toolFun, toolBarActiveLogic);
	if (dropDownItems.isEmpty()) {
	    model.addAttribute("opt", opt.toString());
	} else {
	    model.addAttribute("dropDownFun", dropDownFun.toString());
	    model.addAttribute("dropDwonItem", "[\n"+dropDownItems.toString()+"]");
	}
	model.addAttribute("toolbarOpt", toolbarOpt.toString());
	model.addAttribute("toolFun", toolFun.toString());
	model.addAttribute("activLogic", activLogic.toString());

	model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());
	return btnList;

    }

    private void addDefaultBtn(StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic) {
	addOneBtn("documentBtn", opt, toolFun, activLogic);
	addOneBtn("removeBtn", opt, toolFun, activLogic);
    }

    private void addDefaultDropDown(StringBuilder dropDownItems) {
	addOneDropDown(dropDownItems, "documentBtn");
	addOneDropDown(dropDownItems, "removeBtn");
    }

    private void addManageBtn(Map<String, Object> po, StringBuilder opt, StringBuilder toolbarOpt,
	    StringBuilder toolFun, StringBuilder activLogic, StringBuilder toolBarActiveLogic) {
	Object object = po.get("isManage");
	if (object != null && "on".equals(object)) {
	    addOneBtn("manageBtn", toolbarOpt, toolFun, toolBarActiveLogic);
	    addOneBtn("fieldBtn", opt, toolFun, activLogic);
	}
    }

    private void addDefaultTableHeadBtn(StringBuilder toolbarOpt, StringBuilder toolFun, 
	    StringBuilder toolBarActiveLogic) {
	addOneBtn("createBtn", toolbarOpt, toolFun, toolBarActiveLogic);
	addOneBtn("delListBtn", toolbarOpt, toolFun, toolBarActiveLogic);
    }

    /*
    public List<Map<String, Object>> tableToolBtn(Model model, String label, Map<String, Object> po) {
    List<Map<String, Object>> btnList = listBtn(po);
    
    StringBuilder opt = new StringBuilder();
    StringBuilder toolbarOpt = new StringBuilder();
    
    StringBuilder toolFun = new StringBuilder();
    StringBuilder dropDownFun = new StringBuilder();
    StringBuilder dropDownItems = new StringBuilder();
    StringBuilder activLogic = new StringBuilder();
    StringBuilder toolBarActiveLogic = new StringBuilder();
    
    boolean removeBtn = false;
    
    if(btnList.size()>0) {
        addOneBtn("documentBtn", opt, toolFun, activLogic);
        for (Map<String, Object> btni : btnList) {
    	    if ("removeBtn".equals(btni.get(NODE_CODE))) {
    		removeBtn = true;
    	    }
    //		    addBtn(opt, toolFun, activLogic, btni);
         }
        if (!removeBtn) {
    	    addOneBtn("removeBtn", opt, toolFun, activLogic);
        }
    //	    dropDownMoreOpt(label, btnList, opt, dropDownFun);
    	
        
    }else {
        addOneBtn("documentBtn", opt, toolFun, activLogic);
        addOneBtn("removeBtn", opt, toolFun, activLogic);
    }
    
    
    
    // 是否可管理
    Object object = po.get("isManage");
    if (object != null && "on".equals(object)) {
        addOneBtn("manageBtn", toolbarOpt, toolFun, toolBarActiveLogic);
        addOneBtn("fieldBtn", opt, toolFun, activLogic);
    }
    addOneBtn("createBtn", toolbarOpt, toolFun, toolBarActiveLogic);
    addOneBtn("delListBtn", toolbarOpt, toolFun, toolBarActiveLogic);
    
    model.addAttribute("opt", opt.toString());
    model.addAttribute("toolbarOpt", toolbarOpt.toString());
    model.addAttribute("toolFun", toolFun.toString());
    model.addAttribute("dropDownFun", dropDownFun.toString());
    model.addAttribute("activLogic", activLogic.toString());
    model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());
    return btnList;
    
    }*/
    /**
     * [{layIcon: 'layui-icon-edit', txt: '修改用户名', event:'edit'}]
     * 
     * @param label
     * @param btnList
     * @param opt
     * @param dropDownItems
     */
    private void dropDownItem(List<Map<String, Object>> btnList, StringBuilder dropDownItems) {
	for (Map<String, Object> btni : btnList) {
	    addOneDropDown(dropDownItems, btni);
	}
    }

    private void addOneDropDown(StringBuilder dropDownItems, Map<String, Object> btni) {
	String name = name(btni).replaceAll("按钮", "");
	dropDownItems.append("[{txt: '" + name + "',event: '" + code(btni) + "'}]\n");
    }

    private void addOneDropDown(StringBuilder dropDownItems, String key) {
	Map<String, Object> btnMap = neo4jService.getAttMapBy(NODE_CODE, key, LAYUI_TABLE_TOOL_BTN);
	addOneDropDown(dropDownItems, btnMap);
    }

    private void dropDownMoreOpt(String label, List<Map<String, Object>> btnList, StringBuilder opt,
	    StringBuilder dropDownFun) {
	opt.append(
		"<button class=\"layui-btn layui-btn-sm moreOptlist\" data-id=\"" + label + "moreOpt\">更多操作</button>");
	dropDownFun.append(
		" dropdown.sute({\r\n" + "		    elem: '.moreOptlist'\r\n" + "		    ,data: [");
	int bi = 0;
	for (Map<String, Object> btni : btnList) {
	    if (bi > 0) {
		dropDownFun.append(",");
	    }
	    dropDownFun.append("{title: '" + name(btni) + "',id: '" + code(btni) + "'}");
	    bi++;
	}

	dropDownFun.append("]\n,click: function(data, othis){\n");
	dropDownFun.append("   var elem = $(this.elem)");
	dropDownFun.append("  ,listId = elem.data('id'); //表格列表的预埋数据\n");
	// dropDownFun.append(" layer.msg('得到表格列表的 id：'+ listId +'，下拉菜单 id：'+
	// data.id)\n");
	for (Map<String, Object> btni : btnList) {
	    String btnCodei = code(btni);
	    dropDownFun.append("   if(data.id=='" + btnCodei + "'){" + btnCodei + "(currentNode);}");
	}

	dropDownFun.append(" }\r\n" + "});\n");
    }

    private List<Map<String, Object>> listBtn(Map<String, Object> po) {
	String entityString = "match (n:"+META_DATA+") -[r]->(e:layTableToolOpt) where id(n)=" + po.get("id") + " return e";
	List<Map<String, Object>> btnList = neo4jService.cypher(entityString);
	return btnList;
    }

    private List<Map<String, Object>> listToolBarBtn(Map<String, Object> po) {
	String entityString = "match (n:"+META_DATA+") -[r:ToolBarBtn]->(e:layTableToolOpt) where id(n)=" + po.get("id")
		+ " return e";
	List<Map<String, Object>> btnList = neo4jService.cypher(entityString);
	return btnList;
    }

    private Integer countBtn(Map<String, Object> po) {
	String entityString = "match (n:"+META_DATA+") -[r]->(e:layTableToolOpt) where id(n)=" + po.get("id")
		+ " return count(e) as countBtn";
	List<Map<String, Object>> btnList = neo4jService.cypher(entityString);

	Object object = btnList.get(0).get("countBtn");
	return Integer.valueOf(String.valueOf(object));
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
	    addOneBtn("removeBtn", opt, toolFun, activLogic);
	} else {
	    addOneBtn("removeRelBtn", opt, toolFun, activLogic);
	}

	model.addAttribute("opt", opt.toString());
	model.addAttribute("toolbarOpt", toolbarOpt.toString());
	model.addAttribute("toolFun", toolFun.toString());
	model.addAttribute("activLogic", activLogic.toString());
    }

    /**
     * 添加一个按钮
     * 
     * @param model
     */
    private void addOneBtn(String btnKey, StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic) {
	Map<String, Object> btnMap = neo4jService.getAttMapBy(NODE_CODE, btnKey, LAYUI_TABLE_TOOL_BTN);
	addBtn(opt, toolFun, activLogic, btnMap);
    }

    private void addBtn(StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic,
	    Map<String, Object> btnMap) {
	Object html = btnMap.get("Html");
	if (html != null) {
	    opt.append(html);
	}
	Object javascript = btnMap.get("JavaScript");
	if (javascript != null) {
	    toolFun.append("\n" + javascript);
	}
	Object activeLogic = btnMap.get("btnAcitive");
	if (activeLogic != null) {
	    activLogic.append("\n" + activeLogic);
	}
    }

}

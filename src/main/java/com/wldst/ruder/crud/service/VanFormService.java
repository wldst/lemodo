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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.LayUIDomain;
import com.wldst.ruder.domain.VantUIDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.StringGet;

@Service
public class VanFormService extends VantUIDomain {
    public static final Logger log = LogManager.getLogger(VanFormService.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private CrudNeo4jDriver driver;
    @Autowired
    private ViewService vService;
    @Autowired
    private CrudUtil crudUtil;

    private Map<String, List<Map<String, Object>>> cacheMap = new HashMap<>();

    /**
     * [ {category: "流程信息", items: [{ "type": "checkbox", //表单项类型 "key":
     * "configType",//表单项的id、name "label": "配置项类型 ",//表单项的 name "class": "", //样式
     * "initDataMode": "none",//none,function,request //初始值获取方式none无,function
     * 执行js函数,request 使用ajax请求 
     * "initValueData": [{"key": 1, "label": "代码"} ,
     * .. {"key": 9,"label": "其他"}], 
     * //与initDataMode配合使用， function时为js脚本，ajax请求时为地址、参数信息
     * "value": [],//表单项的值 "showVal": "", "disabled": false, "required": false,
     * "validate": null, "options": {}//其他附加项。如添加事件处理等 } 获取VanVueForm
     * 
     * @param metaData
     */
    public Map<String, Object> getVanFrom(Map<String, Object> metaData) {
	if (metaData.containsKey(HEADER)) {
	    String labelPo = label(metaData);
	    String name = name(metaData);
	    String retColumns = String.valueOf(metaData.get(COLUMNS));
	    String header = String.valueOf(metaData.get(HEADER));
	    String[] columnArray = retColumns.split(",");
	    String[] headers = StringGet.split(header);
	    Map<String, Object> vanFrom = new HashMap<>();
	    vanFrom.put("category", name);

	    JSONObject vo = new JSONObject();
	    vo.put("poId", labelPo);
	    // 查询自定义字段数据
	    List<Map<String, Object>> fieldInfo = objectService.getBy(vo, "Field");

	    List<Map<String, Object>> validateList = objectService.getBy(vo, "FieldValidate");

	    Map<String, Map<String, Object>> customFieldMap = mapCustomFieldInfo(fieldInfo);
	    List<Map<String, Object>> vantFieldList =  vantFieldList(columnArray, headers, customFieldMap,  labelPo);
	    Map<String, Map<String, Object>> list2KeyMap = list2KeyMap(vantFieldList, KEY);
	    if (validateList != null && !validateList.isEmpty()) {
		Map<String, Map<String, Object>> fieldValidator = new HashMap<>(validateList.size());
		for (Map<String, Object> fi : validateList) {
		    Object object = fi.get("field");
		    object = object == null ? fi.get(ID) : object;
		    Object validator = fi.get(COLUMN_VALIDATOR);
		    JSONObject queryParam = new JSONObject();
		    queryParam.put(CODE, validator);
		    String[] columString = { COLUMN_VALIDATOR };
		    List<Map<String, Object>> by = objectService.getColumnsBy(queryParam, "InputValidate", columString);
		    if (by != null && !by.isEmpty()) {
			fi.putAll(by.get(0));
		    }
		    fieldValidator.put(String.valueOf(object), fi);
		}
		fieldInitList(list2KeyMap,customFieldMap, fieldValidator,  labelPo);
	    }
	    vanFrom.put("items", vantFieldList);
	    return vanFrom;
	}
	return null;
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
     * 查询自定义字段数据
     * 
     * @param labelPo
     * @return
     */
    public List<Map<String, Object>> getField(String labelPo) {
	List<Map<String, Object>> list = cacheMap.get(labelPo);
	if (list != null && !list.isEmpty()) {
	    return list;
	}
	JSONObject vo = new JSONObject();
	vo.put("poId", labelPo);
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
	cacheMap.put(labelPo, fieldInfoList);
	return fieldInfoList;
    }


    /**
     * 获取字段模板信息
     * 
     * @param fieldInfoList
     * @return
     */
    private Map<String, Map<String, Object>> mapCustomFieldInfo(List<Map<String, Object>> fieldInfoList) {
	Map<String, Map<String, Object>> customFieldTempateMap = new HashMap<>(fieldInfoList.size());
	for (Map<String, Object> fi : fieldInfoList) {
	    Object fieldKey = fi.get("field");
	    fieldKey = fieldKey == null ? fi.get("id") : fieldKey;

	    String query = "match (n:Field)-[r:template]->(m:LayuiTemplate) where id(n)=" + fi.get("id")
		    + "  return m.templateId,m.content";
	    List<Map<String, Object>> query2 = neo4jService.cypher(query);
	    for (Map<String, Object> templat : query2) {
		fi.put(TABLE_TEMPLATE_ID, templat.get(TABLE_TEMPLATE_ID));
		fi.put(TABLE_TEMPLATE_CONTENT, templat.get("content"));
	    }
	    customFieldTempateMap.put(String.valueOf(fieldKey), fi);
	}
	return customFieldTempateMap;
    }
    
    /**
     * 添加点击响应方法
     * 
     * @param btni
     */
    private void btnOnclick(Map<String, Object> btni) {
	String string = stringIgnoreCase(btni, HTML);
	String code = code(btni);
	int indexOf = string.indexOf(" lay-event");
	String head = string.substring(0, indexOf);
	head += " onclick=\"" + code + "()\" ";
	String tail = string.substring(indexOf);
	btni.put(HTML, head + tail);
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

    public void selectField(String[] columnArray, String[] headers, Map<String, Map<String, Object>> customFieldMap,
	    String key) {
	Map<String, Object> map = new HashMap<>();
	map.put("type", "poColumns");
	map.put(show_type, "select");
	map.put(NAME, "字段");
	map.put(CODE, FIELD);
	map.put(is_po, false);
	customFieldMap.put(key, map);
    }


    private void confirmUseDateTime(Set<String> fieldUseJsMap, String showType) {
	if ("datetime".equalsIgnoreCase(showType) || "date".equalsIgnoreCase(showType)
		|| "time".equalsIgnoreCase(showType)) {
	    fieldUseJsMap.add("laydate");
	}
    }

    private void confirmUseColorPicker(Set<String> fieldUseJsMap, String showType) {
	if ("colorPicker".equalsIgnoreCase(showType) || showType.startsWith("colorPicker")) {
	    fieldUseJsMap.add("colorpicker");
	}
    }

    private void comfirmUse(Set<String> fieldUseJsMap, String showType, String layPlugin) {
	if (layPlugin.equalsIgnoreCase(showType) || showType.startsWith(layPlugin)) {
	    fieldUseJsMap.add(layPlugin);
	}
    }

    private void confirmUsedFile(Set<String> fieldUseJsMap, String showType) {
	String lowerCase = showType.toLowerCase();
	if ("fileUpload".equalsIgnoreCase(showType) || lowerCase.contains("image") || lowerCase.contains("file")
		|| lowerCase.contains("upload")) {
	    fieldUseJsMap.add("upload");
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
    private void confirmUseLayInfo(Set<String> fieldUseJsMap, StringBuilder textEditorJs, Set<String> codeAttList,
	    String code, String showType) {
	confirmUseDateTime(fieldUseJsMap, showType);
	confirmUseColorPicker(fieldUseJsMap, showType);
	comfirmUse(fieldUseJsMap, showType, "slider");
	comfirmUse(fieldUseJsMap, showType, "rate");
	comfirmUse(fieldUseJsMap, showType, "iconPicker");
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
	    fieldUseJsMap.add("layedit");
	    textEditorJs.append("\n if(data." + code + "!=undefined){" + "$('#" + code + "').val(data." + code + ");"
		    +" layedit.set({uploadImage: {url: '"+ LemodoApplication.MODULE_NAME+"/file/uploadImage',type:'post',success: function(data){ \r\n"
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

    
   
    private List<Map<String, Object>> vantFieldList(String[] columnArray, String[] headers,
	    Map<String, Map<String, Object>> customFieldMap, String poLabel) {
	List<Map<String, Object>> vantFields = new ArrayList<>();
	for (int i = 0; i < columnArray.length; i++) {
	    // 收集自定义字段信息
	    String coli = columnArray[i];
	    if ("".equals(coli.trim())) {
		continue;
	    }
	    String headeri = headers[i];
	    Map<String, Object> vanFieldi = initItemi(coli, headeri);
	    
	    Map<String, Object> customField = customFieldMap.get(coli);
	    
	    if (customField != null && !customField.isEmpty()) {
		String showType = string(customField, show_type);		
		if (showType != null && !showType.isEmpty()) {
		    vanFieldi.put("type", showType);
		    if (coli.equals("status")) {
			statusFieldSelect(vanFieldi,poLabel);
		    }else {
			customVantField(vanFieldi, coli, customField, headeri);
		    }
		} else if (!string(customField, is_search_input).isEmpty()) {
		    customVantField(vanFieldi, coli, customField, headeri);
		}
	    } else {
		if (coli.equals("status")) {
		    statusFieldSelect(vanFieldi,poLabel);
		}
	    }
	    vantFields.add(vanFieldi);
	}
	return vantFields;
    }
    
    /**
     * { "type": "checkbox", //表单项类型 "key":
     * "configType",//表单项的id、name 
     * "label": "配置项类型 ",//表单项的 name 
     * "class": "", //样式
     * "initDataMode": "none",//none,function,request //初始值获取方式none无,function
     * 执行js函数,request 使用ajax请求 
     * "initValueData": [{"key": 1, "label": "代码"} ,
     * .. {"key": 9,"label": "其他"}], 
     * //与initDataMode配合使用， function时为js脚本，ajax请求时为地址、参数信息
     * "value": [],//表单项的值 
     * "showVal": "", 
     * "disabled": false, 
     * "required": false,
     * "validate": null, 
     * "options": {}//其他附加项。如添加事件处理等 } 获取VanVueForm
     * @param columni
     * @param headeri
     * @return
     */
    private Map<String, Object> initItemi(String columni, String headeri) {
	Map<String, Object> itemi = new HashMap<>();
	itemi.put(KEY, columni);
	itemi.put(LABEL, headeri);
	itemi.put("type", "text");
	itemi.put("initDataMode", "none");
	itemi.put("initValueData", "none");
	itemi.put("showVal", "");
	itemi.put("disabled", false);
	itemi.put("required", false);
	itemi.put("validate", null);
	itemi.put("options", "{}");
	itemi.put("value", "");
	return itemi;
    }
    /**
     * 字段处理
     * @param itemi
     * @param columni
     * @param field
     * @param headeri
     */
    private void customVantField(Map<String, Object> itemi, String columni, Map<String, Object> field,
	    String headeri) {
	itemi.put(is_po, String.valueOf(field.get(is_po)));
	itemi.put(value_field, String.valueOf(field.get(value_field)));
	String label = string(field,"type");
	String showType = string(field, show_type);
	if(showType==null) {
	    return;
	}
	Set<String> inList = Set.of("select","checkbox","radio","switch");
	
	if(!"".equals(label)) {
	    if(inList.contains(showType.toLowerCase())) {
		List<Map<String, Object>> listAllByLabel = neo4jService.listAllByLabel(label);
		    if(listAllByLabel!=null) {
			 List<Map<String, Object>> dataMap =new ArrayList<>(listAllByLabel.size()); 
			    for(Map<String, Object> di: listAllByLabel) {
				Map<String, Object> kv = new HashMap<>();
				kv.put("key",di.get(ID));
				kv.put("label",di.get(NAME));
				dataMap.add(kv);
			    }
			    itemi.put("initValueData", dataMap);
		    }else {
			log.error("listAllByLabel is null ,lableis {}",label);
		    }
	    }else {
		//其他的都为request
		
		itemi.put("initDataMode", "request");
	    }
	    
	}
	Object object = field.get(is_search_input);
	if (null != object) {
	    itemi.put(is_search_input, String.valueOf(object));
	}
    }

    /**
     * 状态字段用Select，下拉选
     *
     * @param poLabel
     */
    private void statusFieldSelect(Map<String, Object> itemi,String poLabel) {
	String query = Neo4jOptCypher.getStatusList(poLabel);
	List<Map<String, Object>> selectList = neo4jService.cypher(query);
	poSelectType(itemi,selectList);
    }

    private List<Map<String, Object>> fieldInitList(Map<String, Map<String, Object>> list2KeyMap, 
	    Map<String, Map<String, Object>> customFieldMap,
	    Map<String, Map<String, Object>> validatorMap, String poLabel) {
	List<Map<String, Object>> items = new ArrayList<>();
	for (String keyi:list2KeyMap.keySet()) {
	    Map<String, Object> itemi =  list2KeyMap.get(keyi);
	    String columni = string(itemi,KEY);
	    String headeri = string(itemi,LABEL);
	    Map<String, Object> vfield = validatorMap.get(columni);
	    Map<String, Object> fieldInfo = customFieldMap.get(columni);
	    
	    if (fieldInfo != null && !fieldInfo.isEmpty()) {
		Object showTypeobj = fieldInfo.get(show_type);
		if (showTypeobj != null && !"".equals(showTypeobj)) {
		    addCustomFileInfo(itemi, fieldInfo, vfield);
		} else {
		    fieldHtml(itemi,poLabel, vfield);
		}
	    } else {
		fieldHtml(itemi,poLabel, vfield);
	    }
	    items.add(itemi);
	}
	return items;
    }

    private void fieldHtml(Map<String, Object> itemi,String poLabel,  Map<String, Object> vfield) {
	if (string(itemi,KEY).equals("status")) {
	    statusFieldSelect(itemi,poLabel);
	} else {
	    fieldString(itemi,vfield);
	}
    }

    /**
     * 添加自定义字段
     *
     * @param fieldInfo
     * @param vfield
     */
    private void addCustomFileInfo(Map<String, Object> itemi, Map<String, Object> fieldInfo,
	    Map<String, Object> vfield) {
	String type = String.valueOf(fieldInfo.get("type"));
	itemi.put(is_po, String.valueOf(fieldInfo.get(is_po)));
	itemi.put(value_field, String.valueOf(fieldInfo.get(value_field)));
	if (vfield != null) {
	    itemi.put(COLUMN_VALIDATOR, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
	}

	Object object = fieldInfo.get(is_search_input);
	if (null != object) {
	    itemi.put(is_search_input, String.valueOf(object));
	}
	switchOnHandle(fieldInfo, itemi);
	itemi.put(show_type, string(fieldInfo, show_type));

    }

    /**
     * 表单字段
     *
     * @param vfield
     */
    private void fieldString(Map<String, Object> itemi, Map<String, Object> vfield) {
	if (vfield != null) {
	   itemi.put("validate",string(vfield, COLUMN_VALIDATOR));
	} 
    }

    private void switchOnHandle(Map<String, Object> field, Map<String, Object> item) {
	handleBooleanColumn(field, item, "isHide");
	handleBooleanColumn(field, item, "disabled");
	handleBooleanColumn(field, item, "readOnly");
    }

    private void handleBooleanColumn(Map<String, Object> field, Map<String, Object> typeiMap, String columnKey) {
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

    /**
     * table 列定义
     * 
     * @param model
     * @param columnArray
     * @param headers
     */
    private void tableInfo(Model model, String[] columnArray, String[] headers) {
	List<Map<String, String>> cols = new ArrayList<>();
	for (int i = 0; i < headers.length; i++) {
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


    private String updateIcon(Map<String, Object> attMapBy, String html) {
	String icon = string(attMapBy, ICON);
	if (icon != null && !"".equals(icon.trim()) && html.indexOf("${icon}") > 0) {
	    html = html.replaceAll("\\$\\{icon\\}", icon);
	}
	return html;
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


    private void addValidator(String validator, StringBuilder sb) {
	if (StringUtils.isNoneBlank(validator) && !"null".equalsIgnoreCase(validator.trim())) {
	    sb.append(validator);
	}
    }

    /**
     * 单选框
     *
     * @param selectList
     */
    private String poRadio(Map<String, Object> ctypei, List<Map<String, Object>> selectList) {
	String name = string(ctypei,NAME);
	String code = string(ctypei,CODE);
	String value = string(ctypei,"value");

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
     * @param selectList
     */
    private String poCheckBox(Map<String, Object> ctypei, List<Map<String, Object>> selectList) {
	String name = string(ctypei,NAME);
	String code = string(ctypei,CODE);
	String value = string(ctypei,"value");

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

    private String poSelect(Map<String, Object> ctypei, List<Map<String, Object>> selectList) {
	String name = string(ctypei,NAME);
	String code = string(ctypei,CODE);
	String value = string(ctypei,"value");

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
    private void poSelectType(Map<String, Object> ctypei, List<Map<String, Object>> selectList) {
	addSelect(ctypei,  selectList);
    }

    private String poSelectIconFont(Map<String, Object> ctypei, List<Map<String, Object>> selectList) {
	String name = string(ctypei,NAME);
	StringBuilder sb = new StringBuilder();
	sb.append("<div  class=\"layui-inline \">");
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append(" <div class=\"layui-input-inline\">");
	addSelectIconFont(ctypei, sb, selectList);
	sb.append(" </div> </div>");
	return sb.toString();
    }

    /**
     * 添加select
     *
     * @param selectList
     * @return 
     */
    private void addSelect(Map<String, Object> ctypei, List<Map<String, Object>> selectList) {
	String name = string(ctypei,NAME);
	String valueField = string(ctypei,value_field);
	
	String msgString = "请选择" + name;
	if (selectList.size() > 15) {
	    msgString = "直接选择或搜索选择";
	}
	ctypei.put("showVal",msgString);
	ctypei.put("initValueData",addSelectOption(selectList, msgString, valueField));
    }

    private void addSelectIconFont(Map<String, Object> ctypei, StringBuilder sb, List<Map<String, Object>> selectList) {
	String name = string(ctypei,NAME);
	String code = string(ctypei,CODE);
	String value = string(ctypei,unicode);
	String valueField = string(ctypei,value_field);
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
    private List<Map<String,String>> addSelectOption(List<Map<String, Object>> selectList, String value, String msgString,
	    String valueField) {
	List<Map<String,String>> options = new ArrayList<>(selectList.size());
	for (Map<String, Object> opti : selectList) {
	    Object oValueobject = "";
	    if (valueField != null) {
		oValueobject = opti.get(valueField);
	    } else {
		oValueobject = opti.get(CODE);
	    }
	    Map<String, Object> option =new HashMap<>();
	    option.put(KEY,oValueobject);
	    option.put(LABEL,string(opti,NAME));
	}
	return options;
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
    public List<Map<String,String>> addSelectOption(List<Map<String, Object>> selectList, String msgString, String valueField) {
	return addSelectOption(selectList, null, msgString, valueField);
    }

    public List<Map<String,String>> addSelectOption(List<Map<String, Object>> selectList, String msgString) {
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

    private String iconSelected(String name, String code) {
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
	List<Map<String, Object>> btnList = listMetaBtn(po);
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

    public List<Map<String, Object>> pageBtn(Model model, String label, Map<String, Object> po) {
	List<Map<String, Object>> btnList = listBtn(po);

	StringBuilder btnHtml = new StringBuilder();

	StringBuilder jsFun = new StringBuilder();

	if (btnList.size() > 0) {
	    for (Map<String, Object> btni : btnList) {
		addPageBtnBykey(string(btni, CODE), btnHtml, jsFun);
	    }
	}

	model.addAttribute("jsFun", jsFun.toString());
	model.addAttribute("btnHtml", btnHtml.toString());
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
     * @param btnList
     * @param dropDownItems
     */
    private void dropDownItem(List<Map<String, Object>> btnList, StringBuilder dropDownItems) {
	for (Map<String, Object> btni : btnList) {
	    addOneDropDown(dropDownItems, btni);
	}
    }

    private void addOneDropDown(StringBuilder dropDownItems, Map<String, Object> btni) {
	String name = string(btni, NAME).replaceAll("按钮", "");
	String iconString = string(btni, ICON);
	if (iconString != null) {
	    name += "',layIcon:'" + iconString;
	}

	dropDownItems.append("[{txt: '" + name + "',event: '" + code(btni) + "'}]\n");
    }

    private void addOneDropDown(StringBuilder dropDownItems, String key) {
	Map<String, Object> btnMap = neo4jService.getAttMapBy(NODE_CODE, key, LayUIDomain.LAYUI_TABLE_TOOL_BTN);
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
	    dropDownFun.append("{title: '" + btni.get(NAME) + "',id: '" + code(btni) + "'}");
	    bi++;
	}

	dropDownFun.append("]\n,click: function(data, othis){\n");
	dropDownFun.append("   var elem = $(this.elem)");
	dropDownFun.append("  ,listId = elem.data('id'); //表格列表的预埋数据\n");
	// dropDownFun.append(" layer.msg('得到表格列表的 id：'+ listId +'，下拉菜单 id：'+
	// data.id)\n");
	for (Map<String, Object> btni : btnList) {
	    dropDownFun.append("   if(data.id=='" + code(btni) + "'){" + code(btni) + "(currentNode);}");
	}

	dropDownFun.append(" }\r\n" + "});\n");
    }

    private List<Map<String, Object>> listMetaBtn(Map<String, Object> po) {
	Long poId = id(po);
	return listMetaBtnById(poId);
    }

    private List<Map<String, Object>> listMetaBtnById(Long metaId) {
	String entityString = "match (n:" + META_DATA + ") -[r]->(e:layTableToolOpt) where id(n)=" + metaId
		+ " return e";
	List<Map<String, Object>> btnList = neo4jService.cypher(entityString);
	return btnList;
    }

    private List<Map<String, Object>> listBtn(Map<String, Object> po) {
	String entityString = "match (n)-[r]->(e:layTableToolOpt) where id(n)=" + po.get("id") + " return e";
	List<Map<String, Object>> btnList = neo4jService.cypher(entityString);
	return btnList;
    }

    private List<Map<String, Object>> listToolBarBtn(Map<String, Object> po) {
	String entityString = "match (n:" + META_DATA + ") -[r:ToolBarBtn]->(e:layTableToolOpt) where id(n)="
		+ po.get("id") + " return e";
	List<Map<String, Object>> btnList = neo4jService.cypher(entityString);
	return btnList;
    }

    private List<Map<String, Object>> listFormBtn(Map<String, Object> po) {
	Long poId = id(po);
	return listFormBtnById(poId);
    }

    private List<Map<String, Object>> listFormBtnById(Long poId) {
	String entityString = "match (n:" + META_DATA + ") -[r:FormBtn]->(e:layTableToolOpt) where id(n)=" + poId
		+ " return e";
	List<Map<String, Object>> btnList = neo4jService.cypher(entityString);
	return btnList;
    }

    private Integer countBtn(Map<String, Object> po) {
	String entityString = "match (n:" + META_DATA + ") -[r]->(e:layTableToolOpt) where id(n)=" + po.get("id")
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
	    handleHtml(opt, btni);
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
     */
    private void addOneBtn(String btnKey, StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic) {
	Map<String, Object> btnMap = neo4jService.getAttMapBy(NODE_CODE, btnKey, LayUIDomain.LAYUI_TABLE_TOOL_BTN);
	addBtn(opt, toolFun, activLogic, btnMap);
    }

    private void addPageBtnBykey(String btnKey, StringBuilder btnHtml, StringBuilder jsFun) {
	Map<String, Object> btnMap = neo4jService.getAttMapBy(NODE_CODE, btnKey, LayUIDomain.LAYUI_TABLE_TOOL_BTN);
	addPageBtn(btnHtml, jsFun, btnMap);
    }

    private void addPageBtn(StringBuilder opt, StringBuilder toolFun, Map<String, Object> btnMap) {
	handleHtml(opt, btnMap);
	Object javascript = btnMap.get("JavaScript");
	if (javascript != null) {
	    String string = code(btnMap);
	    String[] funBody = string.split("(){ \n");
	    String funHead = funBody[0].trim();
	    if (funHead.startsWith("function ")) {
		String[] function = funHead.split(" ");
		if (toolFun.length() > 1) {
		    toolFun.append(",");
		}
		toolFun.append(function[1]);
		toolFun.append(":function(){\n" + funBody[1]);
	    }
	}
    }

    private void addBtn(StringBuilder html, StringBuilder toolFun, StringBuilder activLogic,
	    Map<String, Object> btnMap) {
	handleHtml(html, btnMap);
	appendContent(toolFun, btnMap, "JavaScript");
	appendContent(activLogic, btnMap, "btnAcitive");
    }

    private void appendContent(StringBuilder toolFun, Map<String, Object> btnMap, String key) {
	String javascript = string(btnMap, key);
	if (javascript != null) {
	    toolFun.append("\n" + javascript);
	}
    }

    private void handleHtml(StringBuilder opt, Map<String, Object> btnMap) {
	String html = string(btnMap, "Html");
	if (html != null) {
	    html = updateIcon(btnMap, html);
	    opt.append(html);
	}
    }

}

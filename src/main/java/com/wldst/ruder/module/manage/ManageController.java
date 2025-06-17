package com.wldst.ruder.module.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.*;
import com.wldst.ruder.domain.ConfigDomain;
import com.wldst.ruder.module.manage.service.ConfigService;
import com.wldst.ruder.util.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.domain.FileDomain;
import com.wldst.ruder.domain.SystemDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * po管理，页面控制器 Created by on 2018/5/28.
 */
@Controller
@RequestMapping("${server.context}/manage")
public class ManageController extends SystemDomain {
    private static Logger logger = LoggerFactory.getLogger(ManageController.class);
    @Autowired
    private CrudNeo4jService neo4jService;
	@Autowired
	private CrudUserNeo4jService userDataService;
    @Autowired
    private HtmlShowService showService;
	@Autowired
	private RelationService relationService;
    @Autowired
    private VanFormService vanFormService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private RelationService cypherService;
//    @Autowired
//    private StaticTemplate st;
	@Autowired
	private ConfigService configService;

    /**
     * 关系节点
     * 
     * @param model
     * @param poLabel
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/tranRelField/{po}", method = { RequestMethod.GET, RequestMethod.POST })
    public String tranRelField(Model model, @PathVariable("po") String poLabel, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> poMap = null;
	try {
	    Long valueOf = Long.valueOf(poLabel);
	    // 无码数据转换
	    poMap = neo4jService.getNodeMapById(valueOf);
	    poLabel = label(poMap);
	} catch (Exception e) {
	    poMap = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	}

	String[] codes = columns(poMap);
	String[] names = headers(poMap);
	List<Map<String, Object>> columnMapList = new ArrayList<>();

	if (codes != null && names != null) {
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
	    field(model, poMap, columnMapList);
	}
	ModelUtil.setKeyValue(model, poMap);
	return "layui/tranRelField";
    }  

    @RequestMapping(value = "/path", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> path(HttpServletResponse response, Model model,
	    HttpServletRequest request) throws Exception {
	String pathCode = neo4jService.getPathBy(FileDomain.FILE_STORE_PATH);
	
	return Result.success(pathCode);
    }

    @RequestMapping(value = "/setting/{key}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> setting(HttpServletResponse response,@PathVariable("key") String key, Model model,
	    HttpServletRequest request) throws Exception {
		String keyValue =  neo4jService.getBySysCode(key);
		return Result.success(keyValue);
    }

	@RequestMapping(value = "/{po}/setting", method = { RequestMethod.GET })
	public String editSetting(Model model, @PathVariable("po") String label,HttpServletRequest request) throws Exception {
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if (po == null || po.isEmpty()) {
			try {
				po = neo4jService.getPropLabelByNodeId(Long.valueOf(label));
				if (po == null) {
					throw new DefineException(label + "未定义！");
				}
			} catch (Exception e) {
				throw new DefineException(label + "未定义！");
			}
		}
		ModelUtil.setKeyValue(model, po);
		showService.configForm(model, po, true);
		return "layui/config";
	}


	@RequestMapping(value = "/{po}/configItem", method = { RequestMethod.GET })
	@ResponseBody
	public Result configItem(Model model, @PathVariable("po") String label,HttpServletRequest request) throws Exception {
		//先去检查配置中有无此配置
		Map<String, Object> configMap = configService.getConfigMap(label);
		return Result.success(configMap);
	}



	@RequestMapping(value = "/{po}/saveSetting", method = { RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result saveSetting(Model model, @PathVariable("po") String label,@RequestBody JSONObject vo,HttpServletRequest request) throws Exception {
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if (po == null || po.isEmpty()) {
			try {
				po = neo4jService.getPropMapBy(label);
				if (po == null) {
					throw new DefineException(label + "未定义！");
				}
			} catch (Exception e) {
				throw new DefineException(label + "未定义！");
			}
		}

		List<Map<String, Object>> config= new ArrayList<>();
		//id,name,value,code,comment
		List<Map<String, Object>> settings=neo4jService.cypher("MATCH(n)-[r:configItem]->(s:Settings) where id(n)="+id(vo)+" return s");
		List<Map<String, Object>> configItems=neo4jService.cypher("MATCH(n)-[r:configItem]->(c:ConfigItem) where id(n)="+id(vo)+" return c");
		if(configItems!=null&&!configItems.isEmpty()){
			config.addAll(configItems);
		}
		if(settings!=null&&!settings.isEmpty()){
			config.addAll(settings);
		}

		for(String ki : vo.keySet()){
			for(Map<String, Object> si : config){
				String value=value(si);
				if(code(si).equals(ki)&&value!=null&&!value.equals(string(vo,ki).trim())||id(si).equals(ki)){
					si.put("value", vo.get(ki));
					neo4jService.update(si, id(si));
				}
			}
		}
		return WrappedResult.successMsg("保存成功");
	}
    
    @RequestMapping(value = "/refreshSetting/{key}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> refreshSetting(HttpServletResponse response,@PathVariable("key") String key, Model model,
	    HttpServletRequest request) throws Exception {
	String value = neo4jService.refreshSetting(key);
	return Result.success(value);
    }

	/**
	 * 清理重复数据
	 * @param response
	 * @param model
	 * @param request
	 * @return
	 * @throws Exception
	 */
    @RequestMapping(value = "/clearDuplicate", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> clearDuplicate(HttpServletResponse response, Model model,
	    HttpServletRequest request) throws Exception {
	List<Map<String, Object>> listAllByLabel = neo4jService.listAllByLabel(META_DATA);
	for(Map<String, Object> mi: listAllByLabel) {
	    String label2 = label(mi);
	    if(label2!=null){
//		List<Map<String, Object>> data = neo4jService.listAllByLabel(label2);
		clearBiggerSame(label2, NAME);
		clearBiggerSame(label2, CODE);
	    }
	}
	return Result.success();
    }

    public void clearBiggerSame(String label2, String key2) {
	String query="Match(n:"+label2+") return distinct n."+key2+" as "+key2;
	List<Map<String, Object>> distincts = neo4jService.cypher(query);
	for(Map<String, Object> ni: distincts) {
	    
	    String querySameName="Match(n:"+label2+") where n."+key2+"=\""+ string(ni,key2)+"\" return id(n)";
		List<Map<String, Object>> sames = neo4jService.cypher(querySameName);
		if(sames!=null&&sames.size()>1) {
		    Long[] nums=new Long[sames.size()];
			int i=0;
			for (Map<String, Object> ei : sames) {
			    nums[i] = id(ei);
			    i++;
			}
			Arrays.sort(nums); // 对数组进行排序
			long minNum = nums[0]; // 获取最小值
			
			for(int k=1;k<nums.length;k++) {
			    //级联删除
			    neo4jService.deleteCascade(nums[k]);
			}
		}
		
	}
    }
    
    
    
    
    @RequestMapping(value = "/relation", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult relation(@RequestBody JSONObject vo) {
//	Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label(vo), META_DATA);
//	if (md == null || md.isEmpty()) {
//	    return ResultWrapper.wrapResult(true, null, null, label(vo) + "元数据缺失！");
//	}
//	Set<String> columns2 = columnSet(md);
	String rel = vo.getString("rel");
	 // 获取关系属性
	    Map<String, Object> relProps = mapObject(vo, "relProp");
	    
	    try {
	    Long endId = longValue(vo, "endId") ;
	    Long startId = longValue(vo, "startId");
	    LoggerTool.info(logger,"===relProps==" + mapString(relProps) + "=\n=rel="+rel+"====startId============" + startId + "======endId==" + endId);
	     
		if (relProps != null && !relProps.isEmpty()) {
		    cypherService.addRel(rel, startId, endId, relProps);
		}
	    } catch (Exception e) {
		LoggerTool.error(logger,e.getMessage(), e);
	    }

	return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }
    
    @RequestMapping(value = "/delRelation", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult delRelation(@RequestBody JSONObject vo) {
	String rel = vo.getString("rel");
	// 获取关系属性
	Map<String, Object> relProps = mapObject(vo, "relProp");

	try {
	    Long endId = longValue(vo, "endId");
	    Long startId = longValue(vo, "startId");
	    LoggerTool.info(logger,"==delRelation=relProps==" + mapString(relProps) + "=\n=rel=" + rel + "====startId============"
		    + startId + "======endId==" + endId);
	    if (relProps != null && !relProps.isEmpty()) {
		relationService.delRelation(rel, startId, endId, relProps);
	    }
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	}

	return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }
    
    @RequestMapping(value = "/delBy", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult delById(@RequestBody JSONObject vo) {
	 Long id2 = id(vo);
	 if(id2==null) {
	     return ResultWrapper.wrapResult(false, null, null, "ID not null");
	 }
	try {
	    neo4jService.delete(id2);
	} catch (Exception e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	}

	return ResultWrapper.wrapResult(true, null, null, DELETE_SUCCESS);
    }
    
    @RequestMapping(value = "/query", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult query(@RequestBody JSONObject vo) throws DefineException {
		PageObject page = crudUtil.validatePage(vo);

		String query = Neo4jOptCypher.queryByProps(vo, page);
		List<Map<String, Object>> dataList = neo4jService.cypher(query);
		/*
		 * for(Map<String, Object> datai:dataList) { Object nameObj = datai.get("name");
		 * if(nameObj!=null&&!StringUtils.isEmpty(String.valueOf(nameObj))) {
		 * datai.put("name",
		 * "<a href=\"javascript:;\" onClick=\"openWindow('"+LemodoApplication.MODULE_NAME+"/layui/"+label+
		 * "/document','"+nameObj+"')\">"+nameObj+"</a>"); // datai.put("name",
		 * "<a href=\"javascript:;\" onClick=\"test()\">"+nameObj+"</a>"); } }
		 */
		page.setTotal(crudUtil.total(query,vo));
		return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }
    /**
     * 选择和管理窗口，需要新增一个选择窗口。只有选择功能。
     * 
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}", method = { RequestMethod.GET, RequestMethod.POST })
    public String metaCrud(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception {
	label = NodeLabelUtil.firstValidate(label);
	Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (metaData == null || metaData.isEmpty()) {
	    LoggerTool.info(logger,label + "未定义！");
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, metaData);
	/*
	 * JSONObject vo = new JSONObject(); vo.put("domainId", value);
	 * doService.cypher(vo, label);
	 */
	showService.showMetaInstanceCrudPage(model, metaData, true);

	// List<Map<String, Object>> oneOutRelation = neo4jService.getOneOutRelation(vo,
	// label, "");
	// tableToolBtn(model, label,po);
//	showService.tableToolBtn(model, label, metaData);
	
	showService.d2tableToolBtn(model, label, metaData);
	/*
	 * if ("Action".equals(label)) { model.addAttribute("opt",
	 * "<a class=\"layui-btn layui-btn-primary layui-btn-xs\" lay-event=\"opt\">操作</a>"
	 * ); }
	 */
	/*
	 * if ("RouteDefinition".equals(label)) { Map<String, Object> btnMap =
	 * neo4jService.getAttMapBy("id", "routeUpdateBtn", "layTableToolOpt");
	 * model.addAttribute("opt", btnMap.get("Html")); model.addAttribute("toolFun",
	 * btnMap.get("JavaScript")); model.addAttribute("activLogic",
	 * btnMap.get("btnAcitive")); }
	 */
	// st.createHtml("instanceCustom", model.asMap());
	return "instanceCustom";
    }

    @RequestMapping(value = "/atreeb/{treeLabel}/{tableDataLabel}", method = { RequestMethod.GET, RequestMethod.POST })
    public String aTreeB(Model model, @PathVariable("treeLabel") String treeLabel,
	    @PathVariable("tableDataLabel") String tableDataLabel, HttpServletRequest request) throws Exception {
	Map<String, Object> startPo = neo4jService.getAttMapBy(LABEL, treeLabel, META_DATA);

	if (startPo == null || startPo.isEmpty()) {
	    startPo = neo4jService.getNodeMapById(treeLabel);
	    if (startPo == null || startPo.isEmpty()) {
	    throw new DefineException(treeLabel + "未定义！");
	    }
	}

	Map<String, Object> endPo = neo4jService.getAttMapBy(LABEL, tableDataLabel, META_DATA);

	if (endPo == null || endPo.isEmpty()) {
	    throw new DefineException(tableDataLabel + "未定义！");
	}

	model.addAttribute(treeLabel, startPo);
	model.addAttribute("treeLabel", treeLabel);

	String setting = "{callback: {\n onClick: childList\n }}";
	model.addAttribute("setting", setting);
	Map<String, Object> tree = neo4jService.getWholeTree(treeLabel);
	JSONArray zNodesList = new JSONArray();
	zNodesList.add(tree);
	model.addAttribute("zNodes", zNodesList);

	endPo = neo4jService.getAttMapBy(LABEL, tableDataLabel, META_DATA);
	ModelUtil.setKeyValue(model, endPo);
	tabList(model, endPo);
	showService.tableToolBtn(model, endPo);
	return "aTreeB";
    }

    @RequestMapping(value = "/abTree/{treeLabel}/{tabelDataLabel}", method = { RequestMethod.GET, RequestMethod.POST })
    public String aBTree(Model model, @PathVariable("treeLabel") String treeLabel,
	    @PathVariable("tableDataLabel") String tableDataLabel, HttpServletRequest request) throws Exception {
	Map<String, Object> startPo = neo4jService.getAttMapBy(LABEL, treeLabel, META_DATA);

	if (startPo == null || startPo.isEmpty()) {
	    throw new DefineException(treeLabel + "未定义！");
	}

	Map<String, Object> endPo = neo4jService.getAttMapBy(LABEL, tableDataLabel, META_DATA);

	if (endPo == null || endPo.isEmpty()) {
	    throw new DefineException(tableDataLabel + "未定义！");
	}

	model.addAttribute(treeLabel, startPo);
	model.addAttribute("treeLabel", treeLabel);

	String setting = "{callback: {\n onClick: childList\n }}";
	model.addAttribute("setting", setting);
	Map<String, Object> tree = neo4jService.getWholeTree(treeLabel);
	JSONArray zNodesList = new JSONArray();
	zNodesList.add(tree);
	model.addAttribute("zNodes", zNodesList);

	endPo = neo4jService.getAttMapBy(LABEL, tableDataLabel, META_DATA);
	ModelUtil.setKeyValue(model, endPo);
	tabList(model, endPo);
	showService.tableToolBtn(model,  endPo);
	return "aBTree";
    }

    @RequestMapping(value = "/abTable/{naviLabel}/{tabelLabel}", method = { RequestMethod.GET, RequestMethod.POST })
    public String abTable(Model model, @PathVariable("naviLabel") String naviLabel,
	    @PathVariable("tabelLabel") String tabelLabel, HttpServletRequest request) throws Exception {
	Map<String, Object> naviPo = neo4jService.getAttMapOf(naviLabel);

	if (naviPo == null || naviPo.isEmpty()) {
	    throw new DefineException(naviLabel + "未定义！");
	}

	Map<String, Object> bTablePo = neo4jService.getAttMapOf(tabelLabel);

	if (bTablePo == null || bTablePo.isEmpty()) {
	    throw new DefineException(tabelLabel + "未定义！");
	}

	model.addAttribute(naviLabel, naviPo);
	model.addAttribute("aLabel", naviLabel);

	String setting = "{callback: {\n onClick: childList\n }}";
	model.addAttribute("setting", setting);
	Map<String, Object> tree = neo4jService.getWholeTree(naviLabel);
	JSONArray zNodesList = new JSONArray();
	zNodesList.add(tree);
	model.addAttribute("zNodes", zNodesList);

	bTablePo = neo4jService.getAttMapOf(tabelLabel);
	ModelUtil.setKeyValue(model, bTablePo);
	tabList(model, bTablePo);
	showService.tableToolBtn(model, bTablePo);
	return "abTable";
    }

    /**
     * 关系节点
     * 
     * @param model
     * @param endLabel
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/PoObject/{poObjectId}/{endLabel}", method = { RequestMethod.GET, RequestMethod.POST })
    public String poInstanceRelations(Model model, @PathVariable("poObjectId") String poObjectId,
	    @PathVariable("endLabel") String endLabel, HttpServletRequest request) throws Exception {

	Map<String, Object> endPo = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);

	if (!"tree".equals(endLabel)) {
	    if (endPo == null || endPo.isEmpty()) {
		throw new DefineException(endLabel + "未定义！");
	    }
	}

	ModelUtil.setKeyValue(model, endPo);

	if (endLabel.equals("Field") || endLabel.equals("FieldValidate") || endLabel.equals("SpiderField")) {
	    Map<String, Object> poMap = neo4jService.getAttMapBy("id", poObjectId, VO);

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
		    fieldValidate(model, endPo, columnMapList, false);
		} else {
		    field(model, endPo, columnMapList);
		}
	    } else {
		table2(model, endPo);
	    }
//	    st.createHtml("layui/poObjectField", model.asMap());
	    return "layui/poObjectField";
	}

	table2(model, endPo);
//	st.createHtml("poSelect", model.asMap());
	return "poSelect";
    }

    @RequestMapping(value = "/{po}/tree", method = { RequestMethod.GET, RequestMethod.POST })
    public String tree(Model model, @PathVariable("po") String poLabel, HttpServletRequest request) throws Exception {

	Map<String, Object> md = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	ModelUtil.setKeyValue(model, md);
	tabList(model, md);
	String setting = "{callback: {\n onClick: childList\n }}";
	model.addAttribute("setting", setting);
	Map<String, Object> treeDefine = neo4jService.getOne("Match(t:TreeDefine) where t.mdLabel='" + label(md)
		+ "' return t.parentIdField AS parentIdField,t.code AS code");
	Set<Long> treeNodeIdSet = new HashSet<>();
	if (treeDefine != null) {
	    Map<String, Object> tree = neo4jService.getTreeByDefine(poLabel, treeDefine, columns(md));
		if(tree==null){
			treeDefine = new HashMap<>();
			treeDefine.put("parentField", "parentId");
			treeDefine.put("code", poLabel);
			treeDefine.put("name", name(md));
			JSONArray zNodesList = new JSONArray();
			tree = neo4jService.getTreeByDefine(poLabel, treeDefine, columns(md));
		}
	    JSONArray zNodesList = new JSONArray();
		if(tree!=null){
			if (name(tree) == null) {
				tree.put(NAME, name(md));
			}
			childData2(poLabel, md, treeDefine, tree,treeNodeIdSet);
			zNodesList.add(tree);
		}

	    
	    model.addAttribute("zNodes", zNodesList);
	    model.addAttribute("parentField", string(treeDefine, "parentField"));
	} else {
	    treeDefine = new HashMap<>();
	    treeDefine.put("parentField", "parentId");
	    treeDefine.put("code", poLabel);
	    treeDefine.put("name", name(md));
		JSONArray zNodesList = new JSONArray();
	    Map<String, Object> tree = neo4jService.getTreeByDefine(poLabel, treeDefine, columns(md));
		if(tree==null){
			tree=new HashMap<>();
		}

	    if (name(tree) == null) {
		tree.put(NAME, name(md));
	    }
	    tree.put("isParent", true);
	    tree.put("open", true);
	    
	    
	    childData(poLabel, tree,treeNodeIdSet);
	    zNodesList.add(tree);
	   
	    model.addAttribute("zNodes", zNodesList);
	    model.addAttribute("parentField", string(treeDefine, "parentField"));
	}

	showService.showMetaInstanceCrudPage(model, md, true);
	showService.tableToolBtn(model, md);
	return "instanceTree";
    }

    public void childData2(String poLabel, Map<String, Object> md, Map<String, Object> treeDefine,
	    Map<String, Object> tree,Set<Long> treeNodeIdSet) {
	
	
		Map<String, Object> param = new HashMap<>();
		String parantField = string(treeDefine, "parentIdField");
		param.put(parantField, id(tree));

		if(!treeNodeIdSet.contains(id(tree))) {
			treeNodeIdSet.add(id(tree));
		 }
		List<Map<String, Object>> chidList = neo4jService.chidrenlList(poLabel, param, parantField);
		// tree.put("child", chidList);
		if (chidList != null && !chidList.isEmpty()) {
			tree.put("isParent", true);
			tree.put("open", true);
			for (Map<String, Object> mi : chidList) {
			if(!treeNodeIdSet.contains(id(mi))) {
				childData2(poLabel, md, treeDefine, mi,treeNodeIdSet);
				}

			mi.remove(URL);
			}
			tree.put(REL_TYPE_CHILDREN, chidList);
		}
	
	
    }

    public void childData(String poLabel, Map<String, Object> tree,Set<Long> treeNodeIdSet) {
	List<Map<String, Object>> chidList = null;
	 
	if (tree.get(REL_TYPE_CHILDREN) == null) {
	    Map<String, Object> param = new HashMap<>();
	    param.put("parentId", id(tree));
	    chidList = neo4jService.chidrenlList(poLabel, param, "parentId");
	}else {
	    chidList=listMapObject(tree, REL_TYPE_CHILDREN);
	}
	if(!treeNodeIdSet.contains(id(tree))) {
	    treeNodeIdSet.add(id(tree));
	 }
	for (Map<String, Object> mi : chidList) {
	    if(!treeNodeIdSet.contains(id(mi))) {
		childData(poLabel, mi,treeNodeIdSet);
	    }else {
		
	    }
	    mi.remove(URL);
	}
	tree.put(REL_TYPE_CHILDREN, chidList);
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
	if(endPo!=null) {
	    ModelUtil.setKeyValue(model, endPo);
	}
	

	Map<String, Object> md = null;
	try {
	    Long valueOf = Long.valueOf(poLabel);
	    // 无码数据转换
	    md = neo4jService.getNodeMapById(valueOf);
		if(md.containsKey("label")){
			poLabel = label(md);
		}
	} catch (Exception e) {
	    md = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	}

	if (endLabel.equals("Field") || endLabel.equals("FieldValidate") || endLabel.equals("SpiderField")) {
	    if (md == null) {
		return "poField";
	    }
	    Object columns = md.get("columns");
	    Object headers = md.get("header");
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
	    }else{//字段选择使用默认的value
			Map<String, Object> columnMap = new HashMap<>();
			columnMap.put("name", "值");
			columnMap.put("code", CruderConstant.VALUE);
			columnMapList.add(columnMap);
		}
	    if (!columnMapList.isEmpty()) {
		if (endLabel.equals("FieldValidate")) {
		    fieldValidate(model, endPo, columnMapList, false);
		} else {
		    field(model, endPo, columnMapList);
		}
	    } else {
		table2(model, endPo);
	    }

	    return "poField";
	}

	

	table2(model, endPo);
//	st.createHtml("poSelect", model.asMap());
	return "poSelect";
    }

    /**
     * 管理步骤，对活动的步骤进行定义管理
     * 
     * @param model
     * @param instanceId
     * @param endLabel
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/{endLabel}/{instanceId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String manyManage(Model model, @PathVariable("po") String poLabel, @PathVariable("endLabel") String endLabel,
	    @PathVariable("instanceId") String instanceId, HttpServletRequest request) throws Exception {
	Map<String, Object> viewMap = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
	if (viewMap == null) {
	    throw new DefineException(endLabel + "未定义！");
	}
	// 根据
	Map<String, Object> poMap = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	if (poMap == null) {
	    throw new DefineException(poLabel + "未定义！");
	}
	Object columns = poMap.get("columns");
	Object headers = poMap.get("header");
	List<Map<String, Object>> fieldList = new ArrayList<>();

	if (columns != null && headers != null) {
	    String[] codes = String.valueOf(columns).split(",");
	    String[] names = String.valueOf(headers).split(",");
	    if (codes.length == names.length) {
			for (int i = 0; i < codes.length; i++) {
				Map<String, Object> columnMap = new HashMap<>();
				columnMap.put("name", names[i]);
				columnMap.put("code", codes[i]);
				fieldList.add(columnMap);
			}
	    }
	}
	if (!fieldList.isEmpty()) {
	    if (endLabel.equals("FieldValidate")) {
			fieldValidate(model, viewMap, fieldList, true);
	    } else {
			field(model, viewMap, fieldList);
	    }
	} else {
	    tabList(model, viewMap);
	}

	model.addAttribute(INSTANCE_ID, instanceId);
	model.addAttribute(POLABEL_ID, poLabel + "Id");
	ModelUtil.setKeyValue(model, viewMap);
	// showService.showMetaInstanceCrudPage(model, endMap, true);
	showService.tableToolBtn(model, viewMap);
	// st.createHtml("layui/one2Many", model.asMap());
	return "layui/one2Many";
    }

    @RequestMapping(value = "/{po}/{endLabel}/label", method = { RequestMethod.GET, RequestMethod.POST })
    public String manyManageByLabel(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("endLabel") String endLabel, HttpServletRequest request) throws Exception {
	Map<String, Object> view = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
	if (view == null) {
	    throw new DefineException(endLabel + "未定义！");
	}
	// 根据
	Map<String, Object> instanceMap = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	if (instanceMap == null) {
	    throw new DefineException(poLabel + "未定义！");
	}

	Object columns = instanceMap.get("columns");
	Object headers = instanceMap.get("header");
	List<Map<String, Object>> fieldList = new ArrayList<>();

	if (columns != null && headers != null) {
	    String[] codes = String.valueOf(columns).split(",");
	    String[] names = String.valueOf(headers).split(",");
	    if (codes.length == names.length) {
		for (int i = 0; i < codes.length; i++) {
		    Map<String, Object> columnMap = new HashMap<>();
		    columnMap.put("name", names[i]);
		    columnMap.put("code", codes[i]);
		    fieldList.add(columnMap);
		}
	    }
	}
	if (!fieldList.isEmpty()) {
	    if (endLabel.equals("FieldValidate")) {
		fieldValidate(model, view, fieldList, true);
	    } else {
		field(model, view, fieldList);
	    }
	} else {
	    tabList(model, view);
	}

	model.addAttribute(INSTANCE_ID, poLabel);
	model.addAttribute(POLABEL_ID, poLabel + "Id");
	ModelUtil.setKeyValue(model, view);
	// showService.showMetaInstanceCrudPage(model, view, true);
	showService.tableToolBtn(model, view);
	// st.createHtml("layui/one2ManyByLabel", model.asMap());
	return "layui/one2ManyByLabel";
    }

    private void field(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	showService.field(model, po, columnMapList, true);
    }

    private void fieldValidate(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList,
	    Boolean useTab) {
	showService.fieldValidate(model, po, columnMapList, useTab);
    }

   
    
    @RequestMapping(value = "transfer/{po}/{start}/{relLabel}/{endLabel}", method = { RequestMethod.GET})
    public String transfer(Model model, @PathVariable("po") String label, @PathVariable("relLabel") String relLabel,
	    @PathVariable("endLabel") String endLabel, @PathVariable("start") String start,
	    HttpServletRequest request) throws Exception {
	label = NodeLabelUtil.firstValidate(label);
	Map<String, Object> metaStart = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (metaStart == null || metaStart.isEmpty()) {
	    LoggerTool.info(logger,label + "未定义！");
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, metaStart);
	Map<String, Object> startData = null;
	try {
	    Long valueOf = Long.valueOf(start);
	    startData = neo4jService.getPropMapByNodeId(valueOf);
	} catch (Exception e) {
	    startData = neo4jService.getAttMapBy(CODE, start, label);
	}
	if (startData == null) {
	    Object metaKey = metaStart.get("key");
	    if (metaKey != null) {
		startData = neo4jService.getAttMapBy(String.valueOf(metaKey), start, label);
	    }
	}
	 Map<String, Object> metaEnd = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
	 
	Long id2 = MapTool.id(startData);
	List<String> existEnds = neo4jService.getEndIdsOf(relLabel, id2,endLabel);
	
	List<Map<String, Object>> ends = neo4jService.pageOne(endLabel,"");
	
	model.addAttribute("endMdName", name(metaEnd));
	model.addAttribute("startId", id2);
	model.addAttribute("existEndIds", existEnds);
	model.addAttribute("endNodes", JSON.toJSONString(neo4jService.valueTitles(ends, endLabel)));
	model.addAttribute("endLabel", endLabel);
	model.addAttribute("start", start);
	model.addAttribute("relLabel", relLabel);
	return "layui/transfer";
    }
    /**
     * 查询 transfer数据
     * @param model
     * @param label
     * @param vo
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "queryTransfer/{po}", method = { RequestMethod.POST })
    @ResponseBody
    public WrappedResult transferData(Model model, 
	    @PathVariable("po") String label, 
	    @RequestBody JSONObject vo,
	    HttpServletRequest request) throws Exception {
	label = NodeLabelUtil.firstValidate(label);
	Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (metaData == null || metaData.isEmpty()) {
	    LoggerTool.info(logger,label + "未定义！");
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, metaData); 
	if(!vo.containsKey("name")&&vo.containsKey("title")) {
	    vo.put(NAME, string(vo,"title"));
	}
	List<Map<String, Object>> ends = neo4jService.pageOne(label,name(vo));
	return ResultWrapper.wrapResult(true, neo4jService.valueTitles(ends, label), null, QUERY_SUCCESS);
    }
    

    @RequestMapping(value = "/{po}/{relLabel}/{startLabel}/{start}", method = { RequestMethod.GET, RequestMethod.POST })
    public String toAddRelMeta(Model model, @PathVariable("po") String label, @PathVariable("relLabel") String relLabel,
	    @PathVariable("startLabel") String startLabel, @PathVariable("start") String start,
	    HttpServletRequest request) throws Exception {
	label = NodeLabelUtil.firstValidate(label);
	Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (metaData == null || metaData.isEmpty()) {
	    LoggerTool.info(logger,label + "未定义！");
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, metaData);
	Map<String, Object> startData = null;
	try {
	    Long valueOf = Long.valueOf(start);
	    startData = neo4jService.getPropMapByNodeId(valueOf);
	} catch (Exception e) {
	    startData = neo4jService.getAttMapBy(CODE, start, startLabel);
	}
	if (startData == null) {
	    Map<String, Object> metaStart = neo4jService.getAttMapBy(LABEL, startLabel, META_DATA);
	    Object metaKey = metaStart.get("key");
	    if (metaKey != null) {
		startData = neo4jService.getAttMapBy(String.valueOf(metaKey), start, startLabel);
	    }
	}

	Long id2 = MapTool.id(startData);
	List<Long> existEnds = neo4jService.getEndIdsOf(relLabel, id2);
	model.addAttribute("startId", id2);
	model.addAttribute("existEndIds", existEnds);
	model.addAttribute("startLabel", startLabel);
	model.addAttribute("start", start);
	model.addAttribute("relLabel", relLabel);
	showService.showMetaInstanceCrudPage(model, metaData, true);
	return "addRelToInstance";
    }

    @RequestMapping(value = "/vanForm/{po}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Map<String, Object> vanFrom(Model model, @PathVariable("po") String label, HttpServletRequest request)
	    throws Exception {
	label = NodeLabelUtil.firstValidate(label);
	Map<String, Object> metaData =null;
	if(label.contains(",")) {
	    String[] labels = NodeLabelUtil.labels(label);
	    for(String si: labels) {
		metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if(metaData!=null) {
		    break;
		}
	    }
	}else {
	    metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	}
	
	if (metaData == null || metaData.isEmpty()) {
	    LoggerTool.info(logger,label + "未定义！");	    
	    throw new DefineException(label + "未定义！");
	}
	Map<String, Object> vanFrom = vanFormService.getVanFrom(metaData);
	Map<String, Object> retData = new HashMap<>();
	List<Map<String, Object>> vanForms = new ArrayList<>();
	vanForms.add(vanFrom);
	retData.put("data", vanForms);
	return retData;
    }

    @RequestMapping(value = "/{po}/struct/{relation}", method = { RequestMethod.GET, RequestMethod.POST })
    public String instanceStruct(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("relation") String relation, HttpServletRequest request) throws Exception {

	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, relation, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(poLabel + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);
	/*
	 * String query = crudUtil.relations(label, relation); JSONArray query2 =
	 * neo4jService.relation(query); ModelUtil.setKeyValue(model, po);
	 */

	/*
	 * JSONObject vo = new JSONObject(); vo.put("poName", poLabel);
	 * objectService.cypher(vo, poLabel);
	 */

	table2(model, po);
//	st.createHtml("instatnce", model.asMap());
	return "instatnce";
    }

    /*
     * private void addHeadi(List<Map<String, String>> headList, String id, String
     * name) { Map<String, String> e = new HashMap<>(); e.put("id", id);
     * e.put("name", name); headList.add(e); }
     */

    @RequestMapping(value = "/{po}/{category}/tree", method = { RequestMethod.GET, RequestMethod.POST })
    public String poNavigation(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("category") String category, HttpServletRequest request) throws Exception {

	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, category, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(category + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);

	table2(model, po);
//	st.createHtml("instatnceTree", model.asMap());
	return "instatnceTree";
    }

    /**
     * @Describe:列表数据展现
     * @param model
     * @param po
     * @throws DefineException
     */
    private void table2(Model model, Map<String, Object> po) {
	showService.showMetaInstanceCrudPage(model, po, false);
    }

    /**
     * 包含关系列表展现的数据列表
     * 
     * @param model
     * @param po
     */
    private void tabList(Model model, Map<String, Object> po) {
	showService.showMetaInstanceCrudPage(model, po, true);
    }
}

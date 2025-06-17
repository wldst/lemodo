package com.wldst.ruder.module.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.*;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.auth.service.UserAdminService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/vo")
public class VoManageController extends RuleConstants {

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private RuleDomain rule;
	@Autowired
	private RelationService relationService;
    @RequestMapping(value = "/getByCode/{code}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult getByCode(@PathVariable("code") String voCode) throws DefineException {

	Map<String, Object> po = neo4jService.getAttMapBy(CODE, voCode, VO);
	String query = Neo4jOptCypher.voListByProps(po, null, null);

	StringBuilder sb = voLogic(po, query);
	// 查询业务逻辑：
	List<Map<String, Object>> dataList = neo4jService.voQuery(sb.toString());
	formatDates(dataList);
	return ResultWrapper.wrapResult(true, dataList, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/queryByCode/{voCode}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult queryByVoCode(@PathVariable("voCode") String voCode, @RequestBody JSONObject params)
	    throws DefineException {
	PageObject page = crudUtil.validatePage(params);

	Map<String, Object> vo = neo4jService.getAttMapBy(CODE, voCode, VO);
	String query = Neo4jOptCypher.voListByProps(vo, params, page);
	Long currentUserId = adminService.getCurrentUserId();
	// 查询业务逻辑：

	List<Map<String, Object>> dataList = neo4jService.voQuery(query);
	formatDates(dataList);
	page.setTotal(crudUtil.totalVo(query));
	return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/getOne/{code}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult getOne(@PathVariable("code") String voCode) throws DefineException {
	Map<String, Object> po = neo4jService.getAttMapBy(CODE, voCode, VO);
	String query = Neo4jOptCypher.voListByProps(po, null, null);

	StringBuilder sb = voLogic(po, query);
	// 查询业务逻辑：
	List<Map<String, Object>> dataList = neo4jService.voQuery(sb.toString());
	formatDates(dataList);
	fileDatas(dataList);
	return ResultWrapper.wrapResult(true, mergOne(dataList), null, QUERY_SUCCESS);
    }

    /**
     * 根据视图，查询视图逻辑。实时替换视图逻辑。
     * 
     * @param po
     * @param query
     * @return
     */
    private StringBuilder voLogic(Map<String, Object> po, String query) {
	StringBuilder sb = new StringBuilder();
	Long currentUserId = adminService.getCurrentUserId();
	// 获取视图逻辑
	List<Map<String, Object>> logics = neo4jService.getOneRelationEndNodeList(id(po), "voLogic");
	if (logics == null || logics.isEmpty()) {
	    sb.append(query);
	    return sb;
	}

	String[] queryWhereReturn = new String[3];
	boolean hasWhere = query.contains(" where ");
	if (hasWhere) {
	    queryWhereReturn = query.split(" where ");
	} else {
	    queryWhereReturn = query.split(" return ");
	}

	List<String> conditions = new ArrayList<>();

	sb.append(queryWhereReturn[0]);
	int i = 0;
	for (Map<String, Object> ri : logics) {
	    String logic = MapTool.string(ri, "content");
	    // 后续追加其他逻辑
	    i = handleCurrentUserId(query, currentUserId, conditions, sb, i, logic);
	}
	// WITH distinct a
	appendConditionReturn(sb, queryWhereReturn, hasWhere, conditions);
	return sb;
    }

    /**
     * 补全：where 追加return
     * 
     * @param sb
     * @param queryWhereReturn
     * @param hasWhere
     * @param conditions
     */
    public void appendConditionReturn(StringBuilder sb, String[] queryWhereReturn, boolean hasWhere,
	    List<String> conditions) {
	if (hasWhere) {
	    sb.append(" where ");
	    String join = String.join(" OR ", conditions);
	    sb.append("(" + join + ") and " + queryWhereReturn[1]);
	} else if (!conditions.isEmpty()) {
	    sb.append(" where ");
	    String join = String.join(" OR ", conditions);
	    sb.append(join + " return  " + queryWhereReturn[1]);
	} else {
	    if (!conditions.isEmpty()) {
		sb.append(" where ");
		String join = String.join(" OR ", conditions);
		sb.append(join + " return  " + queryWhereReturn[1]);
	    }
	    sb.append(" return  " + queryWhereReturn[1]);
	}
    }

    /**
     * 逻辑中带有xxx.f is currentUserId
     * 
     * @param query
     * @param currentUserId
     * @param conditions
     * @param sb
     * @param i
     * @param logic
     * @return
     */
    public int handleCurrentUserId(String query, Long currentUserId, List<String> conditions, StringBuilder sb, int i,
	    String logic) {
	String[] current = logic.split("is currentUserId");
	String left = current[0].trim();
	String[] objField = left.split("\\.");
	String objLabel = objField[0];
	String field = objField[1];
	if (query.indexOf(objLabel) > 0) {
	    String string3 = query.split(":" + objLabel)[0];
	    String[] split3 = string3.split("\\(");
	    String vari = split3[split3.length - 1];
	    if (field.equals(ID)) {
		conditions.add("id(" + vari + ") =" + currentUserId + "");
	    } else {
		conditions.add(vari + "." + field + "=\"" + currentUserId + "\"");
	    }
	} else {
	    sb.append(" OPTIONAL MATCH (a)-->(z" + i + ":" + objLabel.trim() + "{" + field.trim() + ":" + currentUserId
		    + "})  ");// where z"+i+".instanceID=id(a)
	    conditions.add(" z" + i + ".instanceID=id(a)");
	    i++;
	}
	return i;
    }

    /**
     * 根据给定的VO ID和请求体JSONObject进行查询操作，并返回包装后的结果。
     * @param voId 表示VO对象的ID，用于查询特定的VO实例。
     * @param vo 一个包含查询条件的JSONObject对象。
     * @return 返回一个WrappedResult对象，包含查询是否成功、数据列表和分页信息。
     * @throws DefineException 如果查询过程中出现定义错误，抛出此异常。
     */
    @RequestMapping(value = "{voId}/query", method = { RequestMethod.POST,
            RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult query(@PathVariable("voId") String voId, @RequestBody JSONObject vo) throws DefineException {
        // 验证页码信息，并从请求体中获取属性映射
        PageObject page = crudUtil.validatePage(vo);

		Map<String, Object> po = null;
		try {
			if (Long.valueOf(voId) != null) {
				po = neo4jService.getPropMapBy(voId);
			}
		} catch (NumberFormatException e) {
			po = neo4jService.getAttMapBy(CODE, voId, VO);
		}
		String kw=keyWord(vo);
		vo.remove("KeyWord");

        String query = null;

		// 根据请求体中是否包含id字段，构建不同的查询语句
		Map<String, Object> voMap=toVoMap(vo, po);
		query=Neo4jOptCypher.voListByProps(po, voMap, page);

		// 获取当前用户ID，用于后续构建查询条件
        Long currentUserId = adminService.getCurrentUserId();
		StringBuilder sb = voLogic(voId, query, currentUserId);
		query = searchColumn(kw, po, sb.toString());
		// 执行查询，并处理查询结果
        List<Map<String, Object>> dataList = neo4jService.voQuery(query);
        formatDates(dataList);
        page.setTotal(crudUtil.totalVo(query));
        // 返回包装后的查询结果
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

	/**
	 * 将前端的查询字段转换为Vo对应的字段
	 * @param vo
	 * @param po
	 * @return
	 */
	@NotNull
	private static Map<String, Object> toVoMap(JSONObject vo, Map<String, Object> po){
		Map<String, Object> asMap=new HashMap<>();
		//vo查询条件
		String voColumns=string(po, "voColumns");
		String[] split=voColumns.split(",");
		for(String si : split){
			String coli="";
			if(si.contains(" AS ")){
				String[] split1=si.split(" AS ");
				asMap.put(split1[1],split1[0]);
				coli=split1[0];
			}else if(si.contains(" as ")){
				String[] split1=si.split(" as ");
				asMap.put(split1[1],split1[0]);
			}else{
				coli=si;
				asMap.put(si,coli);
			}
		}
		Map<String, Object> voMap=new HashMap<>();
		for(String col : vo.keySet()){
			String voColi=string(asMap,col);
			if(voColi!=null&&!col.equals(voColi)){
				voMap.put(voColi, vo.get(col));
			}else{
				voMap.put(col, vo.get(col));
			}
		}
		return voMap;
	}

	@RequestMapping(value = "/query", method = { RequestMethod.POST,
			RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult query2(@RequestBody JSONObject vo) throws DefineException {
		String voId = string(vo,"voCode");
		vo.remove("voCode");
		// 验证页码信息，并从请求体中获取属性映射
		PageObject page = crudUtil.validatePage(vo);
		Map<String, Object> viewObject = neo4jService.getAttMapBy(CODE,voId, CruderConstant.VO);

		String query = null;
		String kw=keyWord(vo);
		vo.remove("KeyWord");
		// 根据请求体中是否包含id字段，构建不同的查询语句
		if (vo.containsKey("id")) {
			query = Neo4jOptCypher.voListByProps(viewObject, null, page);
		} else {
			query = Neo4jOptCypher.voListByProps(viewObject, vo, page);
		}


		// 获取当前用户ID，用于后续构建查询条件
		Long currentUserId = adminService.getCurrentUserId();
		StringBuilder sb = voLogic(stringId(viewObject), query, currentUserId);
		query = searchColumn(kw, viewObject, sb.toString());
		// 执行查询，并处理查询结果
		List<Map<String, Object>> dataList = neo4jService.voQuery(query);
		formatDates(dataList);
		page.setTotal(crudUtil.totalVo(query));
		// 返回包装后的查询结果
		return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
	}

	private static String searchColumn(String kw, Map<String, Object> po, String query) {

		if(kw==null||"".equals(kw.trim())){
			return query;
		}
		// 处理搜索列的拆分
		String[] searchColumns = splitValue(po, "searchColumn");
		List<String> conditions = new ArrayList<>();
		if (searchColumns != null) {
			for (String searchColumn : searchColumns) {
				conditions.add(searchColumn +" CONTAINS '" + kw + "' ");
			}
		}
		String voColumns=string(po, "voColumns");

		String[] split=voColumns.split(",");
		for(String si : split){
			String coli="";
			if(si.contains(" AS ")){
				String[] split1=si.split(" AS ");
				coli=split1[0];
			}else if(si.contains(" as ")){
				String[] split1=si.split(" as ");
				coli=split1[0];
			}else{
				coli=si;
			}
			conditions.add(coli +" CONTAINS \"" + kw + "\" ");
		}

		if (conditions.size() > 0) {
			String join = String.join(" OR ", conditions);
			String[] queryWhereReturn = new String[3];
			boolean hasWhere = query.contains(" where ");
			if (hasWhere) {
				queryWhereReturn = query.split(" where ");
					query = queryWhereReturn[0] + " where (" + join + ") and " + queryWhereReturn[1];
			} else {
				queryWhereReturn = query.split(" return ");
				query = queryWhereReturn[0] + " where " + join + " return " + queryWhereReturn[1];
			}
		}else{
			query = query.split(" return ")[0] + " return * ";
		}
		return query;
	}

	@NotNull
	private StringBuilder voLogic(String voId, String query, Long currentUserId) {
		// 获取与VO逻辑相关的一端节点列表
		List<Map<String, Object>> oneRelationEndNodeList = neo4jService.getOneRelationEndNodeList(Long.valueOf(voId),
				"voLogic");
		String[] queryWhereReturn = new String[3];
		boolean hasWhere = query.contains(" where ");
		if (hasWhere) {
			queryWhereReturn = query.split(" where ");
		} else {
			queryWhereReturn = query.split(" return ");
		}

		// 构建额外的查询条件，用于过滤当前用户相关的信息
		List<String> conditions = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		sb.append(queryWhereReturn[0]);
		int i = 0;
		for (Map<String, Object> ri : oneRelationEndNodeList) {
			String string = MapTool.string(ri, "content");
			String[] split = string.split("is currentUserId");
			String string2 = split[0].trim();
			String[] split2 = string2.split("\\.");
			String field = split2[1];
			String objLabel = split2[0];
			if (query.indexOf(objLabel) > 0) {
				String string3 = query.split(":" + objLabel)[0];
				String[] split3 = string3.split("\\(");
				String vari = split3[split3.length - 1];
				if (field.equals(ID)) {
					conditions.add("id(" + vari + ") =" + currentUserId + "");
				} else {
					conditions.add(vari + "." + field + "=" + currentUserId + "");
				}
			} else {
				sb.append(" OPTIONAL MATCH (a)-->(z" + i + ":" + objLabel.trim() + "{" + field.trim() + ":"
						+ currentUserId + "})  ");
				conditions.add(" z" + i + ".instanceID=id(a)");
				i++;
			}
		}

		// 将构建的额外条件合并到查询语句中
		if (hasWhere) {
			sb.append(" where ");
			if (conditions != null && !conditions.isEmpty()) {
				String join = String.join(" OR ", conditions);
				sb.append("(" + join + ") and ");
			}
			sb.append(queryWhereReturn[1]);
		} else if (!conditions.isEmpty()) {
			sb.append(" where ");
			String join = String.join(" OR ", conditions);
			sb.append(join + " return  " + queryWhereReturn[1]);
		} else {
			if (!conditions.isEmpty()) {
				sb.append(" where ");
				String join = String.join(" OR ", conditions);
				sb.append(join + " return  " + queryWhereReturn[1]);
			}
			sb.append(" return  " + queryWhereReturn[1]);
		}
		return sb;
	}

	@RequestMapping(value = "/instanceData/{voId}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult instanceData(@PathVariable("voId") String voId, @RequestBody JSONObject params)
	    throws DefineException {
	PageObject page = crudUtil.validatePage(params);
	Map<String, Object> voDefinei = neo4jService.getLablePropBy(voId);
	String query = null;
	if (params.containsKey("id")) {
	    query = Neo4jOptCypher.voListByProps(voDefinei, null, page);
	} else {
	    query = Neo4jOptCypher.voListByProps(voDefinei, params, page);
	}

	List<Map<String, Object>> dataList = neo4jService.voQuery(query);
	formatDates(dataList);
	page.setTotal(crudUtil.totalVo(query));
	return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/{voId}/form", method = { RequestMethod.GET, RequestMethod.POST })
    public String editForm(Model model, @PathVariable("voId") String voId, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> vo = neo4jService.getNodeMapById(Long.valueOf(voId));
	if (vo == null || vo.isEmpty()) {
	    throw new DefineException(voId + "视图未定义！");
	}
	vo.put("voId",voId);
	ModelUtil.setKeyValue(model, vo);
	showService.editVoForm(model, vo, true);
	showService.voTable(model, vo, false);
	return "layui/editVoForm";
    }

    @RequestMapping(value = "/{label}/upload", method = { RequestMethod.GET, RequestMethod.POST })
    public String upload(Model model, @PathVariable("label") String label, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> vo = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (vo == null || vo.isEmpty()) {
	    throw new DefineException(label + "视图未定义！");
	}
	ModelUtil.setKeyValue(model, vo);
	return "layui/dataImport";
    }

    @RequestMapping(value = "rels/{voId}/{relation}", method = { RequestMethod.GET, RequestMethod.POST })
    public String poRelations(Model model, @PathVariable("voId") String voId, @PathVariable("relation") String relation,
	    HttpServletRequest request) throws Exception {

	Map<String, Object> relationPo = neo4jService.getAttMapBy(LABEL, relation, META_DATA);

	if (!"tree".equals(relation)) {
	    if (relationPo == null || relationPo.isEmpty()) {
		throw new DefineException(relation + "未定义！");
	    }
	}

	ModelUtil.setKeyValue(model, relationPo);
	if (relation.equals("Field") || relation.equals("FieldValidate") || relation.equals("SpiderField")) {
	    Map<String, Object> voMap = neo4jService.getAttMapBy("id", voId, VO);

	    if (voMap == null) {
		return "layui/voField";
	    }
	    Object columns = voMap.get("voColumns");
	    Object headers = voMap.get("header");
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
		if (relation.equals("FieldValidate")) {
		    fieldValidate(model, relationPo, columnMapList);
		} else {
		    field(model, relationPo, columnMapList);
		}
	    } else {
		table2(model, relationPo);
	    }

	    return "layui/voField";
	}
	if ("tree".equals(relation)) {
	    relationPo = neo4jService.getAttMapBy(LABEL, voId, VO);
	    ModelUtil.setKeyValue(model, relationPo);
	    // tabList(model, po);
	    String setting = "{callback: {\n onClick: childList\n }}";
	    model.addAttribute("setting", setting);
	    Map<String, Object> tree = neo4jService.getWholeTree(voId);
	    JSONArray zNodesList = new JSONArray();
	    zNodesList.add(tree);
	    model.addAttribute("zNodes", zNodesList);
		model.addAttribute("voId",voId);
	    return "instanceTree";
	}

	table2(model, relationPo);
	return "poSelect";
    }

    private void field(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	showService.field(model, po, columnMapList, true);
    }

    private void fieldValidate(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	showService.fieldValidate(model, po, columnMapList, false);
    }

    @RequestMapping(value = "/{voLabel}/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult save(@PathVariable("voLabel") String voLabel, @RequestBody JSONObject vo)
	    throws DefineException {
	if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
	    return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
	}
	// Map<String, Object> po = neo4jService.getAttMapBy(LABEL, voLabel, VO);
	Map<String, Object> voDef = neo4jService.getNodeMapById(Long.valueOf(voLabel));
	if (voDef == null || voDef.isEmpty()) {
	    throw new DefineException(voLabel + "未定义！");
	}
	parseVo(vo, voDef);

	DateTool.replaceDateTime2Long(vo);

	return ResultWrapper.wrapResult(true, vo, null, SAVE_SUCCESS);
    }

    private void parseVo(JSONObject vo, Map<String, Object> voDef) {
	String[] labelColumns = columns(voDef);
	String[] vcolumns = voColumns(voDef);

	String cypher = cypher(voDef);
	String[] splitx = cypher.split("return");
	// 获取视图返回列中的变量加字段映射。
	Map<String, String> voColMap = voColumnMap(splitx[1]);
	// 截取变量关系label。
	/**
	 * (a:Project)-[r0:managerIs]->(e0:User), (a:Project)-[r1:hasMember]->(e1:User),
	 * (a:Project)-[r2:budget]->(e2:Budget)
	 */
	Map<String, String> parseRel = parseRel(splitx[0]);

	Set<String> varSet = getVarSet(voColMap);
	Set<String> relVarSet = getRelVarSet(voColMap);
	if (relVarSet.size() > 0) {
	    // 解析关系，并更新关系数据。
	}
	Map<String, String> vlMap = new HashMap<>();

	for (int i = 0; i < labelColumns.length; i++) {
	    String vc = voColMap.get(vcolumns[i]);
	    String vi = vc.split("\\.")[0];
	    String li = labelColumns[i].split("\\.")[0];
	    vlMap.put(vi.trim(), li.trim());
	}

	// 识别变量对应的Label

	Set<String> labelSet = collectLabel(labelColumns);
	int i = 0;
	String varTemp = "a";
	for (String vi : varSet) {
	    String labi = vlMap.get(vi);

	    Map<String, Object> mti = new HashMap<>();
	    for (String ci : vcolumns) {// labelCol
		String vci = voColMap.get(ci);
		String trim = vci.trim();
		if (trim.startsWith(vi + ".")) {
		    String[] split = trim.split("\\.");
		    String key2 = split[1];
		    mti.put(key2, vo.get(key2));
		}
	    }

	    if (labelSet.size() == 1) {
		if (labi.equals(META_DATA)) {
		    boolean b = !mti.containsKey(CODE) && !mti.containsKey(LABEL) && !mti.containsKey(COLUMNS);
		    if (b) {
			crudUtil.wumaDataRegular(mti);
		    }
		    rule.validRule(labi, mti);
		    // 主数据，关系数据。逐个保存，保存和关联关系
		    Node save = neo4jService.save(mti, labi);
		    if (b) {
			Long voID = save.getId();
			if (save != null && (label(mti) == null || "".equals(label(mti).trim()))) {
			    mti.put(LABEL, WUMA_CODE + voID);
			}
			mti.put(ID, voID);
			neo4jService.update(mti);
		    }
		    vo.put(ID, save.getId());
		}
	    } else {
		if (i == 0) {
		    Long existId = neo4jService.getNodeIdByPropAndLabel(mti, labi);
		    if (existId != null) {
			neo4jService.update(mti, existId);
			vo.put(ID, existId);
		    } else {
			// 主数据，关系数据。逐个保存，保存和关联关系
			Node save = neo4jService.save(mti, labi);
			vo.put(ID, save.getId());
		    }
		} else {
		    // find rLabel
		    Long existId = neo4jService.getNodeIdByPropAndLabel(mti, labi);
		    String relationLabel = parseRel.get(varTemp + vi);
		    if (existId != null) {
			relationService.addRel(relationLabel, id(vo), existId);
			neo4jService.update(mti, existId);
		    } else {
			Node save = neo4jService.save(mti, vi);
			relationService.addRel(relationLabel, id(vo), save.getId());
		    }
		}
	    }
	    i++;
	}
    }

    /**
     * 解析关系
     * 
     * @param matchStr
     * @return
     */
    private Map<String, String> parseRel(String matchStr) {
	Map<String, String> seRel = new HashMap<>();
	if (matchStr.contains("),")) {
	    String[] matchs = matchStr.split("),");
	    for (String ci : matchs) {
		splitMatchi(seRel, ci);
	    }
	} else {
	    splitMatchi(seRel, matchStr);
	}
	return seRel;
    }

    /**
     * 解析逗号隔开的单元，解析是否含有关系
     * 
     * @param seRel
     * @param ci
     */
    private void splitMatchi(Map<String, String> seRel, String ci) {
	String[] matchi = ci.split(":");
	if (!ci.contains("-[r")) {
	    return;
	}
	if (matchi.length == 4) {
	    String var = matchi[0];
	    String[] startV = var.split("(");
	    String sVar = "";
	    if (startV.length > 1) {
		sVar = startV[1];
	    } else {
		sVar = startV[0];
	    }

	    String relVar = matchi[1].split(")-[")[1];

	    String[] rel = matchi[2].split("]->(");
	    String relLabel = rel[0];
	    if (relLabel.contains("{")) {
		relLabel = relLabel.split("{")[0];
	    }
	    String eVar = rel[1];
	    seRel.put(sVar + eVar, relLabel);
	    seRel.put(relVar, relLabel);
	}
    }

    private Set<String> getVarSet(Map<String, String> voColumnMap) {
	Set<String> varSet = new HashSet<>();
	for (String ki : voColumnMap.keySet()) {
	    String string = voColumnMap.get(ki);
	    if (!string.startsWith("r")) {
		String[] split = string.split("\\.");
		varSet.add(split[0].trim());
	    }
	}
	return varSet;
    }

    private Set<String> getRelVarSet(Map<String, String> voColumnMap) {
	Set<String> varSet = new HashSet<>();
	for (String ki : voColumnMap.keySet()) {
	    String string = voColumnMap.get(ki);
	    if (string.startsWith("r")) {
		String[] split = string.split("\\.");
		varSet.add(split[0].trim());
	    }
	}
	return varSet;
    }

    private Map<String, String> voColumnMap(String returnStr) {
	if (returnStr.indexOf("order") > 0) {
	    returnStr = returnStr.split("order")[0];
	}
	String[] split = returnStr.split(",");
	Map<String, String> colMap = new HashMap<>();
	if (returnStr.contains(" AS ")) {
	    for (String ci : split) {
		String[] asColumn = ci.trim().split(" AS ");
		colMap.put(asColumn[1].trim(), asColumn[0].trim());
	    }
	} else {
	    for (String ci : split) {
		colMap.put(ci, ci);
	    }
	}
	return colMap;
    }

    private Set<String> collectLabel(String[] columns2) {
	Set<String> mdSet = new HashSet<>();
	for (String ci : columns2) {
	    String[] split = ci.split("\\.");
	    if (split.length > 1) {
		mdSet.add(split[0]);
	    }
	}
	return mdSet;
    }

    @RequestMapping(value = "/{voId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String voDataList(Model model, @PathVariable("voId") String voId, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> vo = null;
	try {
	    if (Long.valueOf(voId) != null) {
		vo = neo4jService.getPropMapBy(voId);
	    }
	} catch (NumberFormatException e) {
	    vo = neo4jService.getAttMapBy(CODE, voId, VO);
	}
	// .getAttMapBy("id", voId, VO);
	if (vo == null || vo.isEmpty()) {
	    throw new DefineException(voId + "未定义！");
	}
	String label = label(vo);
	if (label != null && label.startsWith(WUMA_CODE)) {
	    Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	    if (metaData == null || metaData.isEmpty()) {
		throw new DefineException(label + "未定义！");
	    }
	    ModelUtil.setKeyValue(model, metaData);
	    showService.showWumaCrudPage(model, metaData, true);
	    showService.wmTableToolBtn(model, label, metaData);
	    return "instanceCustom";
	}

	Object object2 = vo.get("voColumns");
	if (object2 != null) {
	    String object = (String) object2;
	    String[] split = object.split(",");
	    String voLabel;
	    for (String voCi : split) {
		String[] split2 = voCi.split("\\.");
		if (split2[0].indexOf("_") < 1) {
		    voLabel = split2[0];
		    vo.put(LABEL, voLabel);
		    break;
		}
	    }
	    String[] columnArray = splitColumnValue(vo, "voColumns");
	    String[] noAs = new String[columnArray.length];
	    if (object.contains(" AS ")) {
		for (int i = 0; i < columnArray.length; i++) {
		    if (columnArray[i].contains(" AS ")) {
			noAs[i] = columnArray[i].split(" AS ")[1];
		    } else {
			noAs[i] = columnArray[i];
		    }
		}
		columnArray = noAs;
	    }else {
		noAs =columnArray;
	    }
	    vo.put("voColumns", String.join(",", noAs));
	}

	Long id2 = id(vo);
	vo.put("voId", id2);
	ModelUtil.setKeyValue(model, vo);
	model.addAttribute("sessionId", request.getSession().getId());
	showService.voTable(model, vo, true);
	showService.d2vTableBtn(model, String.valueOf(id2), vo);

	return "layui/voDataList";
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
    @RequestMapping(value = "/{voId}/Field", method = { RequestMethod.GET, RequestMethod.POST })
    public String field(Model model, @PathVariable("voId") String voId, HttpServletRequest request) throws Exception {
	Map<String, Object> fieldMd = neo4jService.getAttMapBy("label", "Field", META_DATA);
		fieldMd.put("voId",voId);
	ModelUtil.setKeyValue(model, fieldMd);

	Map<String, Object> poMap = neo4jService.getAttMapBy("id", voId, VO);
	Object columns = poMap.get("columns");
	Object headers = poMap.get("header");
	List<Map<String, Object>> columnMapList = new ArrayList<>();
	String[] voColumns = splitColumnValue(poMap, "voColumns");
	if (columns != null && headers != null) {
	    String[] codes = columns(poMap);
	    String[] names = headers(poMap);
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
	    field(model, fieldMd, columnMapList);
	} else {
	    table2(model, fieldMd);
	}
	return "layui/voField";

    }

    private void fieldValidate(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList,
	    Boolean useTab) {
	showService.fieldValidate(model, po, columnMapList, useTab);
    }

    @RequestMapping(value = "/list/{voId}/{dataId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String voList(Model model, @PathVariable("voId") String voId, @PathVariable("dataId") String dataId,
	    HttpServletRequest request) throws Exception {
	Map<String, Object> vo = neo4jService.getPropMapBy(voId);
	// .getAttMapBy("id", voId, VO);
	if (vo == null || vo.isEmpty()) {
	    throw new DefineException(voId + "未定义！");
	}
	// List<String> splitValue2List = MapTool.splitValue2List(vo, COLUMNS);
	// String[] split2 = splitValue2List.get(0).split("\\.");
	// vo.put(LABEL, split2[0]);
	vo.put("voId", voId);
	vo.put("dataId", dataId);
	ModelUtil.setKeyValue(model, vo);
	showService.voTable(model, vo, true);
	return "layui/vDataList";
    }

    @RequestMapping(value = "/{voId}/update", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult update(@PathVariable("voId") String voId, @RequestBody JSONObject vo) throws DefineException {
	if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
	    return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
	}

	Map<String, Object> viewObject = null;
	try {
	    Long voIdLong = Long.valueOf(voId);
	    if (voIdLong != null) {
		viewObject = neo4jService.getPropMapBy(voId);
	    }
	} catch (NumberFormatException e) {
	    viewObject = neo4jService.getAttMapBy(CODE, voId, VO);
	}
	if (viewObject == null || viewObject.isEmpty()) {
	    throw new DefineException(voId + "未定义！");
	}

	viewObject.put(HEADER, joinString(vo, HEADER));
	viewObject.put(COLUMNS, joinString(vo, COLUMNS));
	viewObject.put(VO_COLUMN, joinString(vo, VO_COLUMN));

	neo4jService.update(viewObject, Long.valueOf(voId));

	return ResultWrapper.wrapResult(true, viewObject, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/orderColumn/{voId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String orderColumn(Model model, @PathVariable("voId") String voId, HttpServletRequest request)
	    throws Exception {

	Map<String, Object> vo = null;
	try {
	    if (Long.valueOf(voId) != null) {
		vo = neo4jService.getPropMapBy(voId);
	    }
	} catch (NumberFormatException e) {
	    vo = neo4jService.getAttMapBy(CODE, voId, VO);
	}

	ModelUtil.setKeyValue(model, vo);
	String[] columns = columns(vo);
	String[] header = headers(vo);
	String[] voColumns = splitColumnValue(vo, "voColumns");

	if (columns == null) {
	    return "vue/orderColumn";
	}
	JSONArray data = new JSONArray();
	for (int i = 0; i < columns.length; i++) {
	    JSONObject joi = new JSONObject();
	    joi.put("index", i + 1);
	    String hi = header[i];
	    if (hi.contains("[") || hi.contains("]")) {
		String replaceAll = hi.replaceAll("\\[", "").replaceAll("\\]", "");
		joi.put("columnName", replaceAll);
	    } else {
		joi.put("columnName", hi);
	    }

	    String ci = columns[i];
	    if (ci.contains("[") || ci.contains("]")) {
		String replaceAll2 = ci.replaceAll("\\[", "").replaceAll("\\]", "");
		joi.put("columnCode", replaceAll2);
	    } else {
		joi.put("columnCode", ci);
	    }

	    joi.put("vcolumn", voColumns[i]);

	    data.add(joi);
	}
	model.addAttribute("data", data);
	return "vue/orderVoColumn";
    }

    /*
     * private void addHeadi(List<Map<String, String>> headList, String id, String
     * name) { Map<String, String> e = new HashMap<>(); e.put("id", id);
     * e.put("name", name); headList.add(e); }
     */
    @RequestMapping(value = "/tree/{po}/{category}", method = { RequestMethod.GET, RequestMethod.POST })
    public String poNavigation(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("category") String category, HttpServletRequest request) throws Exception {

	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, category, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(category + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);

	table2(model, po);

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
}

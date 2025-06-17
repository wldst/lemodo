package com.wldst.ruder.module.manage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.wldst.ruder.util.*;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.ws.ManageClientWebSocket;
import com.wldst.ruder.module.ws.handler.ServerHandler;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 后台用户管理 Created by macro on 2018/4/26.
 */
@Controller
@RequestMapping("${server.context}/collect")
public class CollectController extends AuthDomain {
    private static Logger logger = LoggerFactory.getLogger(CollectController.class);
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    @Autowired
    private RelationService cypherService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private RestApi rest;
    @Autowired
    private ManageClientWebSocket mcWebSocket;
    @Autowired
    private ServerHandler serverHandler;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private RuleDomain rule;
    private static ExecutorService exec = getExecutorService();

    /**
     * 编辑表单
     * 
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/form", method = { RequestMethod.GET })
    public String editForm(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception {
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
	ModelUtil.setKeyValue(model, po);
	showService.editForm(model, po, true);
	showService.columnInfo(model, po, false);
	return "layui/editForm";
    }

    @RequestMapping(value = "/{label}/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult jielongSave(@PathVariable("label") String label, @RequestBody JSONObject vo)
	    throws DefineException {
	if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
	    return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
	}
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	Long jlId = longValue(vo, "jlId");
	String jlName = string(vo, "jlName");
	String jlAction = string(vo, "jlAction");
	vo.remove("jlId");
	vo.remove("jlName");

	crudUtil.clearColumnOrHeader(vo);

	DateTool.replaceDateTime2Long(vo);
	Map<String,Object> voC = compat(vo, po);
	rule.validRule(label, voC, po);
	if (voC.containsKey(VALID_RESULT) && voC.containsKey(VALIDATE_MSG) && !bool(voC,VALID_RESULT)) {
	    String msg = string(voC, VALIDATE_MSG);
	    return ResultWrapper.wrapResult(false, voC, null, msg);
	}
	Node nodeId = neo4jService.saveByKey(voC, label, NODE_ID);
	Map<String,Object> param = new HashMap<>();
	param.put("jlId", jlId);
	param.put("jlName", jlName);
	try {
	    cypherService.addRel(jlAction, adminService.getCurrentPasswordId(), nodeId.getId(),param);
	    cypherService.addRel(jlAction, adminService.getCurrentPasswordId(), jlId);
	} catch (NumberFormatException e) {
	    LoggerTool.error(logger,"add rel exception", e);
	}

	if (nodeId == null) {
	    return ResultWrapper.wrapResult(false, null, null, SAVE_FAILED);
	}

	return ResultWrapper.wrapResult(true, nodeId.getId(), null, SAVE_SUCCESS);
    }

    /**
     * 编辑表单
     * 
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/jlform", method = { RequestMethod.GET })
    public String jielongForm(Model model, @PathVariable("po") String label, HttpServletRequest request)
	    throws Exception {
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
	ModelUtil.setKeyValue(model, po);
	showService.editForm(model, po, true);
	showService.columnInfo(model, po, false);
	return "layui/jielongForm";
    }

    /**
     * 接龙数据
     * 
     * @param action
     * @param vo
     * @return
     * @throws DefineException
     */
    @RequestMapping(value = "/jielong/{action}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult jielong(@PathVariable("action") String action, @RequestBody JSONObject vo)
	    throws DefineException {
	// 接龙
	String isDoAction = MapTool.lowCaseStr(vo, action);
	vo.remove(action);// 动作不计入查询功能。
	// 获取当前人角色，角色-接龙数据-接龙MetaDataId。
	StringBuilder sb = new StringBuilder();
	sb.append("match (a:Password)-[*1..3]-(b:jielong{status:\"running\"}) where id(a)=" + adminService.getCurrentPasswordId());
	if (isDoAction.equals("true") || isDoAction.equals("false")) {
	    if (isDoAction.equals("true")) {
		sb.append(" and  exists((a)-[:" + action + "]->(b))  ");
	    } else {
		sb.append("  and not exists((a)-[:" + action + "]->(b))  ");
	    }
	}
	sb.append(" return distinct id(b) as jlId,b.name as jlName,b.formId,b.voId");
	List<Map<String, Object>> queryData = neo4jService.cypher(sb.toString());

	return ResultWrapper.wrapResult(true, queryData, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/{po}/readForm", method = { RequestMethod.GET, RequestMethod.POST })
    public String readOnlyForm(Model model, @PathVariable("po") String label, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);
	showService.editForm(model, po, true);
	showService.columnInfo(model, po, false);
	return "layui/editForm";
    }

    private Map<String, Object> sayNotice(Map<String, Object> notice, String httpCLient) {
	Map<String, Object> sayInfo = new HashMap<>();
	sayInfo.put("cmd", CMD_NOTICE);
	sayInfo.put(LABEL_NOTICE, notice);
	return rest.postForObject(httpCLient, sayInfo, Map.class);
    }

    private <T> T vtResult(Callable<T> callabel) {
	Future<T> submit = exec.submit(callabel);
	T list = null;
	try {
	    list = submit.get();
	} catch (InterruptedException | ExecutionException e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	}
	return list;
    }

    /**
     * 解析嵌套字段
     * 
     * @param model
     * @param idString
     */
    private void embedListParse(Model model, String idString) {
	List<Map<String, Object>> oneRelationList = neo4jService.getOneRelationList(Long.valueOf(idString),
		"parseData");
	if (oneRelationList != null && !oneRelationList.isEmpty()) {
	    StringBuilder changeForm = new StringBuilder();
	    StringBuilder bcf = new StringBuilder();

	    changeForm.append(" function formDataChange(formData){");
	    int i = 0;
	    for (Map<String, Object> map : oneRelationList) {
		Map<String, Object> endMap = (Map<String, Object>) map.get(RELATION_ENDNODE_PROP);
		String columns = String.valueOf(endMap.get(COLUMNS));
		List<Map<String, Object>> listAllByLabel = neo4jService
			.listAllByLabel(String.valueOf(endMap.get(LABEL)));
		// map.get(changeForm)
		changeForm.append("\n var selChanForm=formData['parseData" + i + "'];");
		for (Map<String, Object> datai : listAllByLabel) {
		    changeForm.append("\n if(selChanForm=='" + datai.get("code") + "') {\n");
		    for (String key : columns.split(",")) {
			if (" id name code primaryKey ".indexOf(key) == -1) {
			    changeForm.append("\n formData['" + key + "']='" + String.valueOf(datai.get(key)) + "';\n");

			}
		    }
		    changeForm.append(" }");
		}

		bcf.append(" formData['parseData" + i + "']=$('#parseData" + i + "').val();\n");
		i++;
	    }
	    changeForm.append(" }");
	    changeForm.append("\nformDataChange(formData);\n");
	    bcf.append(changeForm.toString());
	    model.addAttribute("formDataChange", bcf.toString());
	}
    }

    
    @RequestMapping(value = "/list", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult listAction(@RequestBody JSONObject vo) throws DefineException {
	
	PageObject page = crudUtil.validatePage(vo);
	vo.remove("page");
	
	String queryText = MapTool.string(vo, "KeyWord");
	if (queryText != null && !"".equals(queryText.trim())) {
	    vo.put(NAME, queryText);
	    // vo.put(CODE, queryText);
	    vo.remove("KeyWord");
	}
	optByUserSevice.setAdminService(adminService);
//	String query = optByUserSevice.moreQuery(vo);
//	
//	List<Map<String, Object>> dataList = neo4jService.cypher(query);
	Set<String> labels = new HashSet<>();
	
//	if(dataList!=null) {
//	    for(Map<String, Object> di:dataList) {
//		    labels.addAll(stringSet(di,"labels"));
//		}
//	}
	String bySysCode = neo4jService.getBySysCode("ActionMeta");
	if(bySysCode==null) {
	    return ResultWrapper.wrapResult(true, null, null, QUERY_FAILED);
	}
	optByUserSevice.setAdminService(adminService); 

	for(String si:bySysCode.split(",")) {
	    labels.add(si);
	}
	
	
	String getData = optByUserSevice.moreDataPage(vo, page, labels);
	List<Map<String, Object>> dataList2 = neo4jService.cypher(getData);
	return ResultWrapper.wrapResult(true, dataList2, page, QUERY_SUCCESS);
    }
    
    @RequestMapping(value = "/listMeta", method = { RequestMethod.GET,RequestMethod.POST }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult listMeta(@RequestBody JSONObject vo) throws DefineException {
	
	PageObject page = crudUtil.validatePage(vo);
	vo.remove("page");
	
	String bySysCode = neo4jService.getBySysCode("ActionMeta");
	if(bySysCode==null) {
	    return ResultWrapper.wrapResult(true, null, null, QUERY_FAILED);
	}
	optByUserSevice.setAdminService(adminService); 

	Set<String> labels = new HashSet<>();
	for(String si:bySysCode.split(",")) {
	    labels.add(si);
	}
	String countData = optByUserSevice.moreDataCount(vo,labels);
	List<Map<String, Object>> moreDataCount = neo4jService.cypher(countData);
	Map<String,Object> labelCount = new HashMap<>();
	for(Map<String, Object> more: moreDataCount) {
	    String[] stringSet = strArray(more,"mLabel");
	    for(String li: stringSet) {
		labelCount.put(li, integer(more, "more"));
	    }	    
	}
	String query = optByUserSevice.moreMetaDataPage(page,labels);
	List<Map<String, Object>> dataList = neo4jService.cypher(query);
	//更新每个元数据下的数据个数
	for(Map<String, Object> di: dataList) {
	    String metaLabel = label(di);
	    di.put("more", labelCount.get(metaLabel));
	}
	return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }
}

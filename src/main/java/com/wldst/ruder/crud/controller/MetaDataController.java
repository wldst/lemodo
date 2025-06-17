package com.wldst.ruder.crud.controller;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.auth.service.UserAdminService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.constant.Msg;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@ResponseBody
@RequestMapping("${server.context}/metadata/")
public class MetaDataController extends AuthDomain {
	private static Logger logger = LoggerFactory.getLogger(MetaDataController.class);
    private final CrudNeo4jService neo4jService;

	@Autowired
	private UserAdminService adminService;
    private final CrudUtil crudUtil;

    private final RuleDomain rule;
	@Autowired
	public MetaDataController(@Lazy CrudNeo4jService neo4jService, @Lazy CrudUtil crudUtil, @Lazy RuleDomain rule){
		this.neo4jService=neo4jService;
        this.crudUtil=crudUtil;
        this.rule=rule;
    }

	/**
     * 
     * 
     * @author liuqiang
     * @date 2019年9月20日 上午9:37:44
     * @version V1.0
     * @param vo
     * @return
     */
    @RequestMapping(value = "/query", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult queryAllData(@RequestBody JSONObject vo) {
		try {
			adminService.checkAuth(META_DATA,"SaveOperate","查询操作");
		}catch (Exception e) {
			return ResultWrapper.wrapResult(false, null, null, e.getMessage());
		}
		PageObject page = crudUtil.validatePage(vo);
		String query = Neo4jOptCypher.queryObj2(vo, META_DATA, PO_COLUMN.split(","), page);
		List<Map<String, Object>> query2 = neo4jService.query(query,vo);
		page.setTotal(crudUtil.total(query,vo));
		return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/relateList", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult relateList(@RequestBody JSONObject vo) {
	try {
		adminService.checkAuth(META_DATA,"RelationQuery","关系查询");
	}catch (Exception e) {
	    return ResultWrapper.wrapResult(false, null, null, e.getMessage());
	}
	PageObject page = crudUtil.validatePage(vo);
	String query = Neo4jOptCypher.queryObj2(vo, META_DATA, PO_COLUMN.split(","), page);
	// if(query.indexOf("where")>0) {
	// String[] split = query.split(" where ");
	// query= split[0]+" n.columns  CONTAINS ',name,' AND "+split[1];
	// }else {
	// String[] split = query.split(" return ");
	// query= split[0]+" where n.columns  CONTAINS ',name,' return "+split[1];
	// }
	page.setTotal(crudUtil.total(query));
	List<Map<String, Object>> query2 = neo4jService.query(query,vo);
	return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/relateNameList", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult relateNameList(@RequestBody JSONObject vo) {
	try {
	    adminService.checkAuth(META_DATA,"RelationQuery","关系查询");
	}catch (Exception e) {
	    return ResultWrapper.wrapResult(false, null, null, e.getMessage());
	}
	PageObject page = crudUtil.validatePage(vo);
	String query = Neo4jOptCypher.queryObj2(vo, META_DATA, PO_COLUMN.split(","), page);
	if (query.indexOf(" where ") > 0) {
	    String[] split = query.split(" where ");
	    query = split[0] + " n.columns  CONTAINS ',name,' AND " + split[1];
	} else {
	    String[] split = query.split(" return ");
	    query = split[0] + " where n.columns  CONTAINS ',name,' return " + split[1];
	}
	List<Map<String, Object>> queryedData = neo4jService.query(query,vo);
	if (queryedData == null || queryedData.isEmpty()) {
	    query = Neo4jOptCypher.queryObj2(vo, META_DATA, PO_COLUMN.split(","), page);
	    if (query.indexOf(" where ") > 0) {
		String[] split = query.split(" where ");
		query = split[0] + " (n.header  CONTAINS '名称' OR n.header  CONTAINS '代码') AND " + split[1];
	    } else {
		String[] split = query.split(" return ");
		query = split[0] + " where (n.header  CONTAINS '名称' OR n.header  CONTAINS '代码') return " + split[1];
	    }
	    queryedData = neo4jService.query(query,vo);
	}

	page.setTotal(crudUtil.total(query,vo));
	return ResultWrapper.wrapResult(true, queryedData, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/list", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult list(@RequestBody JSONObject vo) {
	try {
	    adminService.checkAuth(META_DATA,"QueryOperate","查询");
	}catch (Exception e) {
	    return ResultWrapper.wrapResult(false, null, null, e.getMessage());
	}
	String query = Neo4jOptCypher.safeQueryObj(vo, META_DATA, PO_COLUMN.split(","));
	List<Map<String, Object>> query2 = neo4jService.query(query,vo);
	return ResultWrapper.ret(true, query2, QUERY_SUCCESS);
    }

    /**
     * 领域对象新增
     * 
     * @param vo
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult save(@RequestBody JSONObject vo) {
	try {
	    adminService.checkAuth(META_DATA,"SaveOperate","保存更新");
	}catch (Exception e) {
	    return ResultWrapper.wrapResult(false, null, null, e.getMessage());
	}
	if (vo.isEmpty()) {
	    return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
	}
	crudUtil.clearColumnOrHeader(vo);
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, META_DATA, META_DATA);
	// crudUtil.completePK(vo,CRUD_KEY,COLUMNS,HEADER);
	rule.validRule(null, vo, po);
	Node saveByKey = neo4jService.saveByKey(vo, META_DATA, PO_KEY);
	return ResultWrapper.wrapResult(true, saveByKey.getId(), null, SAVE_SUCCESS);
    }

    /**
     * 领域对象更新
     * 
     * @param vo
     * @param request
     * @return
     */
    @RequestMapping("/update")
    public WrappedResult update(@RequestBody JSONObject vo, HttpServletRequest request) {
	try {
	    adminService.checkAuth(META_DATA,"SaveOperate","保存更新");
	}catch (Exception e) {
	    return ResultWrapper.wrapResult(false, null, null, e.getMessage());
	}
	if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
	    return ResultWrapper.wrapResult(true, null, null, UPDATE_FAILED);
	}
	neo4jService.update(vo, META_DATA, PO_KEY.split(","));
	return ResultWrapper.wrapResult(true, null, null, UPDATE_SUCCESS);
    }

    /**
     * 领域对象删除
     * 
     * @param vo
     * @return
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult del(@RequestBody JSONObject vo) {
	if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
	    return ResultWrapper.wrapResult(true, null, null, DELETE_FAILED);
	}
	
	try {
	    adminService.checkAuth(META_DATA,"DeleteOperate","删除");
	}catch (Exception e) {
	    return ResultWrapper.wrapResult(false, null, null, e.getMessage());
	}

	Object label = vo.get(LABEL);
	remove(String.valueOf(label));

	String delRelBObj = Neo4jOptCypher.delRelbOf(vo, META_DATA);
	delRelBObj = delRelBObj.replaceAll("\"\"", "\"");
	neo4jService.execute(delRelBObj);
	String delRelAObj = Neo4jOptCypher.delRelaOf(vo, META_DATA);
	delRelAObj = delRelAObj.replaceAll("\"\"", "\"");
	neo4jService.execute(delRelAObj);

	neo4jService.removeNodeById(id(vo));

	return ResultWrapper.wrapResult(true, null, null, Msg.DELETE_SUCCESS);
    }

    public void remove(String label) {
	String delRelBObj = Neo4jOptCypher.delRelbOf(label);
	neo4jService.execute(delRelBObj);
	String delRelAObj = Neo4jOptCypher.delRelaOf(label);

	neo4jService.execute(delRelAObj);
	String delObj = Neo4jOptCypher.delObj(label);
	neo4jService.execute(delObj);
    }
    
    @RequestMapping(value = "/getAttMapBy", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult getAttMapBy(@RequestBody JSONObject vo) {
	 
		String key = string(vo,"key");
		String value = string(vo,"value");
		String label = label(vo);
		if(key==null||value==null||label==null) {
			logger.error("请补全必填参数，key={}，value{}，label{}",key,value,label);
			return ResultWrapper.wrapResult(false, null, null, "请补全必填参数，key，value，label");
		}
		Map<String, Object> po = neo4jService.getAttMapBy(key, value, label);

		return ResultWrapper.wrapResult(true, po, null, Msg.QUERY_SUCCESS);
    }

}

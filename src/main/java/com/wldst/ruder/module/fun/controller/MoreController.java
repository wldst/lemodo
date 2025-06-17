package com.wldst.ruder.module.fun.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.controller.BaseLayuiController;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

/**
 * 
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/more/{metaData}")
public class MoreController extends BaseLayuiController {
    private static Logger logger = LoggerFactory.getLogger(MoreController.class);
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    @Autowired
    private UserAdminService adminService;

    @RequestMapping(value = "/get/{action}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult wasDone(@PathVariable("metaData") String label, 
	    @PathVariable("action") String action,
	    @RequestBody JSONObject vo) throws DefineException {
	Map<String, Object> meta = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (meta == null || meta.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	PageObject page = crudUtil.validatePage(vo);
	String[] columns = crudUtil.getMdColumns(label);
	// 脱敏处理字段
	if (columns == null || columns.length <= 0) {
	    return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
	}
	
	vo.remove("page");
	
	String queryText = MapTool.string(vo, "KeyWord");
	if (queryText != null && !"".equals(queryText.trim())) {
	    vo.put(NAME, queryText);
	    // vo.put(CODE, queryText);
	    vo.remove("KeyWord");
	}
	optByUserSevice.setAdminService(adminService);
	String query = optByUserSevice.moreQuery(vo, label,action, columns);
	
	List<Map<String, Object>> dataList = neo4jService.cypher(query);
	
	return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }
    
    
}

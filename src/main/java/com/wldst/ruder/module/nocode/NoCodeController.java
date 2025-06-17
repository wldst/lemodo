package com.wldst.ruder.module.nocode;


import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.*;
import org.neo4j.graphdb.Node;
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

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.constant.Msg;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.auth.service.UserAdminService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("${server.context}/nocode")
public class NoCodeController extends MapTool{
    private static Logger logger = LoggerFactory.getLogger(NoCodeController.class);
	@Autowired
	private CrudNeo4jService neo4jService;
	@Autowired
	private Neo4jOptByUser optByUserSevice;
	@Autowired
	private HtmlShowService showService;
	 @Autowired
	    private UserAdminService adminService;
	@Autowired
	private CrudUtil crudUtil;
	@Autowired
	private RuleDomain rule;
	/**
	 * 
	  *  
	  * @author liuqiang
	  * @date 2019年9月20日 上午9:37:44
	  * @version V1.0
	  * @param vo
	  * @return
	 */
	@RequestMapping(value="/page", method = {RequestMethod.POST,RequestMethod.GET}, produces="application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult queryAllData(@RequestBody JSONObject vo){
		PageObject page = crudUtil.validatePage(vo);
		String query =Neo4jOptCypher.queryObj2(vo, WUMA, PO_COLUMN.split(","), page);
		page.setTotal(crudUtil.total(query,vo));
		List<Map<String, Object>> query2 = neo4jService.query(query,vo);
		return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
	}
	
	@RequestMapping(value="/list", method = {RequestMethod.POST,RequestMethod.GET}, produces="application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult list(@RequestBody JSONObject vo){
		String query =Neo4jOptCypher.safeQueryObj(vo, WUMA, PO_COLUMN.split(","));
		List<Map<String, Object>> query2 = neo4jService.query(query,vo);
		return ResultWrapper.ret(true, query2, QUERY_SUCCESS);
	}
	
	/**
	  *  无码元数据
	 * @param vo
	 * @return
	 */
	@RequestMapping(value="/save", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult save(@RequestBody JSONObject vo){
		if(vo.isEmpty()) {
			return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
		}
		crudUtil.wumaRegular(vo);		
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, WUMA, META_DATA);
		rule.validRule(null, vo, po);
		
		Node saveByKey = neo4jService.saveByKey(vo, WUMA,CODE);
		if(!vo.containsKey(CODE)) {
			vo.put(CODE, WUMA_CODE+saveByKey.getId());
			neo4jService.update(vo);
		}
		return ResultWrapper.wrapResult(true, saveByKey.getId(), null, SAVE_SUCCESS);
	}
	

	
	/**
	   * 领域对象更新
	 * @param vo
	 * @param request
	 * @return
	 */
	@RequestMapping("/update")
	@ResponseBody
	public WrappedResult update(@RequestBody JSONObject vo,HttpServletRequest request){
		if(vo.isEmpty()||!crudUtil.isColumnsNotEmpty(vo)) {
			return ResultWrapper.wrapResult(true, null, null, UPDATE_FAILED);
		}
		neo4jService.update(vo, WUMA,PO_KEY.split(","));
		return ResultWrapper.wrapResult(true, null, null, UPDATE_SUCCESS);
	}
	/**
	   *  领域对象删除
	 * @param vo
	 * @return
	 */
	@RequestMapping(value="/del", method = RequestMethod.POST ,produces="application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult del(@RequestBody JSONObject vo){		
		if(vo.isEmpty()||!crudUtil.isColumnsNotEmpty(vo)) {
			return ResultWrapper.wrapResult(true, null, null, DELETE_FAILED);
		}

		Object label = vo.get(LABEL);
		remove(String.valueOf(label));	
		
		String delRelBObj = Neo4jOptCypher.delRelbOf(vo, WUMA);		
		delRelBObj=delRelBObj.replaceAll("\"\"", "\"");
		neo4jService.execute(delRelBObj);
		String delRelAObj = Neo4jOptCypher.delRelaOf(vo, WUMA);		
		delRelAObj=delRelAObj.replaceAll("\"\"", "\"");
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
	
	@RequestMapping(value = "/{po}", method = { RequestMethod.GET, RequestMethod.POST })
	public String noCodeCrud(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception {
		label=NodeLabelUtil.firstValidate(label);
		Map<String, Object> wumaData = neo4jService.getAttMapBy(LABEL, label, WUMA);
		if (wumaData == null || wumaData.isEmpty()) {
		    LoggerTool.info(logger,label + "未定义！");
			throw new DefineException(label + "未定义！");
		}
		ModelUtil.setKeyValue(model, wumaData);

		showService.showWumaInstanceCrudPage(model, wumaData, true);
		showService.tableToolBtn(model, wumaData);
		return "instanceCustom";
	}
	
	@RequestMapping(value = "/query", method = { RequestMethod.POST,
		    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	    public WrappedResult query(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
		PageObject page = crudUtil.validatePage(vo);
		String[] columns = crudUtil.getMdColumns(label);
		// 脱敏处理字段

		if (columns == null || columns.length <= 0) {
		    return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
		}

		Map<String, Object> wuma = neo4jService.getAttMapBy(LABEL, label, WUMA);
		vo.remove("page");
		if (label.equals("DBTable")) {
		    rule.validMyRule(label, vo, wuma);
		}
		String queryText = string(vo, "KeyWord");
		if (queryText != null && !"".equals(queryText.trim())) {
		    vo.put(NAME, queryText);
		    // vo.put(CODE, queryText);
		    vo.remove("KeyWord");
		}
		optByUserSevice.setAdminService(adminService);
		String query = optByUserSevice.queryObj(vo, label, columns, page);
		List<Map<String, Object>> dataList = neo4jService.query(query,vo);
		if (dataList != null) {
		    rule.formateQueryField(dataList);
		    if (!dataList.isEmpty() && !vo.containsKey(ID)) {
			page.setTotal(crudUtil.total(query,vo));
		    }
		    deSensitive(label, dataList);
		}
		return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
	    }
	
	
	/**
	     * 脱敏处理
	     * 
	     * @param label
	     * @param dataList
	     * @throws DefineException
	     */
	    private void deSensitive(String label, List<Map<String, Object>> dataList) throws DefineException {
		if (!dataList.isEmpty()) {
		    String[] sensitiveColumn = crudUtil.getSensitiveColumn(label);
		    if (sensitiveColumn != null && sensitiveColumn.length > 0) {
			for (Map<String, Object> di : dataList) {
			    for (String si : sensitiveColumn) {
				String string = string(di, si);
				String dsValue = string.substring(0, 1);
				di.put(si, dsValue + "***");
			    }
			}
		    }
		}
	    }

}

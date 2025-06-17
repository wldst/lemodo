package com.wldst.ruder.crud.controller;

import static com.wldst.ruder.constant.CruderConstant.VIEW;
import static com.wldst.ruder.constant.CruderConstant.VIEW_COLUMN;
import static com.wldst.ruder.constant.CruderConstant.VIEW_KEY;
import static com.wldst.ruder.constant.Msg.DELETE_FAILED;
import static com.wldst.ruder.constant.Msg.DELETE_SUCCESS;
import static com.wldst.ruder.constant.Msg.QUERY_SUCCESS;
import static com.wldst.ruder.constant.Msg.SAVE_FAILED;
import static com.wldst.ruder.constant.Msg.SAVE_SUCCESS;
import static com.wldst.ruder.constant.Msg.UPDATE_FAILED;
import static com.wldst.ruder.constant.Msg.UPDATE_SUCCESS;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;
/**
 * 视图管理，任意视图
 * @author wldst
 *
 */
@RestController
@ResponseBody
@RequestMapping("${server.context}/cruder/view")
public class CrudderViewController {
	@Autowired
	private CrudNeo4jService neo4jService;
	@Autowired
	private CrudUtil crudUtil;
	/**
	  * 
	  *  
	  * @author liuqiang
	  * @date 2019年9月20日 上午9:37:44
	  * @version V1.0
	  * @param vo
	  * @return
	 */
	@RequestMapping(value="/query", method = {RequestMethod.POST,RequestMethod.GET}, produces="application/json;charset=UTF-8")
	public WrappedResult queryAllData(@RequestBody JSONObject vo){
		PageObject page = crudUtil.validatePage(vo);
		String query =Neo4jOptCypher.queryObj2(vo, VIEW, VIEW_COLUMN.split(","), page);
		page.setTotal(crudUtil.total(query,vo));
		List<Map<String, Object>> query2 = neo4jService.query(query,vo);
		return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
	}
	
	@RequestMapping(value="/list", method = {RequestMethod.POST,RequestMethod.GET}, produces="application/json;charset=UTF-8")
	public WrappedResult list(@RequestBody JSONObject vo){
		String query =Neo4jOptCypher.safeQueryObj(vo, VIEW, VIEW_COLUMN.split(","));
		List<Map<String, Object>> query2 = neo4jService.query(query,vo);
		return ResultWrapper.ret(true, query2, QUERY_SUCCESS);
	}
	
	/**
	  *  视图对象新增
	 * @param vo
	 * @return
	 */
	@RequestMapping(value="/save", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult save(@RequestBody JSONObject vo){
		if(vo.isEmpty()) {
			return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
		}
		crudUtil.clearColumnOrHeader(vo);
		Node saveByKey = neo4jService.saveByKey(vo, VIEW,VIEW_KEY);
		return ResultWrapper.wrapResult(true, saveByKey.getId(), null, SAVE_SUCCESS);
	}
	/**
	   * 视图对象更新
	 * @param vo
	 * @param request
	 * @return
	 */
	@RequestMapping("/update")
	public WrappedResult update(@RequestBody JSONObject vo,HttpServletRequest request){
		if(vo.isEmpty()||!crudUtil.isColumnsNotEmpty(vo)) {
			return ResultWrapper.wrapResult(true, null, null, UPDATE_FAILED);
		}
		neo4jService.update(vo, VIEW,VIEW_KEY.split(","));
		return ResultWrapper.wrapResult(true, null, null, UPDATE_SUCCESS);
	}
	/**
	   *  视图对象删除
	 * @param vo
	 * @return
	 */
	@RequestMapping(value="/del", method = RequestMethod.POST ,produces="application/json;charset=UTF-8")
	public WrappedResult del(@RequestBody JSONObject vo){		
		if(vo.isEmpty()||!crudUtil.isColumnsNotEmpty(vo)) {
			return ResultWrapper.wrapResult(true, null, null, DELETE_FAILED);
		}
		String delObj = Neo4jOptCypher.delObj(vo, VIEW);
		
		delObj=delObj.replaceAll("\"\"", "\"");
		neo4jService.execute(delObj);
		return ResultWrapper.wrapResult(true, null, null, DELETE_SUCCESS);
	}

}

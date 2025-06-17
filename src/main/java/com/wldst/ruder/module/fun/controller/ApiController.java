package com.wldst.ruder.module.fun.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.GoodsDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

/**
 * 主要实现商品的序列化，传递，反序列化Neo4j
 * 
 * @author wldst
 *
 */
@RestController
@ResponseBody
@RequestMapping("${server.context}/api/{label}")
public class ApiController extends GoodsDomain {

    @Autowired
    private CrudNeo4jService neo4jService;


    @RequestMapping(value = "/import", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult importList(@PathVariable("label") String label,@RequestBody JSONObject vo) throws DefineException {

	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	List<Map<String, Object>> listMap = null;
	String string = MapTool.string(vo, "data");
	if(string.startsWith("[{")) {
	    listMap = MapTool.listMapObject(vo, "data");
	}else {
	    //解密操作
	}
	
	if(listMap!=null&&!listMap.isEmpty()) {
	    Set<String> splitValue2List = columnSet(po);
	    neo4jService.addList(listMap, label,splitValue2List);
	}
	
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }
    
    @RequestMapping(value = "/transKey", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult transKey(@PathVariable("label") String label) throws DefineException {

	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	List<Map<String, Object>> listMap = neo4jService.listAllByLabel(label);
	
	
	if(listMap!=null&&!listMap.isEmpty()) {
	    Set<String> splitValue2List = columnSet(po);
	    neo4jService.refreshList(listMap, label,splitValue2List);
	}
	
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }
}

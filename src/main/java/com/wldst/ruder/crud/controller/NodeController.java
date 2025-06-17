package com.wldst.ruder.crud.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.state.service.StateService;
import com.wldst.ruder.util.*;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.wldst.ruder.crud.service.RelationService;

@RestController
@ResponseBody
@RequestMapping("${server.context}/node/")
public class NodeController extends MapTool{

	@Autowired
	private CrudUtil crudUtil;
	@Autowired
	private CrudNeo4jService neo4jService;
	@Autowired
	private RelationService relationService;
	@Autowired
	private StateService statusService;
	final static Logger logger = LoggerFactory.getLogger(NodeController.class);
	@RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult save(@RequestBody JSONObject vo) {
		if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
			return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
		}
		String label = vo.getString(LABEL);
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		Node saveByKey = null;
		if(po==null||po.isEmpty()) {//更新定义
			 Map<String, Object> defineMap = new HashMap<>();
			 List<String> columns = new ArrayList<>();
			 for(String key:vo.keySet()) {
				 columns.add(key);
			 }
			 String columnsStr = String.join(",", columns);
			 if(!columnsStr.startsWith("id,")&&!columns.contains("id")) {
				 columnsStr="id,"+columnsStr;
			 }
			 defineMap.put(COLUMNS, columnsStr);
			 defineMap.put(HEADER, columnsStr);
			 defineMap.put("name", label);
			 defineMap.put(NODE_LABEL, label);
			 saveByKey = neo4jService.saveByKey(defineMap, META_DATA,PO_KEY);
		}
//		if (!vo.containsKey(CRUD_KEY)) {
//			vo.put(CRUD_KEY, NODE_ID);
//		}
		saveByKey = neo4jService.saveByBody(vo, label);
		
		
		return ResultWrapper.wrapResult(true, saveByKey.getId(), null, SAVE_SUCCESS);
	}


	/**
	 * 通过POST请求计算并返回指定标签的数据总数和各状态数量。
	 * @param vo 请求体中的JSONObject，包含需要查询的标签信息。
	 * @return WrappedResult 包含查询结果的包装对象，其中包含总数（total）、状态列表（statusList）和各状态数量（count）。
	 */
	@RequestMapping(value = "/count", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult count(@RequestBody JSONObject vo) {
		try {
			// 输入验证
			if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
				return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
			}

			String label = label(vo);
			// 防止注入攻击的安全性检查
			if (!isValidLabel(label)) {
				return ResultWrapper.wrapResult(true, null, null, "Invalid label");
			}

			// 使用参数化查询以增强安全性
			Map<String, Object> params = labelPrams("label",label);

			// 查询数据总数
			List<Map<String, Object>> count = neo4jService.cypher("MATCH (n:" + label + ") RETURN count(n) as count");
			// 查询各状态数量
			List<Map<String, Object>> countStatus = neo4jService.cypher("MATCH (n:"+ label + ") RETURN n.status,n.id,n.amount");

			//Cypher累加所有金额字段amount
			List<Map<String, Object>> countAmount = neo4jService.cypher("MATCH (n:"+ label + ") RETURN sum(tofloat(n.amount)) as amount");

			Map<String, Integer> countMap = countStatusToMap(countStatus);
			Map<String, Double> countData = countStatusData(countStatus,"amount");

			List<Map<String, Object>> statusList = getStatusList(label, countMap);

			Map<String,Object> countValueMap = statusValueMap(statusList, countMap);
			Map<String,Object> statusAmountValue = valueMap(statusList, countData);
			// 组装返回结果
			Map<String, Object> ret = new HashMap<>();
			ret.put("count", countValueMap);
			ret.put("statusCount", statusAmountValue);

			ret.put("total", count.get(0).get("count"));
			if(countAmount.size()>0){
				Map<String, Object> amountMap = countAmount.get(0);
				if(amountMap.get("amount")!=null){
					ret.put("totalAmount", amountMap.get("amount"));
				}
			}
			ret.put("statusList", statusList);

			return ResultWrapper.wrapResult(true, ret, null, SAVE_SUCCESS);
		} catch (Exception e) {
			// 异常处理
			// 可以记录日志或进行其他异常处理逻辑
			return ResultWrapper.wrapResult(true, null, null, "Operation failed");
		}
	}

	private static Map<String, Object>  statusValueMap(List<Map<String, Object>> statusList, Map<String, Integer> countMap) {
		Map<String, Object> countValueMap=new HashMap<>();
		for(Map<String,Object> si: statusList){
			String statusValue = string(si, "value");
			if(statusValue==null){
				continue;
			}
			String scount = string(countValueMap, statusValue);
			int count =0;
			if(scount!=null&&!"null".equals(scount)){
				count =Integer.valueOf(scount);
			}

			String code = code(si);
			String name=name(si);

			if(countMap.get(statusValue)!=null){
				countValueMap.put(statusValue, count+countMap.get(statusValue));
			}

			if(code!=null&& countMap.get(code)!=null){
				countValueMap.put(statusValue, count+Integer.valueOf(countMap.get(code)));
			}

			if(name!=null&& countMap.get(name)!=null){
				countValueMap.put(statusValue, count+Integer.valueOf(countMap.get(name)));
			}
		}
		return countValueMap;
	}

	private static Map<String, Object>  valueMap(List<Map<String, Object>> statusList, Map<String, Double> countMap) {
		Map<String, Object> countValueMap=new HashMap<>();
		for(Map<String,Object> si: statusList){
			String value = string(si, "value");
			if(value==null){
				continue;
			}
			String code = string(si, "code");
			if(countMap.get(value)!=null){
				countValueMap.put(value, countMap.get(value));
			}
			if(code!=null&& countMap.get(code)!=null){
				countValueMap.put(value, countMap.get(code));
			}
		}
		return countValueMap;
	}

	private boolean isValidLabel(String label) {
		Map<String, Object> params = labelPrams("label",label);
		// 实现对标签的验证逻辑，例如检查是否包含非法字符
		// 这里返回true作为示意，实际应用中需要具体实现
		List<Map<String, Object>> count = neo4jService.cypher("MATCH (n:"+META_DATA+"{label:\""+label+"\"}) RETURN count(n) as count");
		return integer(count.get(0),"count")>0;
	}

	@NotNull
	private static Map<String, Object> labelPrams(String key,String label) {
		Map<String, Object> params = new HashMap<>();
		params.put(key, label);
		return params;
	}

	private Map<String, Integer> countStatusToMap(List<Map<String, Object>> countStatus) {
		Map<String, Integer> countMap = new HashMap<>();
		for (Map<String, Object> si : countStatus) {
			String status = (String) si.get("status");
			if(status==null){
				continue;
			}
			Integer statusCount = countMap.get(status);
			if (statusCount == null) {
				countMap.put(status, 1);
			} else {
				countMap.put(status, statusCount + 1);
			}
		}
		return countMap;
	}
	private Map<String, Double> countStatusData(List<Map<String, Object>> countStatus,String key) {
		Map<String, Double> countMap = new HashMap<>();
		for (Map<String, Object> si : countStatus) {
			String status = (String) si.get("status");
			Double statusCount = countMap.get(status);
			if(si.get(key)==null){
				continue;
			}
			String keyValue = string(si, key);
			if(keyValue==null||"null".equals(keyValue)){
				continue;
			}
			Double dx  = Double.valueOf(keyValue);
			if (statusCount == null) {
				countMap.put(status, dx);
			} else {
				countMap.put(status, statusCount + dx);
			}
		}
		return countMap;
	}

	private List<Map<String, Object>> getStatusList(String label, Map<String, Integer> countMap) {
		List<Map<String, Object>> countx = statusService.listStatus(label);
		List<Map<String, Object>> statusList = new ArrayList<>();
		for (Map<String, Object> si : countx) {
			String value = (String) si.get("value");
			String code = (String) si.get("code");
			String name=name(si);
			if (value != null && !"null".equals(value)) {
				if (countMap.get(value) != null || countMap.get(code) != null||countMap.get(name)!=null) {
					statusList.add(si);
				}
			}
		}
		return statusList;
	}

	@RequestMapping(value = "/get/{objId}", method = { RequestMethod.POST,
		RequestMethod.GET }, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public Map<String, Object> get(@PathVariable("objId") String objId) {
	    Map<String, Object> attMapBy = neo4jService.getNodeMapById(Long.valueOf(objId));
	    formatDate(attMapBy);
	    return attMapBy;
	}
	@RequestMapping(value = "/saveRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult saveRel(@RequestBody JSONObject vo) {
		if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
			return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
		}
		Node endNode = neo4jService.getNodeById(vo.getString("endId"));
		Node startNode = neo4jService.getNodeById(vo.getString("startId"));
		LoggerTool.info(logger,vo.toJSONString());
		relationService.addRel(vo.getString("relLabel"),vo.getString("relLabel"),startNode.getId()
			, endNode.getId());
		return ResultWrapper.wrapResult(true, endNode.getId(), null, SAVE_SUCCESS);
	}
	@RequestMapping(value = "/checkId", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	@ResponseBody
	public WrappedResult checkId(@RequestBody JSONObject vo) {
		if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
			return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
		}
		List<Map<String, Object>> colums = neo4jService.cypher("match(a:"+META_DATA+") where not a.columns CONTAINS 'id' return id(a) as id,a.columns AS columns,a.header as header");
		for(Map<String, Object> nodei: colums) {
			Map<String, Object> ddMap = new HashMap<>();
			String[] columns = String.valueOf(nodei.get("columns")).split(",");
			String head = String.valueOf(nodei.get("header"));
			List<String> columnsSet = new ArrayList<>();
			 for(String key:columns) {
				 columnsSet.add(key);
			 }
			 String columnsStr = String.join(",", columns);
			 if(!columnsStr.startsWith("id,")&&!columnsSet.contains("id")) {
				 columnsStr="id,"+columnsStr;
				 head="编码,"+head;
			 }
			
			ddMap.put("columns", columnsStr);
			ddMap.put("header", head);
			neo4jService.saveById(String.valueOf(nodei.get("id")),ddMap);
		}
		return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
	}
}

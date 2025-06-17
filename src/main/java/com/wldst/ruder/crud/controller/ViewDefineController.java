package com.wldst.ruder.crud.controller;

import static com.wldst.ruder.constant.CruderConstant.COLUMNS;
import static com.wldst.ruder.constant.CruderConstant.END_LABEL;
import static com.wldst.ruder.constant.CruderConstant.HEADER;
import static com.wldst.ruder.constant.CruderConstant.LABEL;
import static com.wldst.ruder.constant.CruderConstant.META_DATA;
import static com.wldst.ruder.constant.CruderConstant.PO_PROP_SPLITER;
import static com.wldst.ruder.constant.CruderConstant.RELATION_LABEL;
import static com.wldst.ruder.constant.CruderConstant.START_LABEL;
import static com.wldst.ruder.constant.CruderConstant.VIEW;
import static com.wldst.ruder.constant.CruderConstant.VIEW_COLUMN;
import static com.wldst.ruder.constant.CruderConstant.VIEW_KEY;
import static com.wldst.ruder.constant.CruderConstant.VO_COLUMN;
import static com.wldst.ruder.constant.Msg.DELETE_FAILED;
import static com.wldst.ruder.constant.Msg.DELETE_SUCCESS;
import static com.wldst.ruder.constant.Msg.QUERY_SUCCESS;
import static com.wldst.ruder.constant.Msg.SAVE_FAILED;
import static com.wldst.ruder.constant.Msg.SAVE_SUCCESS;
import static com.wldst.ruder.constant.Msg.UPDATE_FAILED;
import static com.wldst.ruder.constant.Msg.UPDATE_SUCCESS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.*;
import jakarta.servlet.http.HttpServletRequest;

import org.neo4j.graphdb.Node;
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
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.module.fun.Neo4jOptCypher;

/**
 * 视图管理，任意视图
 * 
 * @author wldst
 *
 */
@RestController
@ResponseBody
@RequestMapping("${server.context}/viewDefine/{po}")
public class ViewDefineController {
    @Autowired
    private CrudNeo4jService neo4jService;
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
    @RequestMapping(value = "/query", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult queryAllData(@RequestBody JSONObject vo,@PathVariable("po") String label) throws DefineException {
		LabelValidator.validateLabel(label);
	PageObject page = crudUtil.validatePage(vo);
	String query = Neo4jOptCypher.queryObj2(vo, VIEW, VIEW_COLUMN.split(","), page);
	page.setTotal(crudUtil.total(query,vo));
	List<Map<String, Object>> query2 = neo4jService.query(query,vo);
	return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/list", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult list(@RequestBody JSONObject vo,@PathVariable("po") String label) throws DefineException {
		LabelValidator.validateLabel(label);
	String query = Neo4jOptCypher.safeQueryObj(vo, VIEW, VIEW_COLUMN.split(","));
	List<Map<String, Object>> query2 = neo4jService.query(query,vo);
	return ResultWrapper.ret(true, query2, QUERY_SUCCESS);
    }

    /**
     * 视图对象新增
     * 
     * @param vo
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult save(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
			LabelValidator.validateLabel(label);
		if (vo.isEmpty()) {
			return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
		}
		// 主实体
		Map<String, Object> mainPoInfo = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		Map<String, String> columnHeaderMap = new HashMap<>();
		StringBuilder returnColumn = new StringBuilder();
		List<String> keyList = new ArrayList<String>();
		Set<String> labelSet = new HashSet<>();
		Map<String,String> labelName = new HashMap<>();
		Map<String, Map<String, String>> labelMapcolHeader = new HashMap<>();
		labelName.put(label,MapTool.name(mainPoInfo));
		label2ColumnHead(label, mainPoInfo, labelMapcolHeader);
		List<Map<String, Object>> relationDefineList = neo4jService.queryRelationDefine("startLabel", label);
		// 映射实体字段
		for (Map<String, Object> ri : relationDefineList) {
			Object endLabel = ri.get(END_LABEL);
			Object relLabelObj = ri.get(RELATION_LABEL);
			if (endLabel != null) {
			String eLabel = String.valueOf(endLabel);
			String relLabel = String.valueOf(relLabelObj);
			Map<String, Object> endiPo = neo4jService.getAttMapBy(LABEL, eLabel, META_DATA);
			if (endiPo != null && !endiPo.isEmpty()) {
				labelName.put(eLabel,MapTool.name(endiPo));
				label2ColumnHead(relLabel + "_" + eLabel, endiPo, labelMapcolHeader);
			}
			}
		}
		// 收集关系查询语句
		StringBuilder stringBuilder = new StringBuilder();
		// stringBuilder.append("match(a:"+label+"),");
		stringBuilder.append("match ");
		List<String> relMatchList = new ArrayList<>();
		Map<String, Integer> lnMap = new HashMap<>();
		List<String> relNodeList = new ArrayList<>(vo.keySet().size());

		for (String key : vo.keySet()) {
			String relnodeLabel = key;
			keyList.add(key);
			if (key.indexOf(PO_PROP_SPLITER) > 0) {
			relnodeLabel = key.split(PO_PROP_SPLITER)[0];
			relNodeList.add(relnodeLabel);
			}
		}
		int i = 0;
		for (Map<String, Object> ri : relationDefineList) {
			Object endLabel = ri.get(END_LABEL);
			Object relLabelObj = ri.get(RELATION_LABEL);
			Object startLabelObj = ri.get(START_LABEL);
			if (endLabel != null) {
			String eLabel = String.valueOf(endLabel);
			String relLabel = String.valueOf(relLabelObj);
			Map<String, Object> endiPo = neo4jService.getAttMapBy(LABEL, eLabel, META_DATA);
			String relNodeKey = relLabel + "_" + eLabel;
			if (relNodeList.contains(relNodeKey)) {
				if (endiPo != null && !endiPo.isEmpty()) {
				relMatchList.add(
					"(a:" + startLabelObj + ")-[r" + i + ":" + relLabel + "]->(e" + i + ":" + eLabel + ")");
				lnMap.put(relNodeKey, i);
				}
				i++;
			}
			}
		}
		if(!relMatchList.isEmpty()) {
			stringBuilder.append(String.join(",", relMatchList));
		}else {
			stringBuilder.append(" (a:"+label+") ");
		}

		List<String> returnList = new ArrayList<>(vo.keySet().size());
		for (String key : keyList) {
			String relnodeLabel = key;
			if (key.indexOf(PO_PROP_SPLITER) > 0) {
				relnodeLabel = key.split(PO_PROP_SPLITER)[0];
			}
			String mName = labelName.get(relnodeLabel);
			if (mName == null || relnodeLabel.indexOf("_") > 0) {
				String[] split = relnodeLabel.split("_");
				mName = labelName.get(split[split.length - 1]);
			}
			Integer num = lnMap.get(relnodeLabel);

			String fieldCode = key.split(PO_PROP_SPLITER)[1];
			Map<String, String> col2Head = labelMapcolHeader.get(relnodeLabel);
			if (col2Head != null && !col2Head.isEmpty()) {
				String colName = col2Head.get(fieldCode);
				if (colName != null && !"".equals(colName.trim())) {
					if(!colName.startsWith(mName)) {
						columnHeaderMap.put(key,  mName+colName);
					}else {
						columnHeaderMap.put(key,  colName);
					}

					if (num == null) {
						returnList.add("a." + fieldCode+" AS "+fieldCode);
					} else {
						returnList.add("e" + num + "." + fieldCode+" AS "+fieldCode);
					}
				}
			}
		}
		if (returnList != null && !returnList.isEmpty()) {
			stringBuilder.append(" return " + String.join(",", returnList));
		}

		List<String> columnList = new ArrayList<>();
		List<String> headerList = new ArrayList<>();
		for (String ci : keyList) {
			columnList.add(ci.replace(PO_PROP_SPLITER, "."));
			headerList.add(columnHeaderMap.get(ci));
		}
		Map<String, Object> vMap = new HashMap<>();
		String columnsStr = String.join(",", columnList);
		vMap.put(COLUMNS, columnsStr);
		vMap.put(VO_COLUMN, String.join(",", returnList));
		vMap.put(HEADER, String.join(",", headerList));
		crudUtil.clearColumnOrHeader(vo);
		vMap.put("cypher", stringBuilder.toString());
		rule.validRule(label, vMap, mainPoInfo);
		Node saveByKey = neo4jService.saveByKey(vMap, "Vo", "id");
		return ResultWrapper.wrapResult(true, saveByKey.getId(), null, SAVE_SUCCESS);
    }
    

    /**
     * 映射主实体的字段与表头
     * 
     * @param label
     * @param mainPo
     * @param columnMap
     * @return
     */
    private Map<String, String> label2ColumnHead(String label, Map<String, Object> mainPo,
	    Map<String, Map<String, String>> columnMap) {
	String retColumns = String.valueOf(mainPo.get("columns"));
	String header = String.valueOf(mainPo.get("header"));
	String[] columnArray = retColumns.split(",");
	String[] headers = StringGet.split(header);
	// 主实体字段映射
	Map<String, String> mainPofieldMap = new HashMap<>();
	for (int i = 0; i < headers.length; i++) {
	    mainPofieldMap.put(columnArray[i], headers[i]);
	}
	columnMap.put(label, mainPofieldMap);
	return mainPofieldMap;
    }

    /**
     * 视图对象更新
     * 
     * @param vo
     * @param request
     * @return
     */
    @RequestMapping("/update")
    public WrappedResult update(@RequestBody JSONObject vo, HttpServletRequest request,@PathVariable("po") String label) throws DefineException {
		LabelValidator.validateLabel(label);
	if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
	    return ResultWrapper.wrapResult(true, null, null, UPDATE_FAILED);
	}
	neo4jService.update(vo, VIEW, VIEW_KEY.split(","));
	return ResultWrapper.wrapResult(true, null, null, UPDATE_SUCCESS);
    }

    /**
     * 视图对象删除
     * 
     * @param vo
     * @return
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult del(@RequestBody JSONObject vo,@PathVariable("po") String label) throws DefineException {
		LabelValidator.validateLabel(label);
	if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
	    return ResultWrapper.wrapResult(true, null, null, DELETE_FAILED);
	}
	String delObj = Neo4jOptCypher.delObj(vo, VIEW);

	delObj = delObj.replaceAll("\"\"", "\"");
	neo4jService.execute(delObj);
	return ResultWrapper.wrapResult(true, null, null, DELETE_SUCCESS);
    }

}

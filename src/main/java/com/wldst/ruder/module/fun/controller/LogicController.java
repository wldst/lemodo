package com.wldst.ruder.module.fun.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.ModelUtil;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson2.JSONArray;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.domain.StepDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.state.service.StepShowService;
import com.wldst.ruder.util.CrudUtil;

/**
 * 逻辑模型控制器： 根据概设中的逻辑模型。梳理概念。活动等信息。
 * 
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/logic")
public class LogicController extends StepDomain {

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private StepShowService stepShowService;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private DbInfoService dbInfoGather;

    /**
     * 查询活动步骤
     * 
     * @param model
     * @param bizCode
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{biCode}", method = { RequestMethod.GET, RequestMethod.POST })
    public String instance(Model model, @PathVariable("biCode") String bizCode, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> tableMap = neo4jService.getAttMapBy(LABEL, STEP, META_DATA);
	if (tableMap == null || tableMap.isEmpty()) {
	    throw new DefineException("STEP未定义！");
	}

	Map<String, Object> bizMap = neo4jService.getAttMapBy(LABEL, bizCode, BIZ_ACTIVITY);
	Map<String, Object> stepMap = neo4jService.getAttMapBy(LABEL, bizCode, STEP);
	if (stepMap == null || stepMap.isEmpty()) {
	    throw new DefineException(bizCode + "未定义！");
	}

	if (stepMap.get(NAME) == null) {
	    stepMap.put("name", bizCode);
	}
	ModelUtil.setKeyValue(model, stepMap);
	// 根据事务，获取步骤列表。
	// 每个步骤关联表单。获取表单列表。
	// 添加表单中的列表显示
	// 每个表单中添加按钮。步骤关联按钮。
	// stepShowService.
	// 组装表单列表。添加上下步按钮。首尾步骤按钮。
	// stepShowService.table2(model, tableMap, true);

	return "layui/step";
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
    public String activityStep(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("endLabel") String endLabel, HttpServletRequest request) throws Exception {

	Map<String, Object> endPo = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);

	ModelUtil.setKeyValue(model, endPo);

	Map<String, Object> poMap = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
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
		fieldValidate(model, endPo, columnMapList);
	    } else {
		field(model, endPo, columnMapList);
	    }
	} else {
	    table2(model, endPo);
	}

	return "poField";
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

    private void field(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	showService.field(model, po, columnMapList,true);
    }

    private void fieldValidate(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList) {
	showService.fieldValidate(model, po, columnMapList,false);
    }

}

package com.wldst.ruder.crud.controller;

import java.util.Map;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.LayUIDomain;
import com.wldst.ruder.domain.SystemDomain;
import com.wldst.ruder.exception.DefineException;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
public abstract class BaseLayuiController extends SystemDomain {

	@Autowired
	private HtmlShowService showService;
	@Autowired
	private CrudNeo4jService neo4jService;
	
	protected void tableToolBtn(Model model, String label) {
		showService.tableToolBtn(model,label,false);
	}

	
	
	protected void addRemoveBtnToList(StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic) {
		Map<String, Object> removeBtnMap = neo4jService.getAttMapBy(NODE_CODE, "removeBtn", LayUIDomain.LAYUI_TABLE_TOOL_BTN);
		opt.append(removeBtnMap.get("Html"));
		Object object = removeBtnMap.get("JavaScript");
		if(object!=null) {
			toolFun.append(object);
		}
		activLogic.append(removeBtnMap.get("btnAcitive"));
	}

	protected void addRemoveRelBtnToList(StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic) {
		Map<String, Object> removeBtnMap = neo4jService.getAttMapBy(NODE_CODE, "removeRelBtn", LayUIDomain.LAYUI_TABLE_TOOL_BTN);
		opt.append(removeBtnMap.get("Html"));
		Object object = removeBtnMap.get("JavaScript");
		if(object!=null) {
			toolFun.append(object);
		}
		activLogic.append(removeBtnMap.get("btnAcitive"));
	}

	/**
	 * @Describe:列表数据展现
	 * @param model
	 * @param po
	 * @throws DefineException
	 */
	protected void table2(Model model, Map<String, Object> po) {
		showService.showMetaInstanceCrudPage(model, po, false);
	}

}

package com.wldst.ruder.module.manage;

import java.util.Map;

import com.wldst.ruder.util.ModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.crud.service.TabListShowService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/module")
public class ModuleManageController extends MapTool{
	@Autowired
	private CrudNeo4jService neo4jService;
	@Autowired
	private HtmlShowService showService;
	@Autowired
	private TabListShowService tabService;
	
	@RequestMapping(value = "/{module}/tabList", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public WrappedResult tabList(Model model, @PathVariable("module") String label, @RequestBody JSONObject vo,
			HttpServletRequest request) throws Exception {
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, MODULE);
		if (po == null || po.isEmpty()) {
			throw new DefineException(label + "未定义！");
		}

		Map<String, Object> tabList = tabService.modulePoList(label);
		return ResultWrapper.wrapResult(true, tabList, null, QUERY_SUCCESS);
	}


	@RequestMapping(value = "/{module}", method = { RequestMethod.GET, RequestMethod.POST })
	public String instance(Model model, @PathVariable("module") String label, HttpServletRequest request) throws Exception {
		Map<String, Object> module = neo4jService.getAttMapBy(LABEL, label, MODULE);
		if (module == null || module.isEmpty()) {
		    throw new DefineException(label + "模块未定义！");
		}
		showService.module(model);
		ModelUtil.setKeyValue(model, module);
//		st.createHtml("layui/"+label, model.asMap());
		return "layui/module";
	}
	
	@RequestMapping(value = "/{module}Div", method = { RequestMethod.GET, RequestMethod.POST })
	public String instanceDiv(Model model, @PathVariable("module") String label, HttpServletRequest request) throws Exception {
		Map<String, Object> module = neo4jService.getAttMapBy(LABEL, label, MODULE);
		if (module == null || module.isEmpty()) {
			throw new DefineException(label + "未定义！");
		}
		showService.module(model);
		ModelUtil.setKeyValue(model, module);
//		st.createHtml("div/"+label+"Div", model.asMap());
		return "div/moduleDiv";
	}

}

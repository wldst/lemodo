package com.wldst.ruder.module.fun.controller;

import com.wldst.ruder.util.ModelUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.controller.BaseLayuiController;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.exception.DefineException;

/**
 * 
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/calendar")
public class CalendarController extends BaseLayuiController {
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;

    @RequestMapping(value = "/schedule", method = { RequestMethod.GET, RequestMethod.POST })
    public String schedule(Model model, String table, HttpServletRequest request) throws Exception {
	return "layui/calendar/index";
    }

    @RequestMapping(value = "/demo", method = { RequestMethod.GET, RequestMethod.POST })
    public String demo(Model model, String table, HttpServletRequest request) throws Exception {
	return "layui/calendar/demo";
    }
    
    @RequestMapping(value = "/meta/{label}", method = { RequestMethod.GET, RequestMethod.POST })
    public String metaData(Model model, @PathVariable("label") String label, HttpServletRequest request) throws Exception {
	Map<String, Object> mo = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (mo == null || mo.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, mo);
	return "layui/calendar/metaData";
    }

}

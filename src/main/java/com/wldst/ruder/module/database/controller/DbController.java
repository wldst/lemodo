package com.wldst.ruder.module.database.controller;

import java.util.List;
import java.util.Map;

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

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.module.database.DbServiceImpl;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.MsgContext;

import jakarta.servlet.http.HttpServletRequest;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/db/")
public class DbController extends DataBaseDomain{
    final static Logger logger = LoggerFactory.getLogger(DbController.class);
    @Autowired
    private DbInfoService gather;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    
    @ResponseBody
    @RequestMapping(value = "/{dsId}", method = { RequestMethod.GET, RequestMethod.POST })
    public MsgContext sync(Model model, @PathVariable("dsId") String dsId, HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, LABLE_DS, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(LABLE_DS + "未定义！");
	}
	//数据源
	Map<String, Object> propMapBy = neo4jService.getPropMapBy(dsId);
	DbServiceImpl gather = new DbServiceImpl(propMapBy);
	List<Map<String, Object>> work = gather.work();
	for(Map<String, Object> table: work) {
	    if(!table.isEmpty()) {
		String replaceAll = MapTool.string(table, HEADER).replaceAll(",", "");
		if(replaceAll.trim().isBlank()) {
		    table.put(HEADER, table.get(COLUMNS));
		}
		neo4jService.saveByBody(table, LABLE_TABLE);
	    }
	}	
	return MsgContext.success();
    }
    @ResponseBody
    @RequestMapping(value = "/connect/{dsId}", method = { RequestMethod.GET, RequestMethod.POST })
    public MsgContext connect(Model model, @PathVariable("dsId") String dsId, HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, LABLE_DS, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(LABLE_DS + "未定义！");
	}
	//数据源
	Map<String, Object> propMapBy = neo4jService.getPropMapBy(dsId);
	gather.initConnect(propMapBy);
	if(gather.getCon()!=null&&gather.getDsId().equals(Long.valueOf(dsId))) {
	    return MsgContext.success("数据库链接成功");
	}
	return MsgContext.failed("数据库链接失败");
    }
    
    @ResponseBody
    @RequestMapping(value = "/testConnect", method = { RequestMethod.GET, RequestMethod.POST })
    public MsgContext testConnect(Model model, @RequestBody Map<String,Object> data, HttpServletRequest request) throws Exception {
	
	gather.initConnect(data);
	if(gather.getCon()!=null) {
	    return MsgContext.success("数据库链接成功");
	}
	return MsgContext.failed("数据库链接失败");
    }
    
    @ResponseBody
    @RequestMapping(value = "/copy/{tableName}", method = { RequestMethod.GET, RequestMethod.POST })
    public MsgContext copy(Model model, @PathVariable("tableName") String tableName, HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, LABLE_TABLE, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(LABLE_DS + "未定义！");
	}
	//数据源
	Map<String, Object> propMapBy = neo4jService.getPropMapBy(tableName);
	DbServiceImpl gather = new DbServiceImpl(propMapBy);
	gather.copyTableInfo(tableName);	
	return MsgContext.success();
    }

}

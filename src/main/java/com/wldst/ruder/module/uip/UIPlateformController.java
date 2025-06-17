package com.wldst.ruder.module.uip;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.qliu6.FileOperate;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.FileDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.ServiceException;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("${server.context}/ui")
public class UIPlateformController extends FileDomain {
    final static Logger logger = LoggerFactory.getLogger(UIPlateformController.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private Neo4jOptByUser optByUserSevice;

    @RequestMapping(value = "/po", method = { RequestMethod.GET, RequestMethod.POST })
    public String po(Model model, String table, HttpServletRequest request) throws Exception {
	return "layui/po";
    }
    
    @RequestMapping(value = "/publish/{uId}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Result<String> publish(@PathVariable("uId") String uId, @RequestBody JSONObject vo) throws DefineException, ServiceException {
	if (StringUtils.isEmpty(uId)) {
		throw new ServiceException("参数有误！");
	    }
	Map<String, Object> uiPlugin = neo4jService.getNodeMapById(Long.valueOf(uId));
	if (uiPlugin == null || uiPlugin.isEmpty()) {
	   throw new DefineException("资源"+uId + "不存在！");
	}
	
	String targetPath = neo4jService.getPathBy(UI_PUBLISH_PATH) + code(uiPlugin);
	Long fileId = longValue(uiPlugin,  "file");	
	Map<String, Object> fileInfo = neo4jService.getNodeMapById(fileId);
	String fileStorePath = string(fileInfo,  FILE_STORE_NAME);
	String tempPath = tempFile()+File.separator+code(uiPlugin)+name(fileInfo);
	FileOperate.copyFile(fileStorePath, tempPath);
	
	if(isFileExist(tempPath)) {
	   delete(targetPath);	     
	    unzip(targetPath,tempPath);
	    delete(tempPath);
	}
	return Result.successMsg("发布成功");
    }

}

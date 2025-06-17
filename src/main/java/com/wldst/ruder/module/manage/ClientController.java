package com.wldst.ruder.module.manage;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.RestApi;
import com.wldst.ruder.util.TextUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 后台用户管理 Created by macro on 2018/4/26.
 */
@Controller
@RequestMapping("${server.context}/client")
public class ClientController extends AuthDomain {
    private static Logger logger = LoggerFactory.getLogger(ClientController.class);
    @Value("${server.port}")
    private String serverPort;
	@Autowired
	private CrudNeo4jService neo4jService;
    
    @Autowired
    private RestApi restApi;
    
    /**
     * 客户端启动
     * @param clientInfo
     * @param request
     * @return
     * @throws DefineException 
     */
    @RequestMapping(value = "/report/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result report(@Validated @PathVariable("id") String id, @RequestBody JSONObject clientInfo, HttpServletRequest request) throws DefineException {
	
	if(clientInfo==null||clientInfo.isEmpty()) {
	    return Result.failed("参数缺失！");
	}
	Map<String, Object> data = neo4jService.getLablePropBy(id);
	
	if (data == null || data.isEmpty()) {
	   throw new DefineException("资源"+id + "不存在！");
	}
	String label2 = label(data);
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(LABEL, label2, META_DATA);
	
	Map<String, Object> reportResult = restApi.report(data,attMapBy);
	LoggerTool.info(logger,"client report"+mapString(data)+"\n ===============return======="+mapString(reportResult));
	 return Result.success("上线成功");
    }
   
    
    /**
     * 客户端启动,上传数据
     * @param clientInfo
     * @param request
     * @return
     * @throws DefineException 
     */
    @RequestMapping(value = "/reportData/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result<String> reportData(@Validated @PathVariable("id") String id, @RequestBody JSONObject clientInfo, HttpServletRequest request) throws DefineException {
	
	if(clientInfo==null||clientInfo.isEmpty()) {
	    return Result.failed("参数缺失！");
	}
	Map<String, Object> data = neo4jService.getLablePropBy(id);
	
	if (data == null || data.isEmpty()) {
	   throw new DefineException("资源"+id + "不存在！");
	}
	String label2 = label(data);
	List<Map<String, Object>> dataList = neo4jService.listAllByLabel(label2);
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(LABEL, label2, META_DATA);
	 
	Map<String, Object> reportResult = restApi.reportData(dataList,attMapBy);
	LoggerTool.info(logger,"client report"+mapString(data)+"\n ===============return======="+mapString(reportResult));
	 return Result.success("上线成功");
    }
    
    
    /**
     * 客户端启动
     * @param clientInfo
     * @param request
     * @return
     * @throws DefineException 
     */
    @RequestMapping(value = "/share/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result share(@Validated @PathVariable("id") String id, @RequestBody JSONObject clientInfo, HttpServletRequest request) throws DefineException {
	
	if(clientInfo==null||clientInfo.isEmpty()) {
	    return Result.failed("参数缺失！");
	}
	Map<String, Object> data = neo4jService.getLablePropBy(id);
	
	if (data == null || data.isEmpty()) {
	   throw new DefineException("资源"+id + "不存在！");
	}
	String label2 = label(data);
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(LABEL, label2, META_DATA);
	String serverUrl = string(clientInfo,"serverUrl");
	Map<String, Object> reportResult = restApi.reportX(data,attMapBy,serverUrl);
	LoggerTool.info(logger,"client report"+mapString(data)+"\n ===============return======="+mapString(reportResult));
	 return Result.success("上线成功");
    }
    
    
    @RequestMapping(value = "/shareModule", method = RequestMethod.POST)
    @ResponseBody
    public Result shareModule(@RequestBody Map<String,Object> vo, HttpServletRequest request) throws DefineException {
	if(vo==null||vo.isEmpty()) {
	    return Result.failed("参数缺失！");
	}
	Long moduleLabel = longValue(vo,"module");
	String cypher="MATCH (n:module)-[r:moduleMeta]->(m:MetaData) where id(n)="+moduleLabel+" return m";
	List<Map<String, Object>> queryByCypher = neo4jService.cypher(cypher);
	for (Map<String, Object> di : queryByCypher) {
	    String label2 = label(di);
	    Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label2, META_DATA);
	    String serverUrl = string(vo, "serverUrl");
	    restApi.setServerUrl(serverUrl);
//	     Map<String, Object> reportResult = restApi.reportX(di,md);
//	     LoggerTool.info(logger,"client report"+mapString(md)+"\n"
//		     	+ " ===============return======="+mapString(reportResult));
	    List<Map<String, Object>> dataList = neo4jService.listAllByLabel(label2);
	    Map<String, Object> ddd = restApi.reportDataX(dataList, md,serverUrl);
	     LoggerTool.info(logger,"client report"+mapString(md)+"\n"
	     	+ " ===============return======="+mapString(ddd));
	}
	 return Result.success("上线成功");
    }
    
    @RequestMapping(value = "/shareMeta", method = RequestMethod.POST)
    @ResponseBody
    public Result shareMeta(@RequestBody Map<String,Object> vo, HttpServletRequest request) throws DefineException {
	if(vo==null||vo.isEmpty()||id(vo)==null) {
	    return Result.failed("参数缺失！");
	}
	String cypher=null;
	try {
	    Long moduleLabel = longValue(vo,"meta");
	    cypher="MATCH  (m:MetaData) where id(m)="+moduleLabel+" return m";
	}catch(Exception e) {
	    String moduleLabel = string(vo,"meta");
	    if(TextUtil.isChinese(moduleLabel)) {
		cypher="MATCH  (m:MetaData) where  m.name='"+moduleLabel+"' return m";
	    }else {
		cypher="MATCH  (m:MetaData) where m.label='"+moduleLabel+"' return m";
	    }
	}
	List<Map<String, Object>> queryByCypher =null;
	try {
	 queryByCypher = neo4jService.cypher(cypher);
	}catch (Exception e) {
	    String moduleLabel = string(vo,"meta");
	    cypher="MATCH  (m:MetaData) where m.name='"+moduleLabel+"' return m";
	    queryByCypher = neo4jService.cypher(cypher);
	}
	 
	for (Map<String, Object> di : queryByCypher) {
	    String serverUrl = string(vo, "serverUrl");
	    List<Map<String, Object>> dataList = neo4jService.listAllByLabel(label(di));
	    Map<String, Object> ddd = restApi.reportDataX(dataList, di,serverUrl);
	     LoggerTool.info(logger,"client report"+mapString(di)+"\n"
	     	+ " ===============return======="+mapString(ddd));
	}
	 return Result.success("上线成功");
    }
   
    
    /**
     * 客户端启动,上传数据
     * @param clientInfo
     * @param request
     * @return
     * @throws DefineException 
     */
    @RequestMapping(value = "/shareData/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result<String> shareData(@Validated @PathVariable("id") String id, @RequestBody JSONObject clientInfo, HttpServletRequest request) throws DefineException {
	
	if(clientInfo==null||clientInfo.isEmpty()) {
	    return Result.failed("参数缺失！");
	}
	Map<String, Object> data = neo4jService.getLablePropBy(id);
	
	if (data == null || data.isEmpty()) {
	   throw new DefineException("资源"+id + "不存在！");
	}
	String label2 = label(data);
	List<Map<String, Object>> dataList = neo4jService.listAllByLabel(label2);
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(LABEL, label2, META_DATA);
	String serverUrl = string(clientInfo,"serverUrl");
	Map<String, Object> reportResult = restApi.reportDataX(dataList,attMapBy,serverUrl);
	LoggerTool.info(logger,"client report"+mapString(data)+"\n ===============return======="+mapString(reportResult));
	return Result.success("上线成功");
    }
    
}

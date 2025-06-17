package com.wldst.ruder.module.manage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
//
//import com.wldst.ruder.test.EndpointLogger;
import com.wldst.ruder.util.*;
import org.apache.commons.lang3.StringUtils;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.ws.ManageClientWebSocket;
import com.wldst.ruder.module.ws.handler.ServerHandler;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 后台用户管理 Created by macro on 2018/4/26.
 */
@Controller
@RequestMapping("${server.context}/server")
public class ServerController extends AuthDomain {
    private static Logger logger = LoggerFactory.getLogger(ServerController.class);
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private RestApi rest;
    @Autowired
    private ManageClientWebSocket mcWebSocket;
    @Autowired
    private ServerHandler serverHandler;
//
//	@Autowired
//	EndpointLogger endpointLogger;
    private static ExecutorService exec=getExecutorService();
    
    /**
     * 客户端启动
     * @param clientInfo
     * @param request
     * @return
     */
    @RequestMapping(value = "/clientUp", method = RequestMethod.POST)
    @ResponseBody
    public Result clientUp(@Validated @RequestBody JSONObject clientInfo, HttpServletRequest request) {
	
	if(clientInfo==null||clientInfo.isEmpty()) {
	    return Result.failed("参数缺失！");
	}
	//保存启动数据
	neo4jService.save(clientInfo, LABEL_CLIENT_LOG);
	
	String[] columns;
	try {
	    columns = crudUtil.getMdColumns(LABEL_CLIENT);
	    Map<String,String> getParams = new HashMap<>();
	    for(String ci:columns) {
		String string = string(clientInfo,ci);
		if(string!=null&&!ID.equals(ci)) {
		    getParams.put(ci, string);
		}		
	     }
	    if(getParams.size()<2) {
		return Result.failed("参数缺失！");
	    }
	    
	} catch (DefineException e) {
	    e.printStackTrace();
	}
	
	List<Map<String, Object>> queryBy = neo4jService.queryBy(clientInfo, LABEL_CLIENT);
	if(queryBy==null||queryBy.isEmpty()) {
	    //提示用户端注册一下，在用户端弹出注册界面。注册好过后，填写挂载链接，去注册一下
	    clientInfo.put("new", "true");
	}
	
	neo4jService.save(clientInfo, LABEL_CLIENT);
	 return Result.success("上线成功");
    }
    
    @RequestMapping(value = "/say", method = RequestMethod.POST)
    @ResponseBody
    public Result serverSay(@Validated @RequestBody JSONObject cmdInfo, HttpServletRequest request) {
	String cmd=string(cmdInfo,CMD);
	serverHandler.handleIt(cmdInfo);
	
	String[] columns;
	try {
	    columns = crudUtil.getMdColumns(LABEL_CLIENT);
	    Map<String,String> getParams = new HashMap<>();
	    for(String ci:columns) {
		String string = string(cmdInfo,ci);
		if(string!=null&&!ID.equals(ci)) {
		    getParams.put(ci, string);
		}
	     }
	    if(getParams.size()<2) {
		return Result.failed("参数缺失！");
	    }
	} catch (DefineException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	List<Map<String, Object>> queryBy = neo4jService.queryBy(cmdInfo, LABEL_CLIENT);
	if(queryBy==null||queryBy.isEmpty()) {
	    cmdInfo.put("", queryBy);
	}
	
	String macAddress = string(cmdInfo,MAC_ADDRESS);
	
	 return Result.success("发送成功");
    }
    
    @RequestMapping(value = "/share", method = RequestMethod.POST)
    @ResponseBody
    public Result uploadShare(@Validated @RequestBody JSONObject cmdInfo, HttpServletRequest request) {
	
//	Interpreter in = new Interpreter();
//	in.set("vo", vo);
//	in.set("label", string(vo, LABEL));
//	String daima = code(vo);
//	if(daima!=null) {
//	    in.set("code",daima);
//	}
//	in.set(NAME, string(vo, NAME));
//	in.set("so", so);
//	in.set("domain", domain);
//	in.set("logic", logic);
//	// in.setStrictJava(true);
//	String string = string(attMapBy, BS_SCRIPT);
//	in.eval(string);
	
	return serverHandler.handleIt(cmdInfo);
    }
    
    
    @RequestMapping(value = "/publish/{noticeId}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Result<String> publish(@PathVariable("noticeId") String noticeId, @RequestBody JSONObject vo) throws DefineException, ServiceException {
	if (StringUtils.isEmpty(noticeId)) {
		throw new ServiceException("参数有误！");
	    }
	Map<String, Object> notice = neo4jService.getNodeMapById(Long.valueOf(noticeId));
	if (notice == null || notice.isEmpty()) {
	   throw new DefineException("资源"+noticeId + "不存在！");
	}
	List<Map<String,Object>> ret = new ArrayList<>();
	//获取用户和客户端数据，进行发布公告
	List<Map<String, Object>> clients = neo4jService.listAllByLabel(LABEL_CLIENT);
	int clientNum=0;
	for(Map<String, Object> ci: clients) {
	    String clientIp = string(ci,IP_ADDRESS);	
	    if(clientIp.startsWith("192.")||clientIp.startsWith("10.")||clientIp.startsWith("27.")) {
		continue;
	    }
	    //telnet
	    String port = string(ci,CLIENT_PORT);
	    if(TelnetUtil.telnet(clientIp, Integer.valueOf(port), 2000)) {
		Callable<Map<String, Object>> callabel = () -> {
			String httpCLient = "http://"+clientIp+":"+port+LemodoApplication.MODULE_NAME+"//server/say";
			return sayNotice(notice, httpCLient);
		     };
		     Map<String,Object> list = vtResult(callabel);
			    if(list!=null) {
				ret.add(list);
			    }
	    };
	    clientNum++;
	    String httpsPort = string(ci,CLIENT_HTTPS_PORT); 
	    if(TelnetUtil.telnet(clientIp, Integer.valueOf(httpsPort), 2000)) {
		Callable<Map<String, Object>> httpsSay = () -> {
			
			String httpsCLient = "https://"+clientIp+":"+httpsPort+LemodoApplication.MODULE_NAME+"//server/say";
			return sayNotice(notice, httpsCLient);
		    };
		    Map<String,Object> callRet = vtResult(httpsSay);
		    if(callRet!=null) {
			ret.add(callRet);
		    }
	    }
	}
	if(clientNum<clients.size()) {
	    //通过websocket 发送
	    Map<String,Object> data = new HashMap<>();
	    data.put("data", notice);
	    data.put(CMD, "publishNotice");
	    try {
		mcWebSocket.broadcast(mapString(data));
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	return Result.successMsg("发布成功");
    }
    
    @RequestMapping(value = "/push/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result push(@Validated @PathVariable("id") String id, @RequestBody JSONObject clientInfo, HttpServletRequest request) throws DefineException {
	
	if(clientInfo==null||clientInfo.isEmpty()) {
	    return Result.failed("参数缺失！");
	}
	Map<String, Object> notice = neo4jService.getLablePropBy(id);
	
	if (notice == null || notice.isEmpty()) {
	   throw new DefineException("资源"+id + "不存在！");
	}
	String label2 = label(notice);
	Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, label2, META_DATA);
	Map<String,Object> msg = new HashMap<>();
	msg.put(CMD, CMD_PUSH);
	msg.put(META_DATA,metaData);
	msg.put(DATA,notice);
//	Map<String, Object> nodeMapById = neo4jService.getNodeMapById(id(clientInfo));
//	//拼装数据
//	msg.put("target",clientInfo);
	try {
	    mcWebSocket.broadcast(JSON.toJSONString(msg));
	} catch (IOException e) {
	    e.printStackTrace();
	    LoggerTool.error(logger,e.getMessage(),e);
	}
//	Map<String, Object> reportResult = restApi.report(notice,attMapBy);
	LoggerTool.info(logger,"server push report"+mapString(notice));
	 return Result.success("上线成功");
    }

    private Map<String,Object> sayNotice(Map<String, Object> notice, String httpCLient) {
	Map<String,Object> sayInfo = new HashMap<>();
	sayInfo.put("cmd", CMD_NOTICE);
	sayInfo.put(LABEL_NOTICE, notice);
	return rest.postForObject(httpCLient,sayInfo,Map.class);
    }

    
    private <T> T vtResult(Callable<T> callabel) {
	Future<T> submit = exec.submit(callabel);
	T list = null;
	try {
	    list = submit.get();
	} catch (InterruptedException | ExecutionException e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	}
	return list;
    }
    
    @RequestMapping(value = "/getPort", method = RequestMethod.GET)
    @ResponseBody
    public String getPort() {
	return serverPort;
    }


	@RequestMapping(value = "/refreshEndPoint", method = RequestMethod.GET)
	@ResponseBody
	public String refreshEndPoint() throws Exception{
//		endpointLogger.updateEndpint();
		return "";
	}
}

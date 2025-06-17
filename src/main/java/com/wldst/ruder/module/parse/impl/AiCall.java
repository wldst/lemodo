package com.wldst.ruder.module.parse.impl;

import java.util.Map;

import com.wldst.ruder.LemodoApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.NetHandleUtil;
import com.wldst.ruder.util.RestApi;

/**
 * 
 *
 * @return
 */
@Component
public class AiCall extends ParseExcuteDomain implements MsgProcess {

    final static Logger logger = LoggerFactory.getLogger(AiCall.class);
    @Autowired
    private RestApi restApi;
    @Value("${http.port}")
    private Integer port;
    /**
     * 通过本地设置的GPT信息，来读取以ai:开头的问答。并调用AI
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "AICall")
    public Object process(String msg, Map<String, Object> context) {
	// 并行概率执行
	String chatPrefix = "ai:";
	if (!msg.startsWith(chatPrefix)) {
	    return null;
	}
	context.put(USED, true);
	msg = msg.trim().replaceAll("：", ":");

	msg = msg.split(":")[1];
	Map<String, Object> md = neo4jService.getAttMapBy(NAME, "aiChat", "api");
//	Map<String, Object> md = neo4jService.getAttMapBy(NAME, "aiChat-local", "api");
	String params = string(md, "params");
	String serverUrl = string(md, "url");
	restApi.setServerUrl(serverUrl);
	JSONObject parseObject = JSON.parseObject(params);
	parseObject.put("myId", string(context, "MyId"));
	parseObject.put("text", msg);
	parseObject.put("clientAPI","http://"+NetHandleUtil.getLocalIpAddress()+":"+port+ LemodoApplication.MODULE_NAME+"//aiclient/answer");
	Map postApi = restApi.postApi(parseObject, Map.class);

	return string(postApi, DATA);
    }
    
}

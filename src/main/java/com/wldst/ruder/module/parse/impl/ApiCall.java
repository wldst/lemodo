package com.wldst.ruder.module.parse.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.RestApi;

/**
 * 
 * 
 * @param msg
 * @return
 */
@Component
public class ApiCall extends ParseExcuteDomain implements MsgProcess {

    final static Logger logger = LoggerFactory.getLogger(ApiCall.class);
    @Autowired
    private RestApi restApi;

    /**
     *  调用接口
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "ApiCall")
    public Object process(String msg, Map<String, Object> context) {
	// 并行概率执行
	String chatPrefix = "API:";
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
	Map postApi = restApi.postApi(parseObject, Map.class);

	return string(postApi, DATA);
    }
}

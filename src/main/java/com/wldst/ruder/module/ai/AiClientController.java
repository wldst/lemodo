package com.wldst.ruder.module.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.domain.GoodsDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.ws.web.ContextServer;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

/**
 * 主要是接收AI服务器发来的消息，然后将相关消息发送给相关的会话中，websocket，显示到Web页面上。
 * 
 * @author wldst
 *
 */
@RestController
@RequestMapping("${server.context}/aiclient")
public class AiClientController extends GoodsDomain {
    final static Logger logger = LoggerFactory.getLogger(AiClientController.class);
    
    @RequestMapping(value = "/answer", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult answer(@RequestBody JSONObject vo) throws DefineException {
	String myId=string(vo,"myId");
	String answer=string(vo,"answer");
	ContextServer.sendInfo(answer, myId);
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }
 
}

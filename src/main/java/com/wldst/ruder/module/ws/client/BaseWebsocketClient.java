package com.wldst.ruder.module.ws.client;


import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

// import com.alibaba.fastjson.JSONObject;
import com.wldst.ruder.util.LoggerTool;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wldst.ruder.module.ws.handler.ClientHandler;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.NetHandleUtil;
import com.wldst.ruder.util.RestApi;

/**
 *  websocket客户端监听类
 * @author 。
 */
public class BaseWebsocketClient extends WebSocketClient {
    private RestApi ruderApi;
    private  ClientHandler handler;
    private static Logger logger = LoggerFactory.getLogger(BaseWebsocketClient.class);
    public BaseWebsocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LoggerTool.info(logger,">>>>>>>>>>>websocket open");

    }

    @Override
    public void onMessage(String s) {
        LoggerTool.info(logger,">>>>>>>>>> websocket message");
        LoggerTool.info(logger,s);
        InetSocketAddress remoteSocketAddress = this.getConnection().getRemoteSocketAddress();
        LoggerTool.info(logger,">>>>>>>>>>"+remoteSocketAddress.getHostName()
        +":"+remoteSocketAddress.getHostString()+",port="+remoteSocketAddress.getPort());
        //target:匹配相应的IP，端口，macAddress，hostName
        
        
        if(handler!=null&&s.startsWith("{")) {
            JSONObject parseObject = JSON.parseObject(s);
            
            String target = MapTool.string(parseObject,"target");
            if(target!=null) {
        	String me;
    	    try {
    		me = NetHandleUtil.getLocalIpAddress()+"|"+
    		NetHandleUtil.getLocalHostName()+"|"+
    		NetHandleUtil.getMacAddress()+"|"+remoteSocketAddress.getPort();
    		if(target.equals(me)) {
    		    handler.handleIt(parseObject);
    		            }
    	    } catch (UnknownHostException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	    }
            }else {
        	handler.handleIt(parseObject);
            }
            
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        LoggerTool.info(logger,">>>>>>>>>>>websocket close");
    }

    @Override
    public void onError(Exception e) {
        LoggerTool.error(logger,">>>>>>>>>websocket error {}",e);
    }

    public RestApi getRuderApi() {
	return ruderApi;
    }

    public void setRuderApi(RestApi ruderApi) {
	this.ruderApi = ruderApi;
    }

    public ClientHandler getHandler() {
        return handler;
    }

    public void setHandler(ClientHandler handler) {
        this.handler = handler;
    }


}


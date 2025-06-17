package com.wldst.ruder.module.ws;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import com.wldst.ruder.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.module.bs.ShellOperator;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.RemoteEndpoint.Basic;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws/talk/system/{sid}")
@Service
public class Talk2SystemHandle {
    public static Logger logger = LoggerFactory.getLogger(Talk2SystemHandle.class);
    //接收sid
    private String sid = "";
    private Process process;
    private InputStream inputStream;
    private static Map<String, Map<String, Object>> sessionProp = new HashMap<>();
    private static Map<String, Talk2SystemHandle> talkHandleMap = new HashMap<>();

    // 用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<Talk2SystemHandle> webSocketSet = new CopyOnWriteArraySet<Talk2SystemHandle>();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private LinuxReaderThread thread;

    /**
     * 新的WebSocket请求
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
	this.session = session;
	InetSocketAddress remoteAddress = WebsocketUtil.getRemoteAddress(session);
	System.out.println("有新连接加入！" + remoteAddress);
	webSocketSet.add(this); // 加入set中
	this.sid=sid+"-session-"+session.getId();
	LoggerTool.info(logger,"有新连接加入！当前在线人数为" + webSocketSet.size());
	 
	for (Talk2SystemHandle hi : webSocketSet) {
	    if (hi.getKey(session).equals(this.sid)) {
		talkHandleMap.put(this.sid, hi);
		sessionProp.put(this.sid, new HashMap<>());
	    }
	}
	this.session.getAsyncRemote().sendText("连接成功");
    }

    private String getKey(Session session) {
	    return this.sid;
    }

    public void sendWSText(String sessionId, String host, String msg) {
	Talk2SystemHandle rlHandle = talkHandleMap.get(sessionId);
	if (rlHandle == null) {
	    System.out.println("sessionId:" + sessionId + ":" + host + "> rlHandle is null" + msg + "<br>");
	    return;
	}
	try {
	    if (rlHandle.getSession() != null) {
		Basic basicRemote = rlHandle.getSession().getBasicRemote();
		if (basicRemote != null) {
		    basicRemote.sendText("主机:" + host + ">" + msg + "<br>");
		}
		LoggerTool.info(logger,"sessionId:" + sessionId + ":" + host + "> and sent" + msg + "<br>");
	    } else {
		LoggerTool.info(logger,"sessionId:" + sessionId + ":" + host + "> session is null" + msg + "<br>");
	    }
	} catch (IOException e) {
	}
    }

    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose() {
	webSocketSet.remove(this); // 从set中删除
	System.out.println("有一连接关闭！当前在线人数为" + webSocketSet.size());
	HostUtil.udpClient("closeTailLog" + CruderConstant.FEN_HAO + session.getId());
	try {
	    if (inputStream != null)
		inputStream.close();
	    if (thread != null && thread.isAlive()) {
		thread.join();
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}

	if (process != null)
	    process.destroy();
    }

    /**
     * 收到客户端消息后调用的方法
     * 
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {

	System.out.println("来自客户端"+this.sid+"的消息o:" + message);
	if (message == null || message.isEmpty()) {
	    this.session.getAsyncRemote().sendText("send empty or null can not be understand：" + webSocketSet.size());
	    return;
	}
	Basic basicRemote = session.getBasicRemote();
	
	if (basicRemote != null) {
	    try {
		basicRemote.sendText("我收到您的消息了，正在处理“"+ MapTool.string(JSON.parseObject(message),"message")+"”。。。");
	    } catch (IOException e) {
		LoggerTool.error(logger,e.getMessage(),e);
	    }
	}
	
//	broadCast(this.sid+"发送的消息："+message);
	if (message.startsWith("close:")) {
	    onClose();
	    return;
	}
	ShellOperator so = SpringUtil.getBean(ShellOperator.class);
	String msgData = "";
	try {
	    JSONObject parseObject = JSON.parseObject(message);
	    msgData = parseObject.getString("message");
	}catch(JSONException ex) {
	    LoggerTool.error(logger,"不是JSON格式的消息",ex);
	    msgData =  message;
	}
	
	String sucess = "已执行";
	if (so != null) {
	    Map<String, Object> parseAndexcute = so.parseAndexcute(msgData,getKey(session));
	    if(parseAndexcute==null||parseAndexcute.isEmpty()) {
		sucess="未查询到相关数据";
	    }else {
		sucess=MapTool.mapHtmlString(parseAndexcute);
	    }
	}
	
	if (basicRemote != null) {
	    try {
		basicRemote.sendText(msgData +"，请求结果："+ sucess);
	    } catch (IOException e) {
		LoggerTool.error(logger,e.getMessage(),e);
	    }
	}
    }

    private void broadCast(String message) {
	for(Talk2SystemHandle ti : webSocketSet) {
	    try {
		ti.sendMessage(message);
	    }catch(Exception e) {
		LoggerTool.error(logger,e.getMessage(),e);
	    }
	}
    }

    @OnError
    public void onError(Throwable thr) {
	thr.printStackTrace();
    }

    public Session getSession() {
	return session;
    }

    public void setSession(Session session) {
	this.session = session;
    }
    
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public static CopyOnWriteArraySet<Talk2SystemHandle> getWebSocketSet() {
        return webSocketSet;
    }

    public static void setWebSocketSet(CopyOnWriteArraySet<Talk2SystemHandle> webSocketSet) {
        Talk2SystemHandle.webSocketSet = webSocketSet;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

}
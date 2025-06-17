package com.wldst.ruder.module.ws;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.util.LoggerTool;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.RemoteEndpoint.Basic;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.util.HostUtil;
import com.wldst.ruder.util.WebsocketUtil;

@ServerEndpoint("/ws/talk/system")
@Service
public class TalkParseHandle extends BeanShellDomain{
    public static Logger logger = LoggerFactory.getLogger(Talk2SystemHandle.class);

    private Process process;
    private InputStream inputStream;
    private static Map<String, Map<String, String>> sessionProp = new HashMap<>();
    private static Map<String, Talk2SystemHandle> sessionMap = new HashMap<>();

    // 用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<Talk2SystemHandle> webSocketSet = new CopyOnWriteArraySet<Talk2SystemHandle>();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private LinuxReaderThread thread;
    @Autowired
    private CrudNeo4jService neo4jService;

    /**
     * 新的WebSocket请求
     */
    @OnOpen
    public void onOpen(Session session) {
	this.session = session;
	InetSocketAddress remoteAddress = WebsocketUtil.getRemoteAddress(session);
	System.out.println("有新连接加入！" + remoteAddress);
//	webSocketSet.add(this); // 加入set中
	LoggerTool.info(logger,"有新连接加入！当前在线人数为" + webSocketSet.size());
	sessionProp.put(session.getId(), new HashMap<>());
	for (Talk2SystemHandle hi : webSocketSet) {
	    if (hi.getSession().getId().equals(session.getId())) {
		sessionMap.put(session.getId(), hi);
	    }
	}

	this.session.getAsyncRemote().sendText("initok");
    }

    public void sendWSText(String sessionId, String host, String msg) {
	Talk2SystemHandle rlHandle = sessionMap.get(sessionId);
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
	System.out.println("来自客户端的消息o:" + message);
	if (message == null || message.isEmpty()) {
	    this.session.getAsyncRemote().sendText("send empty or null can not be understand：" + webSocketSet.size());
	    return;
	}
	if (message.startsWith("Msg:")) {
	    return;
	}
	if (message.startsWith("close:")) {
	    onClose();
	    return;
	}
	if (message.startsWith("host:")) {
	    Map<String, String> map = sessionProp.get(session.getId());
	    map.put("host", message.replace("host:", ""));
	    sessionProp.put(session.getId(), map);
	    return;
	}
	String url = LemodoApplication.MODULE_NAME+"/cruder/Po/query";
	Map<String, Object> data = new HashMap<>();
	data.put("name", message);
	// crudUtil.
	Node findBy = neo4jService.findBy("name", message, "Po");
	Node findByCode = neo4jService.findBy("code", message, "Po");
	// crudUtil.
	// neo4jService.cypher(query);
	if (findBy != null || findByCode != null) {
	     this.session.getAsyncRemote().sendText("<br>查询到定义");
	    Basic basicRemote = session.getBasicRemote();
	    if (basicRemote != null) {
//		 try {
//		
//		 if(dataRetObject instanceof List) {
//		 List<Map<String, String>> dataMap = (List<Map<String, String>>)dataRetObject;
//		// basicRemote.sendText("<br>"+dataMap);
//		 for(Map<String, String> poi: dataMap) {
//		// basicRemote.sendText("<br>"+poi);
//		 Object object = poi.get(LABEL);
//		 Object name = poi.get("name");
//		 String cruderQuery = LemodoApplication.MODULE_NAME+"/ruder/"+object+ "/query";
//		 /*
//		 * String dataQuery = restApi.cruder() +object+ "/query"; Map<String, Object>
//		 * dataLabel =new HashMap<>(); // dataLabel.put("name", message); Map
//		 dataObject
//		 * = restApi.postForObject(dataQuery, dataLabel, Map.class);
//		 * basicRemote.sendText("<br>"+dataObject.get("data"));
//		 */
//		 basicRemote.sendText("<br><a href='javascript:;' onclick=objectManage('"+name+"','"+object+"')>"+name+"</a>");
//		 }
//		 }else {
//		
//		 }
//		
//		
//		 } catch (IOException e) {
//		 // TODO Auto-generated catch block
//		 LoggerTool.error(logger,e.getLocalizedMessage(), e);
//		 }
	    }
	} else {
	    this.session.getAsyncRemote().sendText("<br>无定义");
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
}
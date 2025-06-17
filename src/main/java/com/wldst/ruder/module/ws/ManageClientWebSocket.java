package com.wldst.ruder.module.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.domain.SystemDomain;
import com.wldst.ruder.module.ws.handler.ServerHandler;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;


@ServerEndpoint("/ws/server")
@Component
public class ManageClientWebSocket extends SystemDomain{

    private Process process;
    private InputStream inputStream;
    // 用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<ManageClientWebSocket> webSocketSet = new CopyOnWriteArraySet<ManageClientWebSocket>();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    

    /**
     * 新的WebSocket请求
     */
    @OnOpen
    public void onOpen(Session session) {
	this.session = session;
	webSocketSet.add(this); // 加入set中
	this.session.getAsyncRemote().sendText("恭喜您成功连接上服务器-->当前在线人数为：" + webSocketSet.size());
    }

    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose() {
	webSocketSet.remove(this); // 从set中删除
	System.out.println("有一连接关闭！当前在线人数为" + webSocketSet.size());
	try {
	    if (inputStream != null)
		inputStream.close();
	   
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
	System.out.println("来自客户端的消息:" + message);
	 
	JSONObject clientSay = JSON.parseObject(message);

	String id2 = session.getId();
	clientSay.put("sessionId", id2);
	//记录客户单发送的消息记录：单独回复相应客户端消息。
	
	
	String cmd = string(clientSay, CMD);
	if(cmd.equals("broadcast")) {
	 // 群发消息
		try {
		    broadcast(message);
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
	}else {
	    ServerHandler bean = (ServerHandler) SpringContextUtil.getBean(ServerHandler.class);
	    bean.handleIt(clientSay);
	}
	
    }

    @OnError
    public void onError(Throwable thr) {
	thr.printStackTrace();
    }

    /**
     * 群发自定义消息
     * 
     * @throws IOException
     */
    public void broadcast(String message) throws IOException {
	if(this.session!=null) {
	    this.session.getBasicRemote().sendText(message);
	}
	for (ManageClientWebSocket item : webSocketSet) {
	    // 同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691 //
	    item.session.getAsyncRemote().sendText(message);// 异步发送消息.
	}
    }
    
    
    /**
     * 给某一个客户端发送消息
     * @param clientSessionId
     * @param message
     * @throws IOException
     */
    public void sayTo(String clientSessionId,String message) throws IOException {
	if(this.session!=null) {
	this.session.getBasicRemote().sendText(message);
	}
	for (ManageClientWebSocket item : webSocketSet) {
	    // 同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691 //
	    Session session2 = item.session;
		if(clientSessionId.equals(session2.getId())) {
		session2.getAsyncRemote().sendText(message);// 异步发送消息.
	    }
	    
	}
    }

}
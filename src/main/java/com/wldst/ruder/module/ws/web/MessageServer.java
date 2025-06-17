package com.wldst.ruder.module.ws.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.LogDomain;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * ClassName: MessageServer <br/>
 * Description: MessageServer服务处理类 <br/>
 * date: 2020/2/4 11:05<br/>
 *
 * @author ccsert<br />
 * @since JDK 1.8
 */
@ServerEndpoint("/message/{userId}")
@Component
public class MessageServer extends LogDomain{

    private static final Logger LOG = LoggerFactory.getLogger(MessageServer.class);
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static CopyOnWriteArraySet<MessageServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    private String userId;
    private static CrudNeo4jService neo4jService; 

    /**
     * 连接建立成功时调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
	this.session = session;
	setUserId(userId);
	if(neo4jService==null) {
	    neo4jService = (CrudNeo4jService) SpringContextUtil.getBean("crudNeo4jService"); 
	}

	// 加入set中
	webSocketSet.add(this);

	// 在线人数加1
	addOnlineCount();
	LOG.info(userId + "连接成功" + "----当前在线人数为：" + onlineCount);
    }

    /**
     * 连接关闭时调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
	// 在线人数减1
	subOnlineCount();
	// 从set中删除
	webSocketSet.remove(this);
	LOG.info(userId + "已关闭连接" + "----剩余在线人数为：" + onlineCount);
    }

    /**
     * 接收客户端发送的消息时调用的方法
     *
     * @param message 接收的字符串消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
	LOG.info(userId + "发送消息为:" + message);
	sendInfo(message, userId);
    }

    /**
     * 服务器主动提推送消息
     *
     * @param message 消息内容
     * @throws IOException io异常抛出
     */
    public void sendMessage(String message) throws IOException {

	this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发消息功能
     *
     * @param message 消息内容
     * @param userId     房间号
     */
    public static Boolean sendInfo(String message, @PathParam("userId") String userId) {
	 LOG.info("推送消息到窗口" + userId + "，推送内容:" + message);
	 boolean onlineMsg = message.startsWith(userId+":");
	for (MessageServer item : webSocketSet) {
	    try {
		if(!onlineMsg) {
		 // 这里可以设定只推送给这个userId的，为null则全部推送
			if (item.getUserId() == null || item.getUserId().equals(userId)) {
			    item.sendMessage(message);
			}
		}else {
		    if(item.getUserId()!=null&&!item.getUserId().equals(userId)) {
		    item.sendMessage(message);
		    }
		}
	    } catch (IOException e) {
		LOG.error("消息发送失败" + e.getMessage(), e);
		return false;
	    }
	}
	return  true;
    }

    /**
     * 原子性的++操作
     */
    public static synchronized void addOnlineCount() {
	MessageServer.onlineCount++;
    }
    
    public static List<String> getOnlineUser(){
	List<String> users = new ArrayList<>();
	for (MessageServer item : webSocketSet) {
	    users.add(item.getUserId());
	}
	return users;
    }

    /**
     * 原子性的--操作
     */
    public static synchronized void subOnlineCount() {
	MessageServer.onlineCount--;
    }

    public String getUserId() {
	return userId;
    }

    public void setUserId(String userId) {
	this.userId = userId;
    }

}

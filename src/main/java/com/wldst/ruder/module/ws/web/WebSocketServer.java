package com.wldst.ruder.module.ws.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.LogDomain;
import com.wldst.ruder.util.MapTool;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * ClassName: webSocketServer <br/>
 * Description: websocket服务处理类 <br/>
 * date: 2020/2/4 11:05<br/>
 *
 * @author ccsert<br />
 * @since JDK 1.8
 */
@ServerEndpoint("/websocket/{sid}")
@Component
public class WebSocketServer extends LogDomain{

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServer.class);
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    private String sid;
    private static CrudNeo4jService neo4jService; 

    /**
     * 连接建立成功时调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
	this.session = session;
	setSid(sid);

	if(neo4jService==null) {
	    neo4jService = (CrudNeo4jService) SpringContextUtil.getBean("crudNeo4jService"); 
	}
	Map<String, Object> data = new HashMap<>();
	data.put(CODE, sid);
	Node findBy = neo4jService.findBy(CODE, sid, OBJECT);
	if (findBy == null) {
	    findBy = neo4jService.saveByBody(data, OBJECT);
	}
	data.put(OBJECT_ID, findBy.getId());
	data.put(CONTENT, "进入对象"+sid);
	// 加入set中
	webSocketSet.add(this);
	List<Map<String, Object>> datas = neo4jService.listDataBy(OBJECT_ID, findBy.getId()+"", OBJECT_LOG);
	for(Map<String, Object> di: datas) {
	    try {
		sendMessage(MapTool.string(di, CONTENT));
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	// 在线人数加1
	addOnlineCount();
	LOG.info(sid + "连接成功" + "----当前在线人数为：" + onlineCount);
    }

    /**
     * 连接关闭时调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
	// 在线人数减1
	subOnlineCount();
	// 从set中删除
	webSocketSet.remove(this);
	LOG.info(sid + "已关闭连接" + "----剩余在线人数为：" + onlineCount);
    }

    /**
     * 接收客户端发送的消息时调用的方法
     *
     * @param message 接收的字符串消息
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
	LOG.info(sid + "发送消息为:" + message);
	sendInfo(message, sid);
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
     * @param sid     房间号
     */
    public static void sendInfo(String message, @PathParam("sid") String sid) {
	Map<String, Object> data = new HashMap<>();
	data.put(CODE, sid);
	Node findBy = neo4jService.findBy(CODE, sid, OBJECT);
	if (findBy == null) {
	    findBy = neo4jService.saveByBody(data, OBJECT);
	}
	data.put(OBJECT_ID, findBy.getId());
	if(message.indexOf(":")>0) {
	    data.put(CONTENT, message.split(":")[1]);
		data.put("userName",message.split(":")[0]);
	}else {
	    data.put(CONTENT, message);
	}
	
	neo4jService.saveByBody(data, OBJECT_LOG);

	LOG.info("推送消息到窗口" + sid + "，推送内容:" + message);
	for (WebSocketServer item : webSocketSet) {
	    try {
		// 这里可以设定只推送给这个sid的，为null则全部推送
		if (item.getSid() == null || item.getSid().equals(sid)) {
		    item.sendMessage(message);
		}
	    } catch (IOException e) {
		LOG.error("消息发送失败" + e.getMessage(), e);
		return;
	    }
	}
    }

    /**
     * 原子性的++操作
     */
    public static synchronized void addOnlineCount() {
	WebSocketServer.onlineCount++;
    }

    /**
     * 原子性的--操作
     */
    public static synchronized void subOnlineCount() {
	WebSocketServer.onlineCount--;
    }

    public String getSid() {
	return sid;
    }

    public void setSid(String sid) {
	this.sid = sid;
    }

}

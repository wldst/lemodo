package com.wldst.ruder.module.ws.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.WebSocketDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.module.parse.ParseExcuteSentence2;
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
 * @author wldst
 *
 */
@ServerEndpoint("/context/{sid}")
@Component
public class ContextServer extends WebSocketDomain{

    private static final Logger LOG = LoggerFactory.getLogger(ContextServer.class);
    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的ContextServer对象。
     */
    private static CopyOnWriteArraySet<ContextServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;
    private String sid;
    private static CrudNeo4jService neo4jService;
    private static ParseExcuteSentence2 pes;

    /**
     * 连接建立成功时调用的方法
     */
    @OnOpen
	public void onOpen(Session session, @PathParam("sid") String sid) {
		this.session = session;
		setSid(sid);
		pes = (ParseExcuteSentence2) SpringContextUtil.getBean("parseExcuteSentence2");
		// 加入set中
		webSocketSet.add(this);
		pes.getMyContext(sid).put(CONVERSATION, this);
		// 在线人数加1
		addOnlineCount();
		LOG.info(sid + "连接成功" + "----当前在线人数为：" + onlineCount);
	}
    
    /**
     * 发消息功能
     *
     * @param message 消息内容
     * @param userId     房间号
     */
	public static Boolean sendInfo(String message, @PathParam("userId") String userId) {
		LOG.info("推送给" + userId + "，内容:" + message);
		if(null == message){
			return false;
		}
		boolean onlineMsg = message.startsWith(userId + ":");
		for (ContextServer item : webSocketSet) {
			try {
				if (!onlineMsg) {
					// 这里可以设定只推送给这个userId的，为null则全部推送
					if (item.getSid() == null || item.getSid().equals(userId)) {
						item.sendMessage(message);
					}
				} else {
					if (item.getSid() != null && !item.getSid().equals(userId)) {
						item.sendMessage(message);
					}
				}
			} catch (IOException e) {
				LOG.error("消息发送失败" + e.getMessage(), e);
				return false;
			}
		}
		return true;
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
	pes.getMyContext(sid).remove(CONVERSATION);
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
		if (message.startsWith("userSelected")) {
			List<Map<String, Object>> handleResult = new ArrayList<>();
			Map<String, Object> myContext = pes.getMyContext(sid);
			String selectValue = message.split(":")[1];
			myContext.put("which", selectValue);
			if(myContext.get("lastProcess")==null){
				return;
			}
			MsgProcess mi = (MsgProcess) myContext.get("lastProcess");

			mi.process(sid, myContext);
			Object doWithSelected = mi.doWithSelected(myContext);

			if (doWithSelected instanceof List l) {

				sendInfo(MapTool.listMapString(l), sid);
			}
			if (doWithSelected instanceof Map m) {
				handleResult.add(m);
				sendInfo(MapTool.listMapString(handleResult), sid);
			}
			if (doWithSelected instanceof String s) {
				handleResult.add(result(s));
				sendInfo(MapTool.listMapString(handleResult), sid);
			}

		} else {
			sendInfo(message, sid);
		}
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
     * 原子性的++操作
     */
    public static synchronized void addOnlineCount() {
	ContextServer.onlineCount++;
    }

    /**
     * 原子性的--操作
     */
    public static synchronized void subOnlineCount() {
	ContextServer.onlineCount--;
    }

    public String getSid() {
	return sid;
    }

    public void setSid(String sid) {
	this.sid = sid;
    }

}

package com.wldst.ruder.module.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CopyOnWriteArraySet;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;

@ServerEndpoint("/ws/cmd")
@Component
public class CMDWebSocket {

    private Process process;
    private InputStream inputStream;
    // 用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<CMDWebSocket> webSocketSet = new CopyOnWriteArraySet<CMDWebSocket>();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private LinuxCommander threadCmd;

    /**
     * 新的WebSocket请求
     */
    @OnOpen
    public void onOpen(Session session) {
	this.session = session;
	// 执行tail -f命令
	String logFile = "";
	// tailLog(logFile);
	// operateLinux();
	webSocketSet.add(this); // 加入set中
	this.session.getAsyncRemote().sendText("恭喜您成功连接上WebSocket-->当前在线人数为：" + webSocketSet.size());
    }

    private void operateLinux(String cmd, String divId) throws IOException {
	if (cmd == null || cmd.isEmpty()) {
	    return;
	}

	if (cmd != null && cmd.isEmpty()) {
	    process = Runtime.getRuntime().exec(cmd);
	    inputStream = process.getInputStream();

	    // 启动新的线程，防止InputStream阻塞处理WebSocket的线
	    threadCmd = new LinuxCommander(inputStream, session);
	    threadCmd.start();
	}
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
	    if (threadCmd != null && threadCmd.isAlive()) {
		threadCmd.join();
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
	System.out.println("来自客户端的消息:" + message);
	String[] split = message.split(":");
	if (split[0].equals("listapp")) {
	    try {
		operateLinux("ps -ef|grep java", split[1]);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	if (message.equals("listLogs")) {

	}

	if (message.equals("listLogs")) {

	}

	// 群发消息
	try {
	    broadcast(message);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
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
	for (CMDWebSocket item : webSocketSet) {
	    // 同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691 //
	    this.session.getBasicRemote().sendText(message);
	    item.session.getAsyncRemote().sendText(message);// 异步发送消息.
	}
    }

}
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

@ServerEndpoint("/ws/log")
@Component
public class LogWebSocketHandle {

    private Process process;
    private InputStream inputStream;
    // 用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<LogWebSocketHandle> webSocketSet = new CopyOnWriteArraySet<LogWebSocketHandle>();
    // 与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    private LinuxReaderThread thread;

    /**
     * 新的WebSocket请求
     */
    @OnOpen
    public void onOpen(Session session) {
	this.session = session;
	try {
	    // 执行tail -f命令
	    String logFile = "";
	    tailLog(logFile);
	    webSocketSet.add(this); // 加入set中
	    // System.out.println("有新连接加入！当前在线人数为" + webSocketSet.size());
	    this.session.getAsyncRemote().sendText("恭喜您成功连接上WebSocket-->当前在线人数为：" + webSocketSet.size());
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void tailLog(String file) throws IOException {
	if (file == null || file.isEmpty()) {
	    return;
	}
	if (!file.endsWith(".log") && !file.endsWith(".out")) {
	    return;
	}

	if (file != null && file.isEmpty()) {
	    process = Runtime.getRuntime().exec("tail -n 500 -f " + file);
	    inputStream = process.getInputStream();

	    // 启动新的线程，防止InputStream阻塞处理WebSocket的线
	    thread = new LinuxReaderThread(inputStream, session);
	    thread.start();
	}
    }

    /**
     * WebSocket请求关闭
     */
    @OnClose
    public void onClose() {
	webSocketSet.remove(this); // 从set中删除
	// System.out.println("有一连接关闭！当前在线人数为" + webSocketSet.size());
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

    /** * 收到客户端消息后调用的方法 * * @param message 客户端发送过来的消息 */
    @OnMessage
    public void onMessage(String message, Session session) {
	System.out.println("来自客户端的消息:" + message);
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
     * * 群发自定义消息 *
     * 
     * @throws IOException
     */
    public void broadcast(String message) throws IOException {
	for (LogWebSocketHandle item : webSocketSet) {
	    // 同步异步说明参考：http://blog.csdn.net/who_is_xiaoming/article/details/53287691 //
	    this.session.getBasicRemote().sendText(message);
	    item.session.getAsyncRemote().sendText(message);// 异步发送消息.
	}
    }

}
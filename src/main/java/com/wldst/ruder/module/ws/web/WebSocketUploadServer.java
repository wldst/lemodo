package com.wldst.ruder.module.ws.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.module.ws.service.SaveFile;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * 
 * @author wldst
 *
 */
@ServerEndpoint("/upload/{sid}")
@Component
public class WebSocketUploadServer {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketUploadServer.class);

    /**
     * 静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
     */
    private static int onlineCount = 0;

    /**
     * concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
     */
    private static CopyOnWriteArraySet<WebSocketUploadServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 注入文件保存的接口
     */
    private static SaveFile saveFile;
    private static String sid;

    @Autowired
    public void setSaveFileI(SaveFile saveFileI) {
        WebSocketUploadServer.saveFile = saveFileI;
    }

    /**
     * 保证文件对象和文件路径的唯一性
     */
    private HashMap docUrl;

    /**
     * 结束标识判断
     */
    private String END_UPLOAD_TAG = "over";

    /**
     * 连接建立成功时调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        setSid(sid);
        //加入set中
        webSocketSet.add(this);
        //在线人数加1
        addOnlineCount();
        LOG.info(sid + "连接成功" + "----当前在线人数为：" + onlineCount);
    }

    /**
     * 连接关闭时调用的方法
     */
    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        //在线人数减1
        subOnlineCount();
        //从set中删除
        webSocketSet.remove(this);
        LOG.info(sid + "已关闭连接" + "----剩余在线人数为：" + onlineCount);
    }

    /**
     * 接收客户端发送的消息时调用的方法
     *
     * @param message 接收的字符串消息。该消息应当为json字符串
     */
    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        //前端传过来的消息都是一个json
        JSONObject jsonObject = JSON.parseObject(message);
        //消息类型
        String type = jsonObject.getString("type");
        //消息内容
        String data = jsonObject.getString("data");
        //判断类型是否为文件名
        if ("fileName".equals(type)) {
            LOG.info("传输文件为:" + data);
            if(data==null) {
        	return;
            }
            //此处的 “.”需要进行转义
            /*String[] split = data.split("\\.");*/
            try {
                Map<String, Object> map = saveFile.docPath(data);
                docUrl = (HashMap) map;
                this.sendMessage("ok");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if ("fileCount".equals(type)){
            LOG.info("传输第"+data+"份");
        }
        //判断是否结束
        else if (END_UPLOAD_TAG.equals(type)) {
            LOG.info("===============>传输成功");
            //返回一个文件下载地址
            String path = (String) docUrl.get("nginxPath");
            //返回客户端文件地址
            try {
                this.sendMessage(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 该方法用于接收字节流数组
     *
     * @param message 文件字节流数组
     * @param session 会话
     */
    @OnMessage
    public void onMessage(byte[] message, Session session) {
        //群发消息
        try {
            //将流写入文件
            saveFile.saveFileFromBytes(message,docUrl);
            //文件写入成功，返回一个ok
            this.sendMessage("ok");
        } catch (IOException e) {
            e.printStackTrace();
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
     * 群发消息功能
     *
     * @param message 消息内容
     * @param sid     房间号
     */
    public static void sendInfo(String message, @PathParam("sid") String sid) {
        LOG.info("推送消息到窗口" + sid + "，推送内容:" + message);
        for (WebSocketUploadServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
        	if(item.getSid()==null||item.getSid().equals(sid)) {
        	    item.sendMessage(message);
        	}
                item.sendMessage(message);
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
        WebSocketUploadServer.onlineCount++;
    }

    /**
     * 原子性的--操作
     */
    public static synchronized void subOnlineCount() {
        WebSocketUploadServer.onlineCount--;
    }

    
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

}

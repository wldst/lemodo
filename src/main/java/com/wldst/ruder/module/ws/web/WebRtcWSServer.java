package com.wldst.ruder.module.ws.web;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebRTC + WebSocket
 */
@Component
@ServerEndpoint(value = "/webrtc/{username}")
public class WebRtcWSServer {

    /**
     * 连接集合
     */
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username, @PathParam("publicKey") String publicKey) {
        sessionMap.put(username, session);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        for (Map.Entry<String, Session> entry : sessionMap.entrySet()) {
            if (entry.getValue() == session) {
                sessionMap.remove(entry.getKey());
                break;
            }
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    /**
     * 服务器接收到客户端消息时调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try{
            //jackson
            ObjectMapper mapper = new ObjectMapper();
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            //JSON字符串转 HashMap
            HashMap hashMap = mapper.readValue(message, HashMap.class);

            //消息类型
            String type = (String) hashMap.get("type");

            //to user
            String toUser = (String) hashMap.get("toUser");
            Session toUserSession = sessionMap.get(toUser);
            String fromUser = (String) hashMap.get("fromUser");

            //msg
            String msg = (String) hashMap.get("msg");

            //sdp
            String sdp = (String) hashMap.get("sdp");

            //ice
            Map iceCandidate  = (Map) hashMap.get("iceCandidate");

            HashMap<String, Object> map = new HashMap<>();
            map.put("type",type);

            //呼叫的用户不在线
            if(toUserSession == null){
                toUserSession = session;
                map.put("type","call_back");
                map.put("fromUser","系统消息");
                map.put("msg","Sorry，呼叫的用户不在线！");

                send(toUserSession,mapper.writeValueAsString(map));
                return;
            }

            //对方挂断
            if ("hangup".equals(type)) {
                map.put("fromUser",fromUser);
                map.put("msg","对方挂断！");
            }

            //视频通话请求
            if ("call_start".equals(type)) {
                map.put("fromUser",fromUser);
                map.put("msg","1");
            }

            //视频通话请求回应
            if ("call_back".equals(type)) {
                map.put("fromUser",toUser);
                map.put("msg",msg);
            }

            //offer
            if ("offer".equals(type)) {
                map.put("fromUser",toUser);
                map.put("sdp",sdp);
            }

            //answer
            if ("answer".equals(type)) {
                map.put("fromUser",toUser);
                map.put("sdp",sdp);
            }

            //ice
            if ("_ice".equals(type)) {
                map.put("fromUser",toUser);
                map.put("iceCandidate",iceCandidate);
            }

            send(toUserSession,mapper.writeValueAsString(map));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 封装一个send方法，发送消息到前端
     */
    private void send(Session session, String message) {
        try {
            System.out.println(message);

            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

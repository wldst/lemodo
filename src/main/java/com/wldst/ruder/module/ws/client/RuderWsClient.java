package com.wldst.ruder.module.ws.client;

import com.alibaba.fastjson.JSON;
import com.wldst.ruder.domain.SystemDomain;
import com.wldst.ruder.module.ws.handler.ClientHandler;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.RestApi;
import org.java_websocket.enums.ReadyState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RuderWsClient extends SystemDomain {
    private static Logger logger = LoggerFactory.getLogger(RuderWsClient.class);
    @Value(value = "${server.root.host}")
    private String rootHost;
    @Value(value = "${server.root.port}")
    private String rootPort;
    private BaseWebsocketClient myClient;
    @Autowired
    private RestApi ruderApi;
    @Autowired
    private ClientHandler handler;
    private Map<String, BaseWebsocketClient> clientMap = new HashMap<>();

    public void sentCmd(Map<String, Object> propMapByNodeId, String cmd) {
        Map<String, Object> sentData = new HashMap<>();
        sentData.put(CMD, cmd);
        sentData.put(DATA, propMapByNodeId);
        sent(sentData);
    }

    public void sendCmdTo(Map<String, Object> propMapByNodeId, String cmd, Map<String, Object> target) {
        Map<String, Object> sentData = new HashMap<>();
        sentData.put(CMD, cmd);
        sentData.put(DATA, propMapByNodeId);
        sent(sentData);
    }

    public void sent(Map<String, Object> data, Map<String, Object> target) {
        try {
            BaseWebsocketClient targetClient = getClientOf(target);
            if (!targetClient.isOpen() || targetClient.isClosed()) {
                targetClient.connect();
            }
            int i = 0;
            LoggerTool.debug(logger, "  连接中。");
            while (!targetClient.getReadyState().equals(ReadyState.OPEN)) {
                // System.out.println("连接中。。。");
                LoggerTool.debug(logger, "。");
                Thread.sleep(2000);
                i++;
            }
            // 连接成功往websocket服务端发送数据

            String jsonString = JSON.toJSONString(data);
            LoggerTool.info(logger, "send info:" + jsonString);
            targetClient.send(jsonString);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            LoggerTool.error(logger, "sent msg error:", e);
        }

    }

    public void sent(Map<String, Object> data) {
        try {
            // myClient = new BaseWebsocketClient(new URI("ws://" + rootHost +
            // ":9500/ws/server"));
            getNewWebsocket();
            if (!myClient.isOpen() || myClient.isClosed()) {
                myClient.connect();
            }
            int i = 0;
            LoggerTool.debug(logger, "  连接中。");
            while (!myClient.getReadyState().equals(ReadyState.OPEN)) {
                // System.out.println("连接中。。。");
                LoggerTool.debug(logger, "。");
                Thread.sleep(2000);
                i++;
            }
            // 连接成功往websocket服务端发送数据

            String jsonString = JSON.toJSONString(data);
            LoggerTool.info(logger, "send info:" + jsonString);
            myClient.send(jsonString);
        } catch (URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            LoggerTool.error(logger, "sent msg error:", e);
        }

    }

    private void getNewWebsocket() throws URISyntaxException {
        if (myClient == null) {
            String wsUri = "ws://" + rootHost + ":" + rootPort + "/ws/server";
            LoggerTool.debug(logger, "websocket start init=========" + wsUri);
            myClient = new BaseWebsocketClient(new URI(wsUri));
            myClient.setRuderApi(ruderApi);
            myClient.setHandler(handler);
        }
    }

    private BaseWebsocketClient getClientOf(Map<String, Object> target) throws URISyntaxException {
        String wsUri = "ws://" + host(target) + ":" + portStr(target) + "/ws/server";
        String uri2 = uri(target);
        if (uri2 != null) {
            wsUri = uri2;
        }
        if (clientMap.get(uri2) != null) {
            return clientMap.get(uri2);
        }
        LoggerTool.debug(logger, "websocket start init=========" + wsUri);
        BaseWebsocketClient tartClient = new BaseWebsocketClient(new URI(wsUri));
        tartClient.setRuderApi(ruderApi);
        tartClient.setHandler(handler);
        clientMap.put(uri2, tartClient);
        return tartClient;

    }

}

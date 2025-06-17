package com.wldst.ruder.module.ws.intercepter;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 
 * @author wldst
 *
 */
@Component
public class RuderWebSocketInterceptors implements HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // TODO
        System.out.println("hello hands");
        /*
        处可以做一些处理，例如校验连接中的参数，保存连接用户信息等，
        用户信息等有用信息可存储在 Map<String, Object> attributes 中，
        在 handler 中可使用 WebSocketSession.getAttributes() 方法取出相应的数据。
 返回 false 会导致连接失败
         */

       
        return true; // [1]
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}

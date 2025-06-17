package com.wldst.ruder.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.util.WebAppRootListener;

import com.wldst.ruder.module.ws.intercepter.RuderWebSocketInterceptors;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;


@Configuration
@EnableWebSocketMessageBroker
//@EnableAutoConfiguration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer  {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
	config.enableSimpleBroker("/topic");
	config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
	registry.addEndpoint("/websocket").setAllowedOrigins("*").withSockJS();
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
	return new ServerEndpointExporter();
    }
//    @Resource
//    WebSocketHandler defaultHandler;
//    @Resource
//    RuderWebSocketInterceptors defaultInterceptors;

//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//        System.out.println("hello  work");
//        registry
//                .addHandler(defaultHandler, "ws") // todo  [2]
//                .addInterceptors(defaultInterceptors) // todo  [3]
//                .setAllowedOrigins("*"); // 解决跨域问题 [4]
//    }
    /**
     * 配置websocket文件接受的文件最大容量
     * @param servletContext    context域对象
     * @throws ServletException 抛出异常
     */
//    @Override
//    public void onStartup(ServletContext servletContext) throws ServletException {
//        servletContext.addListener(WebAppRootListener.class);
//        servletContext.setInitParameter("org.apache.tomcat.websocket.textBufferSize","51200000");
//        servletContext.setInitParameter("org.apache.tomcat.websocket.binaryBufferSize","51200000");
//    }
}

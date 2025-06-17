package com.wldst.ruder.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wldst.ruder.crud.service.CrudUserNeo4jService;

@Configuration
public class HttpsConfiguration {    
    @Value("${http.port}")
    private Integer port;
    @Value("${server.port}")
    private Integer sport;


    @Bean
/**
 * 配置TomcatServletWebServerFactory，用于创建和配置Tomcat服务器。
 *
 * @return TomcatServletWebServerFactory实例，已配置好HTTP连接器和安全约束。
 */
public TomcatServletWebServerFactory tomcatServletWebServerFactory() {
    // 初始化HTTP连接器
    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connector.setScheme("http");
    connector.setPort(port);
    connector.setSecure(false);
    connector.setRedirectPort(sport);
    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
        /**
         * 在Tomcat上下文启动后处理上下文。
         * 此处用于添加安全约束，限制对应用程序的访问。
         *
         * @param context Tomcat的上下文对象。
         */
        @Override
        protected void postProcessContext(Context context) {
            SecurityConstraint securityConstraint = new SecurityConstraint();
            securityConstraint.setUserConstraint("CONFIDENTIAL");
            SecurityCollection collection = new SecurityCollection();
            collection.addPattern("/*");
            collection.addMethod("HEAD");
            collection.addMethod("PUT");
            collection.addMethod("DELETE");
            collection.addMethod("OPTIONS");
            collection.addMethod("TRACE");
            collection.addMethod("COPY");
            collection.addMethod("SEARCH");
            collection.addMethod("PROPFIND");
            securityConstraint.addCollection(collection);
            context.addConstraint(securityConstraint);
        }
    };
    // 添加额外的Tomcat连接器
    tomcat.addAdditionalTomcatConnectors(connector);
    return tomcat;
}
}

package com.wldst.ruder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {
    //60 * 1000
    @Value("${rest.connectTimeout:60000}")
    private int connectTimeout;
    //5 * 60 * 1000
    @Value("${rest.readTimeout:300000}")
    private int readTimeout;
//    @Bean
//    public RestTemplate restTemplate() {
//        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
//        simpleClientHttpRequestFactory.setConnectTimeout(connectTimeout);
//        simpleClientHttpRequestFactory.setReadTimeout(readTimeout);
//        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
//        return restTemplate;
//    }
}

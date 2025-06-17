package com.wldst.ruder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
//import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;

import com.wldst.ruder.config.SpringContextUtil;

//@EnableEurekaClient
//@EnableScheduling
//@ComponentScan(basePackages = { "com.wldst.ruder", "org.springframework.mail" })
//@SpringBootApplication
public class RuderApplication {
    

    public static void main(String[] args) {
	ApplicationContext app = SpringApplication.run(RuderApplication.class, args);

	SpringContextUtil.setApplicationContexts(app);
    }

//    @Bean
    // @LoadBalanced
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
	// Do any additional configuration here
	return builder.build();
    }

}

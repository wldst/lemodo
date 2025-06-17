package com.wldst.ruder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.wldst.ruder.config.SpringContextUtil;

//@EnableEurekaClient
@EnableScheduling
@ComponentScan(basePackages = { "com.wldst.ruder", "org.springframework.mail" })
@SpringBootApplication
public class LemodoApplication {


    //    public static final String  MODULE_NAME ="/show";
//    public static final String  MODULE_NAME ="/mailserver";
//    public static final String  MODULE_NAME ="/bpmserver";
    public static  String  MODULE_NAME ="/cd";
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(LemodoApplication.class, args);

        SpringContextUtil.setApplicationContexts(app);
    }

//    @Bean
//    // @LoadBalanced
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//	// Do any additional configuration here
//	return builder.build();
//    }
        @Value("${server.context}")
    public  void setModuleName(String moduleName){
        MODULE_NAME=moduleName;
    }

}

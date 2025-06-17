package com.wldst.ruder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
    @Bean
    public JavaMailSenderImpl mailSender() {
	JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

	javaMailSender.setProtocol("SMTP");
	javaMailSender.setHost("smtp.qq.com");
	javaMailSender.setPort(465);

	return javaMailSender;
    }
}

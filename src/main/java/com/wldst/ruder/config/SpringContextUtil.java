package com.wldst.ruder.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {
    public static ApplicationContext applicationContext;
    
    public static void setApplicationContexts(ApplicationContext applicationContext)
            throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }
    //设置上下文
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }
    //通过名字获取上下文中的bean
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    public static <T> T getBean(String name, Class<T> requiredType) {
        return applicationContext.getBean(name, requiredType);
    }

    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static boolean isSingleton(String name) {
        return applicationContext.isSingleton(name);
    }

    public static Class<? extends Object> getType(String name) {
        return applicationContext.getType(name);
    }
     
    //获取上下文
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

 
    //通过类型获取上下文中的bean
    public static Object getBean(Class<?> requiredType){
        return applicationContext.getBean(requiredType);
    }
    
    public static Map<String, Object> getBeans() {
	Map<String, Object> b = new HashMap<>();
	String[] beanNames = applicationContext.getBeanDefinitionNames();
	for (String beanName : beanNames) {
	    Object bean = applicationContext.getBean(beanName);
	    // 对bean进行处理
	    b.put(beanName, bean);
	}
	return b;
    }
     
}
package com.wldst.ruder.test;

import java.lang.reflect.Field;
import java.util.Date;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.wldst.ruder.config.SpringContextUtil;
@RestController
public class BeanController {

	/**
	 * 注册Bean
	 * 
	 * @param beanName
	 * @return
	 */
	@GetMapping("/bean/register/{beanName}")
	public String registerBean(@PathVariable String beanName) { 
		// 获取context
		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringContextUtil
				.getApplicationContext();
		
		// 创建bean信息.
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(TestService.class);
		beanDefinitionBuilder.addPropertyValue("id", "1");
		beanDefinitionBuilder.addPropertyValue("name", "张三");
		Object beanObject =null;
		// 判断Bean是否已经注册
		try {
			beanObject = SpringContextUtil.getBean(beanName);
			if (beanObject != null) {
				System.out.println(String.format("Bean %s 已注册", beanName));
			} else {
				// 获取BeanFactory
				DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext
						.getBeanFactory();
				// 动态注册bean.
				defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
				// 获取动态注册的bean.
				beanObject = SpringContextUtil.getBean(beanName);
				if (beanObject != null) {
					System.out.println(String.format("Bean %s 注册成功", beanName));
					return beanObject.toString();
				} else {
					return "register Bean Error";
				}
			}
		} catch (Exception e) {
			// 获取BeanFactory
			DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext
					.getBeanFactory();
			// 动态注册bean.
			defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
			// 获取动态注册的bean.
			beanObject = SpringContextUtil.getBean(beanName);
			if (beanObject != null) {
				System.out.println(String.format("Bean %s 注册成功", beanName));
				return beanObject.toString();
			} else {
				return "register Bean Error";
			}
		}
		
		return "register Bean Success";
	}

	/**
	 * 动态修改Bean
	 * @param beanName
	 * @return
	 */
	@GetMapping("/bean/update/{beanName}")
	public String update(@PathVariable String beanName) {
		ApplicationContext applicationContext = SpringContextUtil.getApplicationContext();
		String[] beans = applicationContext.getBeanDefinitionNames();
		for (String bean : beans) {
			// 拿到bean的Class对象
			Class<?> beanType = applicationContext.getType(bean);
			if (beanType == null) {
				continue;
			} // 拿到当前bean类型的所有字段
			Field[] declaredFields = beanType.getDeclaredFields();
			if (beanName.equals(bean)) {
				for (Field field : declaredFields) {
					// 从spring容器中拿到这个具体的bean对象
					Object beanObject = applicationContext.getBean(bean);
					// 当前字段设置新的值
					try {
						String fieldName = field.getName();
						if ("name".equals(fieldName)) {
							setFieldData(field, beanObject, "AL113A5");
						} else if ("id".equals(fieldName)) {
							setFieldData(field, beanObject, "12");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return "update Bean Success";
	}

	private void setFieldData(Field field, Object bean, String data) throws Exception {
		field.setAccessible(true);
		Class<?> type = field.getType();
		if (type.equals(String.class)) {
			field.set(bean, data);
		} else if (type.equals(Integer.class)) {
			field.set(bean, Integer.valueOf(data));
		} else if (type.equals(Long.class)) {
			field.set(bean, Long.valueOf(data));
		} else if (type.equals(Double.class)) {
			field.set(bean, Double.valueOf(data));
		} else if (type.equals(Short.class)) {
			field.set(bean, Short.valueOf(data));
		} else if (type.equals(Byte.class)) {
			field.set(bean, Byte.valueOf(data));
		} else if (type.equals(Boolean.class)) {
			field.set(bean, Boolean.valueOf(data));
		} else if (type.equals(Date.class)) {
			field.set(bean, new Date(Long.parseLong(data)));
		}
	}

	/**
	 * 移除Bean
	 *
	 * @return
	 */
	@GetMapping("/bean/remove/{beanName}")
	public String removeBeanDefinition(@PathVariable String beanName) {
		Object beanObject = SpringContextUtil.getBean(beanName);
		if (beanObject == null) {
			System.out.println(String.format("Bean %s 不存在", beanName));
			return "remove Error";
		} // 获取context.
		ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) SpringContextUtil
				.getApplicationContext();
		// 获取BeanFactory
		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) configurableApplicationContext
				.getBeanFactory();
		defaultListableBeanFactory.removeBeanDefinition(beanName);
		System.out.println(String.format("Bean %s 已移除", beanName));
		return "remove Success";
	}

	/**
	 * 操作Bean
	 *
	 * @return
	 */
	@GetMapping("/bean/print/{beanName}")
	public String print(@PathVariable String beanName) {
		Object beanObject = SpringContextUtil.getBean(beanName);
		if (beanObject != null) {
			((TestService) beanObject).print();
			return beanObject.toString();
		} else {
			System.out.println(String.format("Bean %s 不存在", beanName));
		}
		return String.format("Bean %s 不存在", beanName);
	}

}

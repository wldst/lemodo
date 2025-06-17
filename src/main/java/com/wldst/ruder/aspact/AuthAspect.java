package com.wldst.ruder.aspact;
 
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.NonCheck;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.exception.ValidateException; 
 

@Aspect// 这个注解表明 使用spring boot 的aop，需要开启aop spring.aop.auto=true
@Component
public class AuthAspect {
    private static final Logger logger = LoggerFactory.getLogger(AuthAspect.class);

    @Before(value = "@annotation(com.wldst.ruder.annotation.NonCheck)")
    public void before(JoinPoint point) throws ValidateException {
        LoggerTool.info(logger,">>>>>Inspect.before");

        Map<String, StringBuffer> errorMap = new HashMap<String, StringBuffer>();
        // 访问目标方法的参数名称parameterNames和方法method
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        Method method = methodSignature.getMethod();
        // 绑定参数
        Map parameterMap = this.bindParameter(parameterNames, point.getArgs());
        // 绑定注解类
        Map<String, Class> clazzMap = null;
        // 目标注解class
        Class[] clazzs = null;
        // 目标注解参数名称
        List<String> parameters = null;

        // 获取目标方法注解的类
        if (method.isAnnotationPresent(NonCheck.class)) {
            clazzs = method.getAnnotation(NonCheck.class).clazzs();
            parameters = List.of(method.getAnnotation(NonCheck.class).params());
            clazzMap = this.bindClazz(parameters, clazzs);
            LoggerTool.info(logger,"拦截对象：{}", JSON.toJSONString(clazzs));
            LoggerTool.info(logger,"拦截参数：{}", JSON.toJSONString(parameters));
        }
 
        Result result = null;
        // NonCheck注解中的两个参数数量不能为空并且保持一致
        if (clazzs.length > 0 && clazzs.length == parameters.size()) {
            for (String parameter : parameterNames) {
                if (parameters.contains(parameter)) {
//                    if (parameterMap.get(parameter) instanceof String) {
//                        // String 类型
//                        Object obj = JSONObject.parseObject((String) parameterMap.get(parameter), clazzMap.get(parameter));
//                        try {
////                            result = VerifyUtils.validateField(obj, errorMap);
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        try {
//                            result = VerifyUtils.validateField(parameterMap.get(parameter), errorMap);
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        }
//                    }
                }
            }
        }

        if (!result.getStatus()) {
            LoggerTool.info(logger,"参数校验失败：{}", JSON.toJSONString(errorMap));
            LoggerTool.info(logger,"<<<<<Inspect.before");
            throw new ValidateException(result.getMsg());
        } else {
            LoggerTool.info(logger,"参数校验成功");
            LoggerTool.info(logger,"<<<<<Inspect.before");
        }
    }

    /**
     * 绑定参数
     *
     * @param parameterNames
     * @param args
     * @return
     */
    private Map<String,Object> bindParameter(String[] parameterNames, Object[] args) {
        Map<String,Object> map = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            map.put(parameterNames[i], args[i]);
        }
        return map;
    }

    /**
     * 绑定注解类
     *
     * @param parameters
     * @param clazzs
     * @return
     */
    private Map<String, Class> bindClazz(List<String> parameters, Class[] clazzs) {
        Map<String, Class> map = new HashMap<>();
        for (int i = 0; i < parameters.size(); i++) {
            map.put(parameters.get(i), clazzs[i]);
        }
        return map;
    }
}

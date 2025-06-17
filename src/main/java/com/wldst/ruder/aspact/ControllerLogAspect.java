package com.wldst.ruder.aspact;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.wldst.ruder.annotation.ControllerLog;
import com.wldst.ruder.util.DataUtil;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 切点类
 *
 * @version 1.0
 * @since 2020年12月15日16:58:59
 */
@Aspect
@Component
public class ControllerLogAspect {

    /**
     * 本地异常日志记录对象
     */
    private static final Logger logger = LoggerFactory.getLogger(ControllerLogAspect.class);
    private static final ThreadLocal<Long> START_TIME_THREAD_LOCAL = new NamedThreadLocal<>("ThreadLocal StartTime");

    /**
     * Controller层切点
     */
    @Pointcut("@annotation(com.wldst.ruder.annotation.ControllerLog)")
    public void controllerAspect() {
    }

    /**
     * 前置通知 用于拦截Controller层记录用户的操作
     *
     * @param joinPoint 切点
     */
    @Before("controllerAspect()")
    public void doBefore(JoinPoint joinPoint) {
        // 线程绑定变量（该数据只有当前请求的线程可见）
        START_TIME_THREAD_LOCAL.set(System.currentTimeMillis());
//        LoggerTool.info(logger,"doBefore controller logs start...");
        String url = "";
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            url = String.valueOf(request.getRequestURL());
            LoggerTool.debug(logger, "\n[method]:{}\n[headers]:{}\n[requestURL]:{}", request.getMethod(), getHeadersInfo(request), url);
            String classMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
	    LoggerTool.debug(logger,"\n========:{}\n{}\n{}", getControllerMethodDescription(joinPoint),classMethod, this.getParamMap(request));
            LoggerTool.debug(logger, "\n[remoteAddr]:{}\n[remoteHost]:{}\n[localAddr]:{}", request.getRemoteAddr(), request.getRemoteHost(), request.getLocalAddr());
        } catch (Exception e) {
            LoggerTool.error(logger,url + "[doBefore controller error={}]", e);
        }
//        LoggerTool.info(logger,url + "[doBefore controller logs end...]");
    }

    @AfterReturning(returning = "response", pointcut = "controllerAspect()")
    public void doAfterReturning(Object response) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String url = String.valueOf(request.getRequestURL());
        if(response instanceof WrappedResult r) {
            LoggerTool.debug(logger,"\n"+url + "[return]:\n {}", JSON.toJSONString(r));
        }else {
            LoggerTool.debug(logger,"\n"+url + "[return]:\n {}", response);
        }
        
        // 1、得到线程绑定的局部变量（开始时间）
        long beginTime = START_TIME_THREAD_LOCAL.get();
        // 2、结束时间
        long endTime = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
	long totalMemory = runtime.totalMemory();
	long maxMemory = runtime.maxMemory();
	long freeMemory = runtime.freeMemory();
	LoggerTool.debug(logger,"\n[" + url + "]" + "\n[耗时：{}ms 最大内存: {}m  已分配内存: {}m  \n已分配内存中的剩余空间: {}m  最大可用内存: {}m]",
                 (endTime - beginTime),
                 DataUtil.toM(maxMemory),
                 DataUtil.toM(totalMemory), DataUtil.toM(freeMemory),
                 DataUtil.toM(maxMemory - totalMemory + freeMemory));
    }
 

    private Map<String, String> getHeadersInfo(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }

    private Map<String, Object> getParamMap(HttpServletRequest request) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        Enumeration<?> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String key = (String) paramNames.nextElement();
            paramMap.put(key, request.getParameter(key));
        }
        return paramMap;
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param joinPoint 切点
     * @return 方法描述
     * @throws Exception
     */
    public static String getControllerMethodDescription(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        String description = "";
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    description = method.getAnnotation(ControllerLog.class).description();
                    break;
                }
            }
        }
        return description;
    }

}
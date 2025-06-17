package com.wldst.ruder.aspact;


import com.alibaba.fastjson.JSON;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.util.DataUtil;

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

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

/**
 * 切点类
 *
 * @version 1.0
 * @since 2020年12月15日16:58:59
 */
@Aspect
@Component
public class ServiceLogAspect {

    /**
     * 本地异常日志记录对象
     */
    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);
    private static final ThreadLocal<Long> START_TIME_THREAD_LOCAL = new NamedThreadLocal<>("ThreadLocal StartTime");

    /**
     * Controller层切点
     */
    @Pointcut("@annotation(com.wldst.ruder.annotation.ServiceLog)")
    public void serviceAspect() {
    }

    /**
     * 前置通知 用于拦截Controller层记录用户的操作
     *
     * @param joinPoint 切点
     */
    @Before("serviceAspect()")
    public void doBefore(JoinPoint joinPoint) {
        // 线程绑定变量（该数据只有当前请求的线程可见）
        START_TIME_THREAD_LOCAL.set(System.currentTimeMillis());
        String classMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        StringBuilder params = new StringBuilder();
        if (joinPoint.getArgs() != null && joinPoint.getArgs().length > 0) {
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                Object object = joinPoint.getArgs()[i];
                if(object!=null) {
                    if(object instanceof String s) {
                	boolean containKuohao = s.startsWith("{")&&s.endsWith("}")||s.startsWith("[")&&s.endsWith("]");
			if(containKuohao&&s.contains(":")) {
                	    params.append(JSON.toJSONString(object) + ";");
                	}else {
                	    params.append(s+";");
                	}
                	
                    }
                    
                }
		
            }
        }
        try {
	    LoggerTool.debug(logger,"\n[description]:{}", getServiceMethodDescription(joinPoint));
	} catch (Exception e) {
	    
	     LoggerTool.debug(logger,"\n[description]:{}", e.getLocalizedMessage());
	}
        
        LoggerTool.debug(logger,"\n[" + classMethod + "]" + "\n[params={}]", params.toString());
    }

    @AfterReturning(pointcut = "serviceAspect()")
    public void doAfterReturning(JoinPoint joinPoint) {
        // 1、得到线程绑定的局部变量（开始时间）
        long beginTime = START_TIME_THREAD_LOCAL.get();
        // 2、结束时间
        long endTime = System.currentTimeMillis();
        String classMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        Runtime runtime = Runtime.getRuntime();
	LoggerTool.debug(logger,"\n[" + classMethod + "]" + "[耗时：{}ms ]",
                 (endTime - beginTime)
             );
    } 
    /**
     * 获取注解中对方法的描述信息 用于service层注解
     *
     * @param joinPoint 切点
     * @return 方法描述
     * @throws Exception
     */
    public static String getServiceMethodDescription(JoinPoint joinPoint)
            throws Exception {
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
                    description = method.getAnnotation(ServiceLog.class).description();
                    break;
                }
            }
        }
        return description;
    }

}

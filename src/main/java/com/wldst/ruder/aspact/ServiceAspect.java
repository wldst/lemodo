package com.wldst.ruder.aspact;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
 
@Aspect
@Component // springboot最基本的注解，表示把这个类交给spring来管理
public class ServiceAspect {
    Logger LOG = LoggerFactory.getLogger(ServiceAspect.class);
    @Pointcut("execution(* comxxx.wldst.ruder.*.*Service.*(..))")
    public void cutPoint(){};
    @Before("cutPoint()")
    public void doBefore(JoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();
        // 开始打印日志
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest(); // 获取请求上下文
        Signature signature = joinPoint.getSignature(); // 目标方法名
        // 打印日志信息
        LOG.info("目标方法:{}, 对应的类名:{}", signature.getName(), signature.getDeclaringType().getSimpleName());
        LOG.info("请求方法:{}", request.getMethod());
        LOG.info("请求地址：{}", request.getRequestURL());
        LOG.info("远程地址:{}", request.getRemoteAddr());
        LOG.info("远程域名:{}", request.getRemoteHost());
        LOG.info("端口号:{}", request.getRemotePort());
        // 打印传递的参数
        Object[] args = joinPoint.getArgs();
        Object[] filterArgs = new Object[args.length]; // 创建一个新的集合，初始化长度
        // 只要是ServletRequest、ServletResponse、MultipartFile都不会添加到filterArgs中
        for(int i = 0; i < args.length; i++) {
            if(args[i] instanceof ServletRequest
                    || args[i] instanceof ServletResponse
                    || args[i] instanceof MultipartFile) {
                continue;
            }
            filterArgs[i] = args[i];
        }
        // 排除敏感字段/太长的字段都不会显示
        String[] excludeProperties = {"password", "file"};
      
//        LOG.info("请求的参数为:{}", JSONObject.toJSONString(filterArgs));
    }
    @Around("cutPoint()")
    public Object doAround(ProceedingJoinPoint pjp) {
        Object result = null;
        try {
            // 前置通知
            long StartTime = System.currentTimeMillis();
            result = pjp.proceed();
            // 排除敏感字段/太长的字段都不会显示
            String[] excludeProperties = {"password", "file"};
            
            LOG.debug("====================结果耗时:{} ms =====================", System.currentTimeMillis() - StartTime);
        }catch (Throwable e) {
            System.out.println("aop异常通知");
        }
        System.out.println("aop后置通知");
        return result;
    }
    
    @Around("cutPoint()")
    public void aroundFn(ProceedingJoinPoint pjp) {
        String methodName = pjp.getSignature().getName();
        System.out.println("==========环绕通知执行了==========");
        Object result = null;
        try{
            // == 前置通知
            System.out.println("【目标方法】"+methodName);
            // 执行目标方法
            result = pjp.proceed();
            // == 结果通知
            System.out.println("目标方法返回结果为："+result);
        }catch (Throwable e) {
            // == 异常通知
            System.out.println(e.getMessage());
        }
        // == 后置通知
        System.out.println("后置通知执行");
    } 
}


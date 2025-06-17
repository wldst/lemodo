package com.wldst.ruder.aspact;


import java.util.Arrays;

import com.wldst.ruder.util.LoggerTool;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
 
/**
 * 创建一个切面
 */
@Aspect // 标注是一个切面,共容器读取，作用于类
@Component
public class Aspact {
    private static final Logger logger = LoggerFactory.getLogger(Aspact.class);
    
    //org.springframework.web.bind.annotation.ResponseBody
    // 定义一个切入点，关于切入点如何定义？
    @Pointcut("@annotation(com.wldst.ruder.annotation.MyAnnotation)")
    public void pointFn(){}
 
    // 定义一个通知,在执行pointFn这个方法之前（切入入进去之前），我们需要执行check方法
    @Before("pointFn()")
    public void check(JoinPoint joinPoint) {
	LoggerTool.info(logger,"************** 获取切入点的相关信息 **************");
        // 获取切入点的方法
        LoggerTool.info(logger,"【切入点方法为】"+joinPoint); // execution(List com.lxc.springboot.service.UserService.getUserById(int))
        // 获取切入点方法名对应的类名
        LoggerTool.info(logger,"【切入点方法名的简单类名为】"+joinPoint.getSignature().getDeclaringType().getSimpleName());
        // 获取切入点方法名
        LoggerTool.info(logger,"【切入点方法名为】"+joinPoint.getSignature().getName()); // getUserId
        // 获取切入点方法参数列表
        Object[] args = joinPoint.getArgs();
        LoggerTool.info(logger,"【切入点方法参数列表】"+Arrays.toString(args)); // 一个集合 [Ljava.lang.Object;@6baa953e
        // 获取被代理的对象
        LoggerTool.info(logger,"【被代理的对象】"+joinPoint.getTarget()); // com.lxc.springboot.service.UserService@7ee8e0a8
        // 获取代理的对象
        LoggerTool.info(logger,"【代理的对象】"+joinPoint.getThis()); // com.lxc.springboot.service.UserService@7ee8e0a8
 
        LoggerTool.info(logger,"----------------------------");
        LoggerTool.info(logger,"对带有了@MyAnnotation注解的方法，做check检查");
    }
    
 // 返回通知：在方法正常结束后，时可以拿到方法的返回值的！！！
    // returning 值是result，所以下边方法参数二 的参数也必须为result
    // 通过result我们可以获取到目标方法返回的结果！！！
    @AfterReturning(value = "pointFn()", returning = "result")
    public void afterRe(JoinPoint joinPoint, Object result) {
        LoggerTool.info(logger,"==========结果通知执行了==========");
        // joinPoint参数与前置通知、后置通知一样，不记录了
        LoggerTool.info(logger,"返回的参数为："+result);
        LoggerTool.info(logger,"返回的JSON格式的参数为："+JSON.toJSONString(result));
    } 
    
 // 在目标方法出现异常时会执行的代码，可以获取到异常对象、信息等
    @AfterThrowing(value = "pointFn()", throwing = "e")
    public void afterTh(JoinPoint joinPoint, Exception e) {
        // joinPoint参数与前置通知、后置通知一样，不记录了
        // 获取异常信息（这个异常信息是我们自定义的！）
        LoggerTool.info(logger,"==========结果通知执行了==========");
        LoggerTool.info(logger,e.getMessage());
    }
 
}

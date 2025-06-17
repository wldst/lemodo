package com.wldst.ruder.test;
/*
import com.wldst.ruder.crud.service.CrudNeo4jService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component

public class EndpointLogger implements ApplicationRunner{

    private RequestMappingHandlerMapping handlerMapping;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    public EndpointLogger(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 获取所有的HandlerMethods
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

//        // 遍历HandlerMethods
//        Set<Map.Entry<RequestMappingInfo, HandlerMethod>> entries = handlerMethods.entrySet();
//        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : entries) {
//            RequestMappingInfo mappingInfo = entry.getKey();
//            HandlerMethod handlerMethod = entry.getValue();
//            // 输出Controller类名和方法名
//            String methodName=handlerMethod.getBeanType().getName()+"."+handlerMethod.getMethod().getName();
//
//            MethodParameter[] methodParameters=handlerMethod.getMethodParameters();
//
//            MethodParameter returnType=handlerMethod.getReturnType();
////            System.out.println(returnType.toString()+" " + mappingInfo);
//            StringBuilder sb=new StringBuilder();
//            for (MethodParameter methodParameter : methodParameters) {
//                if(sb.length()>1){
//                    sb.append(",");
//                }
//                sb.append(methodParameter.getParameterType());
//            }
//            System.out.println(mappingInfo+"  "+methodName+"("+sb.toString()+");");
//
//        }
    }

    public void updateEndpint() throws Exception {
        // 获取所有的HandlerMethods
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();

        // 遍历HandlerMethods
        Set<Map.Entry<RequestMappingInfo, HandlerMethod>> entries = handlerMethods.entrySet();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : entries) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            // 输出Controller类名和方法名
            String methodName=handlerMethod.getBeanType().getName()+"."+handlerMethod.getMethod().getName();

            MethodParameter[] methodParameters=handlerMethod.getMethodParameters();

            MethodParameter returnType=handlerMethod.getReturnType();
//            System.out.println(returnType.toString()+" " + mappingInfo);
            StringBuilder sb=new StringBuilder();
            for (MethodParameter methodParameter : methodParameters) {
                if(sb.length()>1){
                    sb.append(",");
                }
                sb.append(methodParameter.getParameterType());
            }
//            System.out.println(mappingInfo.toString());
            Map<String,Object> mapData= new HashMap<>();
            for (MethodParameter methodParameter :methodParameters) {
                Annotation[] annotations=methodParameter.getParameterAnnotations();
                for (Annotation annotation : annotations) {
                    if(annotation.annotationType().getName().equals("org.springframework.web.bind.annotation.RequestMapping")){
                        mapData.put("url",annotation.toString());
                    }
                }
            }
            mapData.put("method",methodName);
            mapData.put("content",mappingInfo.toString());
            neo4jService.save(mapData,"HttpApi");
//            System.out.println(mappingInfo+"  "+methodName+"("+sb.toString()+");");
            // 输出请求映射信息

//            System.out.println("----------------------------------");
        }
    }
}*/

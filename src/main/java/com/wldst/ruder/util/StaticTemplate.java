package com.wldst.ruder.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.wldst.ruder.crud.service.CrudUserNeo4jService;
@Component
public class StaticTemplate {
    private static Logger logger = LoggerFactory.getLogger(StaticTemplate.class);
    @Autowired
    private   TemplateEngine templateEngine;
    @Autowired
    private   CrudUserNeo4jService neo4jService;
    

/**
 * 构建静态化页面
 * @param id
 */
public void createHtml(String id,Map<String,Object> data){
    // 1. 上下文
    Context context = new Context();  //thymeleaf包下的
    // 1.1 存入数据
    context.setVariables(data);
    // 2 输出流
    String pathBy = neo4jService.getPathBy("staticFile");
    File file = new File(pathBy+File.separator+ id + ".html");
     if(!file.exists()) {
         try {
    	 file.getParentFile().mkdirs();
    	file.createNewFile();
        } catch (IOException e) {
    	// TODO Auto-generated catch block
    	e.printStackTrace();
        }
     }
    try(PrintWriter writer = new PrintWriter(file,"UTF-8")){ //流在小括号里面会被自动的释放
        //生成HTML
//        templateEngine.process(id,context,writer);
    }catch (Exception e){
        LoggerTool.error(logger,"静态页方法异常",e);
    }
}

}

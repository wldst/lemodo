package com.wldst.ruder.module.state.controller;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.wldst.ruder.util.ModelUtil;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.database.service.DbShowService;
import com.wldst.ruder.domain.SourceCodeDomain;
import com.wldst.ruder.util.RunCode;

import jakarta.servlet.http.HttpServletRequest;

//import io.swagger.annotations.ApiOperation;

/**
 * 后台用户管理 Created by macro on 2018/4/26.
 */
@Controller
@RequestMapping("${server.context}/code")
public class CodeController extends SourceCodeDomain {
    @Autowired
    private RunCode runCode;
    @Autowired
    private DbShowService dbShowService;
    @Autowired
    private CrudNeo4jService crudService;

    @RequestMapping(value = "/run/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result run(@PathVariable("id") String id, @RequestBody JSONObject vo) {
	Object[] args = null;
	if (vo != null) {
	    args = new Object[vo.keySet().size()];
	    int i = 0;
	    for (Entry<String, Object> ei : vo.entrySet()) {
		args[i] = ei.getValue();
		i++;
	    }
	}
	Map<String, Object> propMapBy = crudService.getPropMapBy(id);
	runCode.setClassPath(string(propMapBy, CLASS_PATH));

	String callClassMethod = "";
	try {
	    String sourceCode = string(propMapBy, SOURCE_CODE);
	    String method = string(propMapBy, METHOD_NAME);
	    String className = string(propMapBy, CLASS_NAME);
	    if (args != null) {
		callClassMethod = runCode.callClassMethod(sourceCode,
			className, method, args);
	    } else {
		callClassMethod = runCode.callClassMethod(sourceCode,
			className, method);
	    }
	    return Result.success(callClassMethod);
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	    callClassMethod = "找不到类";
	} catch (InstantiationException e) {
	    e.printStackTrace();
	    callClassMethod = "实例化异常";
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	    callClassMethod = "非法访问";
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	    callClassMethod = "没有这份方法";
	} catch (InvocationTargetException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    callClassMethod = "调用异常";
	}

	return Result.failed(callClassMethod);
    }
    
    @RequestMapping(value = "/java/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public String java(Model model,@PathVariable("id") String id,
	    HttpServletRequest request)
	    throws Exception {
	Map<String, Object> propMapBy = crudService.getPropMapBy(id);
	String string = string(propMapBy, CLASS_PATH);
	if(string!=null) {
	    runCode.setClassPath(string);
	}
	
	  String  sourceCode = string(propMapBy, SOURCE_CODE);
	
	
	String callClassMethod = "";
	try {
	    
	    String method = string(propMapBy, METHOD_NAME);
	    String className = string(propMapBy, CLASS_NAME);
	   
	    Object[] args = array(propMapBy, PARAMS);
	    
	    if (args != null) {
		callClassMethod = runCode.callClassMethod(sourceCode,
			className, method, args);
	    } else {
		callClassMethod = runCode.callClassMethod(sourceCode,
			className, method);
	    }
	} catch (MalformedURLException e) {
	    e.printStackTrace();
	    callClassMethod = "调用异常"+e.getMessage();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	    callClassMethod = "找不到类";
	} catch (InstantiationException e) {
	    e.printStackTrace();
	    callClassMethod = "实例化异常";
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	    callClassMethod = "非法访问";
	} catch (NoSuchMethodException e) {
	    e.printStackTrace();
	    callClassMethod = "没有这份方法"+e.getMessage();
	} catch (InvocationTargetException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    callClassMethod = "调用异常";
	}
	model.addAttribute("javaResult", callClassMethod);
	return "code/javaResult";
    }
    
    @RequestMapping(value = "/runJava", method = { RequestMethod.GET, RequestMethod.POST })
    public String runJava(Model model, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> dataMap = new HashMap<>();
	dataMap.put(CLASS_NAME, "Hello");
	dataMap.put(METHOD_NAME, "say");
	dataMap.put(PARAMS, "main");
	dataMap.put(SOURCE_CODE, """
			public class Hello {
        			 
			 }
			
			""");
	Node saveByBody = crudService.saveByBody(dataMap, LABLE_JAVA_CODE);
	model.addAttribute("javaCodeId", saveByBody.getId());
	model.addAttribute("label", LABLE_JAVA_CODE);
	
	dbShowService.defaultLayui(model, dataMap);
	ModelUtil.setKeyValue(model, dataMap);
	return "code/javaExecute";
    }

}

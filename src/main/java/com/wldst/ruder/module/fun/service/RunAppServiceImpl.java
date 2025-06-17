package com.wldst.ruder.module.fun.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.util.MapTool;

@Service
public class RunAppServiceImpl extends MapTool implements RunAppService {
    private static Logger logger = LoggerFactory.getLogger(RunAppServiceImpl.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Override
    public void runPrograms(List<Map<String, Object>> openTasks) {
	if (openTasks != null && !openTasks.isEmpty()) {	      
	    for (Map<String, Object> oi : openTasks) {
		runExe(oi);
	    }
	}
    }
    @Override
    public void runApp(List<Map<String, Object>> openTasks) {
  	if (openTasks != null && !openTasks.isEmpty()) {	      
  	    for (Map<String, Object> oi : openTasks) {
  		run(oi);
  	    }
  	}
      }
    
    public void runExe(Map<String, Object> oi) {
	if(oi==null||oi.isEmpty()) {
	    return;
	}
	String string = string(oi,"path");
	if(string==null||string.equals("")) {
	    return;
	}
	List<String> cmdArgs = new ArrayList<>();
	String params = string(oi,"params");
	if(params!=null) {
	    String[] split = params.split(",");
		for(String pi:split) {
		    cmdArgs.add(pi.trim());
		}
	}
	
	
	AppRun jrp = new AppRun();
	jrp.runCmd(string,cmdArgs);
    }
    @Override
    public void run(Map<String, Object> oi) {
	if(oi==null||oi.isEmpty()) {
	    return;
	}
	String string = string(oi,"exe");
	if(string==null||string.equals("")) {
	    return;
	}
	List<String> cmdArgs = new ArrayList<>();
	String params = string(oi,"params");
	if(params!=null) {
	    String[] split = params.split(",");
		for(String pi:split) {
		    cmdArgs.add(pi.trim());
		}
	}
	
	
	AppRun jrp = new AppRun();
	jrp.runCmd(getProgram(string),cmdArgs);
    }
    
    public void explore(Map<String, Object> oi) {
	String string = string(oi,"path");
	
	if(string==null||string.equals("")) {
	    string = string(oi,"fileStoreName");
	    if(string==null||string.equals("")) {
	    return;
	    }else {
		//默认程序设置.
		String fileType = string(oi,"FileType");
		Map<String,Object> def = neo4jService.getAttMapBy("fileType", fileType, "DefaultApp");
		if(def==null) {		    
		    def = neo4jService.getAttMapBy("fileType", string.split("\\.")[1], "DefaultApp");
		}
		String byId = neo4jService.getById(longValue(def,"exe"), "path");
		if(byId!=null) {
		    List<String> cmdArgs = new ArrayList<>();
		    cmdArgs.add(string);
		    AppRun jrp = new AppRun();
		    jrp.runCmd(byId,cmdArgs);
		    return;
		}
	    }
	}
	AppRun jrp = new AppRun();
	jrp.exploreFile(string);
    }
    public void explore(List<Map<String, Object>> openTasks) {
	if (openTasks != null && !openTasks.isEmpty()) {	      
	    for (Map<String, Object> oi : openTasks) {
		explore(oi);
	    }
	}
    }

    private String getProgram(String string) {
	try {
	    String byId = neo4jService.getById(Long.valueOf(string), "path");
	    if(byId!=null) {
		return byId;
	    }
	}catch (Exception e) {
	    LoggerTool.info(logger,"获取程序路径错误",e);
	}
	
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(NAME, string, "LocalProgram");
	if(attMapBy!=null) {
	    return string(attMapBy,"path");
	}else {
	    attMapBy = neo4jService.getAttMapBy(CODE, string, "LocalProgram");
	}
	 
	if(attMapBy!=null) {
	    return string(attMapBy,"path");
	}
	else {
	    Long longId = Long.valueOf(string);
	    return string(neo4jService.getNodeMapById(longId),"path");
	}
    }
    @Override
    public void runApp(String ids) {
	// TODO Auto-generated method stub
	 List<Map<String,Object>> apps = new ArrayList<>();
	if(ids.contains(",")) {
	    String[] split = ids.split(",");
	   
	    for(String si: split) {
		apps.add(neo4jService.getNodeMapById(si));
	    }
	}else {
	    apps.add( neo4jService.getNodeMapById(ids));
	}
	runPrograms(apps);
    }
    @Override
    public void explore(String ids) {
	 List<Map<String,Object>> apps = new ArrayList<>();
		if(ids.contains(",")) {
		    String[] split = ids.split(",");
		   
		    for(String si: split) {
			apps.add(neo4jService.getNodeMapById(si));
		    }
		}else {
		    apps.add( neo4jService.getNodeMapById(ids));
		}
		explore(apps);
    }

}

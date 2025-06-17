package com.wldst.ruder.module;

import com.wldst.ruder.util.ModelUtil;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.wldst.ruder.LemodoApplication;
 
/**
 * Created by  liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/ui/element")
public class ElementUIController {
    @RequestMapping(value = "/login", method = {RequestMethod.GET,RequestMethod.POST})
    public String login(Model model,String table,HttpServletRequest request)throws Exception{
    	return "element/login";
    }
   
    
    @RequestMapping(value = "/instance", method = {RequestMethod.GET,RequestMethod.POST})
    public String instance(Model model,String table,HttpServletRequest request)throws Exception{
        
        
    	return "element/instance";
    }
    
    @RequestMapping(value = "/relation", method = {RequestMethod.GET,RequestMethod.POST})
    public String hostResource(Model model,String table,HttpServletRequest request)throws Exception{
        
    	return "relation";
    }
    
	@RequestMapping(value = "/crud", method = {RequestMethod.GET,RequestMethod.POST})
    public String tailLogMonitor(Model model,
    		@RequestParam String logFile,
    		@RequestParam String app,
    		HttpServletRequest request)throws Exception{
        
    	return "crud";
    }
	
	@RequestMapping(value = "/demo", method = {RequestMethod.GET,RequestMethod.POST})
    public String demo(Model model)throws Exception{
        
    	return "element/demo";
    }
	@RequestMapping(value = "/main", method = {RequestMethod.GET,RequestMethod.POST})
    public String search(Model model)throws Exception{
        
    	return "element/main";
    }
	
	@RequestMapping(value = "/searchDomain", method = {RequestMethod.GET,RequestMethod.POST})
    public String searchDomain(Model model)throws Exception{
        
    	return "vue/searchDomain";
    }
	
	@RequestMapping(value = "/excute", method = {RequestMethod.GET,RequestMethod.POST})
    public String operate(Model model,HttpServletRequest request)throws Exception{
        
    	return "vue/excute";
    }
	
}

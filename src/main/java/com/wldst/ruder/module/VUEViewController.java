package com.wldst.ruder.module;

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
@RequestMapping("${server.context}/vue")
public class VUEViewController {
	
	
    @RequestMapping(value = "/po", method = {RequestMethod.GET,RequestMethod.POST})
    public String po(Model model,String table,HttpServletRequest request)throws Exception{
    	
	return "po";
    }
    @RequestMapping(value = "/poSelect", method = {RequestMethod.GET,RequestMethod.POST})
    public String poSelect(Model model,String table,HttpServletRequest request)throws Exception{
    	return "poSelect";
    }
    
    @RequestMapping(value = "/bigDomain", method = {RequestMethod.GET,RequestMethod.POST})
    public String bigDomain(Model model,String table,HttpServletRequest request)throws Exception{
    	return "angular/bigDomain";
    }
    
    @RequestMapping(value = "/instance", method = {RequestMethod.GET,RequestMethod.POST})
    public String instance(Model model,String table,HttpServletRequest request)throws Exception{
    	return "instance";
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
    public String demo()throws Exception{
    	return "vue/";
    }
	@RequestMapping(value = "/search", method = {RequestMethod.GET,RequestMethod.POST})
    public String search()throws Exception{
    	return "vue/search";
    }
	
	@RequestMapping(value = "/searchDomain", method = {RequestMethod.GET,RequestMethod.POST})
    public String searchDomain()throws Exception{
    	return "vue/searchDomain";
    }
	
	@RequestMapping(value = "/excute", method = {RequestMethod.GET,RequestMethod.POST})
    public String operate(Model model,HttpServletRequest request)throws Exception{
    	return "vue/excute";
    }
	
}

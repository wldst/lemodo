package com.wldst.ruder.module.fun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.wldst.ruder.LemodoApplication;

import jakarta.servlet.http.HttpServletRequest;
 
/**
 * Created by  liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/view")
public class ViewController {
	
    @RequestMapping(value = "/po", method = {RequestMethod.GET,RequestMethod.POST})
    public String po(Model model,String table,HttpServletRequest request)throws Exception{
    	return "po";
    }
    @RequestMapping(value = "/admin", method = {RequestMethod.GET,RequestMethod.POST})
    public String admin(Model model,String table,HttpServletRequest request)throws Exception{
    	return "admin";
    }
    
    @RequestMapping("/webrtc/{username}.html")
    public ModelAndView socketChartPage(@PathVariable String username) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("webrtc/webrtc.html");
        modelAndView.addObject("username",username);
        return modelAndView;
    }
    
    @RequestMapping(value = "/v", method = {RequestMethod.GET,RequestMethod.POST})
    public String view(Model model,String table,HttpServletRequest request)throws Exception{
    	return "view";
    }
    @RequestMapping(value = "/time", method = {RequestMethod.GET,RequestMethod.POST})
    public String timeIndex(Model model,String table,HttpServletRequest request)throws Exception{
    	return "timeseries/index";
    }
    @RequestMapping(value = "/timeDiv", method = {RequestMethod.GET,RequestMethod.POST})
    public String timeDiv(Model model,String table,HttpServletRequest request)throws Exception{
    	return "timeseries/timeDiv";
    }
    
    @RequestMapping(value = "/geditor", method = {RequestMethod.GET,RequestMethod.POST})
    public String geditor(Model model,String table,HttpServletRequest request)throws Exception{
    	return "geditor/hello";
    }
    
    @RequestMapping(value = "/domainDiv", method = {RequestMethod.GET,RequestMethod.POST})
    public String domainDiv(Model model,String table,HttpServletRequest request)throws Exception{
    	return "div/domainDiv";
    }
    
    @RequestMapping(value = "/poDiv", method = {RequestMethod.GET,RequestMethod.POST})
    public String poDiv(Model model,String table,HttpServletRequest request)throws Exception{
    	
    	return "div/poDiv";
    }
    
    @RequestMapping(value = "/bigDomain", method = {RequestMethod.GET,RequestMethod.POST})
    public String bigDomain(Model model,String table,HttpServletRequest request)throws Exception{
    	return "angular/bigDomain";
    }
    
	@RequestMapping(value = "/interface", method = { RequestMethod.GET, RequestMethod.POST })
    public String domainInterface(Model model,String table,HttpServletRequest request)throws Exception{
    	return "angular/interface";
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
    
	
}

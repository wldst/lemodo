package com.wldst.ruder.module.fun.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.service.AppRun;
import com.wldst.ruder.module.fun.service.RunAppService;
import com.wldst.ruder.module.fun.service.SceneManager;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 操作控制器：读取相应场景下的数据
 * 
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/program")
public class ProgramController extends BeanShellDomain {

    @Autowired
    private CrudUserNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private RunAppService runService;

    private Map<Long, SceneManager> sceneStack = new HashMap<>();
 
    
    @RequestMapping(value = "/run/{programId}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> run(Model model,@PathVariable("programId") String programId, HttpServletRequest request) throws Exception {
	 
	Map<String, Object> program = neo4jService.getNodeMapById(Long.valueOf(programId));
	String string = string(program,"path");
	AppRun jrp = new AppRun();
	jrp.runCmd(string,null);
	 
	return Result.success();
    }
    
    @RequestMapping(value = "/open/{openId}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> open(Model model,@PathVariable("openId") String openId, HttpServletRequest request) throws Exception {
	runService.runApp(openId);
	return Result.success();
    }
    @RequestMapping(value = "/explore/{openId}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> explore(Model model,@PathVariable("openId") String openId, HttpServletRequest request) throws Exception {
	runService.explore(openId);
	return Result.success();
    }
    
}

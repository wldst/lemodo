package com.wldst.ruder.module.schedule.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.schedule.BeanShellScheduler;
import com.wldst.ruder.util.MapTool;


/**
 * 执行定时任务
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/scheduled")
public class ScheduleBsController extends MapTool{
    @Autowired
    private ApplicationContext context;
    @Autowired
    private CrudNeo4jService cronRepository;
    @Autowired
    private BeanShellScheduler bss;
    
    /**
     * 执行定时任务
     */
    @ResponseBody
    @RequestMapping("/runTaskCron")
    public Result<Void> runTaskCron(@RequestBody JSONObject vo) throws Exception {
//        ((ScheduledOfTask) ).execute();
	// 读取定时任务数据
	Map<String, Object> queryBy = cronRepository.getPropMapBy(string(vo,ID));
	if(queryBy==null) {
	    bss.taskRefresh();
	}else {
	    bss.scheduleTask(queryBy);
	}
        return Result.success();
    }
    
    @ResponseBody
    @RequestMapping("/stop")
    public Result<Void> stop(@RequestBody Map<String,Object> vo) throws Exception {
        //        ((ScheduledOfTask) ).execute();
        // 读取定时任务数据
        bss.stopTask(vo);
        return Result.success();
    }

    @ResponseBody
    @RequestMapping(value="/runList", method = {RequestMethod.POST,RequestMethod.GET})
    public Result<List<String>> runList() throws Exception {
        //        ((ScheduledOfTask) ).execute();
        // 读取定时任务数据
        List<String> strings=bss.runList();
        return Result.success(strings);
    }
   
}

package com.wldst.ruder.module.schedule.job;

import java.util.Date;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.util.MapTool;

import bsh.EvalError; 
public class BeanShellJob extends BeanShellDomain implements Job {
      
    @Override  
    public void execute(JobExecutionContext context) throws JobExecutionException { 
	BeanShellService service=(BeanShellService) SpringContextUtil.getBean(BeanShellService.class);  
	CrudNeo4jService repo=(CrudNeo4jService) SpringContextUtil.getBean("crudNeo4jService");  
        JobDataMap mergedJobDataMap = context.getMergedJobDataMap();
	String param1Value = mergedJobDataMap.getString("name");  
        String beanShell = mergedJobDataMap.getString("BeanShell");
        Map<String, Object> attMapBy = repo.getNodeMapById(Long.valueOf(beanShell));
//        mergedJobDataMap.
        Map<String,Object> mp = MapTool.newMap();
        mp.put("name", param1Value);
        mp.put(LABEL, param1Value);
        mp.put(BS_SCRIPT, string(attMapBy,BS_SCRIPT));
        try {
	    service.run(mp);
	    // 执行任务逻辑  
	    System.out.println("Executing task " + context.getJobDetail().getJobClass().getSimpleName() + " at " + new Date());  
	} catch (EvalError e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
       
    }
  
}

package com.wldst.ruder.module.schedule;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.state.service.StateService;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.CronDomain;
import com.wldst.ruder.module.schedule.job.BeanShellJob;

@Component
public class BeanShellScheduler extends CronDomain{
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private StateService statusService;
   private  Scheduler scheduler;
    // public static void main(String[] args) throws SchedulerException {
    // taskRun();
    // }

    /**
     * 任务刷新，停止的停掉，该运行的，启动。
     * @throws SchedulerException
     */
    public void taskRefresh() throws SchedulerException{
        Map<String, Object> params=newMap();
        // 读取定时任务数据
        List<Map<String, Object>> queryBy=neo4jService.queryBy(params, CRON_TASK_LABEL);

        // 动态创建定时任务和删除定时任务
        for(Map<String, Object> task : queryBy){
            String statusCode=statusCode(task);
//            if(statusCode.equals("ended")){
//                scheduleTask(task);
//            }
            if(statusCode.equals("InProgress")){
                //运行时中没有当前任务，则启动执行中的定时任务
                if(!runList().contains(code(task))){
                    scheduleTask(task);
                }
            }else if(statusCode.equals("stop")||statusCode.equals("ended")){
                stopTask(task);
            }
        }
    }

    public List<String> runList(){
        if(scheduler==null){
            try{
                scheduler=StdSchedulerFactory.getDefaultScheduler();
            }catch(SchedulerException e){
                throw new RuntimeException(e);
            }
        }
        List<String> jobNames = new ArrayList<>();
        try{
            List<JobExecutionContext> currentlyExecutingJobs=scheduler.getCurrentlyExecutingJobs();
            for(JobExecutionContext jobi: currentlyExecutingJobs){
                jobi.getFireTime();
                jobi.getJobRunTime();
                String jobName = jobi.getJobDetail().getKey().getName();
                jobNames.add(jobName);
            }
        }catch(SchedulerException e){
            throw new RuntimeException(e);
        }
        return jobNames;
    }


    public String statusCode(Map<String, Object> task){
        return code(statusService.currentStatus(id(task)));
    }

    public void scheduleTask(Map<String, Object> task) throws SchedulerException{
        if(scheduler==null){
            scheduler=StdSchedulerFactory.getDefaultScheduler();
        }

        JobDetail job=JobBuilder.newJob(BeanShellJob.class)
                .withIdentity(code(task), "CronTask") // Job的唯一标识，可以自定义
                .build();

        CronTriggerImpl trigger=new CronTriggerImpl();
        try{
            trigger.setName(code(task)+"-trigger");
            // "0 0 12 * * ?"
            trigger.setCronExpression(string(task, "CronExpress"));
            // 设置参数
            job.getJobDataMap().putAll(task);

            scheduler.scheduleJob(job, trigger);
            scheduler.start();
        }catch(ParseException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        } // 在每天的12点执行
    }

    public void stopTask(Map<String, Object> task) throws SchedulerException{
        if(scheduler==null){
            scheduler=StdSchedulerFactory.getDefaultScheduler();
        }

        JobKey jobkey=new JobKey(code(task), "CronTask");
        scheduler.deleteJob(jobkey);
    }

}

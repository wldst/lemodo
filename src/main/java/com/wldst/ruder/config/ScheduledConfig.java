package com.wldst.ruder.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.CronDomain;
import com.wldst.ruder.util.MapTool;

@Configuration
public class ScheduledConfig extends CronDomain implements SchedulingConfigurer {

    private ApplicationContext context;

    private CrudUserNeo4jService cronRepository;

    @Autowired
    public ScheduledConfig(ApplicationContext context, CrudUserNeo4jService cronRepository) {
        this.context=context;
        this.cronRepository = cronRepository;
    }
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        List<Map<String, Object>> listAllByLabel = cronRepository.listAllByLabel(CRON_TASK_LABEL);
        if(listAllByLabel==null||listAllByLabel.isEmpty()) {
            return;
        }
	for (Map<String, Object> job : listAllByLabel) {
            Class<?> clazz;
            Object task;
            String cronKey = null;
            try {
                cronKey = MapTool.string(job, CRON_KEY);
                if(cronKey==null) {
                    continue;
                }
                clazz = Class.forName(cronKey);
                task = context.getBean(clazz);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("数据" + cronKey + "有误", e);
            } catch (BeansException e) {
                throw new IllegalArgumentException(cronKey + "未纳入到spring管理", e);
            }
//            string(job,"test");
            //
            // 可以通过改变数据库数据进而实现动态改变执行周期
            taskRegistrar.addTriggerTask(((Runnable) task),
                    triggerContext -> {
                        String cronExpression = MapTool.string(job, CRON_EXPRESS);                        
                        return new CronTrigger(cronExpression).nextExecution(triggerContext);
                        	//.nextExecution(triggerContext);
                    }
            );
        }
    }
    @Bean
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(10);
    }
    
  //自定义线程池
    @Bean(name = "hyqThreadPoolTaskExecutor")
    public TaskExecutor  getMyThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(20);
        taskExecutor.setMaxPoolSize(200);
        taskExecutor.setQueueCapacity(25);
        taskExecutor.setKeepAliveSeconds(200);
        taskExecutor.setThreadNamePrefix("hyq-threadPool-");
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(60);
        taskExecutor.initialize();
        return taskExecutor;
    }
    
 // lambda表达式形式
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
//        taskRegistrar.addTriggerTask(() -> {
//            System.err.println("【动态】执行定时任务：" + LocalTime.now().toString() + "\n");
//        }, (triggerContext) -> {
//            Integer id = 1;
//            String cron = scheduleMapper.getCronById(id);
//            System.out.println("cron表达式为：" + cron);
//            // 此处的cron可以从数据库中获取   重点
//            return new CronTrigger(cron).nextExecutionTime(triggerContext);
//        });
//    }
}

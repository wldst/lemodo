package com.wldst.ruder.module.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
public class Schedule {
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    private int i;

//    @Scheduled(cron = "*/15 * * * * ?")
//    //,fixedRate = 1000*10,initialDelay = 1000*20
//    @Async("hyqThreadPoolTaskExecutor")
//    public void test(){
//        System.out.println(Thread.currentThread().getName()+"--->xxxxx--->"+Thread.currentThread().getId());
//        LoggerTool.info(logger,"thread id:{},FixedPrintTask execute times:{}", Thread.currentThread().getId(), ++i);
//    }

}

package com.wldst.ruder.module.schedule.job;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.wldst.ruder.util.DateUtil;

@Component
public class HelloWorldJob implements Runnable{

    @Override
    public void run(){
        System.out.println("欢迎使用yyblog,这是一个定时任务  --小卖铺的老爷爷!"+ DateUtil.dateToStrLong(new Date()));
    }

}

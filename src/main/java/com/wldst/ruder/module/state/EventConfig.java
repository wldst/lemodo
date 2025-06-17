package com.wldst.ruder.module.state;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.annotation.OnTransition;
import org.springframework.statemachine.annotation.WithStateMachine;

@WithStateMachine
public class EventConfig {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @OnTransition(target = "UNPAID")
    public void create() {
        LoggerTool.info(logger,"订单创建，待支付");
    }

    @OnTransition(source = "UNPAID", target = "WAITING_FOR_RECEIVE")
    public void pay() {
        LoggerTool.info(logger,"用户完成支付，待收货");
    }

    @OnTransition(source = "WAITING_FOR_RECEIVE", target = "DONE")
    public void receive() {
        LoggerTool.info(logger,"用户已收货，订单完成");
    }
    
    @OnTransition(source = "SALE", target = "DONE")
    public void sale() {
        LoggerTool.info(logger,"用户已收货，订单完成");
    }
    
    @OnTransition(source = "SHARE", target = "DONE")
    public void share() {
        LoggerTool.info(logger,"用户已共享，订单完成");
    }

}

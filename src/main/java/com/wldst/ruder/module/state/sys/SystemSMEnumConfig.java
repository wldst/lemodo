package com.wldst.ruder.module.state.sys;

import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import com.wldst.ruder.module.goods.GoodsService;
import com.wldst.ruder.util.NetHandleUtil;
import com.wldst.ruder.util.RestApi;

@Configuration
@EnableStateMachine(name = "SystemSM")
public class SystemSMEnumConfig extends EnumStateMachineConfigurerAdapter<SystemStates, SystemEvents> {
    private static Logger logger = LoggerFactory.getLogger(SystemSMEnumConfig.class);
    @Override
    public void configure(StateMachineStateConfigurer<SystemStates, SystemEvents> states) throws Exception {
	states.withStates().initial(SystemStates.STARTING).states(EnumSet.allOf(SystemStates.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<SystemStates, SystemEvents> transitions) throws Exception {
	transitions.withExternal().source(SystemStates.STARTING).target(SystemStates.UNINIT).event(SystemEvents.STARTUP)
		.and().withExternal().source(SystemStates.UNINIT).target(SystemStates.INITED).event(SystemEvents.INIT)
		.and().withExternal().source(SystemStates.INITED).target(SystemStates.STARTED)
		.event(SystemEvents.READY);

    }
   
    @Override
    public void configure(StateMachineConfigurationConfigurer<SystemStates, SystemEvents> config) throws Exception {
	config.withConfiguration().listener(listener());
    }

    @Bean("SystemListener")
    public StateMachineListener<SystemStates, SystemEvents> listener() {
	return new StateMachineListenerAdapter<SystemStates, SystemEvents>() {
	    @Autowired
	    private GoodsService initSystem;
		@Autowired
		private CrudNeo4jService neo4jService;

	    @Override
	    public void transition(Transition<SystemStates, SystemEvents> transition) {
		State<SystemStates, SystemEvents> target = transition.getTarget();
		if (target.getId() == SystemStates.STARTED) {
		    //记录与服务器的通信结果信息
		    LoggerTool.info(logger,"系统已启动");
			logInit();
			return;
		}
		State<SystemStates, SystemEvents> source = transition.getSource();
		if (source != null && target != null) {
			logInit();
		    if (source.getId() == SystemStates.STARTING && target.getId() == SystemStates.UNINIT) {
			LoggerTool.info(logger,"开始启动");
			return;
		    }
		    if (source.getId() == SystemStates.UNINIT && target.getId() == SystemStates.INITED) {
			initSystem.init();
			LoggerTool.info(logger,"完成初始化");
			return;
		    }
		    if (source.getId() == SystemStates.INITED && target.getId() == SystemStates.STARTED) {
			LoggerTool.info(logger,"系统准备好了");
			return;
		    }
		}
	    }
		private void logInit(){
			String lo=neo4jService.getSettingBy("logOpen");
			Boolean openLog = lo!=null&&Boolean.valueOf(lo.trim());
			LoggerTool.setOpenLog(openLog);
		}

	};
    }


}
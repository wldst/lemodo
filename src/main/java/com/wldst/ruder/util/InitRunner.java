package com.wldst.ruder.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

import com.wldst.ruder.module.state.Events;
import com.wldst.ruder.module.state.States;
import com.wldst.ruder.module.state.sys.SystemEvents;
import com.wldst.ruder.module.state.sys.SystemStates;

@Component
public class InitRunner implements CommandLineRunner {
	@Autowired
	private StateMachine<States, Events> stateMachine;
	@Autowired
	private StateMachine<SystemStates, SystemEvents> sysStateMachine;

	@Override
	public void run(String... args) throws Exception {
	    sysStateMachine.start();
	    sysStateMachine.sendEvent(SystemEvents.STARTUP);
	    sysStateMachine.sendEvent(SystemEvents.INIT);
	}
}

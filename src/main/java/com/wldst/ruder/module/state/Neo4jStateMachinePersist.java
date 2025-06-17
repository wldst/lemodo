package com.wldst.ruder.module.state;

import java.util.HashMap;
import java.util.Map;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
@Component
public class Neo4jStateMachinePersist  implements StateMachinePersist<Long, Long, String>{
	private Map<String,StateMachineContext<Long, Long>> smMap = new HashMap<>();

	@Override
	public void write(StateMachineContext<Long, Long> context, String contextObj) throws Exception {
		// TODO Auto-generated method stub
		smMap.put(contextObj, context);
	}

	@Override
	public StateMachineContext<Long, Long> read(String contextObj) throws Exception {
		// TODO Auto-generated method stub
		return smMap.get(contextObj);
	}

}

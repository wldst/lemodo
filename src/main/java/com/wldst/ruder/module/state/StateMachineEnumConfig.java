package com.wldst.ruder.module.state;

import java.util.EnumSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.transition.Transition;


@Configuration
@EnableStateMachine
public class StateMachineEnumConfig extends EnumStateMachineConfigurerAdapter<States,Events>{
 
	    @Override
	    public void configure(StateMachineStateConfigurer<States,Events> states)
	            throws Exception {
	        states
	            .withStates()
	                .initial(States.UNPAID)	
	                .states(EnumSet.allOf(States.class));
	    }
 
	    @Override
	    public void configure(StateMachineTransitionConfigurer<States,Events> transitions)
	            throws Exception {
	        transitions
	            .withExternal()
	                .source(States.UNPAID).target(States.WAITING_FOR_RECEIVE)
	                .event(Events.PAY)
	                .and()
	            .withExternal()
	                .source(States.WAITING_FOR_RECEIVE).target(States.DONE)
	                .event(Events.RECEIVE);
	    }
 
	    @Override
	    public void configure(StateMachineConfigurationConfigurer<States,Events> config)
	            throws Exception {
	        config
	            .withConfiguration()
	                .listener(listener());
	    }
 
	    @Bean
	    public StateMachineListener<States,Events> listener() {
	        return new StateMachineListenerAdapter<States,Events>() {
 
	            @Override
	            public void transition(Transition<States,Events> transition) {
	                if(transition.getTarget().getId() == States.UNPAID) {
	                   System.out.println("订单创建，待支付");
	                    return;
	                }
 
	                if(transition.getSource().getId() == States.UNPAID
	                        && transition.getTarget().getId() == States.WAITING_FOR_RECEIVE) {
	                   System.out.println("用户完成支付，待收货");
	                    return;
	                }
 
	                if(transition.getSource().getId() == States.WAITING_FOR_RECEIVE
	                        && transition.getTarget().getId() == States.DONE) {
	                   System.out.println("用户已收货，订单完成");
	                    return;
	                }
	            }
 
	        };
	    }
}
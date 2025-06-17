package com.wldst.ruder.module.state.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

import com.wldst.ruder.module.state.Neo4jStateMachinePersist;

@Configuration
public class PersistConfig {


    @Autowired
    private Neo4jStateMachinePersist neo4jSMPersist;

    /**
     * 注入StateMachinePersister对象
     *
     * @return
     */
    @Bean(name="smPersister")
    public StateMachinePersister<Long,Long, String> getPersister() {
        return new DefaultStateMachinePersister<>(neo4jSMPersist);
    }

}

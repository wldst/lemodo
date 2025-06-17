package com.wldst.ruder.module.ai;

import com.wldst.ruder.module.ai.container.Agent;
import java.util.Map;
import java.util.HashMap;

/**
 *
 */
public class MultiAgentEnvironment {
    public static void main(String[] args) {
        Map<String,Object> data = new HashMap<>();
        // 创建几个智能体
        data.put("prompt", "Agent1");
        Agent agent1 = new Agent(data);
        data.put("prompt", "Agent2");
        Agent agent2 = new Agent(data);

        // 智能体之间的通信
        agent1.sendMessage(agent2.getId(), "Hello from Agent1");
        agent2.sendMessage(agent1.getId(), "Hello from Agent2");

        // 智能体根据接收到的信息做出决策
        String decision1 = agent1.makeDecision();
        String decision2 = agent2.makeDecision();

        // 执行决策
        agent1.executeDecision(decision1);
        agent2.executeDecision(decision2);
    }
}

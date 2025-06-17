package com.wldst.ruder.module.ai.container;

import java.util.Map;

public interface IAgent {
    // 智能体接收信息并更新知识库
    void receiveTask(String task);
    String getResult();
    // 智能体根据知识库做出决策
    String makeDecision();

    // 智能体执行决策
    void executeDecision(String decision) ;

    // 智能体可以发送消息给其他智能体
    void sendMessage(String recipientId, String message);

    String getId();

    void setId(String id);

    Map<String, Object> getKnowledgeBase();
    void setKnowledgeBase(Map<String, Object> knowledgeBase);

}

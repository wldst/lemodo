package com.wldst.ruder.module.ai.container;

import com.alibaba.fastjson2.JSON;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.module.ai.service.AiService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 如何与智能体关联上？获取自己的角色信息，提示词模版。
 *
 */
public class Agent extends MessageCenter implements MessageObserver, MsgSubject, IAgent {

    private String id;
    private Map<String, Object> knowledgeBase; // 智能体的知识库
    private Map<String, Object> agenti;
    private String ressult;
    private AiService aiService;

    public Agent(Map<String, Object> agenti) {
        super();
       this.agenti = agenti;
       this.id=stringId(agenti);
        aiService = (AiService)SpringContextUtil.getBean(AiService.class);
        this.knowledgeBase = new HashMap<>();
    }

    // 智能体接收信息并更新知识库
    @Override
    public void receiveTask(String task) {
        System.out.println("【"+name(agenti)+"】Received message: " + task);
        // ...接收任务的逻辑
        if (taskRequiresModel(task)) {
            //根据AgentID 获取Agent的提示词，更新提示词
            String prompt = MapTool.string(agenti, "prompt");
            String duty = MapTool.string(agenti, "duty");

           StringBuilder sb = new StringBuilder();
           sb.append(prompt.replace("${userInput}", task));
           sb.append(duty);
            // 如果任务需要调用大模型
            String modelResponse = aiService.callAIByMsg(sb.toString());
            // 处理大模型的响应
            ressult = modelResponse;
            // 解析消息并更新知识库
            // 这里可以集成大模型的API来处理自然语言
            // 例如，使用OpenAI的GPT-3或其他类似的服务

            System.out.println("Updated knowledge base: " + knowledgeBase);
            // 示例代码，直接将消息添加到知识库中
            // 这里可以集成大模型的API来处理自然语言
            // 例如，使用OpenAI的GPT-3或其他类似的服务
            knowledgeBase.put("message", task);
            knowledgeBase.put("ressult", ressult);
        }
    }

    @Override
    public String getResult() {
        return ressult;
    }

    // 判断任务是否需要调用大模型
    private boolean taskRequiresModel(String task) {
        // 实现判断逻辑
        return true; // 假设所有任务都需要调用大模型
    }

    // 智能体根据知识库做出决策
    public String makeDecision() {
        // 这里可以集成大模型的API来生成决策
        String task = "Generate a decision for " + JSON.toJSONString(knowledgeBase);
        String modelResponse = aiService.callAIByMsg(task);
        // 例如，使用深度学习模型来分析知识库并做出决策
        // 简化示例，随机生成一个决策
        return "Decision is: " + modelResponse;
    }

    // 智能体执行决策
    public void executeDecision(String decision) {
        String modelResponse = aiService.callAIByMsg(decision);
        // 执行决策
        // 例如，根据决策执行相应的操作 处理大模型的响应
        System.out.println(id + " is executing: " + decision);
    }

    @Override
    public void sendMessage(String recipientId, String message) {

    }

    public Map<String, Object> getKnowledgeBase() {
        return knowledgeBase;
    }

    public void setKnowledgeBase(Map<String, Object> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }
    public void updateKnowledgeBase(Map<String, Object> knowledgeBase) {
        // 活动中心根据消息，找到对应的智能体，将消息发送给智能体
        this.knowledgeBase.putAll(knowledgeBase);
    }

    @Override
    public void onNewMessage(Message message) {
        if (message.receiver().equals(id)) {// 发送给自己的消息
            String content = message.content();
            System.out.println("Agent " + id + " receive message:" + content);

            String modelResponse = aiService.callAIByMsg(content);
            // 处理大模型的响应
            ressult = modelResponse;
            // 示例代码，直接将消息添加到知识库中
            System.out.println("Updated knowledge base: " + knowledgeBase);
            // 示例代码，直接将消息添加到知识库中
            // 这里可以集成大模型的API来处理自然语言
            // 例如，使用OpenAI的GPT-3或其他类似的服务
            if (content != null && !content.isEmpty()) {
                if (!ressult.equals(knowledgeBase.get(content))) {
                    knowledgeBase.put(content, ressult);
                }
            }
            knowledgeBase.put(content, ressult);
            notifyObservers();
        }else {
            if (message.receiver() == null || "".equals(message.receiver())) {
                // 订阅的消息，如何处理订阅的消息？

            }
        }
    }

    public String getId() {
        return stringId(this.agenti);
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void notifyObservers() {
        // 实现
        for (MessageObserver moi : observers) {
            moi.onNewMessage(new Message(id, "", ressult));
        }
    }

    public void notify(String msg) {// 实现
        for (MessageObserver moi : observers) {
            moi.onNewMessage(new Message(id, "", msg));
        }
    }
}

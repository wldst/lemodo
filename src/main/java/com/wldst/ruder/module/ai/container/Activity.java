package com.wldst.ruder.module.ai.container;

import com.alibaba.fastjson2.JSON;
import com.wldst.ruder.module.ai.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity extends MessageCenter implements MessageObserver, MsgSubject {
    private String id;
    private String name;
    private String conent;
    private String msg;
    private Map<String, Object> knowledgeBase; // 活动的知识库
    private List<Agent> agents;
    private List<ActivityContainer> containers;
    private AiService aiService;

    public Boolean sendMessage(String targetId, String task) {
        Boolean sended = false;
        if (agents != null && !agents.isEmpty()) {
            for (Agent agent : agents) {
                if (agent.getId().equals(targetId)) {
                    sended = true;
                    agent.receiveTask(task);
                    String result = agent.getResult();
                    // 处理智能体返回的结果
                    knowledgeBase.put(task + agent.getId(), result);
                    break;
                }
            }
        }

        if (!sended) {
            if (containers != null && !containers.isEmpty()) {
                sendMessage2(targetId, task);
            }
        }
        return sended;
    }

    /**
     * 将消息广播到容器中
     *
     * @param agentId
     * @param task
     */
    public void sendMessage2(String agentId, String task) {
        boolean sended = false;
        for (ActivityContainer ci : containers) {
            sended = ci.sendMsg(agentId, task);
            if (sended) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        Process pf = new Process("meeting管理进程", "meeting management process");

        Activity ai = new Activity("AI", "An artificial intelligence system");
        pf.registerObserver(ai);

        // 日程安排，检查是否冲突，给出合理安排，安排日程.

        ai.knowledgeBase.put("schedule", "检查是否冲突，给出合理安排，安排日程");
        // 造一个活动,活动名称，活动定义。提取智能体。
        // 没有智能体，造一个对应的智能体。

        // 设计活动，确定活动时间，确定活动地点，确定活动人员，确定活动工具，确定活动流程，
        // 如何根据用户输入，确定启动或者进入什么活动空间。
        // 设计会议室预定这个活动，会使用到哪些元数据，没有元数据，定义一个元数据。
        ai.knowledgeBase.put("activity", "预定会议室");
        Map<String, Object> aiKnowledgeBase = new HashMap<>();
        aiKnowledgeBase.put("meeting", "预定会议室");
        // 创建几个智能体,"办公室""研发中心"
        aiKnowledgeBase.put("prompt","test1");
        Agent emp1 = new Agent(aiKnowledgeBase);
        aiKnowledgeBase.put("prompt","test2");
        Agent emp2 = new Agent(aiKnowledgeBase);

        ai.registerObserver(emp2);
        ai.registerObserver(emp1);

        emp1.registerObserver(emp2);
        emp2.registerObserver(emp1);

        aiKnowledgeBase.put("prompt","会议室管理员");
        Agent emp3 = new Agent(aiKnowledgeBase);

        // 会议室信息，读取会议室数据。
        // 给我安排一个五楼的会议室
        // 如何分配任务：xxx：xxx

        // 智能体之间的通信
        emp1.sendMessage(emp2.getId(), "Hello from Agent1");
        emp2.sendMessage(emp1.getId(), "Hello from Agent2");

        // 智能体根据接收到的信息做出决策
        String decision1 = emp1.makeDecision();
        String decision2 = emp2.makeDecision();

        // 执行决策
        emp1.executeDecision(decision1);
        emp2.executeDecision(decision2);
        System.out.println("Activity Knowledge Base: " + ai.knowledgeBase);
    }

    public Map<String, Object> getKnowledgeBase() {
        return knowledgeBase;
    }

    public void setKnowledgeBase(Map<String, Object> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
        for (Agent ai : agents) {
            registerObserver(ai);
        }
    }

    public List<ActivityContainer> getContainers() {
        return containers;
    }

    public void setContainers(List<ActivityContainer> containers) {
        this.containers = containers;
    }

    public Activity(String name, String conent) {
        super();
        this.name = name;
        this.conent = conent;
        knowledgeBase = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConent() {
        return conent;
    }

    public void setConent(String conent) {
        this.conent = conent;
    }

    @Override
    public void onNewMessage(Message message) {
        // 活动中心监听到消息，转发给智能体，智能体根据消息做决策
        sendMessage(message);
        msg = message.content();
        notifyObservers();
    }

    @Override
    public void notifyObservers() {// 实现
        for (MessageObserver moi : observers) {
            moi.onNewMessage(new Message(name, "", msg));
        }
    }

    public void notify(String msg) {// 实现
        for (MessageObserver moi : observers) {
            moi.onNewMessage(new Message(name, "", msg));
        }
    }
    public void updateKnowledgeBase(Map<String, Object> knowledgeBase) {
        // 活动中心根据消息，找到对应的智能体，将消息发送给智能体
        this.knowledgeBase.putAll(knowledgeBase);
    }
    public void updateKnowledgeBase(String key, String value) {
        // 活动中心根据消息，找到对应的智能体，将消息发送给智能体
        this.knowledgeBase.put(key,value);
    }

    public String makeDecision() {
        // 这里可以集成大模型的API来生成决策
        String task = "Generate a decision for " + JSON.toJSONString(knowledgeBase);
        String modelResponse = aiService.callAIByMsg(task);
        // 例如，使用深度学习模型来分析知识库并做出决策
        // 简化示例，随机生成一个决策
        return "Decision is: " + modelResponse;
    }

    public void setAiService(AiService aiService){
        this.aiService=aiService;
    }
}

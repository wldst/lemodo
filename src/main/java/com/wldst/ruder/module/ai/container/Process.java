package com.wldst.ruder.module.ai.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 进程
 */
public class Process extends MessageCenter implements MessageObserver,MsgSubject{
    private Long id;
    private String name;
    private String conent;
    private String tempMsg;
    private List<Activity> activitys;
    private Map<String, Object> knowledgeBase; // 活动的知识库
    public Process(String name, String conent) {
        super();
        this.name = name;
        this.conent = conent;
        this.knowledgeBase = new HashMap<>();
    }

    public Boolean sendMsg(String agentId,String task) {
//        messageCenter.sendMessage(new Message(agentId,task));
        boolean flag = false;
        for (Activity acti : activitys) {
            flag =  acti.sendMessage(agentId,task);
            if(flag){
                break;
            }
        }
        return flag;
    }


    @Override
    public void onNewMessage(Message message) {
        //接收并处理消息
        //暂停，重启，挂起，销毁
        if("暂停，重启，挂起，销毁,startup,shutdown,pause,kill".contains(message.content())){
            tempMsg=message.content();
            notifyObservers();
        }

    }

    @Override
    public void notifyObservers() {
        for(MessageObserver moi:observers){
            //流程启动，暂停，结束，恢复
            moi.onNewMessage(new Message(name,"",tempMsg));
        }
    }

    public List<Activity> getActivitys() {
        return activitys;
    }

    public void setActivitys(List<Activity> activitys) {
        for (Activity acti : activitys){
            registerObserver(acti);
        }
        this.activitys = activitys;
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

    public Long getId() {
		return id;
	}
    public void setId(Long id) {
		this.id = id;
	}

    public void updateKnowledgeBase(String key, String value) {
        // 活动中心根据消息，找到对应的智能体，将消息发送给智能体
        this.knowledgeBase.put(key,value);
    }

    public void updateKnowledgeBase(Map<String, Object> knowledgeBase) {
        // 活动中心根据消息，找到对应的智能体，将消息发送给智能体
        this.knowledgeBase.putAll(knowledgeBase);
    }
    public Map<String, Object> getKnowledgeBase() {
        return knowledgeBase;
    }

    public void setKnowledgeBase(Map<String, Object> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

}

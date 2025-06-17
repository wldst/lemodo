package com.wldst.ruder.module.ai.container;

import java.util.List;

public class ProcessFlow extends MessageCenter implements MessageObserver,MsgSubject{
    private Long id;
    private String name;
    private String conent;
    private String tempMsg;
    private List<Activity> activitys;
    public ProcessFlow(String name,String conent) {
        super();
        this.name = name;
        this.conent = conent;
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

}

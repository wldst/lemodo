package com.wldst.ruder.module.ai.container;

import java.util.List;

public class ActivityContainer {
    MessageCenter messageCenter = new MessageCenter();
    private List<Activity> activitys;

    public ActivityContainer(List<Activity> activitys) {
        this.activitys = activitys;
        for (Activity acti : this.activitys){
            messageCenter.registerObserver(acti);
        }
    }

    public Boolean sendMsg(String activityId,String task) {
//        messageCenter.sendMessage(new Message(activityId,task));
        boolean flag = false;
        for (Activity acti : activitys) {
            flag =  acti.sendMessage(activityId,task);
            if(flag){
                break;
            }
        }
        return flag;
    }


}

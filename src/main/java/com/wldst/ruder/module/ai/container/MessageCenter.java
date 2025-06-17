package com.wldst.ruder.module.ai.container;

import com.wldst.ruder.util.MapTool;

import java.util.ArrayList;
import java.util.List;

public class MessageCenter extends MapTool {
    protected List<MessageObserver> observers = new ArrayList<>();

    public void registerObserver(MessageObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(MessageObserver observer) {
        observers.remove(observer);
    }

    public void sendMessage(Message message) {
        for (MessageObserver observer : observers) {
            observer.onNewMessage(message);
        }
    }
}

package com.wldst.ruder.module.ai.container;

// 定义主题接口，即被观察者
public interface MsgSubject {
    void registerObserver(MessageObserver observer);
    void removeObserver(MessageObserver observer);
    void notifyObservers();
}

package com.wldst.ruder.module.state.sys;

public enum SystemStates {
    
    
    STARTING(1), // 启动中
    UNINIT(2),                 // 未初始化
    INITED(3), // 已初始化
    STARTED(4), // 已启动
    DONE(5),                    // 结束
    LOCAL(6),	//本地
    SHARE(7); //共享
    
    private int value;
    private SystemStates(int v) {
	value=v;
    }
    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        this.value = value;
    }
}

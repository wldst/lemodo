package com.wldst.ruder.module.command.impl;

import java.util.Map;

import com.wldst.ruder.module.command.Command;

public class StopCommand<T extends MapOperator<Map<String,Object>>> implements Command {

    private T domain;
    private Map<String,Object>  domainData;
    
    public StopCommand(T domainOperate,Map<String,Object> data){
        domain = domainOperate;
        domainData = data;
    }
    /**
     * 执行方法
     */
    @Override
    public void execute() {
        domain.play(domainData);
    }

}

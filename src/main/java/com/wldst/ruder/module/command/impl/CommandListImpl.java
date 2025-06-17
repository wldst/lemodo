package com.wldst.ruder.module.command.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.domain.CommandDomain;
import com.wldst.ruder.module.command.CRUDCommand;
import com.wldst.ruder.module.command.CommandList;

public class CommandListImpl extends CommandDomain implements CommandList {
    private static Logger logger = LoggerFactory.getLogger(CommandListImpl.class);
    private Map<String,Object> data;
    private Map<String,Object> retData;
    
    private List<CRUDCommand> commandList = new ArrayList<>();
    /**
     * 宏命令聚集管理方法
     */
    @Override
    public void add(CRUDCommand cmd) {
        commandList.add(cmd);
    }
    /**
     * 宏命令聚集管理方法
     */
    @Override
    public void remove(CRUDCommand cmd) {
        commandList.remove(cmd);
    }
    
    @Override
    public Map<String,Object> execute(Map<String,Object> newDataMap) {
//	LoggerTool.info(logger,MapTool.mapString(data));
	if(commandList.isEmpty()) {
	  return newDataMap;  
	}
	retData = new HashMap<>(commandList.size());
//	Object object = this.getRetData().get(getLabel());
//	if(object!=null) {
//	    paramMap.put("prop",object);
//	}
	this.data=newDataMap;
	
        for(CRUDCommand cmd : commandList){
            Map<String, Object> execute = cmd.execute(this.data);
	    retData.put(cmd.getLabel(),execute);
        }
        return this.data;
    }
    
    
    
    @Override
    public boolean isEmpty() {
	return commandList.isEmpty();
    }
    @Override
    public Map<String, Object> getRetData() {
	return retData;
    }
    

}

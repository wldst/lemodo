package com.wldst.ruder.module.command;

import java.util.Map;

public interface CRUDCommand {
    /**
     * 执行方法
     */
    Map<String,Object> execute(Map<String,Object> data);
    
    default Map<String,Object> executeChild(Map<String,Object> data,Map<String,Object> parentData){
	return data;
    }
    default String getLabel() {
	return null;
    }
}

package com.wldst.ruder.module.bs;

import java.util.Map;

public interface Shell {
    /**
     * 执行方法
     */
    Map<String, Object> execute();
    Map<String, Object> execute(Map<String,Object> newDataMap);
    String getId();
    default String getLabel() {
	return null;
    }
}

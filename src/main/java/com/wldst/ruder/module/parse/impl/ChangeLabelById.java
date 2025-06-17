package com.wldst.ruder.module.parse.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 * 修改Label
 * 
 * @param msg
 * @return
 */
@Component
public class ChangeLabelById extends ParseExcuteDomain implements MsgProcess {
    
    final static Logger logger = LoggerFactory.getLogger(ChangeLabelById.class);
    
    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "修改Label")
    public String process(String msg, Map<String, Object> context) {
	String prefix2 = "修改Label:,更新Label:,替换Label:";
	for(String pi:prefix2.split(",")) {
	    if(msg.startsWith(pi)) {
		    msg= msg.replace(pi, "");
		    String[] idLabel = msg.split(",");
		    neo4jUService.changeLabelById(Long.valueOf(idLabel[0]),idLabel[1]);
		    bool(context, USED);
		}
	}
	
	// 并行概率执行
	 
	if (context.get(USED).equals(true)) {
	    String answerQ = "\n <BR> OK";
	    
	    return answerQ;
	}

	return null;
    }
}

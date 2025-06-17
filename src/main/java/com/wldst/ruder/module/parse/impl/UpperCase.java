package com.wldst.ruder.module.parse.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 *  
 * 字符串大写小写转换
 * @param msg
 * @return
 */
@Component
public class UpperCase extends ParseExcuteDomain implements MsgProcess {
    
    final static Logger logger = LoggerFactory.getLogger(UpperCase.class);
    
    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "UpperCase")
    public Object process(String msg, Map<String, Object> context) {
	msg = msg.trim().replaceAll("：", ":");
	// 并行概率执行
	for(String ui:upperWords) {
	    if (!bool(context, USED)&&msg.startsWith(ui)) {
		    msg=msg.replaceFirst(ui, "");
		    context.put(USED, true);
			 return msg.toUpperCase();
		    }
	}
	for(String ui:lowerWords) {
	    if (!bool(context, USED)&&msg.startsWith(ui)) {
		    msg=msg.replaceFirst(ui, "");
		    context.put(USED, true);
			 return msg.toLowerCase();
		    }
	}
	return null;
    }
    
      
}

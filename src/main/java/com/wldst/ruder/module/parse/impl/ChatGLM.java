package com.wldst.ruder.module.parse.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.openai.GLMChatQuestion;

/**
 * 
 * 
 * @param msg
 * @return
 */
@Component
public class ChatGLM extends ParseExcuteDomain implements MsgProcess {

    final static Logger logger = LoggerFactory.getLogger(ChatGLM.class);

    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "ChatGLM.process")
    public Object process(String msg, Map<String, Object> context) {
	msg = msg.trim().replaceAll("：", ":");
	// 并行概率执行
	String chatPrefix = "GLM:";
	if (!bool(context, USED) && msg.startsWith(chatPrefix)) {
	    msg = msg.replaceFirst(chatPrefix, "");
	    GLMChatQuestion.chat(msg, string(context, "MyId"));
	    context.put(USED, true);
	}
	if (context.get(USED).equals(true)) {
	    return "<BR> 回答...<BR>";
	}

	return null;
    }
}

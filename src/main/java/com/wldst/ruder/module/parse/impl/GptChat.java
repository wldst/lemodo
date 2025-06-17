package com.wldst.ruder.module.parse.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.openai.ChatQuestion;
/**
 *  
 * 
 * @param msg
 * @return
 */
@Component
public class GptChat extends ParseExcuteDomain implements MsgProcess {
    
    final static Logger logger = LoggerFactory.getLogger(GptChat.class);
    
    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = " GptChat.process")
    public Object process(String msg, Map<String, Object> context) {
	msg = msg.trim().replaceAll("：", ":");
	// 并行概率执行
	
	String chatPrefix = "Chat:";
	if (!bool(context, USED)&&msg.startsWith(chatPrefix)) {
	    msg=msg.replaceFirst(chatPrefix, "");
		ChatQuestion chat = ChatQuestion.chat(msg,string(context,"MyId"));
		context.put("qa",chat);
		String answer = null;
		int count=0;
		while(!chat.isAnswered()&&count<150) {
		    if(chat.getAnswer()!=null&&!"".equals(chat.getAnswer())) {
			answer=chat.getAnswer();
		    }
		    try {
			Thread.sleep(100);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		    count++;
		}
		context.put(USED, true);
		return answer;
	    }
//	if (context.get(USED).equals(true)) {
//	    return "<BR> 回答:<BR>";
//	}

	return answer(context);
    }
    
      
}

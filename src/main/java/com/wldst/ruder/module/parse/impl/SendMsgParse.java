package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.springframework.stereotype.Component;

import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.module.ws.web.MessageServer;
import com.wldst.ruder.util.DateUtil;

/**
 *  @xxx 你好
 * 
 * @param msg
 * @param context
 */
@Component
public class SendMsgParse extends ParseExcuteDomain implements MsgProcess {

    /**
     * 解析元数据执行
     * 
     * @param msg
     * @param context
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {
	if (!msg.contains("@")&&!msg.contains("发送")&&!msg.contains("给") ) {
	    return null;
	}
	if (!bool(context, USED)) {
	    if(msg.startsWith("@")&&(msg.contains(":")||msg.contains("："))) {
		context.put(USED, true);
		String[] split = msg.substring(1).split(":");
		if(split.length<2&& msg.contains("：")) {
		    split =msg.substring(1).split("：");
		}
		
		Map<String, Object> toUser = adminService.loadUserByUsername(split[0].trim());
		if(toUser!=null&&!toUser.isEmpty()) {
		    String name2 = name(toUser);
		    String content=split[1];
		    Map<String,Object> m= newMap();
		    m.put("toId", id(toUser));
		    m.put(NAME,DateUtil.nowDateTime()+name2+"发送1条消息");
		    Long fromUserId = adminService.getCurrentUserId();
		    String currentName = adminService.getCurrentName();
		   
		    Boolean sendInfo = MessageServer.sendInfo(currentName+"说:"+content, string(toUser,"username"));
        		if(!sendInfo) {
        		    m.put("fromId", fromUserId);
        		    m.put(CONTENT, content);
        		    m.put("readed", "false");
        		    m.put("sentTime", DateUtil.getNow());
        		    Node saveByBody = neo4jUService.saveByBody(m, "message");
        		    relationService.addRel("unread", saveByBody.getId(), id(toUser));
        		    relationService.addRel("sent", saveByBody.getId(), fromUserId);
        		}
		    
		}else {
		    return "没有找到用户"+split[0];
		}
	    }
	}
	return null;
    }

    public String trimList(String columns) {
	String[] columnsx = columns.split(","); 
	List<String> columnsSet = new ArrayList<>();
	 for(String key:columnsx) {
		 columnsSet.add(key.trim());
	 }
	return  String.join(",", columnsSet);
    }

}

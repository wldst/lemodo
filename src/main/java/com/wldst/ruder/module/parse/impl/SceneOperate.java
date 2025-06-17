package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.DateTool;
/**
 * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
 * @author wldst
 *
 */
@Component
public class SceneOperate extends ParseExcuteDomain implements MsgProcess {
    protected static List<String> sceneWord = Arrays.asList("cj", "场景", "进入场景", "scene");
    
    public Map<String, Object> getOrSelectScene(String msg, Map<String, Object> context) {
	List<Map<String, Object>> sceneByName = getSceneByName(msg);
	context.put("currentName", msg);
	Map<String, Object> mi = userSelect(context, sceneByName);
	return mi;
    }
    
    @ServiceLog(description = "根据参数获取元数据信息")
    public List<Map<String, Object>> getSceneByName(String metaName) {
	List<Map<String, Object>> ownerScenes;

	String getMetaInfo = " MATCH (m:Scene) where  m.name contains '" + metaName + "'  return distinct m";
	ownerScenes = neo4jUService.cypher(getMetaInfo);
	if (ownerScenes != null && !ownerScenes.isEmpty()) {
	    return ownerScenes;
	}
	return ownerScenes;
    }
    /**
     * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
     * 
     * @param msg
     */
    @Override
    public List<Map<String, Object>> process(String msg, Map<String, Object> context) {
	List<Map<String, Object>> data = new ArrayList<>();
	for (String prefix : sceneWord) {
	    if (!bool(context, USED) && msg.startsWith(prefix)) {// 根据角色权限，账号权限，来确定打开范围
		msg = msg.replaceFirst(prefix, "");
		Map<String, Object> orSelectScene = getOrSelectScene(msg, context);
		if (orSelectScene != null && !orSelectScene.isEmpty()) {
		    processOpenData(code(orSelectScene), orSelectScene);
		    data.add(orSelectScene);
		    context.put(USED, true);
		}
	    }
	}
	return data;
    }
     

    public void processOpenData(String labelOf, Map<String, Object> data2) {
	if (data2 != null) {
	    String url2 = url(data2);
	    if (url2 == null) {
		// 判断权限，只读和修改权限
		data2.put("url", LemodoApplication.MODULE_NAME + "/scene/" + labelOf);

	    } else {
		if (url2.contains("?xxx=")) {
		    String[] split = url2.split("xxx=");
		    String currentJSessionId = adminService.getCurrentJSessionId();
		    if (currentJSessionId == null) {
			currentJSessionId = adminService.getRequestSessionId();
		    }
		    data2.put("url", split[0] + "xxx=" + currentJSessionId);

		    String userName = adminService.getCurrentUserName();
		    Map<String, Object> attMapBy = neo4jService.getAttMapBy("sessionId", currentJSessionId, "Session");
		    if (attMapBy == null) {
			Map<String, Object> session = newMap();
			Long currentPasswordId = adminService.getCurrentPasswordId();
			if (currentPasswordId.intValue() > 0) {
			    session.put("userName", userName);
			    // 保存会话信息,保存之前，删除所有过期数据。
			    // neo4jService.delete("userName", userName, "Session");
			    session.put("sessionId", currentJSessionId);
			    session.put("createTime", DateTool.nowLong());
			    session.put("accountId", currentPasswordId);
			    neo4jService.save(session, "Session");
			}

		    }
		}
	    }

	}
    }

}

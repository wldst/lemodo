package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.crud.service.HtmlShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.DateTool;
/**
 * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
 * @author wldst
 *
 */
@Component
public class StartWithOpen extends ParseExcuteDomain implements MsgProcess {

	@Autowired
	private HtmlShowService showService;
    
    /**
     * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
     * 
     * @param msg
     */
    @Override
    public List<Map<String, Object>> process(String msg, Map<String, Object> context) {
	List<Map<String, Object>> data = new ArrayList<>();

	for (String prefix : openWord) {
	    if (!bool(context, USED)&&msg.startsWith(prefix)) {// 根据角色权限，账号权限，来确定打开范围
		context.put(USED, true);
		msg = msg.replaceFirst(prefix, "");
		if (containLabelInfo(msg)) {
		    Map<String, Object> dataOfKuohao = getDataOfKuohao(msg);
		    String meta = string(dataOfKuohao, "meta");
		    String[] split = strArray(dataOfKuohao, "split");
		    List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
		    for (Map<String, Object> mi : metaDataByName) {
			List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), split[0]);
			if (dataBy != null) {
			    for (Map<String, Object> di : dataBy) {
				di.put("name", name(di) + "（" + name(mi) + "）");
				processOpenData(label(mi), data, di);
			    }
			}
		    }
		} else {
		    handleOpen(msg, "resource", data, context);
		    if (data.isEmpty()) {
			handleOpen(msg, "App", data, context);
		    }
		    if (data.isEmpty()) {
			Map<String, Object> mi = getOrSelectMetaData(msg, context);
			List<Map<String, Object>> dataBy = neo4jUService.getDataBy(label(mi), msg);
			if (dataBy != null) {
			    for (Map<String, Object> di : dataBy) {
				di.put("name", name(di) + "（" + name(mi) + "）");
				processOpenData(label(mi), data, di);
			    }
			}
		    }
		}
	    }
	}
	return data;
    }
    
    
    public void handleOpen(String msg, String labelOf, List<Map<String, Object>> data, Map<String, Object> context) {
	boolean useAnd = false;
	for (String qie : andRel) {
	    if (msg.contains(qie)) {
		String[] resourceAuth = msg.split(qie);
		for (String ri : resourceAuth) {
		    Map<String, Object> mdData = getData(ri, labelOf, context);
		    processOpenData(labelOf, data, mdData);
		}
		useAnd = true;
	    }
	}
	if (!useAnd) {
	    Map<String, Object> data2 = getData(msg, labelOf, context);
	    processOpenData(labelOf, data, data2);
	}
    }

    public void processOpenData(String labelOf, List<Map<String, Object>> data, Map<String, Object> data2) {
	if (data2 != null) {
	    String url2 = url(data2);
	    if (url2 == null) {
		// 判断权限，只读和修改权限

		data2.put("url",
			LemodoApplication.MODULE_NAME + "/layui/" + label(data2) + "/" + id(data2) + "/documentRel");

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
	    if (META_DATA.equals(labelOf)) {
		data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(data2));
	    }
		showService.validUrlPrefix(data2);

	    data.add(data2);
	}
    }

}

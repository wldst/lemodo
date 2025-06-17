package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
 * @author wldst
 *
 */
@Component
public class StartWithManage extends ParseExcuteDomain implements MsgProcess {
    
    protected static List<String> manageNode = Arrays.asList("管理", "manage", "操作", "处理", "列表");
    
    
    /**
     * 管理
     * 
     * @param msg
     */
    @Override
    public List<Map<String, Object>> process(String msg, Map<String, Object> context) {
	List<Map<String, Object>> data = new ArrayList<>();
	for(String mi: manageNode) {
	    if (!bool(context, USED)&&msg.startsWith(mi)) {// 根据角色权限，账号权限，来确定打开范围
		context.put(USED, true);
		    handleManage(msg, META_DATA, data, mi, context);
		}
	}
	
	return data;
    }
    
    public void handleManage(String msg, String labelOf, List<Map<String, Object>> data, String prefix,
	    Map<String, Object> context) {
	String obj = msg.replaceFirst(prefix, "");
	boolean useAnd = false;
	for (String qie : andRel) {
	    if (obj.contains(qie)) {
		String[] resourceAuth = msg.split(qie);
		for (String ri : resourceAuth) {
		    Map<String, Object> mdData = getData(ri, labelOf, context);

		    if (mdData != null) {
			if (META_DATA.equals(labelOf)) {
			    mdData.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(mdData));
			}
			data.add(mdData);
		    }
		}
		useAnd = true;
	    }
	}
	if (!useAnd) {
	    Map<String, Object> data2 = getData(obj, labelOf, context);
	    if (data2 != null) {
		if (META_DATA.equals(labelOf)) {
		    data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(data2));
		}
		data.add(data2);
	    }
	}
    }

}

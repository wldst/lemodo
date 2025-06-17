package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
 * @author wldst
 *
 */
@Component
public class DbTable extends ParseExcuteDomain implements MsgProcess {
    protected static List<String> tableWord = Arrays.asList("表转元数据", "table2Meta","tableMappingMeta","tmm");
    @Autowired
    private DbInfoService dbService;
    /**
     * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
     * 
     * @param msg
     */
    @Override
    public List<Map<String, Object>> process(String msg, Map<String, Object> context) {
	List<Map<String, Object>> data = new ArrayList<>();
	for (String prefix : tableWord) {
	    if (!bool(context, USED) && msg.startsWith(prefix)) {// 
		msg = msg.replaceFirst(prefix, "").trim();
		String[] tables = msg.split(",");
		for (String ti : tables) {
		    try {
			dbService.tableMappingMeta(ti);
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		    List<Map<String, Object>> metaDataBy = neo4jUService.getMetaDataBy(ti);
		    for(Map<String, Object> mi: metaDataBy) {
			 mi.put("url", LemodoApplication.MODULE_NAME + "/md/" + META_DATA);
			 data.add(mi);
		    }
		}
		context.put(USED, true);
	    }
	}
	return data;
    }
     

    

}

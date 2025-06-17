package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.VtPool;
/**
 * 查询谁的属性是什么，关系有哪些
 * 
 * @param msg
 * @return
 */
@Component
public class QueryRelBetweenAandB extends ParseExcuteDomain implements MsgProcess {
    
    final static Logger logger = LoggerFactory.getLogger(QueryRelBetweenAandB.class);
    
    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "查询谁的属性是什么，关系有哪些，并返回答案，在Context中有是否执行")
    public String process(String msg, Map<String, Object> context) {
	if (msg.endsWith("\\?") || msg.endsWith("？")) {
	   return queryAandB(msg, context);
	} else {
	    if (msg.contains(" ")) {
		return queryAandB(msg, context);
	    }
	}

	return null;
    }
    
    /**
     * 查询节点之间的关系
     * 
     * @param msg
     * @param context
     * @return
     */
    @ServiceLog(description = "查询两点之间的关系")
    public String queryAandB(String msg, Map<String, Object> context) {
	StringBuilder sb = new StringBuilder();
	msg = msg.trim().replaceAll("\\?", "").replaceAll("？", "");
	List<String> query = new ArrayList<>();
	query.add("有什么关系");
	query.add("有什么");
	query.add("是什么关系");
	query.add("是否可达");
	for (String si : query) {
	    if (!bool(context, USED)&&msg.endsWith(si)) {// 根据角色权限，账号权限，来确定打开范围
		context.put(USED, true);
		String string = msg.split(si)[0];
		String handleABPath = handleABPath(string, context);
		if (handleABPath != null) {
		    sb.append(handleABPath);
		}
	    }
	}
	if (sb.length() < 1 && msg.contains(" ")) {
	    String[] split = msg.split(" ");
	    if (split.length == 2) {
		context.put(USED, true);
		List<Map<String, Object>> datas = getDatas(split[0], context);
		for(Map<String, Object> di: datas) {
		    Callable<String> callabel = () -> {
			    StringBuilder sbx = new StringBuilder();
			    Long idStart = id(di);
				Long idEnd = getIdOfData(split[1], context);
				if (idStart != null && idEnd != null) {
				    String showPathInfo = adminService.showPathInfo(idStart, idEnd);
				    if (showPathInfo != null) {
					sb.append(showPathInfo);
				    } else {
					relationEndData(sbx, split, idStart);
				    }
				} else {
				    relationEndData(sbx, split, idStart);
				}
			    
			    return sbx.toString();
			    };
			String vt = VtPool.vt(callabel);
			if(vt!=null&&!"".equals(vt.trim())) {
			    sb.append(vt);
			}
		}
	    }
	}
	return sb.toString();
    }
    
    public void relationEndData(StringBuilder sb, String[] split, Long idStart) {
	List<Map<String, Object>> dd = neo4jUService.getDataBy("RelationDefine",split[1]);
	for(Map<String, Object> relMap :dd) {
	    if (relMap != null) {
		Callable<String> callabel = () -> {
		    StringBuilder sbx = new StringBuilder();
			    
		    String relCode = string(relMap, "reLabel");
		    String startLabel = string(relMap, "startLabel");
		    String endLabel = string(relMap, "endLabel");
		    String endsQuery = "MATCH (s:" + startLabel + ")-[r:" + relCode + "]->(e:" + endLabel + ") where id(s)="
			    + idStart + " return e";
		    List<Map<String, Object>> ends = neo4jService.cypher(endsQuery);
		    for (Map<String, Object> ei : ends) {
			if (sbx.length() > 1) {
			    sbx.append("、");
			}
			sbx.append(neo4jUService.seeNode(ei));
		    }
		    return sbx.toString();
		    };
		String vt = VtPool.vt(callabel);
		if(vt!=null&&!"".equals(vt.trim())) {
		    sb.append(vt);
		}
//		    String relCode = string(relMap, "reLabel");
//		    String startLabel = string(relMap, "startLabel");
//		    String endLabel = string(relMap, "endLabel");
//		    String endsQuery = "MATCH (s:" + startLabel + ")-[r:" + relCode + "]->(e:" + endLabel + ") where id(s)="
//			    + idStart + " return e";
//		    List<Map<String, Object>> ends = neo4jService.cypher(endsQuery);
//		    for (Map<String, Object> ei : ends) {
//			if (sb.length() > 1) {
//			    sb.append("、");
//			}
//			sb.append(neo4jUService.seeNode(ei));
//		    }
	    }   
	}
	
	
	 
	
	
    }
    
    public String handleABPath(String msg, Map<String, Object> context) {
	Long idOfStart = null;
	Long idOfEnd = null;

	boolean useAnd = false;
	for (String qie : andRel) {
	    if (msg.contains(qie)) {
		String[] resourceAuth = msg.split(qie);
		for (String ri : resourceAuth) {
		    if (containLabelInfo(ri)) {
			Map<String, Object> onlyContext = new HashMap<>();
			ri = onlyName(onlyContext, ri);
			String dataLabel = string(onlyContext, "dataLabel");
			Map<String, Object> data2 = getData(ri, dataLabel, context);
			Long idOfRoleOrUser2 = id(data2);
			if (idOfStart != null) {
			    idOfEnd = idOfRoleOrUser2;
			} else {
			    idOfStart = idOfRoleOrUser2;
			}
			if (META_DATA.equals(dataLabel)) {
			    data2.put("url", LemodoApplication.MODULE_NAME + "/md/" + dataLabel);
			} else {
			    // document
			    data2.put("url", LemodoApplication.MODULE_NAME + "/layui/" + dataLabel + "/" + id(data2)
				    + "/documentRel");
			}
		    } else {
			Map<String, Object> mdData = getData(ri, META_DATA, context);

			if (mdData != null) {
			    idOfEnd = id(mdData);
			    if (META_DATA.equals(label(mdData))) {
				mdData.put("url", LemodoApplication.MODULE_NAME + "/md/" + label(mdData));
			    } else {
				// document
				mdData.put("url", LemodoApplication.MODULE_NAME + "/layui/" + label(mdData) + "/"
					+ id(mdData) + "/documentRel");
			    }
			} else {
			    Long idOfRoleOrUser2 = getIdOfRoleOrUser(ri);
			    if (idOfStart != null) {
				idOfEnd = idOfRoleOrUser2;
			    } else {
				idOfStart = idOfRoleOrUser2;
			    }
			}
		    }

		}
		useAnd = true;
	    }
	}
	if (useAnd) {
	    return adminService.showPathInfo(idOfStart, idOfEnd);
	}
	return null;
    }

}

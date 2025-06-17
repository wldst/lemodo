package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
 * @author wldst
 *
 */
@Component
public class StartWithGei extends ParseExcuteDomain implements MsgProcess {
    
    
    
    /**
     * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
     * 
     * @param msg
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {
	
		if (!bool(context, USED)&&msg.startsWith("给")) {
		    context.put(USED, true);
		    // 获取默认数据：
		    boolean use = false;
		    for (String addi : authAdd) {
			if (msg.contains(addi)) {
			    use = true;
			    String[] addJuzi = msg.split(addi);
			    // 租户数据授权？该如何授予权限？
			    String userRole = addJuzi[0].replaceFirst("给", "");
			    boolean useAnd = false;
			    List<Long> startIds = new ArrayList<>();
			    for (String qie : andRel) {
				if (userRole.contains(qie)) {
				    String[] resourceAuth = addJuzi[1].split(qie);
				    for (String ri : resourceAuth) {
					if (containLabelInfo(ri)) {
					    Map<String, Object> onlyContext = new HashMap<>();
					    ri = onlyName(onlyContext, ri);
					    String dataLabel = string(onlyContext, "dataLabel");
					    Map<String, Object> data2 = getData(ri, dataLabel, context);
					    startIds.add(id(data2));
					} else {
					    Long idOfRoleOrUser = replacePronoun(context, userRole);
					    startIds.add(idOfRoleOrUser);
					}

				    }
				    useAnd = true;
				}
			    }
			    if (!useAnd) {
				if (containLabelInfo(userRole)) {
				    Map<String, Object> onlyContext = new HashMap<>();
				    userRole = onlyName(onlyContext, userRole);
				    String dataLabel = string(onlyContext, "dataLabel");
				    Map<String, Object> data2 = getData(userRole, dataLabel, context);
				    startIds.add(id(data2));
				} else {
				    Long startId = replacePronoun(context, userRole);
				    startAddAuth(addJuzi[1], startId);
				}

			    } else {
				for (Long si : startIds) {
				    startAddAuth(addJuzi[1], si);
				}
			    }
			}
		    }
		    if (use) {
			return null;
		    }

		    for (String deli : deleteWords) {
			if (msg.contains(deli)) {
			    String[] dels = msg.split(deli);
			    String userRole = dels[0].replaceFirst("给", "");

			    boolean useAnd = false;
			    List<Long> startIds = new ArrayList<>();
			    for (String qie : andRel) {
				if (userRole.contains(qie)) {
				    String[] resourceAuth = dels[1].split(qie);
				    for (String ri : resourceAuth) {
					if (containLabelInfo(ri)) {
					    Map<String, Object> onlyContext = new HashMap<>();
					    ri = onlyName(onlyContext, ri);
					    String dataLabel = string(onlyContext, "dataLabel");
					    Map<String, Object> data2 = getData(ri, dataLabel, context);
					    startIds.add(id(data2));
					} else {
					    Long idOfRoleOrUser = replacePronoun(context, ri);
					    startIds.add(idOfRoleOrUser);
					}
				    }
				    useAnd = true;
				}
			    }
			    Long startId = null;
			    Long endId = null;
			    String relCode = null;
			    if (!useAnd) {
				if (containLabelInfo(userRole)) {
				    Map<String, Object> onlyContext = new HashMap<>();
				    userRole = onlyName(onlyContext, userRole);
				    String dataLabel = string(onlyContext, "dataLabel");
				    Map<String, Object> data2 = getData(userRole, dataLabel, context);
				    startDelRel(dels[1], id(data2));
				} else {
				    startId = replacePronoun(context, userRole);
				    startDelRel(dels[1], startId);
				}
			    } else {
				for (Long si : startIds) {
				    startDelRel(dels[1], si);
				}
			    }

			}
		    }
		}
		return null;
    }
    
    @ServiceLog(description = "删除权限关系，开始节点和结束节点，权限关系")
    private void startDelRel(String end, String rel, Long startId, Map<String, Object> context) {
	Long endId = getIdOfMd(end);
	if (endId == null || endId.equals(startId)) {
	    if (containLabelInfo(rel)) {
		Map<String, Object> onlyContext = new HashMap<>();
		rel = onlyName(onlyContext, rel);
		String dataLabel = string(onlyContext, "dataLabel");
		// Map<String, Object> mapObject = mapObject(onlyContext, "dataMd");
		endId = getIdOfData(rel, dataLabel, context);
	    } else {
		endId = getIdOfData(rel, context);
	    }
	}
	deleteAuthRel(startId, endId, rel);
    }
    
    
    private void startDelRel(String delJuzi, Long startId) {
	Long endId;
	for (String owni : ownWords) {
	    if (delJuzi.contains(owni)) {
		String[] resourceAuth = delJuzi.split(owni);
		String resource = resourceAuth[0];
		endId = getIdOfMd(resource);
		String auth = resourceAuth[1];
		deleteAuthRel(startId, endId, auth);
	    }
	}
    }
    @ServiceLog(description = "删除权限关系，开始节点和结束节点，权限关系")
    public void deleteAuthRel(Long startId, Long endId, String auth) {
	String relCode;
	Map<String, Object> authMap = neo4jUService.getAttMapBy(NAME, auth, "permission");
	relCode = code(authMap);
	String cypher = " MATCH(s)-[r:" + relCode + "{name:\"" + auth + "\"}]->(e)  where id(s)=" + startId
		+ " and id(e)=" + endId + " delete r";
	neo4jService.execute(cypher);
    }
    
    

}

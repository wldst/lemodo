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
public class StartWithJiang extends ParseExcuteDomain implements MsgProcess {
    
    
    
    /**
     * 将{用户或者角色}的什么的什么权限,删除。
     * 
     * @param msg
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {	
	String prefix2 = "将";
	if (!bool(context, USED)&&msg.startsWith(prefix2)) {
	    context.put(USED, true);
	    // 获取默认数据：
	    for (String deli : deleteWords) {
		if (msg.contains(deli)) {
		    String[] dels = msg.split(deli);
		    String objectResourceAuth = dels[0].replaceFirst(prefix2, "");
		    Boolean useOwn = false;
		    // 将xxx删除
		    for (String oi : ownWords) {
			// 资源权限删除
			if (objectResourceAuth.contains(oi)) {
			    useOwn = true;
			    String[] resourceAuth = objectResourceAuth.split(oi);
			    String objectStr = resourceAuth[0];
			    boolean useBetween = false;
			    for (String ci : between) {
				if (objectStr.endsWith(ci)) {
				    objectStr = objectStr.replaceFirst(ci, "");
				    String betweenRel = resourceAuth[1];
				    if (betweenRel.endsWith("关系")) {
					betweenRel = betweenRel.replaceFirst("关系", "");

					List<Long> startIds = new ArrayList<>();
					for (String qie : andRel) {
					    if (objectStr.contains(qie)) {
						String[] objs = objectStr.split(qie);
						for (String obji : objs) {
						    if (containLabelInfo(obji)) {
							Map<String, Object> onlyContext = new HashMap<>();
							obji = onlyName(onlyContext, obji);
							String dataLabel = string(onlyContext, "dataLabel");
							Map<String, Object> data2 = getData(obji, dataLabel, context);
							startIds.add(id(data2));
						    } else {
							startIds.add(getIdOfData(obji, context));
						    }
						}
						if (objs.length == 2 && startIds.size() == 2) {
						    if ("".equals(betweenRel)) {
							deleteRel(startIds.get(0), startIds.get(1));
						    } else {
							deleteRel(startIds.get(0), startIds.get(1));
						    }
						}
					    }
					}

				    } else {

				    }
				    return null;
				}
			    }
			    if (useBetween) {
				return null;
			    }

			    boolean useAnd = false;
			    List<Long> startIds = new ArrayList<>();
			    for (String qie : andRel) {
				if (objectStr.contains(qie)) {
				    String[] objs = objectStr.split(qie);
				    for (String obji : objs) {
					if (containLabelInfo(obji)) {
					    Map<String, Object> onlyContext = new HashMap<>();
					    obji = onlyName(onlyContext, obji);
					    String dataLabel = string(onlyContext, "dataLabel");
					    Map<String, Object> data2 = getData(obji, dataLabel, context);
					    startIds.add(id(data2));
					} else {
					    startIds.add(getIdOfRoleOrUser(obji));
					}
				    }
				    useAnd = true;
				}
			    }
			    Long startId = null;
			    if (!useAnd) {
				if (containLabelInfo(objectStr)) {
				    Map<String, Object> onlyContext = new HashMap<>();
				    objectStr = onlyName(onlyContext, objectStr);
				    String dataLabel = string(onlyContext, "dataLabel");
				    Map<String, Object> data2 = getData(objectStr, dataLabel, context);
				    startId = id(data2);
				    if (resourceAuth.length > 2) {
					startDelRel(resourceAuth[1], resourceAuth[2], startId, context);
				    } else {
					startDelRel(resourceAuth[0], resourceAuth[1], startId, context);
				    }
				} else {
				    startId = getIdOfRoleOrUser(objectStr);
				    if (resourceAuth.length > 2) {
					startDelRel(resourceAuth[1], resourceAuth[2], startId, context);
				    } else {
					startDelRel(resourceAuth[0], resourceAuth[1], startId, context);
				    }
				}

			    } else {
				for (Long si : startIds) {
				    if (resourceAuth.length > 2) {
					startDelRel(resourceAuth[1], resourceAuth[2], si, context);
				    } else {
					startDelRel(resourceAuth[0], resourceAuth[1], si, context);
				    }
				}
			    }

			}
		    }

		    // if(!useOwn) {
		    //
		    // }
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

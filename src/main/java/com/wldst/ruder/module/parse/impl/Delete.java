package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 * 处理以删除为开头的语句,先处理删除数据，再处理删除关系。
 * 
 * @param msg
 * @param context
 */
@Component
public class Delete extends ParseExcuteDomain implements MsgProcess {
    
    
    
    /**
     * 处理以删除为开头的语句,先处理删除数据，再处理删除关系。
     * 
     * @param msg
     * @param context
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {
	
		for (String deli : deleteWords) {
		    boolean startsWith = msg.startsWith(deli);
		    if (!bool(context, USED)&&startsWith) {
			
			String delContent = msg.replaceFirst(deli, "");

			Long idOfData = getIdOfData(delContent, context);
			if (idOfData != null) {// 删除数据
			    neo4jUService.execute("MATCH(n)<-[r]-(m),(n)-[r1]->(k) where id(n)=" + idOfData + " DELETE r,r1");
			    neo4jUService.execute("MATCH(n) where id(n)=" + idOfData + " DETACH DELETE n");
			} else {
			    List<Map<String, Object>> dataByXx = neo4jUService.getDataBy(delContent);
			    if (dataByXx != null && dataByXx.size() > 1) {
				// 多个的情况盖如和处理：和前端进行确认
				return null;
			    }
			    handelDelNode(delContent, context);
			    handelDelRelOrProp(delContent, context);
			}
			context.put(USED, true);
		    }
		}
	return null;
    } 
    
    private boolean handelDelNode(String xx, Map<String, Object> funContext) {
	boolean useAnd = false;
	List<Long> objIds = new ArrayList<>();
	for (String qie : andRel) {
	    if (xx.contains(qie)) {
		String[] starts = xx.split(qie);
		for (String ri : starts) {
		    if (containLabelInfo(ri)) {
			Map<String, Object> onlyContext = new HashMap<>();
			ri = onlyName(onlyContext, ri);
			String dataLabel = string(onlyContext, "dataLabel");
			Map<String, Object> data2 = getData(ri, dataLabel, funContext);
			Long idOfData = id(data2);
			objIds.add(idOfData);
		    } else {
			objIds.add(getIdOfData(ri, funContext));
		    }
		}
		useAnd = true;
	    }
	}
	if (useAnd && objIds.size() > 0) {
	    neo4jUService.execute("MATCH (n) WHERE ID(n) IN [" + joinLong(objIds) + "] DETACH DELETE n ");
	}
	return useAnd;
    }

    /**
     * 删除关系或者删除属性
     * 
     * @param xx
     * @return
     */
    public boolean handelDelRelOrProp(String xx, Map<String, Object> funContext) {
	boolean useAnd = false;
	boolean useOwni = false;
	for (String qie : andRel) {
	    if (xx.contains(qie)) {
		String[] ands = xx.split(qie);
		for (String ai : ands) {
		    for (String oi : ownWords) {
			if (xx.contains(oi)) {
			    useOwni = true;
			    String[] orpi = ai.split(oi);
			    Map<String, Object> oneData = selectedData(funContext, orpi[0]);
			    Long owniId = id(oneData);
			    Map<String, String> nameColById = neo4jUService.getNameColById(owniId);

			    String col = nameColById.get(orpi[1]);
			    if (col != null) {
				neo4jUService.execute("MATCH (n) WHERE ID(n)  =" + owniId + " REMOVE n." + col);
			    } else {
				Long idOfMd = getIdOfMd(orpi[1]);
				if (idOfMd == null) {
				    Long idOfData = getIdOfData(orpi[1], funContext);
				    deleteRel(owniId, idOfData);
				} else {
				    deleteRel(owniId, idOfMd);
				}
			    }
			}
		    }
		}
		useAnd = true;
	    }
	}

	if (!useAnd) {
	    for (String oi : ownWords) {
		if (xx.contains(oi)) {
		    useOwni = true;
		    String[] orpi = xx.split(oi);
		    Map<String, Object> oneData = getData(orpi[0], funContext);
		    Long owniId = id(oneData);
		    Map<String, String> nameColById = neo4jUService.getNameColById(owniId);

		    String col = nameColById.get(orpi[1]);
		    if (col != null) {
			neo4jUService.execute("MATCH (n) WHERE ID(n)  =" + owniId + " REMOVE n." + col);
		    } else {
			Long idOfMd = getIdOfMd(orpi[1]);
			if (idOfMd == null) {
			    Long idOfData = getIdOfData(orpi[1], funContext);
			    deleteRel(owniId, idOfData);
			} else {
			    deleteRel(owniId, idOfMd);
			}
		    }
		}

	    }
	}

	return useAnd;
    }

}

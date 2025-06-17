package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 * 1、A和B是关系C 是朋友关系，恋人，同学，师徒，上级，下级，队友，校友 2、A是B的C /小明是小李的老师，好朋友，兄弟，父母，债务人，买受人
 * 
 * 
 * A和xxx与xx2是关系1 可以访问动作。 A可以/能：看见\访问\读写\更新\删除 B。 未读 已读 A昨天干了什么 今天要干什么？ 出现了什么问题？
 * 
 * 添加关系a-relName(relCode)->b
 * 
 * @param msg
 * @param context
 */
@Component
public class StartWithCreateRel extends ParseExcuteDomain implements MsgProcess {
    
    
    
    /**
     * 1、A和B是关系C 是朋友关系，恋人，同学，师徒，上级，下级，队友，校友 2、A是B的C /小明是小李的老师，好朋友，兄弟，父母，债务人，买受人
     * 
     * 
     * A和xxx与xx2是关系1 可以访问动作。 A可以/能：看见\访问\读写\更新\删除 B。 未读 已读 A昨天干了什么 今天要干什么？ 出现了什么问题？
     * 
     * 添加关系a-relName(relCode)->b
     * 
     * @param msg
     * @param context
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {
		 if(!msg.contains("->")&&!msg.contains("<-")) {
				 return null;
		}
		for (String ari : newRelation) {
			if (!bool(context, USED)) {
				processStartNewRel(msg, context, ari);
			}
		}
		return null;
    }
    
    /**
     * 解析执行新增关系
     * 
     * @param msg
     * @param context
     * @param ari
     */
    public void processStartNewRel(String msg, Map<String, Object> context, String ari) {
	if (msg.startsWith(ari)) {
	    msg = msg.replaceFirst(ari, "");
	    context.put(USED, true);
	    // 获取默认数据：
	    boolean used = false;
	    for (String isRel : kEqualv) {
		if (msg.contains(isRel)) {
		    used = true;
		    String[] startEndOfRel = msg.split(isRel);
		    // 租户数据授权？该如何授予权限？
		    String start = startEndOfRel[0].replaceFirst(ari, "");
		    boolean useAnd = false;
		    List<Long> startIds = new ArrayList<>();
		    String rightOfIs = startEndOfRel[1];
		    for (String qie : andRel) {
			if (start.contains(qie)) {
			    String[] starts = rightOfIs.split(qie);
			    for (String si : starts) {
				if (containLabelInfo(rightOfIs)) {
				    addStartId(startIds, si, context);
				} else {
				    startIds.add(getIdOfData(si, context));
				}
			    }
			    useAnd = true;
			}
		    }
		    Long startId = null;
		    if (!useAnd) {
			if (containLabelInfo(rightOfIs)) {
			    addStartMetaRel2End(start, rightOfIs, context);
			} else {
			    startId = getIdOfData(start, context);
			    startAddRel(rightOfIs, startId, context);
			}
		    } else {
			if (startIds.size() == 2) {
			    createRel(startIds.get(0), startIds.get(1), rightOfIs);
			}
			if (startIds.size() > 2) {
			    for (Long objectId : startIds) {
				for (Long otherId : startIds) {
				    if (!objectId.equals(otherId)) {
					createRel(objectId, otherId, rightOfIs);
				    }
				}
			    }
			}
		    }
		}
	    }
	    if (!used) {
		createRel(msg, context);
	    }
	}
	
	if(!bool( context,USED)) {
	    createRel(msg,context);
	}
    }

    public void createRel(String msg, Map<String, Object> context) {
	String[] args = { "->", "<-" };
	boolean created = false;
	for (String ai : args) {
	    if (!created && msg.contains(ai)) {
		String[] split = msg.split(ai);
		String start = null;
		String end = null;
		String rel = null;
		if (split[0].contains("-")) {//-[]->
		    end = clearBigkh(split[1]);
		    String[] startRel = split[0].split("-");
		    start = clearBigkh(startRel[0]);
		    rel = startRel[1];
		} else if (split[1].contains("-")) {//<-[]-
		    end = clearBigkh(split[0]);
		    String[] relStart = split[1].split("-");
		    start = clearBigkh(relStart[1]);
		    rel = relStart[0];
		}
		Long startId = getIdOfData(start, context);
		if(startId==null) {
		    continue;
		}
		if (end.contains("、")) {
		    String[] split2 = end.split("、");
		    Boolean createi = false;
		    for (String ei : split2) {
			createi = createOneRel(context, created, ei, rel, startId);
		    }
		    if (createi) {
			created = true;
		    }
		} else {
		    created = createOneRel(context, created, end, rel, startId);
		}

	    }
	}
    }

    public boolean createOneRel(Map<String, Object> context, boolean created, String end, String rel, Long startId) {
	Long endId = getIdOfData(end, context);
	String relCode = null;
	// 判断是否存在中文和应为关系名称和代码
	Map<String, Object> propsMap = null;
	int rightx = rel.indexOf("}");
	int leftX = rel.indexOf("{");
	if (leftX >= 0 && rightx > 0) {
	    String props = rel.substring(leftX, rightx + 1);
	    propsMap = JSON.parseObject(props);
	    rel = rel.split("\\{")[0];
	}

	if (rel.contains("(") && rel.contains(")")) {
	    String[] codePart = rel.split("\\(");
	    relCode = codePart[1].replaceAll("\\)", "");
	    rel = codePart[0];

	} else if (!rel.contains("(") && !rel.contains(")")) {
	    Map<String, Object> data2 = getData(rel, context);
	    if (data2 != null) {
		relCode = string(data2, "reLabel");
	    }
	}
	if (endId != null) {
	    updateRelDefine(startId, endId, rel, relCode);
	    if (propsMap != null && !propsMap.isEmpty()) {
		createRel(startId, endId, rel, relCode, propsMap);
	    } else {
		createRel(startId, endId, rel, relCode);
	    }

	    created = true;
	    context.put(USED, true);
	}
	return created;
    }

}

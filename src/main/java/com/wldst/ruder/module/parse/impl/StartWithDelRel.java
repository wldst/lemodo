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
public class StartWithDelRel extends ParseExcuteDomain implements MsgProcess {
    
    
    
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
	for (String ari : deleteRels) {
		if (!bool(context, USED)) {
		    processStartDelRel(msg, context, ari);
		}
	    }
	return null;
    }
    
    /**
     * 解析执行删除关系
     * 
     * @param msg
     * @param context
     * @param ari
     */
    public void processStartDelRel(String msg, Map<String, Object> context, String ari) {
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
			    for (String ri : starts) {
				if (containLabelInfo(rightOfIs)) {
				    addStartId(startIds, ri, context);
				} else {
				    startIds.add(getIdOfData(ri, context));
				}
			    }
			    useAnd = true;
			}
		    }
		    Long startId = null;
		    if (!useAnd) {
			if (containLabelInfo(rightOfIs)) {
			    delStartMetaRel2End(start, rightOfIs, context);
			} else {
			    startId = getIdOfData(start, context);
			    startDelRel(rightOfIs, startId, context);
			}
		    } else {
			if (startIds.size() == 2) {
			    delRel(startIds.get(0), startIds.get(1), rightOfIs);
			}
			if (startIds.size() > 2) {
			    for (Long objectId : startIds) {
				for (Long otherId : startIds) {
				    if (!objectId.equals(otherId)) {
					delRel(objectId, otherId, rightOfIs);
				    }
				}
			    }
			}
		    }
		}
	    }
	    if (!used) {
		String clearmsg = msg.replaceAll("\\}", "").replaceAll("\\{", "");
		String[] args = { "->", "<-" };
		boolean deleted = false;
		for (String ai : args) {

		    if (!deleted && msg.contains(ai)) {
			String[] split = clearmsg.split(ai);
			String start = null;
			String end = null;
			String rel = null;
			if (split[0].contains("-")) {
			    end = split[1];
			    String[] split2 = split[0].split("-");
			    start = split2[0];
			    rel = split2[1];
			} else if (split[1].contains("-")) {
			    end = split[0];
			    String[] split2 = split[1].split("-");
			    start = split2[1];
			    rel = split2[0];
			}
			Long startId = getIdOfData(start, context);
			Long endId = getIdOfData(end, context);
			String relCode = null;
			// 判断是否存在中文和应为关系名称和代码
			if (rel.contains("(") && rel.contains(")")) {
			    String[] split2 = rel.split("\\(");
			    relCode = split2[1].replaceAll("\\)", "");
			    rel = split2[0];
			} else if (!rel.contains("(") && !rel.contains(")")) {
			    Map<String, Object> data2 = getData(rel, context);
			    relCode = code(data2);
			}
			delRel(startId, endId, rel, relCode);
			deleted = true;
		    }
		}
	    }
	}
    }

}

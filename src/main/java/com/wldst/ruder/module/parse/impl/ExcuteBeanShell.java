package com.wldst.ruder.module.parse.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
/**
 * update xx's dd =yy 更新小明的性别为女 更新小白的性别为女
 * 
 * 更新小白和李白的性别为女
 * 
 * @param msg
 * @param context
 */
@Component
public class ExcuteBeanShell extends ParseExcuteDomain implements MsgProcess {
    
    
    
    /**
     * update xx's dd =yy 更新小明的性别为女 更新小白的性别为女
     * 
     * 更新小白和李白的性别为女
     * 
     * @param msg
     * @param context
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {
	for (String updatei : updates) {
	    if (!bool(context, USED)&&msg.startsWith(updatei)) {
		context.put(USED, true);
		// 租户数据授权？该如何授予权限？
		String noPrefix = msg.replaceFirst(updatei, "");
		// 找到等于
		for (String eqi : kEqualv) {
		    if (noPrefix.contains(eqi)) {
			String[] startEndOfRel = noPrefix.split(eqi);
			// 租户数据授权？该如何授予权限？
			handleLeftAndRight(startEndOfRel[0], startEndOfRel[1], context);
		    }
		}
		for (String eqi : relName) {
		    if (noPrefix.contains(eqi)) {
			String[] startEndOfRel = noPrefix.split(eqi);
			// 租户数据授权？该如何授予权限？
			addStartMetaRel2End(startEndOfRel[0], startEndOfRel[1], context);
		    }
		}

	    }
	}
	return null;
    } 
    /**
     * 处理等式的左右两边
     * 
     * @param left
     * @param rightOfIs
     */
    private void handleLeftAndRight(String left, String rightOfIs, Map<String, Object> context) {
	boolean leftHasOi = false;
	for (String oi : ownWords) {
	    if (left.contains(oi)) {
		leftHasOi = true;
		String[] leftBelong = left.trim().split(oi);
		String objectStr = leftBelong[0];
		List<Long> startIds = parseObjectsId(context, objectStr);

		String propOrRel = leftBelong[1];

		for (Long startId : startIds) {
		    Map<String, String> nameColById = neo4jUService.getNameColById(startId);
		    String coli = nameColById.get(propOrRel);
		    if (coli != null) {
			// 判断字段是否是关联字段。管理字段根据
			JSONObject vo = new JSONObject();
			Map<String, Object> metaDataById = neo4jUService.getMetaDataById(startId);
			String labelPo = label(metaDataById);
			vo.put("poId", labelPo);
			// 查询自定义字段数据
			List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
			if (fieldInfoList != null && !fieldInfoList.isEmpty()) {
			    Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
			    for (Map<String, Object> fi : fieldInfoList) {
				Object object = fi.get(FIELD);
				object = object == null ? fi.get(ID) : object;
				customFieldMap.put(String.valueOf(object), fi);
			    }
			    Map<String, Object> field = customFieldMap.get(coli);

			    if (field != null) {
				String type = String.valueOf(field.get("type"));
				if ("true".equals(field.get("isPo"))) {// 更新关联字段
				    List<Map<String, Object>> dataBy = neo4jUService.getDataBy(type, rightOfIs);
				    if (dataBy != null) {
					String id2 = string(dataBy.get(0), ID);
					neo4jUService.updateBy(startId, coli, id2);
				    }
				}
			    }
			} else {// 更新属性
			    neo4jUService.updateBy(startId, coli, rightOfIs);
			}
		    } else {
			// 没有字段则直接添加关系
			startAddRel(rightOfIs, startId, propOrRel, context);
		    }
		}
	    }
	}
	if (!leftHasOi) {
	    // 没有字段则直接添加关系
	    List<Long> startIds = parseObjectsId(context, left);
	    for (Long si : startIds) {
		startAddRel(rightOfIs, si, context);
	    }
	}
    }
     

}

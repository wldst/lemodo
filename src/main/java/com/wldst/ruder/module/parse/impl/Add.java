package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.springframework.stereotype.Component;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.TextUtil;

import bsh.EvalError;
/**
 * create xx  用户，角色
 * 
 * 更新小白和李白的性别为女
 * 
 * @param msg
 * @param context
 */
@Component
public class Add extends ParseExcuteDomain implements MsgProcess {
    
    
    
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
	for (String ni : newNode) {
	    List<Map<String, Object>> createSomething = createSomething(msg, ni, context);
	    if(createSomething!=null&&!createSomething.isEmpty()) {
		StartWithManage sm = (StartWithManage) SpringContextUtil.getBean("startWithManage");
		context.put(USED, false);
//		string(context,"metaName");//解决，创建元数据时，打开待办的情况。本次需要重置上下文吗？
		Map<String, Object> data = createSomething.get(0);
		List<Map<String, Object>> process = sm.process("管理"+string(context,"metaName"), context);
		if(process!=null&&!process.isEmpty()) {
		    return process;
		}
		return data;
	    }
	}
	return null;
    } 
    /**
     * 创建相关的事情
     * 
     * @param string
     * @param newNode
     */
    private List<Map<String, Object>> createSomething(String string, String newCreate
	    , Map<String, Object> context) {
	string = clearFuhao(string);
	List<Map<String, Object>> createObjects = new ArrayList<>();
	if (string.indexOf(newCreate) > -1) {
	    String[] create = string.split(newCreate);
	    for (String createObject : create) {
		if (createObject != null && !"".equals(createObject.trim()) && !createObject.startsWith("关系")) {
		    Map<String, Object> createOne = createOne(createObject,context);
		    if (createOne != null) {
			
			context.put(USED, true);
			createObjects.add(createOne);
		    }
		}
	    }
	} 

	return createObjects;
    }
    
    private Map<String, Object> createOne(String createObject, Map<String, Object> context) {
	if (containLabelInfo(createObject)) {
	    // 测试(场景)
	    Map<String, Object> dataOfKuohao = getDataOfKuohao(createObject);
	    if (dataOfKuohao != null) {
		String[] split = strArray(dataOfKuohao, "split");
		String meta = string(dataOfKuohao, "meta");

		List<Map<String, Object>> metaDataByName = getMetaDataByName(meta);
		Map<String, Object> mdi = null;
		if (metaDataByName.size() > 1) {
		    mdi = userSelect(context, metaDataByName);
		} else {
		    mdi = metaDataByName.get(0);
		}
		String operateLabel = label(mdi);
		context.put("operateLabel", operateLabel);
		context.put("metaName", name(mdi));
		Map<String, Object> data = new HashMap<>();
		// 获取默认属性
		// 字段默认值
		addDefaultValue(operateLabel, data);
		data.put(NAME, split[0]);
		Node saveByBody = neo4jService.saveByBody(data, operateLabel);
		 
		 
		// Beanshell，新建的逻辑，将label数据加入。
		try {
		    Map<String, Object> defaultRel = neo4jService.getAttMapBy(CODE, "saveWithDefaultRel", "BeanShell");
		    defaultRel.put(LABEL, operateLabel);
		    defaultRel.put(ID, id(data));
		    bs.run(defaultRel);

		    Map<String, Object> newLogic = neo4jService.getAttMapBy(CODE, "saveWithDefaultLogic", "BeanShell");
		    if (newLogic != null && string(newLogic, BeanShellDomain.BS_SCRIPT) != null) {
			newLogic.put(LABEL, operateLabel);
			newLogic.put(ID, id(data));
			newLogic.put(NAME, name(data));
			newLogic.put(CODE, code(data));
			bs.run(newLogic);
		    }

		    if (operateLabel.equals("Scene")) {
			Map<String, Object> nameLike = neo4jService.getAttMapBy(CODE, "newSceneNameLike", "BeanShell");
			nameLike.put(LABEL, operateLabel);
			nameLike.put(ID, id(data));
			nameLike.put(NAME, name(data));
			bs.run(nameLike);
		    }
		} catch (EvalError e) {
		    e.printStackTrace();
		}
		context.put(USED, true);
		return data;
	    }
	}

	String propName = getPropName(createObject);
	propName = clearFuhao(propName);
	String metaName = "";
	String name = "";
	Map<String, Object> node = null;
	if (propName.indexOf("叫") > -1) {
	    String[] prop = propName.split("叫");
	    metaName = prop[0].trim();
	    name = prop[1].trim();
	    node = getNode(META_DATA, "name", metaName);
	} else {
	    String operateLabel = getOperateLabel(context);
	    Map<String, Object> data = new HashMap<>();
	    data.put(NAME, propName);
	    if(operateLabel==null) {
		data.put(LABEL, TextUtil.pinyin(propName));
	    } 
	    context.put("metaName", propName);
	    if (operateLabel == null) {
		operateLabel = META_DATA;
	    }
	    context.put("operateLabel", operateLabel);
	    // 获取默认属性
	    // 字段默认值
	    addDefaultValue(operateLabel, data);
	    Node saveByBody = neo4jService.saveByBody(data, operateLabel);
	    return data;
	}
	if (node != null) {
	    String label = label(node);
	    Map<String, Object> data = new HashMap<>();
	    data.put(NAME, name);
	    context.put("operateLabel", label);
	    context.put("metaName", propName);
	    addDefaultValue(label, data);
	    Node saveByBody = neo4jService.saveByBody(data, label);
	    return data;
	}
	return null;
    }

    public void addDefaultValue(String operateLabel, Map<String, Object> data) {
	List<Map<String, Object>> fieldInfoList = objectService.getFieldInfo(operateLabel);
	String key = "field";
	for (Map<String, Object> fi : fieldInfoList) {
	    Object object = fi.get(key);// 获取字段
	    if (object != null) {
		String string = string(fi, FIELD_DEFAULT_VALUE);
		if (string != null) {
		    if(object.equals(CODE)||object.equals(LABEL)) {
			 data.put(String.valueOf(object), string+Calendar.getInstance().getTimeInMillis());
		    }else {
			 data.put(String.valueOf(object), string);
		    }
		   
		}
	    }
	}
    }

}

package com.wldst.ruder.module.workflow.template.beans;

import java.util.Map;

import com.wldst.ruder.util.MapTool;

/**
 * @author wldst
 * 
 */
public class TtaskProp {
    // 任务属性原名称
     public static String originalName="originalName";

    // 任务属性名
     public static String name="name";

    // 任务属性值
     public static String value="value";

    public static String getOriginalName(Map<String, Object> data) {
	 	return MapTool.string(data, originalName);
    }

    public static void setOriginalName(String originalNamex,Map<String, Object> data)
    {
 	data.put(originalName,originalNamex);
    }

    public static String getName(Map<String, Object> data) {
	return MapTool.string(data, name);
    }

    public static void setName(String namex,Map<String, Object> data)
    {
 	data.put(name,namex);
    }

    public static String getValue(Map<String, Object> data) {
	return MapTool.string(data, value);
    }

    public static void setValue(String valuex,Map<String, Object> data)
    {
 	data.put(value,valuex);
    }

}

package com.wldst.ruder.module.workflow.template.beans;

import java.util.Map;

import com.wldst.ruder.util.MapTool;

/**
 * 流程模板任务决策信息
 * 
 * @author wldst
 */
public class TtaskDecision {
    // 任务决策显示名
     public static String viewName="viewName";

    // 任务决策名
     public static String name="name";

    // 任务决策排序号
     public static String orderID="orderID";

    // 决策执行方式
     public static String executeType="executeType";

    public static String getViewName(Map<String, Object> data)
    {
 	return MapTool.string(data, viewName);
    }

    public static void setViewName(String viewNamex,Map<String, Object> data)
    {
 	data.put(viewName,viewNamex);
    }

    public static String getName(Map<String, Object> data)
    {
 	return MapTool.string(data, name);
    }

    public static void setName(String namex,Map<String, Object> data)
    {
 	data.put(name,namex);
    }

    public static String getOrderID(Map<String, Object> data)
    {
 	return MapTool.string(data, orderID);
    }

    public static void setOrderID(String orderIDx,Map<String, Object> data)
    {
 	data.put(orderID,orderIDx);
    }

    public static String getExecuteType(Map<String, Object> data)
    {
 	return MapTool.string(data, executeType);
    }

    public static void setExecuteType(String executeTypex,Map<String, Object> data)
    {
 	data.put(executeType,executeTypex);
    }

}

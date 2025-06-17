package com.wldst.ruder.module.workflow.template.beans;

import java.util.Map;

import com.wldst.ruder.util.MapTool;

/**
 * 流程模板开始任务信息
 * 
 * @author wldst
 */
public class Tstart extends TemplateTask {
    /**
     * 默认构造函数
     */
    // WFEConstants.WFTASK_TYPE_START

    // 后续任务节点ID
    public static String next = "next";

    // 与之匹配的任务节点id
    public static String pairNode = "pairNode";

    // 任务描述
    public static String descript = "descript";

    /**
     * 得到与之匹配的任务节点id
     * 
     * @return 与之匹配的任务节点id.
     */
    public static String getPairNode(Map<String, Object> data) {
	return MapTool.string(data, pairNode);
    }

    /**
     * 设置与之匹配的任务节点id
     * 
     * @param pairNode 与之匹配的任务节点id.
     */
    public static void setPairNode(String pairNod, Map<String, Object> data) {
	data.put(pairNode, pairNod);
    }

    /**
     * 得到开始节点的前续节点id
     * 
     * @return 开始节点的前续节点id.
     */
    public static String getNext(Map<String, Object> data) {
	return MapTool.string(data, next);
    }

    /**
     * 设置开始节点的前续节点id
     * 
     * @param previous 开始节点的前续节点id.
     */
    public static void setNext(String previousS, Map<String, Object> data) {
	data.put(next, previousS);
    }

    /**
     * 得到任务描述
     * 
     * @return 任务描述.
     */
    public static String getDescript(Map<String, Object> data) {
	return MapTool.string(data, descript);
    }

    /**
     * 设置任务描述
     * 
     * @param descript 任务描述.
     */
    public static void setDescript(String descript, Map<String, Object> data) {
	data.put(descript, descript);
    }

}

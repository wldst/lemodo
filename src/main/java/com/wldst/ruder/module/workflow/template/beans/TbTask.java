package com.wldst.ruder.module.workflow.template.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;

/**
 * 流程模板分支任务信息
 * 
 * @author wldst
 * 
 */
public class TbTask extends TemplateTask {

    // 与分支任务相匹配的收缩任务ID
    public static String pairNode = "pairNode";

    // 分支任务描述
    public static String descript = "descript";

    // 分支人员输入节点ID
    public static String input = "input";

    // 分支任务输出信息集合
    public static String branchOutputs = "branchOutputs";

    /**
     * 获取分支任务输出信息列表
     * 
     * @return 分支任务输出信息列表
     */
    public static List<Map<String, Object>> getBranchOutputList(Map<String, Object> data) {
	return MapTool.listMapObject(data, branchOutputs);

    }

    public static Map<String, Map<String, Object>> getBranchOutputMap(Map<String, Object> data) {
	return MapTool.mapKeyMap(data, branchOutputs);

    }

    /**
     * 获取模板任务分支信息
     * 
     * @param outputTaskId 模板任务分支输出ID
     * @return 模板任务分支信息
     */
    public static Map<String, Object> getTemplateBranchOutput(String outputTaskId, Map<String, Object> data) {
	Map<String, Map<String, Object>> branchOutputMap = getBranchOutputMap(data);
	if (branchOutputMap != null) {
	    return branchOutputMap.get(outputTaskId);
	} else {
	    return null;
	}
    }

    /**
     * 移出任务分支输出
     * 
     * @param outputTaskId 分支输出任务ID
     */
    public static void removeTemplateBranchOutput(String outputTaskId, Map<String, Object> data) {
	Map<String, Map<String, Object>> branchOutputMap = getBranchOutputMap(data);
	if (branchOutputMap != null) {
	    branchOutputMap.remove(outputTaskId);
	}
    }

    /**
     * 增加流程模板分支输出条件
     * 
     * @param output 模板分支输出条件
     */
    public static void addTemplateBranchOutput(Map<String, Object> output, Map<String, Object> data) {
	Map<String, Map<String, Object>> branchOutputMap = getBranchOutputMap(data);
	if (branchOutputMap != null) {
	    branchOutputMap = new HashMap<>();
	}
	branchOutputMap.put(MapTool.string(output, "outputTaskId"), output);
    }

    

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
    public static String getInput(Map<String, Object> data) {
 	return MapTool.string(data, input);
    }

    /**
     * 设置开始节点的前续节点id
     * 
     * @param previous 开始节点的前续节点id.
     */
    public static void setInput(String previousS, Map<String, Object> data) {
 	data.put(input, previousS);
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

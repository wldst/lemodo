package com.wldst.ruder.module.workflow.template.beans;

import java.util.Map;

import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;

/**
 * 流程模板结束任务,WFEConstants.WFTASK_TYPE_END
 * 
 * @author wldst
 */
public class Tend extends TemplateTask {

    // 与之匹配的任务节点id
    public static String pairNode = "pairNode";

    // 结束节点的前续节点id
    public static String previous = "previous";

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
     * 得到结束节点的前续节点id
     * 
     * @return 结束节点的前续节点id.
     */
    public static String getPrevious(Map<String, Object> data) {
	return MapTool.string(data, previous);
    }

    /**
     * 设置结束节点的前续节点id
     * 
     * @param previous 结束节点的前续节点id.
     */
    public static void setPrevious(String previousS, Map<String, Object> data) {
	data.put(previous, previousS);
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

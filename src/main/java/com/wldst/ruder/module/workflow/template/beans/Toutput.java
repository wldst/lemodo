package com.wldst.ruder.module.workflow.template.beans;

import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.util.MapTool;

/**
 * 流程模板分支输出信息
 * 
 * @author wldst
 * 
 */
public class Toutput extends BpmDo{
    // 分支输出任务ID
    public static String outputTaskID="outputTaskId";

    // 分支输出条件
    public static String condition="condition";

    /**
     * 得到分支输出任务ID
     * 
     * @return 分支输出任务ID.
     */
    public static String getOutputTaskID(Map<String, Object> data) {
	return MapTool.string( data,outputTaskID);
    }

    /**
     * 设置分支输出任务ID
     * 
     * @param outputTaskID 分支输出任务ID.
     */
    public static void setOutputTaskID(String outputTaskID ,Map<String, Object> data) {
	data.put(outputTaskID,outputTaskID);
    }

    /**
     * 得到分支输出条件
     * 
     * @return 分支输出条件.
     */
    public static String getCondition(Map<String, Object> data) {
	return MapTool.string( data,condition);
    }

    /**
     * 设置分支输出条件
     * 
     * @param condition 分支输出条件.
     */
    public static void setCondition(String conditiond,Map<String, Object> data) {
	data.put(condition, conditiond);
    }

}

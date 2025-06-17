package com.wldst.ruder.module.workflow.beans;

import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.formula.FormulaParseUtil;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.TextUtil;
import com.wldst.ruder.util.MapTool;

/**
 * 流程实例之任务树型数据
 * 
 * @author wldst
 * 
 */
public class TaskProperties extends BpmDo {


    


    // 任务最大执行人数
    public static String maxExecutorNum = "maxExecutorNum";

    // 任务完成所需比例
    public static String maxExecutorPercent = "maxExecutorPercent";













    /**
     * 得到任务节点是否所有执行人都必须执行
     * 
     * @return MapTool.longValue(data, 任务节点是否所有执行人都必须执行
     */
    public static int  getAllExecute(Map<String, Object> data) {
	return MapTool.integer(data, "allExecute");
    }


    public static int  getMaxExecutorNum(Map<String, Object> data) {
//	if (!TextUtil.isBlank("maxExecutorNum")) {
//	    String[] infos = maxExecutorNum.split(",");
//	    if (!TextUtil.isBlank(infos[0])) {
//		maxExecutorNum = NumberUtil.parseInt(infos[0], 1);
//	    }
//	}
	return MapTool.integer(data, maxExecutorNum);
    }

    public static float getMaxExecutorPercent(Map<String, Object> data) throws CrudBaseException {
//	if (!TextUtil.isBlank(propertyVarchar[1])) {
//	    String[] infos = propertyVarchar[1].split(",");
//	    if (!TextUtil.isBlank(infos[1])) {
//		maxExecutorPercent = FormulaParseUtil.parsePercent(infos[1]);
//	    }
//	}
	return MapTool.floatValue(data, maxExecutorPercent);
    }


    /**
     * 得到是否等待子流程执行完毕
     * 
     * @return MapTool.longValue(data, 是否等待子流程执行完毕
     */
    public static int  getWaitExecSubFlow(Map<String, Object> data) {
	return MapTool.integer(data, "waitExecSubFlow");
    }


    /**
     * 得到任务是否为自动执行任务
     * 
     * @return MapTool.longValue(data, 任务是否为自动执行任务
     */
    public static int  getTaskAutoExecute(Map<String, Object> data) {
	return MapTool.integer(data, "taskAutoExecute");
    }

}

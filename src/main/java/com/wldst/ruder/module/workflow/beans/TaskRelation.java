package com.wldst.ruder.module.workflow.beans;

import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.util.MapTool;

/**
 * 流程实例之任务之间的关系
 */
public class TaskRelation extends BpmDo {
    // 任务关系ID



    // 关联任务ID
    private static String relationTaskID = "relationTaskId";
    /**
     * 得到关联任务ID
     * 
     * @return 关联任务ID.
     */
    public static long getRelationTaskID(Map<String, Object> data) {
	return MapTool.longValue(data, relationTaskID);
    }



}

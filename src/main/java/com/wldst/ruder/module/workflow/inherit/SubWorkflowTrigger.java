package com.wldst.ruder.module.workflow.inherit;

import java.util.Map;

import com.wldst.ruder.module.workflow.beans.BpmTask;
import com.wldst.ruder.module.workflow.beans.WfPc;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;

/**
 * 子流程触发接口
 * 
* @author wldst
 */
public abstract class SubWorkflowTrigger
{
   /**
    * 触发子流程业务方法
    * 
    * @param dba 数据库操作对象
    * @param workflow 流程实例
    * @param wfTask 触发子流程的任务实例
    * @param runData 任务运行数据对象
    * @throws Exception
    */
   public abstract Map<String, Object> triggerSubWorkflow(
	   Map<String, Object> workflow, Map<String, Object> wfTask, Map<String, Object> runData)
         throws CrudBaseException;
}

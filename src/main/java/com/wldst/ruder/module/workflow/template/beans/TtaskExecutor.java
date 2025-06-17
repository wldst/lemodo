package com.wldst.ruder.module.workflow.template.beans;

import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.util.MapTool;

/**
 * 流程模板任务执行人
 * 
* @author wldst
 * 
 */
public class TtaskExecutor extends BpmDo
{
   // 流程模板任务执行人定义ID
//   private String id;

   // 流程模板任务执行公式定义
   public static String expression="expression";
   /**
    * 得到流程任务名称
    * 
    * @return 流程任务名称.
    */
   public static String getExpression(Map<String, Object> data)
   {
	return MapTool.string(data, expression);
   }

   /**
    * 设置流程任务名称
    * 
    * @param wfTaskName 流程任务名称.
    */
   public static void setExpression(String taskName,Map<String, Object> data)
   {
	data.put(expression,taskName);
   }
   /**
    * 设置ID
    * @param id
    * @param data
    */
   public static void setId(String id,Map<String, Object> data)
   {
	data.put(ID,id);
   }

}

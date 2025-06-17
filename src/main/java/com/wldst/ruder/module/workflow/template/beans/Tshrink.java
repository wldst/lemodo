package com.wldst.ruder.module.workflow.template.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;

/**
 * 模板任务收缩节点信息，WFEConstants.WFTASK_TYPE_SHRINK
 * 
* @author wldst
 * 
 */
public class Tshrink extends TemplateTask
{
 //WFEConstants.WFTASK_TYPE_SHRINK
 // 与之匹配的任务节点id
    public static String pairNode = "pairNode";

    // 任务描述
    public static String descript = "descript";

   // 收缩任务输出节点id
    public static String output="output";

    public static String inputTaskIDs ="inputTaskIDs";

   /**
    * 获取收缩任务输入节点id列表
    * 
    * @return 输入节点id列表
    */
    public static List<String> getInputTaskIDs(Map<String, Object> data)
   {
      return MapTool.arrayList(data, "inputTaskIDs");
   }

   /**
    * 移出指定的收缩任务输入节点
    * 
    * @param inputTaskID 指定的输入节点id
    */
    public static void removeInputTaskID(String inputTaskID,Map<String, Object> data)
   {
      List<String> inputTaskIDs = getInputTaskIDs(data);
    if (inputTaskIDs != null && inputTaskIDs.size() > 0)
      {
         int listSize = inputTaskIDs.size();
         for (int i = 0; i < listSize; i++)
         {
            String tempInputTaskID = (String) inputTaskIDs.get(i);

            if (inputTaskID != null && inputTaskID.equals(tempInputTaskID))
            {
               inputTaskIDs.remove(i);
               break;
            }
         }
      }
   }

   /**
    * 增加收缩任务输入节点id
    * 
    * @param inputTaskID 收缩任务输入节点id
    */
    public static void addInputTaskID(String inputTaskID,Map<String, Object> data)
   {
       List<String> inputTaskIDs = getInputTaskIDs(data);
      if (inputTaskIDs == null)
      {
         inputTaskIDs = new ArrayList<>();
      }
      inputTaskIDs.add(inputTaskID);
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
   public static String getOutput(Map<String, Object> data) {
	return MapTool.string(data, output);
   }

   /**
    * 设置开始节点的前续节点id
    * 
    * @param previous 开始节点的前续节点id.
    */
   public static void setOutput(String previousS, Map<String, Object> data) {
	data.put(output, previousS);
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

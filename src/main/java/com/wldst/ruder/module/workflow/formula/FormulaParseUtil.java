package com.wldst.ruder.module.workflow.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.TextUtil;

/**
 * 公式解析常用方法
 * 
 * 
 */
public class FormulaParseUtil
{
   // 日志对象
   private static Logger logger = LoggerFactory.getLogger(FormulaParseUtil.class);

   /**
    * 解析给定的普通任务扩展属性中关于执行人的公式<br>
    * 除去了人员重复
    * 
    * @param parse 执行人公式解析对象
    * @param extendsList 扩展属性列表
    * @return 解析后的人员ID数组
    * @throws Exception
    */
   public static long[] parseExecutorFormula(BpmExecutorFormulaParse parse,
         List<Map<String,Object>> extendsList) 
   {
      long[] retArray = null;
      if (parse == null)
      {
         throw new CrudBaseException("执行人解析对象为空,不能进行相关操作");
      }

      try
      {
         Map<String,Long> tempMap = new HashMap<>();
         if (extendsList != null && extendsList.size() > 0)
         {
            int listSize = extendsList.size();
            for (int i = 0; i < listSize; i++)
            {
        	Map<String,Object> extendInfo = extendsList
                     .get(i);
               long[] tempArray = parse.parseExecutorFormula(BpmDo.executorCondition(extendInfo) 
               );
               if (tempArray != null && tempArray.length > 0)
               {
                  for (int j = 0; j < tempArray.length; j++)
                  {
                     tempMap.put(String.valueOf(tempArray[j]),  Long.valueOf(
                           tempArray[j]));
                  }
               }
            }
         }
         if (tempMap != null && tempMap.size() > 0)
         {
            List<Long> tempList = new ArrayList<>(tempMap.values());
            int tempListSize = tempList.size();
            retArray = new long[tempListSize];
            for (int i = 0; i < tempListSize; i++)
            {
               Long obj = tempList.get(i);
               retArray[i] = obj.longValue();
            }
         }
      } catch (Exception ex)
      {
         LoggerTool.error(logger,"待办人解析失败:", ex);
         throw new CrudBaseException("待办人解析失败:", ex);
      }
      return retArray;
   }
   
   /**
    * 将数字字符串或百分数解析成浮点数
    * @param szPercent
    * @return
    * @
    */
   public static float parsePercent(String szPercent) {
	   float rate = 1f;
//	   [1-9]{0,1}[0-9](\\.[0-9])?%?
	   if(!TextUtil.isBlank(szPercent)){
		   int index = szPercent.indexOf("%");
		   if(0 > index){
			   // 小数
			   rate = NumberUtil.parseFloat(szPercent, 1f);
		   }else{
			   // 百分数
			   String data = szPercent.substring(0, index);
			   rate = NumberUtil.parseFloat(data, 1f) / 100;
		   }
	   }
	   return rate;
   }
   
   public static void main(String[] arg){
	   //System.out.println(Math.round(FormulaParseUtil.parsePercent("22.3%")*100));
   }
}

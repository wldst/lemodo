package com.wldst.ruder.module.workflow.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.util.WFEConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import oracle.jdbc.proxy.annotation.Post;

/**
 * 流程任务待办提醒相关页面控制器
 * 
* @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm")
public class TaskHandleRemindController 
{
   // 日志对象
   private static Logger logger = LoggerFactory.getLogger(TaskHandleRemindController.class);
   @Autowired
   private UserAdminService adminService;

   @Autowired
   private CrudNeo4jService crudService;

   /**
    * 流程任务待办首页提醒功能。
    * 该方法用于处理客户端请求，获取当前用户待处理的流程任务，并将这些任务以特定格式展示在首页。
    *
    * @param request 客户端请求，用于接收客户端发送的数据。
    * @param response 服务器端响应，用于向客户端返回数据或视图。
    * @return ModelAndView 业务视图模型，此处为空，因为功能实现直接返回了空的ModelAndView对象。
    * @throws Exception 抛出异常，当获取流程待办任务失败时。
    */
   
   @RequestMapping(value = "/remindOpt", method = RequestMethod.POST)
   @ResponseBody
   public ModelAndView remindIndex(HttpServletRequest request,
         HttpServletResponse response) throws Exception
   {
      ModelAndView mav = null;

      // 获取当前登录用户的ID
      Long currentUserId = adminService.getCurrentPasswordId();
      HashMap remindHandleTaskMap = null;
      List<String> remindHandleMapKeyList = null;
      try
      {
	  String query = "Match(n:BpmTaskExecute)where n.executorID="+currentUserId+" and n.executorStatus='"+WFEConstants.WFTASK_STATUS_READY+"' return n";
//	  4793
	  
         // 以下是处理并组织待办任务数据的逻辑
         List<Map<String,Object>> remindHandleTaskList =  crudService.cypher(query);

         // 如果待办任务列表不为空且大于0，则进行任务数据的处理和组织
         if (remindHandleTaskList != null && remindHandleTaskList.size() > 0)
         {
            remindHandleTaskMap = new HashMap<>();
            int listSize = remindHandleTaskList.size();
            for (int i = 0; i < listSize; i++)
            {
               // 遍历任务列表，对每个任务进行处理
               Map tempObj = (Map) remindHandleTaskList.get(i);

               // 提取任务模板标记
               String tempMark = (String)tempObj.get("TEMPLATEMARK");
               // 根据模板标记获取任务列表
               List<Map<String,Object>> tempMarkList = (List) remindHandleTaskMap.get(tempMark);
               if (tempMarkList == null)
               {
                  // 如果对应模板标记的任务列表为空，则初始化任务列表和标记列表
                  if (remindHandleMapKeyList == null)
                  {
                     remindHandleMapKeyList = new ArrayList<>();
                  }
                  remindHandleMapKeyList.add(tempMark);
                  tempMarkList = new ArrayList<>();
               }
               // 将任务添加到对应的模板标记的任务列表中
               tempMarkList.add(tempObj);
               remindHandleTaskMap.put(tempMark, tempMarkList);
            }
         }
         // 将处理后的任务数据设置到请求对象中，以便在视图中访问
         request.setAttribute("remindHandleTaskMap", remindHandleTaskMap);
         request.setAttribute("remindHandleMapKeyList", remindHandleMapKeyList);
      } catch (Exception ex)
      {
         // 记录获取流程待办任务失败的错误
         LoggerTool.error(logger,"获取流程待办任务首页提醒失败:", ex);
         throw new Exception("获取流程待办任务首页提醒失败:", ex);
      }
      // 创建并返回空的ModelAndView对象，此处实际不返回任何视图数据
      mav = new ModelAndView("");
      return mav;
   }


}

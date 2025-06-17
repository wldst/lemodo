package com.wldst.ruder.module.workflow.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.util.LoggerTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.beans.BpmTaskExecute;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.biz.BpmOperateAssist;
import com.wldst.ruder.module.workflow.formula.BpmExecutorFormulaParse;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程任务决策之同意
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm/agree")
public class AgreeController extends BpmDo {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(AgreeController.class);

    // 流程任务执行人解析类
    @Autowired
    private BpmInstanceManagerService bizWfInstanceManager;
    @Autowired
    private BpmOperateAssist workflowOperateAssist;
    @Autowired
    private BpmInstance bpmi;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService crudService;

//    @RequestMapping(value = "/init", method = { RequestMethod.GET, RequestMethod.POST })
//    public String init(Model model, @PathVariable("po") String poLabel, @RequestBody JSONObject vo,
//	    HttpServletRequest request) throws Exception {
//	String bizDataID = MapTool.string(vo, "bizDataID");
//	String bizTabName = MapTool.string(vo, "bizTabName");
//	String templateMark = MapTool.string(vo, "templateMark");
//	String currEmpID = MapTool.string(vo, "currEmpID");
//	String taskComeDatetime = MapTool.string(vo, "taskComeDatetime");
//
//	BpmExecutorFormulaParse parse = null;
//	try {
//	    Map<String, Object> workflow = bizWfInstanceManager.getBpmi(NumberUtil.parseLong(bizDataID, 0),
//		    bizTabName, templateMark);
//
//	    Map<String, Object> currentTask = BpmDo.nowSimpleTask(workflow);
//	    Map<String, Object> nextTask = bpmi.findNextSimpleTask(workflow);
//
//	    model.addAttribute("currentTask", currentTask);
//	    model.addAttribute("nextTask", nextTask);
//
//	    if (nextTask != null) {
//		List<Map<String, Object>> extendsList = BpmDo.extendsInfo(nextTask);
//		parse = new BpmExecutorFormulaParse(bizTabName, NumberUtil.parseLong(bizDataID, 0),
//			workflow);
//
//		long[] executorArray = FormulaParseUtil.parseExecutorFormula(parse, extendsList);
//		List<Map<String, Object>> xmlWFTaskExecutorList = crudService.getAllByIds(executorArray);
//
//		model.addAttribute("xmlWFTaskExecutorList", xmlWFTaskExecutorList);
//		model.addAttribute("currEmpID", currEmpID);
//		model.addAttribute("taskComeDatetime", taskComeDatetime);
//	    }
//	} catch (Exception ex) {
//	    LoggerTool.error(logger,"初始化流程任务决策同意画面失败:", ex);
//	}
//	return "workflow/wfdecision-agree";
//    }

/**
 * 初始化流程任务决策同意画面
 *
 * @param model 用于在视图和控制器之间传递数据的Model对象
 * @param flowId 流程ID，用于获取流程信息
 * @param request 用户的请求对象，可用于获取请求相关数据
 * @return 返回决策同意页面的视图名称
 * @throws Exception 如果处理过程中发生异常，则抛出
 */
@RequestMapping(value = "/init/{flowId}", method = {RequestMethod.GET, RequestMethod.POST})
public String init(Model model, @PathVariable("flowId") String flowId,
                   HttpServletRequest request) throws Exception {
    BpmExecutorFormulaParse parse = null;
    try {
        // 根据流程ID获取流程信息
        Map<String, Object> workflow = bpmi.getFlowi(Long.valueOf(flowId));
        if(longValue(workflow,"bizDataId")==null) {
            workflow =bpmi.getFlowiByBizId(Long.valueOf(flowId));
        }

        // 获取当前任务信息
        Map<String, Object> currentTask = bpmi.getNowNode(workflow);
        // 获取下一个任务信息
        Map<String, Object> nextTask = bpmi.findNextNormalNode(workflow);

        // 向模型中添加当前任务和下一个任务的信息
        model.addAttribute("currentTask", currentTask);
        model.addAttribute("nextTask", nextTask);

        if (nextTask != null) {
            // 尝试获取任务指定的执行用户列表
            List<Map<String, Object>> taskUsers = listMapObject(nextTask, "nodeUserList");
            if (taskUsers == null ||taskUsers.isEmpty()) {
                // 如果没有指定的执行用户，尝试根据任务角色获取执行用户列表
                taskUsers = new ArrayList<>();
                Long roleId = longValue(nextTask, "roleID");
                String roleUsers = "MATCH(n:Role)<-[r:HAS_ROLE]-(u:User) where id(n)=" + roleId + " return u.name as username,id(u) AS userId";
                List<Map<String, Object>> roleUserList = crudService.cypher(roleUsers);
                taskUsers.addAll(roleUserList);
            }
            model.addAttribute("taskUsers", taskUsers);

            // 处理当前任务的执行人信息，并添加到模型中
            Map<String, Object> param = new HashMap<>();
            param.put("instanceID", Long.valueOf(flowId));
            param.put("taskID", MapTool.id(currentTask));
            taskExecutorInfo(model, param);
        }
    } catch (Exception ex) {
        // 记录初始化流程任务失败的错误
        LoggerTool.error(logger,"初始化流程任务决策同意画面失败:", ex);
    }
    // 返回决策同意页面
    return "workflow/wfdecision-agree";
}

    /**
     * 向模型添加任务执行者信息和任务到达时间。
     *
     * @param model 用于在视图和控制器之间传递数据的模型对象。
     * @param param 包含查询参数的映射，用于从数据库中检索任务执行者信息。
     */
    private void taskExecutorInfo(Model model, Map<String, Object> param) {
        long taskComeDatetime=0;
        // 通过查询参数和实体类“BpmTaskExecute”从crudService中检索任务执行者信息
        List<Map<String, Object>> executorsOfTask = crudService.queryBy(param, "BpmTaskExecute");
        if (!executorsOfTask.isEmpty()) {
            Map<String, Object> currExecutor = null;
            // 获取当前用户的ID
            Long currentUserId = adminService.getCurrentUserId();
            // 遍历任务执行者列表，查找当前用户作为执行者的任务信息
            for (Map<String, Object> ei : executorsOfTask) {

                Long longValue = MapTool.longValue(ei, "executorID");
                if (longValue.equals(currentUserId)) {
                    currExecutor = ei;
                    break;
                }
            }
            if (currExecutor != null) {
                // 如果找到当前用户执行的任务，将任务信息添加到模型
                model.addAttribute("currentExecutor", currExecutor);
                // 获取任务到达时间
                taskComeDatetime = BpmTaskExecute.getTaskComeDatetime(currExecutor);
            }
        }

        // 将任务到达时间转换为字符串，并添加到模型，以便在视图中显示
        model.addAttribute("taskComeDatetime", MapTool.dateStr(taskComeDatetime));
    }

    /**
     * 处理同意操作的请求。
     *
     * @param model 用于在视图和控制器之间传递数据的模型对象。
     * @param vo 包含请求数据的JSONObject对象，通过RequestBody接收前端发送的数据。
     * @param request 用户的请求对象，用于获取请求相关信息。
     * @return WrappedResult 包含操作结果、相关数据和提示信息的封装对象。
     * @throws Exception 处理过程中可能抛出的异常。
     */
    @RequestMapping(value = "/agree", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public WrappedResult agree(Model model, @RequestBody JSONObject vo,
                                   HttpServletRequest request) throws Exception {
        // 尝试获取当前操作用户的ID
        Long currentUserId = adminService.getCurrentUserId();
        // 如果无法获取到用户ID，表示登录状态异常，返回失败结果
        if (currentUserId == 0) {
            return ResultWrapper.wrapResult(true, null, null, "任务提交失败！，请先登录");
        }
       String executorIDs= string(vo,"executorIDs");
        if(StringUtils.isBlank(executorIDs)) {
            return ResultWrapper.wrapResult(false, null, null, "请选择执行人！");
        }
        if(executorIDs.contains("[")&&executorIDs.contains("]")){
           vo.put("executorIDs",executorIDs.replaceAll("\\[","").replaceAll("]",""));
        }

        try {
            // 同意操作的标识
            String wfdecisionAgree = WFEConstants.WFDECISION_AGREE;
            // 执行工作流同意操作，并返回操作结果
            Boolean operateFlow = workflowOperateAssist.operateFlow(vo, currentUserId, wfdecisionAgree);
            if(operateFlow) {
        	 return ResultWrapper.wrapResult(operateFlow, vo, null, "任务提交成功！");
            }else {
        	 return ResultWrapper.wrapResult(operateFlow, vo, null, "任务提交失败！");
            }
        } catch (Exception ex) {
            // 捕获异常，将执行标志设置为失败，并返回异常信息
            model.addAttribute("executeFlag", Boolean.valueOf(false));
            return ResultWrapper.wrapResult(false, ex.getMessage(), null, "执行同意决策失败！");
        }
    }


}

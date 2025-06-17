package com.wldst.ruder.module.workflow.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.beans.*;
import com.wldst.ruder.util.LoggerTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.workflow.biz.BpmOperateAssist;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.formula.FormulaParseUtil;
import com.wldst.ruder.module.workflow.formula.BpmExecutorFormulaParse;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.TextUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程任务决策之不同意
 * 
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm/disAgree")
public class DisAgreeController extends MapTool{
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(DisAgreeController.class);
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private BpmInstance bpmi;
    @Autowired
    private CrudNeo4jService crudService;

    // 流程任务不同意画面URI
    private String disagreePageUri;

    // 不同意内部执行画面URI
    private String disagreeInnerPageUri;

    // 流程任务执行人解析类
    @Autowired
    private BpmInstanceManagerService bizWfInstanceManager;
    @Autowired
    private BpmOperateAssist workflowOperateAssist;

    /**
     * 初始化流程任务决策不同意画面。
     *
     * @param model 用于在视图和控制器之间传递数据的Model对象
     * @param flowId 流程实例的ID，通过URL路径变量传递
     * @param request 用户的请求对象，用于获取请求信息
     * @return 返回页面路径 "workflow/wfdecision-disagree"，用于视图解析
     * @throws Exception 抛出异常，处理流程实例获取、下一个正常节点查找、当前任务获取过程中可能出现的错误
     */
    @RequestMapping(value = "/init/{flowId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String init(Model model, @PathVariable("flowId") String flowId,  HttpServletRequest request) throws Exception {
        // 尝试从请求体中提取业务数据ID、业务表名、模板标记、当前员工ID和任务到达时间，并进行流程初始化
        BpmExecutorFormulaParse parse = null;
        try {
            // 获取流程实例信息
            Map<String, Object> workflow = bpmi.getFlowi(Long.valueOf(flowId));
            // 查找下一个正常节点
            Map<String, Object> nextTask = bpmi.findNextNormalNode(workflow);
            // 获取当前任务信息
            Map<String, Object> currentTask = bpmi.getNowNode(workflow);

            model.addAttribute("currentTask", currentTask);
            Map<String, Object> param = new HashMap<>();
            param.put("instanceID", Long.valueOf(flowId));
            param.put("taskID", MapTool.id(currentTask));
            taskExecutorInfo(model, param);
            // 获取之前审批的人员信息
            List<Map<String, Object>> preTasks = bpmi.getFlowExcute(MapTool.id(workflow),id(currentTask));
            model.addAttribute("preTasks", preTasks);

        } catch (Exception ex) {
            // 记录初始化流程任务失败的错误
            LoggerTool.error(logger,"初始化流程任务决策不同意画面失败:", ex);
        }
        // 返回页面路径
        return "workflow/wfdecision-disagree";
    }


    /**
     * 获取之前节点的执行者请求。
     * 该方法根据提供的任务ID和流程ID，获取任务执行信息，并根据执行信息或角色信息，返回相关的执行用户列表。
     *
     * @param model 用于在视图中传递数据的模型对象，此方法中未使用
     * @param vo 包含任务ID和流程ID的JSONObject对象，通过RequestBody接收前端发送的数据
     * @param request 用户的请求对象，此方法中未使用
     * @return WrappedResult 包装了执行用户列表的结果对象，成功时列表包含执行用户的信息，失败时包含错误信息
     * @throws Exception 处理过程中可能抛出的异常
     */
    @RequestMapping(value = "/preExcutors", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult preExcutors(Model model, @RequestBody JSONObject vo, HttpServletRequest request)
            throws Exception {
        // 从请求中获取任务ID和流程ID
        Long taskId= longValue(vo, "taskId");
        Map<String, Object> workflow = bpmi.getFlowi(longValue(vo, "flowId"));
        // 根据流程ID和任务ID获取任务执行信息
        List<Map<String, Object>> taskExcute = bpmi.getTaskExcute(id(workflow), taskId);
        // 根据任务ID获取任务详细信息
        Map<String, Object>  task = bpmi.getWfTaskByID(taskId);

        List<Map<String, Object>> taskUsers=   new ArrayList<>();

        if (taskExcute == null ||taskExcute.isEmpty()) {
            // 如果没有指定的执行用户，尝试根据任务角色获取执行用户列表
            taskUsers = new ArrayList<>();
            Long roleId = longValue(task, "roleID");
            // 构造查询角色用户的Cypher查询语句
            String roleUsers = "MATCH(n:Role)<-[r:HAS_ROLE]-(u:User) where id(n)=" + roleId + " return u.name as username,id(u) AS userId";
            List<Map<String, Object>> roleUserList = crudService.cypher(roleUsers);
            taskUsers.addAll(roleUserList);
        }else{
            // 如果有指定的执行用户，遍历执行信息列表，构造执行用户列表
            for(Map<String, Object> ti: taskExcute){
                String executorName = crudService.getValueOfFieldObject(ti, "executorID", "name");
                String executorId = crudService.getValueOfFieldObject(ti, "executorID", "id");
                Map<String, Object> taskUser = new HashMap<>();

                taskUser.put("userName", executorName);
                taskUser.put("userId", executorId);
                taskUsers.add(taskUser);
            }
        }
        // 返回执行用户列表
        return ResultWrapper.success(taskUsers);
    }


    private void taskExecutorInfo(Model model, Map<String, Object> param) {
        long taskComeDatetime=0;
        List<Map<String, Object>> executorsOfTask = crudService.queryBy(param, "BpmTaskExecute");
        if (!executorsOfTask.isEmpty()) {
            Map<String, Object> currExecutor = null;
            Long currentUserId = adminService.getCurrentUserId();
            for (Map<String, Object> ei : executorsOfTask) {

                Long longValue = MapTool.longValue(ei, "executorID");
                if (longValue.equals(currentUserId)) {
                    currExecutor = ei;
                    break;
                }
            }
            if (currExecutor != null) {
                model.addAttribute("currentExecutor", currExecutor);
                taskComeDatetime = BpmTaskExecute.getTaskComeDatetime(currExecutor);
            }
        }

        // 将任务到达时间添加到模型
        model.addAttribute("taskComeDatetime", MapTool.dateStr(taskComeDatetime));
    }

    /**
     * 处理不赞同操作的请求。
     * 对接收到的请求进行处理，模拟不赞同（反对）某个工作流任务的操作。
     * 该方法同时处理GET和POST请求。
     *
     * @param model 用于在视图和控制器之间传递数据的模型对象。
     * @param vo 包含请求数据的JSONObject对象，预期包含工作流操作所需的数据。
     * @param request 用户的请求对象，用于获取请求相关信息。
     * @return 返回一个封装了操作结果的WrappedResult对象。成功时，提供成功的标志和消息；失败时，提供失败的标志和错误消息。
     * @throws Exception 处理过程中可能抛出的异常。
     */
    @RequestMapping(value = "/disagree", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult disagree(Model model, @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
        //尝试执行不赞同操作
	try {
	    //获取当前操作用户的ID
	    Long currentUserId = adminService.getCurrentPasswordId();

        String executorIDs= string(vo,"executorIDs");

        if(StringUtils.isBlank(executorIDs)) {
            return ResultWrapper.wrapResult(false, null, null, "请选择执行人！");
        }
        if(executorIDs.contains("[")&&executorIDs.contains("]")){
            vo.put("executorIDs",executorIDs.replaceAll("\\[","").replaceAll("]",""));
        }

	    //执行工作流不赞同操作
        Boolean operateFlow =  workflowOperateAssist.operateFlow(vo, currentUserId, WFEConstants.WFDECISION_DISAGREE);
        //操作成功，设置标志并返回成功消息
        model.addAttribute("executeFlag", operateFlow);
        if(operateFlow) {
            return ResultWrapper.wrapResult(operateFlow, vo, null, "提交成功！");
        }else {
            return ResultWrapper.wrapResult(operateFlow, vo, null, "提交失败！");
        }
	} catch (Exception ex) {
	    //操作失败，记录错误日志，并返回失败消息
	    LoggerTool.error(logger,"执行不赞同失败:", ex);
	    model.addAttribute("executeFlag", Boolean.valueOf(false));
	    return ResultWrapper.failed("提交失败！");

	}
    }

}

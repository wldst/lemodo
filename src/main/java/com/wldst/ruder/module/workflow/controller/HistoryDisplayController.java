package com.wldst.ruder.module.workflow.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.beans.BpmTask;
import com.wldst.ruder.module.workflow.beans.BpmTaskExecute;
import com.wldst.ruder.module.workflow.beans.SimpleTask;
import com.wldst.ruder.module.workflow.biz.BpmGraphService;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程履历信息显示画面控制器
 * 
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm/history")
public class HistoryDisplayController extends BpmDo{
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(HistoryDisplayController.class);
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService neo4jService;
    // 流程模板数据库操作
    private BpmInstanceManagerService bizWfInstanceManager;
    @Autowired
    private BpmInstance bpmi;

    public WrappedResult init(@PathVariable("po") String poLabel, @RequestBody JSONObject vo,
	    HttpServletRequest request, Model model) throws Exception {
	Long bizDataID = longValue(vo, "bizDataID");
	String bizTabName = string(vo, "bizTabName");
	String templateMark = string(vo, "flowId");

	try {
	    Map<String, Object> workflow = bpmi.getFlowiByBizId(bizDataID);
	    model.addAttribute("workflow", workflow);
	    return ResultWrapper.success(workflow);
	} catch (Exception ex) {
	    LoggerTool.error(logger,"初始化提交页面失败:", ex);
	    throw new Exception("初始化提交页面失败:", ex);
	}

    }

    /**
     * 初始化执行流程的控制器方法。
     *
     * @param flowid 流程ID，用于确定要初始化的执行流程。
     * @param request HttpServletRequest对象，用于接收客户端请求。
     * @param model Model对象，用于在控制器和视图之间传递数据。
     * @return 返回视图名称，此处为"workflow/executeList"。
     * @throws Exception 抛出异常，捕获初始化过程中的任何异常。
     */
    @RequestMapping(value = "/initExecute/{flowid}", method = { RequestMethod.GET, RequestMethod.POST })
    public String initExecute(@PathVariable("flowid") String flowid, HttpServletRequest request, Model model)
        throws Exception {

        // 获取当前密码ID
        Long currentPasswordId = adminService.getCurrentPasswordId();

        // 初始化各种标志位和名称
        boolean performFlag = false;
        boolean callbackFlag = false;
        boolean turnbackFlag = false;
        boolean forwardFlag = false;
        boolean reloopFlag = false;
        boolean agreeFlag = false;
        boolean disagreeFlag = false;

        String performName = null;
        String callbackName = null;
        String turnbackName = null;
        String forwardName = null;
        String reloopName = null;
        String agreeName = null;
        String disagreeName = null;
        Map<String, Object> currExecutor = null;
        long taskComeDatetime = 0;
        try {
            // 将flowid转换为Long类型
            Long wfId = Long.valueOf(flowid);
            // 获取指定ID的流程信息
            Map<String, Object> workflow = bpmi.getFlowi(wfId);
            if(longValue(workflow,"bizDataId")==null) {
        	workflow =bpmi.getFlowiByBizId(Long.valueOf(wfId));
	        }

            if (workflow != null) {
                model.addAttribute("workflow", workflow);
                // 获取当前节点信息
                Map<String, Object> currentTask = bpmi.getNowNode(workflow);
                boolean currentExistFlag = false;
                boolean preExistFlag = false;

                if (currentTask != null) {
                    // 查询当前任务的执行人信息
                    Map<String, Object> param = new HashMap<>();
                    param.put("instanceID", wfId);
                    param.put("taskID", id(currentTask));
                    List<Map<String, Object>> executors = neo4jService.queryBy(param, "BpmTaskExecute");

                    if (!executors.isEmpty()) {
                        Long currentUserId = adminService.getCurrentUserId();
                        for (Map<String, Object> ei : executors) {
                            Long longValue = longValue(ei, "executorID");
                            if (longValue.equals(currentUserId)) {
                                currentExistFlag = true;
                                currExecutor = ei;
                                // 处理执行人名称和状态
                                String executorName = neo4jService.getValueOfFieldObject(currExecutor, "executorID", "name");
                                String executorStatusName = WFEConstants
                                        .convertUserExecStateZh(integer(currExecutor, "executorStatus"));
                                currExecutor.put("executorName",executorName);
                                currExecutor.put("executorStatusName", executorStatusName);
                                currentTask.put("executorName",executorName);
                                currentTask.put("executorStatusName", executorStatusName);
                                model.addAttribute("currentExecutor", currExecutor);
                                taskComeDatetime = BpmTaskExecute.getTaskComeDatetime(currExecutor);
                                break;
                            }
                        }

                        if(!currentExistFlag) {
                            // 处理无当前执行人的逻辑
                            List<String> exNames = new ArrayList<>();
                            List<String> exStatuss = new ArrayList<>();
                            for (Map<String, Object> ei : executors) {
                                String executorName = neo4jService.getValueOfFieldObject(ei, "executorID", "name");
                                String executorStatusName = WFEConstants
                                        .convertUserExecStateZh(integer(ei, "executorStatus"));
                                exNames.add(executorName);
                                exStatuss.add(executorStatusName);
                                break;
                            }
                            currentTask.put("executorName",String.join(",",exNames));
                            currentTask.put("executorStatusName", String.join(",",exStatuss));
                        }
                    }

                    // 当前任务存在且状态为等待或准备时，处理按钮显示逻辑
                    if (currentExistFlag && (BpmTask.getTaskStatus(currentTask) == WFEConstants.WFTASK_STATUS_WAIT
                            || BpmTask.getTaskStatus(currentTask) == WFEConstants.WFTASK_STATUS_READY)) {
                        // 处理同意、打回等按钮逻辑
                        String btnName = string(currentTask, "btnName");
                        String canReverse = string(currentTask, "canReverse");
                        String canBackIn = string(currentTask, "canBackIn");
                        String canSelectBackToNode = string(currentTask, "canSelectBackToNode");

                        agreeName = btnName;
                        agreeFlag = true;

                        if ("Y".equals(canReverse)) {
                            // 处理可以反向的逻辑
                        }

                        if ("Y".equals(canBackIn)) {
                            turnbackFlag = true;
                            turnbackName = "打回";
                        }
                    }
                    // 处理前一个任务的执行情况，判断是否可以回调
                    Map<String, Object> preSimpleTask = bpmi.getPreviewWfTask(BpmDo.id(currentTask));
                    if (null != preSimpleTask) {
                        Map<String, Object> preExecutor = SimpleTask.existExecuteUser(currentPasswordId,
                                WFEConstants.WF_EXEC_USERSTATE_NONE, preSimpleTask);
                        if (preExecutor != null) {
                            preExistFlag = true;
                        }

                        if (preExistFlag) {
                            // 处理回调逻辑
                            List<Map<String, Object>> tmpDecisionList = SimpleTask.getTaskDecisionList(preSimpleTask);
                            if (null != tmpDecisionList) {
                                for (int k = 0; k < tmpDecisionList.size(); k++) {
                                    Map<String, Object> tmp = tmpDecisionList.get(k);
                                    String decisionName = string(tmp, "decisionName");
                                    if (null != decisionName
                                            && WFEConstants.WFDECISION_CALLBACK.equalsIgnoreCase(decisionName)) {
                                        callbackFlag = true;
                                        callbackName = decisionName;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    currentTask.put("taskStatusName",
                            WFEConstants.convertWfStatusZh(integer(currentTask, "taskStatus")));
                    model.addAttribute("currentTask", currentTask);
                }
            }

            // 添加各种标志位和名称到model中，用于视图显示
            model.addAttribute("performFlag", Boolean.valueOf(performFlag));
            model.addAttribute("callbackFlag", Boolean.valueOf(callbackFlag));
            model.addAttribute("turnbackFlag", Boolean.valueOf(turnbackFlag));
            model.addAttribute("forwardFlag", Boolean.valueOf(forwardFlag));
            model.addAttribute("reloopFlag", Boolean.valueOf(reloopFlag));
            model.addAttribute("agreeFlag", Boolean.valueOf(agreeFlag));
            model.addAttribute("disagreeFlag", Boolean.valueOf(disagreeFlag));
            model.addAttribute("performName", performName);
            model.addAttribute("callbackName", callbackName);
            model.addAttribute("turnbackName", turnbackName);
            model.addAttribute("forwardName", forwardName);
            model.addAttribute("reloopName", reloopName);
            model.addAttribute("agreeName", agreeName);
            model.addAttribute("disagreeName", disagreeName);
            model.addAttribute("workflow", workflow);
            model.addAttribute("taskComeDatetime", DateUtil.dateTimeString(taskComeDatetime));
            model.addAttribute("currEmpID", currentPasswordId);

            // 查询并添加流程历史信息到model中
            Map<String, Object> param = new HashMap<>();
            param.put("instanceID", wfId);
            String cypher = ""
                    + " Match (wf:BpmGraphInstance)-->(h:BpmHistory)-->(t:BpmNode),(h)-->(u:User) where id(wf)= " + wfId
                    + " return h,u.name AS empName,t.title AS taskName ";
            List<Map<String, Object>> history = neo4jService.cypher(cypher);
            if (history != null) {
                for (Map<String, Object> hi : history) {
                    toDateStrValue(hi, "createTime");
                }
            }

            model.addAttribute("historyList", history);

        } catch (Exception ex) {
            // 记录日志并抛出异常
            LoggerTool.error(logger,"初始化提交页面失败:", ex);
            throw new Exception("初始化提交页面失败:", ex);
        }
        return "workflow/executeList";
    }

    /**
     *  流程数据，以及履历数据
     * @param flowid
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/data/{flowid}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult historyData(@PathVariable("flowid") String flowid, HttpServletRequest request)
            throws Exception {
        Map<String, Object> result = new HashMap<>();
        // 获取当前密码ID
        Long currentPasswordId = adminService.getCurrentPasswordId();

        // 初始化各种标志位和名称
        boolean performFlag = false;
        boolean callbackFlag = false;
        boolean turnbackFlag = false;
        boolean forwardFlag = false;
        boolean reloopFlag = false;
        boolean agreeFlag = false;
        boolean disagreeFlag = false;

        String performName = null;
        String callbackName = null;
        String turnbackName = null;
        String forwardName = null;
        String reloopName = null;
        String agreeName = null;
        String disagreeName = null;
        Map<String, Object> currExecutor = null;
        long taskComeDatetime = 0;
        try {
            // 将flowid转换为Long类型
            Long wfId = Long.valueOf(flowid);
            // 获取指定ID的流程信息
            Map<String, Object> workflow = bpmi.getFlowi(wfId);
            if(longValue(workflow,"bizDataId")==null) {
                workflow =bpmi.getFlowiByBizId(Long.valueOf(wfId));
            }

            if (workflow != null) {
                result.put("workflow", workflow);
                // 获取当前节点信息
                Map<String, Object> currentTask = bpmi.getNowNode(workflow);
                boolean currentExistFlag = false;
                boolean preExistFlag = false;

                if (currentTask != null) {
                    // 查询当前任务的执行人信息
                    Map<String, Object> param = new HashMap<>();
                    param.put("instanceID", wfId);
                    param.put("taskID", id(currentTask));
                    List<Map<String, Object>> executors = neo4jService.queryBy(param, "BpmTaskExecute");

                    if (!executors.isEmpty()) {
                        Long currentUserId = adminService.getCurrentUserId();
                        for (Map<String, Object> ei : executors) {
                            Long longValue = longValue(ei, "executorID");
                            if (longValue.equals(currentUserId)) {
                                currentExistFlag = true;
                                currExecutor = ei;
                                // 处理执行人名称和状态
                                String executorName = neo4jService.getValueOfFieldObject(currExecutor, "executorID", "name");
                                String executorStatusName = WFEConstants
                                        .convertUserExecStateZh(integer(currExecutor, "executorStatus"));
                                currExecutor.put("executorName",executorName);
                                currExecutor.put("executorStatusName", executorStatusName);
                                currentTask.put("executorName",executorName);
                                currentTask.put("executorStatusName", executorStatusName);
                                result.put("currentExecutor", currExecutor);
                                taskComeDatetime = BpmTaskExecute.getTaskComeDatetime(currExecutor);
                                break;
                            }
                        }

                        if(!currentExistFlag) {
                            // 处理无当前执行人的逻辑
                            List<String> exNames = new ArrayList<>();
                            List<String> exStatuss = new ArrayList<>();
                            for (Map<String, Object> ei : executors) {
                                String executorName = neo4jService.getValueOfFieldObject(ei, "executorID", "name");
                                String executorStatusName = WFEConstants
                                        .convertUserExecStateZh(integer(ei, "executorStatus"));
                                exNames.add(executorName);
                                exStatuss.add(executorStatusName);
                                break;
                            }
                            currentTask.put("executorName",String.join(",",exNames));
                            currentTask.put("executorStatusName", String.join(",",exStatuss));
                        }
                    }

                    // 当前任务存在且状态为等待或准备时，处理按钮显示逻辑
                    if (currentExistFlag && (BpmTask.getTaskStatus(currentTask) == WFEConstants.WFTASK_STATUS_WAIT
                            || BpmTask.getTaskStatus(currentTask) == WFEConstants.WFTASK_STATUS_READY)) {
                        // 处理同意、打回等按钮逻辑
                        String btnName = string(currentTask, "btnName");
                        String canReverse = string(currentTask, "canReverse");
                        String canBackIn = string(currentTask, "canBackIn");
                        String canSelectBackToNode = string(currentTask, "canSelectBackToNode");

                        agreeName = btnName;
                        agreeFlag = true;

                        if ("Y".equals(canReverse)) {
                            // 处理可以反向的逻辑
                        }

                        if ("Y".equals(canBackIn)) {
                            turnbackFlag = true;
                            turnbackName = "打回";
                        }
                    }
                    // 处理前一个任务的执行情况，判断是否可以回调
                    Map<String, Object> preSimpleTask = bpmi.getPreviewWfTask(BpmDo.id(currentTask));
                    if (null != preSimpleTask) {
                        Map<String, Object> preExecutor = SimpleTask.existExecuteUser(currentPasswordId,
                                WFEConstants.WF_EXEC_USERSTATE_NONE, preSimpleTask);
                        if (preExecutor != null) {
                            preExistFlag = true;
                        }

                        if (preExistFlag) {
                            // 处理回调逻辑
                            List<Map<String, Object>> tmpDecisionList = SimpleTask.getTaskDecisionList(preSimpleTask);
                            if (null != tmpDecisionList) {
                                for (int k = 0; k < tmpDecisionList.size(); k++) {
                                    Map<String, Object> tmp = tmpDecisionList.get(k);
                                    String decisionName = string(tmp, "decisionName");
                                    if (null != decisionName
                                            && WFEConstants.WFDECISION_CALLBACK.equalsIgnoreCase(decisionName)) {
                                        callbackFlag = true;
                                        callbackName = decisionName;
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    currentTask.put("taskStatusName",
                            WFEConstants.convertWfStatusZh(integer(currentTask, "taskStatus")));
                    result.put("currentTask", currentTask);
                }
            }

            // 添加各种标志位和名称到model中，用于视图显示
            result.put("performFlag", Boolean.valueOf(performFlag));
            result.put("callbackFlag", Boolean.valueOf(callbackFlag));
            result.put("turnbackFlag", Boolean.valueOf(turnbackFlag));
            result.put("forwardFlag", Boolean.valueOf(forwardFlag));
            result.put("reloopFlag", Boolean.valueOf(reloopFlag));
            result.put("agreeFlag", Boolean.valueOf(agreeFlag));
            result.put("disagreeFlag", Boolean.valueOf(disagreeFlag));
            result.put("performName", performName);
            result.put("callbackName", callbackName);
            result.put("turnbackName", turnbackName);
            result.put("forwardName", forwardName);
            result.put("reloopName", reloopName);
            result.put("agreeName", agreeName);
            result.put("disagreeName", disagreeName);
            result.put("workflow", workflow);
            result.put("taskComeDatetime", DateUtil.dateTimeString(taskComeDatetime));
            result.put("currEmpID", currentPasswordId);

            // 查询并添加流程历史信息到model中
            Map<String, Object> param = new HashMap<>();
            param.put("instanceID", wfId);
            String cypher = ""
                    + " Match (wf:BpmGraphInstance)-->(h:BpmHistory)-->(t:BpmNode),(h)-->(u:User) where id(wf)= " + wfId
                    + " return h,u.name AS empName,t.title AS taskName ";
            List<Map<String, Object>> history = neo4jService.cypher(cypher);
            if (history != null) {
                for (Map<String, Object> hi : history) {
                    toDateStrValue(hi, "createTime");
                }
            }

            result.put("historyList", history);
        } catch (Exception ex) {
            // 记录日志并抛出异常
            LoggerTool.error(logger,"初始化提交页面失败:", ex);
            throw new Exception("初始化提交页面失败:", ex);
        }
        return ResultWrapper.success(result);
    }

    /**
     * 得到当前任务节点信息
     *
     * @return
     * @throws Exception
     */
    public WrappedResult getCurrentTask(@PathVariable("po") String poLabel, @RequestBody JSONObject vo,
	    HttpServletRequest request, Model model) throws Exception {

	String bizTabName = string(vo, "bizTabName");
	String templateMark = string(vo, "templateMark");
        Long bizDataID = longValue(vo, "bizDataID");
	try {
        Map<String, Object> workflow = bpmi.getFlowiByBizId(bizDataID);
	    Map<String, Object> currentTask = null;
	    if (workflow != null) {
		currentTask = bpmi.getNowNode(workflow);
	    }
	    return ResultWrapper.success(currentTask);
	} catch (Exception ex) {
	    LoggerTool.error(logger,"初始化提交页面失败:", ex);
	    throw new Exception("初始化提交页面失败:", ex);
	}
    }

    /**
     * 得到历史任务信息
     *
     * @throws Exception
     */
    public WrappedResult getHistoryTasks(@PathVariable("po") String poLabel, @RequestBody JSONObject vo,
                                         HttpServletRequest request, Model model) throws Exception {
        String bizTabName = string(vo, "bizTabName");
        String templateMark = string(vo, "templateMark");
        List<Map<String, Object>> historyTasks = new ArrayList<>();
        Long bizDataID = longValue(vo, "bizDataID");
        try {
            Map<String, Object> workflow = bpmi.getFlowiByBizId(bizDataID);
            if (workflow != null) {
                historyTasks = neo4jService.listAttMapBy("instanceID",id(workflow),"BpmHistory");
            }
            return ResultWrapper.success(historyTasks);
        } catch (Exception ex) {
            LoggerTool.error(logger,"初始化提交页面失败:", ex);
            throw new Exception("初始化提交页面失败:", ex);
        }
    }

}

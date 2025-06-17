package com.wldst.ruder.module.workflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.beans.BpmTaskExecute;
import com.wldst.ruder.module.workflow.beans.Decision;
import com.wldst.ruder.module.workflow.beans.SimpleTask;
import com.wldst.ruder.module.workflow.beans.WfPc;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.biz.BpmOperateAssist;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程任务撤回控制器，负责处理流程任务的撤回操作回调。
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm/callback")
public class OperateCallbackController extends BpmDo{
    // 日志记录器
    private static Logger logger = LoggerFactory.getLogger(OperateCallbackController.class);
    @Autowired
    private UserAdminService adminService;
    // 流程任务执行内部画面URI
    private String opInnerPageUri;

    private BpmInstanceManagerService bizWfInstanceManager;

    private BpmOperateAssist workflowOperateAssist;
    @Autowired
    private BpmInstance bpmi;

    /**
     * 撤回流程任务的回调接口。
     *
     * @param poLabel 请求中的流程订单标签。
     * @param vo 请求体中的JSON对象，包含业务数据ID、业务标签名和模板标记。
     * @param request 用户的HTTP请求对象。
     * @param model Spring模型对象，用于在处理中传递数据。
     * @return 返回处理结果，成功则返回成功消息，失败则返回错误消息。
     * @throws Exception 处理过程中可能抛出的异常。
     */
    public WrappedResult callback(@PathVariable("po") String poLabel, @RequestBody JSONObject vo,
	    HttpServletRequest request, Model model) throws Exception {
	String bizDataID = string(vo, "bizDataID");
	String bizTabName = string(vo, "bizTabName");
	String templateMark = string(vo, "templateMark");

	Long currentUserId = adminService.getCurrentPasswordId();
	boolean flag = false;
	String msg = null;

	boolean canCallbackFlag = true;
	boolean canCallbackFlag2 = false;
	try {

	    // 获取当前流程实例信息
		Map<String, Object> workflow = bpmi.getFlowi(longValue(vo, "flowId"));

	    if (workflow != null) {
		// 获取当前任务信息
		Map<String, Object> currentTask = bpmi.getNowNode(workflow);
		Map<String, Object> previewTask = null;
		if (currentTask != null) {
		    previewTask = bpmi.getPreviewWfTask(BpmInstance.id(currentTask));
		    // 检查当前任务是否有已经被执行的执行人
		    List<Map<String, Object>> executorList = SimpleTask.getTaskExecutorList(currentTask);
		    if (executorList != null && executorList.size() > 0) {
			int listSize = executorList.size();
			for (int i = 0; i < listSize; i++) {
			    Map<String, Object> executor = executorList.get(i);
			    if (integer(executor, "executorStatus") == WF_EXEC_USERSTATE_EXECED) {
				canCallbackFlag = false;
				break;
			    }
			}
		    }
		    if (!canCallbackFlag) {
			msg = "后续流程任务已经被接受或执行，不能进行撤回";
		    } else {
			// 检查前置任务中是否有当前用户可以撤回的任务
			List<Map<String, Object>> previewExecuteList = SimpleTask.getTaskExecutorList(previewTask);
			if (previewExecuteList != null && previewExecuteList.size() > 0) {
			    int listSize = previewExecuteList.size();
			    for (Map<String, Object> executor : previewExecuteList) {
				if (BpmTaskExecute.getExecutorID(executor) == currentUserId && BpmTaskExecute
					.getExecutorStatus(executor) == WF_EXEC_USERSTATE_EXECED) {
				    canCallbackFlag2 = true;
				    break;
				}
			    }
			}
			if (canCallbackFlag2) {
			    // 执行撤回操作
			    Map<String, Object> wfRuntimeData = new HashMap<>();
			    WfPc.setDecisionKey(WFDECISION_CALLBACK, wfRuntimeData);
			    WfPc.setTaskExecuteConfirm("", wfRuntimeData);
			    WfPc.setWorkflowID(BpmInstance.id(workflow), wfRuntimeData);
			    if (currentTask != null) {
				WfPc.setNextTaskID(BpmInstance.id(currentTask), wfRuntimeData);
			    }
			    Map<String, Object> decision = SimpleTask
				    .getWfTaskDecision(WFDECISION_CALLBACK, previewTask);
			    String decisionViewName = Decision.getViewName(decision);
			    if (decision != null && decisionViewName != null && decisionViewName != null
				    && decisionViewName.trim().length() > 0) {

				WfPc.setDecisionName(decisionViewName, wfRuntimeData);
			    } else {
				WfPc.setDecisionName(
					WFEConstants.convertWfDecisionNameZh(WFDECISION_CALLBACK),
					wfRuntimeData);
			    }
			    WfPc.setCurrentExecutorID(currentUserId, wfRuntimeData);
			    WfPc.setCurrentExecutorName(adminService.getCurrentAccount(), wfRuntimeData);
			    workflowOperateAssist.flowAction(workflow, wfRuntimeData);
			    flag = true;
			    msg = "撤回流程任务成功！";
			} else {
			    msg = "当前用户为非任务执行人,不能撤回！";
			}
		    }
		}
	    }
	    model.addAttribute("executeFlag", Boolean.valueOf(true));
	    return ResultWrapper.success(msg);
	    // this.addMessage(request, msg);
	} catch (Exception ex) {
	    model.addAttribute("executeFlag", Boolean.valueOf(false));
	    LoggerTool.error(logger,"执行撤回决策失败:", ex);
	    return ResultWrapper.failed("任务撤回失败！");

	}

    }
}

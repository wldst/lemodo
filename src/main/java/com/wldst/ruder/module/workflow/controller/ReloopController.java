package com.wldst.ruder.module.workflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.workflow.beans.BpmTaskExecute;
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
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.biz.BpmOperateAssist;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 流程跳转操作画面控制器
 * 
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm/reloop")
public class ReloopController {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(ReloopController.class);

    // 流程操作跳转页面URI
    private String wfOPReloopPageUri;

    // 流程跳转内部执行画面URI
    private String wfOPReloopInnerPageUri;

    private BpmInstanceManagerService bizWfInstanceManager;

    private BpmOperateAssist workflowOperateAssist;
	@Autowired
	private BpmInstance bpmi;
	@Autowired
	private UserAdminService adminService;
	@Autowired
	private CrudNeo4jService crudService;
	@RequestMapping(value = "/init/{flowId}", method = {RequestMethod.GET, RequestMethod.POST})
	public String init(Model model, @PathVariable("flowId") String flowId,
					   HttpServletRequest request) throws Exception {
	try {
		Map<String, Object> workflow = bpmi.getFlowi(Long.valueOf(flowId));
//	    Map<String, Object> reloopTask = BpmInstance.getReloopTask(BpmInstance.taskInnerId(currentTask),workflow);
//
//	    if (reloopTask == null) {
//		throw new Exception("当前任务非循环任务,不能进行跳转");
//	    }
//	    model.addAttribute("reloopTask", reloopTask);
		// 获取当前任务信息
		Map<String, Object> currentTask = bpmi.getNowNode(workflow);

		Map<String, Object> param = new HashMap<>();
		param.put("instanceID", Long.valueOf(flowId));
		param.put("taskID", MapTool.id(currentTask));
		taskExecutorInfo(model, param);

	    model.addAttribute("currEmpID", adminService.getCurrentUserId());
	} catch (Exception ex) {
	    LoggerTool.error(logger,"初始化流程操作跳转页面失败:", ex);
	    throw new Exception("初始化流程操作跳转页面失败:", ex);
	}
		return "workflow/wfdecision-agree";
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

    public WrappedResult reloop(@PathVariable("po") String poLabel, @RequestBody JSONObject vo,
	    HttpServletRequest request, Model model) throws Exception {
	// String currEmpID = MapTool.string(vo, "currEmpID");
	Long currentUserId = adminService.getCurrentPasswordId();
	/*
	if(currEmpID != null && !currEmpID.equals(currentUserId))
	{
		model.addAttribute( "executeFlag", new Boolean(false));
		return ResultWrapper.failed("任务提交失败！");
		return ;
	}
	*/
	try {

	    workflowOperateAssist.reloop(vo, currentUserId, WFEConstants.WFDECISION_RELOOP);

	    model.addAttribute("executeFlag", Boolean.valueOf(true));
	    return ResultWrapper.success("任务提交成功！");
	} catch (Exception ex) {
	    model.addAttribute("executeFlag", Boolean.valueOf(false));
	    LoggerTool.error(logger,"执行同意决策失败:", ex);
	    return ResultWrapper.failed("任务提交失败！");
	    
	}
    }

}

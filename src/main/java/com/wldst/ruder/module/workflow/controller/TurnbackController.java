package com.wldst.ruder.module.workflow.controller;

import java.util.Calendar;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.biz.BpmOperateAssist;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.formula.BpmExecutorFormulaParse;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程操作之打回
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm/turnback")
public class TurnbackController extends BpmDo {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(TurnbackController.class);
    @Autowired
    private BpmOperateAssist workflowOperateAssist;
    @Autowired
    private BpmInstance bpmi;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService crudService;
    // 打回页面URI
    private String turnbackPageUri;

    // 打回内部执行画面URI
    private String turnbackInnerPageUri;

    private BpmInstanceManagerService bizWfInstanceManager;

    @RequestMapping(value = "/init/{flowId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String init(@PathVariable("flowId") String flowId, HttpServletRequest request, Model model)
            throws Exception {
        BpmExecutorFormulaParse parse = null;
        try {
            Map<String, Object> workflow = bpmi.getFlowi(Long.valueOf(flowId));
            if (longValue(workflow, "bizDataId") == null) {
                workflow = bpmi.getFlowiByBizId(Long.valueOf(flowId));
            }
            Map<String, Object> currentTask = bpmi.getNowNode(workflow);
            // Map<String, Object> nextTask = bpmi.findNextNormalNode(workflow);

            model.addAttribute("currentTask", currentTask);
            model.addAttribute("workflow", workflow);
            // model.addAttribute("nextTask", nextTask);

            List<Map<String, Object>> completedTask = bpmi.getCompletedNormalNode(workflow);

            model.addAttribute("completedTask", completedTask);
            model.addAttribute("taskComeDatetime", dateStr(Calendar.getInstance().getTimeInMillis()));
        } catch (Exception ex) {
            LoggerTool.error(logger, "初始化打回页面失败:", ex);
            throw new Exception("初始化打回页面失败:", ex);
        }
        return "workflow/wfdecision-turnback";
    }

    @RequestMapping(value = "/completedTask", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public WrappedResult completedTask(@RequestBody JSONObject vo, HttpServletRequest request, Model model)
            throws Exception {
        Long flowId = longValue(vo, "flowId");
        BpmExecutorFormulaParse parse = null;
        try {
            Map<String, Object> workflow = bpmi.getFlowi(flowId);
            Map<String, Object> currentTask = bpmi.getNowNode(workflow);
            model.addAttribute("workflow", workflow);
            List<Map<String, Object>> completedTask = bpmi.getCompletedNormalNode(workflow);
            return ResultWrapper.success(completedTask);
        } catch (Exception ex) {
            return ResultWrapper.failed("获取打回节点数据失败");
        }
    }

    @RequestMapping(value = "/completedTaskById/{flowId}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public WrappedResult completedTask2(@PathVariable("flowId") String flowId, HttpServletRequest request, Model model)
            throws Exception {
        BpmExecutorFormulaParse parse = null;
        try {
            Map<String, Object> workflow = bpmi.getFlowi(Long.valueOf(flowId));
            if (longValue(workflow, "bizDataId") == null) {
                workflow = bpmi.getFlowiByBizId(Long.valueOf(flowId));
            }
            Map<String, Object> currentTask = bpmi.getNowNode(workflow);

            model.addAttribute("workflow", workflow);
            // model.addAttribute("nextTask", nextTask);

            List<Map<String, Object>> completedTask = bpmi.getCompletedNormalNode(workflow);
            return ResultWrapper.success(completedTask);
        } catch (Exception ex) {
            return ResultWrapper.failed("获取打回节点数据失败");
        }
    }

    @RequestMapping(value = "/turnback", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public WrappedResult turnback(@RequestBody JSONObject vo,
                                  HttpServletRequest request, Model model) throws Exception {

        Long currentUserId = adminService.getCurrentUserId();

        try {
            //打回的节点，有执行人。打回节点，执行人还是原来的。
            workflowOperateAssist.turnback(vo, currentUserId, WFDECISION_TURNBACK);

            model.addAttribute("executeFlag", Boolean.valueOf(true));
            return ResultWrapper.success("任务提交成功！");
        } catch (Exception ex) {
            LoggerTool.error(logger, "初始化打回页面失败:", ex);
            String msg = ex.getMessage();
            model.addAttribute("executeFlag", Boolean.valueOf(false));
            return ResultWrapper.error("初始化打回页面失败:" + msg);
        }
    }

    /**
     * 取回单据
     *
     * @param vo
     * @param request
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/reverse", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult reverse(@RequestBody JSONObject vo,
                                 HttpServletRequest request, Model model) throws Exception {
        Long currentUserId = adminService.getCurrentUserId();
        Map<String, Object> workflow = null;
        if (id(vo) != null) {
            workflow = bpmi.getFlowiByBizId(Long.valueOf(id(vo)));
        } else {
            Long flowId = longValue(vo, "flowId");
            workflow = bpmi.getFlowi(flowId);
            if (longValue(workflow, "bizDataId") == null) {
                workflow = bpmi.getFlowiByBizId(Long.valueOf(flowId));
            }
        }

        try {
            if (wfStatus(workflow) != WFSTATUS_RUN) {
                return ResultWrapper.error("流程实例不为运行状态,不能取回");
            }
            workflowOperateAssist.reverse(workflow, currentUserId);
            return ResultWrapper.success("取回成功！");
        } catch (Exception ex) {
            LoggerTool.error(logger, "取回失败:", ex);
            String msg = ex.getMessage();
            return ResultWrapper.error("取回失败:" + msg);
        }
    }

}

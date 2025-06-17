package com.wldst.ruder.module.workflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.*;
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
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程任务提交页面控制器
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm/commit")
public class CommitController extends BpmDo {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(CommitController.class);
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private BpmInstance bpmi;
    @Autowired
    private CrudNeo4jService crudService;
    @Autowired
    private BpmOperateAssist workflowOperateAssist;


    /**
     * 初始化业务流程页面。
     * 根据提供的业务ID（bizId），查询相应的流程实例信息，并准备相关的数据，如当前任务、下一个任务、工作流状态等，供页面展示使用。
     *
     * @param bizId   业务ID，用于查询对应的流程实例。
     * @param request HttpServletRequest对象，用于接收HTTP请求。
     * @param model   Model对象，用于在控制器和视图之间传递数据。
     * @return 返回页面视图名称。
     * @throws Exception 抛出异常，当查询或处理过程中发生错误时。
     */
    @RequestMapping(value = "/init/{bizId}", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public String init(@PathVariable("bizId") String bizId, HttpServletRequest request, Model model) throws Exception {
        try {
            // 根据bizId查询流程实例信息。
            Map<String, Object> query = crudService.getOne("Match(m:BpmGraphInstance) where m.bizDataId=" + Long.valueOf(bizId) + " return id(m) AS id");
            if (query == null) {
                throw new Exception("初始化提交页面失败:未找到流程实例");
            }
            Long flowId = id(query);
            Map<String, Object> flowi = bpmi.getFlowi(flowId);
            if (longValue(flowi, "bizDataId") == null) {
                flowi = bpmi.getFlowiByBizId(Long.valueOf(flowId));
            }
            String wfStatus = string(flowi, "wfStatus");
            if (wfStatus == null || Integer.valueOf(wfStatus).equals(WFEConstants.WFSTATUS_INIT)) {
                flowi.put("wfStatus", WFEConstants.WFSTATUS_RUN);
            } else {
                throw new Exception("初始化提交页面失败:流程已提交");
            }

            //获取流程实例的所有任务信息。

            List<Map<String, Object>> nodeList = crudService.cypher("Match(m:BpmNode) where m.instanceID=" + flowId + " return m");
            for (Map<String, Object> ni : nodeList) {
                String taskType = nodeType(ni);
                if (WFEConstants.NODE_TYPE_START.equals(taskType)) {
                    flowi.put("startTask", ni);
                    break;
                }
            }
            // 查询下一个正常节点的信息。
            Map<String, Object> nextTask = bpmi.findNextNormalNode(flowi);
            if (nextTask == null) {
                throw new Exception("初始化提交页面失败:未找到下一节点");
            }
            // 查询下一个任务的执行用户列表。
            List<Map<String, Object>> taskUsers = listMapObject(nextTask, "nodeUserList");
            if (taskUsers == null || taskUsers.isEmpty()) {
                // 如果没有指定的节点用户，查询该节点关联的角色用户。
                taskUsers = new ArrayList<>();
                Long roleId = longValue(nextTask, "roleID");
                String roleUsers = "MATCH (n:Role)<--(u:User) where id(n)=" + roleId + " return u.id AS userId,u.name AS username";
                List<Map<String, Object>> roleUserList = crudService.cypher(roleUsers);
                taskUsers.addAll(roleUserList);
            }
            // 向模型添加任务用户列表。
            model.addAttribute("taskUsers", taskUsers);

            // 准备当前任务和下一个任务的信息，供页面显示。
            Map<String, Object> currentTask = MapTool.mapObject(flowi, "currentTask");
            nextTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
            Long nowTimeLong = DateTool.nowLong();
            model.addAttribute("nextTask", nextTask);
            model.addAttribute("currentTask", currentTask);
            model.addAttribute("workflow", flowi);
            model.addAttribute("taskComeDatetime", MapTool.dateStr(nowTimeLong));
        } catch (Exception ex) {
            // 记录初始化页面失败的错误。
            LoggerTool.error(logger, ex.getMessage(), ex);
            throw new Exception(ex.getMessage(), ex);
        }
        // 返回页面视图名称。
        return "workflow/wfdecision-commit";
    }


    /**
     * 提交流程实例的请求处理方法。
     *
     * @param vo      包含流程实例相关信息的JSONObject对象，如流程ID和执行评论。
     * @param request 用户的请求对象，用于获取请求信息。
     * @param model   用于在视图和控制器之间传递数据的Model对象。
     * @return 返回一个封装了操作结果的WrappedResult对象，包括操作是否成功和消息。
     * @throws Exception 抛出异常，处理流程提交中的错误情况。
     */
    @RequestMapping(value = "/commit", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult commit(@RequestBody JSONObject vo, HttpServletRequest request, Model model) throws Exception {
        try {
            // 获取当前用户ID
            Long currentUserId = adminService.getCurrentUserId();
            // 从请求中提取流程ID和执行评论
            String flowId = MapTool.string(vo, "flowId");
            String executeComment = MapTool.string(vo, "executeComment");
            String executorIDs = MapTool.string(vo, "executorIDs");
            bpmi.commit(flowId, executeComment, currentUserId, executorIDs);
            // 设置执行标志为成功
            model.addAttribute("executeFlag", Boolean.valueOf(true));
            // 返回成功响应
            return ResultWrapper.success("提交成功！");
        } catch (Exception ex) {
            // 设置执行标志为失败
            model.addAttribute("executeFlag", Boolean.valueOf(false));
            // 记录错误日志
            LoggerTool.error(logger, "执行同意决策失败:", ex);
            // 返回失败响应
            return ResultWrapper.failed("提交失败！");
        }
    }


}

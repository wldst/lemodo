package com.wldst.ruder.module.workflow.controller;

import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jDriver;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程监控画面控制器
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpm/monitor")
public class WorkflowMonitorController {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(WorkflowMonitorController.class);
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService crudService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private CrudNeo4jDriver driver;
    // 流程实例数据库操作

    private BpmInstanceManagerService bizWfInstanceManager;

    /**
     * 进入流程模板维护主界面
     */
    public void toWfInsList(@PathVariable("po") String poLabel, @RequestBody JSONObject vo, HttpServletRequest request,
                            Model model) {
    }

    public void initInstanceList(@PathVariable("po") String poLabel, @RequestBody JSONObject vo,
                                 HttpServletRequest request, Model model) throws Exception {

//	String wfTempMark = MapTool.string(vo, "wfTempMark");
//	String srcBizDataID = MapTool.string(vo, "srcBizDataID");
//	String srchWfStatus = MapTool.string(vo, "srchWfStatus");
//	String createEmployeeID = MapTool.string(vo, "createEmployeeID");
//	model.addAttribute("wfTempMark", wfTempMark);
//	model.addAttribute("srcBizDataID", srcBizDataID);
//	model.addAttribute("srchWfStatus", srchWfStatus);
//	model.addAttribute("createEmployeeID", createEmployeeID);
        ModelUtil.setKeyValue(model, vo);

        String label = "Rule";
        String[] columns;
        try {
            columns = crudUtil.getMdColumns(label);
            String query = Neo4jOptCypher.safeQueryObj(vo, label, columns);
            model.addAttribute("wfInstanceList", crudService.query(query, vo));
        } catch (DefineException e) {
            LoggerTool.error(logger, "loadRules", e);
        }
    }

    /**
     * 操作工作流实例。
     * 根据提供的选择码数组（selectCodeArray）和操作模式（mode），对相应的工作流实例进行启动、停止、暂停或挂起操作。
     *
     * @param poLabel 标签，用于后续实例列表的初始化，但在此函数中未直接使用。
     * @param vo      包含选择码和操作模式的JSON对象。
     * @param request HttpServletRequest对象，用于后续实例列表的初始化，但在此函数中未直接使用。
     * @param model   Model对象，用于后续实例列表的初始化，但在此函数中未直接使用。
     * @throws Exception 抛出异常，捕获并记录流程实例操作失败。
     */
    public void opWfInstance(@PathVariable("po") String poLabel, @RequestBody JSONObject vo, HttpServletRequest request,
                             Model model) throws Exception {
        long[] selectCodeArray = MapTool.splitLong(vo, "selectCode"); // 从请求体中解析出选择的工作流实例编码数组
        String mode = MapTool.string(vo, "mode"); // 从请求体中解析出操作模式

        try {

            // 如果存在选择的工作流实例编码
            if (selectCodeArray != null && selectCodeArray.length > 0) {
                for (int i = 0; i < selectCodeArray.length; i++) { // 遍历每个选择的工作流实例编码
                    Map<String, Object> wfInstance = bizWfInstanceManager.getBpmi(selectCodeArray[i]); // 根据编码获取工作流实例

                    // 如果找到对应的工作流实例
                    if (wfInstance != null) {
                        // 根据操作模式执行相应的操作
                        if (mode != null && mode.equals("stop")) {
                            bizWfInstanceManager.terminateFlow(wfInstance); // 停止流程
                        } else if (mode != null && mode.equals("pause")) {
                            bizWfInstanceManager.pauseFlow(wfInstance); // 暂停流程
                        } else if (mode != null && mode.equals("suspend")) {
                            bizWfInstanceManager.suspendFlow(wfInstance); // 挂起流程
                        } else if (mode != null && mode.equals("run")) {
                            bizWfInstanceManager.runFlow(wfInstance, adminService.getCurrentUserId()); // 启动流程
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LoggerTool.error(logger, "流程实例操作失败:", ex); // 记录操作失败的异常
        }
        this.initInstanceList(poLabel, vo, request, model); // 初始化实例列表
    }

}

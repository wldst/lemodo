package com.wldst.ruder.module.workflow.controller;

import java.util.List;
import java.util.Map;

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
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jDriver;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.ResultWrapper;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 流程监控画面控制器
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/bpmApi")
public class WorkflowController {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(WorkflowController.class);
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService crudService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private CrudNeo4jDriver driver;
    // 流程实例数据库操作

    @Autowired
    private Neo4jOptByUser optByUserSevice;

    @RequestMapping(value = "/todoList", method = RequestMethod.POST)
    @ResponseBody
    public Result<List<Map<String, Object>>> todoList(@RequestBody JSONObject vo) {
        PageObject page = crudUtil.validatePage(vo);
        Long currentPasswordId = adminService.getCurrentPasswordId();
        String query = "Match(n:BpmTaskExecute),(b:BpmNode),(inx:BpmGraphInstance) where n.instanceID=id(inx) and n.taskID=id(b) and n.executorID=" + currentPasswordId + " OR n.executorID='" + currentPasswordId + "' and n.executorStatus=0 return n.id,n.executorStatus,n.executorDecision,n.taskComeDateTime,id(inx) AS processInstanceId,inx.name AS bpmName,inx.code AS bpmCode,b.title AS name";
        // 以下是处理并组织待办任务数据的逻辑
        List<Map<String, Object>> dataList = crudService.cypher(query + " " + optByUserSevice.pageSkip(page));
        if (dataList != null) {
            if (!dataList.isEmpty() && !vo.containsKey(MapTool.ID)) {
                page.setTotal(crudUtil.total(query, vo));
            }
        }
        return Result.wrapResult(dataList, page, "");
    }

    @RequestMapping(value = "/doneList", method = RequestMethod.POST)
    @ResponseBody
    public Result<List<Map<String, Object>>> doneList(@RequestBody JSONObject vo) {
        PageObject page = crudUtil.validatePage(vo);
        Long currentPasswordId = adminService.getCurrentPasswordId();
        String query = "Match(n:BpmTaskExecute),(b:BpmNode),(inx:BpmGraphInstance) where n.instanceId=inx.id and n.taskID=b.id and n.executorID=" + currentPasswordId + " OR n.executorID='" + currentPasswordId + "' and n.executorStatus='1' return n.id,n.executorStatus,n.executorDecision,n.taskComeDateTime,inx.id AS processInstanceId,inx.name AS bpmName,inx.code AS bpmCode,b.title AS name";
        // 以下是处理并组织待办任务数据的逻辑
        List<Map<String, Object>> dataList = crudService.cypher(query + " " + optByUserSevice.pageSkip(page));
        if (dataList != null) {
            if (!dataList.isEmpty() && !vo.containsKey(MapTool.ID)) {
                page.setTotal(crudUtil.total(query, vo));
            }
        }
//        for(Map<String,Object> ri: remindHandleTaskList ) {
//             query = "Match(n:BpmGraphInstance) BpmGraph where n.executorID="+currentPasswordId+" OR n.executorID='"+currentPasswordId+"' and n.executorStatus='0' return n";
//            // 以下是处理并组织待办任务数据的逻辑
//            List<Map<String,Object>> data =  crudService.cypher(query);
//            
//        }
//	return Result.success(remindHandleTaskList);
        return Result.wrapResult(dataList, page, "");
    }
}

package com.wldst.ruder.module.state.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.module.state.service.StateService;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.StatusDomain;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 状态
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/status")
public class StatusController extends StatusDomain {
    @Autowired
    private CrudUserNeo4jService neo4jService;
    @Autowired
    private StateService statusService;

    @RequestMapping(value = "/set/{id}/{statusId}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result set(HttpServletResponse response, Model model, @PathVariable("id") String id,
                      @PathVariable("statusId") String statusId, HttpServletRequest request) throws Exception {
        Long nodeId = Long.valueOf(id);
        Long statusID = Long.valueOf(statusId);
        Map<String, Object> nodeMapById = neo4jService.getNodeMapById(nodeId);
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed();
        }
        Node nodeById = neo4jService.getNodeById(statusID);
        if (nodeById == null) {
            return Result.failed("状态异常");
        }
        statusService.setStatus(nodeId, statusID);
        return Result.success();
    }

    @RequestMapping(value = "/back/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public String back(HttpServletResponse response, Model model, @PathVariable("id") String id,
                       HttpServletRequest request) throws Exception {
        Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(id));
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return "";
        }
        statusService.preStatus(Long.valueOf(id));
        return String.valueOf(nodeMapById.get(NODE_NAME));
    }

    @RequestMapping(value = "/done/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result<Map> done(HttpServletResponse response, Model model, @PathVariable("id") String id,
                            HttpServletRequest request) throws Exception {
        Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(id));
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed("当前节点不存在");
        }
        Map<String, Object> nextStatus = statusService.nextStatus(Long.valueOf(id));
        return Result.success(nextStatus);
    }


    @RequestMapping(value = "/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result<Map> status(HttpServletResponse response, Model model, @PathVariable("id") String id,
                            HttpServletRequest request) throws Exception {
        Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(id));
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed("当前节点不存在");
        }
        Map<String, Object> nextStatus = statusService.currentStatus(Long.valueOf(id));
        return Result.success(nextStatus);
    }


    @RequestMapping(value = "/list/{label}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result<List<Map<String, Object>>> statusList(HttpServletResponse response, Model model, @PathVariable("label") String label,
                            HttpServletRequest request) throws Exception {
        List<Map<String, Object>> nodeMapById = neo4jService.getMetaDataByLabel(label);
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed("元数据不存在");
        }
        List<Map<String, Object>> nextStatus = statusService.listStatus(label);
        return Result.success(nextStatus);
    }

    @RequestMapping(value = "/info/{label}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result<Map<String, Object>> statusInfo(HttpServletResponse response, Model model, @PathVariable("label") String label
            , @RequestBody JSONObject vo, HttpServletRequest request) throws Exception {
        List<Map<String, Object>> nodeMapById = neo4jService.getMetaDataByLabel(label);
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed("元数据不存在");
        }
        String statusValue = string(vo, "value");
        //保存状态步骤，先读取状态列表，如无，则创建状态机，并更新状态。
        List<Map<String, Object>> allStatus = statusService.listStatus(label);

        if(allStatus!=null&&!allStatus.isEmpty()){
           for(Map<String, Object> si:allStatus){
               Boolean flag = false;

               for(Map.Entry<String, Object> e:si.entrySet()){
                   if(String.valueOf(e.getValue()).equals(statusValue)){
                       flag=true;
                       break;
                   }
               }
               if (flag){
                   return Result.success(si);
               }
           }
        }

        return Result.failed("没有相关的状态信息");
    }

    @RequestMapping(value = "/clearSet/{label}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result<List<Map<String, Object>>> clearSet(HttpServletResponse response, Model model, @PathVariable("label") String label
            , @RequestBody JSONObject vo, HttpServletRequest request) throws Exception {
        List<Map<String, Object>> nodeMapById = neo4jService.getMetaDataByLabel(label);
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed("元数据不存在");
        }
        List<Map<String, Object>> statusList = listMapObject(vo, "status");
        //保存状态步骤，先读取状态列表，如无，则创建状态机，并更新状态。
        List<Map<String, Object>> allStatus = statusService.listStatus(label);
        Map<String,Object>  sm=null;
        if(allStatus==null||allStatus.isEmpty()){
            List<Map<String, Object>> maps = neo4jService.queryByCypher("MATCH (n:" + label + ")-[r:status]->(s:stateMachine)  RETURN s");
            if(maps!=null&&!maps.isEmpty()){
                sm =maps.get(0);
            }else{
                sm = new HashMap<>();
                Map<String, Object> md = nodeMapById.get(0);
                sm.put("name", name(md)+"状态机");
                sm.put("code", label+"StateMachine");

                neo4jService.save(sm,"stateMachine");
                neo4jService.addRel("stateMachine",  id(md),id(sm), "状态机");
            }
        }else{
            allStatus.addAll(statusList);
        }
        //clear stateStep
        neo4jService.deleteRel("stateStep",id(sm));

        neo4jService.save(statusList,"stateStep");
        for (Map<String,Object> si:statusList){
            neo4jService.addRel("childrens",id(sm),  id(si), "包含");
        }

        return Result.success(allStatus);
    }


    @RequestMapping(value = "/setting/{label}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result<List<Map<String, Object>>> settingStatus(HttpServletResponse response, Model model, @PathVariable("label") String label
            , @RequestBody JSONObject vo,
                                                        HttpServletRequest request) throws Exception {
        List<Map<String, Object>> nodeMapById = neo4jService.getMetaDataByLabel(label);
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed("元数据不存在");
        }
        List<Map<String, Object>> statusList = listMapObject(vo, "status");
        //保存状态步骤，先读取状态列表，如无，则创建状态机，并更新状态。
        List<Map<String, Object>> allStatus = statusService.listStatus(label);
        Map<String,Object>  sm=null;
        if(allStatus==null||allStatus.isEmpty()){
            List<Map<String, Object>> maps = neo4jService.queryByCypher("MATCH (n:" + label + ")-[r:status]->(s:stateMachine)  RETURN s");
            if(maps!=null&&!maps.isEmpty()){
                sm =maps.get(0);
            }else{
                sm = new HashMap<>();
                Map<String, Object> md = nodeMapById.get(0);
                sm.put("name", name(md)+"状态机");
                sm.put("code", label+"StateMachine");

                neo4jService.save(sm,"stateMachine");
                neo4jService.addRel("stateMachine",  id(md),id(sm), "状态机");
            }
        }else{
            allStatus.addAll(statusList);
        }
        neo4jService.save(statusList,"stateStep");
        for (Map<String,Object> si:statusList){
            neo4jService.addRel("childrens",id(sm),  id(si), "包含");
        }

        return Result.success(allStatus);
    }

    @RequestMapping(value = "/add/{label}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result<List<Map<String, Object>>> addStatus(HttpServletResponse response, Model model, @PathVariable("label") String label,
                                                        @RequestBody JSONObject vo,
                                                              HttpServletRequest request) throws Exception {
        List<Map<String, Object>> nodeMapById = neo4jService.getMetaDataByLabel(label);
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed("元数据不存在");
        }
        //保存状态步骤，先读取状态列表，如无，则创建状态机，并更新状态。
        List<Map<String, Object>> allStatus = statusService.listStatus(label);
        Map<String,Object>  sm=null;
        if(allStatus==null||allStatus.isEmpty()){
            List<Map<String, Object>> maps = neo4jService.queryByCypher("MATCH (n:" + label + ")-[r:status]->(s:stateMachine)  RETURN s");
            if(maps!=null&&!maps.isEmpty()){
                sm =maps.get(0);
            }else{
                sm = new HashMap<>();
                Map<String, Object> md = nodeMapById.get(0);
                sm.put("name", name(md)+"状态机");
                sm.put("code", label+"StateMachine");
                neo4jService.save(sm,"stateMachine");
                neo4jService.addRel("stateMachine",  id(md),id(sm), "状态机");
            }
        }else{
            allStatus=new ArrayList<>();
        }
        for (Map<String,Object> si:allStatus){
            if(si.get("code").equals(vo.get("code"))){
                return Result.failed("状态已存在");
            }
        }
        Map<String, Object> stateMap = copyMap(vo);
        allStatus.add(stateMap);
        neo4jService.save(stateMap,"stateStep");
        neo4jService.addRel("childrens", id(sm), id(stateMap), "包含");
        return Result.success(allStatus);
    }

    @RequestMapping(value = "/first/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result<Map> first(HttpServletResponse response, Model model, @PathVariable("id") String id,
                             HttpServletRequest request) throws Exception {
        Map<String, Object> nodeMapById = neo4jService.getNodeMapById(Long.valueOf(id));
        if (nodeMapById == null || nodeMapById.isEmpty()) {
            return Result.failed("当前节点不存在");
        }
        Map<String, Object> nextStatus = statusService.firstStatus(Long.valueOf(id));
        return Result.success(nextStatus);
    }

}

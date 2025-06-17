package com.wldst.ruder.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.String;

import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.controller.BaseLayuiController;
import com.wldst.ruder.module.workflow.beans.BpmInstance;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/html")
public class HtmlPageController extends BaseLayuiController {
    private static Logger logger = LoggerFactory.getLogger(HtmlPageController.class);

    @Autowired
    private UserAdminService adminService;
    @Autowired
    private BpmInstance bpmi;
    @Autowired
    private CrudUserNeo4jService neo4jService;

    @RequestMapping(value = "/flow", method = {RequestMethod.GET, RequestMethod.POST})
    public String flow(Model model, String table, HttpServletRequest request) throws Exception {
        return "x6/bpmGraph";
    }

    @RequestMapping(value = "/d3", method = {RequestMethod.GET, RequestMethod.POST})
    public String d3(Model model, String table, HttpServletRequest request) throws Exception {
        return "d3/D3";
    }

    @RequestMapping(value = "/form", method = {RequestMethod.GET, RequestMethod.POST})
    public String form(Model model, String table, HttpServletRequest request) throws Exception {
        return "form";
    }

    @RequestMapping(value = "/showVoice", method = {RequestMethod.GET, RequestMethod.POST})
    public String showVoice(Model model, String table, HttpServletRequest request) throws Exception {
        return "showVoice";
    }

    @RequestMapping(value = "/flowInstance/{instanceId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String flowInstance(Model model, @PathVariable("instanceId") String instanceId, String table,
                               HttpServletRequest request) throws Exception {
        // 流程实例数据
        Map<String, Object> flowi = neo4jService.getNodeMapById(Long.valueOf(instanceId));
        // 根据流程记录更新流程履历
        List<Map<String, Object>> history = neo4jService.cypher("MATCH(h:BpmHistory{instanceID:"
                + Long.valueOf(instanceId) + "})-->(u:User),(t:BpmNode{instanceID:" + Long.valueOf(instanceId)
                + "}) where id(t)=h.taskID and id(u)=h.executorID return h,u.id AS userId,u.name AS userName,t.title AS nodeName");
        // 读取流程历史。
        Map<String, List<Map<String, Object>>> dd = new HashMap<>();
        for (Map<String, Object> hi : history) {
            String nodeName = string(hi, "nodeName");
            List<Map<String, Object>> list = dd.get(nodeName);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(hi);
            dd.put(nodeName, list);
        }

        // 获取当前任务信息：
        Map<String, Object> currentTask = bpmi.getNowNode(flowi);
        // 有审批记录的：
        // executeNode(200, 160, '张三', '已审批(同意)', "", 'green');

        String conditions = string(flowi, "conditionList");
        JSONArray joc = JSON.parseArray(conditions);

        String string = string(flowi, "data");

        JSONObject jo = JSON.parseObject(string);
        List<Map<String, Object>> cells = listObjectMap(jo, "cells");

        List<Map<String, Object>> updateDate = new ArrayList<>(cells.size());
        Set<String> historyTask = new HashSet<>();
        String nowTitle = string(currentTask, "title");
        String nowId = null;
        Map<String, String> idShape = new HashMap<>();
        for (Map<String, Object> ci : cells) {
            Map<String, Object> mapObject = mapObject(ci, "position");
            String shape = string(ci, "shape");
            String nodeId = string(ci, ID);
            idShape.put(nodeId, shape);

            if ("normal".equals(shape)) {
                Integer dx = integer(mapObject, "x");
                Integer dy = integer(mapObject, "y");
                if (dx != null && dy != null) {
                    String text = string(ci, "attrs.text.text");
                    if (text.equals(nowTitle)) {
                        nowId = nodeId;
                    }

                    List<Map<String, Object>> list = dd.get(text);
                    StringBuilder sb = new StringBuilder();
                    // 有历史的更新审批记录
                    if (list != null && !list.isEmpty()) {
                        historyTask.add(nodeId);
                        for (Map<String, Object> hi : list) {
                            if (sb.length() > 0) {
                                sb.append(",");
                            }
                            sb.append(string(hi, "userName"));
                            sb.append(dateStr(hi, "createTime"));
                            sb.append(string(hi, "decisionName"));
                            sb.append(":" + string(hi, "opinion"));
                        }
                        String string2 = sb.toString();
                        String color = "green";
                        if (string2.indexOf("打回") > 0) {
                            color = "red";
                        }
                        JSONObject executeNode = executeNode(dx, dy, text, "已审批(" + string2 + ")", "", color);

                        copyValues(ci, executeNode, "id,zIndex,data,ports");
                        updateDate.add(executeNode);
                    } else {

                        if (text.equals(nowTitle)) {// 处理当前节点
                            processCurentTask(currentTask, updateDate, ci, dx, dy, text, sb);
                        } else {
                            updateDate.add(ci);
                        }
                    }
                } else {
                    updateDate.add(ci);
                }
            } else if ("edge".equals(shape)) {
                String vid = string(ci, "data.vid");
                JSONObject[] jos = new JSONObject[joc.size()];
                joc.toArray(jos);
                for (Map<String, Object> ji : jos) {
                    if (string(ji, "vid").equals(vid)) {
                        // route
                        ci.put("condition", string(ji, "logicalExp"));
                        break;
                    }
                }
                routei(historyTask, nowId, idShape, ci);
                updateDate.add(ci);
            } else {
                updateDate.add(ci);
            }

        }
        jo.put("cells", updateDate);
        flowi.put("data", jo.toString());
        ModelUtil.setKeyValue(model, flowi);
        return "x6/bpmInstance";
    }

    @RequestMapping(value = "/flowGraph/{flowId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String flowGraph(Model model, @PathVariable("flowId") String flowId,
                            HttpServletRequest request) throws Exception {
        // 流程实例数据
        Map<String, Object> flowi = neo4jService.getNodeMapById(Long.valueOf(flowId));
        String string = string(flowi, "data");
        JSONObject jo = JSON.parseObject(string);

        flowi.put("data", jo.toString());
        ModelUtil.setKeyValue(model, flowi);
        return "x6/bpmInstance";
    }

    @RequestMapping(value = "/aiWorkFlow", method = {RequestMethod.GET, RequestMethod.POST})
    public String aiWorkFlow(Model model,
                             HttpServletRequest request) throws Exception {
        // 流程实例数据
        Map<String, Object> flowi = neo4jService.getNodeMapById(Long.valueOf(adminService.getCurrentUserId()));

        ModelUtil.setKeyValue(model, flowi);
        return "AI/AIWorkFlow";
    }

    /**
     * 根据appId获取包含的流程信息
     *
     * @param model
     * @param appId
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/appFlow/{appId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String appFlow(Model model, @PathVariable("appId") String appId,
                          HttpServletRequest request) throws Exception {
        // 流程实例数据

        String getFlow = "Match(a:App{appid:\"" + appId + "\"})-->(m:module)-->(c:MetaData)-->(f:BpmGraph)  return f";
        List<Map<String, Object>> query = neo4jService.cypher(getFlow);
        if (query != null && !query.isEmpty()) {
            Map<String, Object> flowi = query.get(0);
            String string = string(flowi, "data");
            JSONObject jo = JSON.parseObject(string);
            //读取可达的流程图信息。
            flowi.put("data", jo.toString());
            ModelUtil.setKeyValue(model, flowi);
        }

        return "x6/bpmInstance";
    }

    private void processCurentTask(Map<String, Object> currentTask, List<Map<String, Object>> updateDate,
                                   Map<String, Object> ci, Integer dx, Integer dy, String text, StringBuilder sb) {
        List<Map<String, Object>> users = listMapObject(currentTask, "nodeUserList");
        if (users == null || users.isEmpty()) {
            return;
        }
        for (Map<String, Object> ui : users) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(string(ui, "username"));
        }
        // sb.append(dateStr(currentTask, "comeDateTime"));
        // sb.append(string(currentTask, "decisionName"));
        String string2 = sb.toString();
        String color = "yellow";
        JSONObject executeNode = executeNode(dx, dy, text, "审批中(" + string2 + ")", "", color);
        copyValues(ci, executeNode, "id,zIndex,data,ports");
        updateDate.add(executeNode);
    }

    private void routei(Set<String> historyTask, String nowId, Map<String, String> idShape, Map<String, Object> ci) {
        String target = string(ci, "target.cell");
        String source = string(ci, "source.cell");

        boolean containT = historyTask.contains(target);
        boolean containS = historyTask.contains(source);

        boolean sStart = idShape.get(source).equals("start");
        boolean sGateway = idShape.get(source).equals("gateway");
        boolean tGateway = idShape.get(target).equals("gateway");
        boolean tCurrent = target.equals(nowId);

        // 多路时，要排除未走连线。
        if (containT && containS || (containT && sStart || containT && sGateway || containS && tGateway
                || containS && tCurrent || sGateway && tCurrent || sStart && tCurrent)) {
            String ddx = """
                            {"line": {
                                "stroke": "#000",
                                "strokeDasharray": "5",
                                            "targetMarker": "classic",
                                            "style": {
                                                "animation": "ant-line 30s infinite linear",
                                            },
                                   }}
                    """;
            JSONObject parseObject = JSON.parseObject(ddx);
            // **

            String conditionStr = string(ci, "condition");
            if (conditionStr != null && !conditionStr.isBlank()) {
                JSONArray labels = addEdgeLabel(conditionStr);
                putKv(ci, "labels", labels);
            }

            putKv(ci, "attrs", parseObject);
        }
    }

    private JSONArray addEdgeLabel(String conditionStr) {
        String lineLabel = """
                  {
                    attrs: {
                      line: {
                        stroke: '#73d13d',
                      },
                      text: {
                        text: 'Custom Label',
                      },
                    },
                  },
                """;
        JSONObject lineLabelAtt = JSON.parseObject(lineLabel);
        putKv(lineLabelAtt, "attrs.text.text", conditionStr);
        JSONArray ja = new JSONArray();
        ja.add(lineLabelAtt);
        return ja;
    }

    public JSONObject executeNode(int x, int y, String rank, String name, String image, String background) {
        StringBuilder template = new StringBuilder();

        template.append("""
                {
                               "width": "180",
                               "height": "60",
                               "x":"x",
                               "y":"y",
                               "shape": "orgnode",
                               "attrs": {
                                   "body": {
                                       "fill": "background",
                                       "stroke": "none",
                                   },
                                   "avatar": {
                                       "opacity": "0.7",
                                   },
                                   "rank": {
                                       "text": "rank",
                                       "fill": "#000",
                                       "wordSpacing": "-5px",
                                       "letterSpacing": "0",
                                   },
                                   "name": {
                                       "text": "name",
                                       "fill": "#000",
                                       "fontSize": "13",
                                       "fontFamily": "Arial",
                                       "letterSpacing": "0"
                                   },
                               },
                           }""");
        String orgNode = template.toString();
        JSONObject jj = JSON.parseObject(orgNode);
        jj.put("x", x);
        jj.put("y", y);
        putKv(jj, "attrs.name.text", name);
        putKv(jj, "attrs.rank.text", rank);
        // putKv(jj,"attrs.avatar.---xlink:href---",image);
        putKv(jj, "attrs.body.fill", background);
        // orgNode =orgNode.replace(":x,",":\""+x+"\",");
        // orgNode =orgNode.replace(":y,",":\""+y+"\",");
        // orgNode =orgNode.replace("text: name,","text: \""+name+"\",");
        // orgNode =orgNode.replace("text: rank,","text: \""+rank+"\",");
        // orgNode =orgNode.replace("fill: background,","fill: \""+background+"\",");
        // orgNode =orgNode.replace("'xlink:href': image,","'xlink:href':
        // \""+image+"\",");

        return jj;
    }
}

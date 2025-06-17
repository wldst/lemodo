package com.wldst.ruder.module.ai;

import bsh.EvalError;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.crud.service.WorkFlowService;
import com.wldst.ruder.domain.GoodsDomain;
import com.wldst.ruder.domain.WebSocketDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.ai.container.Activity;
import com.wldst.ruder.module.ai.container.Agent;
import com.wldst.ruder.module.ai.service.AiService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.parse.ParseExcuteSentence2;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.biz.BpmOperateAssist;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.module.ws.web.ContextServer;
import com.wldst.ruder.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主要是接收AI服务器发来的消息，然后将相关消息发送给相关的会话中，websocket，显示到Web页面上。
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/qwen")
public class QwenApiController extends GoodsDomain {
    final static Logger logger = LoggerFactory.getLogger(QwenApiController.class);
    @Autowired
    private UserAdminService admin;
    @Autowired
    private RelationService relationService;
    @Autowired
    private RestApi restApi;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private AiService aiService;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private BpmInstanceManagerService bizWfInstanceManager;
    @Autowired
    private BpmOperateAssist workflowOperateAssist;
    @Autowired
    private BpmInstance bpmi;
    private static final String chatUrl = "http://192.168.3.132:5000/v1/chat/completions";

    private static Map<String,Object> conversationMap = new HashMap<>();

    @RequestMapping(value = "/ui", method = {RequestMethod.GET, RequestMethod.POST})
    public String chatGlm(Model model, HttpServletRequest request) throws Exception {
        String label = "AuthCommand";
        Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (metaData == null || metaData.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }
        //读取会话历史
	 /*List<Map<String, Object>> listAttMapBy = neo4jService.listAttMapBy("userid", admin.getCurrentAccount(), "ChatSession");
	 model.addAttribute("chatSession", listAttMapBy);*/
        model.addAttribute("myName", admin.getCurrentName());
        model.addAttribute("myId", admin.getCurrentPasswordId());
        ModelUtil.setKeyValue(model, metaData);
        return "qwen";
    }

    @RequestMapping(value = "/talk", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult talk(@RequestBody JSONObject vo)
            throws DefineException, EvalError {
        String msg = string(vo, "msg");
        String myId = string(vo, "myId");
        String chatContextId = string(vo, "chatContextId");

        Map<String, Object> map = new HashMap<>();
        map.put("userId",myId);
        map.put("title",msg);
        Node save = neo4jService.save(map, "ChatSession");
        Long sessionId=save.getId();

//        ChatSession

        msg= msg.replaceAll("\n", "");
        ContextServer.sendInfo(msg, chatContextId);

        Map<String, Object> param = aiService.getChatLLMParam();
        //处理需求
        WrappedResult developData = processNeed(msg, sessionId, myId, param);
        if (developData != null) return developData;
        //元数据解析并更新环境
        WrappedResult metaDataMaintain = agentProcessMsg(msg, sessionId,chatContextId, param);
        //ChatSession
        if (metaDataMaintain != null) return metaDataMaintain;

        //如果是开发请求，新建一个开发进程，并且返回一个进程id，根据进程id，可以获取进程状态，进程结果，进程进度等
        //启动代理处理进程

        // 开发事情判断
         developData = developSomeThing(msg, sessionId, myId, param);
        //ChatSession
        if (developData != null) return developData;
        Map<String, Object> data = aiService.packageMessage(msg, sessionId);
        // Map<String,Object> postData = post(chatUrl,data);
        try {
            Object postForObject = restApi.postForObject(chatUrl, data, Object.class);
            Map<String, Object> datax = (Map<String, Object>) postForObject;
            Map<String, Object> choices = listMapObject(datax, "choices").get(0);
            Map<String, Object> answer = mapObject(choices, "message");
            aiService.addChatItem(sessionId, answer);
            return ResultWrapper.wrapResult(true, content(answer), null, EXECUTE_SUCCESS);
        }catch (Exception e){
            return ResultWrapper.wrapResult(false, "智能体调用失败，请检查智能体服务器是否在线", null, EXECUTE_SUCCESS);
        }
    }

    private WrappedResult agentProcessMsg(String msg1, Long sessionId, String chatContextId, Map<String, Object> param) throws EvalError {
        String msg = msg1.replaceAll("\n", "").replaceAll("：",":");
        Map<String, Object> answer = null;
        if (msg.startsWith("Agent")) {
            String[] talks = msg.split(":");
            if(talks.length<3||talks.length>3){
                return WrappedResult.wrap(false, msg1+ "不符合规范，Agent:智能体:内容", "异常");
            }
            JSONObject userChat = new JSONObject();
            userChat.put("role", "user");
            String content = talks[2].replaceAll("\n", "");
            userChat.put("content", content);

            aiService.addChatItem(sessionId, userChat);
            Long askId = id(userChat);

            Map<String, Object> agentData = neo4jService.getAttMapBy(NAME, talks[1].trim(), "Agent");
            if (agentData == null) {
                agentData = neo4jService.getAttMapBy(CODE, talks[1].trim(), "Agent");
            }

            if (agentData == null) {
                return WrappedResult.wrap(false, talks[1].trim() + "未配置智能体", "未配置智能体");
            }
            ContextServer.sendInfo( "\n【" + name(agentData) + "】收到:"+talks[2].trim() , chatContextId);
            answer = agentWork(userChat,agentData, param, msg);
            //关联智能体的聊天记录
            relationService.addRel("chatHistory", id(agentData), askId);
            WrappedResult content1 = answerProcess(sessionId, chatContextId, param, answer, agentData, content);
            if (content1 != null) return content1;
        }
        return ResultWrapper.wrapResult(true, answer, null, EXECUTE_SUCCESS);
    }

    @Nullable
    private WrappedResult answerProcess(Long sessionId, String chatContextId, Map<String, Object> param, Map<String, Object> answer, Map<String, Object> agentData, String content) throws EvalError {
        if (answer != null&&!"error".equals(code(answer))) {
            //解析answer，然后继续走流程。，将answer更新到节点中。或者更新到执行信息中。
            ContextServer.sendInfo("【" + name(agentData) + "】:"+content(answer), chatContextId);
            aiService.addChatItem(sessionId, answer);
            relationService.addRel("chatHistory", id(agentData), id(answer));
            if("MetaDataAgent".equals(code(agentData))){
                //需要用户确认才可以
                Map<String, Object> myContext = getContext(chatContextId);
                //查询元数据是否存在，如存在，将数据库中存在的和新的进行对比，告诉用户，是否需要更新。
                List<Map<String, Object>> existMetaData = neo4jService.listDataBy(LABEL, content, META_DATA);
                if(existMetaData!=null&&existMetaData.size()>0){
                    JSON.toJSONString(existMetaData);
                    String existMsg = "已经存在元数据" + JSON.toJSONString(existMetaData.get(0)) + "，确定要更新元数据？";
                    if(!userConfirm(myContext,
                            existMsg)){
                        return ResultWrapper.wrapResult(true, content, null, "用户未授权更新元数据");
                    }
                }
                String confirmMsg = "确定要创建实体" + content(answer) + "？";

                if(userConfirm(myContext,confirmMsg)){
                    JSONObject jsonObject =JSON.parseObject(content(answer));
                    neo4jService.saveByBody(jsonObject,META_DATA);
                }
            }
            if("JavaEntityAgent".equals(code(agentData))&&content(answer).contains("结果为：")){
                 String result = content(answer).split("结果为：")[1].trim().replaceAll("\n", "");
               for(String ei:result.split(",")){
                   ei=ei.replaceAll("\"","");

                   Map<String, Object> mdi = neo4jService.getAttMapBy(LABEL, ei, META_DATA);
                   if(mdi ==null){
                       mdi = neo4jService.getAttMapBy(NAME, ei, META_DATA);
                       if(mdi==null){
                           agentProcessMsg("Agent:MetaDataAgent:"+ei, sessionId, chatContextId, param);
                       }else{
                           //添加系统已经存在的实体信息
                           agentProcessMsg("Agent:MetaDataAgent:"+ei+",已经存在元数据信息，"+JSON.toJSONString(mdi), sessionId, chatContextId, param);
                       }
                   }else{
                       //添加系统已经存在的实体信息
                       agentProcessMsg("Agent:MetaDataAgent:"+ei+",已经存在元数据信息，"+JSON.toJSONString(mdi), sessionId, chatContextId, param);
                   }

               }
            }
        }
        return null;
    }

    /**
     * 获取上下文，会话信息
     * @param myId
     * @return
     */
    private static Map<String, Object> getContext(String myId) {
        ParseExcuteSentence2 pes = (ParseExcuteSentence2) SpringContextUtil.getBean("parseExcuteSentence2");
        Map<String, Object> myContext = pes.getMyContext(myId);
        Object converation = myContext.get(WebSocketDomain.CONVERSATION);
        if(converation!=null){
            conversationMap.put(myId, converation);
        }else{
            if(conversationMap.get(myId)!=null){
                myContext.put(WebSocketDomain.CONVERSATION, conversationMap.get(myId));
            }
        }
        if (myContext == null) {
            myContext = new HashMap<>();
        }
        return myContext;
    }


    public boolean userConfirm(Map<String, Object> context, String msg) {
        if(string(context, "which")!=null){
            context.remove("which");
        }
        Object converation = context.get(WebSocketDomain.CONVERSATION);
        if (converation != null) {
            LoggerTool.info(logger,"需要用户确认，且用户已连上Websocket");
            ContextServer cs = (ContextServer) converation;
            try {
                List<String> optionList = new ArrayList<>();
                optionList.add("确定");
                optionList.add("取消");
                cs.sendMessage("\n\n\n" + msg + "，请确认：\n<br>" + confirmOptions(optionList) + "\n\n\n");
                Integer selected = integer(context, "which");
                int count = 0;
                while (selected == null && count < 50) {
                    try {
                        Thread.sleep(1000);
                        LoggerTool.info(logger,"等待用户选择");
                        cs.sendMessage(".");
                        selected = integer(context, "which");
                        count++;
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                context.remove("which");
                if (selected == null) {
                    return false;
                }
                return optionList.get(selected).equals("确定");
            } catch (IOException e1) {
                e1.printStackTrace();
                LoggerTool.error(logger,e1.getMessage(), e1);
            }
        } else {
            return false;
        }
        return false;
    }
    private String confirmOptions(List<String> sis) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String mi : sis) {
            if (sb.length() > 0) {
                sb.append(" 、 ");
            }
            sb.append("  <button type=‘button' onclick=mySelect('" + i + "') >"   + mi + "</button>");
            i++;
        }
        return sb.toString();
    }

    @Nullable
    private WrappedResult developSomeThing(String msg, Long sessionId, String myId, Map<String, Object> param) throws EvalError {
        if ((msg.startsWith("开发") || msg.startsWith("设计")) && (msg.endsWith("程序") || msg.endsWith("工具") || msg.endsWith("应用")|| msg.endsWith("系统")|| msg.endsWith("功能"))) {
            //保存一个任务，新建一个流程。            每个角色去查看待办。
            Map<String, Object> todoi = creatTodoAndFlow(msg);
            Map<String, Object> answer =null;
            List<Map<String, Object>> flowInstance = neo4jService.cypher("match (m:Todo)-[r:HAS_FLOW]->(fi:BpmGraphInstance) where id(m)=" + id(todoi) + " and ( fi.status ='1' or fi.status is null) return fi");
            if (flowInstance.size() > 0) {
                Map<String, Object> flowi = flowInstance.get(0);
                flowi.put("wfStatus",WFEConstants.WFSTATUS_INIT);
                ContextServer.sendInfo("开发类需求，进入流程"+name(flowi), myId);
                if (WFEConstants.WFSTATUS_INIT == integer(flowi, "wfStatus") && "".equals(string(flowi, WFEConstants.NOW_TASK_IDS))) {
                    bizWfInstanceManager.runFlow(flowi, admin.getCurrentUserId());
                }
                //提交流程，同意流程，
                // 获取当前用户ID
                Long currentUserId = admin.getCurrentUserId();
                // 从请求中提取流程ID和执行评论
                Long flowId = id(flowi);
                bpmi.commit(flowId, "提交流程", currentUserId,"");

                flowi = bpmi.getFlowi(id(flowi));
                Integer status = BpmDo.wfStatus(flowi);
                StringBuilder sb = new StringBuilder();
                int ai= 0;
                sb.append(msg.substring(ai));
                int step=1;
                JSONArray conversation = new JSONArray();
                while (status != null && status != WFEConstants.WFSTATUS_END && status != WFEConstants.WFSTATUS_PAUSE) {
                    //刷新数据
                    flowi = bpmi.getFlowi(id(flowi));

                    Map<String, Object> currentTask  = bpmi.getNowNode(flowi);

                    Map<String, Object>  nextTask = bpmi.findNextNormalNode(flowi);
                    if (nextTask == null) {
                        status=WFEConstants.WFTASK_STATUS_END;
                        flowi.put("wfStatus", WFEConstants.WFSTATUS_END);
                        neo4jService.update(flowi, "BpmGraphInstance", "wfStatus".split(","));
                    }
                    ContextServer.sendInfo("\n"+step+"、【"+string(currentTask, "agentName")+"】开始处理【"+string(currentTask,"title")+"】", myId);


                    JSONObject pre = new JSONObject();
                    pre.put("role", "user");
                    if(answer!=null){
                        String contentOfAnswer = content(answer);
                        ai= contentOfAnswer.indexOf("\n\n1.");
                        // 获取下一个任务信息,每次要刷新一下流程状态
                        if(ai>0){
                            String anserHandler = contentOfAnswer.substring(ai).replaceAll("\n", "");
                            sb.append(anserHandler);
                            ContextServer.sendInfo("【"+string(currentTask, "agentName")+"】完成【"+string(currentTask,"title")+"】\n"+ anserHandler, myId);
                        }else{
                            sb.append(contentOfAnswer.replaceAll("\n", ""));
                            ContextServer.sendInfo("【"+string(currentTask, "agentName")+"】完成【"+string(currentTask,"title")+"】\n"+ contentOfAnswer, myId);
                        }
                    }
                    pre.put("content", sb.toString());

                    String agentId  = string(currentTask, "agentID");
                    if (agentId == null) {
                        return WrappedResult.wrap(false, string(currentTask, "title") + "未配置智能体", string(currentTask, "title") + "未配置智能体");
                    }

                    conversation.add(pre);
                    answer = callLLMByAgent(conversation, currentTask, param);
                    status = updateWorkFlow(sessionId, myId, answer, nextTask, currentTask, flowi);
                }
            }
            return ResultWrapper.wrapResult(true, answer, null, EXECUTE_SUCCESS);
        }
        return null;
    }

    /**
     *
     * @param msg
     * @param sessionId
     * @param myId
     * @param param
     * @return
     * @throws EvalError
     */
    private WrappedResult processNeed(String msg, Long sessionId, String myId, Map<String, Object> param) throws EvalError {
        if (msg.contains("需要")||msg.contains("要")||msg.contains("帮我")||msg.contains("给我")||msg.contains("want")||msg.contains("need")) {
            //保存一个任务，新建一个流程。            每个角色去查看待办。
//            String[] querys = msg.split("要");
            String label = "Todo";

            Map<String,Object> md = neo4jService.getAttMapBy(LABEL,label,META_DATA);


            Map<String, Object> datai = creatDataAndWorkFlow(msg,label);
            Map<String, Object> answer =null;
            List<Map<String, Object>> flowInstance = neo4jService.cypher("match (m:"+label+")-[r:HAS_FLOW]->(fi:BpmGraphInstance) where id(m)=" + id(datai) + " and ( fi.status ='1' or fi.status is null) return fi");
            if (flowInstance.size() > 0) {
                Map<String, Object> flowi = flowInstance.get(0);
                flowi.put("wfStatus",WFEConstants.WFSTATUS_INIT);
                ContextServer.sendInfo("进入流程"+name(flowi), myId);
                if (WFEConstants.WFSTATUS_INIT == integer(flowi, "wfStatus") && "".equals(string(flowi, WFEConstants.NOW_TASK_IDS))) {
                    bizWfInstanceManager.runFlow(flowi, admin.getCurrentUserId());
                }
                //提交流程，同意流程，
                // 获取当前用户ID
                Long currentUserId = admin.getCurrentUserId();
                // 从请求中提取流程ID和执行评论
                Long flowId = id(flowi);
                bpmi.commit(flowId, "提交流程", currentUserId,"");

                flowi = bpmi.getFlowi(id(flowi));
                Integer status = BpmDo.wfStatus(flowi);
                StringBuilder sb = new StringBuilder();
                int answerIndex = 0;
                sb.append(msg.substring(answerIndex));
                int step=1;
                JSONArray conversation = new JSONArray();
                while (status != null && status != WFEConstants.WFSTATUS_END && status != WFEConstants.WFSTATUS_PAUSE) {
                    //刷新数据
                    flowi = bpmi.getFlowi(id(flowi));

                    Map<String, Object> currentTask  = bpmi.getNowNode(flowi);
                    List<Map<String, Object>> workAgent = getAgent(currentTask, param);
                    String agentNames = names(workAgent);
                    currentTask.put("agentName",agentNames);
                    String taskName = string(currentTask, "title");
                    //多个智能体协作，进行启动活动
                    if(workAgent!=null&&workAgent.size()>0){
                        ContextServer.sendInfo(" 启动活动【"+ taskName +"】", myId);
                        // 启动活动
                        String noteDes = string(currentTask, "noteDes");
                        Activity activity = new Activity(taskName, noteDes);
                        List<Agent> agents = new ArrayList<>();
                        for(Map<String, Object> ai:workAgent){
                            Agent agent = new Agent(ai);
                            agent.registerObserver(activity);
                            activity.registerObserver(agent);
                            agents.add(agent);
                        }
                        activity.setAgents(agents);
                        activity.notify(noteDes);
                    }else{
                        currentTask.put("agentID", id(workAgent.get(0)));
                    }

                    Map<String, Object>  nextTask = bpmi.findNextNormalNode(flowi);
                    if (nextTask == null) {
                        status=WFEConstants.WFTASK_STATUS_END;
                        flowi.put("wfStatus", WFEConstants.WFSTATUS_END);
                        neo4jService.update(flowi, "BpmGraphInstance", "wfStatus".split(","));
                    }
                    ContextServer.sendInfo("\n"+step+"、【"+string(currentTask, "agentName")+"】开始处理【"+ taskName +"】", myId);

                    JSONObject pre = new JSONObject();
                    pre.put("role", "user");
                    if(answer!=null){
                        String contentOfAnswer = content(answer);
                        answerIndex= contentOfAnswer.indexOf("\n\n1.");
                        // 获取下一个任务信息,每次要刷新一下流程状态
                        if(answerIndex>0){
                            String anserHandler = contentOfAnswer.substring(answerIndex).replaceAll("\n", "");
                            sb.append(anserHandler);
                            ContextServer.sendInfo("【"+string(currentTask, "agentName")+"】完成【"+ taskName +"】\n"+ anserHandler, myId);
                        }else{
                            sb.append(contentOfAnswer.replaceAll("\n", ""));
                            ContextServer.sendInfo("【"+string(currentTask, "agentName")+"】完成【"+ taskName +"】\n"+ contentOfAnswer, myId);
                        }
                    }
                    pre.put("content", sb.toString());

                    String agentId  = string(currentTask, "agentID");
                    if (agentId == null) {
                        return WrappedResult.wrap(false, taskName + "未配置智能体", taskName + "未配置智能体");
                    }

                    conversation.add(pre);
                    answer = callLLMByAgent(conversation, currentTask, param);
                    status = updateWorkFlow(sessionId, myId, answer, nextTask, currentTask, flowi);
                }
            }
            return ResultWrapper.wrapResult(true, answer, null, EXECUTE_SUCCESS);
        }
        return null;
    }


    private List<Map<String, Object>> getAgent(Map<String, Object> currentTask, Map<String, Object> param) {
        String postDesc= string(currentTask,"noteDes");
        List<Map<String, Object>> agents = neo4jService.listAllByLabel("Agent");
        Map<String,Map<String,Object>> agentsIdMap = new HashMap<>();
        for(Map<String, Object> aj: agents){
            agentsIdMap.put(stringId(aj),aj);
        }

        int countRole =countRole(postDesc,param);

        List<Map<String, Object>> matchAgents = new ArrayList<>();
        Map<String, Object> agentAi=null;
        StringBuilder sb1 = new StringBuilder();
        if (agents.size() > 0){
            if(agents.size()>10){
                int pageNum=0;
                while(pageNum*10<agents.size()){
                    for (int i = 0; i < 10; i++) {
                        if(i==0){
                            sb1=new StringBuilder();
                        }

                        if(sb1.length()>1){
                            sb1.append(",");
                        }
                        sb1.append("{");
                        int index = i+pageNum*10;
                        if(index>=agents.size()){
                            break;
                        }
                        if(matchAgents.size()>countRole&&countRole>0){
                            break;
                        }
                        Map<String, Object> agent = agents.get(index);
                        String agentName = name(agent);
                        String agentCode = code(agent);
                        Long agentId = id(agent);
                        String agentDesc = string(agent, "duty");
                        String agentPrompt = string(agent, "prompt");
                        sb1.append("agentId="+agentId+",code="+agentCode+",agentName=【"+agentName+"】,"+agentPrompt+agentDesc+"}");
                        if(i==9){
                            List<Map<String, Object>> withAgentsAndPost = getWithAgentsAndPost(sb1.toString(), postDesc, param);
                            if (withAgentsAndPost!=null&&!withAgentsAndPost.isEmpty()){
                                matchAgents.addAll(withAgentsAndPost);
                            }
                        }
                    }
                    pageNum++;
                }
            }else{
                for (Map<String, Object> agent : agents) {
                    if(sb1.length()>1){
                        sb1.append(",");
                    }
                    sb1.append("{");
                    String agentName = name(agent);
                    String agentCode = code(agent);
                    Long agentId = id(agent);
                    String agentDesc = string(agent, "duty");
                    String agentPrompt = string(agent, "prompt");
                    sb1.append("agentId="+agentId+",code="+agentCode+",agentName=【"+agentName+"】,"+agentPrompt+agentDesc+"}");
                }
                matchAgents = getWithAgentsAndPost(sb1.toString(), postDesc,param);
            }
        }

        for(Map<String, Object> ai: matchAgents){
             ai.put(NAME,name(agentsIdMap.get(stringId(ai))));
        }

        return matchAgents;
    }

    private int countRole(String sb1, Map<String, Object> param) {
         int count=0;

        Map<String, Object> currentNodeAgent=new HashMap<>();
        currentNodeAgent.put("title", "统计角色个数");

        Map<String, Object> agentData = neo4jService.getAttMapBy(CODE, "countRole", "Agent");
        currentNodeAgent.put("agentID", id(agentData));


        JSONArray conversation = new JSONArray();
        JSONObject pre = new JSONObject();
        pre.put("role","user");
        pre.put("content", sb1);
        conversation.add(pre);

        Map<String, Object> answer= callLLMByAgent(conversation, currentNodeAgent, param);
        String content =content(answer);
        if(!content.endsWith("NO")&&content.contains("结果为:")){
            String countRole=content.split("结果为:")[1];
            if(countRole.contains("、")){
                count=countRole.split("、").length;
            }else{
                count=Integer.valueOf(countRole);
            }
        }
        return count;
    }

    /**
     * 智能体识别和选择
     * @param sb1
     * @param postDesc
     * @param param
     * @return
     */
    @NotNull
    private List<Map<String, Object>> getWithAgentsAndPost(String sb1, String postDesc, Map<String, Object> param) {
        List<Map<String, Object>> matchAgents=new ArrayList<>();

        Map<String, Object> currentNodeAgent=new HashMap<>();
        currentNodeAgent.put("title", "智能体识别");

        Map<String, Object> agentData = neo4jService.getAttMapBy(CODE, "AgentRecognize", "Agent");
        currentNodeAgent.put("agentID", id(agentData));

        Map<String, Object> promptTemplate = neo4jService.getAttMapBy(CODE, "AgentSelect", "PromptTemplate");
        String prompt = content(promptTemplate);
        prompt= prompt.replace("${Agents}", sb1);
        prompt= prompt.replace("${postDesc}", postDesc);
        JSONArray conversation = new JSONArray();
        JSONObject pre = new JSONObject();
        pre.put("role","user");
        pre.put("content", prompt);
        conversation.add(pre);

        Map<String, Object> answer= callLLMByAgent(conversation, currentNodeAgent, param);
        String content =content(answer);
        if(!content.endsWith("NO")&&content.contains("结果为:")){
            String agentId=content.split("结果为:")[1];
             for(String ai:agentId.split(",")){
                   Map<String, Object> agent = new HashMap<>();
                   agent.put("id",ai);
                   matchAgents.add(agent);
            }
        }
        return matchAgents;
    }

    @NotNull
    private Integer updateWorkFlow(Long sessionId, String myId, Map<String, Object> answer, Map<String, Object> nextTask, Map<String, Object> currentTask, Map<String, Object> flowi) throws EvalError {
        Integer status;
        Long flowId=id(flowi);
        if (answer != null) {
            //解析answer，然后继续走流程。，将answer更新到节点中。或者更新到执行信息中。
            try {
                //添加流程走下去的逻辑
                JSONObject agreeVo = new JSONObject();
                agreeVo.put("flowId", flowId);
                agreeVo.put("agentID",  longValue(nextTask, "agentID"));
                agreeVo.put("currentAgentName",  string(currentTask, "agentName"));
                agreeVo.put("comment", "智能体" + string(currentTask, "agentName")+"审批同意");
                ContextServer.sendInfo("【"+string(currentTask, "agentName")+"】完成【"+string(currentTask,"title")+"】，流程下一步【"+string(nextTask,"title")+"】环节，由"+string(nextTask, "agentName")+"继续执行", myId);
                Boolean operateFlow = workflowOperateAssist.operateFlow(agreeVo, longValue(currentTask, "agentID"), WFEConstants.WFDECISION_AGREE);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            aiService.addChatItem(sessionId, answer);
        }
        status= BpmDo.wfStatus(flowi);
        return status;
    }

    @Nullable
    private Map<String, Object> callLLMByAgent(JSONArray messages, Map<String, Object> currentTask, Map<String, Object> param) {
        JSONObject agent = packageAgent(currentTask);
        if (agent == null) {
            return null;
        }
//        messages.add(agent);
        List<Map<String,Object>> datas = new ArrayList<>();
        datas.add(agent);
        for(Object ji:messages.toArray()){
            datas.add((Map<String, Object>) ji);
        }
        param.put("messages", datas);
        //判断chatUrl地址是否正常
        try {
            Map postForObject = restApi.postForObject(chatUrl, param, Map.class);

            Map<String, Object> datax = (Map<String, Object>) postForObject;
            Map<String, Object> choices = listMapObject(datax, "choices").get(0);
            Map<String, Object> answer = mapObject(choices, "message");
            return answer;
        }catch (Exception e){
            Map<String, Object> answer = newMap();
            answer.put("content", string(currentTask,"title")+"智能体调用失败，请检查智能体是否在线");
            return answer;
        }
    }

    private Map<String, Object> agentWork( JSONObject m2, Map<String, Object> angetData,  Map<String, Object> param, String msg) {
        JSONObject agent = packageChatByAgent(angetData);
        if (agent == null) {
            return null;
        }
        JSONArray conversation = new JSONArray();
        conversation.add(agent);
        //获取当前Agent的历史会话信息
        List<Map<String, Object>> chatItems = neo4jService.cypher("MATCH (n:Agent)-[r:chatHistory]->(c:ChatItem) where id(n) = "+id(angetData)+" and( c.isPrompt='true' or c.isPrompt='on') return c.role,c.content order by c.createTime desc ");
        if(chatItems.size()>0){
            for(Map<String, Object> ci: chatItems){
                conversation.add(ci);
            }
        }
        conversation.add(m2);

        param.put("messages", conversation);
        //判断chatUrl地址是否正常
        try {
            Map postForObject = restApi.postForObject(chatUrl, param, Map.class);

            Map<String, Object> datax = (Map<String, Object>) postForObject;
            Map<String, Object> choices = listMapObject(datax, "choices").get(0);
            Map<String, Object> answer = mapObject(choices, "message");
            return answer;
        }catch (Exception e){
            Map<String, Object> answer = newMap();
            answer.put("code","error");
            answer.put("content", name(angetData)+"智能体调用失败，请检查智能体是否在线");
            return answer;
        }
    }

    @NotNull
    private Map<String, Object> creatTodoAndFlow(String msg) {
        Map<String, Object> data = new HashMap<>();
        if (msg.length() > 20) {
            data.put("name", msg.substring(0, 20));
        } else {
            data.put("name", msg);
        }

        data.put("content", msg);
        data.put("startTime", System.currentTimeMillis());
        Node save = neo4jService.save(data, TODO_LABEL);
        data.put("id", save.getId());
        Long mdId = neo4jService.getNodeId(CruderConstant.LABEL, TODO_LABEL, CruderConstant.META_DATA);
        workFlowService.createFlow(TODO_LABEL, mdId, id(data));
        return data;
    }

    private Map<String, Object> creatDataAndWorkFlow(String msg, String label) {
        Map<String, Object> data = new HashMap<>();
        if (msg.length() > 20) {
            data.put("name", msg.substring(0, 20));
        } else {
            data.put("name", msg);
        }

        data.put("content", msg);
        data.put("startTime", System.currentTimeMillis());
        Node save = neo4jService.save(data, label);
        data.put("id", save.getId());
        Long mdId = neo4jService.getNodeId(CruderConstant.LABEL, label, CruderConstant.META_DATA);
        workFlowService.createFlow(label, mdId, id(data));
        return data;
    }

    private Map<String, Object> creatChatSessionAndFlow(String msg) {
        Map<String, Object> data = new HashMap<>();
        if (msg.length() > 20) {
            data.put("name", msg.substring(0, 20));
        } else {
            data.put("name", msg);
        }

        data.put("content", msg);
        data.put("startTime", System.currentTimeMillis());
        Node save = neo4jService.save(data, TODO_LABEL);
        data.put("id", save.getId());
        Long mdId = neo4jService.getNodeId(CruderConstant.LABEL, TODO_LABEL, CruderConstant.META_DATA);
        workFlowService.createFlow(TODO_LABEL, mdId, id(data));
        return data;
    }

    public JSONObject packageAgent(Map<String, Object> currentTask) {
        // 处理当前任务的执行人信息，并添加到模型中
        String agentId = string(currentTask, "agentID");
        if (agentId == null) {
            return null;
        }
        Map<String, Object> agent = neo4jService.getNodeMapById(Long.valueOf(agentId));

        // 同意操作的标识

        return packageChatByAgent(agent);
    }

    @NotNull
    private static JSONObject packageChatByAgent(Map<String, Object> agent) {
        String name = name(agent);
        String code = code(agent);
        String prompt = string(agent, "prompt");
        String duty = string(agent, "duty");
        JSONObject mi = new JSONObject();
        // 这里可以根据sessionID获取会话历史,
        mi.put("role", "system");
        StringBuilder sb = new StringBuilder();
        if(prompt!=null){
            sb.append(prompt);
        }
        if(duty!=null){
            sb.append(duty);
        }

        mi.put("content", sb.toString() + ". Follow the user's instructions carefully. " +
                        "Do not return data unrelated to the results. ");
        return mi;
    }

    @RequestMapping(value = "/answer", method = {RequestMethod.GET, RequestMethod.POST})
    public String answer(Model model, HttpServletRequest request) throws Exception {
        String label = "AuthCommand";
        Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (metaData == null || metaData.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }
        //新开一个会话
        model.addAttribute("myName", admin.getCurrentName());
        model.addAttribute("myId", admin.getCurrentPasswordId());
        ModelUtil.setKeyValue(model, metaData);
        return "chatGLM";
    }


    public Map<String, Object> post(String url, Map<String, Object> data) {
        Map<String, Object> ret = null;
        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // 设置请求方法为GET
            con.setRequestMethod("POST");
            con.setReadTimeout(30000);
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : data.entrySet()) {
                if (postData.length() != 0) {
                    postData.append('&');
                }
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            LoggerTool.info(logger,postData.toString());
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");
            con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            con.setRequestProperty(url, url);

            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            con.setConnectTimeout(2000);
            con.setReadTimeout(5000);
            con.setDoOutput(true);
            con.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0; ) {
                sb.append((char) c);
            }
            in.close();
            con.disconnect();

            String responseStr = sb.toString();
            LoggerTool.info(logger,"RequestUtils - responseStr <== " + responseStr);
            if (responseStr.isEmpty()) {
                responseStr = "{}";
            }
            System.out.println("\nResponse Content:\n" + responseStr);
            int statusCode = con.getResponseCode();
            LoggerTool.info(logger,"RequestUtils - statusCode <== " + statusCode);
            if (HttpServletResponse.SC_OK == statusCode) {
                JSONObject dataJson = (JSONObject) JSON.parse(responseStr);
                ret = new HashMap(dataJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return ret;
    }
}

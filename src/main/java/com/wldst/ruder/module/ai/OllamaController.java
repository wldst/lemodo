package com.wldst.ruder.module.ai;

import bsh.EvalError;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.domain.WebSocketDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.ai.service.AiService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.parse.ParseExcuteSentence2;
import com.wldst.ruder.module.ws.web.ContextServer;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.*;

import com.wldst.ruder.module.ai.container.*;
import com.wldst.ruder.module.ai.container.Process;

@Controller
@RequestMapping("${server.context}/ollama")
public class OllamaController extends MapTool{
    final static Logger logger=LoggerFactory.getLogger(OllamaController.class);
    @Autowired
    private AiService aiService;
    @Autowired
    private UserAdminService admin;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private RelationService relationService;
    @Autowired
    private  ParseExcuteSentence2 parseExcuteSentence2;
    private static Map<String, Object> conversationMap=new HashMap<>();
    /**
     * http://localhost:8088/ollama/chat/v1?msg=天空为什么是蓝色的？
     */
    @GetMapping("/chat/v1")
    public String ollamaChat(@RequestParam String msg) {
        Map<String, Object> currentUser=admin.getCurrentUser();
        String myId=admin.getCurrentAccount();
        Long sessionId= aiService.getSessionId(myId,msg);
        //增删改查元数据识别
        Map<String, Object> data=aiService.packageMessage(msg, sessionId);
        WrappedResult answer=aiService.callAI(sessionId, data, myId);
        return content((Map<String,Object>)answer.getData());
    }

    @GetMapping("/chat/task")
    @ResponseBody
    public String ollamaTask(@RequestParam String msg) {
        Map<String, Object> currentUser=admin.getCurrentUser();
        String myId=admin.getCurrentAccount();
        List<String> actions = Arrays.asList("根据", "分析", "查询");
        Long sessionId= aiService.getSessionId(myId,msg);
        Process pf = new Process(myId, msg);
        Long processiID=saveProcess(msg, myId, sessionId);
//        根据需求、待办、任务、功能等数据，相关人员合理的建议，在理进行图数据补充
        for(String acti:actions){
            if(msg.startsWith(acti)){
                String actiMsg=msg.replaceFirst(acti, "");
                if(actiMsg.contains("，")){
                    actiMsg=actiMsg.replace("，",",");
                }
                String names = actiMsg.split(",")[0];

                String[] split=names.split("、");
                List<Map<String, Object>> metaDatas=new ArrayList<>(split.length);
                List<Map<String, Object>> datas=new ArrayList<>();
                JSONArray func = new JSONArray();
                for(String ni: split){
                    Map<String, Object> metaDatai=neo4jService.getAttMapBy("name", ni, "MetaData");
                    if(metaDatai!=null&&!metaDatai.isEmpty()){
                        JSONArray functions=aiService.crudFunctionCall(metaDatai);
                        func.addAll(functions);
                        metaDatas.add(metaDatai);
                    }else{
                        datas.addAll(neo4jService.getDataBy(ni));
                    }
                }
                String content=aiService.renderPrompt(msg, "ActionExcute");
                content=content.replace("${FunctionCall}", func.toJSONString());
                content=content.replace("${data}", JSON.toJSONString(datas));
                content=content.replace("${MetaData}", JSON.toJSONString(metaDatas));
                //实现任务拆解与执行，并返回结果，指定执行计划
                return callAi(content, sessionId, myId, pf, processiID);
            }
        }

        //实现任务拆解与执行，并返回结果，指定执行计划
        return callAi(msg, sessionId, myId, pf, processiID);

    }

    @Nullable
    private String callAi(String msg, Long sessionId, String myId, Process pf, Long processiID){
        String content=aiService.renderPrompt(msg, "AgentTaskList");
        Map<String, Object> data=aiService.taskMessage(msg, sessionId,content);
        WrappedResult answer=aiService.callAI(sessionId, data, myId);
        String result=null;
        if(answer!=null){
            String taskList = content((Map<String,Object>)answer.getData());
            logger.info("taskList:{}",taskList);
            if(taskList!=null&&taskList.contains(",\n")){
                String[] tasks=taskList.trim().split(",\n");
                for(String ti:tasks){
                    //创建任务对应的活动
                    if(ti.contains("[")){
                        ti=ti.split("\\[")[1];
                    }
                    if(ti.contains("]")){
                        ti=ti.split("\\]")[0];
                    }
                    result= handleTaski(msg, ti, sessionId, myId, pf, processiID);
                }
            }else{
                result= handleTaski(msg, taskList, sessionId, myId, pf, processiID);
            }
            return result;
        }

        return "";
    }

    private String handleTaski(String msg, String ti, Long sessionId, String myId, Process pf, Long processiID){
        String result=null;
        String commandText=ti.replaceAll("\n", "");
        commandText=commandText.replaceAll("\"", "");
        commandText=commandText.trim();
//        Map<String, Object> taskData=parseExcuteSentence2.parseAndExcute(commandText);
//        if(taskData!=null){
//          return "";
//        }
        String content =aiService.renderPrompt(commandText, "AgentTaskDispatcher");
        //获取所有智能体
        List<Map<String, Object>> agents = neo4jService.listDataByLabel("Agent");
        content=content.replace("${AIAgents}", jsonString(agents));
        Map<String, Object> data=aiService.taskMessage(msg, sessionId,content);
        WrappedResult answer=aiService.callAI(sessionId, data, myId);
        String matchi=content((Map<String,Object>)answer.getData());
        Activity ai=new Activity(ti, matchi);
        ai.setAiService(aiService);
        if(pf.getKnowledgeBase()!=null&&!pf.getKnowledgeBase().isEmpty()){
            ai.updateKnowledgeBase(pf.getKnowledgeBase());
        }
        pf.registerObserver(ai);
        //接口调用，如何将接口调用数据
        //AgentTask
        Long agentTaskId=saveAgentTask(ti, matchi, sessionId, myId);
        relationService.addRel("work", "工作情况",agentTaskId, processiID);
        if(matchi!=null&&matchi.contains(",")){
            matchi=matchi.split("\\[")[1];
            matchi=matchi.split("\\]")[0];

            String[] matchiList=matchi.split(",");
            for(String matchiItem:matchiList){
                if(matchiItem.contains("//")){
                    continue;
                }
                matchiItem=matchiItem.replace("\n","").trim();
                matchiItem=matchiItem.replace("\"","").trim();

                Map<String, Object> agenti=null;
                try{
                    agenti=neo4jService.getNodeMapById(matchiItem);
                }catch (Exception e){
                    agenti=neo4jService.getAttMapBy(CODE,matchiItem,"Agent");
                }

                Agent agentj=new Agent(agenti);
                Map<String, Object> activityKnowledge=ai.getKnowledgeBase();
                if(activityKnowledge!=null&&!activityKnowledge.isEmpty()){
                    agentj.updateKnowledgeBase(activityKnowledge);
                }
                agentj.receiveTask(ti);
                ai.registerObserver(agentj);
                result=agentj.makeDecision();
                ai.updateKnowledgeBase(matchiItem, result);
                Long workiId=saveWork(ti, agenti, result);
                relationService.addRel("work", "工作情况", workiId,agentTaskId);
            }
            result= ai.makeDecision();
        }
        pf.updateKnowledgeBase(ti, result);
        pf.updateKnowledgeBase(ai.getKnowledgeBase());
        return result;
    }

    @Nullable
    private Long saveProcess(String msg, String myId, Long sessionId){
        Map<String, Object> processi= newMap();
        processi.put("content", msg);
        processi.put("userId", myId);
        processi.put("sessionId", sessionId);
        neo4jService.save(processi,"Process");
        Long processiID = id(processi);
        return processiID;
    }

    @Nullable
    private Long saveAgentTask(String ti, String matchi, Long sessionId, String myId){
        Map<String, Object> agentTask= newMap();
        agentTask.put("sessionId", sessionId);
        agentTask.put("userId", myId);
        agentTask.put("name", ti);
        agentTask.put("agents", matchi);
        neo4jService.save(agentTask,"AgentTask");
        Long agentTaskId = id(agentTask);
        return agentTaskId;
    }

    @Nullable
    private Long saveWork(String ti, Map<String, Object> agenti, String result){
        Map<String, Object> worki= newMap();
        worki.put("prompt", ti);
        worki.put("name", name(agenti));
        worki.put("result", result);
        neo4jService.save(worki,"AgentWork");
        Long workiId = id(worki);
        return workiId;
    }

    /**
     * http://localhost:8088/ollama/chat/v2?msg=人为什么要不断的追求卓越？
     */
    @GetMapping("/chat/v2")
    public Object ollamaChatV2(@RequestParam String msg) throws DefineException, EvalError{
        Map<String, Object> currentUser=admin.getCurrentUser();
        String myId=admin.getCurrentAccount();
        String userName =  string(currentUser,"userName");
        Long sessionId= aiService.getSessionId(myId,msg);
        //增删改查元数据识别
        Map<String, Object> data=aiService.packageMessage(msg, sessionId);
        String message=msg;
        WrappedResult entitData=agentProcessMsg(message, sessionId, userName);
        if(entitData.getStatus()&&entitData.getData()!=null){
            Map<String, Object> metaData=(Map<String, Object>) entitData.getData();
            if(metaData!=null&&label(metaData)!=null){
                JSONArray functions=aiService.crudFunctionCall(metaData);
                data.put("functions", functions);
                WrappedResult answer=aiService.callAI(sessionId, data, myId);
                if(answer!=null) return answer;
            }
        }
        return null;
    }

    /**
     * 智能体处理消息的方法。
     * 该方法主要用于解析和处理来自代理的消息，其中包括与智能体的交互和对智能体相关信息的处理。
     *
     * @param msg1          消息字符串，可能包含换行符和特殊字符，需要进行格式化
     * @param sessionId     会话ID，用于标识当前的会话
     * @param chatContextId 聊天上下文ID，用于标识当前的聊天上下文
     * @return WrappedResult 包含处理结果的对象，包括是否成功和具体的消息内容
     * @throws EvalError 如果消息格式不正确或处理过程中出现错误，则抛出此异常
     */
    private WrappedResult agentProcessMsg(String msg1, Long sessionId, String chatContextId) throws EvalError{
        // 格式化消息字符串，替换换行符和冒号字符，以便后续处理。
        String msg=msg1.replaceAll("\n", "").replaceAll("：", ":");

        Map<String, Object> answer=null;

        // 检查消息是否以"Agent"开头，是则进行智能体相关的处理。
        if(msg.startsWith("Agent")){
            // 根据冒号分割消息，期望得到智能体、内容等信息。
            String[] talks=msg.split(":");

            // 验证消息格式是否符合预期，确保包含足够的信息。
            if(talks.length<3){
                return WrappedResult.wrap(false, msg1+"不符合规范，Agent:智能体:内容", "异常");
            }

            // 构建用户聊天信息的JSON对象。
            JSONObject userChat=new JSONObject();
            userChat.put("role", "user");
            StringBuilder ui=new StringBuilder();
            for(int i=2; i<talks.length; i++){
                if(ui.length()>0){
                    ui.append(":");
                }
                ui.append(talks[i]);
            }
            String content=ui.toString().replaceAll("\n", "");
            userChat.put("content", content);

            // 将用户聊天信息添加到会话中。
            aiService.addChatItem(sessionId, userChat);
            Long askId=id(userChat);

            // 尝试根据名称或代码获取智能体的相关信息。
            Map<String, Object> agentData=neo4jService.getAttMapBy(NAME, talks[1].trim(), "Agent");
            if(agentData==null){
                agentData=neo4jService.getAttMapBy(CODE, talks[1].trim(), "Agent");
            }

            // 如果未能获取到智能体信息，则返回未配置智能体的错误信息。
            if(agentData==null){
                return WrappedResult.wrap(false, talks[1].trim()+"未配置智能体", "未配置智能体");
            }

            // 记录智能体收到的消息。
            ContextServer.sendInfo("\n【"+name(agentData)+"】收到:"+talks[2].trim(), chatContextId);

            // 进行智能体的工作处理，包括回答问题等。
            answer=agentWork(userChat, agentData, msg, sessionId, chatContextId);

            // 关联智能体的聊天记录。
            relationService.addRel("chatHistory", id(agentData), askId);

            // 处理回答内容，并可能根据处理结果返回特定的响应。
            WrappedResult content1=answerProcess(sessionId, chatContextId, answer, agentData, content);
            if(content1!=null) return content1;
        }

        // 如果没有得到回答，则根据处理状态返回相应的结果。
        if(answer==null){
            return ResultWrapper.wrapResult(false, null, null, EXECUTE_FAILED);
        }else{
            // 返回处理成功的结果。
            return ResultWrapper.wrapResult(true, answer, null, EXECUTE_SUCCESS);
        }
    }

    private Map<String, Object> agentWork(JSONObject m2, Map<String, Object> angetData, String msg, Long sessionId, String chatContextId){
        JSONObject agent=packageChatByAgent(angetData);
        if(agent==null){
            return null;
        }
        JSONArray conversation=new JSONArray();
        conversation.add(agent);
        //获取当前Agent的历史会话信息
        List<Map<String, Object>> chatItems=neo4jService.cypher("MATCH (n:Agent)-[r:chatHistory]->(c:ChatItem) where id(n) = "+id(angetData)+" and( c.isPrompt='true' or c.isPrompt='on') return c.role,c.content order by c.createTime desc ");
        if(chatItems.size()>0){
            for(Map<String, Object> ci : chatItems){
                conversation.add(ci);
            }
        }
        conversation.add(m2);

        Map<String, Object> data=aiService.packageMessage(msg, sessionId);

        Map<String, Object> answer=aiService.callAI(data);
        if(answer!=null&&!answer.isEmpty()){
            if(answer.containsKey("content")){
                aiService.addChatItem(sessionId, answer);
                ContextServer.sendInfo(content(answer), chatContextId);
                answer.put("role", "assistant");
                return answer;
            }
        }
        return aiService.localAgent(angetData, conversation);
    }

    @Nullable
    private WrappedResult answerProcess(Long sessionId, String chatContextId, Map<String, Object> answer, Map<String, Object> agentData, String content) throws EvalError{
        if(answer!=null&&(content(answer)!=null||!"error".equals(code(answer)))){
            //解析answer，然后继续走流程。，将answer更新到节点中。或者更新到执行信息中。
            ContextServer.sendInfo("【"+name(agentData)+"】:"+content(answer), chatContextId);
            aiService.addChatItem(sessionId, answer);
            relationService.addRel("chatHistory", id(agentData), id(answer));
            if("MetaDataAgent".equals(code(agentData))){
                //需要用户确认才可以
                Map<String, Object> myContext=getContext(chatContextId);
                //查询元数据是否存在，如存在，将数据库中存在的和新的进行对比，告诉用户，是否需要更新。
                List<Map<String, Object>> existMetaData=neo4jService.listDataBy(LABEL, content, META_DATA);
                if(existMetaData!=null&&existMetaData.size()>0){
                    JSON.toJSONString(existMetaData);
                    String existMsg="已经存在元数据"+JSON.toJSONString(existMetaData.get(0))+"，确定要更新元数据？";
                    if(!userConfirm(myContext, existMsg)){
                        return ResultWrapper.wrapResult(true, content, null, "用户未授权更新元数据");
                    }
                }
                String confirmMsg="确定要创建实体"+content(answer)+"？";

                if(userConfirm(myContext, confirmMsg)){
                    JSONObject jsonObject=JSON.parseObject(content(answer));
                    neo4jService.saveByBody(jsonObject, META_DATA);
                }
            }
            if("JavaEntityAgent".equals(code(agentData))&&content(answer).contains("结果为：")){
                String result=content(answer).split("结果为：")[1].trim().replaceAll("\n", "");
                for(String ei : result.split(",")){
                    ei=ei.replaceAll("\"", "");

                    Map<String, Object> mdi=neo4jService.getAttMapBy(LABEL, ei, META_DATA);
                    if(mdi==null){
                        mdi=neo4jService.getAttMapBy(NAME, ei, META_DATA);
                        if(mdi==null){
                            mdi=neo4jService.getAttMapBy("labels", ei, META_DATA);
                            if(mdi==null){
                                agentProcessMsg("Agent:MetaDataAgent:"+ei, sessionId, chatContextId);
                            }
                        }
                    }
                    //添加系统已经存在的实体信息
                    agentProcessMsg("Agent:MetaDataAgent:"+ei+",已经存在元数据信息，"+JSON.toJSONString(mdi), sessionId, chatContextId);
                }
            }

            if("NounName".equals(code(agentData))&&content(answer).contains("结果为")){
                String ac=content(answer).replace("：", ":");
                String result=ac.split("结果为:")[1].trim().replaceAll("\n", "");
                Map<String, Object> mdi=null;
                for(String ei : result.split(",")){
                    ei=ei.replaceAll("\"", "");
                    ei=ei.replaceAll(" ", "");
                    if(ei.endsWith(".java")){
                        ei=ei.replace(".java", "");
                    }
                    mdi=neo4jService.getAttMapBy(LABEL, ei, META_DATA);
                    if(mdi==null){
                        mdi=neo4jService.getAttMapBy(NAME, ei, META_DATA);
                        if(mdi==null){
                            List<Map<String, Object>> mds=neo4jService.queryBy(buildParam("labels", ei), META_DATA);
                            if(mds!=null&&mds.size()>0){
                                mdi=mds.get(0);
                                break;
                            }
                        }else{
                            break;
                        }
                    }else{
                        break;
                    }
                }
                if(mdi!=null){
                    return WrappedResult.wrap(true, mdi, null, "NounName:识别到实体");
                }
            }
        }
        return null;
    }

    @NotNull
    private static JSONObject packageChatByAgent(Map<String, Object> agent){
        String name=name(agent);
        String code=code(agent);
        String prompt=string(agent, "prompt");
        String duty=string(agent, "duty");
        JSONObject mi=new JSONObject();
        // 这里可以根据sessionID获取会话历史,
        mi.put("role", "system");
        StringBuilder sb=new StringBuilder();
        if(prompt!=null){
            sb.append(prompt);
        }
        if(duty!=null){
            sb.append(duty);
        }

        mi.put("content", sb.toString()+". Follow the user's instructions carefully. "+
                "Do not return data unrelated to the results. ");
        return mi;
    }

    /**
     * 获取上下文，会话信息
     *
     * @param myId
     * @return
     */
    private static Map<String, Object> getContext(String myId){
        ParseExcuteSentence2 pes=(ParseExcuteSentence2) SpringContextUtil.getBean("parseExcuteSentence2");
        Map<String, Object> myContext=pes.getMyContext(myId);
        Object converation=myContext.get(WebSocketDomain.CONVERSATION);
        if(converation!=null){
            conversationMap.put(myId, converation);
        }else{
            if(conversationMap.get(myId)!=null){
                myContext.put(WebSocketDomain.CONVERSATION, conversationMap.get(myId));
            }
        }
        if(myContext==null){
            myContext=new HashMap<>();
        }
        return myContext;
    }

    public boolean userConfirm(Map<String, Object> context, String msg){
        if(string(context, "which")!=null){
            context.remove("which");
        }
        Object converation=context.get(WebSocketDomain.CONVERSATION);
        if(converation!=null){
            LoggerTool.info(logger, "需要用户确认，且用户已连上Websocket");
            ContextServer cs=(ContextServer) converation;
            try{
                List<String> optionList=new ArrayList<>();
                optionList.add("确定");
                optionList.add("取消");
                cs.sendMessage("\n\n\n"+msg+"，请确认：\n<br>"+confirmOptions(optionList)+"\n\n\n");
                Integer selected=integer(context, "which");
                int count=0;
                while(selected==null&&count<50){
                    try{
                        Thread.sleep(1000);
                        LoggerTool.info(logger, "等待用户选择");
                        cs.sendMessage(".");
                        selected=integer(context, "which");
                        count++;
                    }catch(InterruptedException e){
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                context.remove("which");
                if(selected==null){
                    return false;
                }
                return optionList.get(selected).equals("确定");
            }catch(IOException e1){
                e1.printStackTrace();
                LoggerTool.error(logger, e1.getMessage(), e1);
            }
        }else{
            return false;
        }
        return false;
    }

    @NotNull
    private static Map<String, Object> buildParam(String key, String value){
        Map<String, Object> labelQuery=newMap();
        labelQuery.put(key, value);
        return labelQuery;
    }
    private String confirmOptions(List<String> sis){
        StringBuilder sb=new StringBuilder();
        int i=0;
        for(String mi : sis){
            if(sb.length()>0){
                sb.append(" 、 ");
            }
            sb.append("  <button type=‘button' onclick=mySelect('"+i+"') >"+mi+"</button>");
            i++;
        }
        return sb.toString();
    }
}

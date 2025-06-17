package com.wldst.ruder.module.ai.service;

import bsh.EvalError;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.domain.LLMChatDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.module.ws.web.ContextServer;
import com.wldst.ruder.util.RestApi;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.SDKZhipApi;
import com.wldst.ruder.util.WrappedResult;
import com.zhipu.oapi.service.v4.model.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiService extends LLMChatDomain{

    final static Logger debugLog=LoggerFactory.getLogger("debugLogger");
    final static Logger logger=LoggerFactory.getLogger(AiService.class);
    final static Logger resultLog=LoggerFactory.getLogger("reportsLogger");

    private UserAdminService adminService;


    private CrudNeo4jService neo4jService;

    private RelationService relationService;

    private BeanShellService bss;
    private SDKZhipApi zhipApi;
    private JsonToCypher jsonToCypher;
    @Autowired
    private RestApi restApi;
    private static final String chatUrl="http://192.168.3.132:8000/v1/chat/completions";
//    private static final String ollamaChatUrl="http://192.168.3.132:9444/api/chat";

    public boolean isOnline(){
        String llmCallKey=neo4jService.getSettingBy("LLM_CALL_WAY");
        boolean online=llmCallKey!=null&&llmCallKey.equals("online");
        return online;
    }

    public String localChatUrl(){
        String llmCallKey=neo4jService.getSettingBy("ollamaChat");
       return llmCallKey;
    }


    @Autowired
    public AiService(@Lazy UserAdminService adminService,
                     @Lazy CrudNeo4jService neo4jService,
                     @Lazy RelationService relationService,
                     @Lazy BeanShellService bss,
                     @Lazy JsonToCypher jsonToCypher,
                     @Lazy SDKZhipApi zhipApi){
        this.adminService=adminService;
        this.neo4jService=neo4jService;
        this.relationService=relationService;
        this.bss=bss;
        this.zhipApi=zhipApi;
        this.jsonToCypher=jsonToCypher;
    }

    public Map<String, Object> getChatLLMParam(){
        Map<String, Object> param=new HashMap<>();
        param.put("stream", false);
        param.put("max_tokens", 10000);
        param.put("temperature", 0.8);
        param.put("top_p", 0.8);
        String llmCallKey=neo4jService.getSettingBy("LLM_CALL_WAY");
        if(llmCallKey!=null&&llmCallKey.equals("ollama")){
            String model=neo4jService.getSettingBy("model");
            param.put("model", model);
        }else{
            param.put("model", "glm4");
        }
        return param;
    }

    public Map<String, Object> getDeepseekParam(){
        Map<String, Object> param=new HashMap<>();
        param.put("stream", false);
        String llmCallKey=neo4jService.getSettingBy("LLM_CALL_WAY");
        if(llmCallKey!=null&&llmCallKey.equals("ollama")){
            String model=neo4jService.getSettingBy("model");
            param.put("model", model);
        }
        return param;
    }

    /**
     * 包装消息格式并处理会话
     * 该方法主要用于准备聊天数据，根据会话ID包装用户消息和系统消息，
     * 并在必要时创建新的会话。它还根据消息内容的前缀决定消息的类型，并做相应的处理。
     *
     * @param msg 消息内容
     * @param sessionId 会话ID，可能为null
     * @return 包含包装好的消息和会话信息的Map对象
     */
    public Map<String, Object> packageMessage(String msg, Long sessionId){
// 初始化JSON数组用于存储消息
        List<Map<String,Object>> messages=new ArrayList<>();
        if(sessionId!=null){
            // 如果提供了会话ID，则获取并处理该会话下的消息历史
            List<Map<String, Object>> oneRelationList=neo4jService.getOneRelationList(sessionId, "chatHistory");
            for(Map<String, Object> map : oneRelationList){
                messages.add(mapObject(map, RELATION_ENDNODE_PROP));
            }
        }else{
            // 如果没有提供会话ID，则尝试获取当前账户，并根据当前账户创建或获取会话
            String currentAccount=adminService.getCurrentAccount();
            List<Map<String, Object>> listAttMapBy=neo4jService.listAttMapBy("userId", currentAccount, "ChatSession");
            if(listAttMapBy.isEmpty()){
                Map<String, Object> chatSession=new HashMap<>();
                chatSession.put("userid", currentAccount);
                chatSession.put("title", msg);
                Node save=neo4jService.save(chatSession, "ChatSession");
                sessionId=save.getId();
            } /*else {
	      Map<String, Object> map = listAttMapBy.get(0);
	      sessionId = id(map);
	      List<Map<String, Object>> oneRelationList = neo4jService.getOneRelationList(sessionId, "chatHistory");
	      messages.addAll(oneRelationList);
	      }*/
        }
        // 获取聊天参数
        Map<String, Object> data=getChatLLMParam();
        // 创建一个新的消息对象
        JSONObject m2=new JSONObject();

        m2.put("role", "user");

        if(!msg.startsWith("查询")&&msg.endsWith("的元数据")){
            Map<String, Object> xx=new HashMap<>();
            xx.put("code", "metaData");
            String content=renderPrompt(msg, "metaData");
            m2.put("content", content.replaceAll("\n", ""));
        }
        if(msg.startsWith("查询")){
            if(msg.startsWith("查询所有的")){
                String content=renderPrompt(msg, "listAllData");
                m2.put("content", content.replaceAll("\n", ""));
            }else if(msg.endsWith("的元数据。")||msg.endsWith("的元数据")){
                String content=renderPrompt(msg, "queryMetaData");
                m2.put("content", content.replaceAll("\n", ""));
            }else{
                String content=renderPrompt(msg, "queryData");
                m2.put("content", content.replaceAll("\n", ""));
            }
        }else if(msg.startsWith("保存")){
            String content=renderPrompt(msg, "saveData");
            m2.put("content", content.replaceAll("\n", ""));
        }else if(msg.startsWith("删除")){
            String content=renderPrompt(msg, "delData");
            m2.put("content", content.replaceAll("\n", ""));
        }else{
            m2.put("content", msg.replaceAll("\n", ""));
        }

        if(!isOnline()){
            if(messages.size()<1){
                JSONObject mi=new JSONObject();
                // 这里可以根据sessionID获取会话历史,
                mi.put("role", "system");
                mi.put("content",
                        "You are GLM4, a large language model. Follow the user's instructions carefully. Respond using markdown.");
                List<Map<String, Object>> queryBy=neo4jService.queryBy(mi, "ChatItem");
                if(queryBy!=null&&!queryBy.isEmpty()){
                    relationService.addRel("chatHistory", sessionId, id(queryBy.get(0)));
                }
                messages.add(mi);
            }
        }

        boolean duplicate=false;
        Set<String> contentSet=new HashSet<>();
        List<Map<String,Object>> distinctMsg=new ArrayList<>();
        for(Map<String,Object> mi: messages){
           String miContent = content(mi);
            String role = string(mi, "role");
            if("user".equals(role)&&miContent!=null&&miContent.equals(content(m2))){
                duplicate=true;
            }else{
                if(!contentSet.contains(miContent)&&!miContent.startsWith("由于")&&!miContent.contains("没有提供")){
                    contentSet.add(miContent);
                    distinctMsg.add(mi);
                }
            }
        }
        distinctMsg.add(m2);
        if(duplicate){
            addChatItem(sessionId, m2);
        }
//        addFunctionCall(data);
        data.put("sessionId", sessionId);
        data.put("messages", distinctMsg);
        return data;
    }

    /**
     * 包装消息格式并处理会话
     * 该方法主要用于准备聊天数据，根据会话ID包装用户消息和系统消息，
     * 并在必要时创建新的会话。它还根据消息内容的前缀决定消息的类型，并做相应的处理。
     *
     * @param msg 消息内容
     * @param sessionId 会话ID，可能为null
     * @return 包含包装好的消息和会话信息的Map对象
     */
    public Map<String, Object> taskMessage(String msg, Long sessionId,String content){
// 初始化JSON数组用于存储消息
        JSONArray messages=new JSONArray();
        if(sessionId!=null){
            // 如果提供了会话ID，则获取并处理该会话下的消息历史
            List<Map<String, Object>> oneRelationList=neo4jService.getOneRelationList(sessionId, "chatHistory");
            for(Map<String, Object> map : oneRelationList){
                messages.add(mapObject(map, RELATION_ENDNODE_PROP));
            }
        }else{
            // 如果没有提供会话ID，则尝试获取当前账户，并根据当前账户创建或获取会话
            String currentAccount=adminService.getCurrentAccount();
            List<Map<String, Object>> listAttMapBy=neo4jService.listAttMapBy("userId", currentAccount, "ChatSession");
            if(listAttMapBy.isEmpty()){
                Map<String, Object> chatSession=new HashMap<>();
                chatSession.put("userid", currentAccount);
                chatSession.put("title", msg);
                Node save=neo4jService.save(chatSession, "ChatSession");
                sessionId=save.getId();
            } /*else {
	      Map<String, Object> map = listAttMapBy.get(0);
	      sessionId = id(map);
	      List<Map<String, Object>> oneRelationList = neo4jService.getOneRelationList(sessionId, "chatHistory");
	      messages.addAll(oneRelationList);
	      }*/
        }
        // 获取聊天参数
        Map<String, Object> data=getChatLLMParam();
        // 创建一个新的消息对象
        JSONObject m2=new JSONObject();

        m2.put("role", "user");

        m2.put("content", content.replaceAll("\n", ""));

        if(!isOnline()){
            if(messages.size()<1){
                JSONObject mi=new JSONObject();
                // 这里可以根据sessionID获取会话历史,
                mi.put("role", "system");
                mi.put("content",
                        "You are GLM4, a large language model. Follow the user's instructions carefully. Respond using markdown.");
                List<Map<String, Object>> queryBy=neo4jService.queryBy(mi, "ChatItem");
                if(!queryBy.isEmpty()){
                    relationService.addRel("chatHistory", sessionId, id(queryBy.get(0)));
                }
                messages.add(mi);
            }
        }

        messages.add(m2);
        addChatItem(sessionId, m2);
//        addFunctionCall(data);
        data.put("sessionId", sessionId);
        data.put("messages", messages);
        return data;
    }

    /**
     * 根据参数，读取模版，并渲染为提示词
     * @param msg
     * @param xx
     * @return
     */
    @NotNull
    public String renderPrompt(String msg, String opt){
        Map<String, Object> xx=new HashMap<>();
        xx.put("code", opt);
        Map<String, Object> prompt=neo4jService.queryBy(xx, "PromptTemplate").get(0);
        String content=content(prompt);
        content=content.replace("${UserInput}", msg.replaceAll("\n", ""));
        return content;
    }


    @Nullable
    public Map<String, Object> localAI(Map<String, Object> currentTask, Map<String, Object> param){
        Map<String, Object> answer;
        try{
            Map postForObject=restApi.postForObject(chatUrl, param, Map.class);

            Map<String, Object> datax=(Map<String, Object>) postForObject;
            Map<String, Object> choices=listMapObject(datax, "choices").get(0);
            answer=mapObject(choices, "message");
            return answer;
        }catch(Exception e){
            answer=newMap();
            answer.put("code", "error");
            answer.put("content", string(currentTask, "title")+"智能体调用失败，请检查智能体是否在线");
            return answer;
        }
    }

    public Map<String, Object> localAI(String msg, Map<String, Object> param){
        Map<String, Object> answer;
        try{
            Map postForObject=restApi.postForObject(chatUrl, param, Map.class);

            Map<String, Object> datax=(Map<String, Object>) postForObject;
            Map<String, Object> choices=listMapObject(datax, "choices").get(0);
            answer=mapObject(choices, "message");
            return answer;
        }catch(Exception e){
            answer=newMap();
            answer.put("code", "error");
            answer.put("content", msg+"智能体调用失败，请检查智能体是否在线");
            return answer;
        }
    }

    public Map<String, Object> onlineCallLLM(String msg){
        String remoteGlmServer=neo4jService.getSettingBy("REMOTE_GLM_CALL");
        Map<String, Object> paramx=new HashMap<>();
        paramx.put("query", msg);
        Map<String, Object> answer=restApi.postForObject(remoteGlmServer, paramx, Map.class);
        return answer;
    }

    @Nullable
    public Map<String, Object> localAgent(Map<String, Object> angetData, JSONArray conversation){
        Map<String, Object> param=getChatLLMParam();
        param.put("messages", conversation);
        //判断chatUrl地址是否正常
        try{
            Map postForObject=restApi.postForObject(chatUrl, param, Map.class);

            Map<String, Object> datax=(Map<String, Object>) postForObject;
            Map<String, Object> choices=listMapObject(datax, "choices").get(0);
            Map<String, Object> answer=mapObject(choices, "message");
            return answer;
        }catch(Exception e){
            logger.error("localAgent error", e);
            Map<String, Object> answer=onlineCallLLM(param);

            if(answer!=null&&!answer.isEmpty()){
                if(answer.containsKey("content")){
                    answer.put("role", "assistant");
                    return answer;
                }
            }else{
                answer=newMap();
                answer.put("code", "error");
                answer.put("content", name(angetData)+"智能体调用失败，请检查智能体是否在线");
            }
            return answer;
        }
    }

    private Map<String, Object> onlineCallLLM(List<Map<String, Object>> msg){
        String remoteGlmServer=neo4jService.getSettingBy("REMOTE_GLM_CALL");
        Map<String, Object> paramx=new HashMap<>();
        paramx.put("messages", msg);
        Map<String, Object> answer=restApi.postForObject(remoteGlmServer, paramx, Map.class);
        return answer;
    }

    public Map<String, Object> onlineCallLLM(Map<String, Object> paramx){
        List<ChatMessage> chatMessages=zhipApi.llmCall(paramx);
        if(chatMessages!=null&&chatMessages.size()>0){
            Map<String, Object> answer=new HashMap<>();
            ChatMessage chatMessage=chatMessages.get(chatMessages.size()-1);
            answer.put(LLMChatDomain.ROLE, chatMessage.getRole());
            answer.put(CONTENT, chatMessage.getContent());
            return answer;
        }
        String remoteGlmServer=neo4jService.getSettingBy("REMOTE_GLM_CALL").trim();
        try{
            Map<String, Object> answer=restApi.postForObject(remoteGlmServer, paramx, Map.class);
            return answer;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> ollamaCallLLM(Map<String, Object> paramx){
        String ollamaChatServer=neo4jService.getSettingBy("ollamaChat").trim();
        String ollamaModel=neo4jService.getSettingBy("ollamaModel").trim();
        paramx.put("model", ollamaModel);
        logger.info("ollamaChatServer:"+jsonString(paramx));

        try{
            Map<String, Object> answer=restApi.postForObject(ollamaChatServer, paramx, Map.class);
            return answer;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public WrappedResult callAI(Long sessionId, Map<String, Object> data, String myId){
        WrappedResult result=null;
        String llmCallKey=neo4jService.getSettingBy("LLM_CALL_WAY");
        if(llmCallKey!=null&&llmCallKey.equals("online")){
            Map<String, Object> answer=onlineCallLLM(data);
            if(answer!=null&&!answer.isEmpty()){
                if(answer.containsKey("content")){
                    addChatItem(sessionId, answer);
                    ContextServer.sendInfo(content(answer), myId);
                    answer.put("role", "assistant");
                    return ResultWrapper.wrapResult(true, answer, null, EXECUTE_SUCCESS);
                }
            }
            result= ResultWrapper.wrapResult(false, answer, null, EXECUTE_FAILED);
        }else if(llmCallKey!=null&&llmCallKey.equals("ollama")){
            Map<String, Object> answer= ollamaCallLLM(data);
            if(answer!=null&&!answer.isEmpty()){
                answer=mapObject(answer, "message");
                if(answer.containsKey("content")){
                    StringBuilder stringBuilder = jsonToCypher.parseJsonToCypher(content(answer));
                    if(stringBuilder!=null){
                        answer.put("content", stringBuilder.toString());
                    }
                    addChatItem(sessionId, answer);
                    ContextServer.sendInfo(content(answer), myId);
                    answer.put("role", "assistant");
                    result=  ResultWrapper.wrapResult(true, answer, null, EXECUTE_SUCCESS);
                }
            }
        }else{//                    本地调用
            result=  loaclAI(data, sessionId);
        }
        return  result;
    }


    public Map<String, Object> callAI(Map<String, Object> data){
        Map<String, Object> answer=null;
        String llmCallKey=neo4jService.getSettingBy("LLM_CALL_WAY");
        String model=neo4jService.getSettingBy("model");
        if(llmCallKey!=null&&llmCallKey.equals("online")){
            answer=onlineCallLLM(data);
        }else if(llmCallKey!=null&&llmCallKey.equals("ollama")){
            model=neo4jService.getSettingBy("ollamaModel");
            if(model==null||"".equals(model)){
                data.put("model","glm4:latest");
            }else{
                data.put("model",model);
            }
            answer= ollamaCallLLM(data);
        }else{//                    本地调用
            answer=  loaclAnswer(data);
        }
        return  answer;
    }

    public String callAIByMsg(String msg){
        return callAIByMsg(msg, null);
    }
    public String callAIByMsg(String msg,Long sessionId){
        Map<String, Object> data=packageMessage(msg, sessionId);

        Map<String, Object> answer=callAI(data);
        if(sessionId==null) {
            sessionId = longValue(data, "sessionId");
        }

        if(answer!=null&&!answer.isEmpty()){
            if(answer.containsKey("content")){
                addChatItem(sessionId, answer);
                answer.put("role", "assistant");
                return content(answer);
            }
        }

        return content(answer);
    }



    public void addFunctionCall(Map<String, Object> data){
        List<Map<String, Object>> queryBy=neo4jService.listAllByLabel("FunctionCall");
        JSONArray functions=new JSONArray();
        for(int i=0; i<queryBy.size(); i++){
            Map<String, Object> fci=queryBy.get(i);
            if(fci.get("enable")==null||!bool(fci, "enable").equals(true)){
                continue;
            }
            String content=content(fci);
            if(name(fci)==null||content==null){
                continue;
            }
            functions.add(JSON.parseObject(content.replaceAll("\n", "")));
        }
        data.put("functions", functions);
    }

    /**
     * {'name': 'add_Label', 'description': '新增，添加xxx',
     * 'parameters': {
     * 'type': 'object',
     * 'properties':
     * {
     * 'id':{'description': '元数据编码：为空新增，不为空为更新'},
     * 'name':{'description': '元数据名称'},
     * 'label':{'description': '标签，Node的Label'},
     * 'columns':{'description': '列，节点的属性定义，英文字母，多个属性，逗号隔开'},
     * 'header':{'description': '表头，属性的中文名，多个逗号隔开'},
     * 'show':{'description': '可视化字段，展现的字段，来源columns，多个逗号'},
     * 'shortShow':{'description': '简要展现的字段，来源columns，多个逗号'},
     * 'searchColumn':{'description': '模糊查询的字段，来源columns，多个逗号'}
     * },
     * 'required': ['label','operate','vo']}}
     *
     * @param metaData
     */
    public JSONArray crudFunctionCall(Map<String, Object> metaData){
        JSONArray functions=new JSONArray();
        functions.add(addFunction(metaData));
        functions.add(queryFunction(metaData));
        functions.add(delFunction(metaData));
        functions.add(getFunction(metaData));
        functions.add(countFunction(metaData));
        return functions;
    }

    private static JSONObject delFunction(Map<String, Object> metaData){
        Map<String, String> colName=colName(metaData);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("name", "del_"+label(metaData));
        jsonObject.put("description", "删除"+name(metaData)+"数据");

        JSONObject parameters=new JSONObject();
        parameters.put("type", "object");
        JSONObject perperties=new JSONObject();
        for(String ki : colName.keySet()){
            JSONObject coli=new JSONObject();
            coli.put("description", colName.get(ki));
            perperties.put(ki, coli);
        }
        parameters.put("properties", perperties);
        jsonObject.put("parameters", parameters);
        jsonObject.put("required", "['id']");
        return jsonObject;
    }

    private static JSONObject countFunction(Map<String, Object> metaData){
        Map<String, String> colName=colName(metaData);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("name", "count_"+label(metaData));
        jsonObject.put("description", "根据条件，统计"+name(metaData)+"数据");

        JSONObject parameters=new JSONObject();
        parameters.put("type", "object");
        JSONObject perperties=new JSONObject();
        for(String ki : colName.keySet()){
            JSONObject coli=new JSONObject();
            coli.put("description", colName.get(ki));
            perperties.put(ki, coli);
        }
        parameters.put("properties", perperties);
        jsonObject.put("parameters", parameters);
        jsonObject.put("required", "['id']");
        return jsonObject;
    }

    private static JSONObject getFunction(Map<String, Object> metaData){
        Map<String, String> colName=colName(metaData);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("name", "get_"+label(metaData));
        jsonObject.put("description", "根据条件，获取"+name(metaData)+"数据");

        JSONObject parameters=new JSONObject();
        parameters.put("type", "object");
        JSONObject perperties=new JSONObject();
        for(String ki : colName.keySet()){
            JSONObject coli=new JSONObject();
            coli.put("description", colName.get(ki));
            perperties.put(ki, coli);
        }
        parameters.put("properties", perperties);
        jsonObject.put("parameters", parameters);
        jsonObject.put("required", "['id']");
        return jsonObject;
    }

    private static JSONObject queryFunction(Map<String, Object> metaData){
        Map<String, String> colName=colName(metaData);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("name", "query_"+label(metaData));
        jsonObject.put("description", "根据参数查询"+name(metaData)+"数据");
        JSONObject parameters=new JSONObject();
        parameters.put("type", "object");
        JSONObject perperties=new JSONObject();
        for(String ki : colName.keySet()){
            if(ki.equals("id")){
                continue;
            }
            JSONObject coli=new JSONObject();
            coli.put("description", colName.get(ki));
            perperties.put(ki, coli);
        }
        parameters.put("properties", perperties);
        jsonObject.put("parameters", parameters);
        return jsonObject;
    }

    private static JSONObject addFunction(Map<String, Object> metaData){
        String[] columns=columns(metaData);

        String[] headers=headers(metaData);
        Map<String, String> colName=colName(metaData);
        Set<String> requiredColumn=splitValue2Set(metaData, "requiredColumn");
        Set<String> shortShow=splitValue2Set(metaData, "shortShow");

        String requiredCol=null;
        if(requiredColumn!=null&&requiredColumn.size()>0){
            requiredCol=arrayString(requiredColumn);
        }else if(shortShow!=null&&shortShow.size()>0){
            requiredCol=arrayString(shortShow);
        }else{
            requiredCol=arrayString(columns);
        }

        JSONObject jsonObject=new JSONObject();
        jsonObject.put("name", "add_"+label(metaData));
        jsonObject.put("description", "新增，添加，修改"+name(metaData)+"数据");
        JSONObject parameters=new JSONObject();
        parameters.put("type", "object");
        JSONObject perperties=new JSONObject();
        for(String ki : colName.keySet()){
            JSONObject coli=new JSONObject();
            coli.put("description", colName.get(ki));
            perperties.put(ki, coli);
        }
        parameters.put("properties", perperties);
        jsonObject.put("parameters", parameters);
        jsonObject.put("required", requiredCol);
        return jsonObject;
    }

    public void addChatItem(Long sessionId, Map<String, Object> answer){
        Node save=neo4jService.save(answer, "ChatItem");
        relationService.addRel("chatHistory", sessionId, save.getId());
    }


    public void processToolCall(ToolCalls toolCall, Map<String, Map<String, Object>> functionCallMap, List<ChatMessage> messages, String toolCallId){
        //                            System.out.println("tool_call_id: " + toolCall.getId());
        ChatFunctionCall function=toolCall.getFunction();
        String functionName=function.getName();
//                            System.out.println("function_call: " +functionName);
//                            System.out.println("function_arguments: " + function.getArguments());
        JSONObject param=JSON.parseObject(JSON.toJSONString(function.getArguments()));
        String dataLabel=functionName.split("_")[1];
        if(dataLabel==null){
            dataLabel=label(param);
        }

        Map<String, Object> vo=mapObject(param, "vo");
        Object retData=null;
        if(dataLabel!=null){
            retData=functionHasLabel(dataLabel, param, functionName);
        }else{
            String cypher=cypher(param);
            if(cypher!=null&&cypher.length()>0){
                if(functionName.equals("query_by_cypher")){
                    retData=neo4jService.cypher(cypher);
                }
            }else{
                Map<String, Object> funci=functionCallMap.get(functionName);
                String cypherQuery="MATCH(n)-[r:call]->(m) where id(n)="+id(funci)+" return id(m) AS id,labels(m) AS mlabel";

                List<Map<String, Object>> cypherData=neo4jService.cypher(cypherQuery);
                Map<String, Object> callMap=cypherData.get(0);
                String mLablel=oneLabel(callMap, "mlabel");

                if(mLablel.equals("BeanShell")){
                    try{
                        retData=bss.runShell(stringId(callMap), vo);
                    }catch(EvalError e){
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if(retData!=null){
            messages.add(new ChatMessage("tool", JSON.toJSONString(retData),
                    functionName, null, toolCallId));
        }
    }

    public List<ChatTool> functionData(List<Map<String, Object>> functionCall){
        List<ChatTool> chatToolList=new ArrayList();
        if(functionCall!=null&&functionCall.size()>0){
            for(Map<String, Object> fci : functionCall){
                if(content(fci)==null){
                    continue;
                }
                ChatTool chatTool=new ChatTool();
                chatTool.setType(ChatToolType.FUNCTION.value());
                ChatFunctionParameters chatFunctionParameters=new ChatFunctionParameters();
                chatFunctionParameters.setType("object");
                Map<String, Object> properties=new HashMap();
                JSONObject jo=JSON.parseObject(content(fci));
                JSONObject parameters=json(jo, "parameters");
                if(parameters!=null){
                    JSONObject properties2=json(parameters, "properties");
                    for(String ki : properties2.keySet()){
                        properties.put(ki, mapObject(parameters, ki));
                    }
                }

                List<String> required=listString(jo, "required");
                chatFunctionParameters.setProperties(properties);
                ChatFunction chatFunction=ChatFunction.builder().name(name(jo)).description(description(jo)).parameters(chatFunctionParameters).required(required).build();
                chatTool.setFunction(chatFunction);
                chatToolList.add(chatTool);
            }
        }
        return chatToolList;
    }

    public Object functionHasLabel(String dataLabel, JSONObject param, String functionName){
        Object retData=null;
        Map<String, Object> vo=mapObject(param, "vo");
        Map<String, Object> parameter=validateParam(dataLabel, vo);

        List<Map<String, Object>> metaList=new ArrayList<>();
        if(dataLabel.equals(META_DATA)){
            metaList=neo4jService.queryBy(param, META_DATA);
        }
        String operate=functionName.split("_")[0];
        switch(operate){
            case "query":
                retData=neo4jService.queryBy(vo, dataLabel);
                break;
            case "save":
                retData=neo4jService.save(parameter, dataLabel);
                break;
            case "list":
                if(dataLabel.equals(META_DATA)){
                    retData=neo4jService.listDataByLabel(META_DATA, vo);
                }else{
                    retData=neo4jService.listDataByLabel(dataLabel, vo);
                }
                break;
            case "get":
                retData=neo4jService.getOne(stringId(parameter));
                break;
            case "delete":
                neo4jService.delete(id(parameter));
                break;
//            case "count":
//                neo4jService.count(id(parameter));
//                break;
        }


        /*if(functionName.equals("operate_metadata")){
            Map<String, Object> parameter=validateParam(META_DATA, vo);
            String operate=string(param, "operate");
            if(operate!=null){
                switch(operate){
                    case "save":
                        retData=neo4jService.save(parameter, META_DATA);
                        break;
                    case "get":
                        retData=getMetaData(dataLabel);
                        break;
                    case "query":
                        retData=neo4jService.listDataByLabel( META_DATA,parameter);
                        break;
                    case "delete":
                        neo4jService.delete(id(parameter));
                        break;
                }
            }else{
                logger.info("operate is null");
            }
        }else if(functionName.equals("operate_data")){
            Map<String, Object> parameter=validateParam(dataLabel, vo);
            String operate=string(param, "operate");
            if(operate!=null){
                switch(operate){
                    case "save":
                        retData=neo4jService.save(parameter, dataLabel);
                        break;
                    case "get":
                        retData=neo4jService.getOne(stringId(parameter));
                        break;
                    case "listAll":
                        retData=neo4jService.save(parameter, dataLabel);
                        break;
                    case "query":
                        retData=neo4jService.listDataByLabel(dataLabel,parameter);
                        break;
                    case "delete":
//                        neo4jService.deleteBy(vo, dataLabel);
                        break;
                }
            }else {
                logger.info("operate is null");
            }
        }*/
        if(!functionName.startsWith("del")&&retData==null){
            Map<String, Object> paramData=new HashMap<>();
            paramData.put("name", param.getString("name"));
            retData=neo4jService.cypher(cypher(vo));
        }
        return retData;
    }

    public Object getMetaData(String dataLabel){
        Object retData=neo4jService.getAttMapBy(LABEL, dataLabel, META_DATA);
        if(retData==null){
            retData=neo4jService.getAttMapBy(NAME, dataLabel, META_DATA);
        }
        if(retData==null){
            Map<String, Object> map=new HashMap<>();
            map.put("name", dataLabel);
            List<Map<String, Object>> mds=neo4jService.listDataByLabel(META_DATA, map);
            for(Map<String, Object> m : mds){
                if(name(m).equals(dataLabel)||label(m).equals(dataLabel)||string(m, "labels").contains(dataLabel)){
                    retData=m;
                    break;
                }
            }
        }
        if(retData==null){
            Map<String, Object> map=new HashMap<>();
            map.put("labels", dataLabel);
            retData=neo4jService.listDataByLabel(META_DATA, map);
        }
        return retData;
    }

    @NotNull
    public Map<String, Object> validateParam(String dataLabel, Map<String, Object> vo){
        Map<String, Object> md=neo4jService.getAttMapBy(LABEL, dataLabel, META_DATA);
        Map<String, String> colName=colName(md);
        Map<String, String> nameColMap=nameColumn(md);
        Map<String, Object> parameter=new HashMap<>();
        //参数校验
        for(String ki : vo.keySet()){
            Object value=vo.get(ki);
            if(value!=null&&!"".equals(value)){
                if(colName.containsKey(ki)){
                    parameter.put(ki, value);
                }else if(nameColMap.containsKey(ki)){
                    parameter.put(nameColMap.get(ki), value);
                }
            }
        }
        return parameter;
    }

    public Map<String, Object> loaclAnswer(Map<String, Object> data){
        try{
            Object postForObject=restApi.postForObject(chatUrl, data, Object.class);
            Map<String, Object> datax=(Map<String, Object>) postForObject;
            Map<String, Object> choices=listMapObject(datax, "choices").get(0);
            return mapObject(choices, "message");
        }catch(Exception e){
            //使用在线的智能体chatGLM远程
            Map<String, Object> answer=onlineCallLLM(data);
            if(answer!=null&&!answer.isEmpty()){
                if(answer.containsKey("content")){
                    answer.put("role", "assistant");
                }
            }
            return answer;
        }
    }

    @NotNull
    public Long getSessionId(String myId, String msg){
        Map<String, Object> map=new HashMap<>();
        map.put("userId", myId);
        map.put("title", msg);
        Node save=neo4jService.save(map, "ChatSession");
        Long sessionId=save.getId();
        return sessionId;
    }

    @NotNull
    public WrappedResult loaclAI(Map<String, Object> data, Long sessionId){
        try{
            Object postForObject=restApi.postForObject(chatUrl, data, Object.class);
            Map<String, Object> datax=(Map<String, Object>) postForObject;
            Map<String, Object> choices=listMapObject(datax, "choices").get(0);
            Map<String, Object> answer=mapObject(choices, "message");
            addChatItem(sessionId, answer);
            return ResultWrapper.wrapResult(true, content(answer), null, EXECUTE_SUCCESS);
        }catch(Exception e){
            //使用在线的智能体chatGLM远程
            Map<String, Object> answer=onlineCallLLM(data);
            if(answer!=null&&!answer.isEmpty()){
                if(answer.containsKey("content")){
                    addChatItem(sessionId, answer);
                    answer.put("role", "assistant");
                    return ResultWrapper.wrapResult(true, answer, null, EXECUTE_SUCCESS);
                }
            }
            return ResultWrapper.wrapResult(false, e.getMessage(), null, EXECUTE_FAILED);
        }
    }

}

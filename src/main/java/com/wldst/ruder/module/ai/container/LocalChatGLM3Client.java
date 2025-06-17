package com.wldst.ruder.module.ai.container;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.RestApi;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class LocalChatGLM3Client extends MapTool implements IBigModel {
    @Autowired
    protected UserAdminService adminService;
    @Autowired
    protected RelationService relationService;
    @Autowired
    protected CrudNeo4jService neo4jService;
    private static final String chatUrl = "http://192.168.3.132:8000/v1/chat/completions";
    @Autowired
    private RestApi restApi;

    @Override
    public String callModel(String  agentId,String input) {
        return callModel(agentId,input, null);
    }

    @Override
    public String callModel(String  agentId,String input, List<Map<String, Object>> chatItems) {
        // 实现调用大模型的逻辑
        //决定用什么智能体？，使用智能体判断，当前智能体是什么？
        Map<String, Object> agentData = neo4jService.getAttMapBy(NAME, agentId.trim(), "Agent");
        // 可能需要网络请求、API调用等
        packageChatByAgent(agentData);
        JSONObject userChat = new JSONObject();
        userChat.put("role", "user");
        String content = input.replaceAll("\n", "");
        userChat.put("content", content);
        Map<String, Object> answer = agentWork(userChat, agentData, chatItems);
        if(answer==null||answer.get("content")==null){
            return code(answer);
        }
        return content(answer);
    }
    public static Map<String, Object> getChatLLMParam() {
        Map<String, Object> param = new HashMap<>();
        param.put("stream", false);
        param.put("max_tokens", 10000);
        param.put("temperature", 0.8);
        param.put("top_p", 0.8);
        param.put("model", "chatglm3-6b");
        return param;
    }

    public Map<String, Object> packageMessage(String msg, Long sessionId) {

        JSONArray messages = new JSONArray();
        if (sessionId != null) {
            List<Map<String, Object>> oneRelationList = neo4jService.getOneRelationList(sessionId, "chatHistory");
            messages.addAll(oneRelationList);
        } else {
            String currentAccount = adminService.getCurrentAccount();

        }

        Map<String, Object> data = getChatLLMParam();

        JSONObject m2 = new JSONObject();

        m2.put("role", "user");



        if (!msg.startsWith("查询") && msg.endsWith("的元数据")) {
            Map<String, Object> xx = new HashMap<>();
            xx.put("code", "metaData");
            Map<String, Object> prompt = neo4jService.queryBy(xx, "PromptTemplate").get(0);
            String content = content(prompt);
            content = content.replace("${UserInput}", msg.replaceAll("\n", ""));
            m2.put("content", content.replaceAll("\n", ""));
        }
        if (msg.startsWith("查询")) {
            if (msg.startsWith("查询所有的")) {
                Map<String, Object> xx = new HashMap<>();
                xx.put("code", "listAllData");
                Map<String, Object> prompt = neo4jService.queryBy(xx, "PromptTemplate").get(0);
                String content = content(prompt);
                content = content.replace("${UserInput}", msg.replaceAll("\n", ""));
                m2.put("content", content.replaceAll("\n", ""));
            } else if (msg.endsWith("的元数据。") || msg.endsWith("的元数据")) {
                Map<String, Object> xx = new HashMap<>();
                xx.put("code", "queryMetaData");
                Map<String, Object> prompt = neo4jService.queryBy(xx, "PromptTemplate").get(0);
                String content = content(prompt);
                content = content.replace("${UserInput}", msg.replaceAll("\n", ""));
                m2.put("content", content.replaceAll("\n", ""));
            } else {
                Map<String, Object> xx = new HashMap<>();
                xx.put("code", "queryData");
                Map<String, Object> prompt = neo4jService.queryBy(xx, "PromptTemplate").get(0);
                String content = content(prompt);
                content = content.replace("${UserInput}", msg.replaceAll("\n", ""));
                m2.put("content", content.replaceAll("\n", ""));
            }
        } else if (msg.startsWith("保存")) {
            Map<String, Object> xx = new HashMap<>();
            xx.put("code", "saveData");
            Map<String, Object> prompt = neo4jService.queryBy(xx, "PromptTemplate").get(0);
            String content = content(prompt);
            content = content.replace("${UserInput}", msg.replaceAll("\n", ""));
            m2.put("content", content.replaceAll("\n", ""));
        } else if(msg.startsWith("删除")) {
            Map<String,Object> xx=new HashMap<>();
            xx.put("code","saveData");
            Map<String, Object> prompt = neo4jService.queryBy(xx, "PromptTemplate").get(0);
            String content = content(prompt);
            content =content.replace("${UserInput}",msg.replaceAll("\n", ""));
            m2.put("content",content.replaceAll("\n", ""));
        }else {
            m2.put("content", msg.replaceAll("\n", ""));
        }
        if (messages.size() < 1) {
            JSONObject mi = new JSONObject();
            // 这里可以根据sessionID获取会话历史,
            mi.put("role", "system");
            mi.put("content",
                    "You are ChatGLM3, a large language model trained by Zhipu.AI. Follow the user's instructions carefully. Respond using markdown.");
            List<Map<String, Object>> queryBy = neo4jService.queryBy(mi, "ChatItem");
            relationService.addRel("chatHistory", sessionId, id(queryBy.get(0)));
            messages.add(mi);
        }

        messages.add(m2);
        addChatItem(sessionId, m2);

        addFunctionCall(data);

        data.put("messages", messages);

        return data;
    }


    private void addFunctionCall(Map<String, Object> data) {
        List<Map<String, Object>> queryBy = neo4jService.listAllByLabel("FunctionCall");
        JSONArray functions = new JSONArray();
        for (int i = 0; i < queryBy.size(); i++) {
            Map<String, Object> fci = queryBy.get(i);
            functions.add(JSON.parseObject(content(fci).replaceAll("\n", "")));
        }
        data.put("functions", functions);
    }

    public void addChatItem(Long sessionId, Map<String, Object> answer) {
        Node save = neo4jService.save(answer, "ChatItem");
        relationService.addRel("chatHistory", sessionId, save.getId());
    }
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

    private Map<String, Object> agentWork( JSONObject m2, Map<String, Object> angetData, List<Map<String, Object>> chatItems) {
        JSONObject agent = packageChatByAgent(angetData);
        if (agent == null) {
            return null;
        }
        JSONArray conversation = new JSONArray();
        conversation.add(agent);
        //获取当前Agent的历史会话信息
       if(chatItems.size()>0){
            for(Map<String, Object> ci: chatItems){
                conversation.add(ci);
            }
       }
       conversation.add(m2);
       Map<String, Object> param=getChatLLMParam();
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

}
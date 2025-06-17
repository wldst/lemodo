//package com.wldst.ruder.module.ai.service;
//
//import com.alibaba.fastjson2.JSON;
//import com.alibaba.fastjson2.JSONObject;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.wldst.ruder.crud.service.CrudNeo4jService;
//import com.wldst.ruder.util.MapTool;
//import org.jetbrains.annotations.NotNull;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.ai.model.ModelOptionsUtils;
//import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
//import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
//import org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatCompletionMessage.Role;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static cn.hutool.http.HttpUtil.post;
//import static com.wldst.ruder.util.MapTool.*;
//import static org.springframework.ai.zhipuai.api.ZhiPuAiApi.ChatModel.GLM_4;
//
//
//@Service
//public class FunctionCallService{
//    private final Logger logger = LoggerFactory.getLogger(FunctionCallService.class);
//    @Autowired
//    private CrudNeo4jService neo4jService;
//    @Autowired
//    private ZhiPuAiApi zhipuAiApi;
//    @Autowired
//    private ZhiPuAiEmbeddingModel zhiPuEmbed;
//
//    @PostMapping("/register")
//    public String registerFunctionCall(@RequestParam String functionName) {
////        functionCallService.registerFunctionCall(functionName);
//        return "FunctionCall registered successfully";
//    }
//
//
//    /**
//     * 大模型做一个查询功能，输入一个查询语句，大模型判断该语句是否是查询语句
//     * 是查询语句，识别语句中的实体，然后根据实体，映射上元数据。
//     * 将查询语句翻译为Cypher语句。
//     * ，然后根据查询语句，查询出相关的数据，然后返回给用户。
//     * @return
//     */
//    @PostMapping("/funcall2")
//    public String functionCall(@RequestBody Map<String,Object> params) {
//       String query =  string(params,"query");
//        // Step 1: send the conversation and available functions to the model
//        neo4jService.listAllByLabel("");
//
//        List<Map<String, Object>> scene=neo4jService.listAllByLabel("Scene");
////        用户查询，常用场景的函数设置。
//        //判断场景，更具用户输入，判断,
//        var userAsk =userMessage(query);
//        //识别意图，根据意图获取函数列表。
//        //获取用户信息，部门信息，岗位信息，角色信息，判断该用户拥有的权限，角色，可以调用哪些函数调用。
//
////        for( Map<String, Object> sc: scene){
////            JSONObject scContent= JSON.parseObject(MapTool.content(sc));
////            if(scContent.getString("name").equals(userAsk.content())){
////                for( Map<String, Object> fci: functionCall){
////                    JSONObject fciContent= JSON.parseObject(MapTool.content(fci));
////                    functionToolList.add(getFunctionTool(fciContent.getString("description"), fciContent.getString("name"), fciContent.getString("parameters")));
////                }
////            }
////        }
////        if(functionToolList.size()==0){
////            return "没有找到场景，请重新输入";
////        }
//        //进入场景，提供场景下的函数，场景下输入数据，问答
//        List<ZhiPuAiApi.ChatCompletionMessage> messages=llmFunctionCall(userAsk);
//        //结果组织
//        var functionResponseRequest = new ZhiPuAiApi.ChatCompletionRequest(messages, GLM_4.value, 0.8f);
//
//        ResponseEntity<ZhiPuAiApi.ChatCompletion> chatCompletion2 = zhipuAiApi.chatCompletionEntity(functionResponseRequest);
//
//        logger.info("Final response: " + chatCompletion2.getBody());
//        //是否需要发送消息
//        return chatCompletion2.getBody().choices().get(0).message().content();
//    }
//
//    private List<ZhiPuAiApi.ChatCompletionMessage> llmFunctionCall(ZhiPuAiApi.ChatCompletionMessage userAsk){
//
////        判断是否需要进行函数调用
//        List<ZhiPuAiApi.FunctionTool> functionToolList=getFunctionTools();
//
//        List<ZhiPuAiApi.ChatCompletionMessage> messages = new ArrayList<>(List.of(userAsk));
//
//        ZhiPuAiApi.ChatCompletionMessage responseMessage=msgWithTool(messages, functionToolList);
//        messages.add(responseMessage);
//
//        // Send the info for each function call and function response to the model.
//        excuteFunctionCall(responseMessage, messages);
//        return messages;
//    }
//
//    private void excuteFunctionCall(ZhiPuAiApi.ChatCompletionMessage responseMessage, List<ZhiPuAiApi.ChatCompletionMessage> messages){
//        for(ZhiPuAiApi.ChatCompletionMessage.ToolCall toolCall : responseMessage.toolCalls()){
//            var functionName=toolCall.function().name();
//            JSONObject param=JSON.parseObject(toolCall.function().arguments());
//
////            if(functionName.contains("查询实体")){
////                List<Map<String, Object>> maps=neo4jService.queryBy(param, ENTITY);
////            }else if(functionName.contains("查询关系")){
////                List<Map<String, Object>> maps=neo4jService.queryBy(param, RELATION);
////            }else if(functionName.contains("查询属性")){
////                List<Map<String, Object>> maps=neo4jService.queryBy(param, PROPERTY);
////            }else if(functionName.contains("查询元数据")){
////                List<Map<String, Object>> maps=neo4jService.queryBy(param, META_DATA);
////            }
//
//            if(functionName.contains("查询元数据")){
//                List<Map<String, Object>> maps=neo4jService.queryBy(param, META_DATA);
//            }else if(functionName.contains("查询")){
//                Map<String,Object> paramData=new HashMap<>();
//                paramData.put("name",param.getString("name"));
//                List<Map<String, Object>> maps=neo4jService.queryBy(paramData, META_DATA);
////                RuderApi.query
//                if(maps.size()<2){
//                    Map<String, Object> metaData=maps.get(0);
//                    List<Map<String, Object>> datas=neo4jService.queryBy(paramData, label(metaData));
//                }else{
//                    for(Map<String, Object> metaData : maps){
//                        List<Map<String, Object>> datas=neo4jService.queryBy(paramData, label(metaData));
//                    }
//                }
//            }
//
//            JSONObject retData=JSON.parseObject(post("", param.toJSONString()));
//
//            Map<String, Object> data=data(retData);
//            // extend conversation with function response.
//            messages.add(new ZhiPuAiApi.ChatCompletionMessage(JSON.toJSONString(data), Role.TOOL,
//                    functionName, toolCall.id(), null));
//        }
//    }
//
//    private ZhiPuAiApi.ChatCompletionMessage msgWithTool(List<ZhiPuAiApi.ChatCompletionMessage> messages, List<ZhiPuAiApi.FunctionTool> functionToolList){
//        ZhiPuAiApi.ChatCompletionRequest chatCompletionRequest = new ZhiPuAiApi.ChatCompletionRequest(messages, GLM_4.value,
//                functionToolList, ZhiPuAiApi.ChatCompletionRequest.ToolChoiceBuilder.AUTO);
//
//        ResponseEntity<ZhiPuAiApi.ChatCompletion> chatCompletion = zhipuAiApi.chatCompletionEntity(chatCompletionRequest);
//
//        ZhiPuAiApi.ChatCompletionMessage responseMessage = chatCompletion.getBody().choices().get(0).message();
//        return responseMessage;
//    }
//
//    @NotNull
//    private List<ZhiPuAiApi.FunctionTool> getFunctionTools(){
//        List<Map<String, Object>> functionCall=neo4jService.listAllByLabel("FunctionCall");
//        List<ZhiPuAiApi.FunctionTool> functionToolList = new ArrayList<>();
//        for( Map<String, Object> fci: functionCall){
//            JSONObject fciContent= JSON.parseObject(MapTool.content(fci));
//            functionToolList.add(getFunctionTool(fciContent.getString("description"), fciContent.getString("name"), fciContent.getString("parameters")));
//        }
//        return functionToolList;
//    }
//
//
//    private static ZhiPuAiApi.FunctionTool getFunctionTool(String description, String getCurrentWeather, String params){
//        var functionTool = new ZhiPuAiApi.FunctionTool(ZhiPuAiApi.FunctionTool.Type.FUNCTION,
//                new ZhiPuAiApi.FunctionTool.Function(
//                        description, getCurrentWeather,
//                        ModelOptionsUtils.jsonToMap(params)));
//        return functionTool;
//    }
//
//
//    private static ZhiPuAiApi.ChatCompletionMessage userMessage(String query){
//        return new ZhiPuAiApi.ChatCompletionMessage(query,
//                Role.USER);
//    }
//
//
//    private static <T> T fromJson(String json, Class<T> targetClass) {
//        try {
//            return new ObjectMapper().readValue(json, targetClass);
//        }
//        catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}

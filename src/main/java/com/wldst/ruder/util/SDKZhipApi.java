package com.wldst.ruder.util;

import com.alibaba.fastjson2.JSON;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.LLMChatDomain;
import com.wldst.ruder.module.ai.service.AiService;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.zhipu.oapi.demo.V4OkHttpClientTest.mapStreamToAccumulator;

@Service
public class SDKZhipApi extends LLMChatDomain{
    private static Logger logger = LoggerFactory.getLogger(SDKZhipApi.class);


    private CrudNeo4jService cruderService;

    private AiService aiService;
    private String apiKey="e73ca033102167b9353a38d889be99dc.FCFc8z9QdDquH6Z4";
   private ClientV4 client = null;
    @Autowired
    public SDKZhipApi(@Lazy  CrudNeo4jService cruderService, @Lazy AiService aiService){
        this.cruderService=cruderService;
        this.aiService=aiService;
    }

    private ClientV4 getClient(){
      String apiKeyNew= cruderService.getSettingBy("CHAT_GLM_API_KEY");
      if(!apiKey.equals(apiKeyNew)){
          client = new ClientV4.Builder(apiKeyNew).build();
       }

      if(client==null){
          client = new ClientV4.Builder(apiKey).build();
      }
      return client;
   }
    /**
     * 同步调用
     */
    private String testInvoke(String query) {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), query);
        messages.add(chatMessage);
        String requestIdTemplate="reque{}";
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = getClient().invokeModelApi(chatCompletionRequest);
//        System.out.println("model output:" +JSON.toJSONString(invokeModelApiResp));
        ChatMessage message=invokeModelApiResp.getData().getChoices().get(0).getMessage();
        return String.valueOf(message.getContent()) ;
    }

    public String call(List<Map<String,Object>> hisotry) {
        List<ChatMessage> messages = new ArrayList<>();
        historyTranslate(hisotry, messages);
        String requestIdTemplate="reque{}";
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = getClient().invokeModelApi(chatCompletionRequest);
//        System.out.println("model output:" +JSON.toJSONString(invokeModelApiResp));
        ChatMessage message=invokeModelApiResp.getData().getChoices().get(0).getMessage();
        return String.valueOf(message.getContent()) ;
    }

    private void historyTranslate(List<Map<String, Object>> hisotry, List<ChatMessage> messages){
        for(Map<String,Object> hi: hisotry){
            String content = content(hi);
            String role=role(hi);
            if(role.equals("assistant")){
                ChatMessage chatMessage = new ChatMessage(ChatMessageRole.ASSISTANT.value(), content);
                messages.add(chatMessage);
            }
            if(role.equals("user")){
                ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), content);
                messages.add(chatMessage);
            }
            if(role.equals("system")){
                if(!content.equals("")&&content.contains("ChatGLM3")||content.toLowerCase().contains("zhipuai")){
                    continue;
                }
                ChatMessage chatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), content);
                messages.add(chatMessage);
            }
        }
    }

    public String call(String query,List<Map<String,Object>> hisotry) {
        List<ChatMessage> messages = new ArrayList<>();
        historyTranslate(hisotry, messages);
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), query);
        messages.add(chatMessage);
        String requestIdTemplate="reque{}";
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = getClient().invokeModelApi(chatCompletionRequest);
//        System.out.println("model output:" +JSON.toJSONString(invokeModelApiResp));
        ChatMessage message=invokeModelApiResp.getData().getChoices().get(0).getMessage();
        return String.valueOf(message.getContent()) ;
    }

    public String call2(List<ChatMessage> messages) {
        String requestIdTemplate="reque{}";
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = getClient().invokeModelApi(chatCompletionRequest);
//        System.out.println("model output:" +JSON.toJSONString(invokeModelApiResp));
        ChatMessage message=invokeModelApiResp.getData().getChoices().get(0).getMessage();
        return String.valueOf(message.getContent()) ;
    }



    @NotNull
    private List<ChatMessage> llmCall(ChatMessage userAsk){
        List<ChatMessage> messages=new ArrayList<>(List.of(userAsk));
//        判断是否需要进行函数调用
        List<Map<String, Object>> functionCall=cruderService.listAllByLabel("FunctionCall");
        Map<String,Map<String, Object>> functionCallMap=new HashMap<>();
        functionCall.forEach(function->{
            String functionName=String.valueOf(function.get("name"));
            functionCallMap.put(functionName,function);
        });
        List<ChatTool> chatToolList =aiService.functionData(functionCall);
        String requestId = String.format("llmFunctionCall-%d", System.currentTimeMillis());


        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder().model("GLM-4").stream(Boolean.FALSE).invokeMethod("invoke").messages(messages).requestId(requestId).tools(chatToolList).toolChoice("auto").build();
        ModelApiResponse invokeModelApiResp = getClient().invokeModelApi(chatCompletionRequest);

        if (invokeModelApiResp.isSuccess()) {
            invokeModelApiResp.getData().getChoices().forEach((choice) -> {
                if (choice.getMessage() != null) {
//                    System.out.println("message: " + choice.getMessage().getContent());
                    String toolCallId=choice.getMessage().getTool_call_id();
                    if (choice.getMessage().getTool_calls() != null) {
                        choice.getMessage().getTool_calls().forEach((toolCall) -> {
//                            System.out.println("tool_call_id: " + toolCall.getId());
                            aiService.processToolCall(toolCall, functionCallMap, messages, toolCallId);

                        });
                    }else{
                        messages.add(choice.getMessage());
                    }
                }
            });
        }

        return messages;
    }

    /**
     * 根据AgentPackageData执行
     * @param data
     * @return
     */
    public List<ChatMessage> llmCall(Map<String, Object> data){
        if(data==null||data.isEmpty()){
            return null;
        }
        List<ChatMessage> messages = new ArrayList<>();
        historyTranslate(listMapObject(data, "messages"), messages);

//        判断是否需要进行函数调用
        List<Map<String, Object>> functionCall= listMapObject(data, "functions");
        Map<String,Map<String, Object>> functionCallMap=new HashMap<>();
        String requestId = String.format("llmFunctionCall-%d", System.currentTimeMillis());
        ChatCompletionRequest chatCompletionRequest = null;
        if(functionCall!=null&&functionCall.size()>0){
            functionCall.forEach(function->{
                String functionName=String.valueOf(function.get("name"));
                functionCallMap.put(functionName,function);
            });

            List<ChatTool> chatToolList=aiService.functionData(functionCall);
            chatCompletionRequest = ChatCompletionRequest.builder().model("GLM-4").stream(Boolean.FALSE).invokeMethod("invoke").messages(messages).requestId(requestId).tools(chatToolList).toolChoice("auto").build();
        }else{
            chatCompletionRequest = ChatCompletionRequest.builder().model("GLM-4").stream(Boolean.FALSE).invokeMethod("invoke").messages(messages).requestId(requestId).toolChoice("auto").build();
        }

        ModelApiResponse invokeModelApiResp = getClient().invokeModelApi(chatCompletionRequest);

        if (invokeModelApiResp.isSuccess()) {
            invokeModelApiResp.getData().getChoices().forEach((choice) -> {
                if (choice.getMessage() != null) {
//                    System.out.println("message: " + choice.getMessage().getContent());
                    String toolCallId=choice.getMessage().getTool_call_id();
                    if (choice.getMessage().getTool_calls() != null) {
                        choice.getMessage().getTool_calls().forEach((toolCall) -> {
                            aiService.processToolCall(toolCall, functionCallMap, messages, toolCallId);
                        });
                    }else{
                        messages.add(choice.getMessage());
                    }
                }
            });
        }
        return messages;
    }




    /**
     * 异步调用
     */
    private String testAsyncInvoke() {
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), "作为一名营销专家，请为智谱开放平台创作一个吸引人的slogan");
        messages.add(chatMessage);
        String requestIdTemplate="reque{}";
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethodAsync)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse invokeModelApiResp = getClient().invokeModelApi(chatCompletionRequest);
        System.out.println("model output:" + JSON.toJSONString(invokeModelApiResp));
        return invokeModelApiResp.getData().getTaskId();
    }

    /**
     * sse调用
     */
    private void testSseInvoke() {
        List<ChatMessage> messages = new ArrayList<>();
        String content="作为一名营销专家，请为智谱开放平台创作一个吸引人的slogan";
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), content);
        messages.add(chatMessage);
        String requestIdTemplate="reque{}";
        String requestId = String.format(requestIdTemplate, System.currentTimeMillis());

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .messages(messages)
                .requestId(requestId)
                .build();
        ModelApiResponse sseModelApiResp = getClient().invokeModelApi(chatCompletionRequest);
        if (sseModelApiResp.isSuccess()) {
            AtomicBoolean isFirst = new AtomicBoolean(true);
            ChatMessageAccumulator chatMessageAccumulator = mapStreamToAccumulator(sseModelApiResp.getFlowable())
                    .doOnNext(accumulator -> {
                        {
                            if (isFirst.getAndSet(false)) {
                                System.out.print("Response: ");
                            }
                            if (accumulator.getDelta()!=null){
                                List<ToolCalls> toolCalls=accumulator.getDelta().getTool_calls();
                                if(toolCalls!= null){
                                    String jsonString=JSON.toJSONString(toolCalls);
                                    System.out.println("tool_calls: "+jsonString);

                                }
                            }
                            if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
                                System.out.print(accumulator.getDelta().getContent());
                            }
                        }
                    })
                    .doOnComplete(System.out::println)
                    .lastElement()
                    .blockingGet();

            Choice choice = new Choice(chatMessageAccumulator.getChoice().getFinishReason(), 0L, chatMessageAccumulator.getDelta());
            List<Choice> choices = new ArrayList<>();
            choices.add(choice);
            ModelData data = new ModelData();
            data.setChoices(choices);
            data.setUsage(chatMessageAccumulator.getUsage());
            data.setId(chatMessageAccumulator.getId());
            data.setCreated(chatMessageAccumulator.getCreated());
            data.setRequestId(chatCompletionRequest.getRequestId());
            sseModelApiResp.setFlowable(null);
            sseModelApiResp.setData(data);
        }
        System.out.println("model output:" + JSON.toJSONString(sseModelApiResp));
    }
    public static void main(String... args) {
//        System.out.println(new SDKZhipApi().testInvoke("如何在本地利用SpringAI来管理多个FunctionCall"));

//        testInvoke("使用Vue3实现一个BPM设计界面，这个界面使用AntV X6来做图形框架，这个界面要能优雅的实现图编辑。");
    }
}

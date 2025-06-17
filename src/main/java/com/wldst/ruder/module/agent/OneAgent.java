package com.wldst.ruder.module.agent;

import java.util.HashMap;
import java.util.Map;

public class OneAgent{
    public static void main(String[] args) {
        // 模拟用户输入
        String userInput = "请分析这个情况";
        handleInput(userInput);
    }

    private static void handleInput(String userInput){
        Map<String,Object> retMap = new HashMap<>();
        // 创建各个模块实例
        Perception perception = new Perception();
        Parsing parsing = new Parsing();
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        InferenceEngine inferenceEngine = new InferenceEngine(knowledgeBase);
        DecisionModule decisionModule = new DecisionModule();

        try {
            // 获取感知到的数据并解析
            ParsedData parsedData = parsing.parseInput(perception.perceiveData(userInput));
            for(String word:parsedData.getWords()){
                // 从知识库检索相关信息
                String knowledge = knowledgeBase.retrieveKnowledge(parsedData.getWords().get(0));
//                inferenceEngine.setKnowledgeBase(knowledge);
//                decisionModule.setKnowledgeBase(knowledge);

                retMap.put(word,knowledge);
//                //元数据信息
//                retMap.put("metadata",parsedData.getMetadata());
//                //关系数据
//
//                retMap.put("relation",parsedData.getRelations());
//
//                //实例数据
//                retMap.put("instance",parsedData.getInstances());

            }


            // 使用推理引擎进行推理
            String inference = inferenceEngine.inferRelation(parsedData);

            // 做出决策
            String decision = decisionModule.makeDecision(inference);
            retMap.put("decision",decision);
            retMap.put("inference",inference);
            // 打印最终的结果
//            System.out.println("知识库信息: " + knowledge);
            System.out.println("推理结果: " + inference);
            System.out.println("决策: " + decision);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}

package com.wldst.ruder.module.ai.service;

import io.milvus.client.MilvusClient;
import io.milvus.grpc.SearchRequest;
import io.milvus.param.highlevel.dml.response.SearchResponse;
import io.milvus.v2.service.vector.response.SearchResp;

//public class VectorSearchAndDialogue {
//    private MilvusClient client;
//    private OllamaIntegration ollamaIntegration;
//
//    public VectorSearchAndDialogue() throws Exception {
//        this.client = new MilvusClient("http://localhost:19530");
//        this.ollamaIntegration = new OllamaIntegration();
//    }
//
//    public String processQuery(String query) throws Exception {
//        // 1. 将查询文本转为向量
//        float[] queryVector = convertTextToVector(query);
//
//        // 2. 搜索数据库中相似的向量
//        SearchRequest request = new SearchRequest.Builder()
//                .withCollectionName("text_vectors")
//                .addVectors(queryVector)
//                .build();
//        SearchResponse response = client.search(request);
//
//        // 3. 提取最相关的上下文信息
//        String context = extractRelevantContext(response.getResults());
//
//        // 4. 使用Ollama生成回复，结合上下文
//        String enhancedPrompt = "Given the context: " + context + "\n" + query;
//        return ollamaIntegration.generateResponse(enhancedPrompt);
//    }
//
//    private float[] convertTextToVector(String text) {
//        // 实现具体的向量化逻辑
//        return new float[100]; // 示例代码，实际应根据模型生成
//    }
//
//    private String extractRelevantContext(SearchResp.SearchResult results) {
//        // 处理搜索结果，提取相关文本
//        // 这里仅为示例，返回第一个匹配项的内容
//        if (results.getResults().size() > 0) {
//            return "Previous conversation: " + new String(results.getResults().get(0).getVector());
//        } else {
//            return "";
//        }
//    }
//}

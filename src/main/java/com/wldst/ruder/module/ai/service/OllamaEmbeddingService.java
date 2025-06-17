package com.wldst.ruder.module.ai.service;

import com.alibaba.fastjson2.JSON;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.util.MapTool;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OllamaEmbeddingService extends MapTool {
    private static final OkHttpClient client = new OkHttpClient();
    @Autowired
    private CrudNeo4jService neo4jService;
    public static void main(String[] args) {
        // 创建OkHttpClient实例


        // 要嵌入的文本
        String text = "neo4j data,node properties,relation crud.";
        OllamaEmbeddingService ollamaEmbeddingService = new OllamaEmbeddingService();
        ollamaEmbeddingService.getEmbeding(text);
    }

    public List<Float> getEmbeding(String text) {
        // 构建请求体
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("model", "nomic-embed-text");
        requestBodyMap.put("prompt", text);

        // 创建RequestBody对象
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),
                JSON.toJSONString(requestBodyMap));

        // 构建HTTP POST请求
        Request request = new Request.Builder()
                .url("http://localhost:9444/api/embeddings")
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                // 解析响应中的嵌入向量
                Map<String, Object> result = JSON.to(Map.class,responseBody);
                Object embeddingsObj = result.get("embedding");

                // 根据实际情况处理嵌入向量
                if (embeddingsObj instanceof List) {
                    List<Float> embeddings = (List<Float>) embeddingsObj;
                    return embeddings;
                }
            } else {
                throw new IOException("HTTP请求失败，状态码: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}


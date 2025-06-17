package com.wldst.ruder.module.ai.service;

import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * ChromaDB服务
 * @author wldst
 * @date 2023/08/01
 */
@Service
@SuppressWarnings("all")
public class ChromaDBService {

    private static final String CHROMA_ENDPOINT = "http://localhost:8000";
    private static OkHttpClient client = new OkHttpClient();
 
    //插入向量
    public String insertVector(String collectionName, float[] vector) throws IOException {

        String requestBody = "{\"collection_name\": \"" + collectionName + "\", \"embeddings\": [\"" + vector + "\"]}";
        String insertEndpoint = String.format("/api/collections/%s/vectors", collectionName);

        Request insertRequest = new Request.Builder()
                .url(CHROMA_ENDPOINT + insertEndpoint)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(insertRequest).execute()) {
            if (response.isSuccessful()) {
                System.out.println("向量插入成功: " + response.body().string());
                return response.body().string();
            } else {
                System.out.println("插入向量失败: " + response.code());
                return response.code()+response.body().string();
            }
        }

    }

    /**
     *  查询向量
     * @param collectionName
     * @param vector
     * @throws IOException
     */
    public String queryVector(String collectionName, float[] vector) throws IOException {

        String requestBody = "{\"collection_name\": \"" + collectionName + "\", \"query_embeddings\": [\"" + vector + "\"]}";
        String queryEndpoint = String.format("/api/collections/%s/query", collectionName);

        Request queryRequest = new Request.Builder()
                .url(CHROMA_ENDPOINT + queryEndpoint)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
               .addHeader("Content-Type", "application/json").build();
        try (Response response = client.newCall(queryRequest).execute()) {
            if (response.isSuccessful()) {
                System.out.println("查询成功: " + response.body().string());
                return response.body().string();
            } else {
                System.out.println("查询失败: " + response.code());
                return response.code()+response.body().string();
            }
        }
    }

    /**
     * 删除向量
     * @param collectionName
     * @throws IOException
     */
    public static String deleteCollection(String collectionName) throws IOException {

        String deleteEndpoint = String.format("/api/collections/%s", collectionName);

        Request deleteRequest = new Request.Builder()
                .url(CHROMA_ENDPOINT + deleteEndpoint)
                .delete()
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(deleteRequest).execute()) {
            if (response.isSuccessful()) {
                System.out.println("集合删除成功: " + response.body().string());
                return response.body().string();
            }else{
                System.out.println("集合删除失败: " + response.code());
                return response.code()+response.body().string();
            }
        }
    }



        public static void main(String[] args) throws IOException {
            // 创建一个集合
            OkHttpClient client = new OkHttpClient();

            String requestBody = "{ \"name\": \"my_collection\" }";
            Request request = new Request.Builder()
                    .url(CHROMA_ENDPOINT + "/api/collections")
                    .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("集合创建成功: " + response.body().string());
                } else {
                    System.out.println("创建集合失败: " + response.code());
                }
            }
        }

        public String  createCollection(String collectionName) throws IOException {
            String requestBody = "{ \"name\": \""+collectionName+"\" }";
            Request request = new Request.Builder()
                    .url(CHROMA_ENDPOINT + "/api/collections")
                    .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("集合创建成功: " + response.body().string());
                    return response.body().string();
                } else {
                    System.out.println("创建集合失败: " + response.code());
                    return response.code()+response.body().string();
                }
            }
        }

        public void  deleteVector(String collectionName ) throws IOException {
            String deleteEndpoint = String.format("/api/collections/%s/vectors", collectionName);
            String deleteRequest = "{\n" +
                    "    \"ids\": [\"id1\"]\n" +
                    "}";
            Request deleteReq = new Request.Builder()
                    .url(CHROMA_ENDPOINT + deleteEndpoint)
                    .post(RequestBody.create(MediaType.parse("application/json"), deleteRequest))
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(deleteReq).execute()) {
                if (response.isSuccessful()) {
                    System.out.println("删除成功: " + response.body().string());
                } else {
                    System.out.println("删除失败: " + response.code());
                }
            }
        }
    }


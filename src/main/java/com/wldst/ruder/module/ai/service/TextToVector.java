package com.wldst.ruder.module.ai.service;


import io.milvus.client.MilvusClient;
import io.milvus.grpc.FieldSchema;
import milvus.proto.msg.Msg;
import org.drools.core.spi.FieldValue;

//public class TextToVector {
//    private static final String VECTOR_DB_ENDPOINT = "http://localhost:19530";
//
//    public static void main(String[] args) throws Exception {
//        // 初始化Milvus客户端
//        MilvusClient client = new MilvusClient(VECTOR_DB_ENDPOINT);
//
//        // 创建集合（如果尚未存在）
//        Msg.CreateCollectionRequest request = Msg.CreateCollectionRequest.newBuilder()
//                .setCollectionName("text_vectors")
//                .addFields(
//                        FieldSchema.newBuilder().setName("id").setType(DataType.INT64).setPrimaryKey(true).build(),
//                        FieldSchema.newBuilder().setName("vector").setType(DataType.FLOAT_VECTOR).setDimension(100).build()
//                )
//                .build();
//        client.createCollection(request);
//
//        // 示例文本转换为向量
//        String text = "Hello, how are you?";
//        float[] vector = convertTextToVector(text); // 实现此方法将文本转为向量
//
//        // 插入向量到数据库
//        Msg.InsertRequest insertRequest = Msg.InsertRequest.newBuilder()
//                .setCollectionName("text_vectors")
//                .addFields(
//                        new FieldValue().setKey("id").setValue(new int[]{1}),
//                        new FieldValue().setKey("vector").setValue(vector)
//                )
//                .build();
//        client.insert(insertRequest);
//        System.out.println("Vector inserted successfully.");
//    }
//
//    private static float[] convertTextToVector(String text) {
//        // 实现具体的向量化逻辑，例如使用预训练模型
//        // 这里仅为示例，返回随机浮点数数组
//        float[] vector = new float[100];
//        for (int i=0; i<vector.length; i++) {
//            vector[i] = Math.random();
//        }
//        return vector;
//    }
//}


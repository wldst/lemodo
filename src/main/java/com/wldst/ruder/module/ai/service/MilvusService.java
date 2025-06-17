package com.wldst.ruder.module.ai.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.TelnetUtil;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.exception.MilvusException;
import io.milvus.grpc.*;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.v2.service.vector.response.QueryResp;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.lucene.search.CollectionStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

// MilvusService.java
@Service
public class MilvusService extends MapTool {
    private static final Logger logger = LoggerFactory.getLogger(MilvusService.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private OllamaEmbeddingService ollamaEmbeddingService;


    private MilvusClient milvusClient;

    @PostConstruct
    public void init() {
        String milvusHost="127.0.0.1";
        int milvusPort=19530;
        try {
            milvusHost = neo4jService.getSettingBy("milvus.host");
        } catch (Exception e) {
            milvusHost = "127.0.0.1";
        }
        try {
            milvusPort = Integer.parseInt(neo4jService.getSettingBy("milvus.port"));
        } catch (NumberFormatException e) {
            milvusPort = 19530;
        }
        if(milvusHost==null){
            milvusHost = "127.0.0.1";
        }
        if(milvusPort==0){
            milvusPort = 19530;
        }

        if(milvusHost!=null&&milvusPort!=0){
            if(TelnetUtil.telnet(milvusHost, milvusPort, 2000)){
                ConnectParam connectParam = ConnectParam.newBuilder()
                        .withHost(milvusHost)
                        .withPort(milvusPort)
                        .build();
                this.milvusClient = new MilvusServiceClient(connectParam);
            }
        }
    }

    @PreDestroy
    public void close() {
        if (milvusClient != null) {
            try {
                milvusClient.close();
            } catch (Exception e) {
                logger.error("Error closing Milvus client", e);
            }
        }
    }

    public void createCollection(String collectionName, int dimension) throws MilvusException {
        // 构建字段
        FieldType fieldId = FieldType.newBuilder()
                .withName("id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();

        FieldType fieldVector = FieldType.newBuilder()
                .withName("vector")
                .withDataType(DataType.FloatVector)
                .withDimension(dimension)
                .build();

        // 创建schema
        CollectionSchemaParam schema = CollectionSchemaParam.newBuilder()
                .addFieldType(fieldId)
                .addFieldType(fieldVector)
                .build();

        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withSchema(schema)
                .build();

        milvusClient.createCollection(createParam);
    }

    public List<Long> insertEntities(String collectionName, List<List<Float>> vectors) throws MilvusException {
        List<JsonObject> data = new ArrayList<>();
        for (List<Float> vector : vectors) {
            JsonObject entity = new JsonObject();
            entity.add("vector", new Gson().toJsonTree(vector));
            data.add(entity);
        }

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withRows(data)
                .build();

        R<MutationResult> response = milvusClient.insert(insertParam);
        return response.getData().getIDs().getIntId().getDataList();
    }

    public void deleteEntities(String collectionName, List<Long> entityIds) throws MilvusException {
        DeleteParam deleteParam = DeleteParam.newBuilder()
                .withCollectionName(collectionName)
                .withExpr("id in [" + String.join(",", entityIds.stream().map(String::valueOf).toList()) + "]")
                .build();

        milvusClient.delete(deleteParam);
    }

    public SearchResultData searchSimilarVectors(String collectionName,
                                                 List<Float> queryVector,
                                                 int topK) throws MilvusException {
        List<List<Float>> queryVectors = Collections.singletonList(queryVector);

        SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withVectors(queryVectors)
                .withTopK(topK)
                .withParams("{\"nprobe\":10}")
                .build();

        R<SearchResults> response = milvusClient.search(searchParam);
        return response.getData().getResults();
    }

//    public SearchResultData statistics(String collectionName) throws MilvusException {
//        GetCollectionStatisticsParam request = GetCollectionStatisticsParam.newBuilder()
//                .withCollectionName(collectionName)
//                .build();
//        R<GetCollectionStatisticsResponse> stats = milvusClient.getCollectionStatistics(request);
//        System.out.println("集合名称: " +collectionName);
//        System.out.println("总数据量 (Bytes): " + stats.getData().getSerializedSize());
//        System.out.println("当前分区数: " + stats.getData().toString());
//
//        GetCollectionStatisticsResponse rs =(GetCollectionStatisticsResponse)stats.getData();
//        rs.getStatsList().forEach(fieldData -> {
//            System.out.println("字段名称: " + fieldData.getKey());
////            System.out.println("字段类型: " + fieldData.);
//            System.out.println("字段值: " + fieldData.getValue());
//        });
//        return stats.getData();
//    }




    public Map<String,Object> query(String collectionName) throws MilvusException {
        // 构建查询条件
        List<String> outputFields = new ArrayList<>();
        outputFields.add("id");
        outputFields.add("vector");
        QueryParam queryParam = QueryParam.newBuilder().withCollectionName(collectionName)
                .withOutFields(outputFields)
                .build();

        R<QueryResults> result = milvusClient.query(queryParam);
        int fieldsDataCount = result.getData().getFieldsDataCount();
        System.out.println("查询结果总数: " +fieldsDataCount);

        // 遍历结果
        for (FieldData row : result.getData().getFieldsDataList()) {
            System.out.println(row.toString());
        }

        List<FieldData> fieldsDataList = result.getData().getFieldsDataList();
        Map<String,Object> map = new HashMap<>();
        for (FieldData fieldData : fieldsDataList) {
            map.put(fieldData.getFieldName(),fieldData.getType());

        }

        return map;
    }

    public void dropCollection(String collectionName) throws MilvusException {
        DropCollectionParam dropParam = DropCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build();
        milvusClient.dropCollection(dropParam);
    }

    public void transNeo4j2Vector() {
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, META_DATA, META_DATA);

        List<Float> membeding = ollamaEmbeddingService.getEmbeding(jsonString(md));
        createCollection(META_DATA, membeding.size());

        List<Map<String, Object>> metaData = neo4jService.listDataByLabel(META_DATA);
        List<List<Float>> metadataDataEmbedding = new ArrayList<>();
        metadataDataEmbedding.add(membeding);
        for(Map<String, Object> mi: metaData){

            String labeli = label(mi);
            if(labeli==null||labeli.equals(META_DATA)){
                continue;
            }
            List<Float> embeding = ollamaEmbeddingService.getEmbeding(jsonString(mi));

            metadataDataEmbedding.add(embeding);

            createCollection(labeli, embeding.size());
            List<Map<String, Object>> dataList = neo4jService.listDataByLabel(labeli);
            List<List<Float>> dataiEmbedding = new ArrayList<>();
            for(Map<String, Object> di : dataList){
                List<Float> dataEmbeding = ollamaEmbeddingService.getEmbeding(jsonString(di));
                createCollection(labeli, dataEmbeding.size());
                dataiEmbedding.add(dataEmbeding);
            }
            if(!dataiEmbedding.isEmpty()){
                insertEntities(labeli, dataiEmbedding);
            }

        }
        if(!metadataDataEmbedding.isEmpty()){
            insertEntities(META_DATA, metadataDataEmbedding);
        }

    }
}

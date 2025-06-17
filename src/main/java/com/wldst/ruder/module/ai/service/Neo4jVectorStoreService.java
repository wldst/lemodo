package com.wldst.ruder.module.ai.service;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.manage.service.ConfigService;
import com.wldst.ruder.util.MapTool;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Neo4jVectorStoreService extends MapTool {
    private static final Logger logger = LoggerFactory.getLogger(Neo4jVectorStoreService.class);

    @Autowired
    private CrudNeo4jService crudNeo4jService;
    private final ConfigService configService;
    private Driver driver;

    private final OllamaEmbeddingService ollamaEmbeddingService;

    @Autowired
    public Neo4jVectorStoreService(OllamaEmbeddingService embeddedService,ConfigService configService) {
        this.ollamaEmbeddingService = embeddedService;
        this.configService = configService;

        try {
            init();
        } catch (Exception e) {
            logger.error("Failed to initialize Neo4j connection.", e);
        }
    }

    @PostConstruct
    public void init() {
        Map<String, Object> neo4jVectorConfig = null;
        try {
            neo4jVectorConfig = configService.getConfigMap("Neo4jVectorConfig");
            if (neo4jVectorConfig == null) {
                logger.error("Neo4jVectorConfig is not configured.");
                return;
            }
            String neo4jUri = string0(neo4jVectorConfig,"neo4j.uri");
            String neo4jUser = string0(neo4jVectorConfig,"neo4j.user");
            String neo4jPassword = string0(neo4jVectorConfig,"neo4j.password");
            if (StringUtils.isNotBlank(neo4jUri) && StringUtils.isNotBlank(neo4jUser) && StringUtils.isNotBlank(neo4jPassword)) {
                driver = GraphDatabase.driver(neo4jUri, AuthTokens.basic(neo4jUser, neo4jPassword));
            } else {
                logger.error("Neo4j connection details are not configured properly.");
            }
        } catch (DefineException e) {
            throw new RuntimeException(e);
        }

    }

    @PreDestroy
    public void close() {
        if (driver != null) {
            driver.close();
        }
    }

    public void createNode(String label, Map<String, Object> properties) {
        try (Session session = driver.session()) {
            String cypher = "CREATE (n:" + label + " $props)";
            session.writeTransaction(tx -> tx.run(cypher, Values.parameters("props", properties)));
        }
    }

    public void insertEntities(String label, List<List<Float>> vectors) {
        try (Session session = driver.session()) {
            for (List<Float> vector : vectors) {
                Map<String, Object> properties = new HashMap<>();
                properties.put("vector", vector);
                createNode(label, properties);
            }
        }
    }

    public List<Map<String, Object>> searchSimilarVectors(String label, List<Float> queryVector, int topK) {
        try (Session session = driver.session()) {
            String cypher = "MATCH (n:" + label + ")\n" +
                    "RETURN n, apoc.math.euclideanDistance(n.vector, $queryVector) AS distance\n" +
                    "ORDER BY distance ASC\n" +
                    "LIMIT $topK";
            Result result = session.readTransaction(tx -> tx.run(cypher, Values.parameters("queryVector", queryVector, "topK", topK)));

            List<Map<String, Object>> results = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> node = new HashMap<>();
                node.put("id", record.get("n").asNode().id());
                node.put("vector", record.get("n").asNode().get("vector").asList());
                node.put("distance", record.get("distance").asDouble());
                results.add(node);
            }
            return results;
        }
    }

    public void deleteEntities(String label, List<Long> entityIds) {
        try (Session session = driver.session()) {
            String cypher = "MATCH (n:" + label + ")\n" +
                    "WHERE id(n) IN $entityIds\n" +
                    "DETACH DELETE n";
            session.writeTransaction(tx -> tx.run(cypher, Values.parameters("entityIds", entityIds)));
        }
    }

    public List<Map<String, Object>> query(String label) {
        try (Session session = driver.session()) {
            String cypher = "MATCH (n:" + label + ")\n" +
                    "RETURN n";
            Result result = session.readTransaction(tx -> tx.run(cypher));

            List<Map<String, Object>> results = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> node = new HashMap<>();
                node.put("id", record.get("n").asNode().id());
                node.put("vector", record.get("n").asNode().get("vector").asList());
                results.add(node);
            }
            return results;
        }
    }

    public void dropLabel(String label) {
        try (Session session = driver.session()) {
            String cypher = "MATCH (n:" + label + ")\n" +
                    "DETACH DELETE n";
            session.writeTransaction(tx -> tx.run(cypher));
        }
    }

    public void transNeo4j2Vector() {
        Map<String, Object> md = crudNeo4jService.getAttMapBy(LABEL, META_DATA, META_DATA);

        List<Float> membeding = ollamaEmbeddingService.getEmbeding(jsonString(md));
        md.put("vector", membeding);
        createNode(META_DATA, md);

        List<Map<String, Object>> metaDataList = crudNeo4jService.listDataByLabel(META_DATA);
        for(Map<String, Object> mi: metaDataList){

            String labeli = label(mi);
            if(labeli==null||labeli.equals(META_DATA)){
                continue;
            }
            List<Float> embeding = ollamaEmbeddingService.getEmbeding(jsonString(mi));

            List<Map<String, Object>> dataList = crudNeo4jService.listDataByLabel(labeli);

            for(Map<String, Object> di : dataList){
                List<Float> dataEmbeding = ollamaEmbeddingService.getEmbeding(jsonString(di));
                di.put("vector", dataEmbeding);
                createNode(labeli, di);
            }
            mi.put("vector", embeding);
            createNode(META_DATA, mi);
        }

    }
}


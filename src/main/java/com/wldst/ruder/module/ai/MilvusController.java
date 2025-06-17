package com.wldst.ruder.module.ai;

import com.wldst.ruder.module.ai.service.MilvusService;
import com.wldst.ruder.module.ai.service.OllamaEmbeddingService;
import io.milvus.exception.MilvusException;
import io.milvus.grpc.SearchResultData;
import io.milvus.v2.service.vector.response.SearchResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("${server.context}/api/milvus")
public class MilvusController {
    @Autowired
    private  MilvusService milvusService;


    public MilvusController(MilvusService milvusService) {
        this.milvusService = milvusService;
    }

    @RequestMapping(value="/neo4j2Embeding",method = RequestMethod.GET)
    public ResponseEntity<?> neo4j2Embeding() {
        try {
            milvusService.transNeo4j2Vector();
            return ResponseEntity.ok().build();
        } catch (MilvusException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/collections")
    public ResponseEntity<?> createCollection(
            @RequestParam String collectionName,
            @RequestParam int dimension) {
        try {
            milvusService.createCollection(collectionName, dimension);
            return ResponseEntity.ok().build();
        } catch (MilvusException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/collections/{collectionName}/insert")
    public ResponseEntity<?> insertVectors(
            @PathVariable String collectionName,
            @RequestBody List<List<Float>> vectors) {
        try {
            List<Long> entityIds = milvusService.insertEntities(collectionName, vectors);
            return ResponseEntity.ok(entityIds);
        } catch (MilvusException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/collections/{collectionName}/query")
    public ResponseEntity<?> queryVectors(
            @PathVariable String collectionName,
            @RequestBody List<List<Float>> vectors) {
        try {
            List<Long> entityIds = milvusService.insertEntities(collectionName, vectors);
            return ResponseEntity.ok(entityIds);
        } catch (MilvusException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }



    @DeleteMapping("/collections/{collectionName}/entities")
    public ResponseEntity<?> deleteEntities(
            @PathVariable String collectionName,
            @RequestBody List<Long> entityIds) {
        try {
            milvusService.deleteEntities(collectionName, entityIds);
            return ResponseEntity.ok().build();
        } catch (MilvusException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/collections/{collectionName}/search")
    public ResponseEntity<?> searchSimilar(
            @PathVariable String collectionName,
            @RequestBody List<Float> queryVector,
            @RequestParam(defaultValue = "10") int topK) {
        try {
            SearchResultData searchResultData = milvusService.searchSimilarVectors(
                    collectionName, queryVector, topK);
            return ResponseEntity.ok(searchResultData);
        } catch (MilvusException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/collections/{collectionName}")
    public ResponseEntity<?> dropCollection(
            @PathVariable String collectionName) {
        try {
            milvusService.dropCollection(collectionName);
            return ResponseEntity.ok().build();
        } catch (MilvusException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}


package com.wldst.ruder.module.ai;

import com.wldst.ruder.module.ai.service.MilvusService;
import com.wldst.ruder.module.ai.service.Neo4jVectorStoreService;
import com.wldst.ruder.module.ai.service.OllamaEmbeddingService;
import io.milvus.exception.MilvusException;
import io.milvus.grpc.SearchResultData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("${server.context}/api/neo4j")
public class Neo4jVertorStoreController {
    @Autowired
    private Neo4jVectorStoreService neo4jService;

    public Neo4jVertorStoreController(Neo4jVectorStoreService neo4jService) {
        this.neo4jService = neo4jService;
    }

    @RequestMapping(value="/neo4j2Embeding",method = RequestMethod.GET)
    public ResponseEntity<?> neo4j2Embeding() {
        try {
            neo4jService.transNeo4j2Vector();
            return ResponseEntity.ok().build();
        } catch (MilvusException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

}


package com.wldst.ruder.module.bs.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.MapTool;

public class RelationOut extends ShellListImpl{
    private static Logger logger = LoggerFactory.getLogger(RelationOut.class);
    private CrudNeo4jService neo4jService;
    private RelationService relationService;
    private String label;   
    private String nodeLabel;
    private Long targetId;

    public RelationOut(CrudNeo4jService neo4jService,String rlabel,String target, String targetLabel) {
	this.neo4jService = neo4jService;
	this.label = rlabel;
	this.nodeLabel = targetLabel;
	try {
	    this.targetId = Long.valueOf(target);
	} catch (Exception e) {
	    this.targetId = neo4jService.getNodeId(LABEL, target, this.nodeLabel);
	}
    }
    public RelationOut(CrudNeo4jService neo4jService,RelationService relationService,String rlabel,String target, String targetLabel) {
  	this.neo4jService = neo4jService;
  	this.relationService=relationService;
  	this.label = rlabel;
  	this.nodeLabel = targetLabel;
  	try {
  	    this.targetId = Long.valueOf(target);
  	} catch (Exception e) {
  	    this.targetId = neo4jService.getNodeId(LABEL, target, this.nodeLabel);
  	}
      }
    
    public RelationOut(CrudNeo4jService neo4jService,String rlabel,Long targetId, String targetLabel) {
	this.neo4jService = neo4jService;
	this.label = rlabel;
	this.nodeLabel = targetLabel;
	this.targetId = targetId;
    }

    /**
     * 执行方法
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> sourceData) {
	relationService.addRel(label, MapTool.string(sourceData, ID), targetId.toString());
	return sourceData;
    }
    
    @Override
    public String getLabel() {
	return label;
    }


}

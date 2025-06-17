package com.wldst.ruder.module.bs.impl;

import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.MapTool;

public class RelationIn extends ShellListImpl  {
    private static Logger logger = LoggerFactory.getLogger(RelationIn.class);
    private CrudNeo4jService neo4jService;
    private RelationService relationService;
    private String label;
    private String nodeLabel;
    private Long sourceId;
    /**
     * 
     * @param neo4jService
     * @param source 当source为Long类型时，直接建立关系，
     * 当source为String时，则需要结合NodeLabel找到节点ID
     * @param sourceLabel
     * @param relLabel
     */
    public RelationIn(CrudNeo4jService neo4jService,String relLabel,String source,String sourceLabel) {
	this.neo4jService = neo4jService;
	this.label = relLabel;
	this.nodeLabel = sourceLabel;
	try{
	   this.sourceId=Long.valueOf(source);
	}catch(Exception e) {
	    this.sourceId=neo4jService.getNodeId(LABEL, source, this.nodeLabel);
	}
    }
    public RelationIn(CrudNeo4jService neo4jService,RelationService relationService,String relLabel,String source,String sourceLabel) {
	this.neo4jService = neo4jService;
	this.label = relLabel;
	this.nodeLabel = sourceLabel;
	try{
	   this.sourceId=Long.valueOf(source);
	}catch(Exception e) {
	    this.sourceId=neo4jService.getNodeId(LABEL, source, this.nodeLabel);
	}
    }
    
    public RelationIn(CrudNeo4jService neo4jService,String relLabel,Long source,String sourceLabel) {
	this.neo4jService = neo4jService;
	this.label = relLabel;
	this.nodeLabel = sourceLabel;
	this.sourceId=source;
    }
    public RelationIn(CrudNeo4jService neo4jService,RelationService relationService,String relLabel,Long source,String sourceLabel) {
	this.neo4jService = neo4jService;
	this.label = relLabel;
	this.nodeLabel = sourceLabel;
	this.sourceId=source;
    }
    
    /**
     * 执行方法
     */
    @Override
    public Map<String,Object> execute(Map<String, Object> targetData) {
	LoggerTool.info(logger,MapTool.mapString(targetData));
	relationService.addRel(label, sourceId.toString(),MapTool.string(targetData,ID));
	 return targetData;
    }

    @Override
    public String getLabel() {
	return label;
    }

}

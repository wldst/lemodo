package com.wldst.ruder.module.command.impl;

import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.MapTool;

/**
 * 关联两个节点关系
 * 
 * @author wldst
 *
 */
public class RelationCommand extends CommandListImpl {
    private static Logger logger = LoggerFactory.getLogger(RelationCommand.class);
    private CrudNeo4jService neo4jService;
    private RelationService relationService;
    private String label;
    private String sLabel;
    private String eLabel;
    private Long sourceId;
    private Long targetId;

    public String getsLabel() {
	return sLabel;
    }

    public void setsLabel(String sLabel) {
	this.sLabel = sLabel;
    }

    /**
     * 
     * @param neo4jService
     * @param source       当source为Long类型时，直接建立关系，
     *                     当source为String时，则需要结合NodeLabel找到节点ID
     * @param sourceLabel
     * @param relLabel
     */
    public RelationCommand(CrudNeo4jService neo4jService, String relLabel, 
	    String source, String sourceLabel,
	    String target, String targetLabel) {
	this.neo4jService = neo4jService;
	this.label = relLabel;
	this.sLabel = sourceLabel;
	this.eLabel = sourceLabel;
	this.setELabel(targetLabel);
	try {
	    this.sourceId = Long.valueOf(source);
	} catch (Exception e) {
	    this.sourceId = neo4jService.getNodeId("taret", source, this.sLabel);
	}
	
	try {
	    this.targetId = Long.valueOf(target);
	} catch (Exception e) {
	    this.targetId = neo4jService.getNodeId(LABEL, target, this.eLabel);
	}
    }
    
    public RelationCommand(CrudNeo4jService neo4jService, String relLabel, 
	    String sourceLabel, String targetLabel) {
	this.neo4jService = neo4jService;
	this.label = relLabel;
	this.sLabel = sourceLabel;
	this.eLabel = sourceLabel;
	this.setELabel(targetLabel);
    }

    public RelationCommand(CrudNeo4jService neo4jService, String relLabel, 
	    Long source, String sourceLabel,
	    Long target, String targetLabel) {
	this.neo4jService = neo4jService;
	this.label = relLabel;
	this.sLabel = sourceLabel;
	this.setELabel(targetLabel);
	this.sourceId = source;
	this.targetId = target;
    }

    /**
     * 执行方法
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> targetData) {
	LoggerTool.info(logger,MapTool.mapString(targetData));
	relationService.addRel(label, sourceId, targetId);
	return targetData;
    }

    @Override
    public String getLabel() {
	return label;
    }

    public String getELabel() {
	return eLabel;
    }

    public void setELabel(String eLabel) {
	this.eLabel = eLabel;
    }

    public RelationService getRelationService() {
        return relationService;
    }

    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }
    
    

}

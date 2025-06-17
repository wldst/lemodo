package com.wldst.ruder.module.command.impl;

import java.util.Map;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.command.CRUDCommand;
import com.wldst.ruder.util.MapTool;
/**
 * 根据某些数据查询节点Id，使用场景：
 * 查询开始结束节点的ID。比如元数据查询。
 * @author wldst
 *
 */
public class QueryCommand extends MapTool implements CRUDCommand {

    private CrudNeo4jService neo4jService;
    private String label;

    public QueryCommand(CrudNeo4jService neo4jService,String label) {
	this.neo4jService = neo4jService;
	this.label = label;
    }

    /**
     * 执行方法
     */
    @Override
    public Map<String,Object> execute(Map<String, Object> relationInfoData) {
	neo4jService.findBy(label, label, label);
	 return relationInfoData;
    }

    @Override
    public Map<String, Object> executeChild(Map<String, Object> data, Map<String, Object> parentData) {
	// TODO Auto-generated method stub
	return null;
    }

}

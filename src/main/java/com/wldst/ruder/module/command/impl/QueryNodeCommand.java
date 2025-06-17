package com.wldst.ruder.module.command.impl;

import java.util.Map;

import org.neo4j.graphdb.Node;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.command.CRUDCommand;
/**
 * 根据某些数据查询节点Id，使用场景：
 * 查询开始结束节点的ID。比如元数据查询。
 * @author wldst
 *
 */
public class QueryNodeCommand extends CommandListImpl implements CRUDCommand {

    private CrudNeo4jService neo4jService;
    private String label;
    private String key;
    private String value;

    public QueryNodeCommand(CrudNeo4jService neo4jService,String label,String key,String value) {
	this.neo4jService = neo4jService;
	this.label = label;
	this.key = key;
	this.value = value;
    }

    /**
     * 执行方法
     */
    @Override
    public Map<String,Object> execute(Map<String, Object> relationInfoData) {
	relationInfoData=super.execute(relationInfoData);
	Node findBy = neo4jService.findBy(key, value, label);
	relationInfoData.put(label+ID, findBy.getId());
	 return relationInfoData;
    }

}

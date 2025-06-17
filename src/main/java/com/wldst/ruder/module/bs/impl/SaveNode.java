package com.wldst.ruder.module.bs.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.CrudUtil;
/**
 * 保存命令
 * @author wldst
 *
 */
public class SaveNode extends ShellListImpl {
    public SaveNode(CrudNeo4jService neo4jService, String label) {
	super();
	this.neo4jService = neo4jService;
	this.label = label;
    }

    private CrudNeo4jService neo4jService;
    private RelationService relationService;
    private String label;
    private CrudUtil crudUtil;
    private Map<String,String> transKey= new HashMap<>();
    private Map<String,Object> bizValue= new HashMap<>();
    
    public CrudNeo4jService getNeo4jService() {
        return neo4jService;
    }

    public void setNeo4jService(CrudNeo4jService neo4jService) {
        this.neo4jService = neo4jService;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }



    /**
     * 执行方法
     * @throws DefineException 
     */
    @Override
    public Map<String, Object> execute(Map<String, Object> domainData){	
	Map<String, Object> newData = null;
	try {
	    newData = tranColumn(domainData);
	    bizValue(newData);
	    neo4jService.saveByBody(newData, label);
	} catch (DefineException e) {
	    e.printStackTrace();
	}
	Map<String, Object> execute = super.execute(newData);
	if(execute!=null&&!execute.isEmpty()) {
	    return execute;
	}
	return newData;
    }
    /**
     * 转换列数据
     * @param domainData
     * @return
     * @throws DefineException
     */
    public Map<String, Object> tranColumn(Map<String, Object> domainData) throws DefineException {
	Map<String, Object> newData = new HashMap<>();
	//需要转的列数据复制
	for(Entry<String,String> trani: transKey.entrySet()) {
	    String sKey = trani.getKey();
	    String tKey = trani.getValue();
	    newData.put(tKey, domainData.get(sKey));
	}
	//复制同名的列数据
	Set<String> columns2 = crudUtil.getPoColumnSet(label);
	for(Entry<String, Object> ei:domainData.entrySet()) {
	    if(columns2.contains(ei.getKey())&&!ei.getKey().equals(ID)) {
		newData.put(ei.getKey(),ei.getValue());
	    }	    
	}
	return newData;
    }
    public void bizValue(Map<String, Object> domainData) {
	for(Entry<String,Object> bizi: bizValue.entrySet()) {
	    String key = bizi.getKey();
	    String value = String.valueOf(bizi.getValue());
//	    if(value.indexOf("{label}")>0) {
//		value=value.replace("{label}",label);
//	    }	    
	    domainData.put(key, value);
	}
    }

    public Map<String, String> getTransKey() {
        return transKey;
    }

    public void setTransKey(Map<String, String> transKey) {
        this.transKey = transKey;
    }

    public Map<String, Object> getBizValue() {
        return bizValue;
    }

    public void setBizValue(Map<String, Object> bizValue) {
        this.bizValue = bizValue;
    }
    
    public CrudUtil getCrudUtil() {
        return crudUtil;
    }

    public void setCrudUtil(CrudUtil crudUtil) {
        this.crudUtil = crudUtil;
    }

    public RelationService getRelationService() {
        return relationService;
    }

    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }
}

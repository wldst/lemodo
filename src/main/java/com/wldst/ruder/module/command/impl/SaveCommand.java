package com.wldst.ruder.module.command.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.command.CRUDCommand;
import com.wldst.ruder.util.CrudUtil;
/**
 * 保存命令
 * @author wldst
 *
 */
public class SaveCommand extends CommandListImpl implements CRUDCommand {
    private CrudNeo4jService neo4jService;
    private String label;
    private Map<String,String> transKey= new HashMap<>();
    private Map<String,Object> bizValue= new HashMap<>();
    private CrudUtil crudUtil;
    
    public SaveCommand(CrudNeo4jService neo4jService, String label,CrudUtil crudUtil) {
	this.neo4jService = neo4jService;
	this.label = label;
	this.crudUtil = crudUtil;
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
	    Map<String, Object> changeData = new HashMap<>();
	    for(Entry<String,Object> ei:domainData.entrySet()) {
		String vari = "{"+ei.getKey()+"}";
		for(Entry<String,Object> di:newData.entrySet()) {
		    String valueOf = String.valueOf(di.getValue());
		    if(valueOf.contains(ei.getKey())&&valueOf.indexOf(vari)>0) {
			String newV = valueOf.replace(vari,string(domainData,ei.getKey()));
			changeData.put(di.getKey(), newV);
		    }
		}
	    }
	    if(!changeData.isEmpty()) {
		newData.putAll(changeData);
	    }
	    neo4jService.saveByBody(newData, label);
	} catch (DefineException e) {
	    e.printStackTrace();
	}
	return super.execute(newData);
    }
    
    public Map<String, Object> tranColumn(Map<String, Object> domainData) throws DefineException {
	Map<String, Object> newData = new HashMap<>();
	for(Entry<String,String> trani: transKey.entrySet()) {
	    String sKey = trani.getKey();
	    String tKey = trani.getValue();
		Object sourceValue = domainData.get(sKey);		
		newData.put(tKey, sourceValue);
	}
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
	    if(value.indexOf("{label}")>0) {
		value=value.replace("{label}",label);
	    }	    
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
    
    @Override
    public String getLabel() {
	return label;
    }


}

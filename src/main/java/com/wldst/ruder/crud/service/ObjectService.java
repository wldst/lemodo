package com.wldst.ruder.crud.service;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.CrudUtil;
@Service
public class ObjectService {
	@Autowired
	private CrudNeo4jService neo4jService;
	@Autowired
	private CrudUtil crudUtil;
	private final static Logger logger=LoggerFactory.getLogger(ObjectService.class);
    /**
     * 查询数据，根据条件查询所有数据,模糊查询
     * 
     * @param vo
     * @param label
     * @return
     */
	public List<Map<String,Object>> query(JSONObject vo,String label){
		String[] columns;
		try {
			columns = crudUtil.getMdColumns(label);
			String query= Neo4jOptCypher.listAllObject2(vo, label, columns).toString();
			List<Map<String, Object>> query2 = neo4jService.query(query,vo);
			return query2;
		} catch (DefineException e) {
			e.printStackTrace();
		}
		return null;
	}

    /**
     * 精确获取
     * 
     * @param vo
     * @param label
     * @return
     */
    @ServiceLog(description="获取label对应的数据，并根据元数据中的字段信息返回")
    public List<Map<String, Object>> getBy(JSONObject vo, String label) {
		String[] columns;
		try {
		    columns = crudUtil.getMdColumns(label);
		    return getColumnsBy(vo, label, columns);
		} catch (DefineException e) {
		    e.printStackTrace();
		}
		return null;
    }

	public List<Map<String, Object>> getBy(Map<String,Object> vo, String label) {
		String[] columns;
		try {
			columns = crudUtil.getMdColumns(label);
			return getColumnsBy(vo, label, columns);
		} catch (DefineException e) {
			e.printStackTrace();
		}
		return null;
	}
    
    @ServiceLog(description="获取自定义字段信息")
    public List<Map<String, Object>> getFieldInfo(String labelPo) {
	JSONObject vo = new JSONObject();
	vo.put("poId", labelPo);
	// 查询自定义字段数据
	List<Map<String, Object>> fieldInfoList = getBy(vo, "Field");
	return fieldInfoList;
    }
    @ServiceLog(description="获取label下的所有的信息，返回对应字段信息")
    public List<Map<String, Object>> getColumnsBy(JSONObject vo, String label, String[] columns) {
		String query = Neo4jOptCypher.getAllObject(vo, label, columns).toString();
	    List<Map<String, Object>> query2 = neo4jService.cypher(query);
	    return query2;
	}

	public List<Map<String, Object>> getColumnsBy(Map<String, Object> vo, String label, String[] columns) {
		String query = Neo4jOptCypher.getAllObject(vo, label, columns).toString();
		LoggerTool.info(logger, " cypher========="+query+"===");
		List<Map<String, Object>> query2 = neo4jService.cypher(query);
		return query2;
	}

}

package com.wldst.ruder.module.goods;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.GoodsDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

/**
 * 主要实现商品的序列化，传递，反序列化Neo4j
 * 
 * @author wldst
 *
 */
@RestController
@ResponseBody
@RequestMapping("${server.context}/product")
public class ProductController extends GoodsDomain {

    @Autowired
    private CrudNeo4jService neo4jService;


    @RequestMapping(value = "/clearData", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult service() throws DefineException {
//	String clearNonProjectData="";
//	neo4jService.execute(QUERY_SUCCESS)
	String queryNP ="""
			match (m:MetaData)-[*1..2]-(p:resource)
			where p.code='nonProductResource'
			return distinct m.label
			""";
	List<Map<String, Object>> npIdList = neo4jService.cypher(queryNP);
	Set<String> npSet = MapTool.stringSet(npIdList, LABEL);
	
	String queryProduct ="""
		match (m:MetaData)-[*1..2]-(p:resource)
			where p.code='productResource'
			return distinct m.label
		""";
	List<Map<String, Object>> pIdList = neo4jService.cypher(queryProduct);
	Set<String> pSet = MapTool.stringSet(pIdList, LABEL);
	Set<String> needClearDataSet=new HashSet<>();
	for(String mi: npSet) {
	   if( !pSet.contains(mi)) {
	       needClearDataSet.add(mi);
	   }
	}
	
	for(String li:needClearDataSet) {
	    String deleter ="  match (d:"+li+")-[r]-(e) delete r ";
	    neo4jService.execute(deleter);
	    String delete ="  match (d:"+li+") delete d ";
	    neo4jService.execute(delete);
	}
	
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }
}

package com.wldst.ruder.module.fun.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.database.service.DbShowService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

/**
 * 操作控制器：主要用途是直接操作Neo4j
 * 
 * @author wldst
 *
 */
@RestController
@ResponseBody
@RequestMapping("${server.context}/admin")
public class OperateController extends MapTool {

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private DbShowService dbShowService;

    /**
     * 自定义查询功能
     * 
     * @param vo
     * @return
     * @throws DefineException
     */
    @RequestMapping(value = "/query", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult customQuery(@RequestBody JSONObject vo) throws DefineException {
	PageObject page = crudUtil.validatePage(vo);
	String query = (String) vo.get("cypher");
	page.setTotal(crudUtil.total(query,vo));
	List<Map<String, Object>> query2 = neo4jService.cypher(query);
	return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    /*@RequestMapping(value = "/queryResult/{cypherId}", method = { RequestMethod.POST,
        RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public String queryResult(Model model, @PathVariable("cypherId") String cypherId) throws DefineException {
    
    String params = "params";
    Map<String, Object> cypherDefine = neo4jService.getAttMapBy(LABEL, CYPHER_ACTION, META_DATA);
    if (cypherDefine == null || cypherDefine.isEmpty()) {
        throw new DefineException(CYPHER_ACTION + "未定义！");
    }
    Map<String, Object> cypherMap = neo4jService.getPropMapByNodeId(Long.valueOf(cypherId));
    if (cypherMap == null || cypherMap.isEmpty()) {
        throw new DefineException(cypherId + "不存在！");
    }
    String cql = MapTool.string(cypherMap, CYPHER).trim();
    
    String param = MapTool.string(cypherMap, params);
    // dbShowService.table2(model, tableMap, true);
    Map<String, Object> cqlMetaInfo = null;
    String columns = MapTool.string(cypherMap, COLUMNS);
    String headers = MapTool.string(cypherMap, HEADER);
    
    String[] split = cql.split(" return ");
    if (split.length > 1) {
        String[] returnList = split[1].split(",");
        if (returnList != null && returnList.length > 0 && StringUtils.isBlank(columns)) {
    	StringBuilder sb = new StringBuilder();
    	for (String ri : returnList) {
    	    if (!sb.isEmpty()) {
    		sb.append(",");
    	    }
    	    sb.append(ri);
    	}
    	cypherMap.put(COLUMNS, sb.toString());
    	cypherMap.put(HEADER, sb.toString());
        }
    }
    
    model.addAttribute("sqlId", MapTool.string(cypherMap, ID));
    dbShowService.table2(model, cypherMap, false);
    
    // dbShowService.tableToolBtn(model, tableName, tableMap);
    
    List<Map<String, Object>> query2 = neo4jService.cypher(cql);
    return "layui/cypherResult";
    }
    
    @ResponseBody
    @RequestMapping(value = "/query/{cypherId}/data", method = { RequestMethod.GET, RequestMethod.POST })
    public WrappedResult  queryData(Model model, @PathVariable("cypherId") String cypherId, HttpServletRequest request)
        throws Exception {
    PageObject page = new PageObject();
    Map<String, Object> cypherDefine = neo4jService.getAttMapBy(LABEL, CYPHER_ACTION, META_DATA);
    if (cypherDefine == null || cypherDefine.isEmpty()) {
        throw new DefineException(CYPHER_ACTION + "未定义！");
    }
    Map<String, Object> cypherMap = neo4jService.getPropMapByNodeId(Long.valueOf(cypherId));
    if (cypherMap == null || cypherMap.isEmpty()) {
        throw new DefineException(cypherId + "不存在！");
    }
    String cql = MapTool.string(cypherMap, CYPHER).trim();
    
    
    //	dbShowService.table2(model, tableMap, true);
    List<Map<String, Object>> retData =null;
    //	JSONObject paramJSON = JSON.parseObject(param);
    if(sql.startsWith("select")) {
        List<Map<String, Object>> query2 = neo4jService.cypher(cql);
       
        if(retData!=null) {
    	page.setTotal(neo4jService..size());
        }
        
        page.setPageNum(1);
        page.setPageSize(10);
        return ResultWrapper.wrapResult(true, retData, page, QUERY_SUCCESS);
    }
    return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }*/

    /**
     * 执行cypher语句，无返回结果
     * 
     * @param vo
     * @return
     * @throws DefineException
     */
    @RequestMapping(value = "/excute", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult excute(@RequestBody JSONObject vo) throws DefineException {
	PageObject page = crudUtil.validatePage(vo);
	String query = (String) vo.get("cypher");
	page.setTotal(crudUtil.total(query,vo));
	neo4jService.execute(query);
	return ResultWrapper.wrapResult(true, null, page, QUERY_SUCCESS);
    }

    /**
     * 批量执行cypher语句，无返回结果
     * 
     * @param vo
     * @return
     * @throws DefineException
     */
    @RequestMapping(value = "/cyphers", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult cyphers(@RequestBody JSONObject vo) throws DefineException {
	List<String> querys = (List<String>) vo.get("cypher");
	for (String ri : querys) {
	    /*
	     * "MATCH (a:startLabel{code:"codea"}), (b:endLabel{code:"codeb"}) CREATE
	     * (a)-[r:label{name:"relationName"}]->(b)
	     */

	    String[] match = ri.split("CREATE");
	    String[] abNode = match[0].split("\\),\\(");
	    StringBuilder sBuilder = new StringBuilder();
	    String[] arb = match[1].split("\\)\\-\\[");
	    String[] rb = arb[1].split("\\->");
	    String relation = rb[0];

	    String matchR = abNode[0] + ")-[" + relation + "->(" + abNode[1];
	    sBuilder.append(matchR);
	    sBuilder.append(" return r");
	    List<Map<String, Object>> query = neo4jService.cypher(sBuilder.toString());
	    if (query.size() > 1) {
		neo4jService.cypher(matchR + " delete r");
		neo4jService.execute(ri);
	    } else if (query.size() < 1) {
		neo4jService.execute(ri);
	    }
	}
	return ResultWrapper.wrapResult(true, null, null, EXECUTE_SUCCESS);
    }
}

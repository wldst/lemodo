package com.wldst.ruder.module.fun.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ControllerLog;
import com.wldst.ruder.constant.Msg;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CypherShowService;
import com.wldst.ruder.domain.LayUIDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.PageUtil;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.StringGet;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/cypher")
public class CypherController extends LayUIDomain{

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private PageUtil pageUtil;
    @Autowired
    private Neo4jOptByUser nou;

    @Autowired
    private CypherShowService cypherShowService;

    @ControllerLog(description = "cypher 查询结果，或者Cypher执行")
    @ResponseBody
    @RequestMapping(value = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public WrappedResult cyperData(Model model, @RequestBody JSONObject vo,
                                   HttpServletRequest request) throws Exception{
        PageObject page=crudUtil.validatePage(vo);
        String cypher=string(vo, CYPHER).trim();

        // String param = string(poMap, "params");
        // cypherShowService.table2(model, tableMap, true);
        // JSONObject paramJSON = JSON.parseObject(param);
        if(cypher.toLowerCase().indexOf(" return ")>0){
            String lowerCase=cypher.toLowerCase();
            List<Map<String, Object>> dataList=null;
            if(lowerCase.indexOf(" limit ")>0||lowerCase.indexOf(" skip ")>0){
                dataList=neo4jService.cypher(cypher);
            }else{
                String skipPage=nou.appendPage(page, cypher);
                dataList=neo4jService.cypher(skipPage);
            }

            if(dataList!=null){
                for(Map<String, Object> mi : dataList){
                    DateTool.replaceTimeLong2DateStr(mi);
                }
                if(!dataList.isEmpty()){
                    page.setTotal(pageUtil.pageTotal(cypher));
                }
            }
            return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
        }else{
            neo4jService.execute(cypher);
            return ResultWrapper.wrapResult(true, null, null, Msg.EXECUTE_SUCCESS);
        }

    }

    @ControllerLog(description = "cypher 查询结果，或者Cypher执行")
    @ResponseBody
    @RequestMapping(value = "/query/{cypherId}/data", method = {RequestMethod.GET, RequestMethod.POST})
    public WrappedResult queryData(Model model, @PathVariable("cypherId") String cypherId, @RequestBody JSONObject vo,
                                   HttpServletRequest request) throws Exception{
        PageObject page=crudUtil.validatePage(vo);
        Map<String, Object> dataSetDefine=neo4jService.getAttMapBy(LABEL, CYPHER_ACTION, META_DATA);
        if(dataSetDefine==null||dataSetDefine.isEmpty()){
            throw new DefineException(CYPHER_ACTION+"未定义！");
        }
        Map<String, Object> poMap=neo4jService.getPropMapByNodeId(Long.valueOf(cypherId));
        if(poMap==null||poMap.isEmpty()){
            throw new DefineException(cypherId+"不存在！");
        }
        String cypher=string(poMap, CYPHER).trim();

        // String param = string(poMap, "params");
        // cypherShowService.table2(model, tableMap, true);
        // JSONObject paramJSON = JSON.parseObject(param);
        if(cypher.toLowerCase().indexOf(" return ")>0||cypher.toLowerCase().indexOf("\nreturn ")>0){
            String lowerCase=cypher.toLowerCase();
            List<Map<String, Object>> dataList=null;
            if(lowerCase.indexOf(" limit ")>0||lowerCase.indexOf(" skip ")>0){
                dataList=neo4jService.cypher(cypher);
            }else{
                String skipPage=nou.appendPage(page, cypher);
                dataList=neo4jService.cypher(skipPage);
            }

            if(dataList!=null){
                for(Map<String, Object> mi : dataList){
                    DateTool.replaceTimeLong2DateStr(mi);
                }
                if(!dataList.isEmpty()){
                    page.setTotal(pageUtil.pageTotal(cypher));
                }
            }
            return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
        }else{
            neo4jService.execute(cypher);
            return ResultWrapper.wrapResult(true, null, null, Msg.EXECUTE_SUCCESS);
        }

    }

    @ControllerLog(description = "queryByCqlId")
    @RequestMapping(value = "/queryById/{cypherId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String queryBySqlId(Model model, @PathVariable("cypherId") String cypherId, HttpServletRequest request)
            throws Exception{
        Map<String, Object> dataSetDefine=neo4jService.getAttMapBy(LABEL, CYPHER_ACTION, META_DATA);
        if(dataSetDefine==null||dataSetDefine.isEmpty()){
            throw new DefineException(CYPHER_ACTION+"未定义！");
        }
        Map<String, Object> cypherMap=neo4jService.getPropMapByNodeId(Long.valueOf(cypherId));
        if(cypherMap==null||cypherMap.isEmpty()){
            throw new DefineException(cypherId+"不存在！");
        }
        String cypher=string(cypherMap, CYPHER).trim();
        String returnColumn="";
        String rix="return,RETURN , RETURN , return ,\r\nreturn\n,\r\nRETURN\n,\nreturn\n,\nRETURN\n,return\n,RETURN\n";
        for(String ri : rix.split(",")){
            returnColumn=getReturn(cypher, ri);
            if(returnColumn!=null&&returnColumn.length()>1){
                break;
            }
        }
        if(returnColumn!=null&&!"".equals(returnColumn)){

            String lowCypher=returnColumn.toLowerCase();
            int indexOfOrder=lowCypher.indexOf(" order by ");
            if(indexOfOrder>0){
                returnColumn=returnColumn.split(" order ")[0];
            }
            int indexOfLimit=lowCypher.indexOf(" skip ");
            if(indexOfLimit>0){
                returnColumn=lowCypher.split(" skip ")[0];
            }
            String[] columnList=returnColumn.split(",");
            // cypherShowService.table2(model, tableMap, true);
            String columns=string(cypherMap, COLUMNS);
            if(columnList!=null&&columnList.length>0&&StringUtils.isBlank(columns)){
                StringBuilder sb=new StringBuilder();
                StringBuilder sb2=new StringBuilder();
                for(String ci : columnList){
                    if(!sb.isEmpty()){
                        sb.append(",");
                        sb2.append(",");
                    }
                    sb2.append("String");
                    String lowerCase=ci.toLowerCase();
                    if(lowerCase.contains(" as ")){
                        String[] split=lowerCase.split(" as ");
                        sb.append(split[1].trim());
                    }else if(ci.contains(".")){
                        sb.append(ci.split("\\.")[1].trim());
                    }else{
                        sb.append(ci);
                    }
                }
                cypherMap.put(COLUMNS, sb.toString());
                cypherMap.put(HEADER, sb.toString());
                cypherMap.put(COLUMN_TYPE, sb2.toString());
            }
        }

        model.addAttribute("domainId", string(cypherMap, ID));
        model.addAttribute("label", "Action");
        cypherShowService.table2(model, cypherMap, false);
        // cypherShowService.tableToolBtn(model, tableName, tableMap);
        return "table/cypherResult";
    }

    public String getReturn(String cypher, String ri){
        String returnColumn="";
        if(cypher.contains(ri)){
            String[] split=cypher.split(ri);
            if(split.length>1){
                returnColumn=split[1];
            }
        }
        return returnColumn;
    }

    @ResponseBody
    @RequestMapping(value = "/importData", method = {RequestMethod.GET, RequestMethod.POST})
    public WrappedResult importData(Model model, @RequestBody JSONObject vo, HttpServletRequest request)
            throws Exception{
        Set<String> idList=new HashSet<>();

        for(String key : vo.keySet()){
            Object object=vo.get(key);
            if(object instanceof List di){
                List<Map<String, Object>> listMap=listObjectMap(vo, key);
                for(Map<String, Object> dij : listMap){
                    Set<String> keySet=dij.keySet();
                    if(keySet.contains(null)){
                        keySet.remove(null);
                    }
                    if(keySet.isEmpty()){
                        continue;
                    }
                    String join=String.join(",", keySet);
                    Map<String, Object> dataMap=new HashMap<>();
                    dataMap.put(LABEL, key);
                    dataMap.put(COLUMNS, join);
                    dataMap.put(HEADER, join);
                    neo4jService.save(dataMap, META_DATA);
                    List<String> jjList=new ArrayList<>();
                    for(String kk : keySet){
                        jjList.add("n."+kk);
                    }
                    String cypherString="match(n:"+key+") return "+String.join(",", jjList)+"";

                    Map<String, Object> cypherMap=new HashMap<>();
                    cypherMap.put(NAME, "dataImpor"+key);
                    cypherMap.put(CYPHER, cypherString);
                    Node cyphNode=neo4jService.saveByBody(cypherMap, CYPHER_ACTION);

                    idList.add(cyphNode.getId()+"");
                    for(String ki : keySet){
                        // System.out.println(ki);
                        if(ki!=null&&dij.get(ki) instanceof Map mi){
                            // dij.remove(ki);
                            dij.put(ki, mapString(mi));
                        }
                        if(ki!=null&&dij.get(ki) instanceof List li){
                            if(!li.isEmpty()){
                                dij.put(ki, listMapString(li));
                            }else{
                                dij.put(ki, null);
                            }
                        }
                        if(ki!=null&&dij.get(ki) instanceof ArrayList li){
                            if(!li.isEmpty()){
                                dij.put(ki, listMapString(li));
                            }else{
                                dij.put(ki, null);
                            }
                        }
                    }

                    neo4jService.saveByBody(dij, key);
                }
            }
        }
        return ResultWrapper.wrapResult(true, String.join(",", idList), null, QUERY_SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/timeFormat/{prop}", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult timeFormat(@PathVariable("prop") String prop) throws DefineException{
        if(prop==null||!StringGet.isLetterDigit(prop)){
            return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
        }
        String query="match(n) where n."+prop+" contains \"-\" OR n."+prop+" contains \":\" ";
        String qCount=query+" return count(n) as nu ";
        List<Map<String, Object>> count=neo4jService.cypher(qCount);
        Long valueOf=Long.valueOf(String.valueOf(count.get(0).get("nu")));
        int step=100;
        for(int i=0, skip=0; skip<valueOf; i++){
            skip=i*step;
            updateProp(prop, query, skip, step);
        }

        return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }

    private void updateProp(String prop, String query, int skip, int step){
        String qret=query+" return n."+prop+",id(n) AS id skip "+skip+" limit "+step+" ";
        List<Map<String, Object>> query2=neo4jService.cypher(qret);
        if(query2.isEmpty()){
            return;
        }
        Map<String, Long> idDataMap=new HashMap<>();
        Map<String, Object> paraMap=new HashMap<>();
        StringBuilder sBuilder=new StringBuilder();
        for(Map<String, Object> node : query2){
            Long dateValue=dateLongValue(node, prop);
            Long longValue=id(node);
            idDataMap.put(longValue+"", dateValue);
        }
        paraMap.put("batch", idDataMap);
        String batchUpdateString="""
                WITH {batch} as data, [k in keys({batch}) | toInt(k)] as ids
                  MATCH (n) WHERE id(n) IN ids
                """;
        sBuilder.append(batchUpdateString);
        sBuilder.append(" SET n."+prop+" = data[toString(id(n))]");
        neo4jService.executeBatch(sBuilder.toString(), paraMap);
        //
    }
}

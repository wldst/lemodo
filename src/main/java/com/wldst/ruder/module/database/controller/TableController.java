package com.wldst.ruder.module.database.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.module.database.DbServiceImpl;
import com.wldst.ruder.module.database.util.DBOptUtil;
import com.wldst.ruder.module.database.util.ParseSqlJoin;

import jakarta.servlet.http.HttpServletRequest;
import oracle.sql.TIMESTAMP;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/table")
public class TableController extends DataBaseDomain{
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private DbInfoService dbInfoGather;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private RelationService relationService;

    @Autowired
    private DbServiceImpl dbService;

    final static Logger logger=LoggerFactory.getLogger(TableController.class);

    /**
     * 查询方法，用于处理特定数据表的查询请求。
     *
     * @param table 数据表名称，从请求路径中提取。
     * @param vo    包含查询参数的JSON对象，从请求体中提取。
     * @return 返回一个WrappedResult对象，包含查询结果和相关元数据。
     * @throws DefineException 如果查询过程中发生定义的异常。
     */
    @ResponseBody
    @RequestMapping(value = "/{table}/query", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult query(@PathVariable("table") String table, @RequestBody JSONObject vo) throws DefineException{
        // 验证并获取分页对象
        PageObject page=crudUtil.validatePage(vo);
        // 获取指定表的列名数组
        String[] columns=crudUtil.getTableColumn(table);
        // 如果列名数组为空，则查询失败
        if(columns==null||columns.length<=0){
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        // 移除用于分页的参数
        vo.remove("page");
        // 初始化模糊查询字段集合
        Set<String> like=new HashSet<>();
        like.add("name");
        like.add("code");
        like.add("title");
        // 构造分页查询SQL语句
        String query=DBOptUtil.queryPage(dbInfoGather.getDbType(), vo, table, columns, page, like);
        // 执行查询并获取结果列表
        List<Map<String, Object>> dataList;
        List<Map<String, Object>> dataHandleList;
        try{
            dataList=dbInfoGather.query(query);
            // 如果查询结果不为空，则处理查询结果
            if(dataList!=null){
                dataHandleList=new ArrayList<Map<String, Object>>();
                for(Map<String, Object> mpMap : dataList){
                    Map<String, Object> mpMap2=new HashMap<>();
                    mpMap2.putAll(mpMap);
                    // 处理查询结果中的时间戳和列名
                    for(Entry<String, Object> ei : mpMap.entrySet()){
                        Object value=ei.getValue();
                        if(value instanceof TIMESTAMP t){
                            ei.setValue(DateTool.dateStr(t.dateValue().getTime()));
                        }
                        for(String ci : columns){
                            String dbKey=ei.getKey();
                            if(dbKey.equalsIgnoreCase(ci)&&!dbKey.equals(ci)){
                                mpMap2.put(ci, ei.getValue());
                                mpMap2.remove(dbKey);
                            }
                        }
                    }
                    dataHandleList.add(mpMap2);
                }
                // 计算总记录数
                int total=dbInfoGather.count(DBOptUtil.total(dbInfoGather.getDbType(), vo, table, columns, null));
                page.setTotal(total);

                // 查询成功，返回处理后的查询结果
                return ResultWrapper.wrapResult(true, dataHandleList, page, QUERY_SUCCESS);
            }
        }catch(Exception e){
            e.printStackTrace();
            String message=e.getMessage();

            // 查询失败，返回错误信息
            ResultWrapper.wrapResult(false, null, null, message);
        }
        // 默认查询失败响应
        return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);

    }

    // 通过table名称更新所有相关数据
    @ResponseBody
    @RequestMapping(value = "/{table}/updateAll", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult updateAll(@PathVariable("table") String table) throws DefineException{
        // 获取指定表的所有列名
        String[] columns=crudUtil.getTableColumn(table);
        // 如果列名为空，则返回查询失败的结果
        if(columns==null||columns.length<=0){
            return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
        }
        // 获取指定表的属性映射
        Map<String, Object> tableMap=neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);

        // 构造查询所有数据的SQL语句
        String query=DBOptUtil.listAll(table, columns).toString();
        List<Map<String, Object>> dataList;
        try{
            // 执行查询
            dataList=dbInfoGather.query(query);
            // 如果查询结果不为空，则遍历每个查询结果进行更新操作
            if(dataList!=null){
                for(Map<String, Object> dui : dataList){
                    // 获取组织名称、岗位名称和ID
                    String orgName=string(dui, "ORGNAME");
                    String postName=string(dui, "POSTNAME");
                    String ID=string(dui, "ID");
                    // 根据组织名称和岗位名称生成HTML文件路径
                    File dirWordHtml=dirWordHtml("D:\\liuqiang\\pmis\\安全签约\\安全目标责任书模板", orgName, postName);
                    // 如果HTML文件存在，则读取文件内容并更新数据库
                    if(dirWordHtml!=null){
                        String readFile=FileOpt.readFile(dirWordHtml);
                        Map<String, Object> mpMap2=new HashMap<>();
                        LoggerTool.info(logger, "content:"+readFile);
                        mpMap2.put("SAFEDUTY", readFile);
                        mpMap2.put("ID", ID);
                        StringBuilder sb=new StringBuilder();
                        // 构造更新语句
                        List<String> updateById=DBOptUtil.updateById(mpMap2, table, tableMap, sb);
                        LoggerTool.info(logger, "==ID={}=======update sql:{},\n====keys:\n{}", ID, sb.toString(), String.join(",", updateById));
                        // 执行更新操作
                        Boolean prepareExcute=dbInfoGather.prepareExcute(sb.toString(), mpMap2, updateById, columTypeMap(tableMap));
                        LoggerTool.info(logger, "===============update result {} :sql={}", prepareExcute, sb.toString());

                    }
                }
                return ResultWrapper.wrapResult(true, "", null, QUERY_SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();

            ResultWrapper.wrapResult(false, null, null, message);
        }
        return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);

    }
                // 更新操作成功，返回查询成功的结

                @ResponseBody
                @RequestMapping(value = "/{table}/toMeta", method = {RequestMethod.POST,
                        RequestMethod.GET}, produces = "application/json;charset=UTF-8")
                public WrappedResult toMeta (@PathVariable("table") String table) throws DefineException {
                    dbService.tableMappingMeta(table);
                    return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
                }


                private File dirWordHtml (String filepath, String orgName, String postName){
                    String fileName;
                    File dir=new File(filepath);

                    for(File fi : dir.listFiles()){
                        if(fi.isDirectory()){
                            File dirWordHtml=dirWordHtml(fi.getAbsolutePath(), orgName, postName);
                            if(dirWordHtml!=null){
                                return dirWordHtml;
                            }
                        }else{
                            fileName=fi.getName();
                            if(fileName.endsWith("html")&&fileName.indexOf(postName)>-1&&(fileName.indexOf(orgName)>-1||fi.getPath().indexOf(orgName)>-1)){
                                LoggerTool.info(logger, "==根据="+orgName+"="+postName+"=====找到文件====="+fileName);
                                return fi;
                            }
                        }
                    }
                    return null;
                }

                @ResponseBody
                @RequestMapping(value = "/{table}/insert", method = {RequestMethod.POST,
                        RequestMethod.GET}, produces = "application/json;charset=UTF-8")
                public WrappedResult insert (@PathVariable("table") String table, @RequestBody JSONObject vo)
	    throws DefineException {

                    Map<String, Object> tableMap=neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
                    if(tableMap==null||tableMap.isEmpty()){
                        throw new DefineException(table+"未定义！");
                    }

                    String[] columns=crudUtil.getTableColumn(table);
                    if(columns==null||columns.length<=0){
                        return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
                    }
                    vo.remove("page");
                    String idString=vo.getString(ID);
                    String bigId=vo.getString(ID_BIG);
                    if((idString==null||idString.isBlank())
                            &&(bigId==null||bigId.isBlank())){
                        // vo.put(ID, UUIDUtil.getUUID());
                        String columnxs=String.join(",", columns);
                        if(columnxs.toLowerCase().indexOf(",createtime,")>0){
                            vo.put("CREATETIME", DateTool.now());
                        }

                        if(columnxs.toLowerCase().indexOf(",create_time,")>0){
                            vo.put("CREATE_TIME", DateTool.now());
                        }
                        StringBuilder sBuilder=new StringBuilder();
                        List<String> keyList=DBOptUtil.insert(vo, table, tableMap, sBuilder);

                        boolean dataList=dbInfoGather.prepareExcute(sBuilder.toString(), vo, keyList, columTypeMap(tableMap));

                        return ResultWrapper.wrapResult(true, dataList, null, QUERY_SUCCESS);
                    }else{
                        int total;
                        try{
                            if(idString==null){
                                idString=bigId;
                            }
                            table.split("from");
                            String byId=DBOptUtil.getById(table, idString);

                            List<Map<String, Object>> query=dbInfoGather.query(byId);
                            total=query.size();
                            if(total>0){
                                StringBuilder sBuilder=new StringBuilder();
                                List<String> keyList=DBOptUtil.update(vo, table, tableMap, sBuilder);
                                boolean dataList=dbInfoGather.prepareExcute(sBuilder.toString(), vo, keyList, columTypeMap(tableMap));
                                return ResultWrapper.wrapResult(true, dataList, null, QUERY_SUCCESS);
                            }else{
                                String columnxs=String.join(",", columns);
                                if(columnxs.toLowerCase().indexOf(",createtime,")>0){
                                    vo.put("CREATETIME", DateTool.now());
                                }

                                if(columnxs.toLowerCase().indexOf(",create_time,")>0){
                                    vo.put("CREATE_TIME", DateTool.now());
                                }
                                StringBuilder sBuilder=new StringBuilder();
                                List<String> keyList=DBOptUtil.insert(vo, table, tableMap, sBuilder);

                                boolean dataList=dbInfoGather.prepareExcute(sBuilder.toString(), vo, keyList, columTypeMap(tableMap));

                                return ResultWrapper.wrapResult(true, dataList, null, QUERY_SUCCESS);
                            }
                        }catch(Exception e){
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }


                    }
                    return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
                }

                private Map<String, String> columTypeMap (Map<String, Object>tableMap){
                    String[] keySet=string(tableMap, COLUMNS).split(",");
                    String[] types=splitValue(tableMap, COLUMN_TYPE, TYPE_SPLITER);
                    Map<String, String> typeMap=toMap(keySet, types);
                    return typeMap;
                }

                @RequestMapping(value = "/{table}/update", method = {RequestMethod.POST,
                        RequestMethod.GET}, produces = "application/json;charset=UTF-8")
                public WrappedResult update (@PathVariable("table") String table, @RequestBody JSONObject vo)
	    throws DefineException {
                    Map<String, Object> tableMap=neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
                    if(tableMap==null||tableMap.isEmpty()){
                        throw new DefineException(table+"未定义！");
                    }
                    PageObject page=crudUtil.validatePage(vo);
                    String[] columns=crudUtil.getTableColumn(table);
                    if(columns==null||columns.length<=0){
                        return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
                    }
                    vo.remove("page");
                    StringBuilder sBuilder=new StringBuilder();
                    DBOptUtil.queryPage(table, vo, table, columns, page, null);
                    List<String> keyList=DBOptUtil.update(vo, table, tableMap, sBuilder);
                    keyList.add(ID);
                    boolean dataList=dbInfoGather.prepareExcute(sBuilder.toString(), vo, keyList, columTypeMap(tableMap));
                    return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
                }

                @RequestMapping(value = "/{table}/sql", method = {RequestMethod.POST,
                        RequestMethod.GET}, produces = "application/json;charset=UTF-8")
                public WrappedResult tableDefineSql (@PathVariable("table") String table)
	    throws DefineException {
                    Map<String, Object> tableMap=neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
                    if(tableMap==null||tableMap.isEmpty()){
                        throw new DefineException(table+"未定义！");
                    }
                    String[] columns=crudUtil.getTableColumn(table);
                    if(columns==null||columns.length<=0){
                        return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
                    }


                    String tableSql;
                    try{
                        tableSql=dbInfoGather.tableSql(table);
                        return ResultWrapper.wrapResult(true, tableSql, null, QUERY_SUCCESS);
                    }catch(Exception e){
                        e.printStackTrace();
                        LoggerTool.error(logger, "error ", e);
                        return ResultWrapper.wrapResult(true, null, null, QUERY_FAILED);
                    }
                }


                @RequestMapping(value = "/{table}/updateField", method = {RequestMethod.POST,
                        RequestMethod.GET}, produces = "application/json;charset=UTF-8")
                public WrappedResult updateField (@PathVariable("table") String table, @RequestBody JSONObject vo)
	    throws DefineException {
                    Map<String, Object> tableMap=neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
                    if(tableMap==null||tableMap.isEmpty()){
                        throw new DefineException(table+"未定义！");
                    }
                    PageObject page=crudUtil.validatePage(vo);
                    String[] columns=crudUtil.getTableColumn(table);

                    String idSql=DBOptUtil.getIdSql(vo, table);
                    try{
                        List<Map<String, Object>> query=dbInfoGather.query(idSql);
                        if(query!=null&&query.size()>0){
                            if(columns==null||columns.length<=0){
                                return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
                            }
                            vo.remove("page");
                            StringBuilder sBuilder=new StringBuilder();
                            List<String> keyList=DBOptUtil.update(vo, table, tableMap, sBuilder);
                            Map<String, Object> map=query.get(0);
                            String updateId=string(map, ID);
                            vo.put(ID, updateId);
                            boolean dataList=dbInfoGather.prepareExcute(sBuilder.toString(), vo, keyList, columTypeMap(tableMap));
                            return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
                        }
                    }catch(Exception e){
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    return ResultWrapper.wrapResult(true, null, page, EXECUTE_FAILED);
                }

                @ResponseBody
                @RequestMapping(value = "/{table}/del", method = {RequestMethod.POST,
                        RequestMethod.GET}, produces = "application/json;charset=UTF-8")
                public WrappedResult del (@PathVariable("table") String table, @RequestBody JSONObject vo) throws
                DefineException {

                    Map<String, Object> tableMap=neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
                    if(tableMap==null||tableMap.isEmpty()){
                        throw new DefineException(table+"未定义！");
                    }
                    if(vo.containsKey(ID_BIG)){
                        String id=vo.getString(ID_BIG);
                        String delById=DBOptUtil.delById(id, table);

                        dbInfoGather.prepareExcuteById(delById, id);
                    }else{

                        String delBy=DBOptUtil.delByProp(vo, table);

                        // dbInfoGather.prepareExcute(delBy,keyList,columTypeMap(tableMap));
                    }

                    return ResultWrapper.wrapResult(true, null, null, DELETE_SUCCESS);
                }

                @ResponseBody
                @RequestMapping(value = "/parseJoin", method = {RequestMethod.GET, RequestMethod.POST})
                public WrappedResult parseJoin (Model model, @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
                    List<String> listMap=arrayList(vo, "data");
                    if(listMap!=null){
                        for(String ri : listMap){
                            String[] split=ri.split("=");
                            String start=split[0];
                            String[] startTc=start.split("\\.");
                            String end=split[1];
                            String[] endTc=end.split("\\.");
                            Map<String, Object> startTable=neo4jService.getAttMapBy(TABEL_NAME, startTc[0], LABLE_TABLE);
                            if(startTable==null||startTable.isEmpty()){
                                throw new DefineException(startTc[0]+"已存在！");
                            }
                            Map<String, Object> endTable=neo4jService.getAttMapBy(TABEL_NAME, endTc[0], LABLE_TABLE);
                            if(endTable==null||endTable.isEmpty()){
                                throw new DefineException(endTc[0]+"已存在！");
                            }
                            Map<String, Object> table=new HashMap<>();
                            table.put("info", ri);
                            relationService.addRel("join", longValue(startTable, ID), longValue(endTable, ID), table);
                        }
                    }
                    return ResultWrapper.wrapResult(true, "", null, QUERY_SUCCESS);
                }

                @ResponseBody
                @RequestMapping(value = "/parseResultSet", method = {RequestMethod.GET, RequestMethod.POST})
                public WrappedResult parseResultSet (Model model, @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
                    List<String> listMap=arrayList(vo, "data");

                    String cypher="match (m:dataSet) where m.SQL is not null \n"+"  return distinct(m.SQL) as SQL";
                    List<Map<String, Object>> query=neo4jService.cypher(cypher);
                    Set<String> relSet=new HashSet<>();
                    for(Map<String, Object> di : query){
                        String sql=string(di, "SQL");
                        relSet.addAll(ParseSqlJoin.parseSql(sql));
                    }
                    if(relSet!=null&&!relSet.isEmpty()){
                        Set<String> problemTable=new HashSet<>();
                        for(String ri : relSet){
                            String[] split=ri.split("=");
                            String start=split[0];
                            String[] startTc=start.split("\\.");
                            String end=split[1];
                            String[] endTc=end.split("\\.");
                            Map<String, Object> startTable=neo4jService.getAttMapBy(TABEL_NAME, startTc[0], LABLE_TABLE);
                            if(startTable==null||startTable.isEmpty()){
                                problemTable.add(startTc[0]);
                                continue;
                                // throw new DefineException(startTc[0] + "不存在！");
                            }
                            Map<String, Object> endTable=neo4jService.getAttMapBy(TABEL_NAME, endTc[0], LABLE_TABLE);
                            if(endTable==null||endTable.isEmpty()){
                                problemTable.add(startTc[0]);
                                continue;
                                // throw new DefineException(endTc[0] + "不存在！");
                            }
                            Map<String, Object> table=new HashMap<>();
                            table.put("info", ri);
                            table.put("name", "依赖");
                            relationService.addRel("join", id(startTable), id(endTable), table);
                        }
                        if(!problemTable.isEmpty()){
                            ResultWrapper.wrapResult(false, problemTable, null, EXECUTE_EXCEPTION);
                        }
                    }
                    return ResultWrapper.wrapResult(true, "", null, QUERY_SUCCESS);
                }
            }

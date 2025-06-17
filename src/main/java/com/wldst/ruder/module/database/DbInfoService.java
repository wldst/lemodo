package com.wldst.ruder.module.database;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSONObject;

/**
 * 数据库接口服务
 * @author wldst
 *
 */
public interface DbInfoService {

    public void initConnect(Map<String, Object> ds);

    public List<String> getTable() throws Exception;

    public List<Map<String, Object>> query(String query) throws Exception;

    public int count(String query) throws Exception;

    public List<Map<String, Object>> prepareQuery(String query, JSONObject json, List<String> keys,
	    Map<String, String> typeMap);
    public List<Map<String, Object>> prepareQuery(String query, String json);

    public void prepareExcuteById(String query, String id);
    public Boolean prepareExcute(String query, JSONObject json, List<String> keys,
	    Map<String, String> typeMap);
    public Boolean prepareExcute(String query, Map<String,Object> json, List<String> keys,
	    Map<String, String> typeMap);
    public Boolean prepareExcute(String query, String params, List<String> keys,
	    Map<String, String> typeMap);
    
    public Boolean prepareExcuteClob(String query, String params);
    
    public Boolean prepareGetClobLength(String query,String contentCols);
    
    public Boolean prepareExcute(String query, Object[] args);

    public Boolean excute(String query) throws Exception;

    public List<Map<String, Object>> work() throws Exception;

    public void copyTableInfo(String tablei);

    public Long getDsId();

    public void setDsId(Long dsId);

    public Connection getCon();

    public String getDbType();

    public void setDbType(String dbType);
    /**
     * 数据库表元信息
     * @param tableName
     * @return
     * @throws Exception 
     */
    public Map<String, Object> tableMetaInfo(String tableName) throws Exception;
    /**
     * SQL执行后的元信息
     * @param sql
     * @return
     */
    public Map<String, Object> sqlMetaInfo(String sql);
    public Map<String, Object> sqlMetaInfo(String sql,Object[] args);

    String tableSql(String tableName) throws Exception;
    public void tableMappingMeta(String tableName) throws Exception;

    public Map<String, Object> toEntity(Map<String, Object> employeeDoc, String empDocColMap);

}

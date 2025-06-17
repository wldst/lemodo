package com.wldst.ruder.module.database.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.util.Strings;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.PageObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 数据操作功能
 * 
 * @author deeplearn96
 *
 */
public class DBOptUtil extends DataBaseDomain {

    public static String delById(String id, String label) {
	if(id!=null&&id.contains(",")) {
	    String[] split = id.split(",");
	    StringBuilder sBuilder = new StringBuilder();
	    for(String idi: split) {
		if(sBuilder.length()>0) {
		    sBuilder.append(",");
		}
		sBuilder.append(" ? ");
	    }
	    return " delete from " + label + " where id in("+sBuilder.toString()+")";
	}
	return " delete from " + label + " where id = ?";
    }
    
    public static String delByProp(JSONObject props, String label) {
	StringBuilder sBuilder = new StringBuilder();
	sBuilder.append(" delete from " + label + " ");
	where(props, sBuilder, null);
	return sBuilder.toString();
    }

    /**
     * 分页查询
     * 
     * @param props
     * @param tableOrView
     * @param keySet
     * @param page
     * @return
     */
    public static String queryPage(String dbType,JSONObject props, String tableOrView, String[] keySet, PageObject page,
	    Set<String> likeKeys) {
	String querySql = querySql(dbType,props, tableOrView, keySet, likeKeys);
	
	return sqlPage(dbType, page, querySql);
    }
    
    /**
     * SQL分页查询
     * @param dbType
     * @param page
     * @param querySql
     * @return
     */
    public static String sqlPage(String dbType, PageObject page, String querySql) {
	if(dbType!=null&&dbType.toLowerCase().equals("mysql")) {
	    return getMysqlPageSql(querySql, page.getPageNum(), page.getPageSize());
	}
	return getOraclePageSql(querySql, page.getPageNum(), page.getPageSize());
    }
    
    public static String createTable(String dbType,Map<String, Object> tableMap) {
	if(dbType!=null&&dbType.toLowerCase().equals("mysql")) {
	    return createMysqlTable(tableMap);
	}
	return createOracleTable(tableMap);
    }
    
    public static String createOracleTable(Map<String, Object> tableMap) {
	String[] columns = columns(tableMap);
	String[] types = splitValue(tableMap, COLUMN_TYPE,TYPE_SPLITER);
	String[] nullAble = splitValue(tableMap, "nullAble");
	String[] sizes = splitValue(tableMap, COLUMN_SIZE);
	
//	tableName,columns,header,colTypes,nullAble,colSize
	StringBuilder sb = new StringBuilder(); 
		
	sb.append("create TABLE " + string(tableMap, TABEL_NAME)+"( ")  ;
	List<String> tableColumList=new ArrayList<>();
	
	for(int i=0;i<columns.length;i++) {
	    String typei = types[i];
	    String e = columns[i]+" "+typei;
	    if(!typei.contains("(")&&!typei.contains(")")
		    &&!typei.equalsIgnoreCase("BLOB")
		    &&!typei.equalsIgnoreCase("CLOB")) {
		e=e+"("+sizes[i]+")";
	    }
	    if(nullAble[i].toUpperCase().startsWith("N")) {
		e=e+" NOT NULL";
	    }
	    tableColumList.add(e);
	}
	for(int i=0;i<columns.length;i++) {
	    String e = columns[i];
	    if(e.equalsIgnoreCase(ID)) {
		tableColumList.add(" PRIMARY KEY ( ID)");
	    }
	}
	
	sb.append(String.join(",", tableColumList)+")");
	
	return sb.toString();
    }
    
    public static String createMysqlTable(Map<String, Object> tableMap) {
	String[] columns = splitValue(tableMap, COLUMNS);
	String[] types = splitValue(tableMap, COLUMN_TYPE,TYPE_SPLITER);
	String[] nullAble = splitValue(tableMap, "nullAble");
	
//	tableName,columns,header,colTypes,nullAble,colSize
	StringBuilder sb = new StringBuilder(); 
		
	sb.append("create TABLE IF NOT EXISTS " + string(tableMap, TABEL_NAME)+"( ")  ;
	List<String> tableColumList=new ArrayList<>();
	int k=0;
	for(int i=0;i<columns.length;i++) {
	    String e = columns[i]+" "+types[i];
	    
	    if(nullAble[i].toUpperCase().startsWith("N")) {
		e=e+" NOT NULL";
	    }
	    tableColumList.add(e);
	}
	for(int i=0;i<columns.length;i++) {
	    String e = columns[i];
	    if(e.equalsIgnoreCase(ID)) {
		tableColumList.add(" PRIMARY KEY ( id)");
	    }
	}
	
	sb.append(String.join(",", tableColumList)+")");
	
	return sb.toString();
    }
    
    public static String total(String dbType, JSONObject props, String tableOrView, String[] keySet, Set<String> likeKeys) {
	String querySql = querySql(dbType,props, tableOrView, keySet, likeKeys);
	String[] split = querySql.split(" from ");
	
	return " select count(*) from "+split[1];
    }
    
    public static String total(String querySql) {
	String[] split = querySql.split(" from ");
	return " select count(*) from "+split[1];
    }
    
    public static String isIdExist(String dbType,JSONObject props, String tableOrView, String[] keySet, Set<String> likeKeys) {
	String querySql = querySql(dbType,props, tableOrView, keySet, likeKeys);
	String[] split = querySql.split(" from ");
	
	return " select count(*) from "+split[1];
    }
    
    public static String getById(String tableOrView,String id) {
	String querySql = "select * from "+tableOrView+" where id='"+id+"'";
	return querySql;
    }

    public static String querySql(String dbType,JSONObject props, String tableOrView, String[] keySet, Set<String> likeKeys) {
	String querySql = null;
	
	    
	    if ((props == null || props.isEmpty()) && (keySet == null || keySet.length < 1)) {
		    querySql = "select * from  " + tableOrView;
		}
		else if ((props == null || props.isEmpty()) && (keySet != null || keySet.length > 0)) {
		    querySql = listAll(tableOrView, keySet).toString();
		} else {
		    if(dbType.equalsIgnoreCase("mysql")) {
			querySql = listMysql(props, tableOrView, keySet, likeKeys).toString();
		    }		    
		    if(dbType.equalsIgnoreCase("oracle")) {
			querySql = listOracle(props, tableOrView, keySet, likeKeys).toString();
		    }
		}
	return querySql;
    }
    
    public static String getIdSql(JSONObject props, String tableOrView) {
	StringBuilder querySql = new StringBuilder("select id from  " + tableOrView);
	where(props, querySql); 
	return querySql.toString();
    }

    public static List<String> update(JSONObject props, String table, Map<String, Object> tableMap, StringBuilder sql) {
	String[] keySet =string(tableMap, COLUMNS).split(",");
		List<String> keyList=update(props, table, sql, keySet);
		return keyList;
    }

	@Nullable
	private static List<String> update(JSONObject props, String table, StringBuilder sql, String[] keySet){
		List<String> keyList = null;
		if ((props!= null && !props.isEmpty()) && (keySet!= null && keySet.length > 0)) {
			sql.append("update " +table+ " set ");
			keyList = updateSelective(props, sql);
			sql.append(whereId());
		}
		return keyList;
	}

	public static List<String> updateById(Map<String,Object> props, String table, Map<String, Object> tableMap, StringBuilder sql) {
	String[] keySet =string(tableMap, COLUMNS).split(",");
	List<String> keyList = null;
	if ((props != null && !props.isEmpty()) && (keySet != null && keySet.length > 0)) {
	    sql.append("update " + table + " set ");
	    keyList = updateSelective(props, sql);
	    sql.append(whereId());
	}
	return keyList;
    }
    
    public static List<String> updateBy(Map<String,Object> props, Map<String,Object> condition,String table, Map<String, Object> tableMap, StringBuilder sql) {
	String[] keySet =string(tableMap, COLUMNS).split(",");
	List<String> keyList = null;
	if ((props != null && !props.isEmpty()) && (keySet != null && keySet.length > 0)) {
	    sql.append("update " + table + " set ");
	    keyList = updateSelective(props, sql);
	    sql.append(where(condition));
	}
	return keyList;
    }

    public static List<String> insert(JSONObject props, String table, Map<String, Object> tableMap, StringBuilder sql) {
	String[] keySet =string(tableMap, COLUMNS).split(",");

		List<String> keyList=insert(props, table, sql, keySet);
		return keyList;
    }

	@NotNull
	private static List<String> insert(JSONObject props, String table, StringBuilder sql, String[] keySet){
		List<String> keyList = new ArrayList<>();
		if ((props!= null && !props.isEmpty()) && (keySet!= null && keySet.length > 0)) {
			sql.append("insert into  " +table);
			sql.append(" (");
			keyList = columnSelective(props, sql);
			sql.append(") values (" + valueSelective(props, keyList) + ")");
		}
		return keyList;
	}


	private static String valueSelective(Map<String, Object> props, List<String> keyList) {
	StringBuilder sb = new StringBuilder();
	for (String key : keyList) {
	    if (sb.length() > 1) {
		sb.append(" , ");
	    }
	    sb.append(" ? ");
	}
	return sb.toString();
    }

    private static List<String> columnSelective(Map<String, Object> props, StringBuilder sql) {
	List<String> keyList = new ArrayList<>();
	StringBuilder sb = new StringBuilder();
	for (Entry<String, Object> entryi : props.entrySet()) {
	    Object value = entryi.getValue();
	    if (value != null && Strings.isNotBlank(String.valueOf(value))) {
		if (sb.length() > 1) {
		    sb.append(" , ");
		}
		String key = entryi.getKey();
		keyList.add(key);
		sb.append(key);
	    }
	}
	sql.append(sb.toString());
	return keyList;
    }

    public static StringBuilder listOracle(JSONObject props, String label, String[] returnColumn, Set<String> likeKeys) {
	StringBuilder ret = listAll(label, returnColumn);
	where(props, ret, likeKeys);
	return ret;
    }
    public static StringBuilder listMysql(JSONObject props, String label, String[] returnColumn, Set<String> likeKeys) {
	StringBuilder ret = listAll(label, returnColumn);
	mysqlWhere(props, ret, likeKeys);
	return ret;
    }

    public static StringBuilder listAll(String label, String[] returnColumn) {
	StringBuilder ret = new StringBuilder();
	getSelect(returnColumn, ret,"t");
	ret.append(" from  " + label + " t ");
	return ret;
    }

    private static void where(JSONObject props, StringBuilder ret, Set<String> likeKeys) {
	if (!props.isEmpty()) {
	    StringBuilder sb = whereOracle(props, likeKeys);
	    if (sb.length() > 1) {
		ret.append(" where " + sb.toString() + " ");
	    }
	}
    }
    private static void mysqlWhere(JSONObject props, StringBuilder ret, Set<String> likeKeys) {
	if (!props.isEmpty()) {
	    StringBuilder sb = whereMysql(props, likeKeys);
	    if (sb.length() > 1) {
		ret.append(" where " + sb.toString() + " ");
	    }
	}
    }
    
    private static void where(Map<String,Object> props, StringBuilder ret, Set<String> likeKeys) {
 	if (!props.isEmpty()) {
 	    StringBuilder sb = whereOracle(props, likeKeys);
 	    if (sb.length() > 1) {
 		ret.append(" where " + sb.toString() + " ");
 	    }
 	}
     }
    
    private static void where(JSONObject props, StringBuilder ret) {
	if (!props.isEmpty()) {
	    StringBuilder sb = whereOracle(props, null);
	    if (sb.length() > 1) {
		ret.append(" where " + sb.toString() + " ");
	    }
	}
    }
    private static String where(Map<String, Object> props) {
	StringBuilder ret=new StringBuilder();
	if (!props.isEmpty()) {
	    StringBuilder sb = whereOracle(props, null);
	    if (sb.length() > 1) {
		ret.append(" where " + sb.toString() + " ");
	    }
	}
	return ret.toString();
    }

    private static String whereId() {
	    return " where id=? ";
    }

    private static void getSelect(String[] keySet, StringBuilder ret,String alais) {
	if (keySet != null && keySet.length > 1) {
	    ret.append(" select  " + selectLabelColumn(alais, keySet));
	} else {
	    ret.append(" select * ");
	}
    }

    public static StringBuilder getAllObject(JSONObject props, String label, String[] keySet, Set<String> likeKeys) {
	StringBuilder ret = new StringBuilder();
	String alais = "t"+label.length();
	getSelect(keySet, ret,alais);
	ret.append(" from  " + label + " "+alais+" ");
	where(props, ret, likeKeys);
	return ret;
    }
    
    public static StringBuilder getAllObject(Map<String,Object> props, String label, String[] keySet, Set<String> likeKeys) {
	StringBuilder ret = new StringBuilder();
	String alais = "t"+label.length();
	getSelect(keySet, ret,alais);
	ret.append(" from  " + label + " "+alais+" ");
	where(props, ret, likeKeys);
	return ret;
    }

    public static String getOraclePageSql(String sql, int pageNum, int pageSize) {
	StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
	sqlBuilder.append("select * from ( select tmp_page.*, rownum row_id from ( ");
	sqlBuilder.append(sql);
	int max = pageNum * pageSize;
	int min = (pageNum - 1) * pageSize;
	sqlBuilder.append(" ) tmp_page where rownum <= '" + max + "' ) where row_id > '" + min + "'");
	return sqlBuilder.toString();
    }
    
    public static String getMysqlPageSql(String sql, int pageNum, int pageSize) {
	StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
	sqlBuilder.append("select p.* from (");
	sqlBuilder.append(sql);
	int min = (pageNum - 1) * pageSize;
	sqlBuilder.append(" ) p limit " + min + "," + pageSize + "");
	return sqlBuilder.toString();
    }

    private static String selectLabelColumn(String label, String[] keySet) {
	StringBuilder sbRet = new StringBuilder();
	for (String key : keySet) {
	    if (sbRet.length() > 1) {
		sbRet.append(",");
	    }
	    sbRet.append(label + "." + key + " ");
	}
	return sbRet.toString();
    }

    private static List<String> updateSelective(Map<String, Object> props, StringBuilder sql) {
	StringBuilder sb = new StringBuilder();
	List<String> keyList = new ArrayList<>();
	String idString="";
	    
	for (Entry<String, Object> entryi : props.entrySet()) {
	    Object value = entryi.getValue();
	    if (value != null && Strings.isNotBlank(String.valueOf(value))) {
		String key = entryi.getKey();
		if(ID.equals(key.toLowerCase())) {
		    idString=key;
		    continue;
		}
		
		if (sb.length() > 1) {
		    sb.append(" , ");
		}
		
		keyList.add(key);
		
		sb.append(key + "= ? ");
	    }
	}
	sql.append(sb.toString());
	keyList.add(idString);
	return keyList;
    }
    
    private static StringBuilder whereMysql(Map<String, Object> props, Set<String> liekSet) {
	StringBuilder sb = new StringBuilder();
	for (Entry<String, Object> entryi : props.entrySet()) {
	    Object value = entryi.getValue();
	    if (value != null && Strings.isNotBlank(String.valueOf(value))) {
		if (sb.length() > 1) {
		    sb.append(" and ");
		}
		String key = entryi.getKey();
		if (like(key, liekSet)||key.toLowerCase().contains("name")
			||key.toLowerCase().contains("content")||key.toLowerCase().contains("title")) {
		    sb.append(key + " like '%" + String.valueOf(value) + "%'");
		} else {
		    sb.append(key + "='" + String.valueOf(value) + "'");
		}
	    }
	}

	return sb;
    }

    private static StringBuilder whereOracle(Map<String, Object> props, Set<String> liekSet) {
	StringBuilder sb = new StringBuilder();
	for (Entry<String, Object> entryi : props.entrySet()) {
	    Object value = entryi.getValue();
	    if (value != null && Strings.isNotBlank(String.valueOf(value))) {
		if (sb.length() > 1) {
		    sb.append(" and ");
		}
		String key = entryi.getKey();
		if (like(key, liekSet)) {
		    sb.append(key + " like '%'||" + String.valueOf(value) + "||'%'");
		} else {
		    sb.append(key + "='" + String.valueOf(value) + "'");
		}
	    }
	}

	return sb;
    }

    /**
     * 精确查询
     * 
     * @param key
     * @return
     */
    private static boolean like(String key, Set<String> likeColumn) {
	return likeColumn != null && !likeColumn.isEmpty() && likeColumn.contains(key);
    }

}

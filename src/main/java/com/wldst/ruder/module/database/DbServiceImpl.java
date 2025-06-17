package com.wldst.ruder.module.database;

import static com.wldst.ruder.module.database.util.DbTransUtil.initAttrcap;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.MapTool;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.module.database.constant.Ds;
import com.wldst.ruder.module.database.util.DbTransUtil;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.TextUtil;

/**
 * POJO Product
 * 
 * @author qliu 日期：2016-10-10
 */
@Service
public class DbServiceImpl extends DataBaseDomain implements DbInfoService {
    final static Logger logger = LoggerFactory.getLogger(DbServiceImpl.class);

    private String[] colnames; // 列名数组
    private String[] colTypes; // 列名类型数组
    private String[] colHeads; // 列名类型数组
    private String[] colNullAbles; // 列名类型数组
    private String[] colSizes; // 列名大小数组
    private String primaryKey;
    protected String dbType;

    private CrudNeo4jService neo4jService;
    private static Long dsId;
    protected static Map<Long, Map<String, Object>> dsMap = new HashMap<>();
     // 连接
    protected static Map<Long, BasicDataSource> poolMap = new HashMap<>();
    /**
     * ThreadLocal存放Connection
     */
    private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<Connection>();

    public static Connection getConnection() {

	Connection connection = connectionHolder.get();
	BasicDataSource basicDataSource = poolMap.get(dsId);
	boolean connectionLoss;
	try {
	    connectionLoss = connection == null || connection.isClosed();
	    if (connectionLoss&& dsId!=null&&basicDataSource!=null) {
		    // 1.连接池可以理解是一个java类,必须实现接口DateSource
		    try {
			connection= basicDataSource.getConnection();
			connectionHolder.set(connection);
		    } catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
	   }
	} catch (SQLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}
	

	return connection;
    }

    /**
     * 从连接池获取数据
     * 
     * @param dataSourceMap
     * @throws Exception
     */
    public BasicDataSource getDbcp(Map<String, Object> dataSourceMap) throws Exception {
	if(dataSourceMap==null||dataSourceMap.isEmpty()||string(dataSourceMap, Ds.URL)==null) {
	    return null;
	}
	setDbType(string(dataSourceMap, DATABASE_TYPE));
	Properties p = new Properties();

	p.setProperty(Ds.DRIVER_CLASS_NAME, string(dataSourceMap, Ds.DRIVER_CLASS_NAME));
	p.setProperty(Ds.URL, string(dataSourceMap, Ds.URL));
	p.setProperty(Ds.USER_NAME, string(dataSourceMap, Ds.USER_NAME));
	p.setProperty(Ds.PASSWORD, string(dataSourceMap, Ds.PASSWORD));

	p.setProperty("maxActive", "50");// 设置最大并发数
	p.setProperty("initialSize", "20");// 数据库初始化时，创建的连接个数
	p.setProperty("minIdle", "10");// 最小空闲连接数
	p.setProperty("maxIdle", "10");// 数据库最大连接数
	p.setProperty("maxWait", "1000");// 超时等待时间(毫秒）
	p.setProperty("removeAbandoned", "false");// 是否自动回收超时连接
	p.setProperty("removeAbandonedTimeout", "120");// 超时时间(秒)
	p.setProperty("testOnBorrow", "true");// 取得连接时进行有效性验证
	p.setProperty("logAbandoned", "true");// 是否在自动回收超时连接的时候打印连接的超时错误
	p.setProperty("validationQuery", "SELECT COUNT(*) FROM DUAL");
	return BasicDataSourceFactory.createDataSource(p);
    }

    /*
     * 构造函数
     */
	@Autowired
    public DbServiceImpl(@Lazy  CrudNeo4jService neo4jService) {
        this.neo4jService=neo4jService;
    }
    public DbServiceImpl(Map<String, Object> ds) {
        initConnect(ds);
    }

    @Override
	public void initConnect(Map<String, Object> ds) {
		Object object = ds.get(ID);
		Long idLong = null;
		if (object == null) {
			return;
		}
		idLong = (Long) object;
		dsId = idLong;
		setDbType(string(ds, DATABASE_TYPE));
		if (!dsMap.containsKey(idLong)) {
			dsMap.put(idLong, ds);
		}

		BasicDataSource basicDataSource = poolMap.get(idLong);
		if (basicDataSource != null && connectionHolder.get() != null) {
			return;
		}
		if (basicDataSource != null && connectionHolder.get() == null) {
			try {
				Connection connection = basicDataSource.getConnection();
				if (connection != null && !connection.isClosed()) {
					connectionHolder.set(connection);
					return;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


		BasicDataSource dbcp;
		try {
			dbcp = getDbcp(ds);
			poolMap.put(idLong, dbcp);
			connectionHolder.set(dbcp.getConnection());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    private Connection createConnection(Map<String, Object> ds) {
	Connection con = null;
	try {
	    try {
		Class.forName(string(ds, DRIVER_CLASS_NAME));
	    } catch (ClassNotFoundException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	    con = DriverManager.getConnection(string(ds, URL), string(ds, USER_NAME),
		    string(ds, PASSWORD));
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return con;
    }

    @Override
    public void copyTableInfo(String tablei) {
	try {
	    neo4jService.saveByBody(tableMetaInfo(tablei), LABLE_TABLE);
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    protected Map<String, Object> oracleTableMetaInfo(String tableName) {
	Map<String, Object> dbTableInfoMap = new HashMap<>();

	// 查要生成实体类的表
	StringBuilder sql = new StringBuilder();

	sql.append("""
		select
		    ut.COLUMN_NAME,--字段名称
		    uc.comments,--字段注释
		    ut.DATA_TYPE,--字典类型
		    ut.DATA_LENGTH,--字典长度
		    ut.NULLABLE,--是否为空
		    utc.comments
		from user_tab_columns  ut
		inner JOIN  user_tab_comments utc  on ut.TABLE_NAME  = utc.table_name
		inner JOIN user_col_comments uc
		 on ut.TABLE_NAME  = uc.table_name and ut.COLUMN_NAME = uc.column_name
		where  """);
	sql.append(" ut.Table_Name='" + tableName + "'");
	// order by ut.column_name
	StringBuilder sqlx = new StringBuilder();
	sqlx.append("""
		select
		    count(*)
		from user_tab_columns  ut
		inner JOIN user_col_comments uc
		 on ut.TABLE_NAME  = uc.table_name and ut.COLUMN_NAME = uc.column_name
		where  """);
	sqlx.append(" ut.Table_Name='" + tableName + "'");
	int size = 20;
	try (Statement createStatementx = connectionHolder.get().createStatement();
		ResultSet tableRsx = createStatementx.executeQuery(sqlx.toString());) {
	    tableRsx.next();
	    size = tableRsx.getInt(1);
	} catch (SQLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	try (Statement createStatement = getConnection().createStatement();
		ResultSet tableRs = createStatement.executeQuery(sql.toString());) {

	    colnames = new String[size];
	    colTypes = new String[size];
	    colHeads = new String[size];
	    colNullAbles = new String[size];
	    colSizes = new String[size];
	    int i = 0;
	    String tableNameComment=null;
	    while (tableRs.next()) {
		colnames[i] = tableRs.getString(1);
		String commString = tableRs.getString(2);
		if (commString == null || commString.indexOf("?") >= 0 || commString.indexOf("？") > 0) {
		    colHeads[i] = DbTransUtil.initAttrcap(tableRs.getString(1));
		} else {
		    commString=commString.replaceAll(",", "_");
		    colHeads[i] = commString;
		}
		colTypes[i] = tableRs.getString(3);
		colSizes[i] = tableRs.getString(4);
		colNullAbles[i] = tableRs.getString(5);
		if(tableNameComment==null&&tableRs.getString(6)!=null) {
		    tableNameComment= tableRs.getString(6);
		}
		i++;
	    }
	    dbTableInfoMap.put("dsId", dsId);
	    dbTableInfoMap.put("tableName", tableName);
	    dbTableInfoMap.put(NAME, tableNameComment);
	    dbTableInfoMap.put("columns", String.join(",", colnames));
	    dbTableInfoMap.put("colTypes", String.join("=t=", colTypes));
	    dbTableInfoMap.put("header", String.join(",", colHeads));
	    dbTableInfoMap.put("nullAble", String.join(",", colNullAbles));
	    dbTableInfoMap.put("colSize", String.join(",", colSizes));
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return dbTableInfoMap;
    }

    protected Map<String, Object> mysqlTableMetaInfo(String tableName) {
	Map<String, Object> dbTableInfoMap = new HashMap<>();
	// 查要生成实体类的表
	StringBuilder sql = new StringBuilder();

	sql.append(" show full columns from " + tableName);
	StringBuilder sqlx = new StringBuilder();
	sqlx.append("""
		SELECT
			count(*)
		FROM
			information_schema.`COLUMNS`
		WHERE
					""");
	sqlx.append("TABLE_NAME = '" + tableName + "'");
	int size = 20;
	try (Statement createStatementx = getConnection().createStatement();
		ResultSet tableRsx = createStatementx.executeQuery(sqlx.toString());) {
	    tableRsx.next();
	    size = tableRsx.getInt(1);
	} catch (SQLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	}

	try (Statement createStatement = getConnection().createStatement();
		ResultSet tableRs = createStatement.executeQuery(sql.toString());) {

	    colnames = new String[size];
	    colTypes = new String[size];
	    colHeads = new String[size];
	    colNullAbles = new String[size];
	    colSizes = new String[size];
	    int i = 0;
	    while (tableRs.next()) {
		colnames[i] = tableRs.getString(1);
		String commString = tableRs.getString(9);
		if (commString == null || commString.indexOf("?") >= 0 || commString.indexOf("？") > 0) {
		    colHeads[i] = DbTransUtil.initAttrcap(tableRs.getString(1));
		}else if("".equals(commString.trim())){
		    colHeads[i] = colnames[i];
		} else {
		    colHeads[i] = commString;
		}
		colTypes[i] = tableRs.getString(2);
		colNullAbles[i] = tableRs.getString(4);
		i++;
	    }
	    dbTableInfoMap.put("dsId", dsId);
	    dbTableInfoMap.put("tableName", tableName);
	    dbTableInfoMap.put("columns", String.join(",", colnames));
	    dbTableInfoMap.put("colTypes", String.join(",", colTypes));
	    dbTableInfoMap.put("header", String.join(",", colHeads));
	    dbTableInfoMap.put("nullAble", String.join(",", colNullAbles));
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return dbTableInfoMap;
    }

    public String getPrimaryKey() {
	return primaryKey;
    }

    public void setPrimaryKey(String primaryKeys) {
	this.primaryKey = primaryKeys;
    }

    public static List<Map<String, Object>> convertList(ResultSet rs) {
	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
	try {
	    ResultSetMetaData md = rs.getMetaData();
	    int columnCount = md.getColumnCount();
	    while (rs.next()) {
		Map<String, Object> rowData = new HashMap<String, Object>();
		for (int i = 1; i <= columnCount; i++) {
		    String columnTypeName = md.getColumnTypeName(i);
		    String columnName = md.getColumnName(i);
		    if(columnTypeName.equals("DATETIME")) {
			String valueOf = String.valueOf(rs.getObject(i));
//			Long long1 = Long.valueOf(valueOf);
//			String dateTimeString = DateUtil.dateTimeString(long1);
			rowData.put(columnName, valueOf);
		    }else {
			rowData.put(columnName, rs.getObject(i));
			    
		    }
		}
		list.add(rowData);
	    }
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} finally {
	    try {
		if (rs != null)
		    rs.close();
		rs = null;
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
	return list;
    }

    public static Map<String, Object> convertMap(ResultSet rs) {
	Map<String, Object> map = new java.util.TreeMap<String, Object>();
	try {
	    ResultSetMetaData md = rs.getMetaData();
	    int columnCount = md.getColumnCount();
	    while (rs.next()) {
		for (int i = 1; i <= columnCount; i++) {
		    map.put(md.getColumnName(i), rs.getObject(i));
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (rs != null)
		    rs.close();
		rs = null;
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	}
	return map;
    }

    @Override
    public Long getDsId() {
	return dsId;
    }

    @Override
    public void setDsId(Long dsId) {
	this.dsId = dsId;
    }

    @Override
    public Connection getCon() {
	return getConnection();
    }

    @Override
    public String getDbType() {
	return dbType;
    }

    @Override
    public void setDbType(String dbType) {
	this.dbType = dbType;
    }

    @Override
    public List<Map<String, Object>> query(String query) throws Exception {
	if (poolMap.isEmpty()|| dsId == null||getConnection() == null) {
	    throw new Exception("DataSource is not connected， 数据库链接异常！");
	}
	LoggerTool.info(logger,query);
	List<Map<String, Object>> convertList = null;
	try (Statement createStatement = getConnection().createStatement(); ResultSet rSet = createStatement.executeQuery(query);) {
	    convertList = convertList(rSet);

	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return convertList;
    }
    
    @Override
    public String tableSql(String tableName) throws Exception {
	if (poolMap.isEmpty() || dsId == null || getConnection() == null) {
	    throw new Exception("DataSource is not connected， 数据库链接异常！");
	}
	StringBuilder tableSql = new StringBuilder();

	try (Connection conn = getConnection();) {
	    DatabaseMetaData metaData = conn.getMetaData();
	    ResultSet rs = metaData.getTables(null, null, tableName, null);
	    if (rs.next()) {
		tableSql.append("CREATE TABLE  " + rs.getString("TABLE_NAME") + " (");
		rs.next();
		while (rs.next()) {
		    tableSql.append(rs.getString("COLUMN_NAME") + " " + rs.getString("DATA_TYPE") + ", ");
		}
		tableSql.append(rs.getString("PRIMARY_KEY") + " " + rs.getString("COLUMN_NAME") + ", ");
		tableSql.append(
			rs.getString("KEY_SEQ") + " int)ENGINE=InnoDB AUTO_INCREMENT=" + rs.getInt("TABLE_ROWS"));
		System.out.println(tableSql.toString());
	    } else {
		System.out.println("No such table exists: " + tableName);
	    }
	    rs.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return tableSql.toString();
    }
    
    

    @Override
    public int count(String query) throws Exception {
	if (poolMap.isEmpty()|| dsId == null||getConnection() == null) {
	    throw new Exception("数据库链接异常！");
	}
	try (Statement createStatement = getConnection().createStatement(); ResultSet rSet = createStatement.executeQuery(query);) {
	    rSet.next();
	    return rSet.getInt(1);

	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return 0;
    }

    @Override
    public List<Map<String, Object>> prepareQuery(String query, JSONObject json, List<String> keys,
	    Map<String, String> typeMap) {
	List<Map<String, Object>> convertList = null;

	try (PreparedStatement preparedStatement = getConnection().prepareStatement(query);) {
	    int i = 0;
	    for (String key : keys) {
		i++;
		String value = typeMap.get(key);
		if (value != null && typeMap.containsKey(key)
			&& (value.startsWith("TIMESTAMP") || value.toUpperCase().endsWith("TIME"))) {
		    java.sql.Date dateTime = new java.sql.Date(DateTool.dateStrToLong(String.valueOf(json.get(key))));
		    preparedStatement.setDate(i, dateTime);
		    continue;
		}
		preparedStatement.setObject(i, json.get(key));

	    }
	    ResultSet rSet = preparedStatement.executeQuery();
	     convertList = convertList(rSet);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return convertList;
    }

    @Override
    public void prepareExcuteById(String query, String id) {

	try (PreparedStatement preparedStatement = getConnection().prepareStatement(query);) {
	    int parameterCount = preparedStatement.getParameterMetaData().getParameterCount();
	    if (parameterCount > 1) {
		String[] split = id.split(",");
		for (int i = 1; i <= parameterCount; i++) {
		    preparedStatement.setObject(i, split[i - 1]);
		}
	    } else {
		preparedStatement.setString(1, id);
	    }

	    preparedStatement.executeQuery();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public Boolean excute(String query) throws Exception {
	if (poolMap.isEmpty()|| dsId == null||getConnection() == null) {
	    throw new Exception("数据库链接异常！");
	}
	List<Map<String, Object>> convertList = null;
	boolean rSet = false;
	try (Statement createStatement = getConnection().createStatement();) {
	    rSet = createStatement.execute(query);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return rSet;
    }

    public Map<String, Object> tableMetaInfo(String tableName) throws Exception {
	if (poolMap.isEmpty()|| dsId == null||getConnection() == null ) {
	    throw new Exception("数据库链接异常！");
	}
	
	if (getDbType().toLowerCase().equals("oracle")) {
	    return oracleTableMetaInfo(tableName);
	}
	if (getDbType().toLowerCase().equals("mysql")) {
	    return mysqlTableMetaInfo(tableName);
	}
	return null;
    }

    @Override
    public List<Map<String, Object>> work() throws Exception {
	if (poolMap.isEmpty()|| dsId == null||getConnection() == null) {
	    throw new Exception("数据库链接异常！");
	}
	List<Map<String, Object>> tableMaps = new ArrayList<>();
	List<String> tables = getTable();
	// 查询表的注释
	Map<String, String> tableCommentsMap = getTableCommentMap();

	for (String tablei : tables) {
	    Map<String, Object> init = tableMetaInfo(tablei);
	    String tName = tableCommentsMap.get(tablei);
	    if (tName != null && !"".equals(tName)) {
		init.put(NAME, tName);
	    }
	    tableMaps.add(init);
	}
	return tableMaps;
    }

    private Map<String, String> getTableCommentMap() {
	Map<String, String> tableCommentsMap = new HashMap<>();
	StringBuilder sqlTableName = new StringBuilder();
	if (getDbType().toLowerCase().equals("oracle")) {
	    sqlTableName.append("select TABLE_NAME,COMMENTS from user_tab_comments where comments is not null");
	}
	if (getDbType().toLowerCase().equals("mysql")) {
	    sqlTableName.append(
	    "SELECT TABLE_NAME,TABLE_COMMENT FROM information_schema.TABLES where TABLE_COMMENT is not null");
	}
	try (Statement createStatement = getConnection().createStatement();
		ResultSet tableRs = createStatement.executeQuery(sqlTableName.toString());) {
	    int i = 0;
	    while (tableRs.next()) {
		tableCommentsMap.put(tableRs.getString(1), tableRs.getString(2));
		i++;
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return tableCommentsMap;
    }

    @Override
    public List<String> getTable() throws Exception {
	if (poolMap.isEmpty()|| dsId == null||getConnection() == null ) {
	    throw new Exception("数据库链接异常！");
	}
	List<String> tableList = new ArrayList<>();

	String sql2 = "select TABLE_NAME from user_tables";
	if (getDbType().toLowerCase().equals("oracle")) {
	    sql2 = "select TABLE_NAME from user_tables";
	}
	if (getDbType().toLowerCase().equals("mysql")) {
	    sql2 = "show tables";
	}

	try (Statement createStatement = getConnection().createStatement(); ResultSet rSet = createStatement.executeQuery(sql2);) {
	    while (rSet.next()) {
		tableList.add(rSet.getString(1));
	    }

	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return tableList;
    }

    @Override
    public Boolean prepareExcute(String query, JSONObject json, List<String> keys, Map<String, String> typeMap) {
	return prepareExcute(query,(Map<String,Object>)json,keys,typeMap);
    }
    @Override
    public Boolean prepareExcute(String query, Map<String,Object> json, List<String> keys, Map<String, String> typeMap) {
	Boolean convertList = false;
	LoggerTool.info(logger,query);
	Connection connection = getConnection();
	try (PreparedStatement preparedStatement = connection.prepareStatement(query);) {
	    int i = 0;
	    for (String key : keys) {
		i++;
		String value = typeMap.get(key);
		if (value != null && typeMap.containsKey(key)
			&& (value.startsWith("TIMESTAMP") || value.toUpperCase().endsWith("TIME"))) {
		    java.sql.Date dateTime = new java.sql.Date(DateTool.dateStrToLong(String.valueOf(json.get(key))));
		    preparedStatement.setDate(i, dateTime);
		    continue;
		}
		preparedStatement.setObject(i, json.get(key));
		LoggerTool.info(logger,"\n======"+i+"="+json.get(key));
	    }
	    
	    convertList = preparedStatement.execute();
	    if(!connection.getAutoCommit()) {
		LoggerTool.info(logger,"\n======commit sql ");
		connection.commit();
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	    LoggerTool.info(logger,"\n======commit sql ");
	}
	return convertList;
    }
    
    @Override
    public Boolean prepareExcute(String query, Object[] args) {
	Boolean convertList = false;

	try (PreparedStatement preparedStatement = getConnection().prepareStatement(query);) {
	    
	    for (int i = 0; i<args.length;i++) {
		preparedStatement.setObject(i, args[i]);

	    }
	    convertList = preparedStatement.execute();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return convertList;
    }

    @Override
    public Map<String, Object> sqlMetaInfo(String sql) {
	LoggerTool.error(logger,"sql:\n"+sql);
	try (Statement createStatementx = getConnection().createStatement();
		ResultSet tableRsx = createStatementx.executeQuery(sql);) {
	    ResultSetMetaData metaData = tableRsx.getMetaData();
	    int columnCount = metaData.getColumnCount();
	    Map<String, Object> colMap = new HashMap<>();
	    for (int i = 1; i <= columnCount; i++) {
		String columnName = metaData.getColumnName(i);
		String columnType = metaData.getColumnTypeName(i);
		colMap.put(columnName, columnType);
	    }
	    if(!colMap.isEmpty()) {
		return colMap;
	    }

	    return (Map<String, Object>) JSON.toJSON(metaData);
	} catch (SQLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	    LoggerTool.error(logger,"sql:"+sql+e1.getMessage(),e1);
	    
	}
	return null;
    }

    @Override
    public Map<String, Object> sqlMetaInfo(String sql, Object[] args) {
	LoggerTool.error(logger,"sql:\n"+sql);
	try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql);
		) {
	    
	    for(int i=0;i<args.length;i++) {
		Object obi=args[i];
		preparedStatement.setObject(i+1, obi);
	     }
	    ResultSet tableRsx = preparedStatement.executeQuery();
	    ResultSetMetaData metaData = tableRsx.getMetaData();
	    int columnCount = metaData.getColumnCount();
	    Map<String, Object> colMap = new HashMap<>();
	    for (int i = 1; i <= columnCount; i++) {
		String columnName = metaData.getColumnName(i);
		String columnType = metaData.getColumnTypeName(i);
		colMap.put(columnName, columnType);
	    }
	    if(!colMap.isEmpty()) {
		return colMap;
	    }

	    return (Map<String, Object>) JSON.toJSON(metaData);
	} catch (SQLException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	    LoggerTool.error(logger,"sql:"+sql+e1.getMessage(),e1);
	    
	}
	return null;
    }

    @Override
    public List<Map<String, Object>> prepareQuery(String query, String json) {
	List<Map<String, Object>> convertList = null;

	try (PreparedStatement preparedStatement = getConnection().prepareStatement(query);) {
	    int i = 0;
	    for (String pi : json.split(",")) {		
		preparedStatement.setObject(i+1, pi);
		i++;
	    }
	    ResultSet rSet = preparedStatement.executeQuery();
	    convertList = convertList(rSet);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return convertList;
    }

    @Override
    public Boolean prepareExcute(String query, String params, List<String> keys, Map<String, String> typeMap) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Boolean prepareExcuteClob(String query, String data) {
	Connection connection = getConnection();
	try (PreparedStatement preparedStatement = connection.prepareStatement(query);){
	    ResultSet rs = preparedStatement.executeQuery();
	    if (rs.next()) {
		// oracle.sql.BLOB blob = (oracle.sql.BLOB) rs.getBlob("content");
		OutputStream out = rs.getBlob(1).setBinaryStream(1L);
//		OutputStream out = rs.getClob(1).set;
		// OutputStream out = blob.getBinaryOutputStream();
		out.write(data.getBytes());
		out.flush();
		out.close();
	    }
	    boolean autoCommit = connection.getAutoCommit();
	    if(!autoCommit) {
		connection.commit();
	    }
	    
	} catch (SQLException|IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	 
	    
	    return true;
    }
    
    @Override
    public Boolean prepareGetClobLength(String query,String contentCols) {
	try (PreparedStatement preparedStatement = getConnection().prepareStatement(query);){
	    ResultSet rs = preparedStatement.executeQuery();
	    if (rs.next()) {
		  String xx = rs.getString(contentCols);
		if(xx.length()<10) {
		    return true;
		};
	    }
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return false;
    }
    
    @Override
    public void tableMappingMeta(String table) {
	
	Map<String, Object> tableMap = neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
	String[] columns = columns(tableMap);
	String colAtts = tuofengName(columns);

	// 新增一个元数据，新增列映射表。 //tableName,columns,header
	Map<String, Object> data = newMap();
	String nameTable = name(tableMap);
	if(nameTable!=null) {
	    data.put(NAME, nameTable);
	}
	data.put(LABEL, DbTransUtil.initcap(table.toLowerCase()));
	
	
	String header = header(tableMap);
	if(header.contains("�")||header.contains("?")||header.contains("�")||nameTable!=null&&nameTable.contains("�")) {
	    Map<String, Object> tableMetaInfo;
	    try {
		tableMetaInfo = tableMetaInfo(table);
		 header=header(tableMetaInfo);
		 data.put(LABEL,DbTransUtil.initcap(table.toLowerCase()));
		 String name2 = name(tableMetaInfo);
		 if(name2!=null) {
		     data.put(NAME, name2);
			 tableMap.put(NAME, name2);
		 }
	    } catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	   
	}
//	if(!TextUtil.isChinese(header)) {
//	    
//	}
	data.put(HEADER, header);
	
	
	data.put(COLUMNS, colAtts);
	neo4jService.saveByBody(data, META_DATA);
	
	//新增字段映射？DbColumnMap:name,code,columns,header,dbColumn
	
	Map<String, Object> dataCol = newMap();
	dataCol.put(NAME, nameTable);
	dataCol.put(CODE, label(data));
	dataCol.put(HEADER, header);
	dataCol.put(COLUMNS, colAtts);
	dataCol.put("dbColumn", String.join(",",columns));
	neo4jService.saveByBody(dataCol, "DbColumnMap");
    }

    public static String tuofengName(String[] columns) {
	StringBuilder sb = new StringBuilder();
	for (int i = 0; i < columns.length; i++) {
	    if (!columns[i].equals(ID)) {
		String attri = initAttrcap(columns[i].toLowerCase());
		sb.append("," + attri);
	    }
	}
	String colAtts = sb.toString();
	colAtts = colAtts.substring(1, colAtts.length());
	return colAtts;
    }
    
    public static void main(String[] args) {
	String column = "ASCRIPTION_DEPT_NAME,BIZTABLENAME,TEMPLATEMARK,WorkflowName,WfInstanceID,WfStatus,TaskExecutorID,NowTaskIDs,NowInnerTaskID,NowTaskName,NowExecutorStatus";
	System.out.println(tuofengName(column.split(",")));
    }
	@Override
	public Map<String, Object> toEntity(Map<String, Object> employeeDoc, String empDocColMap) {
		Map<String, Object> retEmployeeDoc = MapTool.newMap();
		Map<String, Object> attMapBy = neo4jService.getAttMapBy("code", empDocColMap, "DbColumnMap");
		Map<String, String> colsMap = MapTool.colMap(attMapBy);
		for (Map.Entry<String, String> ei : colsMap.entrySet()) {
			String ki = ei.getKey();
			String ki2 = ei.getValue();
			retEmployeeDoc.put(ki2, employeeDoc.get(ki));
		}
		return retEmployeeDoc;
	}
}

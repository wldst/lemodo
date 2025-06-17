package com.wldst.ruder.module.database.service;

import java.sql.Connection;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.crud.service.CrudNeo4jDriver;
import com.wldst.ruder.module.database.constant.Ds;
import com.wldst.ruder.module.database.jdbc.Column;
import com.wldst.ruder.module.database.util.DbTransUtil;
import com.wldst.ruder.util.MapTool;

@Component
public class DBCPUtil extends MapTool implements IDataBaseCode {
    @Autowired
    private CrudNeo4jDriver driver;

    private Connection con = null;
    private boolean util = true;
    private boolean sql = false;
    private  String dbName;
    private String[] colnames;
    private String[] colTypes;
    private String[] colMarks;
    private Boolean[] nullables;
    private String[] lengths;
    private Map<String, String> constrants = new HashMap<>();

    private String fkSql = """
    	SELECT C.TABLE_SCHEMA  schemaName, C.REFERENCED_TABLE_NAME   ptable,
	     C.REFERENCED_COLUMN_NAME  pcolumn , C.TABLE_NAME ctable,C.COLUMN_NAME  ccolumn 
	FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE C
	WHERE C.REFERENCED_TABLE_NAME IS NOT NULL and TABLE_SCHEMA =?
		      """;

    /**
     * 从连接池获取数据
     * 
     * @param dataSourceMap
     * @throws Exception
     */
    public Connection getConnectionBydbcp(Map<String, Object> dataSourceMap) throws Exception {

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
	BasicDataSource dataSource = BasicDataSourceFactory.createDataSource(p);
	return dataSource.getConnection();
    }

    public Connection getConnectionByDs(Map<String, Object> dataSourceMap) throws SQLException {

	BasicDataSource dataSource = new BasicDataSource();
	dataSource.setDriverClassName(string(dataSourceMap, Ds.DRIVER_CLASS_NAME));
	dataSource.setUrl(string(dataSourceMap, Ds.URL));
	dataSource.setUsername(string(dataSourceMap, Ds.USER_NAME));
	dataSource.setPassword(string(dataSourceMap, Ds.PASSWORD));
	return dataSource.getConnection();
    }

    private void initConnection(Map<String, Object> dataSourceMap) {
	try {
	    Class.forName(string(dataSourceMap, Ds.DRIVER_CLASS_NAME));
	    con = DriverManager.getConnection(string(dataSourceMap, Ds.URL),
		    string(dataSourceMap, Ds.USER_NAME), string(dataSourceMap, Ds.PASSWORD));
	} catch (ClassNotFoundException | SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private void dowork(String tablename, String commentTable, String primaryKey, Long dsId) {
	Map<String, Object> dataSourceMap = driver.getNodePropertiesById(dsId);
	PreparedStatement pStemt = null;
	try {
	    pStemt = getConnectionByDs(dataSourceMap).prepareStatement("select * from " + tablename);
	    ResultSetMetaData rsmd = pStemt.getMetaData();
	    int size = rsmd.getColumnCount();
	    colnames = new String[size];
	    colTypes = new String[size];
	    colMarks = new String[size];
	    lengths = new String[size];
	    nullables = new Boolean[size];
	    for (int i = 0; i < size; i++) {
		colnames[i] = rsmd.getColumnName(i + 1);
		colTypes[i] = rsmd.getColumnTypeName(i + 1);
		colMarks[i] = rsmd.getColumnLabel(i + 1);
		if (colTypes[i].equalsIgnoreCase("datetime")) {
		    util = true;
		}
		if (colTypes[i].equalsIgnoreCase("image") || colTypes[i].equalsIgnoreCase("text")) {
		    sql = true;
		}
	    }
	    ResultSet rs = pStemt.executeQuery("show full columns from " + tablename);
	    int marki = 0;
	    while (rs.next()) {
		String string = rs.getString("Comment");
		if (string != null) {
		    colMarks[marki] = string;
		}
		lengths[marki] = rs.getString("Type");
		nullables[marki] = rs.getString("Null").equalsIgnoreCase("YES");
		marki++;
	    }
	    pStemt.close();
	    pStemt = null;
	    // if (primaryKey != null) {
	    // buildMapperEntity(tablename, commentTable, primaryKey.toLowerCase());
	    // } else {
	    // buildMapperEntity(tablename, commentTable, null);
	    // }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * 获得某表的建表语句
     * 
     * @param tableName
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> getCommentByTableName(List<String> tableName) throws Exception {
	Map<String, String> map = new HashMap<>();
	Statement stmt = con.createStatement();
	for (int i = 0; i < tableName.size(); i++) {
	    String table = tableName.get(i);
	    ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + table);

	    if (rs != null && rs.next()) {
		String createDDL = rs.getString(2);
		String comment = parse(createDDL);
		map.put(table, comment);
		// 正则匹配数据
		Pattern pattern = Pattern.compile("PRIMARY KEY \\(\\`(.*)\\`\\)");
		Matcher matcher = pattern.matcher(rs.getString(2));
		matcher.find();
		String data = "";
		try {
		    data = matcher.group();
		    // 过滤对于字符
		    data = data.replaceAll("\\`|PRIMARY KEY \\(|\\)", "");
		    // 拆分字符
		    map.put(table + "pk", data);
		} catch (Exception e) {
		}

	    }
	    rs.close();
	}
	stmt.close();
	return map;
    }

    /**
     * 返回注释信息
     * 
     * @param all
     * @return
     */
    @Override
    public String parse(String all) {
	String comment = null;
	int index = all.indexOf("COMMENT='");
	if (index < 0) {
	    return "";
	}
	comment = all.substring(index + 9);
	comment = comment.substring(0, comment.length() - 1);
	return comment;
    }

    @Override
    public List<String> showTables() {
	List<String> readMySqlTable = DbTransUtil.readMySqlTable(con, dbName, "");
	workDone();
	return readMySqlTable;
    }

    @Override
    public List<Column> tabeInfo(String tableName) {
	List<Column> column;
	column = getTableInfo(tableName);
	workDone();
	return column;
    }

    /**
     * 获得某表的建表语句
     * 
     * @param tableName
     * @return
     * @throws Exception
     */
    @Override
    public List<Column> getTableInfo(String table) {
	List<Column> columns = new ArrayList<>();
	PreparedStatement pStemt = null;
	try {
	    pStemt = con.prepareStatement("select * from " + table);
	    ResultSetMetaData rsmd = pStemt.getMetaData();
	    int size = rsmd.getColumnCount();

	    for (int i = 0; i < size; i++) {
		Column ci = new Column();
		ci.setName(rsmd.getColumnName(i + 1));
		ci.setType(rsmd.getColumnTypeName(i + 1));
		ci.setMark(rsmd.getColumnLabel(i + 1));
		columns.add(ci);
	    }
	    ResultSet rs = pStemt.executeQuery("show full columns from " + table);
	    int marki = 0;
	    while (rs.next()) {
		Column column = columns.get(marki);
		String string = rs.getString("Comment");
		String extra = rs.getString("Extra");
		String field = rs.getString("Field");
		String key = rs.getString("Key");

		column.setMark(string);
		column.setIsPK("pri".equalsIgnoreCase(key));
		column.setIsFK("mul".equalsIgnoreCase(key));
		column.setType(rs.getString("Type"));
		column.setIsNullable(rs.getString("Null").equalsIgnoreCase("YES"));
		marki++;
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    workDone();
	    try {
		if (pStemt != null) {
		    pStemt.close();
		}
	    } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    pStemt = null;
	}

	return columns;
    }

    private void constrants() {
	PreparedStatement pStemt = null;
	try {
	    pStemt = con.prepareStatement(fkSql);
	    pStemt.setString(1, dbName);
	    ResultSet rs2 = pStemt.executeQuery();
	    Map<String, String> talbeKeys = new HashMap<>();
	    while (rs2.next()) {
		String ccolumn = rs2.getString("ccolumn");
		String cTable = rs2.getString("cTable");
		String ptable = rs2.getString("ptable");
		String pcolumn = rs2.getString("pcolumn");
		if (ccolumn != null && ptable != null && pcolumn != null) {
		    talbeKeys.put(cTable + "." + ccolumn, ptable + "." + pcolumn);
		    System.out.println(cTable + "." + ccolumn + "======" + ptable + "." + pcolumn);
		}
	    }
	    constrants.putAll(talbeKeys);
	    rs2.close();
	} catch (SQLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    @Override
    public void workDone() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public List<String> showDataBases() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String run(String tableName) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Object getDbName() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public void setDbName(String dbName) {
	// TODO Auto-generated method stub
	
    }

}
package com.wldst.ruder.module.database.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * ****************************************************************
 *
 * Package: work Filename: DbTransUtil.java Description:
 * 工具类,处理数据库字段和表名转换为类名,类属性. Copyright: Copyright (c) liuqiang 2017
 * 
 * @author: liuqiang_pc
 * @version: 1.0.0 Create at: 2017年12月10日 下午9:59:18 2017年3月9日 上午10:45:55 - first
 *           revision
 *
 ****************************************************************
 */
public class DbTransUtil {

    /**
     * 功能：将输入字符串的首字母改成大写,驼峰，命名
     * 
     * @param str
     * @return
     */
    public static String initcap(String str) {
	String rename = str;
	if (rename.contains("_")) {
	    StringBuilder sbBuilder = new StringBuilder();
	    for (String stri : rename.trim().split("_")) {
		if (!"".equals(stri)) {
		    sbBuilder.append(firstBig(stri));
		}
	    }
	    ;
	    return firstBig(sbBuilder.toString());
	}
	return firstBig(rename.toLowerCase());
    }

    /**
     * 功能：将输入字符串的首字母改成大写,驼峰，命名
     * 
     * @param str
     * @return
     */
    public static String filterStart(String str, String tablePrefix) {
	String rename = str;
	if (tablePrefix.contains(",")) {
	    for (String prefixi : tablePrefix.split(",")) {
		rename = replacePrefix(prefixi, rename);
	    }
	} else {
	    rename = replacePrefix(tablePrefix, rename);
	}

	return rename;
    }

    /**
     * 替换前缀
     * 
     * @param tablePrefix
     * @param rename
     * @return
     */
    private static String replacePrefix(String tablePrefix, String rename) {
	if (rename.startsWith(tablePrefix)) {
	    rename = rename.replaceFirst(tablePrefix, "");
	}
	return rename;
    }

    /**
     * 属性名称
     * 
     * @param str
     * @return
     */
    public static String initAttrcap(String str) {
	String rename = str.toLowerCase();
	if (rename.contains("_")) {
	    StringBuilder sbBuilder = new StringBuilder();
	    int i = 0;
	    String[] splits = rename.split("_");
	    for (String stri : splits) {
		if (i > 0) {
		    if (!"".equals(stri)) {
			sbBuilder.append(firstBig(stri));
		    }
		}
		i++;
	    }
	    return splits[0] + sbBuilder.toString();
	}
	return rename;
    }

    /**
     * 首字母大写
     * 
     * @param rename
     * @return
     */
    public static String firstBig(String rename) {
	if (rename == null || rename.equals("")) {
	    return "";
	}
	char[] ch = rename.toCharArray();
	if (ch[0] >= 'a' && ch[0] <= 'z') {
	    ch[0] = (char) (ch[0] - 32);
	}

	return new String(ch);
    }
    public static String firstSmall(String rename) {
	if (rename == null || rename.equals("")) {
	    return "";
	}
	char[] ch = rename.toCharArray();
	if (ch[0] >= 'A' && ch[0] <= 'Z') {
	    ch[0] = (char) (ch[0] + 32);
	}

	return new String(ch);
    }

    /**
     * 
     * @param tabList
     */
    public static List<String> readMySqlTable(Connection conn, String dbName, String filter) {
	List<String> tabList = new ArrayList<String>();
	Statement stmt = null;
	String sql = "SHOW TABLES FROM " + dbName;
	try {
	    stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery(sql);
	    while (rs.next()) {
		String table = rs.getString(1);
		if (filter != null && !"".equals(filter)) {
		    boolean filtered = false;
		    String[] split = filter.split(",");
		    for (String si : split) {
			if (table.toLowerCase().contains(si) && table.toLowerCase().indexOf(si) == 0) {
			    filtered = true;
			}
		    }
		    if (!filtered) {
			tabList.add(table);
		    }
		} else {
		    tabList.add(table);
		}
	    }
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return tabList;
    }
    
    public static List<String> readOracleTable(Connection conn, String dbName, String filter) {
	List<String> tabList = new ArrayList<String>();
	Statement stmt = null;
	String sql ="""
		SELECT  table_name 
		FROM all_tables 
		WHERE
		 OWNER = ?
		 ORDER BY table_name
		""";
	try {
	    PreparedStatement  prestate = conn.prepareStatement(sql);
	    prestate.setString(1, dbName);
	    
	    ResultSet rs = prestate.executeQuery();
	    while (rs.next()) {
		String table = rs.getString(1);
		if (filter != null && !"".equals(filter)) {
		    boolean filtered = false;
		    String[] split = filter.split(",");
		    for (String si : split) {
			if (table.toLowerCase().contains(si) && table.toLowerCase().indexOf(si) == 0) {
			    filtered = true;
			}
		    }
		    if (!filtered) {
			tabList.add(table);
		    }
		} else {
		    tabList.add(table);
		}
	    }
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return tabList;
    }

    /**
     * 
     * @param tabList
     */
    public static List<String> readMySqlDbs(Connection conn) {
	List<String> tabList = new ArrayList<String>();
	Statement stmt = null;
	String sql = "SHOW databases";
	try {
	    stmt = conn.createStatement();
	    ResultSet rs = stmt.executeQuery(sql);
	    while (rs.next()) {
		String table = rs.getString(1);
		tabList.add(table);
	    }
	    stmt.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return tabList;
    }
    
    public static List<String> readOracleDbs(Connection conn) {
   	List<String> tabList = new ArrayList<String>();
   	Statement stmt = null;
   	String sql = "select instance_name from v$instance;";
   	try {
   	    stmt = conn.createStatement();
   	    ResultSet rs = stmt.executeQuery(sql);
   	    while (rs.next()) {
   		String table = rs.getString(1);
   		tabList.add(table);
   	    }
   	    stmt.close();
   	} catch (Exception e) {
   	    e.printStackTrace();
   	}
   	return tabList;
       }

    /**
     * 
     * @param sqlType
     * @return
     */
    public static String sqlType2JavaType(String sqlType) {
	if (sqlType.equalsIgnoreCase("bit")) {
	    return "Boolean";
	} else if (sqlType.equalsIgnoreCase("tinyint")) {
	    return "Integer";
	} else if (sqlType.equalsIgnoreCase("smallint")) {
	    return "Integer";
	} else if (sqlType.equalsIgnoreCase("int")) {
	    return "Integer";
	} else if (sqlType.equalsIgnoreCase("bigint")) {
	    return "Long";
	} else if (sqlType.equalsIgnoreCase("float")) {
	    return "Float";
	} else if (sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric")
		|| sqlType.equalsIgnoreCase("double") || sqlType.equalsIgnoreCase("real")
		|| sqlType.equalsIgnoreCase("money") || sqlType.equalsIgnoreCase("smallmoney")) {
	    return "Double";
	} else if (sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char")
		|| sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar")
		|| sqlType.equalsIgnoreCase("text") || sqlType.equalsIgnoreCase("longtext")
		|| sqlType.toLowerCase().contains("blob")) {
	    return "String";
	} else if (sqlType.equalsIgnoreCase("date") || sqlType.equalsIgnoreCase("TIME")
		|| sqlType.equalsIgnoreCase("timestamp") || sqlType.equalsIgnoreCase("timestamp with local time zone")
		|| sqlType.equalsIgnoreCase("timestamp with time zone")) {
	    return "Date";
	} else if (sqlType.equalsIgnoreCase("datetime")) {
	    return "Date";
	} else if (sqlType.equalsIgnoreCase("image")) {
	    return "Blob";
	}
	return null;
    }

    public static String sqlType2JDBCType(String sqlType) {

	if (sqlType.equalsIgnoreCase("binary_double")) {
	    return "NUMERIC";
	} else if (sqlType.equalsIgnoreCase("binary_float")) {
	    return "NUMERIC";
	} else if (sqlType.equalsIgnoreCase("blob")) {
	    return "BLOB";
	} else if (sqlType.equalsIgnoreCase("clob") || sqlType.equalsIgnoreCase("TEXT")) {
	    return "CLOB";
	} else if (sqlType.equalsIgnoreCase("int") || sqlType.equalsIgnoreCase("integer")) {
	    return "INTEGER";
	} else if (sqlType.equalsIgnoreCase("float")) {
	    return "FLOAT";
	} else if (sqlType.equalsIgnoreCase("DOUBLE")) {
	    return "DOUBLE";
	} else if (sqlType.equalsIgnoreCase("SMALLINT")) {
	    return "SMALLINT";
	} else if (sqlType.equalsIgnoreCase("TINYINT")) {
	    return "TINYINT";
	} else if (sqlType.equalsIgnoreCase("BIGINT")) {
	    return "BIGINT";
	} else if (sqlType.equalsIgnoreCase("TIME")) {
	    return "TIME";
	} else if (sqlType.equalsIgnoreCase("bit")) {
	    return "BIT";
	} else if (sqlType.equalsIgnoreCase("DATE")) {
	    return "DATE";
	} else if (sqlType.equalsIgnoreCase("REAL")) {
	    return "REAL";
	} else if (sqlType.equalsIgnoreCase("DECIMAL")) {
	    return "DECIMAL";
	} else if (sqlType.equalsIgnoreCase("char")) {
	    return "CHAR";
	} else if (sqlType.equalsIgnoreCase("nvarchar2") || sqlType.equalsIgnoreCase("varchar2")) {
	    return "VARCHAR";
	} else if (sqlType.equalsIgnoreCase("timestamp") || sqlType.equalsIgnoreCase("timestamp with local time zone")
		|| sqlType.equalsIgnoreCase("timestamp with time zone") || sqlType.equalsIgnoreCase("DATETIME")) {
	    return "TIMESTAMP";
	} else if (sqlType.equalsIgnoreCase("number")) {
	    return "NUMERIC";
	} else if (sqlType.equalsIgnoreCase("NUMERIC")) {
	    return "NUMERIC";
	}
	return "VARCHAR";
    }
    
    
    

}

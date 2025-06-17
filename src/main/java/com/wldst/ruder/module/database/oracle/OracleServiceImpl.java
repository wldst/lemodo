package com.wldst.ruder.module.database.oracle;

/**
 * POJO Product
 * 
 * @author qliu 日期：2016-10-10
 */

public class OracleServiceImpl {
//    @Autowired
//    private CrudNeo4jService neo4jService;
//    private String[] colnames; // 列名数组
//    private String[] colTypes; // 列名类型数组
//    private String[] colHeads; // 列名类型数组
//    private String[] colNullAbles; // 列名类型数组
//    private String[] colSizes; // 列名大小数组
//    private String primaryKey;
//
//    /*
//     * 构造函数
//     */
//    public OracleServiceImpl() {
//    }
//
//    public OracleServiceImpl(Map<String, Object> ds) {
//	initConnect(ds);
//    }
//
//    
//
//    @Override
//    public List<String> getTable() throws Exception {
//	if(con==null||dsId==null) {
//	    throw new Exception("数据库链接异常！");
//	}
//	List<String> tableList = new ArrayList<>();
//
//	String sql2 = "select TABLE_NAME from user_tables";
//	if(dbType.toLowerCase().equals("oracle")) {
//	    sql2 = "select TABLE_NAME from user_tables";
//	}
//	if(dbType.toLowerCase().equals("mysql")) {
//	    sql2 = "show tables";
//	}
//	
//	try (Statement createStatement = con.createStatement(); ResultSet rSet = createStatement.executeQuery(sql2);) {
//	    while (rSet.next()) {
//		tableList.add(rSet.getString(1));
//	    }
//
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	}
//	return tableList;
//    }
//
//    @Override
//    public List<Map<String, Object>> query(String query) throws Exception {
//	if(con==null||dsId==null) {
//	    throw new Exception("数据库链接异常！");
//	}
//	List<Map<String, Object>> convertList=null;
//
//	try (Statement createStatement = con.createStatement();
//		ResultSet rSet = createStatement.executeQuery(query);
//		){
//	    convertList = convertList(rSet);
//
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	}
//	return convertList;
//    }
//    
//    @Override
//    public int count(String query)  throws Exception {
//	if(con==null||dsId==null) {
//	    throw new Exception("数据库链接异常！");
//	}
//	try (Statement createStatement = con.createStatement();
//		ResultSet rSet = createStatement.executeQuery(query);
//		){
//	    rSet.next();
//	   return  rSet.getInt(1);
//
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	}
//	return 0;
//    }
//    
//    @Override
//    public List<Map<String, Object>> prepareQuery(String query,
//	    JSONObject json,List<String> keys,Map<String, String> typeMap) {
//	List<Map<String, Object>> convertList=null;
//
//	try (PreparedStatement preparedStatement= con.prepareStatement(query);
//		){
//	    int i=0;
//	    for(String key:keys) {
//		i++;
//		    String value = typeMap.get(key);
//		    if(value!=null&&typeMap.containsKey(key)&&(value.startsWith("TIMESTAMP")||value.toUpperCase().endsWith("TIME"))) {
//			java.sql.Date dateTime = new java.sql.Date(DateTool.dateStrToLong(String.valueOf(json.get(key))));
//			preparedStatement.setDate(i, dateTime );
//			continue;
//		    }
//		preparedStatement.setObject(i, json.get(key));
//		
//	    }	    
//	    ResultSet rSet = preparedStatement.executeQuery();
////	    convertList = convertList(rSet);
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	}
//	return convertList;
//    }
//    
//    @Override
//    public void prepareExcuteById(String query,
//	    String id) {
//
//	try (PreparedStatement preparedStatement= con.prepareStatement(query);
//		){
//	    int parameterCount = preparedStatement.getParameterMetaData().getParameterCount();
//	    if(parameterCount>1) {
//		String[] split = id.split(",");
//		for(int i=1;i<=parameterCount;i++) {
//			preparedStatement.setObject(i, split[i-1]);
//		    }	
//	    }else {
//		preparedStatement.setString(1, id);
//	    }
//	        
//	    preparedStatement.executeQuery();
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	}
//    }
//    
//    @Override
//    public List<Map<String, Object>> excute(String query)  throws Exception {
//	if(con==null||dsId==null) {
//	    throw new Exception("数据库链接异常！");
//	}
//	List<Map<String, Object>> convertList=null;
//
//	try (Statement createStatement = con.createStatement();
//		ResultSet rSet = createStatement.executeQuery(query);
//		){
//	    convertList = convertList(rSet);
//
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	}
//	return convertList;
//    }
//
//    
//
//    @Override
//    public void copyTableInfo(String tablei) {
//	neo4jService.saveByBody(tableMetaInfo(tablei), LABLE_TABLE);
//    }
//
//    @Override
//    protected Map<String, Object> tableMetaInfo(String tableName) {
//	Map<String, Object> dbTableInfoMap = new HashMap<>();
//	// 查要生成实体类的表
//	StringBuilder sql = new StringBuilder();
//
//	sql.append("""
//		select
//		    ut.COLUMN_NAME,--字段名称
//		    uc.comments,--字段注释
//		    ut.DATA_TYPE,--字典类型
//		    ut.DATA_LENGTH,--字典长度
//		    ut.NULLABLE--是否为空
//		from user_tab_columns  ut
//		inner JOIN user_col_comments uc
//		 on ut.TABLE_NAME  = uc.table_name and ut.COLUMN_NAME = uc.column_name
//		where  """);
//	sql.append(" ut.Table_Name='" + tableName + "' order by ut.column_name");
//	StringBuilder sqlx = new StringBuilder();
//	sqlx.append("""
//		select
//		    count(*)
//		from user_tab_columns  ut
//		inner JOIN user_col_comments uc
//		 on ut.TABLE_NAME  = uc.table_name and ut.COLUMN_NAME = uc.column_name
//		where  """);
//	sqlx.append(" ut.Table_Name='" + tableName + "'");
//	int size = 20;
//	try (Statement createStatementx = con.createStatement();
//		ResultSet tableRsx = createStatementx.executeQuery(sqlx.toString());) {
//	    tableRsx.next();
//	    size = tableRsx.getInt(1);
//	} catch (SQLException e1) {
//	    // TODO Auto-generated catch block
//	    e1.printStackTrace();
//	}
//
//	try (Statement createStatement = con.createStatement();
//		ResultSet tableRs = createStatement.executeQuery(sql.toString());) {
//
//	    colnames = new String[size];
//	    colTypes = new String[size];
//	    colHeads = new String[size];
//	    colNullAbles = new String[size];
//	    colSizes = new String[size];
//	    int i = 0;
//	    while (tableRs.next()) {
//		colnames[i] = tableRs.getString(1);
//		String commString = tableRs.getString(2);
//		if (commString == null || commString.indexOf("?") >= 0 || commString.indexOf("？") > 0) {
//		    colHeads[i] = DbTransUtil.initAttrcap(tableRs.getString(1));
//		} else {
//		    colHeads[i] = commString;
//		}
//		colTypes[i] = tableRs.getString(3);
//		colSizes[i] = tableRs.getString(4);
//		colNullAbles[i] = tableRs.getString(5);
//		i++;
//	    }
//	    dbTableInfoMap.put("dsId", dsId);
//	    dbTableInfoMap.put("tableName", tableName);
//	    dbTableInfoMap.put("columns", String.join(",", colnames));
//	    dbTableInfoMap.put("colTypes", String.join(",", colTypes));
//	    dbTableInfoMap.put("header", String.join(",", colHeads));
//	    dbTableInfoMap.put("nullAble", String.join(",", colNullAbles));
//	    dbTableInfoMap.put("colSize", String.join(",", colSizes));
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	}
//	return dbTableInfoMap;
//    }
//
//    @Override
//    public String getPrimaryKey() {
//	return primaryKey;
//    }
//
//    @Override
//    public void setPrimaryKey(String primaryKeys) {
//	this.primaryKey = primaryKeys;
//    }
//
//    public static List<Map<String, Object>> convertList(ResultSet rs) {
//	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//	try {
//	    ResultSetMetaData md = rs.getMetaData();
//	    int columnCount = md.getColumnCount();
//	    while (rs.next()) {
//		Map<String, Object> rowData = new HashMap<String, Object>();
//		for (int i = 1; i <= columnCount; i++) {
//		    rowData.put(md.getColumnName(i), rs.getObject(i));
//		}
//		list.add(rowData);
//	    }
//	} catch (SQLException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	} finally {
//	    try {
//		if (rs != null)
//		    rs.close();
//		rs = null;
//	    } catch (SQLException e) {
//		e.printStackTrace();
//	    }
//	}
//	return list;
//    }
//
//    public static Map<String, Object> convertMap(ResultSet rs) {
//	Map<String, Object> map = new java.util.TreeMap<String, Object>();
//	try {
//	    ResultSetMetaData md = rs.getMetaData();
//	    int columnCount = md.getColumnCount();
//	    while (rs.next()) {
//		for (int i = 1; i <= columnCount; i++) {
//		    map.put(md.getColumnName(i), rs.getObject(i));
//		}
//	    }
//	} catch (SQLException e) {
//	    e.printStackTrace();
//	} finally {
//	    try {
//		if (rs != null)
//		    rs.close();
//		rs = null;
//	    } catch (SQLException e) {
//		e.printStackTrace();
//	    }
//	}
//	return map;
//    }
//
//    /**
//     * 出口 TODO
//     * 
//     * @param args
//     */
//    public static void main(String[] args) {
//
//    }

}

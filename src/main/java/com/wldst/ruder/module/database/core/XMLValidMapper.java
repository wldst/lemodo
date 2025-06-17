package com.wldst.ruder.module.database.core;

import static com.wldst.ruder.module.database.util.DbTransUtil.initAttrcap;
import static com.wldst.ruder.module.database.util.DbTransUtil.initcap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import com.wldst.ruder.module.database.mysql.QueryColumnInfo;
import com.wldst.ruder.module.database.util.DbTransUtil;
/**
 * POJO Product
 * 
 * @author qliu 日期：2016-10-10
 */
public class XMLValidMapper {

	private String tablename = "VISU_APP_MEETING_FILE";// 表名
	private String domianDao = "VISU_APP_MEETING_FILE";
	private String domain = "VISU_APP_MEETING_FILE";
	private String domainAttr = "test";
	private String packageOutPath="test";
	private String entityPackage="test";
	private String surfix="Mapper";
	private String nameFilteredNoUsePrefix="Test";
	
	
	private QueryColumnInfo queryInfo;


	public String getNameFilteredNoUsePrefix() {
		return nameFilteredNoUsePrefix;
	}

	public void setNameFilteredNoUsePrefix(String nameFilteredNoUsePrefix) {
		this.nameFilteredNoUsePrefix = nameFilteredNoUsePrefix;
	}

	private String[] colnames; // 列名数组
	private String[] colTypes; // 列名类型数组
	private int[] colSizes; // 列名大小数组
	private String primaryKey;
	private String srcPath = "/src/main/java/";
	private String targetPath;
	

	
	// 数据库连接
	private static final String URL = "jdbc:oracle:thin:@127.0.0.1:1521:orcl";
	private static final String NAME = "ldkb4";
	private static final String PASS = "Ldkb#V3_0";
	private static final String DRIVER = "oracle.jdbc.OracleDriver";

	/*
	 * 构造函数
	 */
	public XMLValidMapper() {
	}

	public XMLValidMapper(String packageOutPath) {
		this.packageOutPath = packageOutPath;
	}

	private void init() {
		// 创建连接
		Connection con;
		// 查要生成实体类的表
		String sql = "select * from " + tablename;
		PreparedStatement pStemt = null;
		try {
			try {
				Class.forName(DRIVER);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			con = DriverManager.getConnection(URL, NAME, PASS);
			pStemt = con.prepareStatement(sql);
			ResultSetMetaData rsmd = pStemt.getMetaData();
			int size = rsmd.getColumnCount(); // 统计列
			colnames = new String[size];
			colTypes = new String[size];
			colSizes = new int[size];
			for (int i = 0; i < size; i++) {
				colnames[i] = rsmd.getColumnName(i + 1);
				colTypes[i] = rsmd.getColumnTypeName(i + 1);
			}
				createXml(colnames, colTypes, tablename);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			// try {
			// con.close();
			// } catch (SQLException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
	}

	public void createXml(String[] colnames, String[] colTypes,String tablename) {
		this.colnames=colnames;
		this.colTypes=colTypes;
		this.tablename = tablename;
		this.domianDao = packageOutPath+"."+initcap(nameFilteredNoUsePrefix)+surfix;
		this.domain = initcap(nameFilteredNoUsePrefix);
		this.domainAttr = initAttrcap(nameFilteredNoUsePrefix);
		String content = parse(colnames, colTypes, tablename);
		System.out.println(content);
//		try {
//			String generatorPath = getTargetPath() +File.separator+ getSrcPath();
//			String fileName = generatorPath+ File.separator
//					+packageOutPath.replace(".", File.separator)+File.separator+ initcap(nameFilteredNoUsePrefix) +  surfix+".xml";
////			System.out.println(fileName);
//			System.out.println(content);
//			FileWriter fw = new FileWriter(fileName);
//			PrintWriter pw = new PrintWriter(fw);
//			pw.println(content);
//			pw.flush();
//			pw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * 功能：生成实体类Dao的主要Mapper文件
	 * 
	 * @param colnames
	 * @param colTypes
	 * @param colSizes
	 * @return
	 */
	private String parse(String[] colnames, String[] colTypes,String tablename) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		sb.append(
				" <!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\n");

		sb.append("<mapper namespace=\"" + domianDao + "\">\n");
		baseResultMap(sb);
		sqlColumns(sb);
//		sqlViewColumns(sb);
//		selectView(sb);
//		batchUpdate(sb);
		if(primaryKey==null||"".equals(primaryKey)){
			findbySQL(sb);
			whereCondition(sb);
//			insertSQL(sb);
//			insertSelectSQL(sb);
		}else{
			findbySQL(sb);
			findByIdSQL(sb);
			whereCondition(sb);
//			insertSQL(sb);
//			insertSelectSQL(sb);
//			updateSQL(sb);
		}
		deleteBy(sb);
//		batchDelBy(sb);
		
		sb.append("	 </mapper>\n");
		return sb.toString();
	}


	private void baseResultMap(StringBuffer sb) {
		sb.append("	<resultMap id=\"BaseResultMap\" type=\""+entityPackage+"."+DbTransUtil.initcap(nameFilteredNoUsePrefix)+"\" > \n");
		for (int i = 0; i < colnames.length; i++) {
			String attri = initAttrcap(colnames[i].toLowerCase());	
		   sb.append("	 	<result column=\""+colnames[i]+"\" property=\""+attri+"\"/>\n");
		}
		sb.append("	</resultMap>");
	}
	private void sqlColumns(StringBuffer sb) {
		sb.append("<sql id=\"" + domainAttr + "Columns\">\n");
		String column=processAllColumn().trim();
		column=	column.substring(0,column.length()-1);
		sb.append(column);
		sb.append("</sql>\n");
	}
	
	private void sqlViewColumns(StringBuffer sb) {
		sb.append("<sql id=\"" + domainAttr + "ViewColumns\">\n");
		String column=processViewColumn().trim();
		column=	column.substring(0,column.length()-1);
		sb.append(column);
		sb.append("</sql>\n");
	}

	private void findbySQL(StringBuffer sb) {
		sb.append("	 <select id=\"findBy\" resultType=\""+entityPackage+"."+domain+"\" parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("	 SELECT\n");
		sb.append("	 	<include refid=\""+domainAttr+"Columns\" />\n");
		sb.append("	 FROM "+tablename+" a\n");
		sb.append("	 	<include refid=\"whereCondition\" />\n");
		sb.append("		ORDER BY "+primaryKey+" DESC\n");
		sb.append("	</select>\n\n");
	}
	
	private void selectView(StringBuffer sb) {
		sb.append("	 <select id=\"selectViewBy\" resultType=\""+entityPackage+"."+domain+"\" parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("	 SELECT\n");
		sb.append("	 	<include refid=\""+domainAttr+"ViewColumns\" />\n");
		sb.append("	 FROM "+tablename+" a\n");
		sb.append("	 	<include refid=\"whereCondition\" />\n");		
		sb.append("		ORDER BY "+primaryKey+" DESC\n");
		
		sb.append("	</select>\n\n");
	}
	
	private void batchUpdate(StringBuffer sb) {
		
		sb.append("	 <update id=\"batchUpdate\" parameterType=\"java.util.List\">\n");
		sb.append("	 update "+tablename+" a\n");
		sb.append("	 <trim prefix=\"set\" suffixOverrides=\",\">\n");
		for (int i = 0; i < colnames.length; i++) {
			String attri = initAttrcap(colnames[i].toLowerCase());				
			boolean timeAndBy = attri.equals("createBy")||attri.equals("createDate")||attri.equals("updateBy")||attri.equals("updateDate");
			if(timeAndBy){
				continue;
			}
			sb.append("	 <trim prefix=\""+colnames[i]+" = case\"	suffix=\"end,\" >\n");
			sb.append("	   <foreach collection=\"list\" item=\"cur\" index=\"index\">\n");
			sb.append("	     <if test=\"cur."+attri+" != null \">\n");
			sb.append("	      when id=#{cur.id} then  #{cur."+attri+",jdbcType="+DbTransUtil.sqlType2JDBCType(colTypes[i])+"}\n");		
			sb.append("	     </if>\n");
			sb.append("	   </foreach>\n");
			sb.append("	 </trim>\n");
		}		
		sb.append("	 </trim>\n");
		sb.append("	 WHERE id in ");
		sb.append("	 <foreach  collection=\"list\" separator=\",\" item=\"cur\" open=\"(\" close=\")\">\n");
		sb.append("	  #{cur.id,jdbcType=VARCHAR}\n");
		sb.append("	 </foreach>\n</update>\n");
	}

	private void findByIdSQL(StringBuffer sb) {
		sb.append("	 <select id=\"findById\" resultType=\""+entityPackage+"."+domain+"\" parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("	 SELECT\n");
		sb.append("	 	<include refid=\""+domainAttr+"Columns\" />\n");
		sb.append("	 FROM "+tablename+" a\n");
		for(int i=0;i<colnames.length;i++){
			if(colnames[i].equalsIgnoreCase(primaryKey)){
				sb.append("	 where "+primaryKey+"=#{"+DbTransUtil.initAttrcap(primaryKey)+",jdbcType="+DbTransUtil.sqlType2JDBCType(colTypes[i])+"}\n");
			}
		}
		sb.append("	</select>\n\n");
	}

	private void deleteBy(StringBuffer sb) {
		sb.append("	 <delete id=\"deleteBy\"  parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("	 delete \n");
		sb.append("	 FROM "+tablename+" a\n");
		sb.append("	 	<include refid=\"whereCondition\" />\n");
		sb.append("	</delete>\n\n");
	}
	
	private void batchDelBy(StringBuffer sb) {
		sb.append("	 <delete id=\"deleteBy\"  parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("	 delete \n");
		sb.append("	 FROM "+tablename+" where id in \n");
		sb.append("	 WHERE id in ");
		sb.append("	 <foreach  collection=\"list\" separator=\",\" item=\"cur\" open=\"(\" close=\")\">\n");
		sb.append("	  #{cur.id,jdbcType=VARCHAR}\n");
		sb.append("	 </foreach>\n");
		sb.append("	</delete>\n\n");
	}
	
	private void whereCondition(StringBuffer sb) {
		sb.append("	<sql id=\"whereCondition\"> \n");
		sb.append("	<where> 1=1 \n");
//		Map<String, String> queryDate2 = queryInfo.getQueryDate2();
//		Map<String, String> query = queryInfo.getQuery();
//		StringBuilder sbb = orderBy();
		
		for (int i = 0; i < colnames.length; i++) {
			String columni = colnames[i];
			String attri = initAttrcap(columni.toLowerCase());	
			
			boolean timeAndBy = attri.equals("createBy")||attri.equals("createDate")||attri.equals("updateBy")||attri.equals("updateDate");
			if(timeAndBy){
				continue;
			}
			
			sb.append("	 	<if test=\""+attri+" != null and "+attri+" != ''\">\n");
			
			String sqlType2JDBCType = DbTransUtil.sqlType2JDBCType(colTypes[i]);
			if("TIMESTAMP".equalsIgnoreCase(sqlType2JDBCType)||"DATETIME".equalsIgnoreCase(sqlType2JDBCType)){
					sb.append(" 	AND "+columni+" between DATE_FORMAT(#{"+attri+",jdbcType="+sqlType2JDBCType+"},'%Y-%m-%d00:00:00')   "
							+ " and DATE_FORMAT(#{"+attri+",jdbcType="+sqlType2JDBCType+"},'%Y-%m-%d 23:59:59')\n");
			}else{
				
					sb.append(" 	AND "+columni+" = #{"+attri+",jdbcType="+sqlType2JDBCType+"}\n");	
			}
			sb.append("	 </if>\n");
		}
		sb.append("	</where>");
//		if(!sbb.toString().isEmpty()){
//			sb.append(sbb.toString());
//		}		
		sb.append("\n</sql>");
	}

	private StringBuilder orderBy() {
		Map<String, String> queryOrder = queryInfo.getQueryOrder();
		StringBuilder sbb = new StringBuilder();
		if(queryOrder!=null&&!queryOrder.isEmpty()){
			sbb.append("order by ");
			for(String key:queryOrder.keySet()){
				String orderBY = queryOrder.get(key);
				String lowerCase = orderBY.toLowerCase();
				if(lowerCase.equals("asc")||lowerCase.equals("desc")){
					String string = sbb.toString().toLowerCase();
					if(string.contains("asc")||string.contains("desc")){
						sbb.append(",");
					}
					sbb.append(key+" "+orderBY);
				}
			}
		}
		return sbb;
	}
	
	private void insertSQL(StringBuffer sb) {
		sb.append("	<insert id=\"save\" parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("		INSERT INTO " + tablename + "(\n");		
		sb.append(getAllColumn());	
		sb.append("			) VALUES (\n");
		sb.append(getAllColumnValue());
		sb.append("			)\n");
		sb.append("		 	</insert>\n");
	}
	
	private void insertSelectSQL(StringBuffer sb) {
		sb.append("	<insert id=\"insertSelect\" parameterType=\""+entityPackage+"."+domain+"\">\n");
		sb.append("		INSERT INTO " + tablename + "(\n");		
		sb.append(getNotNullColumn());	
		sb.append("			) VALUES (\n");
		sb.append(getAllNotNullColumnValue());
		sb.append("			)\n");
		sb.append("		 	</insert>\n");
	}

	private void updateSQL(StringBuffer sb) {
		sb.append("	<update id=\"update\">\n");
		sb.append("	UPDATE " + tablename + " \n\t<set>");
		sb.append(getAllColumnSetValue());
		sb.append("</set>\n");
		appendPrimaryKey(sb);
		sb.append("		</update>\n");
	}

	private void appendPrimaryKey(StringBuffer sb) {
		if(!primaryKey.contains(",")){
			sb.append("			WHERE "+primaryKey+" = #{"+DbTransUtil.initAttrcap(primaryKey)+",jdbcType=VARCHAR}\n");
		}else{
			String[] ids = primaryKey.split(",");
			sb.append("			WHERE 1=1 ");
			for(String idi:ids){
				sb.append(" AND "+idi+" = #{"+DbTransUtil.initAttrcap(idi)+",jdbcType=VARCHAR} ");
			}
			sb.append("\n");
		}
	}

/**
 * 获取查询列
 * @return
 */
	private String processAllColumn() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {
			String attri = initAttrcap(colnames[i].toLowerCase());
			if (!colnames[i].toLowerCase().equals(attri)) {
				sb.append("\t  a." + colnames[i] + " AS "+  attri + ",\r\n");
			} else {
				sb.append("\t  a." + colnames[i] + ",\r\n");
			}
		}
		return sb.toString();
	}
	
	private String processViewColumn() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {
			String attri = initAttrcap(colnames[i].toLowerCase());
			sb.append("<if test=\"code != null and code=='"+colnames[i]+"' \">\r\n");
			sb.append("\t  a." + colnames[i] + " AS "+  attri + ",\r\n");
		}
		return sb.toString();
	}
	
	private String getAllNotNullColumnValue() {
        StringBuilder sb = new StringBuilder();
        sb.append("#{" +primaryKey + ",jdbcType=VARCHAR}\n");
		for (int i = 0; i < colnames.length; i++) {	
			if(!colnames[i].equals(primaryKey)){
				String attri = initAttrcap(colnames[i].toLowerCase());
				sb.append("<if test=\""+attri +" != null \">\r\n");
				sb.append(",#{" +attri + ",jdbcType="+DbTransUtil.sqlType2JDBCType(colTypes[i])+"}\r\n");
				sb.append("</if>\r\n");
			}
		}
		String columns=sb.toString();
//		columns=	columns.substring(0,columns.lastIndexOf(","));
		return columns;
	}
	
	private String getAllColumnValue() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {		
			String attri = initAttrcap(colnames[i].toLowerCase());
				sb.append("#{" +attri + ",jdbcType="+DbTransUtil.sqlType2JDBCType(colTypes[i])+"},\r\n");
		}
		String columns=sb.toString();
		columns=	columns.substring(0,columns.lastIndexOf(","));
		return columns;
	}
	
	private String getAllColumnSetValue() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {
			if(colnames[i].toLowerCase().equals("id")){
				continue;
			}			
			String attri = initAttrcap(colnames[i].toLowerCase());
			sb.append("<if test=\""+attri +" != null \">\r\n");
				sb.append(colnames[i]+" = #{" +attri + ",jdbcType="+DbTransUtil.sqlType2JDBCType(colTypes[i])+"},\r\n");
				sb.append("</if>\r\n");
		}
		String columns=sb.toString();
		return columns;
	}
	
	private String getNotNullColumn() {
        StringBuilder sb = new StringBuilder();
        sb.append(primaryKey+"\n");
		for (int i = 0; i < colnames.length; i++) {	
			if(!colnames[i].equals(primaryKey)){
				String attri = initAttrcap(colnames[i].toLowerCase());
				sb.append("<if test=\""+attri +" != null \">\r\n");
				sb.append(","+colnames[i]);
				sb.append("</if>\r\n");
			}
		}
		String columns=sb.toString();
//		columns=	columns.substring(0,columns.length()-1);
		return columns;
	}
	
	private String getAllColumn() {
        StringBuilder sb = new StringBuilder();
		for (int i = 0; i < colnames.length; i++) {			
				sb.append( colnames[i] + ",");
		}
		String columns=sb.toString();
		columns=	columns.substring(0,columns.length()-1);
		return columns;
	}

	public String getSurfix() {
		return surfix;
	}

	public void setSurfix(String surfix) {
		this.surfix = surfix;
	}

	public String getEntityPackage() {
		return entityPackage;
	}

	public void setEntityPackage(String entityPackage) {
		this.entityPackage = entityPackage;
	}
	public String getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(String primaryKeys) {
		this.primaryKey = primaryKeys;
	}

	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}
	public String getTargetPath() {
		return targetPath;
	}
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	/**
	 * 出口 TODO
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		new XMLValidMapper("VISU_APP_MEETING_FILE").init();

	}
	public QueryColumnInfo getQueryInfo() {
		return queryInfo;
	}

	public void setQueryInfo(QueryColumnInfo queryInfo) {
		this.queryInfo = queryInfo;
	}
	
}

package com.wldst.ruder.module.database;

import org.apache.commons.lang3.StringUtils;

/**
 * @author: lli
 * @version: 0.1
 * @Date: 2013-4-22 下午01:46:28
 */
public class OracleDatabaseMeta extends AbstractDatabaseMeta {

	/** @Fields ORACLE_PORT  */
	private static final int ORACLE_PORT = 1521;
	

	@Override
	public String getDatabaseTypeDesc() {
		return "ORACLE";
	}

	/**
	 * @return String
	 */
	@Override
	public String getDatabaseTypeDescLong() {
		return "Oracle";
	}

	/**
	 * @return int
	 */
	@Override
	public int getDatabaseType() {
		return DatabaseMeta.TYPE_DATABASE_ORACLE;
	}

	// public int[] getAccessTypeList()
	// {
	// return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE,
	// DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_OCI,
	// DatabaseMeta.TYPE_ACCESS_JNDI };
	// }
	//	
	/**
	 * @return int
	 */
	@Override
	public int getDefaultDatabasePort() {
		if (getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE)
			return ORACLE_PORT;
		return -1;
	}

	//
	// public boolean supportsAutoInc()
	// {
	// return false;
	// }
	//	
	//
	// public String getLimitClause(int nrRows)
	// {
	// return " WHERE ROWNUM <= "+nrRows;
	// }
	//	
	//	
	// public String getSQLQueryFields(String tableName)
	// {
	// return "SELECT /*+FIRST_ROWS*/ * FROM "+tableName+" WHERE ROWNUM < 1";
	// }
	//
	//	
	// public String getSQLTableExists(String tablename)
	// {
	// return getSQLQueryFields(tablename);
	// }
	//    
	// public String getSQLColumnExists(String columnname, String tablename)
	// {
	// return getSQLQueryColumnFields(columnname, tablename);
	// }
	//    
	// public String getSQLQueryColumnFields(String columnname, String
	// tableName)
	// {
	// return "SELECT /*+FIRST_ROWS*/ " + columnname + " FROM "+tableName
	// +" WHERE ROWNUM < 1";
	// }

	/**
	 * @return boolean
	 */
	public boolean needsToLockAllTables() {
		return false;
	}

	/**
	 * @return String
	 */
	@Override
	public String getDriverClass() {
		if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		} else {
			return "oracle.jdbc.driver.OracleDriver";
		}
	}

	/**
	 * @param hostname 
	 * @param port 
	 * @param databaseName 
	 * @return String
	 */
	@Override
	public String getURL(String hostname, String port, String databaseName) {
		if(getDatabaseName()!=null && getDatabaseName().startsWith("db.url=")){//数据库集群连接字符串处理
    		String dataname=getDatabaseName();
			return dataname.replace("db.url=", "");
		}
		if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
			return "jdbc:odbc:" + databaseName;
		} else if (getAccessType() == DatabaseMeta.TYPE_ACCESS_NATIVE) {
			if (databaseName != null
					&& databaseName.length() > 0
					&& (databaseName.startsWith("/") || databaseName
							.startsWith(":"))) {
				return "jdbc:oracle:thin:@" + hostname + ":" + port
						+ databaseName;
			} else if (StringUtils.isEmpty(getHostname())
					&& (StringUtils.isEmpty(getDatabasePortNumberString()) || getDatabasePortNumberString()
							.equals("-1"))) {
				return "jdbc:oracle:thin:@" + getDatabaseName();
			} else {
				return "jdbc:oracle:thin:@" + hostname + ":" + port + ":"
						+ databaseName;
			}
		} else {
			if (getDatabaseName() != null && getDatabaseName().length() > 0) {
				if (getHostname() != null && getHostname().length() > 0
						&& getDatabasePortNumberString() != null
						&& getDatabasePortNumberString().length() > 0) {
					return "jdbc:oracle:oci:@(description=(address=(host="
							+ getHostname() + ")(protocol=tcp)(port="
							+ getDatabasePortNumberString()
							+ "))(connect_data=(sid=" + getDatabaseName()
							+ ")))";
				} else {
					return "jdbc:oracle:oci:@" + getDatabaseName();
				}
			}
			// else
			// {
			// throw new
			// Exception("Unable to construct a JDBC URL: at least the database name must be specified");
			// }
		}
		return null;
	}
	//    
	//    
	// public boolean supportsOptionsInURL()
	// {
	// return false;
	// }
	//
	//	
	// public boolean supportsSequences()
	// {
	// return true;
	// }
	//
	//    
	// public String getSQLSequenceExists(String sequenceName)
	// {
	// return
	// "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '"+sequenceName.toUpperCase()+"'";
	// }
	//    
	//    
	// public String getSQLCurrentSequenceValue(String sequenceName)
	// {
	// return "SELECT "+sequenceName+".currval FROM DUAL";
	// }
	//
	//    
	// public String getSQLNextSequenceValue(String sequenceName)
	// {
	// return "SELECT "+sequenceName+".nextval FROM dual";
	// }
	//
	//
	//	
	// public boolean useSchemaNameForTableList()
	// {
	// return true;
	// }
	//
	//	
	// public boolean supportsSynonyms()
	// {
	// return true;
	// }

	@Override
	public String toCharSql_yearOfField(String field) {
		// TODO Auto-generated method stub
		return null;
	}

	//	
	// public String getCreateTableStatement(String tablename,List<ValueMeta>
	// valueMetas){
	//		
	// StringBuffer createTableSql=new StringBuffer();
	// createTableSql.append("CREATE TABLE "+tablename+"(");
	// for(int i=0;i<valueMetas.size();i++){
	// if(i<valueMetas.size()-1){
	// createTableSql.append(getFieldDefinition(valueMetas.get(i), null, null,
	// true, true, false)+",");
	// }else{
	// createTableSql.append(getFieldDefinition(valueMetas.get(i), null, null,
	// true, true, false)+")");
	// }
	//			
	// }
	// return createTableSql.toString();
	// }

	// public String getDropTableStatement(String tablename){
	//		
	// return "DROP TABLE "+tablename;
	// }

	// public String getAddColumnStatement(String tablename, ValueMeta v, String
	// tk, boolean use_autoinc, String pk, boolean semicolon)
	// {
	// return "ALTER TABLE "+tablename+" ADD ( "+getFieldDefinition(v, tk, pk,
	// use_autoinc, true, false)+" ) ";
	// }
	//
	// public String getDropColumnStatement(String tablename, ValueMeta v,
	// String tk, boolean use_autoinc, String pk, boolean semicolon)
	// {
	// return
	// "ALTER TABLE "+tablename+" DROP ( "+v.getName()+" ) "+Constants.CR;
	// }

	//
	// public String getModifyColumnStatement(String tablename, ValueMeta v,
	// String tk, boolean use_autoinc, String pk, boolean semicolon)
	// {
	// ValueMeta tmpColumn = v.clone();
	// int threeoh = v.getName().length()>=30 ? 30 : v.getName().length();
	//        
	// tmpColumn.setName(v.getName().substring(0,threeoh)+"_KTL");
	// String sql="";
	//        
	// sql+=getAddColumnStatement(tablename, tmpColumn, tk, use_autoinc, pk,
	// semicolon)+";"+Constants.CR;
	// sql+="UPDATE "+tablename+" SET "+tmpColumn.getName()+"="+v.getName()+";"+Constants.CR;
	// sql+=getDropColumnStatement(tablename, v, tk, use_autoinc, pk,
	// semicolon)+";"+Constants.CR;
	// sql+=getAddColumnStatement(tablename, v, tk, use_autoinc, pk,
	// semicolon)+";"+Constants.CR;
	// sql+="UPDATE "+tablename+" SET "+v.getName()+"="+tmpColumn.getName()+";"+Constants.CR;
	// sql+=getDropColumnStatement(tablename, tmpColumn, tk, use_autoinc, pk,
	// semicolon);
	//        
	// return sql;
	// }

	//	
	// public String getFieldDefinition(ValueMeta v, String tk, String pk,
	// boolean use_autoinc, boolean add_fieldname, boolean add_cr)
	// {
	// StringBuffer retval=new StringBuffer(128);
	//		
	// String fieldname = v.getName();
	// int length = v.getLength();
	// int precision = v.getPrecision();
	//		
	// if (add_fieldname) retval.append(fieldname).append(' ');
	//		
	// int type = v.getType();
	//		
	// Constraint constraint=v.getConstraint();
	//		
	// switch(type)
	// {
	// case ValueMeta.TYPE_DATE : retval.append("DATE"); break;
	// case ValueMeta.TYPE_TIMESTAMP :
	// retval.append("TIMESTAMP(").append(length).append(')'); break;
	// case ValueMeta.TYPE_BOOLEAN: retval.append("CHAR(1)"); break;
	// case ValueMeta.TYPE_NUMBER :
	// case ValueMeta.TYPE_BIGNUMBER:
	// retval.append("NUMBER");
	// if (length>0)
	// {
	// retval.append('(').append(length);
	// if (precision>0)
	// {
	// retval.append(", ").append(precision);
	// }
	// retval.append(')');
	// }
	// break;
	// case ValueMeta.TYPE_INTEGER:
	// retval.append("INTEGER");
	// break;
	// case ValueMeta.TYPE_STRING:
	// if (length>=DatabaseMeta.CLOB_LENGTH)
	// {
	// retval.append("CLOB");
	// }
	// else
	// {
	// if (length==1) {
	// retval.append("CHAR(1)");
	// } else if (length>0 && length<=2000)
	// {
	// retval.append("VARCHAR2(").append(length).append(')');
	// }
	// else
	// {
	// if (length<=0)
	// {
	// retval.append("VARCHAR2(2000)"); }
	// else
	// {
	// retval.append("CLOB");
	// }
	// }
	// }
	// break;
	// case ValueMeta.TYPE_BINARY: {
	// retval.append("BLOB");
	// }
	// break;
	// default:
	// retval.append(" UNKNOWN");
	// break;
	// }
	//		
	// if (add_cr) retval.append(Constants.CR);
	//		
	// switch(constraint){
	// case CONSTRAINT_NONE:retval.append("");break;
	// case CONSTRAINT_PK:retval.append(" PRIMARY KEY");break;
	// case CONSTRAINT_UNIQUE:retval.append(" UNIQUE");break;
	// case CONSTRAINT_NULL:retval.append(" NULL");break;
	// case CONSTRAINT_NOTNULL:retval.append(" NOT NULL");break;
	// }
	//		
	// return retval.toString();
	// }
	//	
	//	
	// public String[] getReservedWords()
	// {
	// return new String[]
	// {
	// "ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ARRAYLEN", "AS", "ASC",
	// "AUDIT", "BETWEEN",
	// "BY", "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS",
	// "CONNECT", "CREATE", "CURRENT", "DATE",
	// "DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE",
	// "EXCLUSIVE", "EXISTS", "FILE", "FLOAT",
	// "FOR", "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE",
	// "IN", "INCREMENT", "INDEX", "INITIAL",
	// "INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK",
	// "LONG", "MAXEXTENTS", "MINUS",
	// "MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT", "NOTFOUND", "NOWAIT",
	// "NULL", "NUMBER", "OF", "OFFLINE",
	// "ON", "ONLINE", "OPTION", "OR", "ORDER", "PCTFREE", "PRIOR",
	// "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
	// "RESOURCE", "REVOKE", "ROW", "ROWID", "ROWLABEL", "ROWNUM", "ROWS",
	// "SELECT", "SESSION", "SET", "SHARE",
	// "SIZE", "SMALLINT", "SQLBUF", "START", "SUCCESSFUL", "SYNONYM",
	// "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER",
	// "UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES",
	// "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER",
	// "WHERE", "WITH"
	// };
	// }
	//	
	//	
	// public String getSQLListOfProcedures()
	// {
	// return
	// "SELECT DISTINCT DECODE(package_name, NULL, '', package_name||'.')||object_name FROM user_arguments";
	// }
	//
	// public String getSQLLockTables(String tableNames[])
	// {
	// StringBuffer sql=new StringBuffer(128);
	// for (int i=0;i<tableNames.length;i++)
	// {
	// sql.append("LOCK TABLE ").append(tableNames[i]).append(" IN EXCLUSIVE MODE;");
	// }
	// return sql.toString();
	// }
	//    
	// public String getSQLUnlockTables(String tableNames[])
	// {
	// return null;
	// }
	//    
	// public String getExtraOptionsHelpText()
	// {
	// return
	// "http://download.oracle.com/docs/cd/B19306_01/java.102/b14355/urls.htm#i1006362";
	// }
	//
	// public String[] getUsedLibraries()
	// {
	// return new String[] { "ojdbc14.jar", "orai18n.jar" };
	// }
	//
	// @Override
	// public String toCharSql_yearOfField(String field) {
	// return "to_char(" + field + ", 'yyyy')";
	// }
	//	
	//	
	// public List<String> getOptimizeTableSQL(String tableName,boolean
	// isNologging){
	// List<String> ls = new ArrayList<String>();
	// StringBuilder sb = new StringBuilder();
	// sb.append("alter table ");
	// sb.append(tableName);
	// sb.append(" move ");
	// sb.append(isNologging?" nologging":"");
	// ls.add(sb.toString());
	// return ls;
	// }
	//    
	//  
	// public String getRebuildIndexSQL(String indexName,boolean
	// isNologging,boolean isOnLine){
	// StringBuilder sb = new StringBuilder();
	// sb.append("alter index ");
	// sb.append(indexName);
	// sb.append(" rebuild ");
	// sb.append(isNologging?" nologging":"");
	// sb.append(isOnLine?" online":"");
	// return sb.toString();
	// }
	//    
	// public String getCreateIndexSQL(List<String> indexColumnNames,String
	// indexName,String tableName,boolean isNologging){
	// StringBuilder sb = new StringBuilder();
	// sb.append(" create index ");
	// sb.append(indexName);
	// sb.append(" on ");
	// sb.append(tableName);
	// sb.append(" (");
	// sb.append(StringUtil.splitListString(indexColumnNames, ","));
	// sb.append(" )");
	// sb.append(isNologging?"nologging":"");
	// return sb.toString();
	// }
	//        
	// public String getDropIndexSQL(String indexName){
	// return "drop index "+indexName;
	// }
	//    
	// public String getIndexNamesSQLByTable(String tableName){
	// StringBuilder sb = new StringBuilder();
	// sb.append("select index_name,index_type,table_name from user_indexes where table_name=");
	// sb.append("'");
	// sb.append(tableName);
	// sb.append("'");
	// return sb.toString();
	// }
}
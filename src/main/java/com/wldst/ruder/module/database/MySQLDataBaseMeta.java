package com.wldst.ruder.module.database;

import org.apache.commons.lang3.StringUtils;
 

public class MySQLDataBaseMeta extends AbstractDatabaseMeta{

	@Override
	public int getDatabaseType() {
		return DatabaseMeta.TYPE_DATABASE_MYSQL;
	}

	@Override
	public String getDatabaseTypeDesc() {
		return "MYSQL";
	}

	@Override
	public String getDatabaseTypeDescLong() {
		return "MySql";
	}

	
	@Override
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_ODBC, DatabaseMeta.TYPE_ACCESS_JNDI };
	}
	
	@Override
	public int getDefaultDatabasePort()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE) return 3306;
		return -1;
	}
	
	public String getLimitClause(int nrRows)
	{
		return " LIMIT "+nrRows;	
	}
	
   
	public String getSQLQueryFields(String tableName)
	{
	    return "SELECT * FROM "+tableName+" LIMIT 0"; 
	}

    public String getSQLTableExists(String tablename)
    {
        return getSQLQueryFields(tablename);
    }
    
    public String getSQLColumnExists(String columnname, String tablename)
    {
        return  getSQLQueryColumnFields(columnname, tablename);
    }
    public String getSQLQueryColumnFields(String columnname, String tableName)
    {
        return "SELECT " + columnname + " FROM "+tableName +" LIMIT 0";      }
    

	@Override
	public String getDriverClass()
	{
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		}
		else
		{
			return "com.mysql.jdbc.Driver";
		}
	}

    @Override
    public String getURL(String hostname, String port, String databaseName)
    {
		if (getAccessType()==DatabaseMeta.TYPE_ACCESS_ODBC)
		{
			return "jdbc:odbc:"+databaseName;
		}
		else
		{
			
            if (StringUtils.isEmpty(port))
            {
                return "jdbc:mysql://"+hostname+"/"+databaseName+"?useUnicode=true&characterEncoding=utf-8";
            }
            else
            {
                return "jdbc:mysql://"+hostname+":"+port+"/"+databaseName+"?useUnicode=true&characterEncoding=utf-8";
            }
		}
	}
    
       public String getExtraOptionSeparator()
    {
        return "&";
    }
    
    
    public String getExtraOptionIndicator()
    {
        return "?";
    }

	
	public boolean supportsTransactions()
	{
		return false;
	}

	
	public boolean supportsBitmapIndex()
	{
		return false;
	}

	
	public boolean supportsViews()
	{
		return true;
	}
	
	
	public boolean supportsSynonyms()
	{
		return false;
	}

	
	@Override
	public String getDropTableStatement(String tablename){
		
		return "DROP TABLE "+tablename;
	}

	public String[] getReservedWords()
	{
		return new String[]
		{
			"ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT",
			"BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER",
			"CHECK", "COLLATE", "COLUMN", "CONDITION", "CONNECTION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE",
			"CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC",
			"DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV",
			"DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN",
			"FALSE", "FETCH", "FLOAT", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT", "GOTO", "GRANT", "GROUP",
			"HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER",
			"INOUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN", "KEY",
			"KEYS", "KILL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCATE", 
			"LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MATCH", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT",
			"MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE",
			"OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "POSITION", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE",
			"READ", "READS", "REAL", "REFERENCES", "REGEXP", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESTRICT", "RETURN",
			"REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW",
			"SMALLINT", "SONAME", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT",
			"SSL", "STARTING", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING",
			"TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING",
			"UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE",
			"WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL"
        };
	}
	
	
	public String getStartQuote()
	{
		return "`";
	}
	
	
	public String getEndQuote()
	{
		return "`";
	}
    
       
    public String getSQLUnlockTables(String tableName[])
    {
        return "UNLOCK TABLES"; 
    }

    
    @Override
    public boolean supportsBooleanDataType()
    {
        return false;
    }
    
    public boolean needsToLockAllTables() {
    	return true;
    }
    
    
    public String getExtraOptionsHelpText()
    {
        return "http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-configuration-properties.html";
    }

    public String[] getUsedLibraries()
    {
    	return new String[]{"mysql-connector-java-8.0.23.jar"};
    }

	@Override
	public String toCharSql_yearOfField(String field) {
		return "DATE_FORMAT(" + field + ", \"%Y\")";
	}

}

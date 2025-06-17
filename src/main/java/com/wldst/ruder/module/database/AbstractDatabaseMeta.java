package com.wldst.ruder.module.database;

import java.util.Properties;

public abstract class AbstractDatabaseMeta implements DatabaseMetaInterface, Cloneable {

    private String name;
    private int accessType;
    private String hostname;
    private String databaseName;
    private String username;
    private String password;
    private String dataTablespace;
    private String indexTablespace;
    private String port;

    private boolean changed;
    private Properties attributes;
    private long id;

    /**
     * 创建一个新的实例 AbstractDatabaseMeta.
     * 
     */
    public AbstractDatabaseMeta() {
	attributes = new Properties();
	changed = false;
    }

    /**
     * 创建一个新的实例 AbstractDatabaseMeta.
     * 
     * @param name
     * @param accessType
     * @param hostname
     * @param databaseName
     * @param username
     * @param password
     */
    public AbstractDatabaseMeta(String name, int accessType, String hostname, String databaseName, String username,
	    String password) {
	super();
	this.name = name;
	this.accessType = accessType;
	this.hostname = hostname;
	this.databaseName = databaseName;
	this.username = username;
	this.password = password;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public void setName(String name) {
	this.name = name;
    }

    @Override
    public int getAccessType() {
	return accessType;
    }

    @Override
    public void setAccessType(int accessType) {
	this.accessType = accessType;
    }

    @Override
    public String getHostname() {
	return hostname;
    }

    @Override
    public void setHostname(String hostname) {
	this.hostname = hostname;
    }

    @Override
    public String getDatabaseName() {
	return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
	this.databaseName = databaseName;
    }


    @Override
    public String getUsername() {
	return username;
    }


    @Override
    public void setUsername(String username) {
	this.username = username;
    }


    @Override
    public String getPassword() {
	return password;
    }


    @Override
    public void setPassword(String password) {
	this.password = password;
    }

    public String getDataTablespace() {
	return dataTablespace;
    }

    public void setDataTablespace(String dataTablespace) {
	this.dataTablespace = dataTablespace;
    }

    public String getIndexTablespace() {
	return indexTablespace;
    }

    public void setIndexTablespace(String indexTablespace) {
	this.indexTablespace = indexTablespace;
    }

    /**
     * @return boolean
     */
    @Override
    public boolean isChanged() {
	return changed;
    }

    /**
     * @param changed void
     */
    @Override
    public void setChanged(boolean changed) {
	this.changed = changed;
    }

    /**
     * @return Properties
     */
    public Properties getAttributes() {
	return attributes;
    }

    /**
     * @param attributes void
     */
    public void setAttributes(Properties attributes) {
	this.attributes = attributes;
    }

    /**
     * @return long
     */
    public long getId() {
	return id;
    }

    /**
     * @param id void
     */
    public void setId(long id) {
	this.id = id;
    }

    /**
     * @return DatabaseMetaInterface
     * @see java.lang.Object#clone()
     */
    @Override
    public DatabaseMetaInterface clone() {

	DatabaseMetaInterface cloneVal = null;
	try {
	    cloneVal = (DatabaseMetaInterface) super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new RuntimeException(e);
	}
	return cloneVal;
    }
    
     public void addExtraOption(String databaseTypeCode, String option,
     String value) {
    
    
     }
    
     public int[] getAccessTypeList() {
    
     return null;
     }

    
//     public String getCreateTableStatement(String tablename,
//     List<ValueMeta> valueMetas) {
//    
//     return null;
//     }
    
     public String getDropTableStatement(String tablename){
     return null;
     }

//     public String getAddColumnStatement(String tablename, ValueMeta v,
//     String tk, boolean use_autoinc, String pk, boolean semicolon) {
//    
//     return null;
//     }
    
     public String getConnectSQL() {
    
     return null;
     }

     public Properties getConnectionPoolingProperties() {
    
     return null;
     }

    @Override
    public String getDatabasePortNumberString() {

	return port;
    }

    @Override
    public abstract int getDatabaseType();

    @Override
    public abstract String getDatabaseTypeDesc();

    @Override
    public abstract String getDatabaseTypeDescLong();

    @Override
    public abstract String getDriverClass();

    public abstract String toCharSql_yearOfField(String field);

    public int getDefaultDatabasePort() {

	return 0;
    }

    // public String getDropColumnStatement(String tablename, ValueMeta v,
    // String tk, boolean use_autoinc, String pk, boolean semicolon) {
    //
    // return null;
    // }

    // public String getEndQuote() {
    //
    // return null;
    // }
    //
    // public String getExtraOptionIndicator() {
    //
    // return null;
    // }
    //
    // public String getExtraOptionSeparator() {
    //
    // return null;
    // }
    //
    // public String getExtraOptionValueSeparator() {
    //
    // return null;
    // }
    //
    // public Map<String, String> getExtraOptions() {
    //
    // return null;
    // }
    //
    // public String getExtraOptionsHelpText() {
    //
    // return null;
    // }

    // public String getFieldDefinition(ValueMeta v, String tk, String pk,
    // boolean use_autoinc, boolean add_fieldname, boolean add_cr) {
    //
    // return null;
    // }
    //
    // public String getFunctionAverage() {
    //
    // return "AVG";
    // }
    //
    // public String getFunctionCount() {
    //
    // return "COUNT";
    // }
    //
    // public String getFunctionMaximum() {
    //
    // return "MAX";
    // }
    //
    // public String getFunctionMinimum() {
    //
    // return "MIN";
    // }
    //
    // public String getFunctionSum() {
    //
    // return "SUM";
    // }
    //
    // public int getInitialPoolSize() {
    //
    // return 0;
    // }
    //
    // public String getLimitClause(int nrRows) {
    //
    // return "";
    // }
    //
    // public int getMaxTextFieldLength() {
    //
    // return DatabaseMeta.CLOB_LENGTH;
    // }
    //
    // public int getMaxVARCHARLength() {
    //
    // return 0;
    // }
    //
    // public int getMaximumPoolSize() {
    //
    // return 0;
    // }

    // public String getModifyColumnStatement(String tablename, ValueMeta v,
    // String tk, boolean use_autoinc, String pk, boolean semicolon) {
    //
    // return null;
    // }
    //
    // public int getNotFoundTK(boolean use_autoinc) {
    //
    // return 0;
    // }
    //
    // public String[] getReservedWords() {
    //
    // return null;
    // }
    //
    // public String getSQLColumnExists(String column, String tablename) {
    //
    // return null;
    // }
    //
    // public String getSQLCurrentSequenceValue(String sequenceName) {
    //
    // return null;
    // }
    //
    // public String getSQLListOfProcedures() {
    //
    // return null;
    // }
    //
    // public String getSQLLockTables(String[] tableNames) {
    //
    // return null;
    // }
    //
    // public String getSQLNextSequenceValue(String sequenceName) {
    //
    // return null;
    // }
    //
    // public String getSQLQueryFields(String tableName) {
    //
    // return null;
    // }
    //
    // public String getSQLSequenceExists(String sequenceName) {
    //
    // return null;
    // }
    //
    // public String getSQLTableExists(String tablename) {
    //
    // return null;
    // }
    //
    // public String getSQLUnlockTables(String[] tableNames) {
    //
    // return null;
    // }
    //
    // public String getSchemaTableCombination(String schema_name,
    // String table_part) {
    //
    // return null;
    // }
    //
    // public String getStartQuote() {
    //
    // return null;
    // }
    //
    // public String[] getSynonymTypes() {
    //
    // return null;
    // }
    //
    // public String[] getTableTypes() {
    //
    // return null;
    // }
    //
    // public String getTruncateTableStatement(String tableName) {
    //
    // return null;
    // }
    //
    // public String getURL(String hostname, String port, String databaseName)
    // {
    //
    // return null;
    // }
    //
    // public String[] getUsedLibraries() {
    //
    // return null;
    // }
    //
    // public String[] getViewTypes() {
    //
    // return null;
    // }
    //
    // public boolean isDefaultingToUppercase() {
    //
    // return false;
    // }
    //
    // public boolean isFetchSizeSupported() {
    //
    // return false;
    // }
    //
    // public boolean isForcingIdentifiersToLowerCase() {
    //
    // return false;
    // }
    //
    // public boolean isForcingIdentifiersToUpperCase() {
    //
    // return false;
    // }
    //
    // public boolean isQuoteAllFields() {
    //
    // return false;
    // }
    //
    // public boolean isRequiringTransactionsOnQueries() {
    //
    // return false;
    // }
    //
    // public boolean isStreamingResults() {
    //
    // return false;
    // }
    //
    // public boolean isUsingConnectionPool() {
    //
    // return false;
    // }
    //
    // public boolean isUsingDoubleDecimalAsSchemaTableSeparator() {
    //
    // return false;
    // }
    //
    // public boolean needsPlaceHolder() {
    //
    // return false;
    // }
    //
    // public boolean needsToLockAllTables() {
    //
    // return false;
    // }
    //
    // public boolean quoteReservedWords() {
    //
    // return false;
    // }
    //
    // public void setConnectSQL(String sql) {
    //
    //
    // }
    //
    // public void setConnectionPoolingProperties(Properties properties) {
    //
    //
    // }
    @Override
    public void setDatabasePortNumberString(String databasePortNumberString) {

	port = databasePortNumberString;
    }

    // public void setForcingIdentifiersToLowerCase(boolean forceLowerCase) {
    //
    //
    // }
    //
    // public void setForcingIdentifiersToUpperCase(boolean forceUpperCase) {
    //
    //
    // }
    //
    // public void setInitialPoolSize(int initalPoolSize) {
    //
    //
    // }
    //
    // public void setMaximumPoolSize(int maximumPoolSize) {
    //
    //
    // }
    //
    // public void setQuoteAllFields(boolean quoteAllFields) {
    //
    //
    // }
    //
    // public void setStreamingResults(boolean useStreaming) {
    //
    //
    // }
    //
    // public void setSupportsBooleanDataType(boolean b) {
    //
    //
    // }
    //
    // public void setUsingConnectionPool(boolean usePool) {
    //
    //
    // }
    //
    // public void setUsingDoubleDecimalAsSchemaTableSeparator(
    // boolean useDoubleDecimalSeparator) {
    //
    //
    // }
    //
    // public boolean supportsAutoInc() {
    //
    // return false;
    // }
    //
    // public boolean supportsBatchUpdates() {
    //
    // return true;
    // }
    //
    // public boolean supportsBitmapIndex() {
    //
    // return false;
    // }
    //
    public boolean supportsBooleanDataType() {

	return false;
    }
    //
    // public boolean supportsCatalogs() {
    //
    // return false;
    // }
    //
    // public boolean supportsEmptyTransactions() {
    //
    // return false;
    // }
    //
    // public boolean supportsFloatRoundingOnUpdate() {
    //
    // return false;
    // }
    //
    // public boolean supportsGetBlob() {
    //
    // return false;
    // }
    //
    // public boolean supportsOptionsInURL() {
    //
    // return false;
    // }
    //
    // public boolean supportsRepository() {
    // return false;
    // }
    //
    // public boolean supportsSchemas() {
    // return false;
    // }
    //
    // public boolean supportsSequences() {
    // return false;
    // }
    //
    // public boolean supportsSetCharacterStream() {
    // return true;
    // }
    //
    // public boolean supportsSetLong() {
    // return false;
    // }
    //
    // public boolean supportsSetMaxRows() {
    // return true;
    // }
    //
    // public boolean supportsSynonyms() {
    // return false;
    // }
    //
    // public boolean supportsTimeStampToDateConversion() {
    // return true;
    // }
    //
    // public boolean supportsTransactions() {
    // return false;
    // }
    //
    // public boolean supportsViews() {
    // return false;
    // }
    //
    // public boolean useSchemaNameForTableList() {
    // return false;
    // }
    //
    // public List<String> getOptimizeTableSQL(String tableName,boolean
    // isNologging){
    // return null;
    // }
    //
    //
    // public String getRebuildIndexSQL(String indexName,boolean isNologging,boolean
    // isOnLine){
    // return null;
    // }
    //
    // public String getCreateIndexSQL(List<String> indexColumnNames,String
    // indexName,String tableName,boolean isNologging){
    // return null;
    // }
    //
    // public String getDropIndexSQL(String indexName){
    // return null;
    // }
    //
    //
    // public String getIndexNamesSQLByTable(String tableName){
    // return null;
    // }
    //
    //

}
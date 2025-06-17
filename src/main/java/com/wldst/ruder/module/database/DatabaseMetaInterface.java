package com.wldst.ruder.module.database;

public interface DatabaseMetaInterface {
	
	/**
	 * @return  
	 * int
	 */
	int getDatabaseType();
	
	
	/**
	 * @return  
	 * String
	 */
	String getDatabaseTypeDesc();
	
	
	/**
	 * @return  
	 * String
	 */
	String getDatabaseTypeDescLong();
	
	
	/**
	 * @return  
	 * int
	 */
	int getAccessType();
	
	
	/**
	 * @param accessType  
	 * void
	 */
	void setAccessType(int accessType);
	
	
	boolean isChanged();
	
	
	void setChanged(boolean changed);
	
	
	/**
	 * @return  
	 * String
	 */
	String getName();
	
	
	/**
	 * @param name  
	 * void
	 */
	void setName(String name);
	
	
	/**
	 * @return  
	 * String
	 */
	String getDatabaseName();
	
	
	/**
	 * @param databaseName  
	 * void
	 */
	void setDatabaseName(String databaseName);
	
	
	/**
	 * @return  
	 * String
	 */
	String getDatabasePortNumberString();
	
	
	/**
	 * @param databasePortNumberString  
	 * void
	 */
	void setDatabasePortNumberString(String databasePortNumberString);

	
	/**
	 * @return  
	 * String
	 */
	String getHostname();
	
	
	/**
	 * @param hostname  
	 * void
	 */
	void setHostname(String hostname);
//	
//	
//	long getId();
//	
//	
//	void setId(long id);
	
	
	/**
	 * @return  
	 * String
	 */
	String getUsername();
	
	
	/**
	 * @param username  
	 * void
	 */
	void setUsername(String username);
	
	
	/**
	 * @return  
	 * String
	 */
	String getPassword();
	
	
	/**
	 * @param password  
	 * void
	 */
	void setPassword(String password);
//	
//	
//	String getDataTablespace();
//	
//	
//	void setDataTablespace(String data_tablespace);
//	
//	
//	String getIndexTablespace();
//
//	
//	void setIndexTablespace(String index_tablespace);
//	
//    
//    Properties getAttributes();
//   
//   
//    void setAttributes(Properties attributes);
//	
//	
//	boolean supportsSetCharacterStream();
//	
//	
//	boolean supportsAutoInc();

	
//	String getFieldDefinition(ValueMeta v, String tk, String pk, boolean use_autoinc, boolean add_fieldname, boolean add_cr);

	
//	int[] getAccessTypeList();
//
//	
//	int getDefaultDatabasePort();
//
//	
//	String getLimitClause(int nrRows);
//
//	
//	String getSQLQueryFields(String tableName);
//
//	
//	int getNotFoundTK(boolean use_autoinc);

	
	/**
	 * @return  
	 * String
	 */
	String getDriverClass();

    
	/**
	 * @param hostname 
	 * @param port 
	 * @param databaseName 
	 * @return  
	 * String
	 */
	String getURL(String hostname, String port, String databaseName);

    
//    boolean supportsSequences();
//
//	
//	String getSQLNextSequenceValue(String sequenceName);
//
//    
//    String getSQLCurrentSequenceValue(String sequenceName);
//    
//    
//    String getSQLSequenceExists(String sequenceName);
//
//	
//	boolean isFetchSizeSupported();
//
//	
//	boolean supportsTransactions();
//
//	
//	boolean supportsBitmapIndex();
//
//	
//	boolean supportsSetLong();
//
//	
//	boolean supportsSchemas();
//	
//    
//    boolean supportsCatalogs();
//
//	
//	boolean supportsEmptyTransactions();
//	
//	
//	boolean needsPlaceHolder();
//
//	
//	String getFunctionSum();
//
//	
//	String getFunctionAverage();
//
//	
//	String getFunctionMinimum();
//
//	
//	String getFunctionMaximum();
//
//	
//	String getFunctionCount();
//
//	
//	String getSchemaTableCombination(String schema_name, String table_part);
//
//	
//	int getMaxTextFieldLength();
//
//	
//	int getMaxVARCHARLength();
	
//	
//	String getCreateTableStatement(String tablename,List<ValueMeta> valueMetas);
//	
//	
//	String getDropTableStatement(String tablename);
//
//	
//	String getAddColumnStatement(String tablename, ValueMeta v, String tk, boolean use_autoinc, String pk, boolean semicolon);
//
//	
//	String getDropColumnStatement(String tablename, ValueMeta v, String tk, boolean use_autoinc, String pk, boolean semicolon);
//
//	
//	String getModifyColumnStatement(String tablename, ValueMeta v, String tk, boolean use_autoinc, String pk, boolean semicolon);

	
	/**
	 * @return  
	 * Object
	 */
	Object clone();
	
	
//	String[] getReservedWords();
//	
//	
//	boolean quoteReservedWords();
//	
//	
//	String getStartQuote();
//	
//	
//	String getEndQuote();
//	
//	
//	boolean supportsRepository();
//	
//	
//	String[] getTableTypes();
//
//	
//	String[] getViewTypes();
//
//	
//	String[] getSynonymTypes();
//	
//	
//	
//	String getSQLListOfProcedures();
//	
//	
//	
//	String getTruncateTableStatement(String tableName);
//	
//
//	
//	boolean useSchemaNameForTableList();
//	
//	
//	
//	boolean supportsViews();
//	
//	
//	boolean supportsSynonyms();
//    
//    
//    boolean supportsFloatRoundingOnUpdate();
//    
//    
//    String getSQLLockTables(String tableNames[]);
//    
//    
//    String getSQLUnlockTables(String tableNames[]);
//
//    
//    boolean supportsTimeStampToDateConversion();
//
//    
//    boolean supportsBatchUpdates();
//
//    
//    boolean supportsBooleanDataType();
//    
//    
//	void setSupportsBooleanDataType(boolean b);
//
//      
//    boolean supportsOptionsInURL();
//    
//    
//    boolean supportsGetBlob();
//
//    
//    String getConnectSQL();
//
//    
//    void setConnectSQL(String sql);
//
//    
//    boolean supportsSetMaxRows();
//
//    
//    boolean isUsingConnectionPool();
//    
//    
//    void setUsingConnectionPool(boolean usePool);
//    
//    
//    int getMaximumPoolSize();
//
//    
//    void setMaximumPoolSize(int maximumPoolSize);
//
//    
//    int getInitialPoolSize();
//    
//    
//    void setInitialPoolSize(int initalPoolSize);    
    

//    
//    String[] getUsedLibraries();

//    
//    Properties getConnectionPoolingProperties();
//
//    
//    void setConnectionPoolingProperties(Properties properties);
//
//    
//    String getSQLTableExists(String tablename);
//
//    
//    
//    String getSQLColumnExists(String column, String tablename);
//    
//    
//    boolean needsToLockAllTables();
//
//    
//    boolean isStreamingResults();
//   
//    
//    void setStreamingResults(boolean useStreaming);
//
//    
//    boolean isQuoteAllFields();
//
//    
//    void setQuoteAllFields(boolean quoteAllFields);
//    
//    
//    boolean isForcingIdentifiersToLowerCase();
//    
//    
//    void setForcingIdentifiersToLowerCase(boolean forceLowerCase);
//    
//    
//    boolean isForcingIdentifiersToUpperCase();
//    
//    
//    void setForcingIdentifiersToUpperCase(boolean forceUpperCase);
//    
//    
//    boolean isUsingDoubleDecimalAsSchemaTableSeparator();
//
//    
//    void setUsingDoubleDecimalAsSchemaTableSeparator(boolean useDoubleDecimalSeparator);
//    
//	
//	boolean isRequiringTransactionsOnQueries();
//	
//	 
//    boolean isDefaultingToUppercase();

    
//    Map<String, String> getExtraOptions();

//    
//    void addExtraOption(String databaseTypeCode, String option, String value);
//    
//    
//    String getExtraOptionSeparator();
//    
//    
//    String getExtraOptionValueSeparator();
//
//    
//    String getExtraOptionIndicator();
//    
//    
//    
//    String getExtraOptionsHelpText();
//    
//    
//    String toCharSql_yearOfField(String field);     
//    
    
//    List<String> getOptimizeTableSQL(String tableName,boolean isNologging);    
    
    
//    String getRebuildIndexSQL(String indexName,boolean isNologging,boolean isOnLine);
//    
//    
//    
//    String getCreateIndexSQL(List<String> indexColumnNames,String indexName,String tableName,boolean isNoLogging);    
//    
//    
//    String getDropIndexSQL(String indexName);
//    
//    
//    String getIndexNamesSQLByTable(String tableName);

}
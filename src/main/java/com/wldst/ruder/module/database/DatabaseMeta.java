package com.wldst.ruder.module.database;

public class DatabaseMeta {
    public static final int TYPE_DATABASE_NONE = 0;
    public static final int TYPE_DATABASE_MSSQL = 1;
    public static final int TYPE_DATABASE_ORACLE = 2;
    public static final int TYPE_DATABASE_MYSQL = 3;
    public static final int TYPE_DATABASE_SYBASE = 4;
    public static final int TYPE_DATABASE_H2 = 5;
    public static final int TYPE_DATABASE_POSTGRESQL = 6;
    public static final int TYPE_DATABASE_DB2 = 7;
    public static final int TYPE_DATABASE_DM = 8;
    public static final int TYPE_DATABASE_SYBASEIQ = 9;
    public static final int TYPE_DATABASE_GBASE = 10;
    public static final int TYPE_DATABASE_INFORMIX = 11;

    public static final int TYPE_ACCESS_NATIVE = 0;
    public static final int TYPE_ACCESS_ODBC = 1;
    public static final int TYPE_ACCESS_OCI = 2;
    public static final int TYPE_ACCESS_JNDI = 3;

    private static final String[] DBACCESSTYPECODE = { "Native", "ODBC", "OCI", "JNDI" };
    private static final String[] DBACCESSTYPEDESC = { "Native (JDBC)", "ODBC", "OCI", "JNDI", "Custom" };
    public static final int CLOB_LENGTH = 9999999;
    public static final String EMPTY_OPTIONS_STRING = "><EMPTY><";
    public static final String DB_PRODUCT_NAME_ORACLE = "Oracle";
    public static final String DB_PRODUCT_NAME_SYBASE_ASE = "ASE";
    public static final String DB_PRODUCT_NAME_SYBASE_IQ = "IQ";
    public static final String DB_PRODUCT_NAME_MYSQL = "MySQL";
    public static final String DB_PRODUCT_NAME_MSSERVER = "Microsoft SQL Server";
    public static final String DB_PRODUCT_NAME_DB2 = "DB2";
    public static final String DB_PRODUCT_NAME_H2 = "H2";
    public static final String DB_PRODUCT_NAME_POSTGRESQL = "PostgreSQL";
    public static final String DB_PRODUCT_NAME_INFORMIX = "GBaseSQL";
    public static final String DB_PRODUCT_NAME_GBASE = "InformixSQL";
    private DatabaseMetaInterface databaseMetaInterface;

    public DatabaseMeta() {
    }

    public DatabaseMeta(DatabaseMetaInterface paramDatabaseMetaInterface) {
	this.databaseMetaInterface = paramDatabaseMetaInterface;
    }

    public DatabaseMeta(String name, String type, String access, String host, String db, String port, String user,
	    String pass) {
	this.databaseMetaInterface = DatabaseMetaInterfaceFactory.getInstance(type);
	databaseMetaInterface.setName(name);
	databaseMetaInterface.setAccessType(getAccessType(access));
	databaseMetaInterface.setHostname(host);
	databaseMetaInterface.setDatabaseName(db);
	databaseMetaInterface.setDatabasePortNumberString(port);
	databaseMetaInterface.setUsername(user);
	databaseMetaInterface.setPassword(pass);
    }

    /**
     * @param dbaccess
     * @return int
     */
    public static final int getAccessType(String dbaccess) {
	int i;

	if (dbaccess == null)
	    return TYPE_ACCESS_NATIVE;

	for (i = 0; i < DBACCESSTYPECODE.length; i++) {
	    if (DBACCESSTYPECODE[i].equalsIgnoreCase(dbaccess)) {
		return i;
	    }
	}
	for (i = 0; i < DBACCESSTYPEDESC.length; i++) {
	    if (DBACCESSTYPEDESC[i].equalsIgnoreCase(dbaccess)) {
		return i;
	    }
	}

	return TYPE_ACCESS_NATIVE;
    }

    public int getAccessType() {
	return this.databaseMetaInterface.getAccessType();
    }

    public String getName() {
	return this.databaseMetaInterface.getName();
    }

    public String getURL() {
	return this.databaseMetaInterface.getURL(this.databaseMetaInterface.getHostname(),
		this.databaseMetaInterface.getDatabasePortNumberString(), this.databaseMetaInterface.getDatabaseName());
    }

    public String getUsername() {
	return this.databaseMetaInterface.getUsername();
    }

    public String getPassword() {
	return this.databaseMetaInterface.getPassword();
    }

    public String getDriverClass() {
	return this.databaseMetaInterface.getDriverClass();
    }

    public int getDatabaseType() {
	return this.databaseMetaInterface.getDatabaseType();
    }

    public String stripCR(String paramString) {
	return paramString;
    }

    public DatabaseMetaInterface getDatabaseMetaInterface() {
	return this.databaseMetaInterface;
    }

    public void setDatabaseMetaInterface(DatabaseMetaInterface paramDatabaseMetaInterface) {
	this.databaseMetaInterface = paramDatabaseMetaInterface;
    }
}
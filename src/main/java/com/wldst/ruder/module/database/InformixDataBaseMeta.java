package com.wldst.ruder.module.database;

import org.apache.commons.lang3.StringUtils;

import com.wldst.ruder.module.database.core.SystemProperties;

public class InformixDataBaseMeta extends AbstractDatabaseMeta  {
	
	private String driver_parameters=":db_locale=zh_cn.gb18030-2000;client_locale=zh_cn.gb18030-2000;NEWCODESET=gb18030,gb18030-2000,5488;";

	@Override
	public int getDatabaseType() {
		 return DatabaseMeta.TYPE_DATABASE_INFORMIX;
	}

	@Override
	public String getDatabaseTypeDesc() {
		return "INFORMIX";
	}

	@Override
	public String getDatabaseTypeDescLong() {
		return "Iinformix";
	}

	@Override
	public String getDriverClass() {
		if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
            return "sun.jdbc.odbc.JdbcOdbcDriver";
        } else {
        	return "com.informix.jdbc.IfxDriver";
        }
	}

	@Override
	public String toCharSql_yearOfField(String field) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getURL(String hostname, String port, String databaseName) {
        if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
            return "jdbc:odbc:" + databaseName;
        } else {
        	String para = SystemProperties.getInstance().getProperties("informix_driver_parameters");
        	if(StringUtils.isEmpty(para))
        		para = this.driver_parameters;
            if (StringUtils.isEmpty(port)) {
                return "jdbc:informix-sqli://" + hostname + "/" + databaseName + para;
            } else {
                return "jdbc:informix-sqli://" + hostname + ":" + port + "/" + databaseName + para;
            }
        }
    }

}

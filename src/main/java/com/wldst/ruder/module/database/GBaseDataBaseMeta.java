package com.wldst.ruder.module.database;

import org.apache.commons.lang3.StringUtils;

import com.wldst.ruder.module.database.core.SystemProperties;

public class GBaseDataBaseMeta extends AbstractDatabaseMeta  {
	
	private String driver_parameters="?db_locale=zh_cn.gb18030-2000;client_locale=zh_cn.gb18030-2000;NEWCODESET=gb18030,gb18030-2000,5488;";

	@Override
	public int getDatabaseType() {
		 return DatabaseMeta.TYPE_DATABASE_GBASE;
	}

	@Override
	public String getDatabaseTypeDesc() {
		return "GBASE";
	}

	@Override
	public String getDatabaseTypeDescLong() {
		return "Gbase";
	}

	@Override
	public String getDriverClass() {
		if (getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC) {
            return "sun.jdbc.odbc.JdbcOdbcDriver";
        } else {
        	return "com.gbase.jdbc.Driver";
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
        	String para = SystemProperties.getInstance().getProperties("gbase_driver_parameters");
        	if(StringUtils.isEmpty(para))
        		para = this.driver_parameters;
        	para="";
            if (StringUtils.isEmpty(port)) {
                return "jdbc:gbase://" + hostname + "/" + databaseName + para;
            } else {
                return "jdbc:gbase://" + hostname + ":" + port + "/" + databaseName + para;
            }
        }
    }

}

package com.wldst.ruder.module.database.service;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.database.jdbc.Column;

/**
 * 代码生成器
 * 
 * @author liuqiang
 *
 */
public interface IDataBaseCode {

	public void workDone();

	/**
	 * 获得某表的建表语句
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> getCommentByTableName(List<String> tableName) throws Exception;

	/**
	 * 返回注释信息
	 * 
	 * @param all
	 * @return
	 */

	public String parse(String all);

	/**
	 * 列举table
	 * 
	 * @return
	 */
	public List<String> showTables();
	
	/**
	 * 列举数据库
	 * 
	 * @return
	 */
	public List<String> showDataBases();

	/**
	 * 获取当前表的列列表
	 * 
	 * @param tableName
	 * @return
	 */
	public List<Column> tabeInfo(String tableName);

	/**
	 * 获得某表的建表语句
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public List<Column> getTableInfo(String table);

	/**
	 * 生成单个表的代码
	 * 
	 * @param tableName
	 * @return
	 */
	public String run(String tableName);

	/**
	 * 获取数据库名称
	 * @return
	 */
	public Object getDbName();
	/**
	 *  设置数据名称
	 * @param dbName
	 */
	public void setDbName(String dbName);

}

package com.wldst.ruder.module.database.mysql;

import java.util.Map;

/**
 * 
 * @author Fred
 *
 */
public class QueryColumnInfo {
	private Map<String,String> query;
	private Map<String,String> queryOrder;
	private Map<String,String> queryDate2;
	public Map<String, String> getQuery() {
		return query;
	}
	public void setQuery(Map<String, String> query) {
		this.query = query;
	}
	public Map<String, String> getQueryDate2() {
		return queryDate2;
	}
	public void setQueryDate2(Map<String, String> queryDate2) {
		this.queryDate2 = queryDate2;
	}
	public Map<String, String> getQueryOrder() {
		return queryOrder;
	}
	public void setQueryOrder(Map<String, String> queryOrder) {
		this.queryOrder = queryOrder;
	}

}

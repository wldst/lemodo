package com.wldst.ruder.crud;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.util.PageObject;

/**
 * 查询数据,
 * @author deeplearn96
 *
 */
public class QueryVO {
	/**
	 * 查询条件
	 */
	private JSONObject query;//查询条件
	/**
	 * 返回列数
	 */
	private String retColumns;
	/**
	 * 查询对象标签
	 */
	private String label;//查询对象标签
	
	/**
	 * 分页数据
	 */
	private PageObject page;
	

	public JSONObject getQuery() {
		return query;
	}
	public void setQuery(JSONObject query) {
		this.query = query;
	}
	public String getRetColumns() {
		return retColumns;
	}
	public void setRetColumns(String retColumns) {
		this.retColumns = retColumns;
	}
	public PageObject getPage() {
		return page;
	}
	public void setPage(PageObject page) {
		this.page = page;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
}

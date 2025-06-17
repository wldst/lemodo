package com.wldst.ruder.crud;

import java.util.Map;

/**
 * 查询数据,
 * @author deeplearn96
 *
 */
public class VO {
	/**
	 * 查询条件
	 */
	private Map<String,Object> property;//查询条件
	/**
	 * 查询对象标签
	 */
	private String label;//查询对象标签
	
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Map<String, Object> getProperty() {
		return property;
	}
	public void setProperty(Map<String, Object> property) {
		this.property = property;
	}
	
}

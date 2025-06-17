package com.wldst.ruder.util;
/**
 * 分页对象
 * @author deeplearn96
 *
 */
public class PageObject {
	private Integer pageSize;
	private Integer pageNum;
	private Integer total;


	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public Integer getPageNum() {
		return pageNum;
	}
	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}
	public Integer getTotal() {
		return total;
	}
	public void setTotal(Integer total) {
		this.total = total;
	}

}

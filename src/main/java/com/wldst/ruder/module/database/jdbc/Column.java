package com.wldst.ruder.module.database.jdbc;

import java.io.Serializable;

/**
 * 
 * @author liuqiang
 *
 */
public class Column implements Serializable{
	private String name;
	private String mark;
	private Boolean isPK;
	private Boolean isFK;
	private Boolean isNullable;
	private String type;
	private String comment;
	
	private Boolean queryMore;
	private Boolean queryDate;
	private Boolean queryDate2;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getIsPK() {
		return isPK;
	}
	public void setIsPK(Boolean isPK) {
		this.isPK = isPK;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Boolean getIsNullable() {
		return isNullable;
	}
	public void setIsNullable(Boolean isNullable) {
		this.isNullable = isNullable;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public Boolean getIsFK() {
		return isFK;
	}
	public void setIsFK(Boolean isFK) {
		this.isFK = isFK;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	@Override
	public String toString() {
		return "{\"name\":\"" + name + 
				"\", \"mark\":\"" + mark + 
				"\", \"isPK\":\"" + isPK + 
				"\", \"isFK\":\"" + isFK + 
				"\", \"isNullable\":\""	+ isNullable + 
				"\", \"type\":\"" + type +
				"\", \"comment\":\"" + comment + "\"}";
	}
	public Boolean getQueryMore() {
		return queryMore;
	}
	public void setQueryMore(Boolean queryMore) {
		this.queryMore = queryMore;
	}
	public Boolean getQueryDate() {
		return queryDate;
	}
	public void setQueryDate(Boolean queryDate) {
		this.queryDate = queryDate;
	}
	public Boolean getQueryDate2() {
		return queryDate2;
	}
	public void setQueryDate2(Boolean queryDate2) {
		this.queryDate2 = queryDate2;
	}

}

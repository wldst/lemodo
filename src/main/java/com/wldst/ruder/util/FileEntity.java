package com.wldst.ruder.util;

import java.sql.Blob;

/**
 * 文件实体类
 * 
 * @author zhaowei
 *
 */
public class FileEntity {

    /**
     * 文件名
     */
    private String name;
    /**
     * 文件类型
     */
    private String type;
    /**
     * 文件大小
     */
    private long size;
    /**
     * 文件内容
     */
    private String context;
    /**
     * 内容
     */
    private Blob content;

    /**
     * 文件地址，如果是存文件系统，则需要这个数据
     */
    private String location;
    /**
     * 图片存md5用以判断是否重复
     */
    private String md5;
    /**
     * 业务数据
     */
    private String bizdata;
    /**
     * 上传人UserID
     */
    private String creator;
    /**
     * 上传时间
     */
    private String createdate;

    public String getBizdata() {
	return bizdata;
    }

    public void setBizdata(String bizdata) {
	this.bizdata = bizdata;
    }

    public String getCreator() {
	return creator;
    }

    public void setCreator(String creator) {
	this.creator = creator;
    }

    public String getCreatedate() {
	return createdate;
    }

    public void setCreatedate(String createdate) {
	this.createdate = createdate;
    }

    public String getMd5() {
	return md5;
    }

    public void setMd5(String md5) {
	this.md5 = md5;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public long getSize() {
	return size;
    }

    public void setSize(long size) {
	this.size = size;
    }

    public String getContext() {
	return context;
    }

    public void setContext(String context) {
	this.context = context;
    }

    public String getLocation() {
	return location;
    }

    public void setLocation(String location) {
	this.location = location;
    }

    public Blob getContent() {
	return content;
    }

    public void setContent(Blob content) {
	this.content = content;
    }

}

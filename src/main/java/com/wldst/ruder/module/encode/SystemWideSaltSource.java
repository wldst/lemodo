package com.wldst.ruder.module.encode;

import org.springframework.beans.factory.InitializingBean;

public class SystemWideSaltSource implements InitializingBean {
    private String systemWideSalt;

    public SystemWideSaltSource() {
    }

    public SystemWideSaltSource(String salt) {
	this.systemWideSalt = salt;
    }

    public void setSystemWideSalt(String systemWideSalt) {
	this.systemWideSalt = systemWideSalt;
    }

    public String getSystemWideSalt() {
	return this.systemWideSalt;
    }

    @Override
    public String toString() {
	return this.systemWideSalt;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
	if (this.systemWideSalt == null || "".equals(this.systemWideSalt))
	    throw new EncodingException("systemWideSalt为空，操作出错");
    }
}

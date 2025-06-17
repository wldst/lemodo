/**
 * 
 */
package com.wldst.ruder.module.cluster;

/** @author liuqiang
 *
 * email:wldst.kerlais@gmail.com
 * qq:1721903353
 */
public enum OSEnvironment {
	x86_64("java", "32", "1.6.0_45"), 
	jdk16045x64("java", "x86_64","1.6.0_45"), 
	opendjk717025("OpenJDK", "32", "1.7.0_25"), 
	opendjk717025x64(	"OpenJDK", "x86_64", "1.7.0_25");

	private String javaprovider;
	private String javaBit;
	private String javaversion;

	OSEnvironment(String javap, String javaB, String javav) {
		javaprovider = javap;
		javaBit = javaB;
		javaversion = javav;
	}

	public String getJavaprovider() {
		return javaprovider;
	}

	public void setJavaprovider(String javaprovider) {
		this.javaprovider = javaprovider;
	}

	public String getJavaBit() {
		return javaBit;
	}

	public void setJavaBit(String javaBit) {
		this.javaBit = javaBit;
	}

	public String getJavaversion() {
		return javaversion;
	}

	public void setJavaversion(String javaversion) {
		this.javaversion = javaversion;
	}
	
	

}

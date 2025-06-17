package com.wldst.ruder.module.cluster;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.constant.UpdaterConstants;

/**
 * 
 * Copyright C 刘强个人
 * Version
 * 设计目标：版本
 * 记录版本信息，包括版本持有者的详细信息
 * 
 * @author liuqiang
 * @describe
 * @version 2015-1-31 上午9:21:56
 */
public class Version {
	private String version = "";
	private int versioncompareResult = 0;
	/**
	 * "version",
	 * "myip",
	 * "port"
	 * "cmds",{"xxx0","cmdxxxx1"}
	 * "files","['','','']":处理方式为覆盖本地文件。
	 * "dirs","['','','']"：copy目录内容到本地。
	 * "shfiles","['xxx','','']"
	 */
	private JSONObject versionDetailInfo;

	public Version(String version) {
		super();
		this.version = version;
	}

	public static String getVersion() {
		return getInstance().version;
	}

	public void setVersion(String v) {
		version = v;
	}

	public static boolean isNeedUpdate(String leastVersion, String readMyVersion) {
		if (!validateVersion(leastVersion)) {
			return false;
		}
		return getInstance().newerVersion(leastVersion, readMyVersion);
	}

	private static boolean validateVersion(String leastVersion) {
		if (leastVersion == null || leastVersion.equals("")
				|| !leastVersion.contains("\\.")) {
			return false;
		}
		return true;
	}

	public static boolean isNeedUpdate(String leastVersion) {
		if (!validateVersion(leastVersion)) {
			return false;
		}
		return getInstance().isOldThan(leastVersion);
	}

	public static Version getInstance() {
		return new Version(
				UpdaterConstants.VERSION);
	}

	public boolean newerVersion(String v1, String v2) {
		String ver1[] = v1.split("\\.");
		String ver2[] = v2.split("\\.");
		for (int i = 0; i < ver1.length; i++) {
			Integer version1i = Integer.valueOf(ver1[i]);
			Integer version2i = Integer.valueOf(ver2[i]);
			if (version1i > version2i)
				return true;
		}
		return false;
	}

	public boolean isOldThan(String v1) {
		if (v1.equals("")) {
			return false;
		}

		boolean result = false;
		if (!v1.equals("") && version.equals("")) {
			return result;
		}
		if (!v1.equals("")) {
			String ver1[] = v1.split("\\.");
			String ver2[] = version.split("\\.");
			for (int i = 0; i < ver1.length; i++) {
				Integer version1i = Integer.valueOf(ver1[i]);
				Integer version2i = Integer.valueOf(ver2[i]);
				if (version1i > version2i)
					result = true;
			}
		}

		return false;
	}

	public int compare(String v1) {
		if (v1.equals("") && version.equals("")) {
			return 0;
		}
		if (!v1.equals("") && version.equals("")) {
			return 1;
		}
		if (!v1.equals("") && !version.equals("")) {
			String ver1[] = v1.split("\\.");
			String ver2[] = version.split("\\.");
			for (int i = 0; i < ver1.length; i++) {
				Integer versioIncomeii = Integer.valueOf(ver1[i]);
				if(i+1==ver2.length&&ver2.length<ver1.length){
					return 1;
				}
				Integer versionlocal = Integer.valueOf(ver2[i]);
				if (versioIncomeii > versionlocal)
					return 1;
				if (versioIncomeii < versionlocal)
					return -1;
			}
		}
		return 0;
	}

	public JSONObject getVersionInfo() {
		if (versionDetailInfo == null) {
			return new JSONObject();
		}
		return versionDetailInfo;
	}

	public void setVersionInfo(JSONObject versionInfo) {
		this.versionDetailInfo = versionInfo;
	}

	public String toString() {
		String str = getVersion();
		if (getVersionInfo().toString().length() > 2) {
			str += "\n" + getVersionInfo().toString();
		}
		return str;
	}
}

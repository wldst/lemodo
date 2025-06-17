package com.wldst.ruder.module.cluster.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * 工具类
 * @author liuqiang
 *
 */
public class SystemConfigUtil {

	public static String getProperty(String property) {
		String str = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		URL url = loader.getResource("CONFIG_FILE");
		InputStream in = null;
		try {
			in = url.openStream();
			Properties prop = new Properties();
			prop.load(in);
			str = prop.getProperty(property).trim();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return str;
	}

	public static String[] getPropertyasStream() {
		String[] strArr = null;
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource("CONFIG_FILE");
		InputStream in = null;
		try {
			in = url.openStream();
			Properties prop = new Properties();
			prop.load(in);
			strArr = new String[1];
			strArr[0] = prop.getProperty("mq.url").trim();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return strArr;
	}
}

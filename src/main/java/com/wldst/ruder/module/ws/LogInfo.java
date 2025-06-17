package com.wldst.ruder.module.ws;

import java.util.HashMap;
import java.util.Map;

/**
 * login info
 * 
 * @author deeplearn96
 *
 */
public class LogInfo {

    private static Map<String, String> logMap = new HashMap<>();

    public static String get(String key) {
	return logMap.get(key);
    }

    public static String put(String key, String value) {
	return logMap.put(key, value);
    }
}

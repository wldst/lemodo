package com.wldst.ruder.domain;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DomainBuffer {
	private static Map<String,String> bufferCode = new HashMap<>();
	private static Map<String,Object> bufferData = new HashMap<>();
	private static Map<String,Long> keyTime = new HashMap<>();

	public static Map<String,String> getBufferCode() {
		return bufferCode;
	}
	
	public static Map<String,Object> getBufferData() {
		return bufferData;
	}

	public static void setBufferCode(Map<String,String> bufferCode) {
		DomainBuffer.bufferCode = bufferCode;
	}
	
	public static void clear() {
	    bufferCode.clear();
	    bufferData.clear();
	}
	
	public static void clear(String key) {
	    bufferData.remove(key);
	    bufferCode.remove(key);
	}
	
	public static void put(String key,Object object) {
	    bufferData.put(key,object);
		bufferCode.put(key,String.valueOf(object));
	    keyTime.put(key,Calendar.getInstance().getTimeInMillis());
	}
	
	public static Boolean isExpired(String key) {
	    return Calendar.getInstance().getTimeInMillis()-keyTime.get(key)>10*60*1000;
	}
	

}

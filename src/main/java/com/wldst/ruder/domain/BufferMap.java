package com.wldst.ruder.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.MapTool;

public class BufferMap extends MapTool{
	private static Map<String,Map<String,Object>> bufferCode = new HashMap<>();

	public static Map<String,Map<String,Object>> getBufferCode() {
		return bufferCode;
	}

	public static void setBuffer(Map<String,Map<String,Object>> bufferCode) {
		BufferMap.bufferCode = bufferCode;
	}
	
	public static void clear() {
	    bufferCode.clear();
	}
	
	public static Object get(String key) {
	    if(!bufferCode.containsKey(key)) {
		return null;
	    }
	    Map<String, Object> map = bufferCode.get(key);
	    return map.get(VALUE);
	}
	
	public static void put(String key,String value) {
	    if(!bufferCode.containsKey(key)) {
		Map<String,Object> xx = newMap();
		xx.put(VALUE, value);
		bufferCode.put(key, xx);
	    }else {
		Map<String,Object> xx = newMap();
		xx.put(VALUE, value);
		xx.put("createTime", DateTool.nowLong());
		bufferCode.put(key, xx);
	    }
	}
	
	public static void validat() {
	    Set<String> invalidate= new HashSet<>();
	    for(String key: bufferCode.keySet()) {
		Map<String, Object> data = bufferCode.get(key);
		Long longValue = longValue(data,"createTime");
		Long nowLong = DateTool.nowLong();
		long l = nowLong-longValue;
		if(l>1000*30*60*60) {
		    invalidate.add(key);
		}
	    }
	    for(String ki: invalidate) {
		bufferCode.remove(ki);
	    }
	}
	

}

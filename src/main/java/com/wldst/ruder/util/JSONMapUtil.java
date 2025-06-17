package com.wldst.ruder.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONArray;

public class JSONMapUtil {
	public static Map<String,Object> propString(JSONObject props) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> propsMap=null;
		try {
			propsMap = mapper.readValue(props.toJSONString(), Map.class);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return propsMap;
	}
	
	public static <T> T json2Object(JSONObject props,T t) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			t = (T) mapper.readValue(props.toJSONString(), t.getClass());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return t;
	}
	
	/**
	 * 将对象转换问JSONObject
	 * @param <T>
	 * @param map
	 * @return
	 */
	public static <T> JSONObject jsonObject(T map) {
		if(map.getClass().isPrimitive()||map instanceof Long||map instanceof String) {
			Map<String, Object> mapDataMap= new HashMap<>();
			mapDataMap.put("value", map);
			return JSON.parseObject(JSON.toJSONString(mapDataMap));
		}
		return JSON.parseObject(JSON.toJSONString(map));
	}
	
	private static List<Map<String,Object>> getListMap(JSONObject jsonObject, String key) {
	    List<Map<String,Object>> deplist;
	    JSONArray jsonArray = jsonObject.getJSONArray(key);
	    deplist = new ArrayList<>(jsonArray.size());
	    for(int i=0;i<jsonArray.size();i++) {
	    deplist.add(JSON.to(Map.class,jsonArray.get(i)));
	    }
	    return deplist;
	}
}

package com.wldst.ruder.module.fun.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.util.MapTool;  
  

public class DataCacheManager extends MapTool{
    final static Logger logger = LoggerFactory.getLogger(DataCacheManager.class);
     
    private Map<String,Object> dataOneMinute= newMap() ;
    private Map<String,Long> dataCacheTime= new HashMap<>() ;

     
    
    public DataCacheManager() {
	 
    }
 

     public void putData(String key, Object data) {
	 dataCacheTime.put(key,Calendar.getInstance().getTimeInMillis());
	 dataOneMinute.put(key,data);
     }
     
     public Object getData(String key) {//默认缓存读取一分钟之内的重复数据
	 Long long1 = dataCacheTime.get(key);
	 if(long1!=null) {
	     if(Calendar.getInstance().getTimeInMillis()-long1<1000*60) {
		 return dataOneMinute.get(key);
		     
		 }
	 }
	 dataOneMinute.remove(key);
	     return null;
	 
	 
     }
    
    
    
}
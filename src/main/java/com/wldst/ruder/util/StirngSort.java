package com.wldst.ruder.util;

import java.util.Arrays;
import java.util.Comparator;

public class StirngSort {
    public static String[] sort(String[] dates){
	// 使用 Comparator 接口实现日期降序排序  
	    Arrays.sort(dates, new Comparator<String>() {  
	        @Override  
	        public int compare(String s1, String s2) {  
	            return s2.compareTo(s1);  
	        }  
	    });  
	    return dates;
    }
}

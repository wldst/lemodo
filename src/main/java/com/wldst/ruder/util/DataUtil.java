package com.wldst.ruder.util;

public class DataUtil {
    public static long m = 1024 * 1024;
    
    public static long toM(long data) {
	return data/m;
    }

}

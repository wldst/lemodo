package com.wldst.ruder.module.workflow.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class NumberUtil {
    public static final int MIN_INT_VALUE = -2147483648;
    public static final long MIN_LONG_VALUE = -9223372036854775808L;

    public static final double parseDouble(String parseStr, double defaultDouble) {
	double retDouble = defaultDouble;
	try {
	    retDouble = Double.valueOf(parseStr);
	} catch (Exception ex) {
	    retDouble = defaultDouble;
	}

	return retDouble;
    }

    public static final float parseFloat(String parseStr, float defaultFloat) {
	float retFloat = defaultFloat;
	try {
	    retFloat = Float.valueOf(parseStr);
	} catch (Exception ex) {
	    retFloat = defaultFloat;
	}
	return retFloat;
    }

    public static final int parseInt(String parseStr, int defaultInt) {
	int retInt = defaultInt;
	try {
	    retInt = Integer.valueOf(parseStr);
	} catch (Exception ex) {
	    retInt = defaultInt;
	}
	return retInt;
    }

    public static final long parseLong(String parseStr, long defaultLong) {
	long retLong = defaultLong;
	try {
	    retLong = Long.valueOf(parseStr);
	} catch (Exception ex) {
	    retLong = defaultLong;
	}
	return retLong;
    }

    public static double setScale(double val, int scale) {
	return new BigDecimal(val).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static double setScale(double val) {
	return setScale(val, 2);
    }

    /**
     * 
     * 方法描述 : 随机生成5位数 创建者：zhouwei 类名： NumberUtil.java 创建时间： 2012-8-28 下午02:30:51
     * 
     * @param prefix 前缀
     * @param flag   是否需要日期组合(例:BZDH201008XXXXX)
     * @return String
     */
    public static final String getRandom(String prefix, boolean flag) {
	int i = new java.util.Random().nextInt(99999) + 10000;
	String returnRandom = "";
	if (flag == true && StringUtils.isNotBlank(prefix)) {
	    Date de = new Date();
	    SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
	    String date = df.format(de);
	    returnRandom = prefix + date + String.valueOf(i);
	} else {
	    returnRandom = prefix + String.valueOf(i);
	}
	return returnRandom;
    }

    /**
     * 将字符串数组转为long型数组，即是将里面的每个元素 从String转为long
     * 
     * @param str 字符串数组
     * @return long型数组
     */
    public static final long[] toLongArray(String[] str) {
	if (str == null) {
	    return null;
	}

	long[] l = new long[str.length];
	for (int i = 0; i < str.length; i++) {
	    // 异常由外部处理
	    l[i] = NumberUtil.parseLong(str[i], 0);
	}
	return l;
    }

    /**
     * 将字符串数组转为long型数组，即是将里面的每个元素 从String转为long
     * 
     * @param str 字符串数组
     * @return long型数组
     */
    public static final double[] toDoubleArray(String[] str) {
	if (str == null) {
	    return null;
	}

	double[] l = new double[str.length];
	for (int i = 0; i < str.length; i++) {
	    // 异常由外部处理
	    l[i] = NumberUtil.parseDouble(str[i], 0);
	}
	return l;
    }

    /**
     * 将字符串数组据转换为整数数组
     * 
     * @param str 字符串数组
     * @return 整数数组
     */
    public static final int[] toIntArray(String[] str) {
	int[] retIntArray = null;
	if (str != null && str.length > 0) {
	    retIntArray = new int[str.length];
	    for (int i = 0; i < str.length; i++) {
		retIntArray[i] = NumberUtil.parseInt(str[i], 0);
	    }
	}
	return retIntArray;
    }

}

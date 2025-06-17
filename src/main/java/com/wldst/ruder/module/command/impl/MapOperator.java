package com.wldst.ruder.module.command.impl;

import java.util.Map;

public interface MapOperator<T extends Map<String,Object>> {

    public default T play(T t) {
	System.out.println("play。。。。。");
	t.put("playT","MapOperator");
	return t;
    }

    public default T  rewind(T t) {
	System.out.println("rewind。。。。。");
	t.put("rewindT","MapOperator");
	return t;
    }
    public default T   addCreateInfo(T t) {
	System.out.println("addCreateInfo。。。。。");
	t.put("addCreateInfoT","addCreateInfo");
	return t;
    }
    public default  T  addUpdateInfo(T t) {
	System.out.println("addUpdateInfo。。。。。");
	
	t.put("addUpdateInfoT","addCreateInfo");
	return t;
    }
    public default  T  formateImg(T t) {
	System.out.println("formateImg。。。。。");
	
	t.put("formateImgT","addCreateInfo");
	return t;
    }
    public default  T  formateStatus(T t) {
	System.out.println("formateStatus。。。。。");
	t.put("formateStatusT","addCreateInfo");
	return t;
    }
    public default T formateLongTime(T t) {
	System.out.println("formateLongTime。。。。。");
	t.put("formateLongTimeT","addCreateInfo");
	return t;
    }

    public default T stop(T t) {
	System.out.println("stop。。。。。");
	t.put("stopT","addCreateInfo");
	return t;
    }

}

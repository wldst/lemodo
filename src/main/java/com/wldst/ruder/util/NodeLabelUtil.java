package com.wldst.ruder.util;

public class NodeLabelUtil {
	public static String firstValidate(String label) {
		if(label.startsWith("[")&&label.endsWith("]")) {
			label=label.replace("[", "");
			label=label.replace("]", "");
			if(label.contains(",")) {
				label=label.split(",")[0];
			}			
		}
		return label;
	}
	
	public static String[] labels(String label) {
		if(label.startsWith("[")&&label.endsWith("]")) {
			label=label.replace("[", "");
			label=label.replace("]", "");
			if(label.contains(",")) {
			    return  label.split(",");
			}			
		}
		return  label.split(",");
	}
}
